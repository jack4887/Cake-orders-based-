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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Scale
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CakeShape
import com.example.data.CutStyle
import com.example.data.DesignStylePreset
import com.example.data.PatisserieCatalog
import com.example.data.TierServingCalculator
import com.example.ui.TierStudioState
import com.example.ui.components.PosFilterKeypadButton
import com.example.ui.components.PosTouchButton
import com.example.ui.components.TierBlueprintCanvas
import com.example.ui.components.VoiceDictationNotesCard
import com.example.ui.components.parseHexColor

@Composable
fun TierCalculatorScreen(
    studioState: TierStudioState,
    onSelectShape: (CakeShape) -> Unit,
    onSelectCutStyle: (CutStyle) -> Unit,
    onSelectStylePreset: (DesignStylePreset) -> Unit,
    onUpdateTierHeight: (Int) -> Unit,
    onAddTier: (Int) -> Unit,
    onRemoveTierAt: (Int) -> Unit,
    onApplyPreset: (List<Int>) -> Unit,
    onToggleColorHex: (String) -> Unit,
    onUpdateInscription: (String) -> Unit,
    onUpdateDesignNotes: (String) -> Unit = {},
    onStartOrderFromStudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    val breakdown = studioState.breakdown

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("tier_studio_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "TIER & SERVING ARCHITECT",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Interactive Cake Sizing & Batter Calculator",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Plan stacked tiers, compare Wedding vs. Party slice yields, and compute exact batter, buttercream, and support dowels.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Quick Tier Combination Presets
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val presets = listOf(
                    "1-Tier (8\")" to listOf(8),
                    "2-Tier (8\"+6\")" to listOf(8, 6),
                    "3-Tier (10\"+8\"+6\")" to listOf(10, 8, 6),
                    "4-Tier (12\"+10\"+8\"+6\")" to listOf(12, 10, 8, 6)
                )
                presets.forEach { (label, sizes) ->
                    PosFilterKeypadButton(
                        label = label,
                        selected = studioState.tierSizesInches == sizes,
                        onClick = { onApplyPreset(sizes) }
                    )
                }
            }
        }

        // Live Architectural Tier Blueprint
        item {
            TierBlueprintCanvas(
                tierSizesInches = studioState.tierSizesInches,
                tierHeightInches = studioState.tierHeightInches,
                shape = studioState.selectedShape,
                stylePreset = studioState.selectedStylePreset,
                colorHexes = studioState.selectedColorsHex,
                inscriptionText = studioState.inscriptionPreview
            )
        }

        // Live Summary Metrics Banner (Pure White Card with Crisp Border)
        item {
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
                        Column {
                            Text(
                                text = "${breakdown.totalServings} Total Servings",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${studioState.selectedCutStyle.label} (${studioState.selectedCutStyle.sliceDimensions})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.End
                            ) {
                                Text(
                                    text = formatCurrency(breakdown.suggestedBasePrice),
                                    style = MaterialTheme.typography.titleLarge,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Suggested Quote",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        StudioMetricColumn("Sponge Batter", "${breakdown.totalBatterGrams} g")
                        StudioMetricColumn("Buttercream", "${breakdown.totalButtercreamGrams} g")
                        StudioMetricColumn("Support Dowels", "${breakdown.totalDowels} rods")
                        StudioMetricColumn("Cake Drum", "${breakdown.baseBoardDiameterInches}\" board")
                    }

                    PosTouchButton(
                        text = stringResource(R.string.action_use_tier_setup),
                        icon = Icons.Default.Add,
                        onClick = onStartOrderFromStudio,
                        minHeight = 50.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_order_from_studio_button")
                    )
                }
            }
        }

        // Tier Stack Builder Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
                        Text(
                            text = "Stacked Tiers (${studioState.tierSizesInches.size}/4 Tiers)",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tap + to stack a tier",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Add Tier Size POS Keypad Buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TierServingCalculator.availableSizes.forEach { sizeInch ->
                            PosTouchButton(
                                text = "+ ${sizeInch}\"",
                                onClick = { onAddTier(sizeInch) },
                                enabled = studioState.tierSizesInches.size < 4,
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                                borderColor = MaterialTheme.colorScheme.secondary,
                                minHeight = 48.dp,
                                modifier = Modifier.testTag("add_tier_${sizeInch}_chip")
                            )
                        }
                    }

                    // Per-Tier Breakdown Rows
                    breakdown.tiers.forEachIndexed { index, tierSpec ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Tier ${index + 1} (Bottom-Up): ${tierSpec.diameterInches}\" ${studioState.selectedShape.label}",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${tierSpec.servings} servings • ${tierSpec.batterGrams}g batter • ${tierSpec.buttercreamGrams}g frosting" +
                                            if (tierSpec.dowelsNeeded > 0) " • ${tierSpec.dowelsNeeded} dowels" else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (studioState.tierSizesInches.size > 1) {
                                    IconButton(onClick = { onRemoveTierAt(index) }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tier"
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // Tier Height Slider
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Tier Height (Layers per Tier)",
                                style = MaterialTheme.typography.labelLarge
                            )
                            Text(
                                text = "${studioState.tierHeightInches}\" tall",
                                style = MaterialTheme.typography.labelLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Slider(
                            value = studioState.tierHeightInches.toFloat(),
                            onValueChange = { onUpdateTierHeight(it.toInt()) },
                            valueRange = 3f..6f,
                            steps = 2
                        )
                    }
                }
            }
        }

        // Shape, Cut Style & Frosting Palette Customizer
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Shape & Slice Style",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CakeShape.entries.forEach { shape ->
                            PosFilterKeypadButton(
                                label = shape.label.uppercase(),
                                selected = studioState.selectedShape == shape,
                                onClick = { onSelectShape(shape) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CutStyle.entries.forEach { cut ->
                            PosFilterKeypadButton(
                                label = "${cut.label.uppercase()} (${cut.sliceDimensions})",
                                selected = studioState.selectedCutStyle == cut,
                                onClick = { onSelectCutStyle(cut) },
                                activeColor = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    Text(
                        text = "Piping & Surface Aesthetic",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DesignStylePreset.entries.forEach { preset ->
                            PosFilterKeypadButton(
                                label = preset.label.uppercase(),
                                selected = studioState.selectedStylePreset == preset,
                                onClick = { onSelectStylePreset(preset) }
                            )
                        }
                    }

                    Text(
                        text = "Frosting Color Swatches (Tap up to 4)",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PatisserieCatalog.colorSwatches.forEach { swatch ->
                            val isSelected = studioState.selectedColorsHex.contains(swatch.hex)
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { onToggleColorHex(swatch.hex) }
                                    .padding(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(swatch.color)
                                        .border(
                                            width = if (isSelected) 2.5.dp else 1.dp,
                                            color = if (isSelected) {
                                                MaterialTheme.colorScheme.primary
                                            } else {
                                                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Selected",
                                            tint = Color(0xFF241916),
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = swatch.name.substringBefore(" "),
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = studioState.inscriptionPreview,
                        onValueChange = onUpdateInscription,
                        label = { Text("Topper / Cake Board Inscription Preview") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    VoiceDictationNotesCard(
                        notesText = studioState.studioDesignNotes,
                        onNotesChange = onUpdateDesignNotes,
                        title = "Voice-to-Text Design Sketch & Client Feedback",
                        subtitle = "Dictate design requirements or client consultation notes while planning tiers."
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioMetricColumn(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
        )
    }
}
