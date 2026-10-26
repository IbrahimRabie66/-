package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.LibraryBooks
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.HijriCalendarEngine
import com.example.ui.IslamicPlatformViewModel
import com.example.ui.MainDestination
import com.example.ui.screens.BookmarksSectionScreen
import com.example.ui.screens.FloatingQuranAudioPlayerBar
import com.example.ui.screens.HadithAndPdfScreen
import com.example.ui.screens.HijriCalendarScreen
import com.example.ui.screens.QuranSectionScreen
import com.example.ui.screens.SearchSectionScreen
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicAmiriFontFamily
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PetrolDark
import com.example.ui.theme.PetrolPrimary
import com.example.ui.theme.PureWhite

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: IslamicPlatformViewModel = viewModel()
            val isDarkMode by vm.isDarkMode.collectAsStateWithLifecycle()

            MyApplicationTheme(darkTheme = isDarkMode) {
                AlMishkatIslamicApp(viewModel = vm)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlMishkatIslamicApp(viewModel: IslamicPlatformViewModel) {
    val currentDestination by viewModel.currentDestination.collectAsStateWithLifecycle()
    val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
    val audioState by viewModel.audioState.collectAsStateWithLifecycle()
    val bookmarks by viewModel.bookmarksFlow.collectAsStateWithLifecycle()
    val calendarNotes by viewModel.calendarNotesFlow.collectAsStateWithLifecycle()

    // Quran states
    val surahSearchFilter by viewModel.surahSearchFilter.collectAsStateWithLifecycle()
    val revelationFilter by viewModel.revelationFilter.collectAsStateWithLifecycle()
    val selectedSurah by viewModel.selectedSurah.collectAsStateWithLifecycle()
    val isSurahDetailOpen by viewModel.isSurahDetailOpen.collectAsStateWithLifecycle()
    val currentSurahAyahs by viewModel.currentSurahAyahs.collectAsStateWithLifecycle()
    val isLoadingSurah by viewModel.isLoadingSurah.collectAsStateWithLifecycle()
    val showTafsirMuyassar by viewModel.showTafsirMuyassar.collectAsStateWithLifecycle()
    val ayahSearchInsideSurah by viewModel.ayahSearchInsideSurah.collectAsStateWithLifecycle()

    // Hadith & PDF states
    val hadithSubTab by viewModel.hadithSubTab.collectAsStateWithLifecycle()
    val selectedHadithChapter by viewModel.selectedHadithChapter.collectAsStateWithLifecycle()
    val selectedHadithBookFilter by viewModel.selectedHadithBookFilter.collectAsStateWithLifecycle()
    val pdfReaderState by viewModel.pdfReaderState.collectAsStateWithLifecycle()

    // Search states
    val globalSearchQuery by viewModel.globalSearchQuery.collectAsStateWithLifecycle()
    val searchCategoryFilter by viewModel.searchCategoryFilter.collectAsStateWithLifecycle()
    val quranSearchResults by viewModel.quranSearchResults.collectAsStateWithLifecycle()
    val hadithSearchResults by viewModel.hadithSearchResults.collectAsStateWithLifecycle()
    val isSearching by viewModel.isSearching.collectAsStateWithLifecycle()

    // Calendar states
    val displayedGregorianYear by viewModel.displayedGregorianYear.collectAsStateWithLifecycle()
    val displayedGregorianMonth by viewModel.displayedGregorianMonth.collectAsStateWithLifecycle()
    val calendarMonthDays by viewModel.calendarMonthDays.collectAsStateWithLifecycle()
    val selectedCalendarDay by viewModel.selectedCalendarDay.collectAsStateWithLifecycle()
    val dateConverterState by viewModel.dateConverterState.collectAsStateWithLifecycle()

    val todayHijri = remember { HijriCalendarEngine.getTodayHijriDay() }

    // BackHandler for non-home tabs
    if (currentDestination != MainDestination.QURAN) {
        BackHandler {
            viewModel.navigateTo(MainDestination.QURAN)
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                TopAppBar(
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "منصة المشكاة الإسلامية",
                                    fontFamily = IslamicAmiriFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 24.sp,
                                    color = PureWhite
                                )
                                Text(
                                    text = "${todayHijri.formattedHijri} • ${todayHijri.formattedGregorian}",
                                    fontSize = 11.sp,
                                    color = OrangeLight,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    },
                    actions = {
                        Surface(
                            color = EmeraldPrimary,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.padding(end = 6.dp)
                        ) {
                            Text(
                                text = audioState.selectedReciter.arabicName.removePrefix("الشيخ "),
                                color = PureWhite,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        IconButton(
                            onClick = { viewModel.toggleDarkMode() },
                            modifier = Modifier.testTag("toggle_theme_btn")
                        ) {
                            Icon(
                                imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                                contentDescription = "تبديل الوضع الليلي والنهاري",
                                tint = OrangeLight
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = PetrolPrimary,
                        titleContentColor = PureWhite
                    )
                )
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    NavigationBar(
                        containerColor = PetrolDark,
                        contentColor = PureWhite
                    ) {
                        MainDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationBarItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest) },
                                modifier = Modifier.testTag("nav_tab_${dest.route}"),
                                icon = {
                                    Icon(
                                        imageVector = destinationIcon(dest),
                                        contentDescription = dest.titleArabic,
                                        modifier = Modifier.size(22.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = dest.titleArabic,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = PureWhite,
                                    selectedTextColor = OrangeLight,
                                    indicatorColor = EmeraldPrimary,
                                    unselectedIconColor = PureWhite.copy(alpha = 0.7f),
                                    unselectedTextColor = PureWhite.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background)
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = PetrolDark,
                        contentColor = PureWhite,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        MainDestination.entries.forEach { dest ->
                            val selected = currentDestination == dest
                            NavigationRailItem(
                                selected = selected,
                                onClick = { viewModel.navigateTo(dest) },
                                modifier = Modifier.testTag("nav_rail_${dest.route}"),
                                icon = {
                                    Icon(
                                        imageVector = destinationIcon(dest),
                                        contentDescription = dest.titleArabic
                                    )
                                },
                                label = { Text(dest.titleArabic, fontSize = 11.sp) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = PureWhite,
                                    selectedTextColor = OrangeLight,
                                    indicatorColor = EmeraldPrimary,
                                    unselectedIconColor = PureWhite.copy(alpha = 0.7f),
                                    unselectedTextColor = PureWhite.copy(alpha = 0.7f)
                                )
                            )
                        }
                    }
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (currentDestination) {
                        MainDestination.QURAN -> {
                            QuranSectionScreen(
                                surahSearchFilter = surahSearchFilter,
                                revelationFilter = revelationFilter,
                                selectedSurah = selectedSurah,
                                isSurahDetailOpen = isSurahDetailOpen,
                                currentSurahAyahs = currentSurahAyahs,
                                isLoadingSurah = isLoadingSurah,
                                showTafsirMuyassar = showTafsirMuyassar,
                                ayahSearchInsideSurah = ayahSearchInsideSurah,
                                audioState = audioState,
                                bookmarks = bookmarks,
                                onUpdateSurahFilter = viewModel::updateSurahSearchFilter,
                                onUpdateRevelationFilter = viewModel::updateRevelationFilter,
                                onOpenSurah = { surahNum -> viewModel.openSurah(surahNum) },
                                onCloseSurahDetail = viewModel::closeSurahDetail,
                                onToggleTafsir = viewModel::toggleTafsirVisibility,
                                onUpdateAyahInsideFilter = viewModel::updateAyahSearchInsideSurah,
                                onSelectReciter = viewModel::selectReciter,
                                onPlayAyah = viewModel::playAyah,
                                onPlayFullSurah = viewModel::playFullSurah,
                                onToggleBookmark = viewModel::toggleAyahBookmark
                            )
                        }

                        MainDestination.HADITH_SIHAH -> {
                            HadithAndPdfScreen(
                                subTab = hadithSubTab,
                                selectedChapter = selectedHadithChapter,
                                selectedBookFilter = selectedHadithBookFilter,
                                pdfReaderState = pdfReaderState,
                                bookmarks = bookmarks,
                                onSelectSubTab = viewModel::setHadithSubTab,
                                onSelectChapter = viewModel::selectHadithChapter,
                                onSelectBookFilter = viewModel::selectHadithBookFilter,
                                onOpenSihahPdf = viewModel::openSihahPdfBook,
                                onClosePdfReader = viewModel::closePdfReader,
                                onGoToPdfPage = viewModel::goToPdfPage,
                                onAdjustPdfZoom = viewModel::adjustPdfZoom,
                                onToggleHadithBookmark = viewModel::toggleHadithBookmark
                            )
                        }

                        MainDestination.SEARCH -> {
                            SearchSectionScreen(
                                searchQuery = globalSearchQuery,
                                categoryFilter = searchCategoryFilter,
                                quranResults = quranSearchResults,
                                hadithResults = hadithSearchResults,
                                isSearching = isSearching,
                                bookmarks = bookmarks,
                                onUpdateQuery = viewModel::updateGlobalSearchQuery,
                                onUpdateCategory = viewModel::updateSearchCategoryFilter,
                                onPlayAyah = viewModel::playAyah,
                                onOpenSurahAtAyah = { sNum, aNum ->
                                    viewModel.navigateTo(MainDestination.QURAN)
                                    viewModel.openSurah(sNum, aNum)
                                },
                                onToggleAyahBookmark = viewModel::toggleAyahBookmark,
                                onToggleHadithBookmark = viewModel::toggleHadithBookmark
                            )
                        }

                        MainDestination.CALENDAR -> {
                            HijriCalendarScreen(
                                displayedGregorianYear = displayedGregorianYear,
                                displayedGregorianMonth = displayedGregorianMonth,
                                monthDays = calendarMonthDays,
                                selectedDay = selectedCalendarDay,
                                converterState = dateConverterState,
                                calendarNotes = calendarNotes,
                                onChangeMonth = viewModel::changeCalendarMonth,
                                onJumpToToday = viewModel::jumpCalendarToToday,
                                onSelectDay = viewModel::selectCalendarDay,
                                onSetConverterDirection = viewModel::setConverterDirection,
                                onUpdateConverterInput = viewModel::updateConverterInput,
                                onAddNote = viewModel::addCalendarNote,
                                onDeleteNote = viewModel::deleteCalendarNote
                            )
                        }

                        MainDestination.BOOKMARKS -> {
                            BookmarksSectionScreen(
                                bookmarks = bookmarks,
                                onOpenQuranSurah = { sNum, aNum ->
                                    viewModel.navigateTo(MainDestination.QURAN)
                                    viewModel.openSurah(sNum, aNum)
                                },
                                onRemoveBookmark = viewModel::removeBookmark
                            )
                        }
                    }

                    // Persistent Floating Quran Audio Player Bar at bottom of content
                    if (pdfReaderState.activeBook == null) {
                        FloatingQuranAudioPlayerBar(
                            audioState = audioState,
                            onSelectReciter = viewModel::selectReciter,
                            onTogglePlayPause = viewModel::toggleAudioPlayPause,
                            onNextAyah = viewModel::skipNextAyah,
                            onPreviousAyah = viewModel::skipPreviousAyah,
                            onStop = viewModel::stopAudio,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }
            }
        }
    }
}

private fun destinationIcon(destination: MainDestination): ImageVector {
    return when (destination) {
        MainDestination.QURAN -> Icons.AutoMirrored.Filled.MenuBook
        MainDestination.HADITH_SIHAH -> Icons.AutoMirrored.Filled.LibraryBooks
        MainDestination.SEARCH -> Icons.Default.Search
        MainDestination.CALENDAR -> Icons.Default.CalendarMonth
        MainDestination.BOOKMARKS -> Icons.Default.Bookmarks
    }
}
