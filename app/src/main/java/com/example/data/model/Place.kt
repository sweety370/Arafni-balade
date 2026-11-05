package com.example.data.model

data class GeoPoint(
    val latitude: Double,
    val longitude: Double
)

enum class PlaceCategory(val titleAr: String, val iconKey: String) {
    PHARAONIC("آثار فرعونية", "account_balance"),
    ISLAMIC_COPTIC("معالم إسلامية وقبطية", "mosque"),
    NATURE_OASES("طبيعة ومحميات", "terrain"),
    MODERN_CULTURE("متاحف ومعالم حديثة", "museum"),
    COASTAL_RESORTS("شواطئ ومدن ساحلية", "beach_access"),
    HISTORIC_CITIES("مدن ومراكز مصرية", "location_city")
}

enum class ServiceType(val titleAr: String, val iconKey: String) {
    GAS_STATION("محطات وقود", "local_gas_station"),
    PARKING("مواقف وجراجات", "local_parking"),
    RESTAURANT("مطاعم ومأكولات", "restaurant"),
    CAFE("مقاهي وكافيهات", "local_cafe"),
    HOSPITAL("مستشفيات وطوارئ", "local_hospital")
}

data class ServicePOI(
    val id: String,
    val nameAr: String,
    val type: ServiceType,
    val location: GeoPoint,
    val addressAr: String,
    val openStatusAr: String
)

data class RouteStop(
    val titleAr: String,
    val distanceKmFromStart: Int,
    val descriptionAr: String
)

data class RouteInfo(
    val distanceKm: Int,
    val durationCarMinutes: Int,
    val durationBusMinutes: Int,
    val durationTrainMinutes: Int?,
    val mainHighwaysAr: List<String>,
    val waypoints: List<GeoPoint>,
    val routeStops: List<RouteStop>,
    val travelTipsAr: List<String>
)

data class Place(
    val id: String,
    val nameAr: String,
    val nameEn: String,
    val governorateAr: String,
    val category: PlaceCategory,
    val location: GeoPoint,
    val shortDescriptionAr: String,
    // Deep historical questions asked by user:
    val namingOriginAr: String,         // سبب التسمية ومَن سمّاها
    val establishedDateAr: String,      // تاريخ التأسيس والنشأة
    val whyEstablishedAr: String,       // لماذا أُنشئت وما سبب تأسيسها
    val egyptAffiliationAr: String,     // متى أصبحت تابعة لمصر وتاريخها الوطني
    val historicEventsAr: String,       // أهم الأحداث التاريخية والمعارك
    val rulersAndKingsAr: String,       // الملوك والفراعنة والقادة الذين عاشوا أو ماتوا أو حكموا هنا
    val famousBornAr: String,          // أبرز الأعلام والشخصيات والعلماء الذين وُلدوا بها
    val visitingInfoAr: String,
    val ticketPriceAr: String,
    val openingHoursAr: String,
    val interestingFactsAr: List<String>,
    val bestTimeToVisitAr: String,
    val defaultRouteFromCairo: RouteInfo,
    val nearbyServices: List<ServicePOI> = emptyList()
)

data class EgyptDistrict(
    val id: String,
    val nameAr: String,
    val governorateAr: String,
    val location: GeoPoint,
    val centerTypeAr: String,
    val namingOriginAr: String,
    val establishedDateAr: String,
    val whyEstablishedAr: String,
    val egyptAffiliationAr: String,
    val historicEventsAr: String,
    val rulersAndKingsAr: String,
    val famousBornAr: String
)

data class CityLabel(
    val nameAr: String,
    val location: GeoPoint,
    val isCapital: Boolean = false,
    val regionAr: String = "مصر"
)

data class CityBridge(
    val nameAr: String,
    val governorateAr: String,
    val startPt: GeoPoint,
    val endPt: GeoPoint
)
