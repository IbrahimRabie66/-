package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.PauseCircleFilled
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.data.QuranCatalogData
import com.example.data.local.BookmarkEntity
import com.example.data.model.AyahItem
import com.example.data.model.Reciter
import com.example.data.model.SUPPORTED_RECITERS
import com.example.data.model.SurahMeta
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicAmiriFontFamily
import com.example.ui.theme.IslamicBlack
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PetrolDark
import com.example.ui.theme.PetrolMedium
import com.example.ui.theme.PetrolPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.UthmaniFontFamily

@Composable
fun QuranSectionScreen(
    surahSearchFilter: String,
    revelationFilter: String,
    selectedSurah: SurahMeta?,
    isSurahDetailOpen: Boolean,
    currentSurahAyahs: List<AyahItem>,
    isLoadingSurah: Boolean,
    showTafsirMuyassar: Boolean,
    ayahSearchInsideSurah: String,
    audioState: AudioPlaybackState,
    bookmarks: List<BookmarkEntity>,
    onUpdateSurahFilter: (String) -> Unit,
    onUpdateRevelationFilter: (String) -> Unit,
    onOpenSurah: (Int) -> Unit,
    onCloseSurahDetail: () -> Unit,
    onToggleTafsir: () -> Unit,
    onUpdateAyahInsideFilter: (String) -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onPlayAyah: (AyahItem) -> Unit,
    onPlayFullSurah: (SurahMeta) -> Unit,
    onToggleBookmark: (AyahItem) -> Unit
) {
    if (isSurahDetailOpen && selectedSurah != null) {
        BackHandler {
            onCloseSurahDetail()
        }
        SurahDetailReaderView(
            surah = selectedSurah,
            ayahs = currentSurahAyahs,
            isLoading = isLoadingSurah,
            showTafsir = showTafsirMuyassar,
            ayahSearchQuery = ayahSearchInsideSurah,
            audioState = audioState,
            bookmarks = bookmarks,
            onBack = onCloseSurahDetail,
            onToggleTafsir = onToggleTafsir,
            onUpdateAyahQuery = onUpdateAyahInsideFilter,
            onSelectReciter = onSelectReciter,
            onPlayAyah = onPlayAyah,
            onPlayFullSurah = { onPlayFullSurah(selectedSurah) },
            onToggleBookmark = onToggleBookmark
        )
    } else {
        SurahIndexView(
            surahSearchFilter = surahSearchFilter,
            revelationFilter = revelationFilter,
            audioState = audioState,
            onUpdateSurahFilter = onUpdateSurahFilter,
            onUpdateRevelationFilter = onUpdateRevelationFilter,
            onSelectReciter = onSelectReciter,
            onOpenSurah = onOpenSurah,
            onPlayFullSurah = onPlayFullSurah
        )
    }
}

@Composable
private fun SurahIndexView(
    surahSearchFilter: String,
    revelationFilter: String,
    audioState: AudioPlaybackState,
    onUpdateSurahFilter: (String) -> Unit,
    onUpdateRevelationFilter: (String) -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onOpenSurah: (Int) -> Unit,
    onPlayFullSurah: (SurahMeta) -> Unit
) {
    val filteredSurahs = remember(surahSearchFilter, revelationFilter) {
        val normFilter = QuranCatalogData.normalizeArabic(surahSearchFilter)
        val numQuery = surahSearchFilter.trim().toIntOrNull()
        QuranCatalogData.ALL_114_SURAHS.filter { s ->
            val matchesRev = revelationFilter == "الكل" || s.revelationType == revelationFilter
            val matchesQuery = surahSearchFilter.isBlank() ||
                (numQuery != null && s.number == numQuery) ||
                QuranCatalogData.normalizeArabic(s.nameArabic).contains(normFilter) ||
                s.nameTransliteration.contains(surahSearchFilter, ignoreCase = true)
            matchesRev && matchesQuery
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("quran_surah_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Ornate Islamic Hero Card & Reciters Selector
        item {
            IslamicQuranHeroBanner(
                selectedReciter = audioState.selectedReciter,
                onSelectReciter = onSelectReciter
            )
        }

        // 2. Quick Access Featured Surahs & Ayat Al-Kursi
        item {
            QuickQuranAccessRow(onOpenSurah = onOpenSurah)
        }

        // 3. Search & Filter Bar for the 114 Surahs
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = surahSearchFilter,
                        onValueChange = onUpdateSurahFilter,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("surah_filter_input"),
                        placeholder = { Text("ابحث باسم السورة أو رقمها (١ - ١١٤)...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "بحث في السور",
                                tint = EmeraldPrimary
                            )
                        },
                        trailingIcon = {
                            if (surahSearchFilter.isNotEmpty()) {
                                IconButton(onClick = { onUpdateSurahFilter("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = EmeraldPrimary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf("الكل", "مكية", "مدنية").forEach { filterType ->
                            val selected = revelationFilter == filterType
                            FilterChip(
                                selected = selected,
                                onClick = { onUpdateRevelationFilter(filterType) },
                                label = {
                                    Text(
                                        text = if (filterType == "الكل") "كل السور (١١٤)" else "السور ال$filterType",
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PetrolPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }
        }

        // 4. List of Surahs
        items(filteredSurahs, key = { it.number }) { surah ->
            val isCurrentlyPlayingSurah =
                (audioState.isPlaying || audioState.isBuffering) && audioState.currentSurahNumber == surah.number

            SurahCardItem(
                surah = surah,
                isPlaying = isCurrentlyPlayingSurah,
                reciterName = audioState.selectedReciter.arabicName,
                onOpen = { onOpenSurah(surah.number) },
                onPlayFull = { onPlayFullSurah(surah) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun IslamicQuranHeroBanner(
    selectedReciter: Reciter,
    onSelectReciter: (Reciter) -> Unit
) {
    Card(
        shape = RoundedCornerShape(22.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(PetrolDark, PetrolPrimary, EmeraldPrimary)
                    )
                )
        ) {
            // Subtle Geometric Islamic Arches & Circles Canvas Overlay
            Canvas(modifier = Modifier.matchParentSize()) {
                val strokeColor = Color(0x22E67E22)
                drawCircle(
                    color = strokeColor,
                    radius = size.minDimension * 0.48f,
                    center = Offset(size.width * 0.15f, size.height * 0.25f),
                    style = Stroke(width = 3f)
                )
                drawCircle(
                    color = Color(0x18FFFFFF),
                    radius = size.minDimension * 0.35f,
                    center = Offset(size.width * 0.85f, size.height * 0.8f),
                    style = Stroke(width = 2f)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = OrangeAccent,
                        shape = RoundedCornerShape(50),
                    ) {
                        Text(
                            text = "المصحف المرتل والتفسير الميسر",
                            color = PureWhite,
                            style = MaterialTheme.typography.labelLarge,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }

                    Text(
                        text = "﴿ إِنَّ هَٰذَا الْقُرْآنَ يَهْدِي لِلَّتِي هِيَ أَقْوَمُ ﴾",
                        fontFamily = UthmaniFontFamily,
                        fontSize = 18.sp,
                        color = OrangeLight,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = "اختر الشيخ القارئ للاستماع الفوري",
                    style = MaterialTheme.typography.titleMedium,
                    color = PureWhite
                )
                Text(
                    text = selectedReciter.bio,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFD5E8E2)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 3 Requested Reciters Chips
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SUPPORTED_RECITERS.forEach { reciter ->
                        val isSelected = reciter.id == selectedReciter.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) OrangeAccent else IslamicBlack.copy(alpha = 0.55f),
                            border = BorderStroke(
                                width = 1.5.dp,
                                color = if (isSelected) PureWhite else EmeraldPrimary
                            ),
                            modifier = Modifier
                                .clickable { onSelectReciter(reciter) }
                                .testTag("reciter_chip_${reciter.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.Headphones,
                                    contentDescription = reciter.arabicName,
                                    tint = PureWhite,
                                    modifier = Modifier.size(18.dp)
                                )
                                Column {
                                    Text(
                                        text = reciter.arabicName,
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                    Text(
                                        text = reciter.styleTitle,
                                        color = PureWhite.copy(alpha = 0.85f),
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickQuranAccessRow(onOpenSurah: (Int) -> Unit) {
    val quickItems = listOf(
        Triple(1, "سورة الفاتحة", "السبع المثاني"),
        Triple(2, "آية الكرسي والبقرة", "أعظم آية"),
        Triple(18, "سورة الكهف", "نور ما بين الجمعتين"),
        Triple(36, "سورة يس", "قلب القرآن"),
        Triple(55, "سورة الرحمن", "عروس القرآن"),
        Triple(67, "سورة الملك", "المانعة من عذاب القبر"),
        Triple(112, "الإخلاص والمعوذتين", "أذكار الحفظ")
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "وصول سريع للسور والآيات المباركة",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            quickItems.forEach { (surahNum, label, sub) ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = PetrolPrimary,
                    border = BorderStroke(1.dp, EmeraldPrimary),
                    modifier = Modifier.clickable { onOpenSurah(surahNum) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = label,
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = sub,
                            color = OrangeLight,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SurahCardItem(
    surah: SurahMeta,
    isPlaying: Boolean,
    reciterName: String,
    onOpen: () -> Unit,
    onPlayFull: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpen() }
            .testTag("surah_card_${surah.number}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) PetrolPrimary else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isPlaying) 2.dp else 1.dp,
            color = if (isPlaying) OrangeAccent else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                // Ornate Number Badge
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isPlaying) OrangeAccent else PetrolPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "${surah.number}",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "سُورَةُ ${surah.nameArabic}",
                            fontFamily = IslamicAmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = if (isPlaying) PureWhite else MaterialTheme.colorScheme.onSurface
                        )
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (surah.revelationType == "مكية") EmeraldPrimary.copy(alpha = 0.16f)
                            else OrangeAccent.copy(alpha = 0.18f)
                        ) {
                            Text(
                                text = surah.revelationType,
                                color = if (isPlaying) OrangeLight else if (surah.revelationType == "مكية") EmeraldPrimary else OrangeAccent,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }

                    Text(
                        text = "عدد آياتها: ${surah.ayahCount} آية • الجزء ${surah.juzStart}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isPlaying) Color(0xFFD5E8E2) else MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (isPlaying) {
                        Text(
                            text = "جاري التلاوة بصوت $reciterName",
                            color = OrangeLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onPlayFull,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(EmeraldPrimary)
                        .testTag("play_surah_btn_${surah.number}")
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.GraphicEq else Icons.Default.PlayArrow,
                        contentDescription = "تشغيل سورة ${surah.nameArabic}",
                        tint = PureWhite
                    )
                }
            }
        }
    }
}

@Composable
private fun SurahDetailReaderView(
    surah: SurahMeta,
    ayahs: List<AyahItem>,
    isLoading: Boolean,
    showTafsir: Boolean,
    ayahSearchQuery: String,
    audioState: AudioPlaybackState,
    bookmarks: List<BookmarkEntity>,
    onBack: () -> Unit,
    onToggleTafsir: () -> Unit,
    onUpdateAyahQuery: (String) -> Unit,
    onSelectReciter: (Reciter) -> Unit,
    onPlayAyah: (AyahItem) -> Unit,
    onPlayFullSurah: () -> Unit,
    onToggleBookmark: (AyahItem) -> Unit
) {
    val context = LocalContext.current
    val filteredAyahs = remember(ayahs, ayahSearchQuery) {
        if (ayahSearchQuery.isBlank()) {
            ayahs
        } else {
            val normQ = QuranCatalogData.normalizeArabic(ayahSearchQuery)
            val numQ = ayahSearchQuery.trim().toIntOrNull()
            ayahs.filter { a ->
                (numQ != null && a.ayahNumber == numQ) ||
                    QuranCatalogData.normalizeArabic(a.textUthmani).contains(normQ) ||
                    QuranCatalogData.normalizeArabic(a.tafsirMuyassar).contains(normQ)
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("surah_detail_reader"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header Card for Surah
        item {
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PetrolPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                            border = BorderStroke(1.dp, OrangeAccent),
                            modifier = Modifier.testTag("back_to_surah_index_btn")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "رجوع لفهرس السور",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("فهرس السور")
                        }

                        Surface(
                            color = EmeraldPrimary,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "${surah.revelationType} • ${surah.ayahCount} آية • الجزء ${surah.juzStart}",
                                color = PureWhite,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "سُورَةُ ${surah.nameArabic}",
                        fontFamily = UthmaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 34.sp,
                        color = PureWhite,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (surah.number != 9) {
                        Text(
                            text = "بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ",
                            fontFamily = UthmaniFontFamily,
                            fontSize = 26.sp,
                            color = OrangeLight,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Reciter Pills inside Surah Reader
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SUPPORTED_RECITERS.forEach { reciter ->
                            val selected = reciter.id == audioState.selectedReciter.id
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectReciter(reciter) },
                                label = { Text(reciter.arabicName, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeAccent,
                                    selectedLabelColor = PureWhite,
                                    containerColor = PetrolDark,
                                    labelColor = PureWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onPlayFullSurah,
                            colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("play_full_surah_detail_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("تلاوة السورة كاملة", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = onToggleTafsir,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                            border = BorderStroke(1.dp, EmeraldPrimary),
                            modifier = Modifier.testTag("toggle_tafsir_btn")
                        ) {
                            Icon(
                                imageVector = if (showTafsir) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showTafsir) "إخفاء التفسير" else "إظهار التفسير")
                        }
                    }
                }
            }
        }

        // Filter Ayahs inside Surah
        item {
            OutlinedTextField(
                value = ayahSearchQuery,
                onValueChange = onUpdateAyahQuery,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("inside_surah_search_input"),
                placeholder = { Text("ابحث برقم الآية أو بكلمة داخل سورة ${surah.nameArabic}...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = EmeraldPrimary) },
                trailingIcon = {
                    if (ayahSearchQuery.isNotEmpty()) {
                        IconButton(onClick = { onUpdateAyahQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "مسح")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp)
            )
        }

        if (isLoading) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = EmeraldPrimary)
                        Spacer(modifier = Modifier.height(12.dp))
                        Text("جاري جلب الآيات بالرسم العثماني والتفسير الميسر...")
                    }
                }
            }
        }

        items(filteredAyahs, key = { it.referenceKey }) { ayah ->
            val isThisAyahPlaying = (audioState.isPlaying || audioState.isBuffering) &&
                !audioState.isFullSurahMode &&
                audioState.currentSurahNumber == ayah.surahNumber &&
                audioState.currentAyahNumber == ayah.ayahNumber

            val isBookmarked = bookmarks.any {
                it.idKey == "QURAN_${ayah.surahNumber}_${ayah.ayahNumber}"
            }

            AyahReaderCard(
                ayah = ayah,
                showTafsir = showTafsir,
                isPlaying = isThisAyahPlaying,
                isBookmarked = isBookmarked,
                reciterName = audioState.selectedReciter.arabicName,
                onPlayAyah = { onPlayAyah(ayah) },
                onToggleBookmark = { onToggleBookmark(ayah) },
                onCopyAyah = {
                    copyTextToClipboard(
                        context = context,
                        label = "آية قرآنية",
                        text = "﴿ ${ayah.textUthmani} ﴾ [سورة ${ayah.surahName}: ${ayah.ayahNumber}]\nالتفسير الميسر: ${ayah.tafsirMuyassar}"
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

@Composable
private fun AyahReaderCard(
    ayah: AyahItem,
    showTafsir: Boolean,
    isPlaying: Boolean,
    isBookmarked: Boolean,
    reciterName: String,
    onPlayAyah: () -> Unit,
    onToggleBookmark: () -> Unit,
    onCopyAyah: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("ayah_card_${ayah.surahNumber}_${ayah.ayahNumber}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isPlaying) PetrolPrimary else MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(
            width = if (isPlaying) 2.dp else 1.dp,
            color = if (isPlaying) OrangeAccent else MaterialTheme.colorScheme.outline
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Top bar: Ayah Number Badge + Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        color = if (isPlaying) OrangeAccent else EmeraldPrimary,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "الآية ﴿${ayah.ayahNumber}﴾",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "الجزء ${ayah.juz} • ص ${ayah.page}",
                        fontSize = 12.sp,
                        color = if (isPlaying) Color(0xFFD5E8E2) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onPlayAyah,
                        modifier = Modifier.testTag("play_ayah_btn_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.PauseCircleFilled else Icons.Default.PlayCircleFilled,
                            contentDescription = "استماع للآية بصوت $reciterName",
                            tint = if (isPlaying) OrangeLight else EmeraldPrimary,
                            modifier = Modifier.size(30.dp)
                        )
                    }
                    IconButton(
                        onClick = onToggleBookmark,
                        modifier = Modifier.testTag("bookmark_ayah_btn_${ayah.ayahNumber}")
                    ) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ الآية",
                            tint = if (isBookmarked) OrangeAccent else if (isPlaying) PureWhite else PetrolPrimary
                        )
                    }
                    IconButton(onClick = onCopyAyah) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الآية",
                            tint = if (isPlaying) PureWhite else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Uthmanic Script Ayah Text
            Text(
                text = "${ayah.textUthmani} ﴿${ayah.ayahNumber}﴾",
                fontFamily = UthmaniFontFamily,
                fontSize = 28.sp,
                lineHeight = 48.sp,
                fontWeight = FontWeight.Bold,
                color = if (isPlaying) PureWhite else MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Right,
                modifier = Modifier.fillMaxWidth()
            )

            if (isPlaying) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "🔊 يتلو الآن: $reciterName",
                    color = OrangeLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            // Al-Tafsir Al-Muyassar Box
            AnimatedVisibility(visible = showTafsir && ayah.tafsirMuyassar.isNotBlank()) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = if (isPlaying) PetrolMedium else MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f),
                    border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                                contentDescription = null,
                                tint = if (isPlaying) OrangeLight else EmeraldPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "التفسير الميسر:",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (isPlaying) OrangeLight else EmeraldPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ayah.tafsirMuyassar,
                            fontFamily = IslamicAmiriFontFamily,
                            fontSize = 18.sp,
                            lineHeight = 29.sp,
                            color = if (isPlaying) PureWhite else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}

fun copyTextToClipboard(context: Context, label: String, text: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
    clipboard?.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, "تم النسخ إلى الحافظة", Toast.LENGTH_SHORT).show()
}
