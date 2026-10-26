package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HadithSihahCatalog
import com.example.data.local.BookmarkEntity
import com.example.data.model.HadithItem
import com.example.data.model.SihahBookPdf
import com.example.ui.PdfReaderState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicAmiriFontFamily
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PetrolDark
import com.example.ui.theme.PetrolPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.UthmaniFontFamily

@Composable
fun HadithAndPdfScreen(
    subTab: Int,
    selectedChapter: String,
    selectedBookFilter: String,
    pdfReaderState: PdfReaderState,
    bookmarks: List<BookmarkEntity>,
    onSelectSubTab: (Int) -> Unit,
    onSelectChapter: (String) -> Unit,
    onSelectBookFilter: (String) -> Unit,
    onOpenSihahPdf: (SihahBookPdf, Int) -> Unit,
    onClosePdfReader: () -> Unit,
    onGoToPdfPage: (Int) -> Unit,
    onAdjustPdfZoom: (Float) -> Unit,
    onToggleHadithBookmark: (HadithItem) -> Unit
) {
    // If a Sihah PDF Book is open in the built-in reader, display the PDF Reader View
    if (pdfReaderState.activeBook != null) {
        BackHandler {
            onClosePdfReader()
        }
        BuiltInSihahPdfReaderView(
            state = pdfReaderState,
            onClose = onClosePdfReader,
            onGoToPage = onGoToPdfPage,
            onAdjustZoom = onAdjustPdfZoom
        )
        return
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Sub-Tab Selector: 0 = الأحاديث المسندة والأبواب, 1 = كتب الصحاح الستة (PDF)
        TabRow(
            selectedTabIndex = subTab,
            containerColor = PetrolPrimary,
            contentColor = PureWhite
        ) {
            Tab(
                selected = subTab == 0,
                onClick = { onSelectSubTab(0) },
                modifier = Modifier.testTag("tab_musnad_hadiths"),
                text = {
                    Text(
                        text = "الأحاديث المسندة والأبواب",
                        fontWeight = if (subTab == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 0) OrangeLight else PureWhite
                    )
                }
            )
            Tab(
                selected = subTab == 1,
                onClick = { onSelectSubTab(1) },
                modifier = Modifier.testTag("tab_sihah_pdfs"),
                text = {
                    Text(
                        text = "كتب الصحاح الستة (PDF)",
                        fontWeight = if (subTab == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (subTab == 1) OrangeLight else PureWhite
                    )
                }
            )
        }

        if (subTab == 0) {
            MusnadHadithBrowserList(
                selectedChapter = selectedChapter,
                selectedBookFilter = selectedBookFilter,
                bookmarks = bookmarks,
                onSelectChapter = onSelectChapter,
                onSelectBookFilter = onSelectBookFilter,
                onToggleHadithBookmark = onToggleHadithBookmark
            )
        } else {
            SihahPdfBooksLibraryView(
                onOpenPdfInApp = onOpenSihahPdf
            )
        }
    }
}

@Composable
private fun MusnadHadithBrowserList(
    selectedChapter: String,
    selectedBookFilter: String,
    bookmarks: List<BookmarkEntity>,
    onSelectChapter: (String) -> Unit,
    onSelectBookFilter: (String) -> Unit,
    onToggleHadithBookmark: (HadithItem) -> Unit
) {
    val context = LocalContext.current
    val bookTitles = remember {
        listOf("الكل") + HadithSihahCatalog.SIHAH_BOOKS_PDF.map { it.title }
    }

    val filteredHadiths = remember(selectedChapter, selectedBookFilter) {
        HadithSihahCatalog.MUSNAD_HADITHS.filter { h ->
            val matchChapter = selectedChapter == "الكل" || h.chapterName == selectedChapter
            val matchBook = selectedBookFilter == "الكل" || h.bookName == selectedBookFilter
            matchChapter && matchBook
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("musnad_hadith_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Chapter Filter Row
        item {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "تصفح حسب الأبواب الفقهية والحديثية:",
                        style = MaterialTheme.typography.labelLarge,
                        color = EmeraldPrimary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        HadithSihahCatalog.CHAPTER_CATEGORIES.forEach { chapter ->
                            val selected = selectedChapter == chapter
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectChapter(chapter) },
                                label = { Text(chapter) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = PetrolPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تصفية حسب كتاب الصحاح:",
                        style = MaterialTheme.typography.labelLarge,
                        color = OrangeAccent
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        bookTitles.forEach { bookName ->
                            val selected = selectedBookFilter == bookName
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectBookFilter(bookName) },
                                label = { Text(bookName) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = EmeraldPrimary,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }
                }
            }
        }

        items(filteredHadiths, key = { it.id }) { hadith ->
            val isBookmarked = bookmarks.any { it.idKey == "HADITH_${hadith.id}" }
            MusnadHadithCard(
                hadith = hadith,
                isBookmarked = isBookmarked,
                onToggleBookmark = { onToggleHadithBookmark(hadith) },
                onCopy = {
                    copyTextToClipboard(
                        context = context,
                        label = "حديث نبوي مسند",
                        text = "${hadith.bookName} - ${hadith.chapterName} (حديث ${hadith.hadithNumber})\n" +
                            "السند: ${hadith.sanad}\nالمتن: ${hadith.matn}\nدرجة الحديث: ${hadith.grade}"
                    )
                }
            )
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
fun MusnadHadithCard(
    hadith: HadithItem,
    isBookmarked: Boolean,
    onToggleBookmark: () -> Unit,
    onCopy: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("hadith_card_${hadith.id}"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.5.dp, PetrolPrimary.copy(alpha = 0.35f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Book Name + Hadith Number + Actions
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
                        color = PetrolPrimary,
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "${hadith.bookName} • رقم (${hadith.hadithNumber})",
                            color = PureWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    Surface(
                        color = OrangeAccent.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = hadith.chapterName,
                            color = OrangeAccent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Row {
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "حفظ الحديث",
                            tint = if (isBookmarked) OrangeAccent else PetrolPrimary
                        )
                    }
                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "نسخ الحديث",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Narrator Badge
            Text(
                text = "الصحابي الراوي: ${hadith.narrator}",
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = EmeraldPrimary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Connected Chain of Narration (السند المتصل)
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "◈ السند المتصل:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PetrolPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = hadith.sanad,
                        fontFamily = IslamicAmiriFontFamily,
                        fontSize = 17.sp,
                        lineHeight = 27.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hadith Matn in Uthmanic Font
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = PetrolPrimary,
                border = BorderStroke(1.5.dp, OrangeAccent),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = hadith.matn,
                        fontFamily = UthmaniFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 23.sp,
                        lineHeight = 38.sp,
                        color = PureWhite,
                        textAlign = TextAlign.Right
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Grading Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = EmeraldPrimary,
                    modifier = Modifier.size(18.dp)
                )
                Text(
                    text = "درجة الحديث والتخريج: ${hadith.grade}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = EmeraldPrimary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Explanation
            Text(
                text = "الفقه والشرح: ${hadith.explanation}",
                fontFamily = IslamicAmiriFontFamily,
                fontSize = 17.sp,
                lineHeight = 26.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun SihahPdfBooksLibraryView(
    onOpenPdfInApp: (SihahBookPdf, Int) -> Unit
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("sihah_pdf_books_list"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PetrolPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PictureAsPdf,
                            contentDescription = null,
                            tint = OrangeLight,
                            modifier = Modifier.size(28.dp)
                        )
                        Column {
                            Text(
                                text = "مكتبة كتب الصحاح الستة (PDF أونلاين وقارئ مدمج)",
                                style = MaterialTheme.typography.titleMedium,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "تصفح صفحات الكتاب مباشرة بالخط العثماني والإسلامي داخل التطبيق أو افتح نسخة الـ PDF الكاملة عبر الإنترنت.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFFD5E8E2)
                            )
                        }
                    }
                }
            }
        }

        items(HadithSihahCatalog.SIHAH_BOOKS_PDF, key = { it.id }) { book ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("sihah_book_card_${book.id}"),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, Color(book.accentHex)),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = book.title,
                                fontFamily = IslamicAmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 26.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${book.author} (ت ${book.deathYearHijri})",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp,
                                color = EmeraldPrimary
                            )
                        }

                        Surface(
                            color = OrangeAccent,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = book.hadithCountLabel,
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = book.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "أبرز الأبواب المتاحة في القارئ المدمج:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = PetrolPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        book.chaptersSummary.forEachIndexed { idx, chapterName ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                modifier = Modifier.clickable {
                                    // Open built-in PDF reader directly
                                    onOpenPdfInApp(book, if (idx == 0) 0 else 1)
                                }
                            ) {
                                Text(
                                    text = chapterName,
                                    fontSize = 12.sp,
                                    color = PetrolDark,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { onOpenPdfInApp(book, 0) },
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("open_pdf_reader_btn_${book.id}")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.MenuBook, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("قارئ PDF المدمج", fontWeight = FontWeight.Bold)
                        }

                        OutlinedButton(
                            onClick = {
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(book.onlinePdfUrl))
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    onOpenPdfInApp(book, 0)
                                }
                            },
                            border = BorderStroke(1.5.dp, OrangeAccent),
                            modifier = Modifier.testTag("download_online_pdf_btn_${book.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Download,
                                contentDescription = "تحميل PDF أونلاين",
                                tint = OrangeAccent
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("PDF أونلاين", color = OrangeAccent, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
private fun BuiltInSihahPdfReaderView(
    state: PdfReaderState,
    onClose: () -> Unit,
    onGoToPage: (Int) -> Unit,
    onAdjustZoom: (Float) -> Unit
) {
    val context = LocalContext.current
    val book = state.activeBook ?: return
    val totalPages = state.pages.size
    val currentBitmap = state.pages.getOrNull(state.currentPageIndex)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(PetrolDark)
            .testTag("built_in_pdf_reader_view")
    ) {
        // Top Reader Control Bar
        Surface(
            color = PetrolPrimary,
            shadowElevation = 4.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onClose,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                        border = BorderStroke(1.dp, OrangeAccent),
                        modifier = Modifier.testTag("close_pdf_reader_btn")
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("العودة للمكتبة")
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = book.title,
                            fontFamily = IslamicAmiriFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = PureWhite
                        )
                        if (totalPages > 0) {
                            Text(
                                text = "صفحة ${state.currentPageIndex + 1} من $totalPages • تكبير ${(state.zoomScale * 100).toInt()}%",
                                fontSize = 12.sp,
                                color = OrangeLight
                            )
                        }
                    }

                    Row {
                        IconButton(onClick = { onAdjustZoom(0.25f) }) {
                            Icon(Icons.Default.ZoomIn, contentDescription = "تكبير", tint = PureWhite)
                        }
                        IconButton(onClick = { onAdjustZoom(-0.25f) }) {
                            Icon(Icons.Default.ZoomOut, contentDescription = "تصغير", tint = PureWhite)
                        }
                        IconButton(
                            onClick = {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(book.onlinePdfUrl)))
                                } catch (_: Exception) {
                                }
                            }
                        ) {
                            Icon(
                                Icons.Default.OpenInBrowser,
                                contentDescription = "فتح الرابط المباشر",
                                tint = OrangeLight
                            )
                        }
                    }
                }
            }
        }

        // PDF Page Viewport
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            if (state.isLoadingPdf) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = OrangeAccent)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = state.statusNotice ?: "جاري تحميل صفحات الـ PDF...",
                        color = PureWhite
                    )
                }
            } else if (currentBitmap != null) {
                Card(
                    shape = RoundedCornerShape(14.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .horizontalScroll(rememberScrollState())
                ) {
                    Image(
                        bitmap = currentBitmap.asImageBitmap(),
                        contentDescription = "صفحة ${state.currentPageIndex + 1} من ${book.title}",
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .width((360 * state.zoomScale).dp)
                            .padding(4.dp)
                    )
                }
            } else {
                Text(
                    text = state.statusNotice ?: "لا توجد صفحات متاحة",
                    color = PureWhite
                )
            }
        }

        // Bottom Page Navigation Bar
        if (totalPages > 0) {
            Surface(color = PetrolPrimary) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = { onGoToPage(state.currentPageIndex - 1) },
                        enabled = state.currentPageIndex > 0,
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("الصفحة السابقة")
                    }

                    Text(
                        text = "${state.currentPageIndex + 1} / $totalPages",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )

                    Button(
                        onClick = { onGoToPage(state.currentPageIndex + 1) },
                        enabled = state.currentPageIndex + 1 < totalPages,
                        colors = ButtonDefaults.buttonColors(containerColor = OrangeAccent)
                    ) {
                        Text("الصفحة التالية")
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                    }
                }
            }
        }
    }
}
