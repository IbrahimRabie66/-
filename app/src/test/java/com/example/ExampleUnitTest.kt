package com.example

import com.example.data.HadithSihahCatalog
import com.example.data.HijriCalendarEngine
import com.example.data.QuranCatalogData
import com.example.data.model.SUPPORTED_RECITERS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun all114Surahs_andThreeReciters_areConfigured() {
        assertEquals(114, QuranCatalogData.ALL_114_SURAHS.size)
        assertEquals(3, SUPPORTED_RECITERS.size)
        val abdulBasit = SUPPORTED_RECITERS.first { it.id == "abdulbasit" }
        assertEquals(
            "https://everyayah.com/data/Abdul_Basit_Murattal_192kbps/002255.mp3",
            abdulBasit.getAyahAudioUrl(2, 255)
        )
    }

    @Test
    fun arabicNormalization_andHadithSearch_workCorrectly() {
        val normalized = QuranCatalogData.normalizeArabic("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ")
        assertTrue(normalized.contains("الرحمن"))
        val hadithMatches = HadithSihahCatalog.searchHadiths("النيات")
        assertTrue(hadithMatches.isNotEmpty())
        assertEquals(6, HadithSihahCatalog.SIHAH_BOOKS_PDF.size)
    }

    @Test
    fun hijriGregorianConversion_isReversible() {
        val (hY, hM, hD) = HijriCalendarEngine.gregorianToHijri(2026, 10, 26)
        assertTrue(hY >= 1448)
        assertTrue(hM in 1..12)
        assertTrue(hD in 1..30)

        val (gY, gM, gD) = HijriCalendarEngine.hijriToGregorian(hY, hM, hD)
        assertEquals(2026, gY)
        assertEquals(10, gM)
        assertTrue(kotlin.math.abs(26 - gD) <= 1)
    }
}
