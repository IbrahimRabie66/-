package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.audio.AudioPlaybackState
import com.example.audio.QuranAudioPlayer
import com.example.data.HadithSihahCatalog
import com.example.data.HijriCalendarEngine
import com.example.data.PdfBookEngine
import com.example.data.QuranCatalogData
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.CalendarNoteEntity
import com.example.data.model.AyahItem
import com.example.data.model.HadithItem
import com.example.data.model.HijriCalendarDay
import com.example.data.model.Reciter
import com.example.data.model.SUPPORTED_RECITERS
import com.example.data.model.SihahBookPdf
import com.example.data.model.SurahMeta
import com.example.data.remote.QuranRemoteService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

enum class MainDestination(val route: String, val titleArabic: String) {
    QURAN("quran", "القرآن الكريم"),
    HADITH_SIHAH("hadith", "الأحاديث والصحاح"),
    SEARCH("search", "البحث المنظم"),
    CALENDAR("calendar", "التقويم الهجري"),
    BOOKMARKS("bookmarks", "المحفوظات")
}

data class PdfReaderState(
    val activeBook: SihahBookPdf? = null,
    val isLoadingPdf: Boolean = false,
    val pages: List<Bitmap> = emptyList(),
    val currentPageIndex: Int = 0,
    val zoomScale: Float = 1f,
    val pdfFilePath: String? = null,
    val statusNotice: String? = null
)

data class DateConverterState(
    val isHijriToGregorian: Boolean = true,
    val inputDay: Int = 15,
    val inputMonth: Int = 9, // رمضان
    val inputYear: Int = 1448,
    val resultPrimary: String = "",
    val resultSecondary: String = ""
)

class IslamicPlatformViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).islamicDao()
    private val quranRemoteService = QuranRemoteService()
    private val audioPlayer = QuranAudioPlayer()

    val audioState: StateFlow<AudioPlaybackState> = audioPlayer.state

    // Theme Mode (Light vs Dark Islamic Mode)
    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Navigation State
    private val _currentDestination = MutableStateFlow(MainDestination.QURAN)
    val currentDestination: StateFlow<MainDestination> = _currentDestination.asStateFlow()

    // Quran Section State
    private val _surahSearchFilter = MutableStateFlow("")
    val surahSearchFilter: StateFlow<String> = _surahSearchFilter.asStateFlow()

    private val _revelationFilter = MutableStateFlow("الكل") // "الكل", "مكية", "مدنية"
    val revelationFilter: StateFlow<String> = _revelationFilter.asStateFlow()

    private val _selectedSurah = MutableStateFlow<SurahMeta?>(QuranCatalogData.ALL_114_SURAHS.first())
    val selectedSurah: StateFlow<SurahMeta?> = _selectedSurah.asStateFlow()

    private val _isSurahDetailOpen = MutableStateFlow(false)
    val isSurahDetailOpen: StateFlow<Boolean> = _isSurahDetailOpen.asStateFlow()

    private val _currentSurahAyahs = MutableStateFlow<List<AyahItem>>(
        QuranCatalogData.OFFLINE_SURAHS[1].orEmpty()
    )
    val currentSurahAyahs: StateFlow<List<AyahItem>> = _currentSurahAyahs.asStateFlow()

    private val _isLoadingSurah = MutableStateFlow(false)
    val isLoadingSurah: StateFlow<Boolean> = _isLoadingSurah.asStateFlow()

    private val _showTafsirMuyassar = MutableStateFlow(true)
    val showTafsirMuyassar: StateFlow<Boolean> = _showTafsirMuyassar.asStateFlow()

    private val _ayahSearchInsideSurah = MutableStateFlow("")
    val ayahSearchInsideSurah: StateFlow<String> = _ayahSearchInsideSurah.asStateFlow()

    // Hadith & Sihah PDF Section State
    private val _hadithSubTab = MutableStateFlow(0) // 0 = الأحاديث المسندة, 1 = كتب الصحاح PDF
    val hadithSubTab: StateFlow<Int> = _hadithSubTab.asStateFlow()

    private val _selectedHadithChapter = MutableStateFlow("الكل")
    val selectedHadithChapter: StateFlow<String> = _selectedHadithChapter.asStateFlow()

    private val _selectedHadithBookFilter = MutableStateFlow("الكل")
    val selectedHadithBookFilter: StateFlow<String> = _selectedHadithBookFilter.asStateFlow()

    private val _pdfReaderState = MutableStateFlow(PdfReaderState())
    val pdfReaderState: StateFlow<PdfReaderState> = _pdfReaderState.asStateFlow()

    // Global Search Section State
    private val _globalSearchQuery = MutableStateFlow("")
    val globalSearchQuery: StateFlow<String> = _globalSearchQuery.asStateFlow()

    private val _searchCategoryFilter = MutableStateFlow("الكل") // "الكل", "القرآن الكريم", "الأحاديث المسندة"
    val searchCategoryFilter: StateFlow<String> = _searchCategoryFilter.asStateFlow()

    private val _quranSearchResults = MutableStateFlow<List<AyahItem>>(
        QuranCatalogData.getAllBundledAyahs().take(8)
    )
    val quranSearchResults: StateFlow<List<AyahItem>> = _quranSearchResults.asStateFlow()

    private val _hadithSearchResults = MutableStateFlow<List<HadithItem>>(
        HadithSihahCatalog.MUSNAD_HADITHS
    )
    val hadithSearchResults: StateFlow<List<HadithItem>> = _hadithSearchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    // Hijri-Gregorian Calendar Section State
    private val todayCal = Calendar.getInstance()
    private val _displayedGregorianYear = MutableStateFlow(todayCal.get(Calendar.YEAR))
    val displayedGregorianYear: StateFlow<Int> = _displayedGregorianYear.asStateFlow()

    private val _displayedGregorianMonth = MutableStateFlow(todayCal.get(Calendar.MONTH) + 1)
    val displayedGregorianMonth: StateFlow<Int> = _displayedGregorianMonth.asStateFlow()

    private val _calendarMonthDays = MutableStateFlow<List<HijriCalendarDay>>(emptyList())
    val calendarMonthDays: StateFlow<List<HijriCalendarDay>> = _calendarMonthDays.asStateFlow()

    private val _selectedCalendarDay = MutableStateFlow<HijriCalendarDay>(HijriCalendarEngine.getTodayHijriDay())
    val selectedCalendarDay: StateFlow<HijriCalendarDay> = _selectedCalendarDay.asStateFlow()

    private val _dateConverterState = MutableStateFlow(DateConverterState())
    val dateConverterState: StateFlow<DateConverterState> = _dateConverterState.asStateFlow()

    // Room Flows: Bookmarks & Calendar Notes
    val bookmarksFlow: StateFlow<List<BookmarkEntity>> = dao.getAllBookmarks()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calendarNotesFlow: StateFlow<List<CalendarNoteEntity>> = dao.getAllCalendarNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        refreshCalendarMonth()
        recalculateDateConverter()
    }

    fun toggleDarkMode() {
        _isDarkMode.update { !it }
    }

    fun navigateTo(destination: MainDestination) {
        _currentDestination.value = destination
    }

    // --- Quran & Audio Recitation Actions ---
    fun updateSurahSearchFilter(query: String) {
        _surahSearchFilter.value = query
    }

    fun updateRevelationFilter(type: String) {
        _revelationFilter.value = type
    }

    fun toggleTafsirVisibility() {
        _showTafsirMuyassar.update { !it }
    }

    fun updateAyahSearchInsideSurah(query: String) {
        _ayahSearchInsideSurah.value = query
    }

    fun openSurah(surahNumber: Int, scrollToAyah: Int? = null) {
        val meta = QuranCatalogData.ALL_114_SURAHS.find { it.number == surahNumber }
            ?: QuranCatalogData.ALL_114_SURAHS.first()
        _selectedSurah.value = meta
        _isSurahDetailOpen.value = true
        _ayahSearchInsideSurah.value = scrollToAyah?.toString() ?: ""

        viewModelScope.launch {
            _isLoadingSurah.value = true
            val ayahs = quranRemoteService.fetchSurahWithTafsir(surahNumber)
            _currentSurahAyahs.value = ayahs
            _isLoadingSurah.value = false
        }
    }

    fun closeSurahDetail() {
        _isSurahDetailOpen.value = false
        _ayahSearchInsideSurah.value = ""
    }

    fun selectReciter(reciter: Reciter) {
        audioPlayer.selectReciter(reciter)
    }

    fun playAyah(ayah: AyahItem) {
        val currentList = if (_currentSurahAyahs.value.isNotEmpty() &&
            _currentSurahAyahs.value.first().surahNumber == ayah.surahNumber
        ) {
            _currentSurahAyahs.value
        } else {
            listOf(ayah)
        }
        audioPlayer.playAyahFromSurah(ayah, currentList)
    }

    fun playFullSurah(surahMeta: SurahMeta) {
        audioPlayer.playFullSurah(surahMeta.number, surahMeta.nameArabic)
    }

    fun toggleAudioPlayPause() {
        audioPlayer.togglePlayPause(_currentSurahAyahs.value)
    }

    fun skipNextAyah() {
        audioPlayer.skipToNextAyah()
    }

    fun skipPreviousAyah() {
        audioPlayer.skipToPreviousAyah()
    }

    fun stopAudio() {
        audioPlayer.stopPlayback()
    }

    // --- Hadith & Built-in Sihah PDF Reader Actions ---
    fun setHadithSubTab(tabIndex: Int) {
        _hadithSubTab.value = tabIndex
    }

    fun selectHadithChapter(chapter: String) {
        _selectedHadithChapter.value = chapter
    }

    fun selectHadithBookFilter(bookTitle: String) {
        _selectedHadithBookFilter.value = bookTitle
    }

    fun openSihahPdfBook(book: SihahBookPdf, initialPage: Int = 0) {
        _pdfReaderState.update {
            it.copy(
                activeBook = book,
                isLoadingPdf = true,
                pages = emptyList(),
                currentPageIndex = initialPage,
                zoomScale = 1f,
                statusNotice = "جاري تجهيز وعرض صفحات ${book.title} بصيغة PDF..."
            )
        }
        viewModelScope.launch {
            try {
                val pdfFile = PdfBookEngine.getOrGenerateBookPdfFile(getApplication(), book)
                val renderedBitmaps = PdfBookEngine.renderPdfPagesToBitmaps(pdfFile)
                _pdfReaderState.update {
                    it.copy(
                        isLoadingPdf = false,
                        pages = renderedBitmaps,
                        currentPageIndex = initialPage.coerceIn(0, (renderedBitmaps.size - 1).coerceAtLeast(0)),
                        pdfFilePath = pdfFile.absolutePath,
                        statusNotice = "تم فتح النسخة الإلكترونية (${renderedBitmaps.size} صفحات PDF عالية الدقة)"
                    )
                }
            } catch (e: Exception) {
                _pdfReaderState.update {
                    it.copy(
                        isLoadingPdf = false,
                        statusNotice = "تعذر توليد صفحات الـ PDF: ${e.localizedMessage}"
                    )
                }
            }
        }
    }

    fun closePdfReader() {
        _pdfReaderState.value = PdfReaderState()
    }

    fun goToPdfPage(pageIndex: Int) {
        val total = _pdfReaderState.value.pages.size
        if (total > 0) {
            _pdfReaderState.update {
                it.copy(currentPageIndex = pageIndex.coerceIn(0, total - 1))
            }
        }
    }

    fun adjustPdfZoom(delta: Float) {
        _pdfReaderState.update {
            it.copy(zoomScale = (it.zoomScale + delta).coerceIn(1f, 2.5f))
        }
    }

    // --- Organized Search Actions (Ayah or Keyword) ---
    fun updateGlobalSearchQuery(query: String) {
        _globalSearchQuery.value = query
        executeGlobalSearch(query)
    }

    fun updateSearchCategoryFilter(category: String) {
        _searchCategoryFilter.value = category
    }

    fun executeGlobalSearch(query: String = _globalSearchQuery.value) {
        val trimmed = query.trim()
        if (trimmed.isBlank()) {
            _quranSearchResults.value = QuranCatalogData.getAllBundledAyahs().take(10)
            _hadithSearchResults.value = HadithSihahCatalog.MUSNAD_HADITHS
            _isSearching.value = false
            return
        }

        _hadithSearchResults.value = HadithSihahCatalog.searchHadiths(trimmed)
        viewModelScope.launch {
            _isSearching.value = true
            val qResults = quranRemoteService.searchQuran(trimmed)
            _quranSearchResults.value = qResults
            _isSearching.value = false
        }
    }

    // --- Hijri & Gregorian Calendar + Converter + Personal Notes Actions ---
    private fun refreshCalendarMonth() {
        val days = HijriCalendarEngine.generateGregorianMonthDays(
            _displayedGregorianYear.value,
            _displayedGregorianMonth.value
        )
        _calendarMonthDays.value = days
    }

    fun changeCalendarMonth(deltaMonths: Int) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.YEAR, _displayedGregorianYear.value)
            set(Calendar.MONTH, _displayedGregorianMonth.value - 1)
            set(Calendar.DAY_OF_MONTH, 1)
            add(Calendar.MONTH, deltaMonths)
        }
        _displayedGregorianYear.value = cal.get(Calendar.YEAR)
        _displayedGregorianMonth.value = cal.get(Calendar.MONTH) + 1
        refreshCalendarMonth()
    }

    fun jumpCalendarToToday() {
        val now = Calendar.getInstance()
        _displayedGregorianYear.value = now.get(Calendar.YEAR)
        _displayedGregorianMonth.value = now.get(Calendar.MONTH) + 1
        refreshCalendarMonth()
        _selectedCalendarDay.value = HijriCalendarEngine.getTodayHijriDay()
    }

    fun selectCalendarDay(day: HijriCalendarDay) {
        _selectedCalendarDay.value = day
    }

    fun setConverterDirection(hijriToGregorian: Boolean) {
        val today = HijriCalendarEngine.getTodayHijriDay()
        _dateConverterState.update {
            if (hijriToGregorian) {
                it.copy(
                    isHijriToGregorian = true,
                    inputDay = today.hijriDay,
                    inputMonth = today.hijriMonth,
                    inputYear = today.hijriYear
                )
            } else {
                it.copy(
                    isHijriToGregorian = false,
                    inputDay = today.gregorianDay,
                    inputMonth = today.gregorianMonth,
                    inputYear = today.gregorianYear
                )
            }
        }
        recalculateDateConverter()
    }

    fun updateConverterInput(day: Int? = null, month: Int? = null, year: Int? = null) {
        _dateConverterState.update { current ->
            val maxM = 12
            val newM = (month ?: current.inputMonth).coerceIn(1, maxM)
            val maxD = if (current.isHijriToGregorian) 30 else 31
            val newD = (day ?: current.inputDay).coerceIn(1, maxD)
            val newY = (year ?: current.inputYear).coerceIn(1, 3000)
            current.copy(inputDay = newD, inputMonth = newM, inputYear = newY)
        }
        recalculateDateConverter()
    }

    private fun recalculateDateConverter() {
        val st = _dateConverterState.value
        if (st.isHijriToGregorian) {
            val (gY, gM, gD) = HijriCalendarEngine.hijriToGregorian(st.inputYear, st.inputMonth, st.inputDay)
            val gMonthName = HijriCalendarEngine.GREGORIAN_MONTH_NAMES[(gM - 1).coerceIn(0, 11)]
            val dayObj = HijriCalendarEngine.buildCalendarDay(gY, gM, gD)
            val hMonthName = HijriCalendarEngine.HIJRI_MONTH_NAMES[(st.inputMonth - 1).coerceIn(0, 11)]
            _dateConverterState.update {
                it.copy(
                    resultPrimary = "${dayObj.dayOfWeekArabic}، $gD $gMonthName $gY م",
                    resultSecondary = "الموافق لـ ${st.inputDay} $hMonthName ${st.inputYear} هـ" +
                        (if (st.inputDay in 13..15) " • من الأيام البيض المباركة" else "")
                )
            }
        } else {
            val dayObj = HijriCalendarEngine.buildCalendarDay(st.inputYear, st.inputMonth, st.inputDay)
            val gMonthName = HijriCalendarEngine.GREGORIAN_MONTH_NAMES[(st.inputMonth - 1).coerceIn(0, 11)]
            _dateConverterState.update {
                it.copy(
                    resultPrimary = "${dayObj.dayOfWeekArabic}، ${dayObj.hijriDay} ${dayObj.hijriMonthName} ${dayObj.hijriYear} هـ",
                    resultSecondary = "الموافق لـ ${st.inputDay} $gMonthName ${st.inputYear} م" +
                        (if (dayObj.isWhiteDay) " • من الأيام البيض المباركة" else "") +
                        (dayObj.occasion?.let { occ -> " • ${occ.title}" } ?: "")
                )
            }
        }
    }

    fun addCalendarNote(title: String, details: String, category: String) {
        val day = _selectedCalendarDay.value
        if (title.isBlank()) return
        viewModelScope.launch {
            dao.insertCalendarNote(
                CalendarNoteEntity(
                    gregorianDateKey = day.gregorianDateKey,
                    hijriDateDisplay = "${day.dayOfWeekArabic} ${day.formattedHijri}",
                    title = title.trim(),
                    noteDetails = details.trim(),
                    category = category
                )
            )
        }
    }

    fun deleteCalendarNote(noteId: Int) {
        viewModelScope.launch {
            dao.deleteCalendarNote(noteId)
        }
    }

    // --- Bookmark Actions ---
    fun toggleAyahBookmark(ayah: AyahItem) {
        val key = "QURAN_${ayah.surahNumber}_${ayah.ayahNumber}"
        val exists = bookmarksFlow.value.any { it.idKey == key }
        viewModelScope.launch {
            if (exists) {
                dao.deleteBookmarkByKey(key)
            } else {
                dao.insertBookmark(
                    BookmarkEntity(
                        idKey = key,
                        itemType = "QURAN",
                        title = "سورة ${ayah.surahName} • الآية ${ayah.ayahNumber}",
                        subtitle = "الجزء ${ayah.juz} • صفحة ${ayah.page}",
                        contentUthmani = ayah.textUthmani,
                        secondaryInfo = ayah.tafsirMuyassar,
                        surahNumber = ayah.surahNumber,
                        ayahNumber = ayah.ayahNumber
                    )
                )
            }
        }
    }

    fun toggleHadithBookmark(hadith: HadithItem) {
        val key = "HADITH_${hadith.id}"
        val exists = bookmarksFlow.value.any { it.idKey == key }
        viewModelScope.launch {
            if (exists) {
                dao.deleteBookmarkByKey(key)
            } else {
                dao.insertBookmark(
                    BookmarkEntity(
                        idKey = key,
                        itemType = "HADITH",
                        title = "${hadith.bookName} • حديث رقم (${hadith.hadithNumber})",
                        subtitle = "${hadith.chapterName} • الراوي: ${hadith.narrator}",
                        contentUthmani = hadith.matn,
                        secondaryInfo = "السند: ${hadith.sanad}\nالحكم: ${hadith.grade}"
                    )
                )
            }
        }
    }

    fun removeBookmark(idKey: String) {
        viewModelScope.launch {
            dao.deleteBookmarkByKey(idKey)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.releasePlayer()
    }
}
