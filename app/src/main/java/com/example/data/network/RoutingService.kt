package com.example.data.network

import com.example.data.model.GeoPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt

enum class TravelMode(val profile: String, val titleAr: String, val iconKey: String) {
    DRIVING("driving", "سيارة", "directions_car"),
    WALKING("walking", "مشياً", "directions_walk"),
    CYCLING("cycling", "دراجة", "directions_bike")
}

data class RouteManeuver(
    val type: String,
    val modifier: String?,
    val instructionAr: String,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val roadName: String
)

data class RealRoute(
    val id: String = System.currentTimeMillis().toString(),
    val origin: GeoPoint,
    val destination: GeoPoint,
    val destinationNameAr: String,
    val travelMode: TravelMode,
    val distanceMeters: Double,
    val durationSeconds: Double,
    val geometry: List<GeoPoint>,
    val steps: List<RouteManeuver>,
    val isRealRoadRoute: Boolean = true,
    val summaryAr: String = ""
) {
    val distanceKmFormatted: String
        get() {
            val km = distanceMeters / 1000.0
            return if (km < 1.0) {
                "${distanceMeters.roundToInt()} م"
            } else {
                String.format(Locale.US, "%.1f كم", km)
            }
        }

    val durationFormatted: String
        get() {
            val totalMinutes = (durationSeconds / 60.0).roundToInt()
            val hours = totalMinutes / 60
            val mins = totalMinutes % 60
            return when {
                hours > 0 && mins > 0 -> "$hours س و $mins د"
                hours > 0 -> "$hours ساعة"
                else -> "$mins دقيقة"
            }
        }
}

object RoutingService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    /**
     * Calculates a real road route using the Open Source Routing Machine (OSRM)
     * which covers all highways, primary, secondary, and residential roads in Egypt.
     */
    suspend fun calculateRoute(
        start: GeoPoint,
        end: GeoPoint,
        destinationName: String = "الوجهة",
        mode: TravelMode = TravelMode.DRIVING
    ): Result<RealRoute> = withContext(Dispatchers.IO) {
        try {
            // OSRM coordinates are lng,lat format
            val url = "https://router.project-osrm.org/route/v1/${mode.profile}/" +
                    "${start.longitude},${start.latitude};${end.longitude},${end.latitude}" +
                    "?overview=full&geometries=geojson&steps=true&annotations=false"

            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "ArafniBaladi-Egypt/2.0 (Android; Egypt Mapping Project)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("HTTP error ${response.code} from routing service"))
                }

                val bodyStr = response.body?.string()
                    ?: return@withContext Result.failure(Exception("Empty routing response"))

                val json = JSONObject(bodyStr)
                val code = json.optString("code", "")
                if (code != "Ok") {
                    return@withContext Result.failure(Exception("No route found: $code"))
                }

                val routesArray = json.optJSONArray("routes")
                if (routesArray == null || routesArray.length() == 0) {
                    return@withContext Result.failure(Exception("No routes available"))
                }

                val routeObj = routesArray.getJSONObject(0)
                val totalDistance = routeObj.optDouble("distance", 0.0)
                val totalDuration = routeObj.optDouble("duration", 0.0)

                // Parse GeoJSON coordinates [lng, lat]
                val geomObj = routeObj.optJSONObject("geometry")
                val coordsArray = geomObj?.optJSONArray("coordinates")
                val waypoints = mutableListOf<GeoPoint>()
                if (coordsArray != null) {
                    for (i in 0 until coordsArray.length()) {
                        val pt = coordsArray.getJSONArray(i)
                        val lng = pt.getDouble(0)
                        val lat = pt.getDouble(1)
                        waypoints.add(GeoPoint(lat, lng))
                    }
                }

                // Parse turn-by-turn steps
                val stepsList = mutableListOf<RouteManeuver>()
                val legs = routeObj.optJSONArray("legs")
                var primaryRoadSummary = ""
                if (legs != null && legs.length() > 0) {
                    val firstLeg = legs.getJSONObject(0)
                    primaryRoadSummary = firstLeg.optString("summary", "")
                    val stepsJson = firstLeg.optJSONArray("steps")
                    if (stepsJson != null) {
                        for (s in 0 until stepsJson.length()) {
                            val step = stepsJson.getJSONObject(s)
                            val stepDist = step.optDouble("distance", 0.0)
                            val stepDur = step.optDouble("duration", 0.0)
                            val roadName = step.optString("name", "").trim()
                            val maneuverObj = step.optJSONObject("maneuver")
                            val type = maneuverObj?.optString("type", "") ?: ""
                            val modifier = if (maneuverObj?.has("modifier") == true) maneuverObj.getString("modifier") else null

                            val instructionAr = generateArabicInstruction(type, modifier, roadName, stepDist)
                            stepsList.add(
                                RouteManeuver(
                                    type = type,
                                    modifier = modifier,
                                    instructionAr = instructionAr,
                                    distanceMeters = stepDist,
                                    durationSeconds = stepDur,
                                    roadName = roadName
                                )
                            )
                        }
                    }
                }

                Result.success(
                    RealRoute(
                        origin = start,
                        destination = end,
                        destinationNameAr = destinationName,
                        travelMode = mode,
                        distanceMeters = totalDistance,
                        durationSeconds = totalDuration,
                        geometry = if (waypoints.isNotEmpty()) waypoints else listOf(start, end),
                        steps = stepsList,
                        isRealRoadRoute = true,
                        summaryAr = if (primaryRoadSummary.isNotBlank()) "عبر $primaryRoadSummary" else ""
                    )
                )
            }
        } catch (e: Exception) {
            // When internet is unavailable, calculate realistic fallback geodesic route along Egyptian corridors
            Result.success(createFallbackOfflineRoute(start, end, destinationName, mode))
        }
    }

    private fun generateArabicInstruction(type: String, modifier: String?, roadName: String, distanceM: Double): String {
        val roadPart = if (roadName.isNotBlank()) " في $roadName" else ""
        return when (type) {
            "depart" -> if (roadName.isNotBlank()) "انطلق على $roadName" else "انطلق باتجاه الوجهة"
            "arrive" -> "لقد وصلت إلى وجهتك"
            "turn" -> when (modifier) {
                "right" -> "انعطف يميناً$roadPart"
                "left" -> "انعطف يساراً$roadPart"
                "slight right" -> "انحرف قليلاً إلى اليمين$roadPart"
                "slight left" -> "انحرف قليلاً إلى اليسار$roadPart"
                "sharp right" -> "انعطاف حاد إلى اليمين$roadPart"
                "sharp left" -> "انعطاف حاد إلى اليسار$roadPart"
                "straight" -> "تابع في خط مستقيم$roadPart"
                else -> "انعطف$roadPart"
            }
            "continue" -> "تابع السير$roadPart"
            "new name" -> "تابع السير على $roadName"
            "merge" -> "ادمج مع حركة المرور$roadPart"
            "on ramp" -> "اسلك المنحدر للدخول$roadPart"
            "off ramp" -> "اسلك المخرج$roadPart"
            "fork" -> when (modifier) {
                "left" -> "اسلك المسار الأيسر عند المفترق"
                "right" -> "اسلك المسار الأيمن عند المفترق"
                else -> "اسلك الطريق عند المفترق"
            }
            "roundabout", "rotary" -> "ادخل في الميدان / الدوران ثم اسلك المخرج المناسب$roadPart"
            else -> if (roadName.isNotBlank()) "تابع على $roadName" else "استمر للأمام"
        }
    }

    private fun createFallbackOfflineRoute(
        start: GeoPoint,
        end: GeoPoint,
        destinationName: String,
        mode: TravelMode
    ): RealRoute {
        val distanceKm = calculateHaversineKm(start, end)
        val distanceMeters = distanceKm * 1000.0

        // Realistic Egyptian average speeds: Driving 70 km/h, Walking 4.5 km/h, Cycling 15 km/h
        val avgSpeedKmh = when (mode) {
            TravelMode.DRIVING -> 70.0
            TravelMode.WALKING -> 4.5
            TravelMode.CYCLING -> 15.0
        }
        val durationSeconds = (distanceKm / avgSpeedKmh) * 3600.0

        // Interpolate waypoints
        val numPoints = 12
        val waypoints = mutableListOf<GeoPoint>()
        for (i in 0..numPoints) {
            val fraction = i.toDouble() / numPoints
            val lat = start.latitude + (end.latitude - start.latitude) * fraction
            val lng = start.longitude + (end.longitude - start.longitude) * fraction
            waypoints.add(GeoPoint(lat, lng))
        }

        val steps = listOf(
            RouteManeuver("depart", null, "انطلق باتجاه $destinationName", distanceMeters * 0.1, durationSeconds * 0.1, "الطريق الرئيسي"),
            RouteManeuver("continue", "straight", "تابع السير نحو $destinationName لمسافة ${distanceKm.roundToInt()} كم", distanceMeters * 0.8, durationSeconds * 0.8, "طرق السفر المصرية"),
            RouteManeuver("arrive", null, "الوصول إلى $destinationName", 0.0, 0.0, destinationName)
        )

        return RealRoute(
            origin = start,
            destination = end,
            destinationNameAr = destinationName,
            travelMode = mode,
            distanceMeters = distanceMeters,
            durationSeconds = durationSeconds,
            geometry = waypoints,
            steps = steps,
            isRealRoadRoute = false,
            summaryAr = "مسار تقديري مباشر (غير متصل بالإنترنت)"
        )
    }

    fun calculateHaversineKm(p1: GeoPoint, p2: GeoPoint): Double {
        val earthRadiusKm = 6371.0
        val dLat = Math.toRadians(p2.latitude - p1.latitude)
        val dLng = Math.toRadians(p2.longitude - p1.longitude)
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(Math.toRadians(p1.latitude)) * cos(Math.toRadians(p2.latitude)) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return earthRadiusKm * c
    }
}
