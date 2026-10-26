package com.example.data.remote

import com.example.data.QuranCatalogData
import com.example.data.model.AyahItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

class QuranRemoteService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(12, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    private val memoryCache = mutableMapOf<Int, List<AyahItem>>()

    /**
     * Fetches a full Surah with both Uthmanic text (quran-uthmani) and Al-Tafsir Al-Muyassar (ar.muyassar)
     * from AlQuran Cloud API, with automatic fallback to bundled offline Uthmanic verses.
     */
    suspend fun fetchSurahWithTafsir(surahNumber: Int): List<AyahItem> = withContext(Dispatchers.IO) {
        memoryCache[surahNumber]?.let { return@withContext it }

        val surahMeta = QuranCatalogData.ALL_114_SURAHS.find { it.number == surahNumber }
        val surahName = surahMeta?.nameArabic ?: "سورة رقم $surahNumber"

        try {
            val url = "https://api.alquran.cloud/v1/surah/$surahNumber/editions/quran-uthmani,ar.muyassar"
            val request = Request.Builder().url(url).get().build()
            client.newCall(request).execute().use { response ->
                if (response.isSuccessful) {
                    val bodyStr = response.body?.string()
                    if (!bodyStr.isNullOrBlank()) {
                        val root = JSONObject(bodyStr)
                        val dataArray = root.optJSONArray("data")
                        if (dataArray != null && dataArray.length() >= 2) {
                            val uthmaniEdition = dataArray.getJSONObject(0)
                            val tafsirEdition = dataArray.getJSONObject(1)
                            val uthmaniAyahs = uthmaniEdition.getJSONArray("ayahs")
                            val tafsirAyahs = tafsirEdition.getJSONArray("ayahs")

                            val parsedList = mutableListOf<AyahItem>()
                            val count = minOf(uthmaniAyahs.length(), tafsirAyahs.length())
                            for (i in 0 until count) {
                                val uObj = uthmaniAyahs.getJSONObject(i)
                                val tObj = tafsirAyahs.getJSONObject(i)
                                val ayahNum = uObj.optInt("numberInSurah", i + 1)
                                var textUthmani = uObj.optString("text", "")
                                // Strip leading Bismillah from Ayah 1 for Surahs other than Al-Fatihah (1) and At-Tawbah (9)
                                if (surahNumber != 1 && surahNumber != 9 && ayahNum == 1) {
                                    textUthmani = textUthmani
                                        .removePrefix("بِسْمِ ٱللَّهِ ٱلرَّحْمَٰنِ ٱلرَّحِيمِ ")
                                        .removePrefix("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ")
                                        .trim()
                                }
                                parsedList.add(
                                    AyahItem(
                                        surahNumber = surahNumber,
                                        surahName = surahName,
                                        ayahNumber = ayahNum,
                                        textUthmani = textUthmani,
                                        tafsirMuyassar = tObj.optString("text", ""),
                                        juz = uObj.optInt("juz", surahMeta?.juzStart ?: 1),
                                        page = uObj.optInt("page", 1)
                                    )
                                )
                            }
                            if (parsedList.isNotEmpty()) {
                                memoryCache[surahNumber] = parsedList
                                return@withContext parsedList
                            }
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // Fall through to offline cache
        }

        val offline = QuranCatalogData.OFFLINE_SURAHS[surahNumber]
        if (!offline.isNullOrEmpty()) {
            return@withContext offline
        }

        // If user is offline and selected a Surah not in full cache, provide its opening verse structure
        listOf(
            AyahItem(
                surahNumber = surahNumber,
                surahName = surahName,
                ayahNumber = 1,
                textUthmani = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ ﴿١﴾ سُورَةُ $surahName (${surahMeta?.revelationType ?: "مكية"} • عدد آياتها ${surahMeta?.ayahCount ?: 0})",
                tafsirMuyassar = "يمكنك تشغيل التلاوة الصوتية الكاملة لسورة $surahName بصوت القارئ المختار أو الاتصال بالإنترنت لتحميل جميع آيات السورة مع التفسير الميسر.",
                juz = surahMeta?.juzStart ?: 1,
                page = 1
            )
        )
    }

    /**
     * Searches Quran by keyword or Ayah reference (e.g. "2:255", "الكرسي", "الرحمن", "الصبر")
     * combining instant local indexed search with AlQuran Cloud API search.
     */
    suspend fun searchQuran(query: String): List<AyahItem> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) return@withContext emptyList()

        val normalizedQuery = QuranCatalogData.normalizeArabic(trimmed)
        val results = mutableListOf<AyahItem>()

        // 1. Check if query is a Surah:Ayah reference like "2:255" or just an Ayah number
        if (trimmed.contains(":")) {
            val parts = trimmed.split(":")
            val sNum = parts.getOrNull(0)?.trim()?.toIntOrNull()
            val aNum = parts.getOrNull(1)?.trim()?.toIntOrNull()
            if (sNum != null && aNum != null) {
                val surahAyahs = fetchSurahWithTafsir(sNum)
                surahAyahs.find { it.ayahNumber == aNum }?.let { results.add(it) }
            }
        }

        val asNumber = trimmed.toIntOrNull()

        // 2. Local search across all cached & bundled Surahs
        val allLocalAyahs = (memoryCache.values.flatten() + QuranCatalogData.getAllBundledAyahs())
            .distinctBy { it.referenceKey }

        for (ayah in allLocalAyahs) {
            val normText = QuranCatalogData.normalizeArabic(ayah.textUthmani)
            val normTafsir = QuranCatalogData.normalizeArabic(ayah.tafsirMuyassar)
            val normSurah = QuranCatalogData.normalizeArabic(ayah.surahName)
            val matchByNumber = asNumber != null && (ayah.ayahNumber == asNumber || ayah.surahNumber == asNumber)
            if (matchByNumber ||
                normText.contains(normalizedQuery) ||
                normTafsir.contains(normalizedQuery) ||
                normSurah.contains(normalizedQuery)
            ) {
                results.add(ayah)
            }
        }

        // 3. Remote API search across the entire Quran if query is an Arabic word
        if (asNumber == null && trimmed.length >= 2) {
            try {
                val encoded = URLEncoder.encode(trimmed, "UTF-8")
                val url = "https://api.alquran.cloud/v1/search/$encoded/all/quran-uthmani"
                val request = Request.Builder().url(url).get().build()
                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val bodyStr = response.body?.string()
                        if (!bodyStr.isNullOrBlank()) {
                            val root = JSONObject(bodyStr)
                            val matches = root.optJSONObject("data")?.optJSONArray("matches")
                            if (matches != null) {
                                val limit = minOf(matches.length(), 25)
                                for (i in 0 until limit) {
                                    val m = matches.getJSONObject(i)
                                    val surahObj = m.optJSONObject("surah")
                                    val sNum = surahObj?.optInt("number") ?: 1
                                    val sName = surahObj?.optString("name")?.removePrefix("سُورَةُ ")?.removePrefix("سورة ")?.trim()
                                        ?: QuranCatalogData.ALL_114_SURAHS.find { it.number == sNum }?.nameArabic
                                        ?: "سورة $sNum"
                                    val aNum = m.optInt("numberInSurah", 1)
                                    val key = "$sNum:$aNum"
                                    if (results.none { it.referenceKey == key }) {
                                        results.add(
                                            AyahItem(
                                                surahNumber = sNum,
                                                surahName = sName,
                                                ayahNumber = aNum,
                                                textUthmani = m.optString("text", ""),
                                                tafsirMuyassar = "انقر لفتح السورة وعرض التفسير الميسر الكامل والاستماع للتلاوة الصوتية.",
                                                juz = m.optInt("juz", 1),
                                                page = m.optInt("page", 1)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                // Keep local matches on network error
            }
        }

        results.distinctBy { it.referenceKey }
    }
}
