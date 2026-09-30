package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CakeShape
import com.example.data.DesignStylePreset

fun parseHexColor(hex: String, fallback: Color = Color(0xFFFFF5E8)): Color {
    return try {
        val clean = hex.removePrefix("#")
        val value = clean.toLong(16)
        if (clean.length == 6) {
            Color(0xFF000000 or value)
        } else {
            Color(value)
        }
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun TierBlueprintCanvas(
    tierSizesInches: List<Int>,
    tierHeightInches: Int,
    shape: CakeShape,
    stylePreset: DesignStylePreset,
    colorHexes: List<String>,
    inscriptionText: String,
    modifier: Modifier = Modifier
) {
    val sortedTiers = tierSizesInches.sortedDescending().ifEmpty { listOf(8) }
    val primaryFrosting = parseHexColor(colorHexes.getOrElse(0) { "#FFF5E8" })
    val accentFrosting = parseHexColor(colorHexes.getOrElse(1) { "#F4C2C2" })
    val detailGold = parseHexColor(colorHexes.getOrElse(2) { "#E6C27A" }, Color(0xFFE6C27A))
    val outlineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.45f)
    val dowelColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.55f)
    val pedestalColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.28f)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        tonalElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Cake Tier Blueprint",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${sortedTiers.joinToString("\" + ")}\" • ${shape.label}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (inscriptionText.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    shape = RoundedCornerShape(50),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                ) {
                    Text(
                        text = "“$inscriptionText”",
                        style = MaterialTheme.typography.bodySmall,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
            }

            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(190.dp)
                    .padding(top = 8.dp)
            ) {
                val maxInch = (sortedTiers.maxOrNull() ?: 10).coerceAtLeast(8)
                val maxTierWidthPx = size.width * 0.68f
                val pxPerInch = maxTierWidthPx / maxInch.toFloat()

                val pedestalBaseY = size.height - 18f
                val pedestalFootWidth = maxTierWidthPx * 0.42f
                val boardWidth = (sortedTiers.first() + 2).coerceAtMost(16) * pxPerInch

                // Draw pedestal stand base
                drawRoundRect(
                    color = pedestalColor,
                    topLeft = Offset((size.width - pedestalFootWidth) / 2f, pedestalBaseY - 10f),
                    size = Size(pedestalFootWidth, 10f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
                drawRoundRect(
                    color = pedestalColor,
                    topLeft = Offset((size.width - boardWidth) / 2f, pedestalBaseY - 18f),
                    size = Size(boardWidth, 8f),
                    cornerRadius = CornerRadius(4f, 4f)
                )

                val availableHeight = pedestalBaseY - 34f
                val singleTierHeightPx = ((availableHeight / sortedTiers.size.coerceAtLeast(1))
                    .coerceAtMost(58f) * (tierHeightInches / 4.5f)).coerceIn(30f, 62f)

                var currentBottomY = pedestalBaseY - 18f

                sortedTiers.forEachIndexed { index, diameterInch ->
                    val tierWidth = diameterInch * pxPerInch
                    val leftX = (size.width - tierWidth) / 2f
                    val topY = currentBottomY - singleTierHeightPx

                    val tierFill = if (index % 2 == 0) primaryFrosting else accentFrosting.copy(alpha = 0.9f)
                    val cornerRadius = when (shape) {
                        CakeShape.SQUARE -> CornerRadius(4f, 4f)
                        CakeShape.ROUND -> CornerRadius(12f, 12f)
                        CakeShape.HEART -> CornerRadius(18f, 18f)
                    }

                    // Tier body
                    drawRoundRect(
                        color = tierFill,
                        topLeft = Offset(leftX, topY),
                        size = Size(tierWidth, singleTierHeightPx),
                        cornerRadius = cornerRadius
                    )

                    // Tier crisp outline
                    drawRoundRect(
                        color = outlineColor,
                        topLeft = Offset(leftX, topY),
                        size = Size(tierWidth, singleTierHeightPx),
                        cornerRadius = cornerRadius,
                        style = Stroke(width = 2.2f)
                    )

                    // Style-specific decorations
                    when (stylePreset) {
                        DesignStylePreset.LAMBETH_VINTAGE -> {
                            // Draw scalloped Lambeth swags across the tier
                            val swagCount = (diameterInch / 2).coerceAtLeast(3)
                            val swagWidth = tierWidth / swagCount
                            val path = Path()
                            for (i in 0 until swagCount) {
                                val startX = leftX + i * swagWidth
                                path.moveTo(startX, topY + singleTierHeightPx * 0.32f)
                                path.quadraticTo(
                                    startX + swagWidth / 2f,
                                    topY + singleTierHeightPx * 0.72f,
                                    startX + swagWidth,
                                    topY + singleTierHeightPx * 0.32f
                                )
                            }
                            drawPath(
                                path = path,
                                color = accentFrosting,
                                style = Stroke(width = 4f)
                            )
                            // Bottom shell border dots
                            val dotCount = (diameterInch * 1.5f).toInt()
                            val step = tierWidth / dotCount
                            for (d in 0..dotCount) {
                                drawCircle(
                                    color = detailGold,
                                    radius = 3.5f,
                                    center = Offset(leftX + d * step, currentBottomY - 4f)
                                )
                            }
                        }

                        DesignStylePreset.BOTANICAL_FLORAL -> {
                            // Pressed floral blooms & sage leaves along diagonal
                            val bloomCount = 3
                            for (b in 0 until bloomCount) {
                                val bx = leftX + tierWidth * (0.25f + b * 0.24f)
                                val by = topY + singleTierHeightPx * (0.35f + (b % 2) * 0.28f)
                                drawCircle(
                                    color = detailGold,
                                    radius = 6f,
                                    center = Offset(bx, by)
                                )
                                drawCircle(
                                    color = accentFrosting,
                                    radius = 3.2f,
                                    center = Offset(bx, by)
                                )
                            }
                        }

                        DesignStylePreset.MODERN_ARCH -> {
                            // Sculpted arch motif on front of tier
                            val archW = tierWidth * 0.34f
                            val archH = singleTierHeightPx * 0.7f
                            drawRoundRect(
                                color = detailGold.copy(alpha = 0.65f),
                                topLeft = Offset((size.width - archW) / 2f, currentBottomY - archH),
                                size = Size(archW, archH),
                                cornerRadius = CornerRadius(archW / 2f, archW / 2f)
                            )
                        }

                        DesignStylePreset.RUSTIC_SEMI_NAKED -> {
                            // Horizontal sponge layer lines visible through semi-naked frosting
                            drawLine(
                                color = outlineColor.copy(alpha = 0.3f),
                                start = Offset(leftX + 8f, topY + singleTierHeightPx * 0.35f),
                                end = Offset(leftX + tierWidth - 8f, topY + singleTierHeightPx * 0.35f),
                                strokeWidth = 2.5f
                            )
                            drawLine(
                                color = outlineColor.copy(alpha = 0.3f),
                                start = Offset(leftX + 8f, topY + singleTierHeightPx * 0.68f),
                                end = Offset(leftX + tierWidth - 8f, topY + singleTierHeightPx * 0.68f),
                                strokeWidth = 2.5f
                            )
                        }
                    }

                    // Internal support dowels if supporting an upper tier
                    if (index < sortedTiers.lastIndex) {
                        val upperWidth = sortedTiers[index + 1] * pxPerInch
                        val upperLeft = (size.width - upperWidth) / 2f
                        val dowelCount = 4
                        val spacing = upperWidth / (dowelCount + 1)
                        val dashEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 5f), 0f)
                        for (d in 1..dowelCount) {
                            val dx = upperLeft + d * spacing
                            drawLine(
                                color = dowelColor,
                                start = Offset(dx, topY + 3f),
                                end = Offset(dx, currentBottomY - 3f),
                                strokeWidth = 2f,
                                pathEffect = dashEffect
                            )
                        }
                    }

                    currentBottomY = topY
                }
            }

            // Legend row: Frosting swatches + Tier sizes
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    colorHexes.take(4).forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(16.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                        )
                    }
                    Text(
                        text = stylePreset.label,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "${tierHeightInches}\" tall/tier",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
