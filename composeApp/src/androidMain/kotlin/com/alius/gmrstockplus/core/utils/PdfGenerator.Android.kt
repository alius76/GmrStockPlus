package com.alius.gmrstockplus.core.utils

import android.content.Intent
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.util.Log
import androidx.core.content.FileProvider
import com.alius.gmrstockplus.core.AppContextProvider
import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.MaterialGroup
import com.alius.gmrstockplus.domain.model.Ratio
import com.alius.gmrstockplus.domain.model.Venta
import java.io.File
import java.io.FileOutputStream
import kotlinx.datetime.*

actual object PdfGenerator {

    private const val TAG = "PdfGenerator"

    // Colores del theme para Canvas nativo
    private val PrimaryPdfColor = Color.rgb(2, 144, 131)     // 0xFF029083
    private val ReservedPdfColor = Color.rgb(183, 28, 28)    // 0xFFB71C1C
    private val DarkGrayPdfColor = Color.rgb(85, 85, 85)     // 0xFF555555
    private val TextPrimaryPdf = Color.rgb(51, 51, 51)       // 0xFF333333
    private val GrayPdfColor = Color.rgb(204, 204, 204)      // 0xFFCCCCCC
    private val LightGrayBg = Color.rgb(245, 245, 245)       // Fondo para KPIs
    private val WarningPdfColor = Color.rgb(240, 154, 0)      // 0xFFF09A00 (Ámbar)

    // Helper corregido para que ambas fechas del rango muestren el año correctamente
    private fun ensureYearInRange(range: String): String {
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year

        // Dividimos por espacios para procesar cada palabra (ej: "1/11", "al", "28/12/2025")
        return range.split(" ").joinToString(" ") { word ->
            // Regex flexible: \d{1,2} acepta 1 o 2 dígitos.
            // Verificamos si es una fecha tipo "1/11" o "01/11" pero que NO tenga ya el año
            if (word.matches(Regex("""\d{1,2}/\d{1,2}"""))) {
                "$word/$currentYear"
            } else {
                word
            }
        }
    }

    actual fun generateProductionReportPdf(
        ratios: List<Ratio>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String,
        loteNombresMap: Map<String, String>
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 45f
        var y = 60f

        // --- CABECERA ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = DarkGrayPdfColor
        canvas.drawText("INFORME DE PRODUCCIÓN", margin, y, paint)

        paint.color = PrimaryPdfColor
        val logoText = "GMR Stock"
        canvas.drawText(logoText, pageWidth - margin - paint.measureText(logoText), y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY

        canvas.drawText("Rango: ${ensureYearInRange(dateRange)}", margin, y, paint)

        y += 45f

        // --- CUADRO RESUMEN KPIs ---
        val summaryRect = RectF(margin, y, pageWidth - margin, y + 75f)
        paint.color = LightGrayBg
        canvas.drawRoundRect(summaryRect, 12f, 12f, paint)

        paint.color = TextPrimaryPdf
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL KILOS", margin + 25f, y + 25f, paint)
        canvas.drawText("LOTES", margin + 210f, y + 25f, paint)
        canvas.drawText("MEDIA DIARIA", margin + 410f, y + 25f, paint)

        paint.textSize = 17f
        paint.color = PrimaryPdfColor
        canvas.drawText("${formatWeight(totalKilos)} kg", margin + 25f, y + 55f, paint)
        canvas.drawText("${ratios.size}", margin + 210f, y + 55f, paint)
        canvas.drawText("${formatWeight(promedio)} kg", margin + 410f, y + 55f, paint)

        y += 110f

        // ==========================================
        // 📊 SECCIÓN: DESGLOSE MENSUAL (Barras Visuales)
        // ==========================================
        val mesesEspanol = mapOf(
            1 to "Enero", 2 to "Febrero", 3 to "Marzo", 4 to "Abril", 5 to "Mayo", 6 to "Junio",
            7 to "Julio", 8 to "Agosto", 9 to "Septiembre", 10 to "Octubre", 11 to "Noviembre", 12 to "Diciembre"
        )

        // Agrupamos los ratios para las barras de progreso mensuales
        val datosMensuales = ratios.groupBy {
            val date = Instant.fromEpochMilliseconds(it.ratioDate).toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${date.monthNumber}-${date.year}"
        }.map { (key, lista) ->
            val partes = key.split("-")
            val mesNum = partes[0].toInt()
            val anio = partes[1]
            object {
                val label = "${mesesEspanol[mesNum]} $anio"
                val kilos = lista.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 }
                val porcentaje = if (totalKilos > 0) (kilos / totalKilos).toFloat() else 0f
                val mesAnioSort = key // Para ordenar si fuera necesario
            }
        }.sortedBy { it.label }

        // MODIFICACIÓN: Se quita la restricción de size > 1 para que aparezca siempre
        if (datosMensuales.isNotEmpty()) {
            paint.color = DarkGrayPdfColor
            paint.textSize = 12f
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("DESGLOSE MENSUAL", margin, y, paint)
            y += 20f

            datosMensuales.forEach { mes ->
                // Control de salto de página dentro del desglose mensual si fuera necesario
                if (y > pageHeight - 80f) {
                    pdfDocument.finishPage(page)
                    pageNumber++
                    pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    y = 60f
                }

                paint.textSize = 10f
                paint.color = TextPrimaryPdf
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                canvas.drawText(mes.label, margin, y, paint)

                val pesoMesText = "${formatWeight(mes.kilos)} kg (${(mes.porcentaje * 100).toInt()}%)"
                canvas.drawText(pesoMesText, pageWidth - margin - paint.measureText(pesoMesText), y, paint)

                y += 8f
                val barWidth = pageWidth - (margin * 2)
                val barRectFondo = RectF(margin, y, margin + barWidth, y + 6f)
                paint.color = Color.rgb(230, 230, 230)
                canvas.drawRoundRect(barRectFondo, 3f, 3f, paint)

                val barRectActiva = RectF(margin, y, margin + (barWidth * mes.porcentaje), y + 6f)
                paint.color = PrimaryPdfColor
                canvas.drawRoundRect(barRectActiva, 3f, 3f, paint)

                y += 25f
            }
            y += 10f
        }

        // --- TABLA DETALLADA (REGISTRO POR REGISTRO) ---
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DETALLE DE PRODUCCIÓN", margin, y, paint)
        y += 20f

        // Cabeceras de tabla
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("FECHA", margin + 10f, y, paint)
        canvas.drawText("NÚMERO DE LOTE", margin + 140f, y, paint) // Más espacio para el nombre del lote
        val labelPeso = "PESO TOTAL"
        canvas.drawText(labelPeso, pageWidth - margin - paint.measureText(labelPeso) - 10f, y, paint)

        y += 8f
        paint.color = GrayPdfColor
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 25f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        // Mostramos cada ratio individual ordenado por fecha
        ratios.sortedByDescending { it.ratioDate }.forEachIndexed { index, ratio ->
            if (y > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f

                // Repetir cabeceras en nueva página para claridad
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("FECHA", margin + 10f, y, paint)
                canvas.drawText("NÚMERO DE LOTE", margin + 140f, y, paint)
                canvas.drawText(labelPeso, pageWidth - margin - paint.measureText(labelPeso) - 10f, y, paint)
                y += 25f
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            }

            // Fila cebreada (como en la pantalla)
            if (index % 2 != 0) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(RectF(margin, y - 16f, pageWidth - margin, y + 8f), paint)
            }

            paint.color = TextPrimaryPdf

            // Formatear Fecha
            val instant = Instant.fromEpochMilliseconds(ratio.ratioDate)
            val f = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val dateStr = "${f.dayOfMonth.toString().padStart(2,'0')}/${f.monthNumber.toString().padStart(2,'0')}/${f.year}"
            canvas.drawText(dateStr, margin + 10f, y, paint)

            // Nombre del Lote (Mapa resuelto)
            val nombreLote = loteNombresMap[ratio.ratioLoteId] ?: "Desconocido"
            canvas.drawText("Lote: $nombreLote", margin + 140f, y, paint)

            // Peso
            val kilosIndividual = ratio.ratioTotalWeight.toDoubleOrNull() ?: 0.0
            val pesoText = "${formatWeight(kilosIndividual)} kg"
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(pesoText, pageWidth - margin - paint.measureText(pesoText) - 10f, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            y += 28f
        }

        pdfDocument.finishPage(page)
        saveAndSharePdf(pdfDocument, "Informe_Produccion_${Clock.System.now().toEpochMilliseconds()}")
    }

    // ============================================================
    // 2. GENERAR PLANNING COMANDAS (ACTUALIZADO: MULTI-MATERIAL)
    // ============================================================
    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595f
        val pageHeight = 842f
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 40f
        var y = 60f

        // Aumentamos ligeramente la altura de la celda para que quepan varios materiales
        val columnWidth = (pageWidth - (margin * 2) - 10f) / 2f
        val cellHeight = 140f

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        fun checkNewPage(needed: Float) {
            if (y + needed > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth.toInt(), pageHeight.toInt(), pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }
        }

        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toSortedMap(compareBy { it })

        // --- CABECERA ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = DarkGrayPdfColor
        canvas.drawText(title.uppercase(), margin, y, paint)

        paint.color = PrimaryPdfColor
        val logoText = "GMR Stock"
        val logoWidth = paint.measureText(logoText)
        canvas.drawText(logoText, pageWidth - margin - logoWidth, y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY
        canvas.drawText("Rango: $dateRange", margin, y, paint)

        y += 45f

        groupedComandas.forEach { (date, list) ->
            checkNewPage(160f)

            val dateText = if (date == null) "SIN FECHA" else "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 14f
            paint.color = Color.BLACK
            canvas.drawText(dateText, margin, y, paint)

            y += 15f

            list.forEachIndexed { index, comanda ->
                val isRightColumn = index % 2 != 0
                val xOffset = if (isRightColumn) margin + columnWidth + 10f else margin

                if (!isRightColumn && index > 0) {
                    checkNewPage(cellHeight + 10f)
                }

                // Dibujo de Celda
                paint.style = Paint.Style.STROKE
                paint.color = Color.LTGRAY
                paint.strokeWidth = 0.5f
                val rect = RectF(xOffset, y, xOffset + columnWidth, y + cellHeight)
                canvas.drawRoundRect(rect, 8f, 8f, paint)

                paint.style = Paint.Style.FILL
                var innerY = y + 20f

                // Cliente
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                paint.textSize = 10.5f
                paint.color = DarkGrayPdfColor
                canvas.drawText(comanda.bookedClientComanda?.cliNombre?.take(22) ?: "Sin Cliente", xOffset + 10f, innerY, paint)

                // Etiqueta RETRASADA
                if (date != null && date < today) {
                    val labelPaint = Paint().apply { color = ReservedPdfColor; style = Paint.Style.FILL }
                    val labelRect = RectF(xOffset + columnWidth - 55f, y + 8f, xOffset + columnWidth - 8f, y + 22f)
                    canvas.drawRoundRect(labelRect, 4f, 4f, labelPaint)
                    paint.color = Color.WHITE; paint.textSize = 7f; paint.typeface = Typeface.DEFAULT_BOLD
                    canvas.drawText("RETRASO", labelRect.centerX() - paint.measureText("RETRASO")/2, labelRect.centerY() + 2.5f, paint)
                }

                // --- LISTADO DE MATERIALES Y LOTES (DINÁMICO) ---
                innerY += 15f
                paint.textSize = 9f

                // Iteramos sobre las asignaciones (máximo 3 para que quepa en el PDF)
                comanda.listaAsignaciones.take(3).forEach { asig ->
                    paint.typeface = Typeface.DEFAULT_BOLD
                    paint.color = if (asig.numeroLote.isNotBlank()) PrimaryPdfColor else WarningPdfColor

                    val matText = "• ${asig.materialNombre.take(20)}"
                    canvas.drawText(matText, xOffset + 10f, innerY, paint)

                    innerY += 11f
                    paint.typeface = Typeface.DEFAULT
                    paint.color = Color.DKGRAY
                    val detailText = if (asig.numeroLote.isNotBlank()) {
                        "  Lote: ${asig.numeroLote} (${asig.cantidadBB} BB)"
                    } else {
                        "  PENDIENTE DE ASIGNAR"
                    }
                    canvas.drawText(detailText, xOffset + 10f, innerY, paint)
                    innerY += 13f
                }

                if (comanda.listaAsignaciones.size > 3) {
                    paint.textSize = 8f; paint.color = Color.GRAY
                    canvas.drawText("  +${comanda.listaAsignaciones.size - 3} materiales adicionales", xOffset + 10f, innerY, paint)
                    innerY += 10f
                }

                // Peso Total y Observaciones (Fijo al final de la celda o tras materiales)
                innerY = y + cellHeight - 25f
                paint.typeface = Typeface.DEFAULT_BOLD; paint.textSize = 9f; paint.color = Color.BLACK
                canvas.drawText("Peso Total: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg", xOffset + 10f, innerY, paint)

                if (!comanda.remarkComanda.isNullOrBlank()) {
                    innerY += 12f
                    paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.ITALIC); paint.textSize = 7.5f; paint.color = Color.GRAY
                    val obs = comanda.remarkComanda!!.take(45) + if (comanda.remarkComanda!!.length > 45) "..." else ""
                    canvas.drawText("Obs: $obs", xOffset + 10f, innerY, paint)
                }

                if (isRightColumn || index == list.size - 1) {
                    y += cellHeight + 10f
                }
            }
            y += 20f
        }

        pdfDocument.finishPage(page)
        saveAndSharePdf(pdfDocument, "Planning_GmrStock_${Clock.System.now().toEpochMilliseconds()}")
    }

    // ============================================================
// 3. GENERAR LISTADO DE VENTAS (DISEÑO GRID + LOGO + INTELIGENTE)
// ============================================================
    actual fun generateVentasReportPdf(
        clienteNombre: String,
        ventas: List<Venta>,
        totalKilos: Double,
        dateRange: String,
        desgloseMateriales: Map<String, Double>
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 45f
        var y = 60f

        // --- CABECERA ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 20f
        paint.color = DarkGrayPdfColor
        canvas.drawText("INFORME DE VENTAS", margin, y, paint)

        // Logo a la derecha alineado con el título
        paint.color = PrimaryPdfColor
        val logoText = "GMR Stock"
        canvas.drawText(logoText, pageWidth - margin - paint.measureText(logoText), y, paint)

        // Ajuste: Rango y Cliente debajo del título (lado izquierdo)
        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY

        // Aplicamos ensureYearInRange para corregir el formato de fecha
        canvas.drawText("Rango: ${ensureYearInRange(dateRange)}", margin, y, paint)

        y += 16f
        canvas.drawText("Cliente: $clienteNombre", margin, y, paint)

        y += 45f

        // --- CUADRO RESUMEN (KPIs) ---
        val summaryRect = RectF(margin, y, pageWidth - margin, y + 70f)
        paint.color = LightGrayBg
        canvas.drawRoundRect(summaryRect, 12f, 12f, paint)

        paint.color = TextPrimaryPdf
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL KILOS", margin + 25f, y + 25f, paint)
        canvas.drawText("LOTES", margin + 210f, y + 25f, paint)
        // Ajuste: Columna de MATERIALES más a la derecha (de 395f a 430f)
        canvas.drawText("MATERIALES", margin + 430f, y + 25f, paint)

        paint.textSize = 17f
        paint.color = PrimaryPdfColor
        canvas.drawText("${formatWeight(totalKilos)} kg", margin + 25f, y + 55f, paint)
        canvas.drawText("${ventas.size}", margin + 210f, y + 55f, paint)
        canvas.drawText("${desgloseMateriales.size}", margin + 430f, y + 55f, paint)

        y += 110f

        // --- DESGLOSE POR MATERIAL (Barras visuales) ---
        paint.color = DarkGrayPdfColor
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DESGLOSE POR MATERIAL", margin, y, paint)
        y += 25f

        desgloseMateriales.forEach { (material, kilos) ->
            val porcentaje = if (totalKilos > 0) (kilos / totalKilos).toFloat() else 0f

            paint.textSize = 10f
            paint.color = TextPrimaryPdf
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            canvas.drawText(material, margin, y, paint)

            val kilosText = "${formatWeight(kilos)} kg (${(porcentaje * 100).toInt()}%)"
            canvas.drawText(kilosText, pageWidth - margin - paint.measureText(kilosText), y, paint)

            y += 8f
            val barWidth = pageWidth - (margin * 2)
            paint.color = Color.rgb(235, 235, 235)
            canvas.drawRoundRect(RectF(margin, y, margin + barWidth, y + 5f), 3f, 3f, paint)

            paint.color = PrimaryPdfColor
            canvas.drawRoundRect(RectF(margin, y, margin + (barWidth * porcentaje), y + 5f), 3f, 3f, paint)
            y += 25f
        }

        y += 15f

        // --- TABLA DETALLADA ---
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DETALLE DE VENTAS", margin, y, paint)
        y += 20f

        // Cabeceras
        paint.textSize = 9f
        paint.color = Color.GRAY
        canvas.drawText("FECHA / LOTE", margin + 5f, y, paint)
        canvas.drawText("MATERIAL", margin + 180f, y, paint)
        val hPeso = "PESO TOTAL"
        canvas.drawText(hPeso, pageWidth - margin - paint.measureText(hPeso) - 5f, y, paint)

        y += 8f
        paint.color = GrayPdfColor
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 20f

        // Filas de ventas
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        ventas.sortedByDescending { it.ventaFecha }.forEachIndexed { index, venta ->
            // Salto de página
            if (y > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            // Fondo cebra
            if (index % 2 != 0) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(RectF(margin, y - 14f, pageWidth - margin, y + 18f), paint)
            }

            paint.color = TextPrimaryPdf
            paint.textSize = 10f
            val fecha = venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
            val fechaStr = if (fecha != null) "${fecha.dayOfMonth}/${fecha.monthNumber}/${fecha.year}" else "---"

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(fechaStr, margin + 5f, y, paint)

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.color = Color.GRAY
            canvas.drawText("Lote: ${venta.ventaLote}", margin + 5f, y + 12f, paint)

            paint.color = TextPrimaryPdf
            canvas.drawText(venta.ventaMaterial?.take(25) ?: "General", margin + 180f, y + 6f, paint)

            val pText = "${formatWeight(venta.ventaPesoTotal?.toDoubleOrNull() ?: 0.0)} kg"
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText(pText, pageWidth - margin - paint.measureText(pText) - 5f, y + 6f, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            y += 35f
        }

        pdfDocument.finishPage(page)
        saveAndSharePdf(pdfDocument, "Ventas_${clienteNombre.replace(" ", "_")}_${Clock.System.now().toEpochMilliseconds()}")
    }

    // ============================================================
    // 4. GENERAR INFORME DE STOCK (NUEVO)
    // ============================================================
    actual fun generateStockReportPdf(
        materialGroups: List<MaterialGroup>,
        totalKilos: Double
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 45f
        var y = 60f

        // --- CABECERA ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = DarkGrayPdfColor
        canvas.drawText("MATERIALES EN STOCK", margin, y, paint)

        paint.color = PrimaryPdfColor
        val logoText = "GMR Stock"
        canvas.drawText(logoText, pageWidth - margin - paint.measureText(logoText), y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY
        val fechaActual = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        canvas.drawText("Fecha de informe: ${fechaActual.dayOfMonth}/${fechaActual.monthNumber}/${fechaActual.year}", margin, y, paint)

        y += 45f

        // --- CUADRO RESUMEN KPIs (ESTILO CENTRADO) ---
        val summaryRect = RectF(margin, y, pageWidth - margin, y + 75f)
        paint.color = LightGrayBg
        canvas.drawRoundRect(summaryRect, 12f, 12f, paint)

        paint.color = TextPrimaryPdf
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("TOTAL KILOS", margin + 25f, y + 25f, paint)
        canvas.drawText("MATERIALES", margin + 225f, y + 25f, paint)
        canvas.drawText("LOTES", margin + 460f, y + 25f, paint)

        paint.textSize = 17f
        paint.color = PrimaryPdfColor
        canvas.drawText("${formatWeight(totalKilos)} kg", margin + 25f, y + 55f, paint)
        canvas.drawText("${materialGroups.size}", margin + 225f, y + 55f, paint)
        canvas.drawText("${materialGroups.sumOf { it.totalLotes }}", margin + 460f, y + 55f, paint)

        y += 115f

        // --- TABLA DETALLADA ---
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("DETALLE DE STOCK", margin, y, paint)
        y += 20f

        // Cabeceras de tabla
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("MATERIALES", margin + 10f, y, paint)
        canvas.drawText("LOTES / BB", margin + 300f, y, paint)
        val labelPeso = "PESO TOTAL"
        canvas.drawText(labelPeso, pageWidth - margin - paint.measureText(labelPeso) - 10f, y, paint)

        y += 8f
        paint.color = GrayPdfColor
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 25f

        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

        materialGroups.sortedBy { it.description }.forEachIndexed { index, group ->
            if (y > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f
            }

            // Fila cebreada
            if (index % 2 != 0) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(RectF(margin, y - 18f, pageWidth - margin, y + 10f), paint)
            }

            paint.color = TextPrimaryPdf
            paint.textSize = 10f
            // Descripción
            val desc = if (group.description.length > 45) group.description.take(42) + "..." else group.description
            canvas.drawText(desc, margin + 10f, y, paint)

            // Info de Lotes y BigBags
            paint.color = Color.GRAY
            canvas.drawText("${group.totalLotes} Lotes / ${group.totalBigBags} BB", margin + 300f, y, paint)

            // Peso
            paint.color = TextPrimaryPdf
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val pesoGroup = group.totalWeight.toString().toDoubleOrNull() ?: 0.0
            val pesoText = "${formatWeight(pesoGroup)} kg"
            canvas.drawText(pesoText, pageWidth - margin - paint.measureText(pesoText) - 10f, y, paint)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)

            y += 30f
        }

        pdfDocument.finishPage(page)
        saveAndSharePdf(pdfDocument, "Stock_Materiales_${Clock.System.now().toEpochMilliseconds()}")
    }

    // ==========================================
    // 5. GENERAR INFORME VERTISOL (ANDROID) - DESGLOSADO POR FECHA
    // ==========================================
    actual fun generateVertisolReportPdf(
        vertisolList: List<com.alius.gmrstockplus.domain.model.Vertisol>,
        totalKilos: Double
    ) {
        val pdfDocument = PdfDocument()
        val pageWidth = 595 // A4
        val pageHeight = 842
        var pageNumber = 1

        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        val paint = Paint()
        val margin = 45f
        var y = 60f

        // --- PREPARACIÓN DE DATOS (Explosión por fecha de trasvase) ---
        val filasDesglosadas = vertisolList.flatMap { lote ->
            lote.vertisolBigBag.groupBy { bb ->
                bb.bbTrasvaseDate?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
            }.map { (fecha, bags) ->
                val fechaStr = if (fecha != null) {
                    "${fecha.dayOfMonth.toString().padStart(2, '0')}/${fecha.monthNumber.toString().padStart(2, '0')}/${fecha.year}"
                } else {
                    "Sin fecha"
                }
                val pesoTotalFecha = bags.sumOf { it.bbWeight.toDoubleOrNull() ?: 0.0 }

                object {
                    val numeroLote = lote.vertisolNumber
                    val descripcion = lote.vertisolDescription
                    val fechaTrasvase = fechaStr
                    val cantidadBB = bags.size
                    val peso = pesoTotalFecha
                }
            }
        }.sortedByDescending { it.fechaTrasvase }

        // --- CABECERA ---
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textSize = 22f
        paint.color = DarkGrayPdfColor
        canvas.drawText("LOTES EN VERTISOL", margin, y, paint)

        paint.color = PrimaryPdfColor
        val logoText = "GMR Stock"
        canvas.drawText(logoText, pageWidth - margin - paint.measureText(logoText), y, paint)

        y += 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        paint.textSize = 11f
        paint.color = Color.GRAY
        val fechaHoy = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        canvas.drawText("Generado el: ${fechaHoy.dayOfMonth}/${fechaHoy.monthNumber}/${fechaHoy.year}", margin, y, paint)

        y += 45f

        // --- CUADRO RESUMEN (KPIs) ---
        val summaryRect = RectF(margin, y, pageWidth - margin, y + 75f)
        paint.color = LightGrayBg
        canvas.drawRoundRect(summaryRect, 12f, 12f, paint)

        paint.color = TextPrimaryPdf
        paint.textSize = 9f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)

        // Etiquetas de los KPIs con nuevas posiciones
        canvas.drawText("TOTAL KILOS", margin + 25f, y + 25f, paint)
        canvas.drawText("LOTES", margin + 250f, y + 25f, paint)    // Movido de 210f a 250f
        canvas.drawText("BIGBAGS", margin + 430f, y + 25f, paint) // Movido de 460f a 430f

        paint.textSize = 17f
        paint.color = PrimaryPdfColor

        // Valores de los KPIs con nuevas posiciones
        canvas.drawText("${formatWeight(totalKilos)} kg", margin + 25f, y + 55f, paint)
        canvas.drawText("${vertisolList.size}", margin + 250f, y + 55f, paint)

        val totalBB = vertisolList.sumOf { it.vertisolBigBag.size }
        canvas.drawText("$totalBB", margin + 430f, y + 55f, paint)

        y += 115f

        // --- TABLA DETALLADA ---
        paint.color = Color.BLACK
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("REGISTROS DE TRASVASE", margin, y, paint)
        y += 20f

        // Cabeceras de tabla
        paint.textSize = 10f
        paint.color = Color.GRAY
        canvas.drawText("LOTE / MATERIAL", margin + 10f, y, paint)
        canvas.drawText("TRASVASE", margin + 260f, y, paint)
        canvas.drawText("BB", margin + 370f, y, paint)
        val labelPeso = "PESO TOTAL"
        canvas.drawText(labelPeso, pageWidth - margin - paint.measureText(labelPeso) - 10f, y, paint)

        y += 8f
        paint.color = GrayPdfColor
        canvas.drawLine(margin, y, pageWidth - margin, y, paint)
        y += 25f

        // --- FILAS DESGLOSADAS ---
        filasDesglosadas.forEachIndexed { index, fila ->
            if (y > pageHeight - 60f) {
                pdfDocument.finishPage(page)
                pageNumber++
                pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, pageNumber).create()
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                y = 60f

                // Repetir cabeceras en nueva página
                paint.textSize = 10f
                paint.color = Color.GRAY
                paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                canvas.drawText("LOTE / MATERIAL", margin + 10f, y, paint)
                canvas.drawText("TRASVASE", margin + 260f, y, paint)
                canvas.drawText("BB", margin + 370f, y, paint)
                canvas.drawText(labelPeso, pageWidth - margin - paint.measureText(labelPeso) - 10f, y, paint)
                y += 25f
            }

            // Fila cebreada
            if (index % 2 != 0) {
                paint.color = Color.rgb(250, 250, 250)
                canvas.drawRect(RectF(margin, y - 16f, pageWidth - margin, y + 22f), paint)
            }

            // Lote (Negrita)
            paint.color = TextPrimaryPdf
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            paint.textSize = 10f
            canvas.drawText(fila.numeroLote, margin + 10f, y, paint)

            // Material (Debajo en Gris)
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 8.5f
            paint.color = Color.GRAY
            val desc = if (fila.descripcion.length > 40) fila.descripcion.take(37) + "..." else fila.descripcion
            canvas.drawText(desc, margin + 10f, y + 12f, paint)

            // Fecha de Trasvase
            paint.color = TextPrimaryPdf
            paint.textSize = 10f
            canvas.drawText(fila.fechaTrasvase, margin + 260f, y + 6f, paint)

            // Cantidad de BigBags de esa fecha
            canvas.drawText("${fila.cantidadBB} BB", margin + 370f, y + 6f, paint)

            // Peso Parcial de esa fecha
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            val pText = "${formatWeight(fila.peso)} kg"
            canvas.drawText(pText, pageWidth - margin - paint.measureText(pText) - 10f, y + 6f, paint)

            y += 38f
        }

        pdfDocument.finishPage(page)
        saveAndSharePdf(pdfDocument, "Reporte_Vertisol_${Clock.System.now().toEpochMilliseconds()}")
    }

    private fun saveAndSharePdf(pdf: PdfDocument, fileName: String) {
        val context = AppContextProvider.appContext
        val file = File(context.cacheDir, "$fileName.pdf")

        try {
            FileOutputStream(file).use { outputStream ->
                pdf.writeTo(outputStream)
            }
            pdf.close()

            val contentUri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Compartir Reporte")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

        } catch (e: Exception) {
            Log.e(TAG, "Error al guardar o compartir PDF: ${e.message}")
        }
    }
}