package com.example.data.network

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class OnlinePlaceDetails(
    val title: String,
    val summary: String,
    val extract: String,
    val imageUrl: String?,
    val articleUrl: String?,
    val relatedArticles: List<RelatedOnlineItem> = emptyList()
)

data class RelatedOnlineItem(
    val title: String,
    val snippet: String,
    val url: String
)

object OnlineExplorerService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(6, TimeUnit.SECONDS)
        .readTimeout(8, TimeUnit.SECONDS)
        .build()

    // Mapping for clean Wikipedia search terms
    private val searchKeywordMap = mapOf(
        "giza_pyramids" to "أهرامات_الجيزة",
        "grand_egyptian_museum" to "المتحف_المصري_الكبير",
        "cairo_citadel" to "قلعة_صلاح_الدين_الأيوبي_(القاهرة)",
        "cairo_tower" to "برج_القاهرة",
        "khan_el_khalili" to "خان_الخليلي",
        "saqqara_pyramid" to "هرم_سقارة_المدرج",
        "qaitbay_citadel" to "قلعة_قايتباي",
        "bibliotheca_alexandrina" to "مكتبة_الإسكندرية_الجديدة",
        "agiba_beach_matrouh" to "مرسى_مطروح",
        "karnak_temple" to "معبد_الكرنك",
        "hatshepsut_temple" to "الدير_البحري",
        "philae_temple_aswan" to "معبد_فيلة",
        "abu_simbel_temples" to "أبو_سمبل",
        "st_catherine_monastery" to "دير_سانت_كاترين",
        "ras_mohamed_reserve" to "محمية_رأس_محمد",
        "dahab_blue_hole" to "الثقب_الأزرق_(البحر_الأحمر)",
        "hurghada_giftun" to "جزيرة_الجفتون",
        "siwa_oasis" to "واحة_سيوة",
        "farafra_white_desert" to "الصحراء_البيضاء_(مصر)",
        "wadi_el_hitan_fayoum" to "وادي_الحيتان",
        "suez_canal_ismailia" to "قناة_السويس",
        "port_said_historic" to "بورسعيد"
    )

    suspend fun fetchExpandedInfo(placeId: String, fallbackQuery: String): Result<OnlinePlaceDetails> = withContext(Dispatchers.IO) {
        try {
            val searchTerm = searchKeywordMap[placeId] ?: fallbackQuery.replace(" ", "_")
            val encodedTerm = URLEncoder.encode(searchTerm, "UTF-8")

            val summaryUrl = "https://ar.wikipedia.org/api/rest_v1/page/summary/$encodedTerm"
            val request = Request.Builder()
                .url(summaryUrl)
                .header("User-Agent", "ArafniBaladiEgyptMap/1.0 (amr272870@gmail.com)")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    // Try general search
                    return@withContext searchWikipedia(fallbackQuery)
                }

                val bodyStr = response.body?.string() ?: return@withContext Result.failure(Exception("Empty body"))
                val json = JSONObject(bodyStr)

                val title = json.optString("title", fallbackQuery)
                val description = json.optString("description", "معلم تاريخي وسياحي في مصر")
                val extract = json.optString("extract", "")
                val thumbnailObj = json.optJSONObject("thumbnail")
                val imageUrl = thumbnailObj?.optString("source")
                val contentUrls = json.optJSONObject("content_urls")?.optJSONObject("desktop")
                val pageUrl = contentUrls?.optString("page")

                // Fetch related links
                val related = fetchRelatedTopics(fallbackQuery)

                Result.success(
                    OnlinePlaceDetails(
                        title = title,
                        summary = description,
                        extract = extract,
                        imageUrl = imageUrl,
                        articleUrl = pageUrl,
                        relatedArticles = related
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun searchWikipedia(query: String): Result<OnlinePlaceDetails> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://ar.wikipedia.org/w/api.php?action=opensearch&search=$encoded&limit=3&namespace=0&format=json"

            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "ArafniBaladiEgyptMap/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return Result.failure(Exception("Search failed"))
                val body = response.body?.string() ?: return Result.failure(Exception("Empty"))
                val array = JSONArray(body)
                val titles = array.optJSONArray(1)
                val snippets = array.optJSONArray(2)
                val urls = array.optJSONArray(3)

                if (titles != null && titles.length() > 0) {
                    val firstTitle = titles.getString(0)
                    val firstSnippet = snippets?.optString(0) ?: ""
                    val firstUrl = urls?.optString(0)

                    val related = mutableListOf<RelatedOnlineItem>()
                    for (i in 1 until titles.length()) {
                        related.add(
                            RelatedOnlineItem(
                                title = titles.getString(i),
                                snippet = snippets?.optString(i) ?: "",
                                url = urls?.optString(i) ?: ""
                            )
                        )
                    }

                    Result.success(
                        OnlinePlaceDetails(
                            title = firstTitle,
                            summary = "نتائج موسوعية مباشرة عبر الإنترنت",
                            extract = firstSnippet,
                            imageUrl = null,
                            articleUrl = firstUrl,
                            relatedArticles = related
                        )
                    )
                } else {
                    Result.failure(Exception("No results found"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchRelatedTopics(query: String): List<RelatedOnlineItem> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val searchUrl = "https://ar.wikipedia.org/w/api.php?action=opensearch&search=$encoded&limit=4&namespace=0&format=json"
            val request = Request.Builder()
                .url(searchUrl)
                .header("User-Agent", "ArafniBaladiEgyptMap/1.0")
                .build()

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) return emptyList()
                val body = response.body?.string() ?: return emptyList()
                val array = JSONArray(body)
                val titles = array.optJSONArray(1)
                val snippets = array.optJSONArray(2)
                val urls = array.optJSONArray(3)
                val list = mutableListOf<RelatedOnlineItem>()
                if (titles != null) {
                    for (i in 0 until titles.length()) {
                        list.add(
                            RelatedOnlineItem(
                                title = titles.getString(i),
                                snippet = snippets?.optString(i) ?: "",
                                url = urls?.optString(i) ?: ""
                            )
                        )
                    }
                }
                list
            }
        } catch (e: Exception) {
            emptyList()
        }
    }
}
