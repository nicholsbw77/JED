package com.jed.app.data

/** Shared list of vehicle makes the app supports as generic OBD-II targets. */
object VehicleCatalog {
    val MAKES: List<String> = listOf(
        "Acura", "Audi", "BMW", "Buick", "Cadillac", "Chevrolet", "Chrysler",
        "Dodge", "Fiat", "Ford", "GMC", "Honda", "Hyundai", "Infiniti", "Jeep",
        "Kia", "Land Rover", "Lexus", "Lincoln", "Mazda", "Mercedes-Benz",
        "Mini", "Mitsubishi", "Nissan", "Porsche", "Ram", "Subaru", "Tesla",
        "Toyota", "Volkswagen", "Volvo", "Other"
    )

    private val FORD_FAMILY = setOf("Ford", "Lincoln", "Mercury")
    private val GM_FAMILY = setOf("Chevrolet", "GMC", "Buick", "Cadillac", "Pontiac", "Saturn", "Oldsmobile")
    private val VAG_FAMILY = setOf("Volkswagen", "Audi", "Skoda", "SEAT", "Porsche")
    private val BMW_FAMILY = setOf("BMW", "Mini")

    fun isFord(make: String) = make in FORD_FAMILY
    fun isGm(make: String) = make in GM_FAMILY
    fun isVag(make: String) = make in VAG_FAMILY
    fun isBmw(make: String) = make in BMW_FAMILY
}
