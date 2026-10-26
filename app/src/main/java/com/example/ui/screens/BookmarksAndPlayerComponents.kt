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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BookmarkRemove
import androidx.compose.material.icons.filled.Bookmarks
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.audio.AudioPlaybackState
import com.example.data.local.BookmarkEntity
import com.example.data.model.Reciter
import com.example.data.model.SUPPORTED_RECITERS
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicAmiriFontFamily
import com.example.ui.theme.IslamicBlack
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PetrolDark
import com.example.ui.theme.PetrolPrimary
import com.example.ui.theme.PureWhite
import com.example.ui.theme.UthmaniFontFamily

@Composable
fun BookmarksSectionScreen(
    bookmarks: List<BookmarkEntity>,
    onOpenQuranSurah: (Int, Int) -> Unit,
    onRemoveBookmark: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("bookmarks_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = PetrolPrimary)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmarks,
                        contentDescription = null,
                        tint = OrangeLight,
                        modifier = Modifier.size(32.dp)
                    )
                    Column {
                        Text(
                            text = "محفوظات القرآن الكريم والأحاديث المسندة",
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = PureWhite
                        )
                        Text(
                            text = "الآيات والأحاديث التي قمت بحفظها للرجوع السريع والتدبر (${bookmarks.size} عنصر).",
                            fontSize = 13.sp,
                            color = Color(0xFFD5E8E2)
                        )
                    }
                }
            }
        }

        if (bookmarks.isEmpty()) {
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = EmeraldPrimary,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "قائمة المحفوظات فارغة حالياً",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "اضغط على أيقونة الحفظ بجانب أي آية قرآنية أو حديث نبوي مسند لإضافته هنا.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        } else {
            items(bookmarks, key = { it.idKey }) { item ->
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = BorderStroke(
                        1.5.dp,
                        if (item.itemType == "QURAN") EmeraldPrimary else OrangeAccent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (item.itemType == "QURAN" && item.surahNumber > 0) {
                                onOpenQuranSurah(item.surahNumber, item.ayahNumber)
                            }
                        }
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = if (item.itemType == "QURAN") EmeraldPrimary else OrangeAccent,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = item.title,
                                    color = PureWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }

                            IconButton(onClick = { onRemoveBookmark(item.idKey) }) {
                                Icon(
                                    imageVector = Icons.Default.BookmarkRemove,
                                    contentDescription = "إزالة من المحفوظات",
                                    tint = OrangeAccent
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = item.contentUthmani,
                            fontFamily = UthmaniFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            lineHeight = 40.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            textAlign = TextAlign.Right,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (item.secondaryInfo.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = item.secondaryInfo,
                                fontFamily = IslamicAmiriFontFamily,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
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
fun FloatingQuranAudioPlayerBar(
    audioState: AudioPlaybackState,
    onSelectReciter: (Reciter) -> Unit,
    onTogglePlayPause: () -> Unit,
    onNextAyah: () -> Unit,
    onPreviousAyah: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag("floating_audio_player_bar"),
        shape = RoundedCornerShape(20.dp),
        color = IslamicBlack,
        border = BorderStroke(1.5.dp, OrangeAccent),
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
            // Top Row: Reciter Quick Switch Pills + Status
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "القارئ:",
                    color = OrangeLight,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                SUPPORTED_RECITERS.forEach { reciter ->
                    val active = reciter.id == audioState.selectedReciter.id
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = if (active) EmeraldPrimary else PetrolDark,
                        border = BorderStroke(
                            width = 1.dp,
                            color = if (active) OrangeLight else PetrolPrimary
                        ),
                        modifier = Modifier.clickable { onSelectReciter(reciter) }
                    ) {
                        Text(
                            text = reciter.arabicName.removePrefix("الشيخ "),
                            color = PureWhite,
                            fontSize = 11.sp,
                            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Controls Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = audioState.statusLabel,
                        color = PureWhite,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (audioState.errorMessage != null) {
                        Text(
                            text = audioState.errorMessage,
                            color = OrangeLight,
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    } else {
                        Text(
                            text = "سورة ${audioState.currentSurahName} • ${audioState.selectedReciter.arabicName}",
                            color = Color(0xFFB5C9C3),
                            fontSize = 11.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = onPreviousAyah,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "الآية السابقة",
                            tint = PureWhite
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = OrangeAccent,
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .clickable { onTogglePlayPause() }
                            .testTag("audio_play_pause_btn")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            if (audioState.isBuffering) {
                                CircularProgressIndicator(
                                    color = PureWhite,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(20.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = if (audioState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = if (audioState.isPlaying) "إيقاف مؤقت" else "تشغيل التلاوة",
                                    tint = PureWhite
                                )
                            }
                        }
                    }

                    IconButton(
                        onClick = onNextAyah,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "الآية التالية",
                            tint = PureWhite
                        )
                    }

                    if (audioState.isPlaying || audioState.isBuffering) {
                        IconButton(
                            onClick = onStop,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = "إيقاف",
                                tint = OrangeLight
                            )
                        }
                    }
                }
            }
        }
    }
}
