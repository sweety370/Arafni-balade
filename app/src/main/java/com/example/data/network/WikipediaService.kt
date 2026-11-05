package com.example.data.network

import com.example.data.local.CachedWikiArticle
import com.example.data.local.WikiCacheDao
import com.example.data.model.DetailedSection
import com.example.data.model.FullWikiArticle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object WikipediaService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(8, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    private const val USER_AGENT = "ArafniBaladiEgypt/2.0 (505omerfive@gmail.com; Android OpenStreetMap & Egyptian Guide)"

    /**
     * Maps local entity queries to accurate Wikipedia article titles.
     */
    val WIKI_TITLE_MAPPING = mapOf(
        "القاهرة" to "القاهرة",
        "الجيزة" to "محافظة_الجيزة",
        "الإسكندرية" to "الإسكندرية",
        "بورسعيد" to "بورسعيد",
        "السويس" to "محافظة_السويس",
        "الإسماعيلية" to "محافظة_الإسماعيلية",
        "دمياط" to "محافظة_دمياط",
        "الدقهلية" to "محافظة_الدقهلية",
        "الدقهلية (المنصورة)" to "محافظة_الدقهلية",
        "الغربية" to "محافظة_الغربية",
        "الغربية (طنطا)" to "محافظة_الغربية",
        "الشرقية" to "محافظة_الشرقية",
        "الشرقية (الزقازيق)" to "محافظة_الشرقية",
        "القليوبية" to "محافظة_القليوبية",
        "القليوبية (بنها)" to "محافظة_القليوبية",
        "المنوفية" to "محافظة_المنوفية",
        "المنوفية (شبين الكوم)" to "محافظة_المنوفية",
        "كفر الشيخ" to "محافظة_كفر_الشيخ",
        "البحيرة" to "محافظة_البحيرة",
        "البحيرة (دمنهور)" to "محافظة_البحيرة",
        "الفيوم" to "محافظة_الفيوم",
        "بني سويف" to "محافظة_بني_سويف",
        "المنيا" to "محافظة_المنيا",
        "أسيوط" to "محافظة_أسيوط",
        "سوهاج" to "محافظة_سوهاج",
        "قنا" to "محافظة_قنا",
        "الأقصر" to "محافظة_الأقصر",
        "أسوان" to "محافظة_أسوان",
        "مطروح" to "محافظة_مطروح",
        "البحر الأحمر" to "محافظة_البحر_الأحمر",
        "البحر الأحمر (الغردقة)" to "محافظة_البحر_الأحمر",
        "جنوب سيناء" to "محافظة_جنوب_سيناء",
        "جنوب سيناء (طور سيناء)" to "محافظة_جنوب_سيناء",
        "شمال سيناء" to "محافظة_شمال_سيناء",
        "شمال سيناء (العريش)" to "محافظة_شمال_سيناء",
        "الوادي الجديد" to "محافظة_الوادي_الجديد",
        "الوادي الجديد (الخارجة)" to "محافظة_الوادي_الجديد"
    )

    suspend fun fetchArticle(
        query: String,
        wikiCacheDao: WikiCacheDao? = null,
        forceRefresh: Boolean = false
    ): Result<FullWikiArticle> = withContext(Dispatchers.IO) {
        val cleanQuery = query.trim()
        val mappedTitle = WIKI_TITLE_MAPPING[cleanQuery] ?: WIKI_TITLE_MAPPING[cleanQuery.replace("محافظة ", "")] ?: cleanQuery

        val cacheKey = mappedTitle.lowercase()

        // 1. Check local cache first if not forceRefresh
        if (!forceRefresh && wikiCacheDao != null) {
            try {
                val cached = wikiCacheDao.getCachedArticle(cacheKey)
                if (cached != null) {
                    val sections = parseSectionsJson(cached.sectionsJson)
                    return@withContext Result.success(
                        FullWikiArticle(
                            title = cached.title,
                            summary = cached.summary,
                            fullText = cached.fullText,
                            imageUrl = cached.imageUrl,
                            articleUrl = cached.articleUrl,
                            sections = sections,
                            language = cached.language,
                            isFromCache = true
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        // 2. Attempt fetching from Arabic Wikipedia REST API
        val arabicResult = fetchFromRestAndActionApi(mappedTitle, "ar")
        if (arabicResult.isSuccess) {
            val article = arabicResult.getOrNull()!!
            // Cache locally in Room
            if (wikiCacheDao != null) {
                try {
                    wikiCacheDao.insertCachedArticle(
                        CachedWikiArticle(
                            cacheKey = cacheKey,
                            title = article.title,
                            summary = article.summary,
                            fullText = article.fullText,
                            imageUrl = article.imageUrl,
                            articleUrl = article.articleUrl,
                            sectionsJson = serializeSectionsJson(article.sections),
                            language = "ar"
                        )
                    )
                } catch (_: Exception) {}
            }
            return@withContext Result.success(article)
        }

        // 3. Try Opensearch if direct title failed
        val searchArabicResult = searchAndFetchFirstResult(cleanQuery, "ar")
        if (searchArabicResult.isSuccess) {
            val article = searchArabicResult.getOrNull()!!
            if (wikiCacheDao != null) {
                try {
                    wikiCacheDao.insertCachedArticle(
                        CachedWikiArticle(
                            cacheKey = cacheKey,
                            title = article.title,
                            summary = article.summary,
                            fullText = article.fullText,
                            imageUrl = article.imageUrl,
                            articleUrl = article.articleUrl,
                            sectionsJson = serializeSectionsJson(article.sections),
                            language = "ar"
                        )
                    )
                } catch (_: Exception) {}
            }
            return@withContext Result.success(article)
        }

        // 4. Fallback to English Wikipedia if Arabic is unavailable
        val englishResult = fetchFromRestAndActionApi(cleanQuery, "en")
        if (englishResult.isSuccess) {
            val article = englishResult.getOrNull()!!.copy(language = "en")
            return@withContext Result.success(article)
        }

        // 5. Final fallback to cached version even if expired
        if (wikiCacheDao != null) {
            try {
                val cached = wikiCacheDao.getCachedArticle(cacheKey)
                if (cached != null) {
                    val sections = parseSectionsJson(cached.sectionsJson)
                    return@withContext Result.success(
                        FullWikiArticle(
                            title = cached.title,
                            summary = cached.summary,
                            fullText = cached.fullText,
                            imageUrl = cached.imageUrl,
                            articleUrl = cached.articleUrl,
                            sections = sections,
                            language = cached.language,
                            isFromCache = true
                        )
                    )
                }
            } catch (_: Exception) {}
        }

        Result.failure(Exception("لم يتم العثور على مقالة مطابقة في ويكيبيديا، يرجى التأكد من الاتصال بالإنترنت."))
    }

    private fun fetchFromRestAndActionApi(title: String, lang: String): Result<FullWikiArticle> {
        return try {
            val encodedTitle = URLEncoder.encode(title.replace(" ", "_"), "UTF-8")
            val summaryUrl = "https://$lang.wikipedia.org/api/rest_v1/page/summary/$encodedTitle"

            val req = Request.Builder()
                .url(summaryUrl)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return Result.failure(Exception("Summary HTTP ${resp.code}"))
                val bodyStr = resp.body?.string() ?: return Result.failure(Exception("Empty body"))
                val json = JSONObject(bodyStr)

                val pageTitle = json.optString("title", title)
                val description = json.optString("description", "")
                val extract = json.optString("extract", "")
                val imageUrl = json.optJSONObject("thumbnail")?.optString("source")
                val pageUrl = json.optJSONObject("content_urls")?.optJSONObject("desktop")?.optString("page")
                    ?: "https://$lang.wikipedia.org/wiki/$encodedTitle"

                // Fetch comprehensive full text & sections from Action API
                val fullTextAndSections = fetchFullTextAndSections(pageTitle, lang)

                val fullText = fullTextAndSections.first.ifBlank { extract }
                val sections = fullTextAndSections.second

                Result.success(
                    FullWikiArticle(
                        title = pageTitle,
                        summary = description.ifBlank { extract },
                        fullText = fullText,
                        imageUrl = imageUrl,
                        articleUrl = pageUrl,
                        sections = sections,
                        language = lang,
                        isFromCache = false
                    )
                )
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun fetchFullTextAndSections(title: String, lang: String): Pair<String, List<DetailedSection>> {
        return try {
            val encoded = URLEncoder.encode(title, "UTF-8")
            val url = "https://$lang.wikipedia.org/w/api.php?action=query&prop=extracts&explaintext=1&titles=$encoded&format=json"

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return Pair("", emptyList())
                val body = resp.body?.string() ?: return Pair("", emptyList())
                val root = JSONObject(body)
                val pages = root.optJSONObject("query")?.optJSONObject("pages") ?: return Pair("", emptyList())

                val firstKey = pages.keys().asSequence().firstOrNull() ?: return Pair("", emptyList())
                val pageObj = pages.getJSONObject(firstKey)
                val fullRawText = pageObj.optString("extract", "")

                val sections = parseTextIntoSections(fullRawText)
                Pair(fullRawText, sections)
            }
        } catch (e: Exception) {
            Pair("", emptyList())
        }
    }

    private fun parseTextIntoSections(text: String): List<DetailedSection> {
        val list = mutableListOf<DetailedSection>()
        val lines = text.split("\n")
        var currentTitle = "مقدمة المقالة"
        val currentContent = StringBuilder()

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("==") && trimmed.endsWith("==")) {
                if (currentContent.isNotBlank()) {
                    list.add(DetailedSection(currentTitle, currentContent.toString().trim()))
                    currentContent.clear()
                }
                currentTitle = trimmed.replace("=", "").trim()
            } else {
                if (trimmed.isNotBlank()) {
                    currentContent.append(trimmed).append("\n\n")
                }
            }
        }

        if (currentContent.isNotBlank()) {
            list.add(DetailedSection(currentTitle, currentContent.toString().trim()))
        }

        return list.filter {
            val t = it.title
            !t.contains("مراجع") && !t.contains("وصلات خارجية") && !t.contains("انظر أيضاً") && !t.contains("المصادر")
        }
    }

    private fun searchAndFetchFirstResult(query: String, lang: String): Result<FullWikiArticle> {
        return try {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://$lang.wikipedia.org/w/api.php?action=opensearch&search=$encoded&limit=1&namespace=0&format=json"

            val req = Request.Builder()
                .url(url)
                .header("User-Agent", USER_AGENT)
                .build()

            client.newCall(req).execute().use { resp ->
                if (!resp.isSuccessful) return Result.failure(Exception("Search failed"))
                val body = resp.body?.string() ?: return Result.failure(Exception("Empty"))
                val arr = JSONArray(body)
                val titles = arr.optJSONArray(1)
                if (titles != null && titles.length() > 0) {
                    val foundTitle = titles.getString(0)
                    fetchFromRestAndActionApi(foundTitle, lang)
                } else {
                    Result.failure(Exception("No article found for $query"))
                }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun serializeSectionsJson(sections: List<DetailedSection>): String {
        val arr = JSONArray()
        for (s in sections) {
            val obj = JSONObject()
            obj.put("title", s.title)
            obj.put("content", s.content)
            arr.put(obj)
        }
        return arr.toString()
    }

    private fun parseSectionsJson(jsonStr: String): List<DetailedSection> {
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<DetailedSection>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                list.add(DetailedSection(obj.getString("title"), obj.getString("content")))
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }
}
