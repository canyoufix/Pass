package com.canyoufix.autofill.service


import android.app.assist.AssistStructure
import android.net.Uri
import android.os.CancellationSignal
import android.service.autofill.*
import android.util.Log
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.canyoufix.autofill.R
import com.canyoufix.autofill.auth.AuthHelper
import com.canyoufix.crypto.SessionAESKeyHolder
import com.canyoufix.data.repository.PasswordRepository
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.util.LinkedList
import java.util.Locale

class MyAutofillService : AutofillService() {

    val passwordRepository: PasswordRepository by inject()

    override fun onCreate() {
        super.onCreate()
    }

    override fun onFillRequest(
        request: FillRequest,
        cancellationSignal: CancellationSignal,
        callback: FillCallback
    ) {
        Log.d("AutofillService", "FILL REQUEST STARTED")

        val structure = request.fillContexts.lastOrNull()?.structure
        if (structure == null) {
            callback.onFailure("No structure available")
            return
        }

        val nodes = LinkedList<AssistStructure.ViewNode>()
        for (i in 0 until structure.windowNodeCount) {
            val rootNode = structure.getWindowNodeAt(i).rootViewNode
            dumpStructure(rootNode)
            nodes.add(rootNode)
        }

        var usernameFieldId: AutofillId? = null
        var passwordFieldId: AutofillId? = null

        while (nodes.isNotEmpty()) {
            val node = nodes.poll()
            val autofillHints = node.autofillHints?.map { it.lowercase() }

            if (autofillHints?.any { it.contains("username") ||
                        it.contains("email") || it.contains("login") } == true) {
                usernameFieldId = node.autofillId
            } else if (autofillHints?.any { it.contains("password") } == true) {
                passwordFieldId = node.autofillId
            }

            for (i in 0 until node.childCount) {
                nodes.add(node.getChildAt(i))
            }
        }

        if (usernameFieldId == null && passwordFieldId == null) {
            Log.w("AutofillService", "No autofillable fields found")
            callback.onFailure("No fields found")
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val responseBuilder = FillResponse.Builder()

                if (!SessionAESKeyHolder.isAuthenticated) {
                    val lockedDataset = Dataset.Builder().apply {
                        val lockedView = RemoteViews(packageName, R.layout.autofill_locked).apply {
                            setTextViewText(R.id.locked_text, "Хранилище защищено")
                            setImageViewResource(R.id.locked_icon, R.drawable.ic_lock)
                        }

                        usernameFieldId?.let { fieldId ->
                            setValue(fieldId, AutofillValue.forText(""), lockedView)
                        }

                        passwordFieldId?.let { fieldId ->
                            setValue(fieldId, AutofillValue.forText(""), lockedView)
                        }

                        setId("AUTH_REQUIRED")
                        setAuthentication(AuthHelper.createAuthIntentSender(this@MyAutofillService))
                    }.build()

                    responseBuilder.addDataset(lockedDataset)
                }

                if (SessionAESKeyHolder.isAuthenticated) {
                    val webDomain = findDomainFromStructure(structure)
                    val accounts = passwordRepository.getAccountsByDomain(webDomain)

                    accounts.forEach { entry ->
                        val dataset = Dataset.Builder().apply {
                            val remoteViews = RemoteViews(packageName, R.layout.autofill_item).apply {
                                setTextViewText(R.id.username, entry.username)
                                setTextViewText(R.id.source, "For: ${entry.url}")
                            }

                            usernameFieldId?.let { fieldId ->
                                setValue(fieldId, AutofillValue.forText(entry.username), remoteViews)
                            }

                            passwordFieldId?.let { fieldId ->
                                setValue(fieldId, AutofillValue.forText(entry.password), createPresentation("••••••••"))
                            }
                        }.build()

                        responseBuilder.addDataset(dataset)
                    }
                }

                withContext(Dispatchers.Main) {
                    callback.onSuccess(responseBuilder.build())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    callback.onFailure(e.message ?: "Ошибка автозаполнения")
                }
            }
        }

    }

    override fun onSaveRequest(request: SaveRequest, callback: SaveCallback) {
        callback.onSuccess()
    }

    private fun createPresentation(text: String): RemoteViews {
        return RemoteViews(packageName, android.R.layout.simple_list_item_1).apply {
            setTextViewText(android.R.id.text1, text)
        }
    }

    private fun dumpStructure(node: AssistStructure.ViewNode, depth: Int = 0) {
        val indent = "  ".repeat(depth)
        Log.d(
            "AutofillStructure",
            "$indent class=${node.className}, id=${node.idEntry}, hint=${node.hint}, " +
                    "autofillHints=${node.autofillHints?.joinToString()}, " +
                    "inputType=${node.inputType}, text=${node.text}"
        )
        for (i in 0 until node.childCount) {
            dumpStructure(node.getChildAt(i), depth + 1)
        }
    }

    private fun findDomainFromStructure(structure: AssistStructure): String {
        for (i in 0 until structure.windowNodeCount) {
            val rootNode = structure.getWindowNodeAt(i).rootViewNode
            val url = findWebDomain(rootNode)
            if (url.isNotEmpty()) {
                return extractDomain(url)
            }
        }
        return ""
    }

    private fun findWebDomain(node: AssistStructure.ViewNode): String {
        node.webDomain?.let {
            Log.d("DomainFinder", "Found webDomain: $it")
            return it
        }

        when (node.idEntry) {
            "mozac_browser_toolbar_url_view", "url_bar", "address_bar", "location_bar", "omnibox" -> {
                val url = node.text?.toString() ?: ""
                if (url.isNotEmpty()) {
                    Log.d("DomainFinder", "Found URL in toolbar ($url)")
                    return url
                }
            }
        }

        if (node.className == "android.widget.EditText" && node.text != null) {
            val text = node.text.toString()
            if (text.contains("http://") || text.contains("https://") || text.contains(".")) {
                Log.d("DomainFinder", "Found URL-like text: $text")
                return text
            }
        }

        for (i in 0 until node.childCount) {
            val childDomain = findWebDomain(node.getChildAt(i))
            if (childDomain.isNotEmpty()) return childDomain
        }

        return ""
    }

    private fun extractDomain(url: String): String {
        if (url.isEmpty()) return ""
        return try {
            val fullUrl = when {
                url.startsWith("http") -> url
                url.contains("://") -> url
                else -> "https://$url"
            }

            val uri = Uri.parse(fullUrl)
            val host = uri.host ?: return ""
            host.removePrefix("www.").lowercase(Locale.getDefault())
        } catch (e: Exception) {
            Log.e("DomainExtractor", "Error parsing URL: $url", e)
            ""
        }
    }
}
