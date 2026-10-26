package com.example.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.core.content.res.ResourcesCompat
import com.example.R
import com.example.data.model.HadithItem
import com.example.data.model.SihahBookPdf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

/**
 * Generates and renders real multi-page PDF files (.pdf) for the Books of Sihah using Android's
 * native PdfDocument and PdfRenderer APIs so users can read, zoom, and flip through pages inside the app.
 */
object PdfBookEngine {

    suspend fun getOrGenerateBookPdfFile(context: Context, book: SihahBookPdf): File = withContext(Dispatchers.IO) {
        val pdfDir = File(context.cacheDir, "sihah_pdfs").apply { mkdirs() }
        val targetFile = File(pdfDir, "${book.id}_edition_v1.pdf")
        if (targetFile.exists() && targetFile.length() > 1024L) {
            return@withContext targetFile
        }

        val amiriTypeface = try {
            ResourcesCompat.getFont(context, R.font.amiri) ?: Typeface.SERIF
        } catch (_: Exception) {
            Typeface.SERIF
        }

        val uthmaniTypeface = try {
            ResourcesCompat.getFont(context, R.font.scheherazade_new) ?: amiriTypeface
        } catch (_: Exception) {
            amiriTypeface
        }

        val bookHadiths = HadithSihahCatalog.MUSNAD_HADITHS.filter { it.bookId == book.id }
            .ifEmpty { HadithSihahCatalog.MUSNAD_HADITHS }

        val pdfDocument = PdfDocument()
        val pageWidth = 840
        val pageHeight = 1188

        // Page 1: Ornate Islamic Cover & Book Introduction + Chapter Index
        run {
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawIslamicPageFrame(canvas, pageWidth, pageHeight)

            val headerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0A3641")
                typeface = Typeface.create(amiriTypeface, Typeface.BOLD)
                textSize = 44f
                textAlign = Paint.Align.CENTER
            }
            val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F6B4A")
                typeface = Typeface.create(uthmaniTypeface, Typeface.BOLD)
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            val orangePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E67E22")
                typeface = Typeface.create(amiriTypeface, Typeface.BOLD)
                textSize = 22f
                textAlign = Paint.Align.CENTER
            }
            val bodyPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#121A1D")
                typeface = amiriTypeface
                textSize = 22f
                textAlign = Paint.Align.RIGHT
            }

            canvas.drawText("بِسْمِ اللَّهِ الرَّحْمَٰنِ الرَّحِيمِ", pageWidth / 2f, 130f, subPaint)
            canvas.drawText("مكتبة المشكاة الإسلامية • طبعة إلكترونية موثقة (PDF)", pageWidth / 2f, 175f, orangePaint)

            // Central Title Box
            val titleBox = RectF(100f, 210f, pageWidth - 100f, 380f)
            val boxFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0A3641")
                style = Paint.Style.FILL
            }
            canvas.drawRoundRect(titleBox, 24f, 24f, boxFill)

            val whiteTitlePaint = Paint(headerPaint).apply { color = Color.WHITE }
            val goldSubPaint = Paint(orangePaint).apply { color = Color.parseColor("#F39C12") }
            canvas.drawText(book.title, pageWidth / 2f, 285f, whiteTitlePaint)
            canvas.drawText("${book.author} (ت ${book.deathYearHijri}) • ${book.hadithCountLabel}", pageWidth / 2f, 340f, goldSubPaint)

            // Description wrapped
            drawMultilineRtlText(
                canvas = canvas,
                text = book.description,
                rightX = pageWidth - 95f,
                startY = 440f,
                maxWidth = pageWidth - 190f,
                lineHeight = 36f,
                paint = bodyPaint
            )

            // Chapters Index Box
            val indexTitlePaint = Paint(subPaint).apply {
                textAlign = Paint.Align.RIGHT
                textSize = 26f
            }
            canvas.drawText("فهرس أبواب الكتاب المعتمدة في هذه النسخة:", pageWidth - 95f, 560f, indexTitlePaint)

            var yPos = 615f
            book.chaptersSummary.forEach { chapter ->
                val rowRect = RectF(95f, yPos - 28f, pageWidth - 95f, yPos + 14f)
                val rowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    color = Color.parseColor("#F1F7F4")
                }
                canvas.drawRoundRect(rowRect, 10f, 10f, rowPaint)
                canvas.drawText("•  $chapter", pageWidth - 115f, yPos, bodyPaint)
                yPos += 52f
            }

            drawPageFooter(canvas, pageWidth, pageHeight, 1, bookHadiths.size + 1, book.title, amiriTypeface)
            pdfDocument.finishPage(page)
        }

        // Subsequent Pages: Authenticated Hadiths with Full Sanad, Matn, Grade & Explanation
        bookHadiths.forEachIndexed { index, hadith ->
            val pageNumber = index + 2
            val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            drawIslamicPageFrame(canvas, pageWidth, pageHeight)

            val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0A3641")
                typeface = Typeface.create(amiriTypeface, Typeface.BOLD)
                textSize = 28f
                textAlign = Paint.Align.CENTER
            }
            val badgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#E67E22")
                typeface = Typeface.create(amiriTypeface, Typeface.BOLD)
                textSize = 21f
                textAlign = Paint.Align.CENTER
            }
            val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F6B4A")
                typeface = Typeface.create(amiriTypeface, Typeface.BOLD)
                textSize = 24f
                textAlign = Paint.Align.RIGHT
            }
            val sanadPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#2C3E50")
                typeface = amiriTypeface
                textSize = 22f
                textAlign = Paint.Align.RIGHT
            }
            val matnPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0B1114")
                typeface = Typeface.create(uthmaniTypeface, Typeface.BOLD)
                textSize = 26f
                textAlign = Paint.Align.RIGHT
            }

            canvas.drawText("${book.title}  •  ${hadith.chapterName}", pageWidth / 2f, 115f, titlePaint)
            canvas.drawText("الحديث رقم (${hadith.hadithNumber}) • الراوي: ${hadith.narrator} • [${hadith.grade}]", pageWidth / 2f, 155f, badgePaint)

            var currentY = 220f
            canvas.drawText("◈ السند المتصل:", pageWidth - 95f, currentY, labelPaint)
            currentY += 42f
            currentY = drawMultilineRtlText(
                canvas = canvas,
                text = hadith.sanad,
                rightX = pageWidth - 95f,
                startY = currentY,
                maxWidth = pageWidth - 190f,
                lineHeight = 38f,
                paint = sanadPaint
            )

            currentY += 32f
            // Highlight box for Matn
            val matnTop = currentY - 10f
            canvas.drawText("◈ متن الحديث الشريف:", pageWidth - 95f, currentY + 24f, labelPaint)
            currentY += 70f
            currentY = drawMultilineRtlText(
                canvas = canvas,
                text = hadith.matn,
                rightX = pageWidth - 110f,
                startY = currentY,
                maxWidth = pageWidth - 220f,
                lineHeight = 44f,
                paint = matnPaint
            )
            val matnBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#0F6B4A")
                style = Paint.Style.STROKE
                strokeWidth = 2.5f
            }
            canvas.drawRoundRect(RectF(85f, matnTop, pageWidth - 85f, currentY + 20f), 16f, 16f, matnBorderPaint)

            currentY += 65f
            canvas.drawText("◈ الفقه والشرح الميسر:", pageWidth - 95f, currentY, labelPaint)
            currentY += 42f
            drawMultilineRtlText(
                canvas = canvas,
                text = hadith.explanation,
                rightX = pageWidth - 95f,
                startY = currentY,
                maxWidth = pageWidth - 190f,
                lineHeight = 38f,
                paint = sanadPaint
            )

            drawPageFooter(canvas, pageWidth, pageHeight, pageNumber, bookHadiths.size + 1, book.title, amiriTypeface)
            pdfDocument.finishPage(page)
        }

        FileOutputStream(targetFile).use { out ->
            pdfDocument.writeTo(out)
        }
        pdfDocument.close()
        targetFile
    }

    suspend fun renderPdfPagesToBitmaps(pdfFile: File): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        val descriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(descriptor)
        try {
            val pageCount = renderer.pageCount
            for (i in 0 until pageCount) {
                val page = renderer.openPage(i)
                val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(Color.WHITE)
                page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                page.close()
                bitmaps.add(bitmap)
            }
        } finally {
            renderer.close()
            descriptor.close()
        }
        bitmaps
    }

    private fun drawIslamicPageFrame(canvas: Canvas, width: Int, height: Int) {
        val bgPaint = Paint().apply { color = Color.parseColor("#FCFDFD") }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

        val outerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A3641")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        val innerBorder = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E67E22")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        canvas.drawRoundRect(RectF(42f, 42f, width - 42f, height - 42f), 18f, 18f, outerBorder)
        canvas.drawRoundRect(RectF(54f, 54f, width - 54f, height - 54f), 12f, 12f, innerBorder)
    }

    private fun drawPageFooter(
        canvas: Canvas,
        width: Int,
        height: Int,
        pageNum: Int,
        totalPages: Int,
        bookTitle: String,
        typeface: Typeface
    ) {
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0A3641")
            this.typeface = typeface
            textSize = 18f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "$bookTitle  —  صفحة $pageNum من $totalPages  —  منصة المشكاة الإسلامية",
            width / 2f,
            height - 72f,
            footerPaint
        )
    }

    private fun drawMultilineRtlText(
        canvas: Canvas,
        text: String,
        rightX: Float,
        startY: Float,
        maxWidth: Float,
        lineHeight: Float,
        paint: Paint
    ): Float {
        val words = text.split(" ")
        var currentLine = StringBuilder()
        var y = startY
        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine = StringBuilder(testLine)
            } else {
                canvas.drawText(currentLine.toString(), rightX, y, paint)
                y += lineHeight
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), rightX, y, paint)
            y += lineHeight
        }
        return y
    }
}
