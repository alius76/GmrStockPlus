package com.alius.gmrstockplus.core.utils

import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.MaterialGroup
import com.alius.gmrstockplus.domain.model.Ratio
import com.alius.gmrstockplus.domain.model.Venta
import kotlinx.datetime.*
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.pdmodel.PDPage
import org.apache.pdfbox.pdmodel.PDPageContentStream
import org.apache.pdfbox.pdmodel.font.PDType1Font
import org.apache.pdfbox.pdmodel.common.PDRectangle
import java.awt.Color
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter
import java.awt.Desktop

actual object PdfGenerator {

    private val PrimaryPdfColor = Color(2, 144, 131)
    private val ReservedPdfColor = Color(183, 28, 28)
    private val DarkGrayPdfColor = Color(85, 85, 85)
    private val TextPrimaryPdf = Color(51, 51, 51)
    private val GrayPdfColor = Color(204, 204, 204)
    private val LightGrayBg = Color(245, 245, 245)
    private val WarningPdfColor = Color(240, 154, 0)

    private fun String.pdfSafe(): String {
        return this.replace("\n", " ").replace("\r", " ").trim()
    }

    // CORRECCIÓN FECHA: Regex flexible para 1 o 2 dígitos
    private fun ensureYearInRange(range: String): String {
        val currentYear = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).year
        return range.split(" ").joinToString(" ") { word ->
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
        val document = PDDocument()
        val margin = 45f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height
        var y = pageHeight - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        // Función interna para manejar saltos de página
        fun checkNewPage(needed: Float = 40f) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = pageHeight - 60f
            }
        }

        // --- CABECERA ---
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("INFORME DE PRODUCCIÓN".pdfSafe())
        contentStream.endText()

        val logoText = "GMR Stock"
        val logoWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(logoText) / 1000 * 22f
        contentStream.beginText()
        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.newLineAtOffset(pageWidth - margin - logoWidth, y)
        contentStream.showText(logoText)
        contentStream.endText()

        y -= 25f
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("Rango: ${ensureYearInRange(dateRange)}".pdfSafe())
        contentStream.endText()

        y -= 45f

        // --- CUADRO RESUMEN (KPIs) - AJUSTADO ---
        contentStream.setNonStrokingColor(LightGrayBg)
        // Dibujamos el rectángulo de 75f de alto
        contentStream.addRect(margin, y - 75f, pageWidth - (margin * 2), 75f)
        contentStream.fill()

        contentStream.setNonStrokingColor(TextPrimaryPdf)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
        contentStream.beginText()
        // Etiquetas centradas verticalmente respecto al rect
        contentStream.newLineAtOffset(margin + 25f, y - 25f)
        contentStream.showText("TOTAL KILOS")
        contentStream.newLineAtOffset(185f, 0f)
        contentStream.showText("LOTES")
        contentStream.newLineAtOffset(200f, 0f)
        contentStream.showText("MEDIA DIARIA")
        contentStream.endText()

        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 17f)
        contentStream.beginText()
        // Valores con el offset ajustado para que no toquen el borde inferior
        contentStream.newLineAtOffset(margin + 25f, y - 55f)
        contentStream.showText("${formatWeight(totalKilos)} kg")
        contentStream.newLineAtOffset(185f, 0f)
        contentStream.showText("${ratios.size}")
        contentStream.newLineAtOffset(200f, 0f)
        contentStream.showText("${formatWeight(promedio)} kg")
        contentStream.endText()

        y -= 100f

        // --- SECCIÓN: DESGLOSE MENSUAL (BARRAS VISUALES) ---
        val mesesEspanol = mapOf(
            1 to "Enero", 2 to "Febrero", 3 to "Marzo", 4 to "Abril", 5 to "Mayo", 6 to "Junio",
            7 to "Julio", 8 to "Agosto", 9 to "Septiembre", 10 to "Octubre", 11 to "Noviembre", 12 to "Diciembre"
        )

        val datosMensuales = ratios.groupBy {
            val date = Instant.fromEpochMilliseconds(it.ratioDate).toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${date.monthNumber}-${date.year}"
        }.map { (key, lista) ->
            val partes = key.split("-")
            val mesNum = partes[0].toInt()
            object {
                val label = "${mesesEspanol[mesNum]} ${partes[1]}"
                val kilos = lista.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 }
                val porcentaje = if (totalKilos > 0) (kilos / totalKilos).toFloat() else 0f
            }
        }.sortedBy { it.label }

        if (datosMensuales.isNotEmpty()) {
            checkNewPage(40f)
            contentStream.setNonStrokingColor(DarkGrayPdfColor)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin, y)
            contentStream.showText("DESGLOSE MENSUAL".pdfSafe())
            contentStream.endText()
            y -= 20f

            datosMensuales.forEach { mes ->
                checkNewPage(35f)
                contentStream.setNonStrokingColor(TextPrimaryPdf)
                contentStream.setFont(PDType1Font.HELVETICA, 10f)
                contentStream.beginText()
                contentStream.newLineAtOffset(margin, y)
                contentStream.showText(mes.label.pdfSafe())
                contentStream.endText()

                val pesoMesText = "${formatWeight(mes.kilos)} kg (${(mes.porcentaje * 100).toInt()}%)"
                val pesoWidth = PDType1Font.HELVETICA.getStringWidth(pesoMesText) / 1000 * 10f
                contentStream.beginText()
                contentStream.newLineAtOffset(pageWidth - margin - pesoWidth, y)
                contentStream.showText(pesoMesText)
                contentStream.endText()

                y -= 10f
                val barWidth = pageWidth - (margin * 2)
                contentStream.setNonStrokingColor(Color(230, 230, 230))
                contentStream.addRect(margin, y, barWidth, 6f)
                contentStream.fill()

                contentStream.setNonStrokingColor(PrimaryPdfColor)
                contentStream.addRect(margin, y, barWidth * mes.porcentaje.coerceIn(0f, 1f), 6f)
                contentStream.fill()
                y -= 25f
            }
            y -= 10f
        }

        // --- TABLA DETALLADA (REGISTRO POR REGISTRO) ---
        checkNewPage(50f)
        contentStream.setNonStrokingColor(Color.BLACK)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 10f, y)
        contentStream.showText("DETALLE DE PRODUCCIÓN".pdfSafe())
        contentStream.endText()

        y -= 20f
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 10f, y)
        contentStream.showText("FECHA")
        contentStream.newLineAtOffset(140f, 0f)
        contentStream.showText("NÚMERO DE LOTE")
        contentStream.endText()

        val labelPesoTab = "PESO TOTAL"
        val labelW = PDType1Font.HELVETICA_BOLD.getStringWidth(labelPesoTab) / 1000 * 10f
        contentStream.beginText()
        contentStream.newLineAtOffset(pageWidth - margin - labelW - 10f, y)
        contentStream.showText(labelPesoTab)
        contentStream.endText()

        y -= 8f
        contentStream.setStrokingColor(GrayPdfColor)
        contentStream.setLineWidth(1f)
        contentStream.moveTo(margin, y)
        contentStream.lineTo(pageWidth - margin, y)
        contentStream.stroke()
        y -= 25f

        ratios.sortedByDescending { it.ratioDate }.forEachIndexed { index, ratio ->
            checkNewPage(30f)

            if (index % 2 != 0) {
                contentStream.setNonStrokingColor(Color(250, 250, 250))
                contentStream.addRect(margin, y - 8f, pageWidth - (margin * 2), 28f)
                contentStream.fill()
            }

            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.setFont(PDType1Font.HELVETICA, 11f)

            val instant = Instant.fromEpochMilliseconds(ratio.ratioDate)
            val f = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val dateStr = "${f.dayOfMonth.toString().padStart(2,'0')}/${f.monthNumber.toString().padStart(2,'0')}/${f.year}"

            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 10f, y)
            contentStream.showText(dateStr)

            val nombreLote = (loteNombresMap[ratio.ratioLoteId] ?: "Desconocido").pdfSafe()
            contentStream.newLineAtOffset(140f, 0f)
            contentStream.showText("Lote: $nombreLote")
            contentStream.endText()

            val peso = ratio.ratioTotalWeight.toDoubleOrNull() ?: 0.0
            val pText = "${formatWeight(peso)} kg"
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 11f)
            val pW = PDType1Font.HELVETICA_BOLD.getStringWidth(pText) / 1000 * 11f
            contentStream.beginText()
            contentStream.newLineAtOffset(pageWidth - margin - pW - 10f, y)
            contentStream.showText(pText)
            contentStream.endText()

            y -= 28f
        }

        contentStream.close()
        val fileName = "Informe_Produccion_${System.currentTimeMillis()}"
        savePdfDesktop(document, fileName)
    }

    // ============================================================
// 2. GENERAR PLANNING COMANDAS (ACTUALIZADO MULTI-MATERIAL - DESKTOP)
// ============================================================
    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val document = PDDocument()
        val margin = 40f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height
        var y = pageHeight - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        val columnWidth = (pageWidth - (margin * 2) - 10f) / 2f
        val cellHeight = 140f // Aumentado para multi-material
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

        fun checkNewPage(needed: Float) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = pageHeight - 60f
            }
        }

        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toSortedMap(compareBy { it })

        // --- CABECERA ---
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText(title.uppercase().pdfSafe())
        contentStream.endText()

        val logoText = "GMR Stock"
        val logoWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(logoText) / 1000 * 22f
        contentStream.beginText()
        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.newLineAtOffset(pageWidth - margin - logoWidth, y)
        contentStream.showText(logoText)
        contentStream.endText()

        y -= 22f
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("Rango: ${dateRange.pdfSafe()}")
        contentStream.endText()

        y -= 45f

        groupedComandas.forEach { (date, list) ->
            checkNewPage(160f)
            val dateText = if (date == null) "SIN FECHA" else "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"

            contentStream.beginText()
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 15f)
            contentStream.setNonStrokingColor(Color.BLACK)
            contentStream.newLineAtOffset(margin, y)
            contentStream.showText(dateText)
            contentStream.endText()

            y -= 15f

            list.forEachIndexed { index, comanda ->
                val isRightColumn = index % 2 != 0
                val xOffset = if (isRightColumn) margin + columnWidth + 10f else margin
                if (!isRightColumn && index > 0) checkNewPage(cellHeight + 10f)

                // Dibujar Celda
                contentStream.setStrokingColor(Color.LIGHT_GRAY)
                contentStream.setLineWidth(0.5f)
                contentStream.addRect(xOffset, y - cellHeight, columnWidth, cellHeight)
                contentStream.stroke()

                var innerY = y - 20f

                // Cliente
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10.5f)
                contentStream.setNonStrokingColor(DarkGrayPdfColor)
                contentStream.newLineAtOffset(xOffset + 10f, innerY)
                contentStream.showText((comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente").take(22).pdfSafe())
                contentStream.endText()

                // Badge RETRASO
                if (date != null && date < today) {
                    val labelWidth = 55f
                    val labelHeight = 14f
                    contentStream.setNonStrokingColor(ReservedPdfColor)
                    contentStream.addRect(xOffset + columnWidth - labelWidth - 8f, innerY - 2f, labelWidth, labelHeight)
                    contentStream.fill()

                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 7f)
                    contentStream.setNonStrokingColor(Color.WHITE)
                    contentStream.newLineAtOffset(xOffset + columnWidth - labelWidth + 3f, innerY + 2f)
                    contentStream.showText("RETRASO")
                    contentStream.endText()
                }

                // --- LISTADO DINÁMICO DE MATERIALES ---
                innerY -= 15f
                comanda.listaAsignaciones.take(3).forEach { asig ->
                    val tieneLote = asig.numeroLote.isNotBlank()

                    // Nombre Material
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
                    contentStream.setNonStrokingColor(if (tieneLote) PrimaryPdfColor else WarningPdfColor)
                    contentStream.newLineAtOffset(xOffset + 10f, innerY)
                    contentStream.showText("- ${asig.materialNombre.take(25).pdfSafe()}")
                    contentStream.endText()

                    innerY -= 11f

                    // Info Lote
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA, 8.5f)
                    contentStream.setNonStrokingColor(Color.DARK_GRAY)
                    contentStream.newLineAtOffset(xOffset + 18f, innerY)
                    val infoLote = if (tieneLote) "Lote: ${asig.numeroLote} (${asig.cantidadBB} BB)" else "PENDIENTE ASIGNAR"
                    contentStream.showText(infoLote.pdfSafe())
                    contentStream.endText()

                    innerY -= 13f
                }

                if (comanda.listaAsignaciones.size > 3) {
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA, 7.5f)
                    contentStream.setNonStrokingColor(Color.GRAY)
                    contentStream.newLineAtOffset(xOffset + 18f, innerY)
                    contentStream.showText("+${comanda.listaAsignaciones.size - 3} adicionales...")
                    contentStream.endText()
                }

                // --- INFO FIJA INFERIOR (Peso y Obs) ---
                val footerY = y - cellHeight + 25f
                contentStream.beginText()
                contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
                contentStream.setNonStrokingColor(Color.BLACK)
                contentStream.newLineAtOffset(xOffset + 10f, footerY)
                contentStream.showText("Peso Total: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg")
                contentStream.endText()

                if (!comanda.remarkComanda.isNullOrBlank()) {
                    contentStream.beginText()
                    contentStream.setFont(PDType1Font.HELVETICA_OBLIQUE, 7.5f)
                    contentStream.setNonStrokingColor(Color.GRAY)
                    contentStream.newLineAtOffset(xOffset + 10f, footerY - 12f)
                    val obs = comanda.remarkComanda!!.take(45).pdfSafe()
                    contentStream.showText("Obs: $obs")
                    contentStream.endText()
                }

                if (isRightColumn || index == list.size - 1) y -= (cellHeight + 10f)
            }
            y -= 20f
        }

        contentStream.close()
        val fileName = "Planning_GmrStock_${System.currentTimeMillis()}"
        savePdfDesktop(document, fileName)
    }

    // ============================================================
// 3. GENERAR LISTADO DE VENTAS (DISEÑO GRID + LOGO - DESKTOP)
// ============================================================
    actual fun generateVentasReportPdf(
        clienteNombre: String,
        ventas: List<Venta>,
        totalKilos: Double,
        dateRange: String,
        desgloseMateriales: Map<String, Double>
    ) {
        val document = PDDocument()
        val margin = 45f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height
        var y = pageHeight - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        fun checkNewPage(needed: Float = 35f) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = pageHeight - 60f
            }
        }

        // --- CABECERA ---
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 20f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("INFORME DE VENTAS".pdfSafe())
        contentStream.endText()

        val logoText = "GMR Stock"
        val logoWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(logoText) / 1000 * 20f
        contentStream.beginText()
        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.newLineAtOffset(pageWidth - margin - logoWidth, y)
        contentStream.showText(logoText)
        contentStream.endText()

        y -= 22f
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("Rango: ${ensureYearInRange(dateRange).pdfSafe()}")
        contentStream.endText()

        y -= 16f
        contentStream.beginText()
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("Cliente: ${clienteNombre.pdfSafe()}")
        contentStream.endText()

        y -= 45f

        // --- CUADRO RESUMEN (KPIs) - AJUSTADO PARA CENTRADO ---
        contentStream.setNonStrokingColor(LightGrayBg)
        // Dibujamos el rectángulo con alto 75f (igual que Producción) para consistencia
        contentStream.addRect(margin, y - 75f, pageWidth - (margin * 2), 75f)
        contentStream.fill()

        contentStream.setNonStrokingColor(TextPrimaryPdf)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
        contentStream.beginText()
        // Etiquetas: bajamos 25f desde el techo (y)
        contentStream.newLineAtOffset(margin + 25f, y - 25f)
        contentStream.showText("TOTAL KILOS")
        contentStream.newLineAtOffset(185f, 0f)
        contentStream.showText("LOTES")
        contentStream.newLineAtOffset(220f, 0f)
        contentStream.showText("MATERIALES")
        contentStream.endText()

        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 17f)
        contentStream.beginText()
        // Valores: bajamos 55f desde el techo (y)
        contentStream.newLineAtOffset(margin + 25f, y - 55f)
        contentStream.showText("${formatWeight(totalKilos)} kg")
        contentStream.newLineAtOffset(185f, 0f)
        contentStream.showText("${ventas.size}")
        contentStream.newLineAtOffset(220f, 0f)
        contentStream.showText("${desgloseMateriales.size}")
        contentStream.endText()

        y -= 105f

        // --- DESGLOSE POR MATERIAL (Barras visuales) ---
        checkNewPage(desgloseMateriales.size * 30f + 20f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("DESGLOSE POR MATERIAL")
        contentStream.endText()
        y -= 25f

        desgloseMateriales.forEach { (material, kilos) ->
            val porcentaje = if (totalKilos > 0) (kilos / totalKilos).toFloat() else 0f

            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin, y)
            contentStream.showText(material.pdfSafe())
            contentStream.endText()

            val kilosText = "${formatWeight(kilos)} kg (${(porcentaje * 100).toInt()}%)"
            val textWidth = PDType1Font.HELVETICA.getStringWidth(kilosText) / 1000 * 10f
            contentStream.beginText()
            contentStream.newLineAtOffset(pageWidth - margin - textWidth, y)
            contentStream.showText(kilosText)
            contentStream.endText()

            y -= 10f
            val barWidth = pageWidth - (margin * 2)
            contentStream.setNonStrokingColor(Color(235, 235, 235))
            contentStream.addRect(margin, y, barWidth, 5f)
            contentStream.fill()

            contentStream.setNonStrokingColor(PrimaryPdfColor)
            contentStream.addRect(margin, y, barWidth * porcentaje.coerceIn(0f, 1f), 5f)
            contentStream.fill()
            y -= 25f
        }

        y -= 15f

        // --- TABLA DETALLADA ---
        checkNewPage(40f)
        contentStream.setNonStrokingColor(Color.BLACK)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("DETALLE DE VENTAS")
        contentStream.endText()
        y -= 20f

        // Cabeceras
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 5f, y)
        contentStream.showText("FECHA / LOTE")
        contentStream.newLineAtOffset(180f, 0f)
        contentStream.showText("MATERIAL")
        contentStream.endText()

        val hPeso = "PESO TOTAL"
        val hPesoW = PDType1Font.HELVETICA_BOLD.getStringWidth(hPeso) / 1000 * 9f
        contentStream.beginText()
        contentStream.newLineAtOffset(pageWidth - margin - hPesoW - 5f, y)
        contentStream.showText(hPeso)
        contentStream.endText()

        y -= 8f
        contentStream.setStrokingColor(GrayPdfColor)
        contentStream.setLineWidth(1f)
        contentStream.moveTo(margin, y)
        contentStream.lineTo(pageWidth - margin, y)
        contentStream.stroke()
        y -= 20f

        // Filas de ventas
        ventas.sortedByDescending { it.ventaFecha }.forEachIndexed { index, venta ->
            checkNewPage(40f)

            if (index % 2 != 0) {
                contentStream.setNonStrokingColor(Color(250, 250, 250))
                contentStream.addRect(margin, y - 14f, pageWidth - (margin * 2), 32f)
                contentStream.fill()
            }

            val fecha = venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
            val fechaStr = if (fecha != null) "${fecha.dayOfMonth.toString().padStart(2,'0')}/${fecha.monthNumber.toString().padStart(2,'0')}/${fecha.year}" else "---"

            // Fecha
            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 5f, y)
            contentStream.showText(fechaStr)
            contentStream.endText()

            // Lote (debajo de fecha)
            contentStream.setNonStrokingColor(Color.GRAY)
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 5f, y - 12f)
            contentStream.showText("Lote: ${venta.ventaLote.pdfSafe()}")
            contentStream.endText()

            // Material
            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 180f, y - 6f)
            contentStream.showText((venta.ventaMaterial?.take(25) ?: "General").pdfSafe())
            contentStream.endText()

            // Peso
            val pText = "${formatWeight(venta.ventaPesoTotal?.toDoubleOrNull() ?: 0.0)} kg"
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
            val pTextW = PDType1Font.HELVETICA_BOLD.getStringWidth(pText) / 1000 * 10f
            contentStream.beginText()
            contentStream.newLineAtOffset(pageWidth - margin - pTextW - 5f, y - 6f)
            contentStream.showText(pText)
            contentStream.endText()

            y -= 35f
        }

        contentStream.close()
        savePdfDesktop(document, "Ventas_${clienteNombre.replace(" ", "_")}")
    }

    // ============================================================
    // 4. GENERAR INFORME DE STOCK (NUEVO - PDFBOX DESKTOP)
    // ============================================================
    actual fun generateStockReportPdf(
        materialGroups: List<MaterialGroup>,
        totalKilos: Double
    ) {
        val document = PDDocument()
        val margin = 45f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height
        var y = pageHeight - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        fun checkNewPage(needed: Float = 40f) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = pageHeight - 60f
            }
        }

        // --- CABECERA ---
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("MATERIALES EN STOCK".pdfSafe())
        contentStream.endText()

        val logoText = "GMR Stock"
        val logoWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(logoText) / 1000 * 22f
        contentStream.beginText()
        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.newLineAtOffset(pageWidth - margin - logoWidth, y)
        contentStream.showText(logoText)
        contentStream.endText()

        y -= 25f
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        val fechaActual = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        contentStream.showText("Fecha de informe: ${fechaActual.dayOfMonth}/${fechaActual.monthNumber}/${fechaActual.year}")
        contentStream.endText()

        y -= 45f

        // --- CUADRO RESUMEN KPIs ---
        contentStream.setNonStrokingColor(LightGrayBg)
        contentStream.addRect(margin, y - 75f, pageWidth - (margin * 2), 75f)
        contentStream.fill()

        contentStream.setNonStrokingColor(TextPrimaryPdf)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
        contentStream.beginText()
        // Posición inicial: margin + 25
        contentStream.newLineAtOffset(margin + 25f, y - 25f)
        contentStream.showText("TOTAL KILOS")
        // Salto a Materiales: +200 (Total X = margin + 225)
        contentStream.newLineAtOffset(200f, 0f)
        contentStream.showText("MATERIALES")
        // Salto a Lotes: +235 (Total X = margin + 460) -> Desplazado a la derecha
        contentStream.newLineAtOffset(235f, 0f)
        contentStream.showText("LOTES")
        contentStream.endText()

        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 17f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 25f, y - 55f)
        contentStream.showText("${formatWeight(totalKilos)} kg")
        contentStream.newLineAtOffset(200f, 0f)
        contentStream.showText("${materialGroups.size}")
        contentStream.newLineAtOffset(235f, 0f) // Mismo desplazamiento que las etiquetas
        contentStream.showText("${materialGroups.sumOf { it.totalLotes }}")
        contentStream.endText()

        y -= 115f

        // --- TABLA DETALLADA ---
        contentStream.setNonStrokingColor(Color.BLACK)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 10f, y)
        contentStream.showText("DETALLE DE STOCK".pdfSafe())
        contentStream.endText()

        y -= 20f
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 10f, y)
        contentStream.showText("MATERIALES")
        contentStream.newLineAtOffset(290f, 0f)
        contentStream.showText("LOTES / BB")
        contentStream.endText()

        val labelPeso = "PESO TOTAL"
        val labelW = PDType1Font.HELVETICA_BOLD.getStringWidth(labelPeso) / 1000 * 10f
        contentStream.beginText()
        contentStream.newLineAtOffset(pageWidth - margin - labelW - 10f, y)
        contentStream.showText(labelPeso)
        contentStream.endText()

        y -= 8f
        contentStream.setStrokingColor(GrayPdfColor)
        contentStream.setLineWidth(1f)
        contentStream.moveTo(margin, y)
        contentStream.lineTo(pageWidth - margin, y)
        contentStream.stroke()
        y -= 25f

        materialGroups.sortedBy { it.description }.forEachIndexed { index, group ->
            checkNewPage(30f)

            // Fila cebreada
            if (index % 2 != 0) {
                contentStream.setNonStrokingColor(Color(250, 250, 250))
                contentStream.addRect(margin, y - 8f, pageWidth - (margin * 2), 28f)
                contentStream.fill()
            }

            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.setFont(PDType1Font.HELVETICA, 10f)

            // Descripción
            val desc = if (group.description.length > 45) group.description.take(42) + "..." else group.description
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 10f, y)
            contentStream.showText(desc.pdfSafe())

            // Info Lotes / BB
            contentStream.setNonStrokingColor(Color.GRAY)
            contentStream.newLineAtOffset(290f, 0f)
            contentStream.showText("${group.totalLotes} Lotes / ${group.totalBigBags} BB")
            contentStream.endText()

            // Peso Total
            val pesoGroup = group.totalWeight.toString().toDoubleOrNull() ?: 0.0
            val pText = "${formatWeight(pesoGroup)} kg"
            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
            val pW = PDType1Font.HELVETICA_BOLD.getStringWidth(pText) / 1000 * 10f
            contentStream.beginText()
            contentStream.newLineAtOffset(pageWidth - margin - pW - 10f, y)
            contentStream.showText(pText)
            contentStream.endText()

            y -= 30f
        }

        contentStream.close()
        // Uso del timestamp para el nombre del archivo igual que en Android
        savePdfDesktop(document, "Stock_Materiales_${Clock.System.now().toEpochMilliseconds()}")
    }

    // ============================================================
    // 5. GENERAR INFORME VERTISOL (DESKTOP - DESGLOSADO POR FECHA)
    // ============================================================
    actual fun generateVertisolReportPdf(
        vertisolList: List<com.alius.gmrstockplus.domain.model.Vertisol>,
        totalKilos: Double
    ) {
        val document = PDDocument()
        val margin = 45f
        val pageWidth = PDRectangle.A4.width
        val pageHeight = PDRectangle.A4.height
        var y = pageHeight - 60f

        var currentPage = PDPage(PDRectangle.A4)
        document.addPage(currentPage)
        var contentStream = PDPageContentStream(document, currentPage)

        // Helper para saltos de página
        fun checkNewPage(needed: Float = 40f) {
            if (y - needed < 60f) {
                contentStream.close()
                currentPage = PDPage(PDRectangle.A4)
                document.addPage(currentPage)
                contentStream = PDPageContentStream(document, currentPage)
                y = pageHeight - 60f
            }
        }

        // --- PREPARACIÓN DE DATOS ---
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
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 22f)
        contentStream.setNonStrokingColor(DarkGrayPdfColor)
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("LOTES EN VERTISOL".pdfSafe())
        contentStream.endText()

        val logoText = "GMR Stock"
        val logoWidth = PDType1Font.HELVETICA_BOLD.getStringWidth(logoText) / 1000 * 22f
        contentStream.beginText()
        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.newLineAtOffset(pageWidth - margin - logoWidth, y)
        contentStream.showText(logoText)
        contentStream.endText()

        y -= 25f
        contentStream.beginText()
        contentStream.setFont(PDType1Font.HELVETICA, 11f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.newLineAtOffset(margin, y)
        val fechaActual = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        contentStream.showText("Generado el: ${fechaActual.dayOfMonth}/${fechaActual.monthNumber}/${fechaActual.year}")
        contentStream.endText()

        y -= 45f

        // --- CUADRO RESUMEN (KPIs) ---
        contentStream.setNonStrokingColor(LightGrayBg)
        contentStream.addRect(margin, y - 75f, pageWidth - (margin * 2), 75f)
        contentStream.fill()

        contentStream.setNonStrokingColor(TextPrimaryPdf)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 9f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 25f, y - 25f)
        contentStream.showText("TOTAL KILOS")
        contentStream.newLineAtOffset(225f, 0f)
        contentStream.showText("LOTES")
        contentStream.newLineAtOffset(180f, 0f)
        contentStream.showText("BIGBAGS")
        contentStream.endText()

        contentStream.setNonStrokingColor(PrimaryPdfColor)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 17f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 25f, y - 55f)
        contentStream.showText("${formatWeight(totalKilos)} kg")
        contentStream.newLineAtOffset(225f, 0f)
        contentStream.showText("${vertisolList.size}")
        contentStream.newLineAtOffset(180f, 0f)
        val totalBB = vertisolList.sumOf { it.vertisolBigBag.size }
        contentStream.showText("$totalBB")
        contentStream.endText()

        y -= 115f

        // --- TABLA DETALLADA ---
        contentStream.setNonStrokingColor(Color.BLACK)
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 12f)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin, y)
        contentStream.showText("REGISTROS DE TRASVASE".pdfSafe())
        contentStream.endText()

        y -= 20f
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
        contentStream.setNonStrokingColor(Color.GRAY)
        contentStream.beginText()
        contentStream.newLineAtOffset(margin + 10f, y)
        contentStream.showText("LOTE / MATERIAL")
        contentStream.newLineAtOffset(250f, 0f)
        contentStream.showText("TRASVASE")
        contentStream.newLineAtOffset(110f, 0f)
        contentStream.showText("BB")
        contentStream.endText()

        val labelPeso = "PESO TOTAL"
        val lpW = PDType1Font.HELVETICA_BOLD.getStringWidth(labelPeso) / 1000 * 10f
        contentStream.beginText()
        contentStream.newLineAtOffset(pageWidth - margin - lpW - 10f, y)
        contentStream.showText(labelPeso)
        contentStream.endText()

        y -= 8f
        contentStream.setStrokingColor(GrayPdfColor)
        contentStream.setLineWidth(1f)
        contentStream.moveTo(margin, y)
        contentStream.lineTo(pageWidth - margin, y)
        contentStream.stroke()

        y -= 30f // Espacio inicial antes de la primera fila

        // --- FILAS DESGLOSADAS ---
        filasDesglosadas.forEachIndexed { index, fila ->
            checkNewPage(45f)

            // Fila cebreada: ajustamos el rectángulo para que el texto no quede al límite inferior
            if (index % 2 != 0) {
                contentStream.setNonStrokingColor(Color(250, 250, 250))
                // Subimos el rectángulo 18 puntos respecto a 'y' para centrar el contenido
                contentStream.addRect(margin, y - 18f, pageWidth - (margin * 2), 38f)
                contentStream.fill()
            }

            // Lote (Negrita) - Posición base
            contentStream.setNonStrokingColor(TextPrimaryPdf)
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 10f, y)
            contentStream.showText(fila.numeroLote.pdfSafe())
            contentStream.endText()

            // Material - Lo subimos un poco (antes y - 12f, ahora y - 10f) para que no se corte
            contentStream.setFont(PDType1Font.HELVETICA, 8.5f)
            contentStream.setNonStrokingColor(Color.GRAY)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 10f, y - 10f)
            val desc = if (fila.descripcion.length > 40) fila.descripcion.take(37) + "..." else fila.descripcion
            contentStream.showText(desc.pdfSafe())
            contentStream.endText()

            // Datos de columnas (Fecha, BB, Peso) - Los centramos un poco mejor verticalmente
            contentStream.setNonStrokingColor(TextPrimaryPdf)

            // Fecha de Trasvase
            contentStream.setFont(PDType1Font.HELVETICA, 10f)
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 260f, y - 2f)
            contentStream.showText(fila.fechaTrasvase)
            contentStream.endText()

            // BigBags
            contentStream.beginText()
            contentStream.newLineAtOffset(margin + 370f, y - 2f)
            contentStream.showText("${fila.cantidadBB} BB")
            contentStream.endText()

            // Peso Parcial
            val pText = "${formatWeight(fila.peso)} kg"
            contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10f)
            val pW = PDType1Font.HELVETICA_BOLD.getStringWidth(pText) / 1000 * 10f
            contentStream.beginText()
            contentStream.newLineAtOffset(pageWidth - margin - pW - 10f, y - 2f)
            contentStream.showText(pText)
            contentStream.endText()

            y -= 38f // Salto a la siguiente fila
        }

        contentStream.close()
        savePdfDesktop(document, "Reporte_Vertisol_${Clock.System.now().toEpochMilliseconds()}")
    }

    private fun savePdfDesktop(document: PDDocument, fileName: String) {
        val fileChooser = JFileChooser().apply {
            dialogTitle = "Guardar PDF"
            fileFilter = FileNameExtensionFilter("Documento PDF", "pdf")
            selectedFile = File("$fileName.pdf")
        }

        if (fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
            val fileToSave = fileChooser.selectedFile
            val finalFile = if (fileToSave.absolutePath.endsWith(".pdf")) fileToSave else File("${fileToSave.absolutePath}.pdf")
            document.save(finalFile)
            document.close()
            if (Desktop.isDesktopSupported()) Desktop.getDesktop().open(finalFile)
        } else {
            document.close()
        }
    }
}