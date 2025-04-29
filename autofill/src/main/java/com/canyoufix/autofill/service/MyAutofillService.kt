package com.canyoufix.autofill.service


import android.app.assist.AssistStructure
import android.os.CancellationSignal
import android.service.autofill.*
import android.text.InputType
import android.util.Log
import android.view.View
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.canyoufix.data.repository.PasswordRepository
import kotlinx.coroutines.*
import org.koin.android.ext.android.inject
import java.util.LinkedList

class MyAutofillService : AutofillService() {

    // Объявите свойство с инъекцией
    val passwordRepository: PasswordRepository by inject()

    override fun onCreate() {
        super.onCreate()
        // Теперь passwordRepository уже доступен
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

            if (autofillHints?.any { it.contains("username") || it.contains("email") } == true) {
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

        // Теперь ищем подходящий аккаунт из базы
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val webDomain = findDomainFromStructure(structure) // Получить домен из структуры
                Log.d("AutofillService", "Detected domain: $webDomain")

                val accounts = if (webDomain.isNotEmpty()) {
                    passwordRepository.getAccountsByDomain(webDomain)
                } else {
                    emptyList()
                }

                val entry = accounts.firstOrNull()

                if (entry != null) {
                    val datasetBuilder = Dataset.Builder()

                    usernameFieldId?.let {
                        datasetBuilder.setValue(
                            it,
                            AutofillValue.forText(entry.username),
                            createPresentation(entry.username)
                        )
                    }

                    passwordFieldId?.let {
                        datasetBuilder.setValue(
                            it,
                            AutofillValue.forText(entry.password),
                            createPresentation(entry.password)
                        )
                    }

                    val dataset = datasetBuilder.build()
                    val response = FillResponse.Builder()
                        .addDataset(dataset)
                        .build()

                    withContext(Dispatchers.Main) {
                        callback.onSuccess(response)
                    }
                } else {
                    Log.w("AutofillService", "No account found for domain: $webDomain")
                    withContext(Dispatchers.Main) {
                        callback.onFailure("No account found")
                    }
                }
            } catch (e: Exception) {
                Log.e("AutofillService", "Error during autofill: ${e.message}", e)
                withContext(Dispatchers.Main) {
                    callback.onFailure("Autofill error")
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
            "$indent class=${node.className}, id=${node.idEntryName()}, hint=${node.hint}, autofillHints=${node.autofillHints?.joinToString()}, inputType=${node.inputType}"
        )
        for (i in 0 until node.childCount) {
            dumpStructure(node.getChildAt(i), depth + 1)
        }
    }

    private fun AssistStructure.ViewNode.idEntryName(): String? {
        return try {
            this.idEntry
        } catch (e: Exception) {
            null
        }
    }

    private fun findDomainFromStructure(structure: AssistStructure): String {
        for (i in 0 until structure.windowNodeCount) {
            val rootNode = structure.getWindowNodeAt(i).rootViewNode
            val url = findWebDomain(rootNode)
            if (url.isNotEmpty()) {
                return url
            }
        }
        return ""
    }

    private fun findWebDomain(node: AssistStructure.ViewNode): String {
        node.webDomain?.let { return it }
        for (i in 0 until node.childCount) {
            val childDomain = findWebDomain(node.getChildAt(i))
            if (childDomain.isNotEmpty()) {
                return childDomain
            }
        }
        return ""
    }
}
