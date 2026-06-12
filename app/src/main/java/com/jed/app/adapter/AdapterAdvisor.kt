package com.jed.app.adapter

import com.jed.app.data.VehicleCatalog
import com.jed.app.transport.AdapterType

enum class RecommendationTier(val label: String) {
    BEST("Best choice"),
    GOOD("Solid option"),
    BUDGET("Budget pick"),
    AVOID("Avoid")
}

data class AdapterRecommendation(
    val product: String,
    val tier: RecommendationTier,
    val links: List<AdapterType>,
    val approxPrice: String,
    val why: String
)

data class AdapterGuide(
    val headline: String,
    val notes: List<String>,
    val recommendations: List<AdapterRecommendation>
)

/**
 * Suggests which ELM327-style adapter to buy for a given vehicle. Higher-end
 * adapters are only worth it when a make uses extra buses (Ford MS-CAN, GM
 * GMLAN single-wire) for its non-emissions modules; for plain live data and
 * trouble codes on any 1996+ vehicle, a genuine basic ELM327 is enough.
 */
object AdapterAdvisor {

    private val GENERAL_NOTES = listOf(
        "Any genuine ELM327 reads live data and trouble codes on any 1996+ (OBD-II) vehicle.",
        "Avoid the $5–10 'ELM327 v2.1' clones — most are counterfeit and flaky. A real v1.5 chip is more reliable.",
        "Wi-Fi adapters need no pairing but you must join their Wi-Fi network, which drops your phone's internet. Bluetooth (Classic or LE) keeps your data connection."
    )

    fun recommendFor(make: String, year: Int): AdapterGuide {
        val recs = mutableListOf<AdapterRecommendation>()
        val notes = mutableListOf<String>()
        val headline: String

        when {
            VehicleCatalog.isFord(make) -> {
                headline = "$make needs MS-CAN access for full coverage"
                notes += "Ford routes ABS, airbag, body and (on many models) PATS over a separate MS-CAN bus. Cheap adapters only see the HS-CAN/emissions bus."
                notes += "Pair a capable adapter with the free FORScan app for the deepest Ford diagnostics."
                recs += AdapterRecommendation(
                    "OBDLink MX+", RecommendationTier.BEST,
                    listOf(AdapterType.BLUETOOTH_CLASSIC),
                    "~\$100",
                    "Switches to Ford MS-CAN automatically; fast and well-supported by FORScan."
                )
                recs += AdapterRecommendation(
                    "Vgate vLinker MC+ / FS", RecommendationTier.GOOD,
                    listOf(AdapterType.BLUETOOTH_CLASSIC, AdapterType.BLUETOOTH_LE),
                    "~\$35–50",
                    "FORScan-friendly with an HS/MS-CAN switch; great value for Ford owners."
                )
            }
            VehicleCatalog.isGm(make) -> {
                headline = "$make uses GMLAN single-wire CAN for body modules"
                notes += "GM puts many comfort/body modules on a single-wire CAN (GMLAN) bus that basic adapters can't reach."
                recs += AdapterRecommendation(
                    "OBDLink MX+", RecommendationTier.BEST,
                    listOf(AdapterType.BLUETOOTH_CLASSIC),
                    "~\$100",
                    "Supports GM single-wire CAN (GMLAN) plus standard buses — the most complete GM option."
                )
                recs += AdapterRecommendation(
                    "OBDLink LX", RecommendationTier.GOOD,
                    listOf(AdapterType.BLUETOOTH_CLASSIC),
                    "~\$60",
                    "Reliable for engine/trans live data and codes; no single-wire CAN."
                )
            }
            VehicleCatalog.isBmw(make) -> {
                headline = "$make pairs best with a BLE adapter and BMW apps"
                notes += "BimmerCode / BimmerLink (for coding and deep diagnostics) recommend specific adapters."
                recs += AdapterRecommendation(
                    "OBDLink CX", RecommendationTier.BEST,
                    listOf(AdapterType.BLUETOOTH_LE),
                    "~\$60",
                    "BLE adapter purpose-built for BMW/Mini coding apps; works for generic OBD-II here too."
                )
            }
            VehicleCatalog.isVag(make) -> {
                headline = "$make (VW/Audi group) works with a quality general adapter"
                notes += "For dealer-level VAG functions, VCDS-style tools go deeper, but a good ELM327 covers live data and codes."
                recs += AdapterRecommendation(
                    "OBDLink MX+", RecommendationTier.BEST,
                    listOf(AdapterType.BLUETOOTH_CLASSIC),
                    "~\$100",
                    "Broad protocol support and reliable on VAG vehicles."
                )
            }
            else -> {
                headline = "A genuine basic ELM327 covers $make"
                notes += "For live data, trouble codes and emissions readiness, any reliable ELM327 is enough."
            }
        }

        // Universally reliable picks offered to everyone.
        if (recs.none { it.product == "OBDLink LX" }) {
            recs += AdapterRecommendation(
                "OBDLink LX", RecommendationTier.GOOD,
                listOf(AdapterType.BLUETOOTH_CLASSIC),
                "~\$60",
                "Rock-solid genuine adapter for everyday live data and code reading."
            )
        }
        recs += AdapterRecommendation(
            "Veepeak OBDCheck BLE+", RecommendationTier.BUDGET,
            listOf(AdapterType.BLUETOOTH_LE),
            "~\$20–30",
            "Inexpensive, genuine BLE adapter; fine for live data and codes."
        )
        recs += AdapterRecommendation(
            "Generic Wi-Fi ELM327 (genuine v1.5)", RecommendationTier.BUDGET,
            listOf(AdapterType.WIFI),
            "~\$15–25",
            "Cheapest way to go wireless; pick a seller with good reviews to avoid clones."
        )
        recs += AdapterRecommendation(
            "$5 'ELM327 v2.1' clone", RecommendationTier.AVOID,
            listOf(AdapterType.BLUETOOTH_CLASSIC, AdapterType.WIFI),
            "~\$5",
            "Counterfeit firmware, dropped connections and missing protocols. Skip it."
        )

        val yearNote = if (year < 1996) {
            "Heads up: $year is pre-OBD-II. This app and ELM327 adapters need a 1996-or-newer vehicle."
        } else null

        return AdapterGuide(
            headline = headline,
            notes = (listOfNotNull(yearNote) + notes + GENERAL_NOTES),
            recommendations = recs
        )
    }
}
