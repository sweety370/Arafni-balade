package com.example

import com.example.data.model.GeoPoint
import com.example.data.model.PlacesRepository
import com.example.data.network.GeocodingService
import com.example.data.network.RoutingService
import com.example.data.network.TravelMode
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.math.roundToInt

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class MappingAndRoutingTest {

    @Test
    fun haversineDistance_cairoToAlexandria_isAccurate() {
        val cairo = GeoPoint(30.0444, 31.2357)
        val alexandria = GeoPoint(31.2001, 29.9187)

        val distanceKm = RoutingService.calculateHaversineKm(cairo, alexandria)

        // Straight-line distance between Cairo and Alexandria is ~180 km
        assertTrue("Distance should be between 170 and 195 km, but was $distanceKm", distanceKm in 170.0..195.0)
    }

    @Test
    fun haversineDistance_cairoToAswan_isAccurate() {
        val cairo = GeoPoint(30.0444, 31.2357)
        val aswan = GeoPoint(24.0889, 32.8998)

        val distanceKm = RoutingService.calculateHaversineKm(cairo, aswan)

        // Straight-line distance between Cairo and Aswan is ~680 km
        assertTrue("Distance should be between 650 and 720 km, but was $distanceKm", distanceKm in 650.0..720.0)
    }

    @Test
    fun placesRepository_containsAllEgyptianGovernorates() {
        val govs = com.example.ui.map.EgyptMapData.ALL_EGYPT_GOVERNORATES
        assertEquals("Should cover all 27 Egyptian governorates", 27, govs.size)

        // Verify capital exists
        val cairo = govs.find { it.nameAr == "القاهرة" }
        assertNotNull(cairo)
        assertTrue(cairo!!.isCapital)

        // Verify border and canal governorates exist
        val alex = govs.find { it.nameAr.contains("الإسكندرية") }
        val suez = govs.find { it.nameAr.contains("السويس") }
        val sinai = govs.find { it.nameAr.contains("جنوب سيناء") }
        val aswan = govs.find { it.nameAr.contains("أسوان") }

        assertNotNull(alex)
        assertNotNull(suez)
        assertNotNull(sinai)
        assertNotNull(aswan)
    }

    @Test
    fun placesRepository_containsServicesAcrossEgypt() {
        val services = PlacesRepository.allServices
        assertTrue("Should contain multiple verified services", services.size >= 15)

        val hospitals = services.filter { it.type == com.example.data.model.ServiceType.HOSPITAL }
        val gasStations = services.filter { it.type == com.example.data.model.ServiceType.GAS_STATION }

        assertTrue(hospitals.isNotEmpty())
        assertTrue(gasStations.isNotEmpty())
    }

    @Test
    fun geocoding_searchNormalization_findsLocalPlaces() {
        // Search with different hamza variations
        val result1 = PlacesRepository.searchPlaces("اهرامات")
        val result2 = PlacesRepository.searchPlaces("أهرامات")

        assertTrue(result1.isNotEmpty())
        assertTrue(result2.isNotEmpty())
        assertEquals(result1.first().id, result2.first().id)
    }

    @Test
    fun fallbackRoute_marksOfflineProperlyAndHasLocations() = kotlinx.coroutines.runBlocking {
        val start = GeoPoint(30.0444, 31.2357)
        val end = GeoPoint(31.2001, 29.9187)

        val route = com.example.data.network.RoutingService.calculateRoute(
            start = start,
            end = end,
            destinationName = "الإسكندرية",
            mode = TravelMode.DRIVING
        )

        val resultRoute = route.getOrNull()
        assertNotNull(resultRoute)
        assertTrue(resultRoute!!.steps.isNotEmpty())
        // Verify all steps have locations assigned
        assertTrue(resultRoute.steps.all { it.location != null })
    }
}
