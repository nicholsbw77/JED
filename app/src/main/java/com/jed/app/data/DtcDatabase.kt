package com.jed.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jed.app.R
import com.jed.app.data.model.DtcDefinition
import com.jed.app.data.model.DtcSeverity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DtcDatabase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val codes: Map<String, DtcDefinition> by lazy { loadCodes() }

    fun lookup(code: String): DtcDefinition? = codes[code.uppercase()]

    fun lookupMultiple(codes: List<String>): List<DtcDefinition> {
        return codes.mapNotNull { lookup(it) }
    }

    fun search(query: String): List<DtcDefinition> {
        val q = query.uppercase()
        return codes.values.filter {
            it.code.contains(q) || it.shortDescription.uppercase().contains(q)
        }
    }

    private fun loadCodes(): Map<String, DtcDefinition> {
        val json = context.resources.openRawResource(R.raw.dtc_codes)
            .bufferedReader().use { it.readText() }

        val type = object : TypeToken<List<JsonDtcEntry>>() {}.type
        val entries: List<JsonDtcEntry> = Gson().fromJson(json, type)

        return entries.associate { entry ->
            entry.code to DtcDefinition(
                code = entry.code,
                shortDescription = entry.short,
                description = entry.desc,
                commonCauses = entry.causes,
                severity = mapSeverity(entry.severity)
            )
        }
    }

    private data class JsonDtcEntry(
        val code: String,
        val short: String,
        val desc: String,
        val causes: List<String>,
        val severity: String
    )

    companion object {
        fun mapSeverity(s: String): DtcSeverity = when (s.lowercase()) {
            "critical" -> DtcSeverity.CRITICAL
            "warning" -> DtcSeverity.WARNING
            "info" -> DtcSeverity.INFO
            else -> DtcSeverity.WARNING
        }

        fun parseDtcEntry(json: String): DtcDefinition? {
            return try {
                val entry = Gson().fromJson(json, object : TypeToken<Map<String, Any>>() {}.type) as Map<*, *>
                @Suppress("UNCHECKED_CAST")
                DtcDefinition(
                    code = entry["code"] as String,
                    shortDescription = entry["short"] as String,
                    description = entry["desc"] as String,
                    commonCauses = entry["causes"] as List<String>,
                    severity = mapSeverity(entry["severity"] as String)
                )
            } catch (e: Exception) {
                null
            }
        }
    }
}
