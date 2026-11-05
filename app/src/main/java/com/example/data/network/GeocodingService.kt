package com.example.data.network

import com.example.data.model.GeoPoint
import com.example.data.model.PlacesRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class SearchResultItem(
    val id: String,
    val titleAr: String,
    val subtitleAr: String,
    val location: GeoPoint,
    val categoryAr: String,
    val iconKey: String = "place",
    val isOnlineResult: Boolean = true
)

object GeocodingService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    /**
     * Search OpenStreetMap Nominatim for any location, street, facility or village in Egypt.
     */
    suspend fun searchEgypt(query: String): Result<List<SearchResultItem>> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            return@withContext Result.success(emptyList())
        }

        val localMatches = searchLocalVerifiedPlaces(trimmed)

        try {
            val encodedQuery = URLEncoder.encode(trimmed, "UTF-8")
            // Constrain search specifically to Egypt (countrycodes=eg) and request Arabic responses
            val url = "https://nominatim.openstreetmap.org/search" +
                    "?format=json&addressdetails=1&countrycodes=eg&accept-language=ar,en" +
                    "&limit=12&q=$encodedQuery"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ArafniBaladi-Egypt/2.0 (Android; support@arafni-baladi.eg)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.success(localMatches)
                }

                val bodyStr = response.body?.string() ?: return@withContext Result.success(localMatches)
                val jsonArray = JSONArray(bodyStr)

                val onlineResults = mutableListOf<SearchResultItem>()
                for (i in 0 until jsonArray.length()) {
                    val item = jsonArray.getJSONObject(i)
                    val lat = item.optDouble("lat", 0.0)
                    val lon = item.optDouble("lon", 0.0)
                    if (lat == 0.0 && lon == 0.0) continue

                    val displayName = item.optString("display_name", "")
                    val name = item.optString("name", "")
                    val type = item.optString("type", "")
                    val categoryClass = item.optString("class", "")
                    val placeId = item.optString("place_id", "osm_$i")

                    val address = item.optJSONObject("address")
                    val state = address?.optString("state", "")
                        ?: address?.optString("governorate", "")
                        ?: ""
                    val city = address?.optString("city", "")
                        ?: address?.optString("town", "")
                        ?: address?.optString("village", "")
                        ?: address?.optString("county", "")
                        ?: ""
                    val road = address?.optString("road", "") ?: ""

                    val title = if (name.isNotBlank()) name else extractMainTitle(displayName)
                    val subtitle = formatSubtitle(road, city, state, displayName)
                    val categoryAr = mapOsmCategoryToArabic(categoryClass, type)
                    val iconKey = mapOsmCategoryToIcon(categoryClass, type)

                    onlineResults.add(
                        SearchResultItem(
                            id = "osm_$placeId",
                            titleAr = title,
                            subtitleAr = subtitle,
                            location = GeoPoint(lat, lon),
                            categoryAr = categoryAr,
                            iconKey = iconKey,
                            isOnlineResult = true
                        )
                    )
                }

                // Merge online results with local verified matches (avoiding exact duplicates)
                val combined = mutableListOf<SearchResultItem>()
                combined.addAll(localMatches)
                onlineResults.forEach { online ->
                    val isDuplicate = combined.any {
                        calculateDistanceMeters(it.location, online.location) < 300.0
                    }
                    if (!isDuplicate) {
                        combined.add(online)
                    }
                }

                Result.success(combined)
            }
        } catch (e: Exception) {
            // Offline fallback returns local matches
            Result.success(localMatches)
        }
    }

    /**
     * Reverse geocodes coordinates to an address description in Arabic.
     */
    suspend fun reverseGeocode(geo: GeoPoint): Result<String> = withContext(Dispatchers.IO) {
        try {
            val url = "https://nominatim.openstreetmap.org/reverse" +
                    "?format=json&lat=${geo.latitude}&lon=${geo.longitude}&accept-language=ar,en"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ArafniBaladi-Egypt/2.0 (Android; support@arafni-baladi.eg)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return@withContext Result.failure(Exception("Reverse geocode failed"))
                val bodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val json = JSONObject(bodyStr)
                val displayName = json.optString("display_name", "")
                val address = json.optJSONObject("address")
                val road = address?.optString("road", "") ?: ""
                val suburb = address?.optString("suburb", "") ?: ""
                val city = address?.optString("city", "") ?: address?.optString("town", "") ?: ""
                val state = address?.optString("state", "") ?: ""

                val formatted = listOf(road, suburb, city, state)
                    .filter { it.isNotBlank() }
                    .joinToString("، ")

                Result.success(if (formatted.isNotBlank()) formatted else displayName)
            }
        } catch (e: Exception) {
            val nearest = PlacesRepository.getNearestPlace(geo)
            Result.success("بالقرب من ${nearest.nameAr} - ${nearest.governorateAr}")
        }
    }

    private fun searchLocalVerifiedPlaces(query: String): List<SearchResultItem> {
        val normalized = normalizeArabic(query)
        val matches = mutableListOf<SearchResultItem>()

        // 1. Search Places
        PlacesRepository.places.forEach { place ->
            val pName = normalizeArabic(place.nameAr)
            val pGov = normalizeArabic(place.governorateAr)
            val pEn = place.nameEn.lowercase()
            val qLower = query.lowercase()

            if (pName.contains(normalized) || pGov.contains(normalized) || pEn.contains(qLower)) {
                matches.add(
                    SearchResultItem(
                        id = place.id,
                        titleAr = place.nameAr,
                        subtitleAr = "${place.governorateAr} • ${place.category.titleAr}",
                        location = place.location,
                        categoryAr = place.category.titleAr,
                        iconKey = place.category.iconKey,
                        isOnlineResult = false
                    )
                )
            }
        }

        // 2. Search Governorates
        com.example.ui.map.EgyptMapData.ALL_EGYPT_GOVERNORATES.forEach { gov ->
            if (normalizeArabic(gov.nameAr).contains(normalized)) {
                matches.add(
                    SearchResultItem(
                        id = "gov_${gov.nameAr}",
                        titleAr = "محافظة ${gov.nameAr}",
                        subtitleAr = "إحدى محافظات جمهورية مصر العربية الـ 27",
                        location = gov.location,
                        categoryAr = "محافظة مصرية",
                        iconKey = "location_city",
                        isOnlineResult = false
                    )
                )
            }
        }

        // 3. Search Districts
        com.example.ui.map.EgyptMapData.ALL_CENTERS.forEach { dist ->
            if (normalizeArabic(dist.nameAr).contains(normalized) || normalizeArabic(dist.governorateAr).contains(normalized)) {
                matches.add(
                    SearchResultItem(
                        id = dist.id,
                        titleAr = dist.nameAr,
                        subtitleAr = "${dist.centerTypeAr} بمحافظة ${dist.governorateAr}",
                        location = dist.location,
                        categoryAr = "مركز ومدينة",
                        iconKey = "location_city",
                        isOnlineResult = false
                    )
                )
            }
        }

        // 4. Search Services
        PlacesRepository.allServices.forEach { service ->
            if (normalizeArabic(service.nameAr).contains(normalized) || normalizeArabic(service.addressAr).contains(normalized)) {
                matches.add(
                    SearchResultItem(
                        id = service.id,
                        titleAr = service.nameAr,
                        subtitleAr = "${service.addressAr} • ${service.openStatusAr}",
                        location = service.location,
                        categoryAr = service.type.titleAr,
                        iconKey = service.type.iconKey,
                        isOnlineResult = false
                    )
                )
            }
        }

        return matches.take(8)
    }

    private fun normalizeArabic(text: String): String {
        return text
            .replace("[إأآا]".toRegex(), "ا")
            .replace("ة", "ه")
            .replace("ى", "ي")
            .replace("[\u064B-\u065F]".toRegex(), "") // remove tashkeel diacritics
            .trim()
            .lowercase()
    }

    private fun extractMainTitle(full: String): String {
        val parts = full.split(",")
        return parts.firstOrNull()?.trim() ?: full
    }

    private fun formatSubtitle(road: String, city: String, state: String, fallback: String): String {
        val components = listOf(road, city, state).filter { it.isNotBlank() }
        return if (components.isNotEmpty()) {
            components.joinToString("، ")
        } else {
            fallback.split(",").take(3).joinToString("، ")
        }
    }

    private fun mapOsmCategoryToArabic(categoryClass: String, type: String): String {
        return when {
            categoryClass == "amenity" && type == "hospital" -> "مستشفى وطوارئ"
            categoryClass == "amenity" && type == "pharmacy" -> "صيدلية"
            categoryClass == "amenity" && type == "fuel" -> "محطة وقود"
            categoryClass == "amenity" && (type == "restaurant" || type == "fast_food") -> "مطعم ومأكولات"
            categoryClass == "amenity" && type == "cafe" -> "مقهى / كافيه"
            categoryClass == "amenity" && (type == "bank" || type == "atm") -> "بنك وماكينة ATM"
            categoryClass == "amenity" && (type == "school" || type == "university") -> "مؤسسة تعليمية"
            categoryClass == "amenity" && (type == "place_of_worship" || type == "mosque") -> "مسجد / دار عبادة"
            categoryClass == "tourism" && (type == "hotel" || type == "motel") -> "فندق وإقامة"
            categoryClass == "tourism" && (type == "museum" || type == "attraction") -> "معلم سياحي ومتحف"
            categoryClass == "historic" -> "معلم تاريخي وأثري"
            categoryClass == "highway" -> "طريق ومحور مروري"
            categoryClass == "boundary" || categoryClass == "place" -> "مدينة / محافظة"
            else -> "موقع في مصر"
        }
    }

    private fun mapOsmCategoryToIcon(categoryClass: String, type: String): String {
        return when {
            categoryClass == "amenity" && type == "hospital" -> "local_hospital"
            categoryClass == "amenity" && type == "pharmacy" -> "local_pharmacy"
            categoryClass == "amenity" && type == "fuel" -> "local_gas_station"
            categoryClass == "amenity" && (type == "restaurant" || type == "fast_food") -> "restaurant"
            categoryClass == "amenity" && type == "cafe" -> "local_cafe"
            categoryClass == "amenity" && (type == "bank" || type == "atm") -> "account_balance"
            categoryClass == "tourism" && (type == "hotel" || type == "motel") -> "hotel"
            categoryClass == "tourism" || categoryClass == "historic" -> "museum"
            else -> "place"
        }
    }

    private fun calculateDistanceMeters(p1: GeoPoint, p2: GeoPoint): Double {
        return RoutingService.calculateHaversineKm(p1, p2) * 1000.0
    }
}
