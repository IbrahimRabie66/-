package com.example.data.model

import java.util.Locale

data class Reciter(
    val id: String,
    val arabicName: String,
    val styleTitle: String,
    val everyAyahFolder: String,
    val fullSurahServerUrl: String,
    val bio: String
) {
    fun getAyahAudioUrl(surahNumber: Int, ayahNumber: Int): String {
        val s = String.format(Locale.US, "%03d", surahNumber)
        val a = String.format(Locale.US, "%03d", ayahNumber)
        return "https://everyayah.com/data/$everyAyahFolder/$s$a.mp3"
    }

    fun getFullSurahAudioUrl(surahNumber: Int): String {
        val s = String.format(Locale.US, "%03d", surahNumber)
        return "$fullSurahServerUrl$s.mp3"
    }
}

val SUPPORTED_RECITERS = listOf(
    Reciter(
        id = "abdulbasit",
        arabicName = "الشيخ عبد الباسط عبد الصمد",
        styleTitle = "المصحف المرتل • صوت مكة الذهبي",
        everyAyahFolder = "Abdul_Basit_Murattal_192kbps",
        fullSurahServerUrl = "https://server7.mp3quran.net/basit/",
        bio = "من أشهر قراء القرآن الكريم في العالم الإسلامي، يتميز بنقاء الصوت وخشوع الأداء وإتقان أحكام التجويد."
    ),
    Reciter(
        id = "minshawi",
        arabicName = "الشيخ محمد صديق المنشاوي",
        styleTitle = "المصحف المرتل • الصوت الباكي الخاشع",
        everyAyahFolder = "Minshawy_Murattal_128kbps",
        fullSurahServerUrl = "https://server10.mp3quran.net/minsh/",
        bio = "إمام الخاشعين في التلاوة، عُرف بعذوبة الصوت والتدبر العميق لمعاني الآيات القرآنية."
    ),
    Reciter(
        id = "alafasy",
        arabicName = "الشيخ مشاري راشد العفاسي",
        styleTitle = "تلاوة مرتلة ندية • رواية حفص عن عاصم",
        everyAyahFolder = "Alafasy_128kbps",
        fullSurahServerUrl = "https://server8.mp3quran.net/afs/",
        bio = "إمام المسجد الكبير في الكويت، يتميز بأداء ندي واضح ومؤثر يساعد على الحفظ والتدبر."
    )
)

data class SurahMeta(
    val number: Int,
    val nameArabic: String,
    val nameTransliteration: String,
    val revelationType: String, // "مكية" or "مدنية"
    val ayahCount: Int,
    val juzStart: Int
)

data class AyahItem(
    val surahNumber: Int,
    val surahName: String,
    val ayahNumber: Int,
    val textUthmani: String,
    val tafsirMuyassar: String,
    val juz: Int = 1,
    val page: Int = 1
) {
    val referenceKey: String get() = "$surahNumber:$ayahNumber"
}

data class HadithItem(
    val id: Int,
    val bookId: String,
    val bookName: String,
    val chapterName: String,
    val hadithNumber: Int,
    val narrator: String,
    val sanad: String, // السند المتصل
    val matn: String, // متن الحديث الشريف
    val grade: String, // درجة الصحة
    val explanation: String // الفقه والشرح الميسر
)

data class SihahBookPdf(
    val id: String,
    val title: String,
    val author: String,
    val deathYearHijri: String,
    val hadithCountLabel: String,
    val description: String,
    val onlinePdfUrl: String,
    val accentHex: Long,
    val chaptersSummary: List<String>
)

data class IslamicOccasion(
    val hijriMonth: Int,
    val hijriDay: Int,
    val title: String,
    val description: String,
    val isMajorHoliday: Boolean = false
)

data class HijriCalendarDay(
    val gregorianYear: Int,
    val gregorianMonth: Int,
    val gregorianDay: Int,
    val hijriYear: Int,
    val hijriMonth: Int,
    val hijriMonthName: String,
    val hijriDay: Int,
    val dayOfWeekArabic: String,
    val isToday: Boolean,
    val isWhiteDay: Boolean, // الأيام البيض (13, 14, 15)
    val occasion: IslamicOccasion? = null
) {
    val gregorianDateKey: String
        get() = String.format(Locale.US, "%04d-%02d-%02d", gregorianYear, gregorianMonth, gregorianDay)

    val formattedHijri: String
        get() = "$hijriDay $hijriMonthName $hijriYear هـ"

    val formattedGregorian: String
        get() = String.format(Locale.US, "%04d/%02d/%02d م", gregorianYear, gregorianMonth, gregorianDay)
}
