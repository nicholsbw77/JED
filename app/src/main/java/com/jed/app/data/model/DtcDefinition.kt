package com.jed.app.data.model

data class DtcDefinition(
    val code: String,
    val shortDescription: String,
    val description: String,
    val commonCauses: List<String>,
    val severity: DtcSeverity
)

enum class DtcSeverity(val label: String) {
    CRITICAL("Critical"),
    WARNING("Warning"),
    INFO("Informational")
}
