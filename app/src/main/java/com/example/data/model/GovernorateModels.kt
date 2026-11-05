package com.example.data.model

data class DetailedSection(
    val title: String,
    val content: String
)

data class FullWikiArticle(
    val title: String,
    val summary: String,
    val fullText: String,
    val imageUrl: String?,
    val articleUrl: String,
    val sections: List<DetailedSection> = emptyList(),
    val language: String = "ar",
    val isFromCache: Boolean = false
)

enum class EgyptPlaceType(val titleAr: String, val iconKey: String) {
    TOURIST("معلم سياحي وتاريخي", "account_balance"),
    NATURE_BEACH("طبيعة وشواطئ وحدائق", "terrain"),
    HOTEL("فندق وإقامة", "hotel"),
    RESTAURANT_CAFE("مطعم وكافيه", "restaurant"),
    SHOPPING("سوق ومول تجاري", "shopping_cart"),
    SERVICE_HOSPITAL("مستشفى وخدمات", "local_hospital"),
    TRANSPORT("مواصلات وتنقل", "directions_bus")
}

data class EgyptGovernoratePlace(
    val id: String,
    val nameAr: String,
    val governorateNameAr: String,
    val cityNameAr: String,
    val type: EgyptPlaceType,
    val location: GeoPoint,
    val shortDescriptionAr: String,
    val fullDescriptionAr: String,
    val imageUrl: String? = null,
    val sourceUrl: String? = null,
    val wikiSearchTerm: String? = null,
    val servicesOffered: List<String> = emptyList(),
    val addressAr: String = ""
)

data class EgyptGovernorateInfo(
    val nameAr: String,
    val capitalCityAr: String,
    val regionAr: String,
    val location: GeoPoint,
    val areaKm2: String,
    val population: String,
    val populationCensusYear: String,
    val overviewAr: String,
    val historicalBackgroundAr: String,
    val economyAndCultureAr: String,
    val keyCitiesAndCenters: List<String>,
    val imageUrl: String? = null,
    val wikiSearchTerm: String = "محافظة_$nameAr",
    val placesList: List<EgyptGovernoratePlace> = emptyList()
)
