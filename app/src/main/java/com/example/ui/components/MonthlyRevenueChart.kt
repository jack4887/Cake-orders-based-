package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.CakeOrder
import com.example.data.MonthlyRevenueCalculator
import com.example.data.MonthlyRevenuePoint
import com.example.ui.screens.formatCurrency
import kotlin.math.roundToInt

enum class RevenueChartStyle(val label: String) {
    AREA_LINE("Area Trend"),
    BAR("Bar Chart")
}

@Composable
fun MonthlyRevenueChartSection(
    orders: List<CakeOrder>,
    modifier: Modifier = Modifier
) {
    val monthlyPoints = remember(orders) {
        MonthlyRevenueCalculator.buildMonthlyTrends(orders, monthCount = 6)
    }

    var chartStyle by remember { mutableStateOf(RevenueChartStyle.AREA_LINE) }
    var showPipelineOverlay by remember { mutableStateOf(true) }
    var selectedMonthIndex by remember(monthlyPoints) {
        mutableIntStateOf((monthlyPoints.size - 1).coerceAtLeast(0))
    }

    val totalCompletedRevenue = remember(monthlyPoints) {
        monthlyPoints.sumOf { it.completedRevenue }
    }
    val totalCompletedCount = remember(monthlyPoints) {
        monthlyPoints.sumOf { it.completedOrdersCount }
    }
    val averageMonthlyCompleted = remember(monthlyPoints) {
        if (monthlyPoints.isEmpty()) 0.0 else totalCompletedRevenue / monthlyPoints.size
    }
    val peakMonth = remember(monthlyPoints) {
        monthlyPoints.maxByOrNull { it.completedRevenue }
    }
    val momGrowthPercent = remember(monthlyPoints) {
        if (monthlyPoints.size >= 2) {
            val prev = monthlyPoints[monthlyPoints.size - 2].completedRevenue
            val curr = monthlyPoints.last().completedRevenue
            if (prev > 0.0) (((curr - prev) / prev) * 100.0).roundToInt() else 0
        } else 0
    }

    val selectedPoint = monthlyPoints.getOrNull(selectedMonthIndex) ?: monthlyPoints.lastOrNull()

    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("monthly_revenue_chart_section"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. KPI Summary Strip for Completed Cake Orders (Pure White Card with Crisp Border)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "6-Month Completed Cake Revenue",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(50),
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        val sign = if (momGrowthPercent >= 0) "+" else ""
                        Text(
                            text = "${sign}${momGrowthPercent}% vs Last Mo",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    RevenueKpiBlock(
                        label = "Completed Total",
                        value = formatCurrency(totalCompletedRevenue),
                        subtext = "$totalCompletedCount cakes delivered"
                    )
                    RevenueKpiBlock(
                        label = "Monthly Avg",
                        value = formatCurrency(averageMonthlyCompleted),
                        subtext = "6-month mean"
                    )
                    RevenueKpiBlock(
                        label = "Peak Month",
                        value = peakMonth?.let { formatCurrency(it.completedRevenue) } ?: "$0",
                        subtext = peakMonth?.fullMonthLabel ?: "-"
                    )
                }
            }
        }

        // 2. Interactive Recharts-style Chart Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Monthly Revenue Trends",
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Tap any month on the chart to inspect completed order revenue.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Chart Controls: Area vs Bar + Pipeline Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = chartStyle == RevenueChartStyle.AREA_LINE,
                        onClick = { chartStyle = RevenueChartStyle.AREA_LINE },
                        label = { Text("Area Trend") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ShowChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("chart_mode_area_chip")
                    )
                    FilterChip(
                        selected = chartStyle == RevenueChartStyle.BAR,
                        onClick = { chartStyle = RevenueChartStyle.BAR },
                        label = { Text("Bar Chart") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.BarChart,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        modifier = Modifier.testTag("chart_mode_bar_chip")
                    )
                    FilterChip(
                        selected = showPipelineOverlay,
                        onClick = { showPipelineOverlay = !showPipelineOverlay },
                        label = { Text("Include Pipeline") },
                        modifier = Modifier.testTag("chart_toggle_pipeline_chip")
                    )
                }

                // Selected Month Interactive Tooltip Banner
                if (selectedPoint != null) {
                    InteractiveRevenueTooltip(point = selectedPoint, showPipeline = showPipelineOverlay)
                }

                // Chart Canvas with Y-Axis Labels + Interactive Plot
                val maxRevenueValue = remember(monthlyPoints, showPipelineOverlay) {
                    val rawMax = monthlyPoints.maxOfOrNull {
                        if (showPipelineOverlay) {
                            maxOf(it.completedRevenue, it.pipelineRevenue)
                        } else {
                            it.completedRevenue
                        }
                    } ?: 1000.0
                    ((rawMax / 250.0).toInt() + 1).coerceAtLeast(4) * 250.0
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(210.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Y-Axis Scale Column
                    Column(
                        modifier = Modifier
                            .height(180.dp)
                            .padding(end = 6.dp),
                        verticalArrangement = Arrangement.SpaceBetween,
                        horizontalAlignment = Alignment.End
                    ) {
                        listOf(1.0, 0.75, 0.5, 0.25, 0.0).forEach { fraction ->
                            Text(
                                text = formatCurrency(maxRevenueValue * fraction),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Plot Canvas
                    val primaryColor = MaterialTheme.colorScheme.primary
                    val secondaryColor = MaterialTheme.colorScheme.secondary
                    val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                    val highlightColor = MaterialTheme.colorScheme.tertiary

                    Column(modifier = Modifier.weight(1f)) {
                        Canvas(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .testTag("monthly_revenue_canvas")
                                .pointerInput(monthlyPoints.size) {
                                    detectTapGestures { tapOffset ->
                                        if (monthlyPoints.isNotEmpty() && size.width > 0) {
                                            val slotWidth = size.width.toFloat() / monthlyPoints.size
                                            val index = (tapOffset.x / slotWidth).toInt()
                                                .coerceIn(0, monthlyPoints.lastIndex)
                                            selectedMonthIndex = index
                                        }
                                    }
                                }
                        ) {
                            val count = monthlyPoints.size.coerceAtLeast(1)
                            val slotWidth = size.width / count
                            val chartHeight = size.height - 12f
                            val topPadding = 8f
                            val usableHeight = (chartHeight - topPadding).coerceAtLeast(1f)
                            val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)

                            // 1. Horizontal Grid Lines (5 levels)
                            for (g in 0..4) {
                                val y = topPadding + usableHeight * (g / 4f)
                                drawLine(
                                    color = gridColor,
                                    start = Offset(0f, y),
                                    end = Offset(size.width, y),
                                    strokeWidth = 1.5f,
                                    pathEffect = if (g < 4) dashEffect else null
                                )
                            }

                            // 2. Selected Month Vertical Crosshair Line
                            if (selectedMonthIndex in monthlyPoints.indices) {
                                val selectedX = slotWidth * selectedMonthIndex + slotWidth / 2f
                                drawLine(
                                    color = highlightColor.copy(alpha = 0.55f),
                                    start = Offset(selectedX, topPadding),
                                    end = Offset(selectedX, chartHeight),
                                    strokeWidth = 2.5f,
                                    pathEffect = dashEffect
                                )
                            }

                            if (chartStyle == RevenueChartStyle.AREA_LINE) {
                                // Compute Completed Revenue coordinates
                                val completedOffsets = monthlyPoints.mapIndexed { idx, pt ->
                                    val x = slotWidth * idx + slotWidth / 2f
                                    val ratio = (pt.completedRevenue / maxRevenueValue).toFloat().coerceIn(0f, 1f)
                                    val y = chartHeight - ratio * usableHeight
                                    Offset(x, y)
                                }

                                // Optional Pipeline Line
                                if (showPipelineOverlay && monthlyPoints.any { it.pipelineRevenue > 0 }) {
                                    val pipelineOffsets = monthlyPoints.mapIndexed { idx, pt ->
                                        val x = slotWidth * idx + slotWidth / 2f
                                        val ratio = (pt.pipelineRevenue / maxRevenueValue).toFloat().coerceIn(0f, 1f)
                                        val y = chartHeight - ratio * usableHeight
                                        Offset(x, y)
                                    }
                                    val pipePath = Path().apply {
                                        pipelineOffsets.forEachIndexed { i, offset ->
                                            if (i == 0) moveTo(offset.x, offset.y)
                                            else lineTo(offset.x, offset.y)
                                        }
                                    }
                                    drawPath(
                                        path = pipePath,
                                        color = secondaryColor,
                                        style = Stroke(
                                            width = 4f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round,
                                            pathEffect = dashEffect
                                        )
                                    )
                                    pipelineOffsets.forEach { ptOffset ->
                                        drawCircle(
                                            color = secondaryColor,
                                            radius = 5f,
                                            center = ptOffset
                                        )
                                    }
                                }

                                // Completed Revenue Smooth Area + Line
                                if (completedOffsets.isNotEmpty()) {
                                    val areaPath = Path().apply {
                                        moveTo(completedOffsets.first().x, chartHeight)
                                        completedOffsets.forEach { pt ->
                                            lineTo(pt.x, pt.y)
                                        }
                                        lineTo(completedOffsets.last().x, chartHeight)
                                        close()
                                    }
                                    drawPath(
                                        path = areaPath,
                                        brush = Brush.verticalGradient(
                                            colors = listOf(
                                                primaryColor.copy(alpha = 0.38f),
                                                primaryColor.copy(alpha = 0.04f)
                                            ),
                                            startY = topPadding,
                                            endY = chartHeight
                                        )
                                    )

                                    val linePath = Path().apply {
                                        completedOffsets.forEachIndexed { i, pt ->
                                            if (i == 0) moveTo(pt.x, pt.y)
                                            else lineTo(pt.x, pt.y)
                                        }
                                    }
                                    drawPath(
                                        path = linePath,
                                        color = primaryColor,
                                        style = Stroke(
                                            width = 6f,
                                            cap = StrokeCap.Round,
                                            join = StrokeJoin.Round
                                        )
                                    )

                                    completedOffsets.forEachIndexed { idx, ptOffset ->
                                        val isSelected = idx == selectedMonthIndex
                                        if (isSelected) {
                                            drawCircle(
                                                color = primaryColor.copy(alpha = 0.25f),
                                                radius = 14f,
                                                center = ptOffset
                                            )
                                        }
                                        drawCircle(
                                            color = Color.White,
                                            radius = if (isSelected) 8f else 6f,
                                            center = ptOffset
                                        )
                                        drawCircle(
                                            color = primaryColor,
                                            radius = if (isSelected) 5.5f else 4f,
                                            center = ptOffset
                                        )
                                    }
                                }
                            } else {
                                // Grouped Bar Chart Mode
                                monthlyPoints.forEachIndexed { idx, pt ->
                                    val centerX = slotWidth * idx + slotWidth / 2f
                                    val barWidth = (slotWidth * if (showPipelineOverlay) 0.28f else 0.45f)
                                        .coerceAtMost(42f)

                                    val completedRatio = (pt.completedRevenue / maxRevenueValue)
                                        .toFloat().coerceIn(0f, 1f)
                                    val completedBarH = (completedRatio * usableHeight).coerceAtLeast(4f)
                                    val completedLeft = if (showPipelineOverlay) {
                                        centerX - barWidth - 2f
                                    } else {
                                        centerX - barWidth / 2f
                                    }

                                    drawRoundRect(
                                        color = if (idx == selectedMonthIndex) primaryColor else primaryColor.copy(alpha = 0.8f),
                                        topLeft = Offset(completedLeft, chartHeight - completedBarH),
                                        size = Size(barWidth, completedBarH),
                                        cornerRadius = CornerRadius(6f, 6f)
                                    )

                                    if (showPipelineOverlay && pt.pipelineRevenue > 0) {
                                        val pipeRatio = (pt.pipelineRevenue / maxRevenueValue)
                                            .toFloat().coerceIn(0f, 1f)
                                        val pipeBarH = (pipeRatio * usableHeight).coerceAtLeast(4f)
                                        drawRoundRect(
                                            color = secondaryColor.copy(alpha = 0.75f),
                                            topLeft = Offset(centerX + 2f, chartHeight - pipeBarH),
                                            size = Size(barWidth, pipeBarH),
                                            cornerRadius = CornerRadius(6f, 6f)
                                        )
                                    }
                                }
                            }
                        }

                        // X-Axis Month Labels Row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 4.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            monthlyPoints.forEachIndexed { idx, pt ->
                                val isSelected = idx == selectedMonthIndex
                                Text(
                                    text = pt.monthLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) {
                                        MaterialTheme.colorScheme.primary
                                    } else {
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                    },
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedMonthIndex = idx }
                                )
                            }
                        }
                    }
                }

                // Legend Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    LegendDotItem(
                        color = MaterialTheme.colorScheme.primary,
                        label = "Completed Cake Orders Revenue"
                    )
                    if (showPipelineOverlay) {
                        LegendDotItem(
                            color = MaterialTheme.colorScheme.secondary,
                            label = "Active Pipeline Orders"
                        )
                    }
                }
            }
        }

        // 3. Monthly Breakdown Table
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Monthly Completed Revenue Breakdown",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                monthlyPoints.reversed().forEachIndexed { revIdx, pt ->
                    val actualIdx = monthlyPoints.lastIndex - revIdx
                    val isSelected = actualIdx == selectedMonthIndex
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.65f)
                        } else {
                            MaterialTheme.colorScheme.surface
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedMonthIndex = actualIdx }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = pt.fullMonthLabel,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${pt.completedOrdersCount} completed cakes • ${pt.completedServings} servings",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatCurrency(pt.completedRevenue),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (pt.pipelineRevenue > 0) {
                                    Text(
                                        text = "+${formatCurrency(pt.pipelineRevenue)} pipeline",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.secondary
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
private fun InteractiveRevenueTooltip(
    point: MonthlyRevenuePoint,
    showPipeline: Boolean
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("revenue_chart_tooltip"),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = point.fullMonthLabel.uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${point.completedOrdersCount} Completed Orders • ${point.completedServings} Servings",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${formatCurrency(point.completedRevenue)} Completed",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                if (showPipeline && point.pipelineRevenue > 0) {
                    Text(
                        text = "${formatCurrency(point.pipelineRevenue)} Active Pipeline",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}

@Composable
private fun RevenueKpiBlock(
    label: String,
    value: String,
    subtext: String
) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = subtext,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun LegendDotItem(
    color: Color,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
