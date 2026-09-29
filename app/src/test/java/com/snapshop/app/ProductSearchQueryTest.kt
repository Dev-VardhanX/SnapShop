package com.snapshop.app

import com.snapshop.app.data.remote.SerpApiProductDto
import com.snapshop.app.data.remote.SerpApiResponseDto
import com.snapshop.app.domain.RecognizedProductInfo
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ProductSearchQueryTest {

    @Test
    fun `gemini provided search queries are prioritized and sanitized`() {
        val info = RecognizedProductInfo(
            productTitle = "Apple AirPods Pro (2nd Generation)",
            brand = "Apple",
            model = "AirPods Pro 2",
            searchQueries = listOf(
                "Apple AirPods Pro 2nd Gen",
                "Apple AirPods Pro 2",
                "Apple Wireless Earbuds"
            )
        )

        val queries = info.generateSearchQueries()
        assertTrue(queries.isNotEmpty())
        assertEquals("Apple AirPods Pro 2nd Gen", queries[0])
        assertEquals("Apple AirPods Pro 2", queries[1])
    }

    @Test
    fun `algorithmic query avoids brand duplication when model already includes brand`() {
        val info = RecognizedProductInfo(
            brand = "Logitech",
            model = "Logitech MX Master 3S",
            productType = "Wireless Mouse",
            searchQueries = emptyList()
        )

        val queries = info.generateSearchQueries()
        assertTrue(queries.isNotEmpty())
        // Should NOT be "Logitech Logitech MX Master 3S"
        val primary = queries[0]
        assertFalse(primary.contains("Logitech Logitech"))
        assertTrue(primary.contains("Logitech MX Master 3S"))
    }

    @Test
    fun `fashion product includes target audience and color`() {
        val info = RecognizedProductInfo(
            category = "Fashion",
            productType = "Denim Jacket",
            color = "Blue",
            targetAudience = "Men",
            searchQueries = emptyList()
        )

        val queries = info.generateSearchQueries()
        assertTrue(queries.isNotEmpty())
        val primary = queries[0]
        assertTrue(primary.contains("Men", ignoreCase = true))
        assertTrue(primary.contains("Denim Jacket", ignoreCase = true))
    }

    @Test
    fun `repeated words are deduplicated in queries`() {
        val info = RecognizedProductInfo(
            searchQueries = listOf("Nike Nike Running Shoes Shoes")
        )

        val queries = info.generateSearchQueries()
        assertEquals("Nike Running Shoes", queries[0])
    }

    @Test
    fun `serpapi response dto handles inline shopping results fallback`() {
        val item = SerpApiProductDto(
            productId = "123",
            title = "Sample Sneaker",
            price = "₹2,499"
        )
        val dtoWithInline = SerpApiResponseDto(
            shoppingResults = null,
            inlineShoppingResults = listOf(item)
        )

        assertEquals(1, dtoWithInline.allShoppingResults.size)
        assertEquals("Sample Sneaker", dtoWithInline.allShoppingResults[0].title)
    }
}
