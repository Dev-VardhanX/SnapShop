package com.snapshop.app.ui.components

enum class PriceRangeFilter(val displayName: String, val minInclusive: Double?, val maxExclusive: Double?) {
    ALL("All prices", null, null),
    UNDER_1000("Under ₹1,000", null, 1_000.0),
    FROM_1000_TO_5000("₹1,000 – ₹5,000", 1_000.0, 5_000.0),
    FROM_5000_TO_10000("₹5,000 – ₹10,000", 5_000.0, 10_000.0),
    OVER_10000("₹10,000+", 10_000.0, null);

    fun matches(price: Double): Boolean {
        if (this == ALL) return true
        val minOk = minInclusive == null || price >= minInclusive
        val maxOk = maxExclusive == null || price < maxExclusive
        return minOk && maxOk
    }
}
