package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AddAlert
import androidx.compose.material.icons.filled.Brightness3
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HijriCalendarEngine
import com.example.data.local.CalendarNoteEntity
import com.example.data.model.HijriCalendarDay
import com.example.ui.DateConverterState
import com.example.ui.theme.EmeraldPrimary
import com.example.ui.theme.IslamicAmiriFontFamily
import com.example.ui.theme.OrangeAccent
import com.example.ui.theme.OrangeLight
import com.example.ui.theme.PetrolDark
import com.example.ui.theme.PetrolPrimary
import com.example.ui.theme.PureWhite

@Composable
fun HijriCalendarScreen(
    displayedGregorianYear: Int,
    displayedGregorianMonth: Int,
    monthDays: List<HijriCalendarDay>,
    selectedDay: HijriCalendarDay,
    converterState: DateConverterState,
    calendarNotes: List<CalendarNoteEntity>,
    onChangeMonth: (Int) -> Unit,
    onJumpToToday: () -> Unit,
    onSelectDay: (HijriCalendarDay) -> Unit,
    onSetConverterDirection: (Boolean) -> Unit,
    onUpdateConverterInput: (Int?, Int?, Int?) -> Unit,
    onAddNote: (String, String, String) -> Unit,
    onDeleteNote: (Int) -> Unit
) {
    var noteTitleInput by remember { mutableStateOf("") }
    var noteDetailsInput by remember { mutableStateOf("") }
    var selectedNoteCategory by remember { mutableStateOf("صيام الأيام البيض") }

    val selectedDayNotes = remember(calendarNotes, selectedDay) {
        calendarNotes.filter { it.gregorianDateKey == selectedDay.gregorianDateKey }
    }

    val gMonthName = HijriCalendarEngine.GREGORIAN_MONTH_NAMES[(displayedGregorianMonth - 1).coerceIn(0, 11)]
    val hijriMonthsInGrid = remember(monthDays) {
        monthDays.map { "${it.hijriMonthName} ${it.hijriYear} هـ" }.distinct().joinToString(" / ")
    }
    val firstDayOffset = remember(displayedGregorianYear, displayedGregorianMonth) {
        HijriCalendarEngine.getFirstDayOffsetInWeek(displayedGregorianYear, displayedGregorianMonth)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("hijri_calendar_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Today's Dual Hijri & Gregorian Banner + White Days Reminder
        item {
            val today = remember { HijriCalendarEngine.getTodayHijriDay() }
            Card(
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = PetrolPrimary),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = OrangeAccent,
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "التقويم الهجري ومقابله الميلادي",
                                color = PureWhite,
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }

                        OutlinedButton(
                            onClick = onJumpToToday,
                            border = BorderStroke(1.dp, EmeraldPrimary),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PureWhite),
                            modifier = Modifier.testTag("jump_today_btn")
                        ) {
                            Icon(Icons.Default.Today, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("اليوم", fontSize = 12.sp)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "${today.dayOfWeekArabic}، ${today.formattedHijri}",
                        fontFamily = IslamicAmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = PureWhite
                    )
                    Text(
                        text = "الموافق: ${today.formattedGregorian}",
                        fontSize = 15.sp,
                        color = OrangeLight,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // White Days Indicator Banner
                    Surface(
                        color = PetrolDark,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, OrangeAccent.copy(alpha = 0.6f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Brightness3,
                                contentDescription = null,
                                tint = OrangeLight
                            )
                            Text(
                                text = "تذكير الأيام البيض: ١٣ و١٤ و١٥ من كل شهر هجري مميزة باللون البرتقالي في شبكة التقويم.",
                                color = PureWhite,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }

        // 2. Dual Monthly Calendar Grid (Hijri + Gregorian)
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    // Month Switcher Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = { onChangeMonth(-1) },
                            modifier = Modifier.testTag("prev_month_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "الشهر السابق")
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = hijriMonthsInGrid,
                                fontFamily = IslamicAmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = EmeraldPrimary
                            )
                            Text(
                                text = "$gMonthName $displayedGregorianYear م",
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        IconButton(
                            onClick = { onChangeMonth(1) },
                            modifier = Modifier.testTag("next_month_btn")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "الشهر التالي")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Weekday Names Header
                    Row(modifier = Modifier.fillMaxWidth()) {
                        listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت").forEach { dayLabel ->
                            Text(
                                text = dayLabel,
                                modifier = Modifier.weight(1f),
                                textAlign = TextAlign.Center,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = PetrolPrimary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    // Calendar Cells Grid (Rows of 7)
                    val totalSlots = firstDayOffset + monthDays.size
                    val rowsCount = (totalSlots + 6) / 7
                    for (r in 0 until rowsCount) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            for (c in 0 until 7) {
                                val cellIndex = r * 7 + c
                                val dayIndex = cellIndex - firstDayOffset
                                val dayItem = monthDays.getOrNull(dayIndex)
                                if (dayItem == null) {
                                    Box(modifier = Modifier.weight(1f).height(56.dp))
                                } else {
                                    val isSelected = dayItem.gregorianDateKey == selectedDay.gregorianDateKey
                                    val hasNote = calendarNotes.any { it.gregorianDateKey == dayItem.gregorianDateKey }
                                    DualCalendarDayCell(
                                        day = dayItem,
                                        isSelected = isSelected,
                                        hasNote = hasNote,
                                        modifier = Modifier.weight(1f),
                                        onClick = { onSelectDay(dayItem) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Legend for Colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        LegendItem(color = EmeraldPrimary, label = "اليوم / مناسبة")
                        LegendItem(color = OrangeAccent, label = "الأيام البيض (١٣-١٥)")
                        LegendItem(color = PetrolPrimary, label = "اليوم المحدد")
                    }
                }
            }
        }

        // 3. Selected Day Details & Personal Notes / Reminders
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = BorderStroke(1.5.dp, PetrolPrimary)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "تفاصيل اليوم المحدد: ${selectedDay.dayOfWeekArabic}",
                                fontWeight = FontWeight.Bold,
                                color = PetrolPrimary,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "${selectedDay.formattedHijri}  •  ${selectedDay.formattedGregorian}",
                                fontFamily = IslamicAmiriFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        if (selectedDay.isWhiteDay) {
                            Surface(
                                color = OrangeAccent,
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = "من الأيام البيض",
                                    color = PureWhite,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }

                    selectedDay.occasion?.let { occ ->
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            color = EmeraldPrimary.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, EmeraldPrimary),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "★ مناسبة إسلامية: ${occ.title}",
                                    fontWeight = FontWeight.Bold,
                                    color = EmeraldPrimary,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = occ.description,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "إضافة ملاحظة أو تذكير شخصي لهذا اليوم:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = EmeraldPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("صيام الأيام البيض", "ورد قرآني", "عبادة وذكر", "تذكير عام").forEach { cat ->
                            FilterChip(
                                selected = selectedNoteCategory == cat,
                                onClick = { selectedNoteCategory = cat },
                                label = { Text(cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = OrangeAccent,
                                    selectedLabelColor = PureWhite
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    OutlinedTextField(
                        value = noteTitleInput,
                        onValueChange = { noteTitleInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_note_title_input"),
                        placeholder = { Text("عنوان التذكير (مثال: صيام اليوم الثالث عشر، قراءة سورة الكهف)...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = noteDetailsInput,
                        onValueChange = { noteDetailsInput = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("calendar_note_details_input"),
                        placeholder = { Text("تفاصيل إضافية أو دعاء...") },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            if (noteTitleInput.isNotBlank()) {
                                onAddNote(noteTitleInput, noteDetailsInput, selectedNoteCategory)
                                noteTitleInput = ""
                                noteDetailsInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = EmeraldPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_calendar_note_btn")
                    ) {
                        Icon(Icons.Default.AddAlert, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("حفظ التذكير في هذا اليوم", fontWeight = FontWeight.Bold)
                    }

                    // Existing notes for this day or upcoming notes
                    if (selectedDayNotes.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "ملاحظاتك المسجلة في هذا اليوم (${selectedDayNotes.size}):",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            color = PetrolPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        selectedDayNotes.forEach { note ->
                            CalendarNoteRow(note = note, onDelete = { onDeleteNote(note.id) })
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }

        // 4. Instant Bidirectional Date Converter Card (محول التاريخ الفوري هجري ⇄ ميلادي)
        item {
            InstantDateConverterCard(
                state = converterState,
                onSetDirection = onSetConverterDirection,
                onUpdateInput = onUpdateConverterInput
            )
        }

        // 5. Major Islamic Occasions & Holidays Directory
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.EventAvailable, contentDescription = null, tint = OrangeAccent)
                        Text(
                            text = "دليل المناسبات والأعياد الإسلامية على مدار العام الهجري",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    HijriCalendarEngine.ISLAMIC_OCCASIONS.forEach { occ ->
                        val monthName = HijriCalendarEngine.HIJRI_MONTH_NAMES[(occ.hijriMonth - 1).coerceIn(0, 11)]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = occ.title,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = if (occ.isMajorHoliday) EmeraldPrimary else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = occ.description,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Surface(
                                color = if (occ.isMajorHoliday) OrangeAccent else PetrolPrimary,
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = "${occ.hijriDay} $monthName",
                                    color = PureWhite,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
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
private fun DualCalendarDayCell(
    day: HijriCalendarDay,
    isSelected: Boolean,
    hasNote: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val bgColor = when {
        isSelected -> PetrolPrimary
        day.isToday -> EmeraldPrimary
        day.isWhiteDay -> OrangeAccent.copy(alpha = 0.18f)
        day.occasion != null -> EmeraldPrimary.copy(alpha = 0.12f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
    }

    val primaryTextColor = when {
        isSelected || day.isToday -> PureWhite
        day.isWhiteDay -> OrangeAccent
        else -> MaterialTheme.colorScheme.onSurface
    }

    val secondaryTextColor = when {
        isSelected || day.isToday -> OrangeLight
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .border(
                width = if (isSelected || day.isWhiteDay || day.occasion != null) 1.5.dp else 0.5.dp,
                color = when {
                    isSelected -> OrangeAccent
                    day.isWhiteDay -> OrangeAccent
                    day.occasion != null -> EmeraldPrimary
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(10.dp)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Hijri Day (Prominent)
            Text(
                text = "${day.hijriDay}",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = primaryTextColor
            )
            // Gregorian Day (Counterpart)
            Text(
                text = "${day.gregorianDay} م",
                fontSize = 10.sp,
                color = secondaryTextColor
            )
            if (hasNote || day.occasion != null) {
                Box(
                    modifier = Modifier
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (hasNote) OrangeAccent else EmeraldPrimary)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(text = label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CalendarNoteRow(
    note: CalendarNoteEntity,
    onDelete: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f),
        border = BorderStroke(1.dp, EmeraldPrimary.copy(alpha = 0.4f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = OrangeAccent,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = note.category,
                            color = PureWhite,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Text(
                        text = note.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                if (note.noteDetails.isNotBlank()) {
                    Text(
                        text = note.noteDetails,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "حذف الملاحظة",
                    tint = OrangeAccent
                )
            }
        }
    }
}

@Composable
private fun InstantDateConverterCard(
    state: DateConverterState,
    onSetDirection: (Boolean) -> Unit,
    onUpdateInput: (Int?, Int?, Int?) -> Unit
) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PetrolPrimary),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("instant_date_converter_card")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = OrangeLight)
                Text(
                    text = "محول التاريخ الفوري (هجري ⇄ ميلادي)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = PureWhite
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Mode Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onSetDirection(true) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (state.isHijriToGregorian) OrangeAccent else PetrolDark
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("من هجري إلى ميلادي", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Button(
                    onClick = { onSetDirection(false) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (!state.isHijriToGregorian) OrangeAccent else PetrolDark
                    ),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("من ميلادي إلى هجري", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Steppers for Day, Month, Year
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                NumberStepperBox(
                    label = "اليوم",
                    valueText = "${state.inputDay}",
                    onDecrement = { onUpdateInput(state.inputDay - 1, null, null) },
                    onIncrement = { onUpdateInput(state.inputDay + 1, null, null) },
                    modifier = Modifier.weight(1f)
                )

                val monthLabel = if (state.isHijriToGregorian) {
                    HijriCalendarEngine.HIJRI_MONTH_NAMES[(state.inputMonth - 1).coerceIn(0, 11)]
                } else {
                    HijriCalendarEngine.GREGORIAN_MONTH_NAMES[(state.inputMonth - 1).coerceIn(0, 11)]
                }

                NumberStepperBox(
                    label = "الشهر",
                    valueText = monthLabel,
                    onDecrement = { onUpdateInput(null, state.inputMonth - 1, null) },
                    onIncrement = { onUpdateInput(null, state.inputMonth + 1, null) },
                    modifier = Modifier.weight(1.3f)
                )

                NumberStepperBox(
                    label = "السنة",
                    valueText = "${state.inputYear}",
                    onDecrement = { onUpdateInput(null, null, state.inputYear - 1) },
                    onIncrement = { onUpdateInput(null, null, state.inputYear + 1) },
                    modifier = Modifier.weight(1.1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Result Display Box
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = EmeraldPrimary,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "نتيجة التحويل الفوري:",
                        fontSize = 12.sp,
                        color = OrangeLight,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = state.resultPrimary,
                        fontFamily = IslamicAmiriFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 24.sp,
                        color = PureWhite,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = state.resultSecondary,
                        fontSize = 13.sp,
                        color = PureWhite.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun NumberStepperBox(
    label: String,
    valueText: String,
    onDecrement: () -> Unit,
    onIncrement: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = PetrolDark,
        border = BorderStroke(1.dp, EmeraldPrimary)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 11.sp, color = OrangeLight)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "−",
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable { onDecrement() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
                Text(
                    text = valueText,
                    color = PureWhite,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "+",
                    color = OrangeLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier
                        .clickable { onIncrement() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}
