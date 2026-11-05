package com.example

import com.example.data.model.PlaceCategory
import com.example.data.model.PlacesRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleUnitTest {
    @Test
    fun placesRepository_containsCuratedPlaces() {
        val places = PlacesRepository.places
        assertTrue(places.isNotEmpty())
        assertTrue(places.size >= 5)
    }

    @Test
    fun placesRepository_searchWorks() {
        val pyramids = PlacesRepository.searchPlaces("أهرامات")
        assertTrue(pyramids.isNotEmpty())
        assertEquals("giza_pyramids", pyramids.first().id)

        val alexandria = PlacesRepository.searchPlaces("الإسكندرية")
        assertTrue(alexandria.isNotEmpty())
    }

    @Test
    fun placesRepository_categoryFilterWorks() {
        val pharaonic = PlacesRepository.getPlacesByCategory(PlaceCategory.PHARAONIC)
        assertTrue(pharaonic.isNotEmpty())
        assertTrue(pharaonic.all { it.category == PlaceCategory.PHARAONIC })
    }

    @Test
    fun placesRepository_routesHaveWaypoints() {
        val giza = PlacesRepository.getPlaceById("giza_pyramids")
        assertNotNull(giza)
        assertTrue(giza!!.defaultRouteFromCairo.waypoints.isNotEmpty())
        assertTrue(giza.defaultRouteFromCairo.distanceKm > 0)
    }
}
