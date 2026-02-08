package com.alius.gmrstockplus.core.utils

import com.alius.gmrstockplus.domain.model.Comanda
import com.alius.gmrstockplus.domain.model.Ratio
import com.alius.gmrstockplus.domain.model.Venta
import com.alius.gmrstockplus.domain.model.MaterialGroup
import com.alius.gmrstockplus.domain.model.Vertisol

expect object PdfGenerator {

    // 1. Nuevo reporte de Stock
    fun generateStockReportPdf(
        materialGroups: List<MaterialGroup>,
        totalKilos: Double
    )

    // 2. Reporte de Producción
    fun generateProductionReportPdf(
        ratios: List<Ratio>,
        totalKilos: Double,
        promedio: Double,
        dateRange: String,
        loteNombresMap: Map<String, String>
    )

    // 3. Planning de Comandas
    fun generatePlanningPdf(
        comandas: List<Comanda>,
        title: String,
        dateRange: String
    )

    // 4. Reporte de Ventas por Cliente
    fun generateVentasReportPdf(
        clienteNombre: String,
        ventas: List<Venta>,
        totalKilos: Double,
        dateRange: String,
        desgloseMateriales: Map<String, Double>
    )

    // 5. NUEVO: Reporte de Lotes en Vertisol
    fun generateVertisolReportPdf(
        vertisolList: List<Vertisol>,
        totalKilos: Double
    )
}