package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BookmarkEntity
import com.example.data.model.AyahItem
import com.example.data.model.HadithItem
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicAmiriFontFamily
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PetrolPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.UthmaniFontFamily

@Composable
fun SearchSectionScreen(
    searchQuery: String,
    categoryFilter: String,
    quranResults: List<AyahItem>,
    hadithResults: List<HadithItem>,
    isSearching: Boolean,
    bookmarks: List<BookmarkEntity>,
    onUpdateQuery: (String) -> Unit,
    onUpdateCategory: (String) -> Unit,
    onPlayAyah: (AyahItem) -> Unit,
    onOpenSurahAtAyah: (Int, Int) -> Unit,
    onToggleAyahBookmark: (AyahItem) -> Unit,
    onToggleHadithBookmark: (HadithItem) -> Unit
) {
    val context = LocalContext.current
    val quickKeywords = listOf(
        "اللَّهُ لَا إِلَٰهَ إِلَّا هُوَ",
        "2:255",
        "الرحمن",
        "الصبر",
        "الصلاة",
        "القرآن",
        "إنما الأعمال بالنيات",
        "الأيام البيض"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("organized_search_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Search Header & Input Box
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PetrolPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "محرك البحث المنظم (القرآن الكريم والأحاديث المسندة)",
                        style = MaterialTheme.typography.titleMedium,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "ابحث بكلمة معينة، جزء من آية، رقم السورة والآية (مثل 2:255)، أو لفظ في متن وسند الحديث.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFFD5E8E2)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = onUpdateQuery,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("global_search_input"),
                        placeholder = {
                            Text(
                                "اكتب كلمة أو رقم آية (مثال: الكرسي، الصبر، 2:255)...",
                                color = Color(0xFFB5C9C3)
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.Search, contentDescription = "بحث", tint = OrangeLight)
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { onUpdateQuery("") }) {
                                    Icon(Icons.Default.Clear, contentDescription = "مسح البحث", tint = PureWhite)
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(14.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = PureWhite,
                            unfocusedTextColor = PureWhite,
                            focusedBorderColor = OrangeAccent,
                            unfocusedBorderColor = EmeraldPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Suggested Quick Search Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickKeywords.forEach { kw ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = EmeraldPrimary,
                                modifier = Modifier.clickable { onUpdateQuery(kw) }
                            ) {
                                Text(
                                    text = kw,
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 2. Category Filter Chips + Counts
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val categories = listOf(
                    "الكل" to "الكل (${quranResults.size + hadithResults.size})",
                    "القرآن الكريم" to "آيات القرآن (${quranResults.size})",
                    "الأحاديث المسندة" to "الأحاديث (${hadithResults.size})"
                )
                categories.forEach { (key, label) ->
                    val selected = categoryFilter == key
                    FilterChip(
                        selected = selected,
                        onClick = { onUpdateCategory(key) },
                        label = {
                            Text(
                                text = label,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = OrangeAccent,
                            selectedLabelColor = PureWhite
                        )
                    )
                }
            }
        }

        if (isSearching) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(
                            color = EmeraldPrimary,
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.5.dp
                        )
                        Text("جاري البحث في آيات المصحف الشريف والأحاديث المسندة...")
                    }
                }
            }
        }

        // 3. Quran Search Results Section
        if (categoryFilter == "الكل" || categoryFilter == "القرآن الكريم") {
            item {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = EmeraldPrimary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "نتائج القرآن الكريم (${quranResults.size} آية مطابقة)",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            if (quranResults.isEmpty() && !isSearching) {
                item {
                    Text(
                        text = "لم يتم العثور على آيات مطابقة لهذا اللفظ. جرب البحث بكلمة مثل: الرحمن، الصبر، الصلاة.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            } else {
                items(quranResults, key = { "SEARCH_Q_${it.referenceKey}" }) { ayah ->
                    val isBookmarked = bookmarks.any {
                        it.idKey == "QURAN_${ayah.surahNumber}_${ayah.ayahNumber}"
                    }
                    Card(
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.45f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    color = PetrolPrimary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "سورة ${ayah.surahName} • الآية (${ayah.ayahNumber})",
                                        color = PureWhite,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    IconButton(onClick = { onPlayAyah(ayah) }) {
                                        Icon(
                                            Icons.Default.PlayCircleFilled,
                                            contentDescription = "استماع للآية",
                                            tint = EmeraldPrimary,
                                            modifier = Modifier.size(28.dp)
                                        )
                                    }
                                    IconButton(onClick = { onToggleAyahBookmark(ayah) }) {
                                        Icon(
                                            imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                            contentDescription = "حفظ",
                                            tint = if (isBookmarked) OrangeAccent else PetrolPrimary
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "${ayah.textUthmani} ﴿${ayah.ayahNumber}﴾",
                                fontFamily = UthmaniFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 25.sp,
                                lineHeight = 42.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                textAlign = TextAlign.Right,
                                modifier = Modifier.fillMaxWidth()
                            )

                            if (ayah.tafsirMuyassar.isNotBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "التفسير: ${ayah.tafsirMuyassar}",
                                    fontFamily = IslamicAmiriFontFamily,
                                    fontSize = 16.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedButton(
                                onClick = { onOpenSurahAtAyah(ayah.surahNumber, ayah.ayahNumber) },
                                border = BorderStroke(1.dp, OrangeAccent),
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = OrangeAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("فتح في المصحف مع التفسير", color = OrangeAccent, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // 4. Hadith Search Results Section
        if (categoryFilter == "الكل" || categoryFilter == "الأحاديث المسندة") {
            item {
                Spacer(modifier = Modifier.height(4.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = OrangeAccent,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "نتائج الأحاديث الشريفة المسندة (${hadithResults.size} حديث مطابق)",
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            items(hadithResults, key = { "SEARCH_H_${it.id}" }) { hadith ->
                val isBookmarked = bookmarks.any { it.idKey == "HADITH_${hadith.id}" }
                MusnadHadithCard(
                    hadith = hadith,
                    isBookmarked = isBookmarked,
                    onToggleBookmark = { onToggleHadithBookmark(hadith) },
                    onCopy = {
                        copyTextToClipboard(
                            context = context,
                            label = "حديث شريف",
                            text = "${hadith.bookName}: ${hadith.matn}"
                        )
                    }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}
