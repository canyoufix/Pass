package com.canyoufix.autofill.service

import android.app.assist.AssistStructure
import android.content.Context
import android.content.IntentSender
import android.service.autofill.Dataset
import android.service.autofill.FillResponse
import android.view.autofill.AutofillId
import android.view.autofill.AutofillValue
import android.widget.RemoteViews
import com.canyoufix.domain.model.PasswordEntry
import com.canyoufix.domain.model.websiteDomain
import com.canyoufix.pass.R
import java.util.Locale

/** Only framework webDomain metadata is used; text fields/toolbars are never read as origins. */
data class AutofillFields(
    val username: AutofillId?,
    val password: AutofillId?,
    val domain: String,
) {
    val ids: Array<AutofillId>
        get() = listOfNotNull(username, password).toTypedArray()
}

object AutofillResponseFactory {
    fun fields(structure: AssistStructure): AutofillFields? {
        val nodes = ArrayDeque<AssistStructure.ViewNode>()
        repeat(structure.windowNodeCount) { nodes.add(structure.getWindowNodeAt(it).rootViewNode) }
        var username: AutofillId? = null
        var password: AutofillId? = null
        val domains = mutableSetOf<String>()
        while (nodes.isNotEmpty()) {
            val node = nodes.removeFirst()
            node.webDomain?.let { domain -> websiteDomain(domain)?.let(domains::add) }
            val hints = node.autofillHints.orEmpty().map { it.lowercase(Locale.ROOT) }
            if (hints.any { it in setOf("username", "emailaddress", "email") })
                username = node.autofillId
            if (hints.any { it == "password" }) password = node.autofillId
            repeat(node.childCount) { nodes.add(node.getChildAt(it)) }
        }
        if ((username == null && password == null) || domains.size != 1) return null
        return AutofillFields(username, password, domains.single())
    }

    @Suppress("DEPRECATION")
    fun locked(context: Context, fields: AutofillFields, auth: IntentSender): FillResponse =
        FillResponse.Builder()
            .setAuthentication(
                fields.ids,
                auth,
                RemoteViews(context.packageName, R.layout.autofill_locked),
            )
            .build()

    @Suppress("DEPRECATION")
    fun unlocked(
        context: Context,
        fields: AutofillFields,
        entries: List<PasswordEntry>,
    ): FillResponse? {
        if (entries.isEmpty()) return null
        val builder = FillResponse.Builder()
        entries.forEach { entry ->
            val presentation =
                RemoteViews(context.packageName, R.layout.autofill_item).apply {
                    setTextViewText(R.id.username, entry.username)
                    setTextViewText(R.id.source, entry.title)
                }
            val dataset = Dataset.Builder().setId(entry.id)
            fields.username?.let {
                dataset.setValue(it, AutofillValue.forText(entry.username), presentation)
            }
            fields.password?.let {
                dataset.setValue(it, AutofillValue.forText(entry.password), presentation)
            }
            builder.addDataset(dataset.build())
        }
        return builder.build()
    }
}
