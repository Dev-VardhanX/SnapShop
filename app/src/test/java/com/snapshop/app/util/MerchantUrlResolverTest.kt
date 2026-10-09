package com.snapshop.app.util

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class MerchantUrlResolverTest {

    @Test
    fun `unwrap Google url parameter with encoded flipkart url`() {
        val googleUrl = "https://www.google.com/url?url=https%3A%2F%2Fwww.flipkart.com%2Fapple-iphone-15-black-128-gb%2Fp%2Fitm6ac6485515ae4%3Fpid%3DMOBGTAGPTB3VS24W&rct=j&q=&esrc=s&source=web&cd=&ved=2ahUKEwj"
        val resolved = MerchantUrlResolver.unwrapGoogleUrl(googleUrl)

        assertNotNull(resolved)
        assertTrue(resolved!!.startsWith("https://www.flipkart.com/apple-iphone-15-black-128-gb/p/itm6ac6485515ae4?pid=MOBGTAGPTB3VS24W"))
        assertFalse(resolved.contains("google.com"))
        assertFalse(resolved.contains("rct=j"))
    }

    @Test
    fun `unwrap Google adurl parameter with flipkart ad destination`() {
        val googleAdUrl = "https://www.google.com/aclk?sa=l&ai=DChcSEwiv4v-k&sig=AOD64_3n&adurl=https%3A%2F%2Fwww.flipkart.com%2Fmotorola-g85-5g-cobalt-blue-128-gb%2Fp%2Fitm0d440"
        val resolved = MerchantUrlResolver.unwrapGoogleUrl(googleAdUrl)

        assertNotNull(resolved)
        assertEquals("https://www.flipkart.com/motorola-g85-5g-cobalt-blue-128-gb/p/itm0d440", resolved)
    }

    @Test
    fun `unwrap Google q parameter when q is a full merchant URL`() {
        val googleUrl = "https://www.google.com/url?q=https://www.flipkart.com/realme-12-pro-plus-5g/p/itm123&sa=U&ved=0ahUKEw"
        val resolved = MerchantUrlResolver.unwrapGoogleUrl(googleUrl)

        assertNotNull(resolved)
        assertEquals("https://www.flipkart.com/realme-12-pro-plus-5g/p/itm123", resolved)
    }

    @Test
    fun `do not unwrap Google search q parameter when q is normal text`() {
        val googleSearchUrl = "https://www.google.com/search?q=flipkart+phone+deals"
        val resolved = MerchantUrlResolver.unwrapGoogleUrl(googleSearchUrl)

        // Should NOT extract "flipkart+phone+deals" as a URL
        assertEquals(googleSearchUrl, resolved)
    }

    @Test
    fun `already direct merchant URL is returned unmodified`() {
        val flipkartDirect = "https://www.flipkart.com/samsung-galaxy-s24-ultra-5g/p/itm987"
        val resolved = MerchantUrlResolver.resolveDirectMerchantUrl(
            rawUrl = flipkartDirect,
            source = "Flipkart",
            title = "Samsung Galaxy S24 Ultra"
        )

        assertEquals(flipkartDirect, resolved)
    }

    @Test
    fun `fallback to Flipkart direct search when url is Google Shopping comparison page`() {
        val googleShoppingPage = "https://www.google.com/shopping/product/123456789?gl=in&hl=en"
        val resolved = MerchantUrlResolver.resolveDirectMerchantUrl(
            rawUrl = googleShoppingPage,
            source = "Flipkart",
            title = "Nothing Phone (2a) (Black, 128 GB)"
        )

        assertNotNull(resolved)
        assertTrue(resolved!!.startsWith("https://www.flipkart.com/search?q="))
        assertTrue(resolved.contains("Nothing+Phone"))
        assertFalse(resolved.contains("google.com"))
    }

    @Test
    fun `fallback to Amazon direct search when source is Amazon and link is Google product page`() {
        val googleShoppingPage = "https://www.google.com/shopping/product/998877"
        val resolved = MerchantUrlResolver.resolveDirectMerchantUrl(
            rawUrl = googleShoppingPage,
            source = "Amazon.in",
            title = "OnePlus 12R 5G"
        )

        assertNotNull(resolved)
        assertTrue(resolved!!.startsWith("https://www.amazon.in/s?k="))
        assertTrue(resolved.contains("OnePlus+12R"))
    }

    @Test
    fun `handle regional google domains like google co in`() {
        val regionalGoogleUrl = "https://www.google.co.in/url?url=https%3A%2F%2Fwww.flipkart.com%2Fitem-123"
        val resolved = MerchantUrlResolver.unwrapGoogleUrl(regionalGoogleUrl)

        assertEquals("https://www.flipkart.com/item-123", resolved)
    }

    @Test
    fun `handle double encoded URLs`() {
        val doubleEncoded = "https://www.google.com/url?url=https%253A%252F%252Fwww.flipkart.com%252Fitem-abc"
        val resolved = MerchantUrlResolver.unwrapGoogleUrl(doubleEncoded)

        assertEquals("https://www.flipkart.com/item-abc", resolved)
    }

    @Test
    fun `strip Google tracking parameters from clean merchant URL`() {
        val dirtyUrl = "https://www.flipkart.com/product/123?pid=XYZ&rct=j&ved=0ahUKEwi&opi=89978449"
        val cleaned = MerchantUrlResolver.cleanMerchantUrl(dirtyUrl)

        assertEquals("https://www.flipkart.com/product/123?pid=XYZ", cleaned)
    }

    @Test
    fun `build merchant search url supports various Indian merchants and store domains`() {
        val cromaUrl = MerchantUrlResolver.buildMerchantSearchUrl("Croma", "Sony Bravia TV")
        assertEquals("https://www.croma.com/searchB?q=Sony+Bravia+TV", cromaUrl)

        val myntraUrl = MerchantUrlResolver.buildMerchantSearchUrl("Myntra", "Puma RS-X")
        assertEquals("https://www.myntra.com/Puma+RS+X", myntraUrl)

        val domainUrl = MerchantUrlResolver.buildMerchantSearchUrl("samsung.com", "Galaxy Tab S9")
        assertEquals("https://www.samsung.com/search?q=Galaxy+Tab+S9", domainUrl)
    }

    @Test
    fun `handles null, blank, and malformed URLs gracefully`() {
        assertNull(MerchantUrlResolver.unwrapGoogleUrl(null))
        assertNull(MerchantUrlResolver.unwrapGoogleUrl(""))
        assertNull(MerchantUrlResolver.resolveDirectMerchantUrl(null, null, null))
        assertFalse(MerchantUrlResolver.isGoogleUrl(null))
        assertFalse(MerchantUrlResolver.isGoogleUrl("not a url at all ://"))
    }
}
