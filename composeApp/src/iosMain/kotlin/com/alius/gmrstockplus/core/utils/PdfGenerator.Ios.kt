package com.alius.gmrstockplus.core.utils

import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.Ratio
import com.alius.gmrstockplus.domain.model.Venta
import kotlinx.cinterop.*
import kotlinx.datetime.*
import platform.CoreGraphics.*
import platform.Foundation.*
import platform.UIKit.*


@OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)
actual object PdfGenerator {

    private val primaryColor = UIColor.colorWithRed(2.0/255.0, green = 144.0/255.0, blue = 131.0/255.0, alpha = 1.0)
    private val reservedColor = UIColor.colorWithRed(183.0/255.0, green = 28.0/255.0, blue = 28.0/255.0, alpha = 1.0)
    private val darkGrayColor = UIColor.colorWithRed(85.0/255.0, green = 85.0/255.0, blue = 85.0/255.0, alpha = 1.0)
    private val lightGrayBg = UIColor.colorWithRed(245.0/255.0, green = 245.0/255.0, blue = 245.0/255.0, alpha = 1.0)
    private val textPrimaryColor = UIColor.colorWithRed(51.0/255.0, green = 51.0/255.0, blue = 51.0/255.0, alpha = 1.0)
    private val warningColor = UIColor.colorWithRed(240.0/255.0, green = 154.0/255.0, blue = 0.0/255.0, alpha = 1.0)

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

    // ==========================================
    // 1. GENERAR INFORME DE PRODUCCIÓN (iOS)
    // ==========================================
    actual fun generateProductionReportPdf(
        ratios: List<Ratio>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String,
        loteNombresMap: Map<String, String>
    ) {
        // Nombre de archivo con marca de tiempo igual que en Android
        val fileName = "Informe_Produccion_${Clock.System.now().toEpochMilliseconds()}.pdf"
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = cacheDirectory + "/" + fileName

        val pageWidth = 595.2
        val pageHeight = 841.8
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)
        val margin = 45.0

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        var currentY = 60.0

        // --- CABECERA ---
        drawText("INFORME DE PRODUCCIÓN", margin, currentY, UIFont.boldSystemFontOfSize(22.0), darkGrayColor)

        val logoText = "GMR Stock"
        val logoWidth = (logoText as NSString).sizeWithAttributes(
            mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(22.0)) as Map<Any?, *>
        ).useContents { width }
        drawText(logoText, pageWidth - margin - logoWidth, currentY, UIFont.boldSystemFontOfSize(22.0), primaryColor)

        currentY += 22.0
        drawText("Rango: ${ensureYearInRange(dateRange)}", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)
        currentY += 45.0

        // --- CUADRO RESUMEN (KPIs) ---
        val summaryRect = CGRectMake(margin, currentY, pageWidth - (margin * 2), 75.0)
        lightGrayBg.setFill()
        UIBezierPath.bezierPathWithRoundedRect(summaryRect, 12.0).fill()

        drawText("TOTAL KILOS", margin + 25.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("LOTES", margin + 210.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("MEDIA DIARIA", margin + 410.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)

        drawText("${formatWeight(totalKilos)} kg", margin + 25.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${ratios.size}", margin + 210.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${formatWeight(promedio)} kg", margin + 410.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)

        currentY += 100.0

        // --- DESGLOSE MENSUAL ---
        val mesesEspanol = mapOf(
            1 to "Enero", 2 to "Febrero", 3 to "Marzo", 4 to "Abril", 5 to "Mayo", 6 to "Junio",
            7 to "Julio", 8 to "Agosto", 9 to "Septiembre", 10 to "Octubre", 11 to "Noviembre", 12 to "Diciembre"
        )

        val datosMensuales = ratios.groupBy {
            val date = Instant.fromEpochMilliseconds(it.ratioDate).toLocalDateTime(TimeZone.currentSystemDefault()).date
            "${date.monthNumber}-${date.year}"
        }.map { (key, lista) ->
            val partes = key.split("-")
            object {
                val label = "${mesesEspanol[partes[0].toInt()]} ${partes[1]}"
                val kilos = lista.sumOf { it.ratioTotalWeight.toDoubleOrNull() ?: 0.0 }
                val porcentaje = if (totalKilos > 0) (kilos / totalKilos).toFloat() else 0f
            }
        }.sortedBy { it.label }

        // MODIFICACIÓN: isNotEmpty() para que aparezca siempre
        if (datosMensuales.isNotEmpty()) {
            drawText("DESGLOSE MENSUAL", margin, currentY, UIFont.boldSystemFontOfSize(12.0), darkGrayColor)
            currentY += 20.0

            datosMensuales.forEach { mes ->
                if (currentY + 50.0 > pageHeight - 60.0) {
                    UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                    currentY = 60.0
                }
                drawText(mes.label, margin, currentY, UIFont.systemFontOfSize(10.0), textPrimaryColor)
                val pesoMesText = "${formatWeight(mes.kilos)} kg (${(mes.porcentaje * 100).toInt()}%)"
                val pWidth = (pesoMesText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.systemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
                drawText(pesoMesText, pageWidth - margin - pWidth, currentY, UIFont.systemFontOfSize(10.0), textPrimaryColor)

                currentY += 12.0
                UIColor.colorWithWhite(0.9, 1.0).setFill()
                UIRectFill(CGRectMake(margin, currentY, pageWidth - (margin * 2), 6.0))
                primaryColor.setFill()
                UIRectFill(CGRectMake(margin, currentY, (pageWidth - (margin * 2)) * mes.porcentaje.toDouble(), 6.0))
                currentY += 25.0
            }
            currentY += 10.0
        }

        // --- TABLA DETALLADA (REGISTRO POR REGISTRO) ---
        if (currentY + 60.0 > pageHeight - 60.0) {
            UIGraphicsBeginPDFPageWithInfo(pageRect, null)
            currentY = 60.0
        }

        drawText("DETALLE DE PRODUCCIÓN", margin + 10.0, currentY, UIFont.boldSystemFontOfSize(12.0), UIColor.blackColor)
        currentY += 20.0
        drawText("FECHA", margin + 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)
        drawText("NÚMERO DE LOTE", margin + 140.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)

        val labelPeso = "PESO TOTAL"
        val lpWidth = (labelPeso as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
        drawText(labelPeso, pageWidth - margin - lpWidth - 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)

        currentY += 15.0
        val context = UIGraphicsGetCurrentContext()
        CGContextSetStrokeColorWithColor(context, UIColor.lightGrayColor.CGColor)
        CGContextSetLineWidth(context, 1.0)
        CGContextMoveToPoint(context, margin, currentY)
        CGContextAddLineToPoint(context, pageWidth - margin, currentY)
        CGContextStrokePath(context)
        currentY += 20.0

        ratios.sortedByDescending { it.ratioDate }.forEachIndexed { index, ratio ->
            if (currentY + 35.0 > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }

            if (index % 2 != 0) {
                val rowRect = CGRectMake(margin, currentY - 8.0, pageWidth - (margin * 2), 28.0)
                UIColor.colorWithWhite(0.98, 1.0).setFill()
                UIRectFill(rowRect)
            }

            val instant = Instant.fromEpochMilliseconds(ratio.ratioDate)
            val f = instant.toLocalDateTime(TimeZone.currentSystemDefault()).date
            val dateStr = "${f.dayOfMonth.toString().padStart(2,'0')}/${f.monthNumber.toString().padStart(2,'0')}/${f.year}"

            drawText(dateStr, margin + 10.0, currentY, UIFont.systemFontOfSize(11.0), textPrimaryColor)

            val nombreLote = loteNombresMap[ratio.ratioLoteId] ?: "Desconocido"
            drawText("Lote: $nombreLote", margin + 140.0, currentY, UIFont.systemFontOfSize(11.0), textPrimaryColor)

            val pesoText = "${formatWeight(ratio.ratioTotalWeight.toDoubleOrNull() ?: 0.0)} kg"
            val pesoWidth = (pesoText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(11.0)) as Map<Any?, *>).useContents { width }
            drawText(pesoText, pageWidth - margin - pesoWidth - 10.0, currentY, UIFont.boldSystemFontOfSize(11.0), textPrimaryColor)

            currentY += 28.0
        }

        UIGraphicsEndPDFContext()
        showShareSheet(filePath)
    }

    // ============================================================
    // 2. GENERAR PLANNING COMANDAS (iOS)
    // ============================================================
    actual fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    ) {
        val fileName = "Planning_Comandas_${Clock.System.now().toEpochMilliseconds()}.pdf"
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = cacheDirectory + "/" + fileName

        val pageWidth = 595.2
        val pageHeight = 841.8
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)
        val margin = 40.0
        val columnWidth = (pageWidth - (margin * 2.0) - 10.0) / 2.0
        val cellHeight = 115.0

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        var currentY = 60.0

        fun checkNewPage(needed: Double) {
            if (currentY + needed > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }
        }

        val groupedComandas = comandas
            .filter { !it.fueVendidoComanda }
            .groupBy { it.dateBookedComanda?.toLocalDateTime(TimeZone.currentSystemDefault())?.date }
            .toList()
            .sortedBy { it.first }

        // --- CABECERA ---
        drawText(title.uppercase(), margin, currentY, UIFont.boldSystemFontOfSize(22.0), darkGrayColor)

        val logoText = "GMR Stock"
        val logoWidth = (logoText as NSString).sizeWithAttributes(
            mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(22.0)) as Map<Any?, *>
        ).useContents { width }
        drawText(logoText, pageWidth - margin - logoWidth, currentY, UIFont.boldSystemFontOfSize(22.0), primaryColor)

        currentY += 22.0
        drawText("Rango: ${ensureYearInRange(dateRange)}", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)
        currentY += 45.0

        groupedComandas.forEach { (date, list) ->
            checkNewPage(155.0)

            val dateText = if (date == null) "SIN FECHA" else "${date.dayOfMonth.toString().padStart(2, '0')}/${date.monthNumber.toString().padStart(2, '0')}/${date.year}"
            drawText(dateText, margin, currentY, UIFont.boldSystemFontOfSize(15.0), UIColor.blackColor)
            currentY += 15.0

            list.forEachIndexed { index, comanda ->
                val isRightColumn = index % 2 != 0
                val xOffset = if (isRightColumn) margin + columnWidth + 10.0 else margin
                if (!isRightColumn && index > 0) checkNewPage(cellHeight + 10.0)

                val rect = CGRectMake(xOffset, currentY, columnWidth, cellHeight)
                UIColor.lightGrayColor.setStroke()
                UIBezierPath.bezierPathWithRoundedRect(rect, 8.0).apply { setLineWidth(0.5); stroke() }

                var innerY = currentY + 10.0
                val clienteNombre = comanda.bookedClientComanda?.cliNombre ?: "Sin Cliente"
                val truncatedNombre = if (clienteNombre.length > 25) clienteNombre.take(22) + "..." else clienteNombre
                drawText(truncatedNombre, xOffset + 10.0, innerY, UIFont.boldSystemFontOfSize(11.0), darkGrayColor)

                if (date != null && date < today) {
                    val labelRect = CGRectMake(xOffset + columnWidth - 65.0, currentY + 8.0, 57.0, 14.0)
                    reservedColor.setFill()
                    UIBezierPath.bezierPathWithRoundedRect(labelRect, 4.0).fill()
                    drawTextInRect("RETRASO", labelRect, UIFont.boldSystemFontOfSize(7.0), UIColor.whiteColor, NSTextAlignmentCenter)
                }

                innerY += 18.0
                drawText("Material: ${comanda.descriptionLoteComanda.take(28)}", xOffset + 10.0, innerY, UIFont.systemFontOfSize(10.0), textPrimaryColor)
                innerY += 15.0
                drawText("Peso: ${formatWeight(comanda.totalWeightComanda?.toDoubleOrNull() ?: 0.0)} Kg", xOffset + 10.0, innerY, UIFont.systemFontOfSize(10.0), UIColor.grayColor)
                innerY += 18.0

                val isAssigned = comanda.numberLoteComanda.isNotBlank()
                drawText(if (isAssigned) "Lote: ${comanda.numberLoteComanda}" else "PENDIENTE DE ASIGNAR", xOffset + 10.0, innerY, UIFont.boldSystemFontOfSize(11.0), if (isAssigned) primaryColor else warningColor)

                if (comanda.remarkComanda.isNotBlank()) {
                    innerY += 14.0
                    val obs = if (comanda.remarkComanda.length > 30) comanda.remarkComanda.take(27) + "..." else comanda.remarkComanda
                    drawText("Obs: $obs", xOffset + 10.0, innerY, UIFont.italicSystemFontOfSize(8.0), UIColor.grayColor)
                }

                if (isRightColumn || index == list.size - 1) currentY += cellHeight + 10.0
            }
            currentY += 20.0
        }

        UIGraphicsEndPDFContext()
        showShareSheet(filePath)
    }

    // ============================================================
    // 3. GENERAR LISTADO DE VENTAS (DISEÑO GRID + LOGO - iOS)
    // ============================================================
    actual fun generateVentasReportPdf(
        clienteNombre: String,
        ventas: List<Venta>,
        totalKilos: Double,
        dateRange: String,
        desgloseMateriales: Map<String, Double>
    ) {
        val fileName = "Informe_Ventas_${Clock.System.now().toEpochMilliseconds()}.pdf"
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = cacheDirectory + "/" + fileName

        val pageWidth = 595.2
        val pageHeight = 841.8
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)
        val margin = 45.0

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        var currentY = 60.0

        fun checkNewPage(needed: Double) {
            if (currentY + needed > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }
        }

        // --- CABECERA ---
        drawText("INFORME DE VENTAS", margin, currentY, UIFont.boldSystemFontOfSize(20.0), darkGrayColor)

        val logoText = "GMR Stock"
        val logoWidth = (logoText as NSString).sizeWithAttributes(
            mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(20.0)) as Map<Any?, *>
        ).useContents { width }
        drawText(logoText, pageWidth - margin - logoWidth, currentY, UIFont.boldSystemFontOfSize(20.0), primaryColor)

        currentY += 22.0
        drawText("Rango: ${ensureYearInRange(dateRange)}", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)

        currentY += 16.0
        drawText("Cliente: $clienteNombre", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)

        currentY += 45.0

        // --- CUADRO RESUMEN (KPIs) ---
        val summaryRect = CGRectMake(margin, currentY, pageWidth - (margin * 2), 70.0)
        lightGrayBg.setFill()
        UIBezierPath.bezierPathWithRoundedRect(summaryRect, 12.0).fill()

        drawText("TOTAL KILOS", margin + 25.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("LOTES", margin + 210.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("MATERIALES", margin + 430.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)

        drawText("${formatWeight(totalKilos)} kg", margin + 25.0, currentY + 38.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${ventas.size}", margin + 210.0, currentY + 38.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${desgloseMateriales.size}", margin + 430.0, currentY + 38.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)

        currentY += 110.0

        // --- DESGLOSE POR MATERIAL (Barras) ---
        drawText("DESGLOSE POR MATERIAL", margin, currentY, UIFont.boldSystemFontOfSize(12.0), darkGrayColor)
        currentY += 25.0

        desgloseMateriales.forEach { (material, kilos) ->
            checkNewPage(40.0)
            val porcentaje = if (totalKilos > 0) (kilos / totalKilos).toFloat() else 0f

            drawText(material, margin, currentY, UIFont.systemFontOfSize(10.0), textPrimaryColor)

            val kilosText = "${formatWeight(kilos)} kg (${(porcentaje * 100).toInt()}%)"
            val kWidth = (kilosText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.systemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
            drawText(kilosText, pageWidth - margin - kWidth, currentY, UIFont.systemFontOfSize(10.0), textPrimaryColor)

            currentY += 12.0
            val barWidth = pageWidth - (margin * 2)
            UIColor.colorWithWhite(0.92, 1.0).setFill()
            UIRectFill(CGRectMake(margin, currentY, barWidth, 5.0))

            primaryColor.setFill()
            UIRectFill(CGRectMake(margin, currentY, barWidth * porcentaje.toDouble(), 5.0))
            currentY += 25.0
        }

        currentY += 15.0

        // --- TABLA DETALLADA ---
        checkNewPage(60.0)
        drawText("DETALLE DE VENTAS", margin, currentY, UIFont.boldSystemFontOfSize(12.0), UIColor.blackColor)
        currentY += 20.0

        // Cabeceras
        drawText("FECHA / LOTE", margin + 5.0, currentY, UIFont.boldSystemFontOfSize(9.0), UIColor.grayColor)
        drawText("MATERIAL", margin + 180.0, currentY, UIFont.boldSystemFontOfSize(9.0), UIColor.grayColor)

        val hPeso = "PESO TOTAL"
        val hpWidth = (hPeso as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(9.0)) as Map<Any?, *>).useContents { width }
        drawText(hPeso, pageWidth - margin - hpWidth - 5.0, currentY, UIFont.boldSystemFontOfSize(9.0), UIColor.grayColor)

        currentY += 12.0
        val context = UIGraphicsGetCurrentContext()
        CGContextSetStrokeColorWithColor(context, UIColor.lightGrayColor.CGColor)
        CGContextSetLineWidth(context, 0.5)
        CGContextMoveToPoint(context, margin, currentY)
        CGContextAddLineToPoint(context, pageWidth - margin, currentY)
        CGContextStrokePath(context)
        currentY += 15.0

        // Filas de ventas
        ventas.sortedByDescending { it.ventaFecha }.forEachIndexed { index, venta ->
            checkNewPage(45.0)

            if (index % 2 != 0) {
                val rowRect = CGRectMake(margin, currentY - 10.0, pageWidth - (margin * 2), 32.0)
                UIColor.colorWithWhite(0.98, 1.0).setFill()
                UIRectFill(rowRect)
            }

            val fecha = venta.ventaFecha?.toLocalDateTime(TimeZone.currentSystemDefault())?.date
            val fechaStr = if (fecha != null) "${fecha.dayOfMonth.toString().padStart(2,'0')}/${fecha.monthNumber.toString().padStart(2,'0')}/${fecha.year}" else "---"

            // Fecha (Negrita)
            drawText(fechaStr, margin + 5.0, currentY, UIFont.boldSystemFontOfSize(10.0), textPrimaryColor)
            // Lote (Debajo, Gris)
            drawText("Lote: ${venta.ventaLote}", margin + 5.0, currentY + 12.0, UIFont.systemFontOfSize(9.0), UIColor.grayColor)

            // Material (Centrado relativo)
            drawText(venta.ventaMaterial?.take(25) ?: "General", margin + 180.0, currentY + 6.0, UIFont.systemFontOfSize(10.0), textPrimaryColor)

            // Peso (Derecha, Negrita)
            val pText = "${formatWeight(venta.ventaPesoTotal?.toDoubleOrNull() ?: 0.0)} kg"
            val pWidth = (pText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
            drawText(pText, pageWidth - margin - pWidth - 5.0, currentY + 6.0, UIFont.boldSystemFontOfSize(10.0), textPrimaryColor)

            currentY += 35.0
        }

        UIGraphicsEndPDFContext()
        showShareSheet(filePath)
    }

    // ==========================================
    // 4. GENERAR INFORME DE STOCK (iOS)
    // ==========================================
    actual fun generateStockReportPdf(
        materialGroups: List<com.alius.gmrstockplus.domain.model.MaterialGroup>,
        totalKilos: Double
    ) {
        // Nombre de archivo con marca de tiempo para evitar duplicados
        val fileName = "Stock_Materiales_${Clock.System.now().toEpochMilliseconds()}.pdf"
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = cacheDirectory + "/" + fileName

        val pageWidth = 595.2
        val pageHeight = 841.8
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)
        val margin = 45.0

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        var currentY = 60.0

        fun checkNewPage(needed: Double) {
            if (currentY + needed > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }
        }

        // --- CABECERA ---
        drawText("MATERIALES EN STOCK", margin, currentY, UIFont.boldSystemFontOfSize(22.0), darkGrayColor)

        val logoText = "GMR Stock"
        val logoWidth = (logoText as NSString).sizeWithAttributes(
            mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(22.0)) as Map<Any?, *>
        ).useContents { width }
        drawText(logoText, pageWidth - margin - logoWidth, currentY, UIFont.boldSystemFontOfSize(22.0), primaryColor)

        currentY += 22.0
        val fechaActual = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        drawText("Fecha de informe: ${fechaActual.dayOfMonth}/${fechaActual.monthNumber}/${fechaActual.year}", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)
        currentY += 45.0

        // --- CUADRO RESUMEN (KPIs) - COLUMNAS AJUSTADAS ---
        val summaryRect = CGRectMake(margin, currentY, pageWidth - (margin * 2), 75.0)
        lightGrayBg.setFill()
        UIBezierPath.bezierPathWithRoundedRect(summaryRect, 12.0).fill()

        // Etiquetas (X: 45+25, 45+180, 45+415 para llegar a 460)
        drawText("TOTAL KILOS", margin + 25.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("MATERIALES", margin + 210.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("LOTES", margin + 460.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)

        // Valores
        drawText("${formatWeight(totalKilos)} kg", margin + 25.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${materialGroups.size}", margin + 210.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${materialGroups.sumOf { it.totalLotes }}", margin + 460.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)

        currentY += 115.0

        // --- TABLA DETALLADA ---
        drawText("DETALLE DE STOCK", margin + 10.0, currentY, UIFont.boldSystemFontOfSize(12.0), UIColor.blackColor)
        currentY += 20.0

        // Cabeceras
        drawText("MATERIALES", margin + 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)
        drawText("LOTES / BB", margin + 300.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)

        val labelPeso = "PESO TOTAL"
        val lpWidth = (labelPeso as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
        drawText(labelPeso, pageWidth - margin - lpWidth - 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)

        currentY += 15.0
        val context = UIGraphicsGetCurrentContext()
        CGContextSetStrokeColorWithColor(context, UIColor.lightGrayColor.CGColor)
        CGContextSetLineWidth(context, 1.0)
        CGContextMoveToPoint(context, margin, currentY)
        CGContextAddLineToPoint(context, pageWidth - margin, currentY)
        CGContextStrokePath(context)
        currentY += 20.0

        // Filas de materiales
        materialGroups.sortedBy { it.description }.forEachIndexed { index, group ->
            checkNewPage(35.0)

            // Fila cebreada (fondo gris claro para filas impares)
            if (index % 2 != 0) {
                val rowRect = CGRectMake(margin, currentY - 8.0, pageWidth - (margin * 2), 28.0)
                UIColor.colorWithWhite(0.98, 1.0).setFill()
                UIRectFill(rowRect)
            }

            // Descripción (con truncado si es muy largo)
            val desc = if (group.description.length > 45) group.description.take(42) + "..." else group.description
            drawText(desc, margin + 10.0, currentY, UIFont.systemFontOfSize(10.0), textPrimaryColor)

            // Info Lotes / BB
            drawText("${group.totalLotes} Lotes / ${group.totalBigBags} BB", margin + 300.0, currentY, UIFont.systemFontOfSize(10.0), UIColor.grayColor)

            // Peso (Negrita a la derecha)
            val pesoGroup = group.totalWeight.toDoubleOrNull() ?: 0.0
            val pesoText = "${formatWeight(pesoGroup)} kg"
            val pWidth = (pesoText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
            drawText(pesoText, pageWidth - margin - pWidth - 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), textPrimaryColor)

            currentY += 30.0
        }

        UIGraphicsEndPDFContext()
        // Llamada a la función de compartir nativa de iOS
        showShareSheet(filePath)
    }

    // ==========================================
    // 5. GENERAR INFORME VERTISOL (iOS) - DESGLOSADO POR FECHA
    // ==========================================
    actual fun generateVertisolReportPdf(
        vertisolList: List<com.alius.gmrstockplus.domain.model.Vertisol>,
        totalKilos: Double
    ) {
        val fileName = "Reporte_Vertisol_${Clock.System.now().toEpochMilliseconds()}.pdf"
        val paths = NSSearchPathForDirectoriesInDomains(NSCachesDirectory, NSUserDomainMask, true)
        val cacheDirectory = paths.first() as String
        val filePath = cacheDirectory + "/" + fileName

        val pageWidth = 595.2
        val pageHeight = 841.8
        val pageRect = CGRectMake(0.0, 0.0, pageWidth, pageHeight)
        val margin = 45.0

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

        UIGraphicsBeginPDFContextToFile(filePath, pageRect, null)
        UIGraphicsBeginPDFPageWithInfo(pageRect, null)

        var currentY = 60.0

        fun checkNewPage(needed: Double) {
            if (currentY + needed > pageHeight - 60.0) {
                UIGraphicsBeginPDFPageWithInfo(pageRect, null)
                currentY = 60.0
            }
        }

        // --- CABECERA ---
        drawText("LOTES EN VERTISOL", margin, currentY, UIFont.boldSystemFontOfSize(22.0), darkGrayColor)

        val logoText = "GMR Stock"
        val logoWidth = (logoText as NSString).sizeWithAttributes(
            mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(22.0)) as Map<Any?, *>
        ).useContents { width }
        drawText(logoText, pageWidth - margin - logoWidth, currentY, UIFont.boldSystemFontOfSize(22.0), primaryColor)

        currentY += 22.0
        val fechaActual = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
        drawText("Generado el: ${fechaActual.dayOfMonth}/${fechaActual.monthNumber}/${fechaActual.year}", margin, currentY, UIFont.systemFontOfSize(11.0), UIColor.grayColor)
        currentY += 45.0

        // --- CUADRO RESUMEN (KPIs) - AJUSTADO SEGÚN ANDROID ---
        val summaryRect = CGRectMake(margin, currentY, pageWidth - (margin * 2), 75.0)
        lightGrayBg.setFill()
        UIBezierPath.bezierPathWithRoundedRect(summaryRect, 12.0).fill()

        // Etiquetas (Nuevas posiciones: Lotes +250f, BigBags +430f)
        drawText("TOTAL KILOS", margin + 25.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("LOTES", margin + 250.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)
        drawText("BIGBAGS", margin + 430.0, currentY + 15.0, UIFont.boldSystemFontOfSize(9.0), textPrimaryColor)

        // Valores
        drawText("${formatWeight(totalKilos)} kg", margin + 25.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)
        drawText("${vertisolList.size}", margin + 250.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)

        val totalBB = vertisolList.sumOf { it.vertisolBigBag.size }
        drawText("$totalBB", margin + 430.0, currentY + 40.0, UIFont.boldSystemFontOfSize(17.0), primaryColor)

        currentY += 115.0

        // --- TABLA DETALLADA ---
        drawText("REGISTROS DE TRASVASE", margin, currentY, UIFont.boldSystemFontOfSize(12.0), UIColor.blackColor)
        currentY += 20.0

        // Cabeceras (Estado cambiado a Trasvase)
        drawText("LOTE / MATERIAL", margin + 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)
        drawText("TRASVASE", margin + 260.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)
        drawText("BB", margin + 370.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)

        val labelPeso = "PESO TOTAL"
        val lpWidth = (labelPeso as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
        drawText(labelPeso, pageWidth - margin - lpWidth - 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), UIColor.grayColor)

        currentY += 15.0
        val context = UIGraphicsGetCurrentContext()
        CGContextSetStrokeColorWithColor(context, UIColor.lightGrayColor.CGColor)
        CGContextSetLineWidth(context, 1.0)
        CGContextMoveToPoint(context, margin, currentY)
        CGContextAddLineToPoint(context, pageWidth - margin, currentY)
        CGContextStrokePath(context)
        currentY += 20.0

        // --- FILAS DESGLOSADAS ---
        filasDesglosadas.forEachIndexed { index, fila ->
            checkNewPage(45.0)

            if (index % 2 != 0) {
                val rowRect = CGRectMake(margin, currentY - 10.0, pageWidth - (margin * 2), 38.0)
                UIColor.colorWithWhite(0.98, 1.0).setFill()
                UIRectFill(rowRect)
            }

            // Lote (Negrita)
            drawText(fila.numeroLote, margin + 10.0, currentY, UIFont.boldSystemFontOfSize(10.0), textPrimaryColor)

            // Material (Gris pequeño debajo)
            val desc = if (fila.descripcion.length > 35) fila.descripcion.take(32) + "..." else fila.descripcion
            drawText(desc, margin + 10.0, currentY + 12.0, UIFont.systemFontOfSize(8.5), UIColor.grayColor)

            // Fecha de Trasvase (En lugar de Estado)
            drawText(fila.fechaTrasvase, margin + 260.0, currentY + 6.0, UIFont.systemFontOfSize(10.0), textPrimaryColor)

            // Cantidad BigBags del desglose
            drawText("${fila.cantidadBB} BB", margin + 370.0, currentY + 6.0, UIFont.systemFontOfSize(10.0), textPrimaryColor)

            // Peso Parcial del desglose
            val pText = "${formatWeight(fila.peso)} kg"
            val pWidth = (pText as NSString).sizeWithAttributes(mapOf(NSFontAttributeName to UIFont.boldSystemFontOfSize(10.0)) as Map<Any?, *>).useContents { width }
            drawText(pText, pageWidth - margin - pWidth - 10.0, currentY + 6.0, UIFont.boldSystemFontOfSize(10.0), textPrimaryColor)

            currentY += 38.0
        }

        UIGraphicsEndPDFContext()
        showShareSheet(filePath)
    }

    @Suppress("UNCHECKED_CAST")
    private fun drawText(text: String, x: Double, y: Double, font: UIFont, color: UIColor) {
        val attributes = mapOf(NSFontAttributeName to font, NSForegroundColorAttributeName to color)
        (text as NSString).drawAtPoint(CGPointMake(x, y), withAttributes = attributes as Map<Any?, *>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun drawTextInRect(text: String, rect: CValue<CGRect>, font: UIFont, color: UIColor, alignment: NSTextAlignment) {
        val para = NSMutableParagraphStyle().apply { setAlignment(alignment) }
        val attributes = mapOf(NSFontAttributeName to font, NSForegroundColorAttributeName to color, NSParagraphStyleAttributeName to para)
        (text as NSString).drawInRect(rect, withAttributes = attributes as Map<Any?, *>)
    }

    private fun showShareSheet(filePath: String) {
        val fileUrl = NSURL.fileURLWithPath(filePath)
        val activityVC = UIActivityViewController(listOf(fileUrl), null)
        val rootVC = UIApplication.sharedApplication.keyWindow?.rootViewController
        rootVC?.presentViewController(activityVC, animated = true, completion = null)
    }
}