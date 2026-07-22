package com.example.educloud.ui.screens.catalog

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CatalogueRoutesTest {

    @Test
    fun validCatalogueDeepLinksAreAccepted() {
        assertTrue(CatalogueRoutes.hasTerm(0))
        assertTrue(CatalogueRoutes.hasTerm(2))
        assertTrue(CatalogueRoutes.hasUnit(0, 0))
        assertTrue(CatalogueRoutes.hasUnit(2, 7))
    }

    @Test
    fun malformedCatalogueDeepLinksAreRejectedBeforeIndexing() {
        assertFalse(CatalogueRoutes.hasTerm(-1))
        assertFalse(CatalogueRoutes.hasTerm(3))
        assertFalse(CatalogueRoutes.hasUnit(-1, 0))
        assertFalse(CatalogueRoutes.hasUnit(0, -1))
        assertFalse(CatalogueRoutes.hasUnit(0, 8))
        assertFalse(CatalogueRoutes.hasUnit(99, 99))
    }
}
