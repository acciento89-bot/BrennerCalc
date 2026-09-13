package de.kamilunavo.brennercalc

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class StoreScreenshotModeTest {
    @Test
    fun hidesBillingStatusOnlyForStoreScreenshots() {
        assertEquals("de.kamilunavo.brennercalc.STORE_SCREENSHOTS", storeScreenshotsExtra("de.kamilunavo.brennercalc"))
        assertFalse(shouldShowBillingStatus(storeScreenshots = true))
        assertTrue(shouldShowBillingStatus(storeScreenshots = false))
    }
}
