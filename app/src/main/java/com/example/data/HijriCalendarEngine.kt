package com.example.data

import com.example.data.model.HijriCalendarDay
import com.example.data.model.IslamicOccasion
import java.util.Calendar
import kotlin.math.floor

object HijriCalendarEngine {

    val HIJRI_MONTH_NAMES = listOf(
        "محرم",
        "صفر",
        "ربيع الأول",
        "ربيع الآخر",
        "جمادى الأولى",
        "جمادى الآخرة",
        "رجب",
        "شعبان",
        "رمضان",
        "شوال",
        "ذو القعدة",
        "ذو الحجة"
    )

    val GREGORIAN_MONTH_NAMES = listOf(
        "يناير",
        "فبراير",
        "مارس",
        "أبريل",
        "مايو",
        "يونيو",
        "يوليو",
        "أغسطس",
        "سبتمبر",
        "أكتوبر",
        "نوفمبر",
        "ديسمبر"
    )

    val ARABIC_WEEKDAYS = listOf(
        "الأحد",
        "الإثنين",
        "الثلاثاء",
        "الأربعاء",
        "الخميس",
        "الجمعة",
        "السبت"
    )

    val ISLAMIC_OCCASIONS = listOf(
        IslamicOccasion(1, 1, "رأس السنة الهجرية", "ذكرى الهجرة النبوية الشريفة وبداية العام الهجري الجديد.", true),
        IslamicOccasion(1, 9, "يوم تاسوعاء", "يستحب صيامه مع يوم عاشوراء مخالفةً لليهود.", false),
        IslamicOccasion(1, 10, "يوم عاشوراء", "اليوم الذي نجى الله فيه موسى عليه السلام؛ صيامه يكفر سنة ماضية.", true),
        IslamicOccasion(3, 12, "ذكرى المولد النبوي الشريف", "مولد سيد الخلق محمد بن عبد الله ﷺ في عام الفيل.", true),
        IslamicOccasion(7, 27, "ذكرى الإسراء والمعراج", "رحلة الإسراء إلى المسجد الأقصى والمعراج إلى السماوات العلى وفرض الصلوات الخمس.", true),
        IslamicOccasion(8, 15, "ليلة النصف من شعبان", "ليلة مباركة ترفع فيها الأعمال ويستحب فيها الدعاء والاستغفار.", false),
        IslamicOccasion(9, 1, "غرة شهر رمضان المبارك", "أول أيام شهر الصيام والقرآن والعتق من النيران.", true),
        IslamicOccasion(9, 17, "ذكرى غزوة بدر الكبرى", "يوم الفرقان الذي أعز الله فيه الإسلام وأهله.", false),
        IslamicOccasion(9, 20, "ذكرى فتح مكة", "الفتح الأعظم وتطهير البيت الحرام في السنة الثامنة للهجرة.", false),
        IslamicOccasion(9, 27, "ليلة القدر (الوتر المرجى)", "ليلة خير من ألف شهر تنزل فيها الملائكة والروح بإذن ربهم.", true),
        IslamicOccasion(10, 1, "عيد الفطر المبارك", "يوم الجائزة والفرح بإتمام صيام رمضان وزكاة الفطر.", true),
        IslamicOccasion(12, 8, "يوم التروية", "بداية مناسك الحج والتوجه إلى مشعر منى.", false),
        IslamicOccasion(12, 9, "وقفة عرفات (يوم عرفة)", "أعظم أيام السنة؛ صيامه لغير الحاج يكفر سنتين ماضية وباقية.", true),
        IslamicOccasion(12, 10, "عيد الأضحى المبارك", "يوم النحر الأكبر وأعظم الأيام عند الله تبارك وتعالى.", true),
        IslamicOccasion(12, 11, "أول أيام التشريق", "أيام أكل وشرب وذكر لله عز وجل.", false),
        IslamicOccasion(12, 12, "ثاني أيام التشريق", "من الأيام المعدودات المباركة لذكر الله.", false),
        IslamicOccasion(12, 13, "ثالث أيام التشريق", "ختام أيام التشريق ومناسك الحج.", false)
    )

    /**
     * Converts Gregorian Date (Year, Month 1..12, Day 1..31) to Hijri Triple (Year, Month 1..12, Day 1..30)
     * using the standard Julian Day Number (JDN) astronomical conversion.
     */
    fun gregorianToHijri(gYear: Int, gMonth: Int, gDay: Int): Triple<Int, Int, Int> {
        val jd = gregorianToJulianDay(gYear, gMonth, gDay)
        return julianDayToHijri(jd)
    }

    /**
     * Converts Hijri Date (Year, Month 1..12, Day 1..30) to Gregorian Triple (Year, Month 1..12, Day 1..31).
     */
    fun hijriToGregorian(hYear: Int, hMonth: Int, hDay: Int): Triple<Int, Int, Int> {
        val jd = hijriToJulianDay(hYear, hMonth, hDay)
        return julianDayToGregorian(jd)
    }

    private fun gregorianToJulianDay(year: Int, month: Int, day: Int): Int {
        val a = (14 - month) / 12
        val y = year + 4800 - a
        val m = month + 12 * a - 3
        return day + (153 * m + 2) / 5 + 365 * y + y / 4 - y / 100 + y / 400 - 32045
    }

    private fun julianDayToHijri(jd: Int): Triple<Int, Int, Int> {
        val l0 = jd - 1948440 + 10632
        val n = (l0 - 1) / 10631
        val l1 = l0 - 10631 * n + 354
        val j = ((10985 - l1) / 5316) * ((50 * l1) / 17719) + (l1 / 5670) * ((43 * l1) / 15238)
        val l2 = l1 - ((30 - j) / 15) * ((17719 * j) / 50) - (j / 16) * ((15238 * j) / 43) + 29
        val hMonth = (24 * l2) / 709
        val hDay = l2 - (709 * hMonth) / 24
        val hYear = 30 * n + j - 30
        return Triple(hYear, hMonth.coerceIn(1, 12), hDay.coerceIn(1, 30))
    }

    private fun hijriToJulianDay(hYear: Int, hMonth: Int, hDay: Int): Int {
        return floor((11 * hYear + 3) / 30.0).toInt() +
            354 * hYear +
            30 * hMonth -
            floor((hMonth - 1) / 2.0).toInt() +
            hDay + 1948440 - 385
    }

    private fun julianDayToGregorian(jd: Int): Triple<Int, Int, Int> {
        val l = jd + 68569
        val n = (4 * l) / 146097
        val l1 = l - (146097 * n + 3) / 4
        val i = (4000 * (l1 + 1)) / 1461001
        val l2 = l1 - (1461 * i) / 4 + 31
        val j = (80 * l2) / 2447
        val gDay = l2 - (2447 * j) / 80
        val l3 = j / 11
        val gMonth = j + 2 - 12 * l3
        val gYear = 100 * (n - 49) + i + l3
        return Triple(gYear, gMonth.coerceIn(1, 12), gDay.coerceIn(1, 31))
    }

    fun getTodayHijriDay(): HijriCalendarDay {
        val now = Calendar.getInstance()
        val gY = now.get(Calendar.YEAR)
        val gM = now.get(Calendar.MONTH) + 1
        val gD = now.get(Calendar.DAY_OF_MONTH)
        return buildCalendarDay(gY, gM, gD, isToday = true)
    }

    fun buildCalendarDay(gYear: Int, gMonth: Int, gDay: Int, isToday: Boolean = false): HijriCalendarDay {
        val (hYear, hMonth, hDay) = gregorianToHijri(gYear, gMonth, gDay)
        val cal = Calendar.getInstance().apply {
            set(gYear, gMonth - 1, gDay)
        }
        val dowIndex = (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
        val occasion = ISLAMIC_OCCASIONS.find { it.hijriMonth == hMonth && it.hijriDay == hDay }
        val isWhite = hDay in 13..15
        return HijriCalendarDay(
            gregorianYear = gYear,
            gregorianMonth = gMonth,
            gregorianDay = gDay,
            hijriYear = hYear,
            hijriMonth = hMonth,
            hijriMonthName = HIJRI_MONTH_NAMES[(hMonth - 1).coerceIn(0, 11)],
            hijriDay = hDay,
            dayOfWeekArabic = ARABIC_WEEKDAYS[dowIndex],
            isToday = isToday,
            isWhiteDay = isWhite,
            occasion = occasion
        )
    }

    fun generateGregorianMonthDays(gYear: Int, gMonth: Int): List<HijriCalendarDay> {
        val today = Calendar.getInstance()
        val tY = today.get(Calendar.YEAR)
        val tM = today.get(Calendar.MONTH) + 1
        val tD = today.get(Calendar.DAY_OF_MONTH)

        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, gYear)
            set(Calendar.MONTH, gMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        return (1..maxDays).map { day ->
            buildCalendarDay(
                gYear = gYear,
                gMonth = gMonth,
                gDay = day,
                isToday = (gYear == tY && gMonth == tM && day == tD)
            )
        }
    }

    fun getFirstDayOffsetInWeek(gYear: Int, gMonth: Int): Int {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, gYear)
            set(Calendar.MONTH, gMonth - 1)
            set(Calendar.DAY_OF_MONTH, 1)
        }
        // Sunday = 0 .. Saturday = 6
        return (cal.get(Calendar.DAY_OF_WEEK) - 1).coerceIn(0, 6)
    }
}
