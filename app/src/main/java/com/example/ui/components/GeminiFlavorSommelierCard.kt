package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.FlavorPairingSuggestion
import com.example.data.FlavorSommelierUiState
import com.example.data.IngredientItem

@Composable
fun GeminiFlavorSommelierCard(
    sommelierState: FlavorSommelierUiState,
    eventType: String,
    dietaryTags: List<String>,
    pantryIngredients: List<IngredientItem>,
    onRequestSuggestions: (String, String, List<String>) -> Unit,
    onApplySuggestion: (FlavorPairingSuggestion) -> Unit,
    applyButtonLabel: String = "Apply Flavor Combination",
    onOpenSecurityGuide: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var userPrompt by rememberSaveable {
        mutableStateOf("Autumn garden wedding, honey, figs, brown butter, citrus")
    }

    val quickThemePrompts = listOf(
        "Raspberry, Rosewater & Pistachio",
        "Dark Chocolate, Espresso & Salted Caramel",
        "Meyer Lemon, Elderflower & Thyme",
        "Matcha, Yuzu & White Chocolate",
        "Spiced Chai, Pear & Brown Butter"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("gemini_flavor_sommelier_card"),
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
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Column {
                        Text(
                            text = "Pastry Pairing Guide",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Complementary Cake Flavor Pairings",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (onOpenSecurityGuide != null) {
                    AssistChip(
                        onClick = onOpenSecurityGuide,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.VerifiedUser,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = {
                            Text(
                                text = "Play & Security",
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        modifier = Modifier.testTag("sommelier_open_security_guide_chip")
                    )
                }
            }

            Text(
                text = "Enter ingredients, seasonal fruits, or celebration themes to generate balanced sponge, filling, and frosting combinations.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedTextField(
                value = userPrompt,
                onValueChange = { userPrompt = it },
                label = { Text("Ingredients, Notes, or Event Theme") },
                placeholder = { Text("e.g., Blood orange, dark chocolate, winter gala…") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_flavor_prompt_input")
            )

            // Quick Inspiration & Pantry Ingredient Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                quickThemePrompts.forEach { preset ->
                    AssistChip(
                        onClick = { userPrompt = preset },
                        label = { Text(preset, style = MaterialTheme.typography.labelSmall) }
                    )
                }
                pantryIngredients.take(4).forEach { item ->
                    AssistChip(
                        onClick = {
                            userPrompt = if (userPrompt.isBlank()) {
                                item.name
                            } else {
                                "$userPrompt, ${item.name}"
                            }
                        },
                        label = {
                            Text("+ ${item.name}", style = MaterialTheme.typography.labelSmall)
                        }
                    )
                }
            }

            Button(
                onClick = {
                    val clean = userPrompt.trim().ifEmpty { "$eventType celebration cake" }
                    onRequestSuggestions(clean, eventType, dietaryTags)
                },
                enabled = sommelierState !is FlavorSommelierUiState.Loading,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("gemini_suggest_flavors_button")
            ) {
                if (sommelierState is FlavorSommelierUiState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Crafting Pâtisserie Pairings…")
                } else {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Suggest Complementary Flavors")
                }
            }

            when (sommelierState) {
                is FlavorSommelierUiState.Idle -> Unit

                is FlavorSommelierUiState.Loading -> Unit

                is FlavorSommelierUiState.Error -> {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.errorContainer,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("gemini_flavor_error_banner")
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onErrorContainer
                            )
                            Text(
                                text = sommelierState.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }

                is FlavorSommelierUiState.Success -> {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.testTag("gemini_flavor_results_list")
                    ) {
                        Text(
                            text = "Suggested Pairings for “${sommelierState.promptSummary}”",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )

                        sommelierState.suggestions.forEachIndexed { index, suggestion ->
                            FlavorSuggestionItemCard(
                                suggestion = suggestion,
                                index = index,
                                applyButtonLabel = applyButtonLabel,
                                onApply = { onApplySuggestion(suggestion) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FlavorSuggestionItemCard(
    suggestion: FlavorPairingSuggestion,
    index: Int,
    applyButtonLabel: String,
    onApply: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("flavor_suggestion_card_$index"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = suggestion.pairingTitle,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    suggestion.recommendedColorsHex.take(3).forEach { hex ->
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(parseHexColor(hex))
                                .border(0.5.dp, Color.Gray, CircleShape)
                        )
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))

            Text(
                text = "• Sponge: ${suggestion.spongeFlavor}\n• Filling: ${suggestion.fillingFlavor}\n• Frosting: ${suggestion.frostingType}",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )

            Text(
                text = suggestion.tastingNotes,
                style = MaterialTheme.typography.bodySmall,
                fontStyle = FontStyle.Italic,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (suggestion.designTip.isNotBlank()) {
                Text(
                    text = "Pastry Tip: ${suggestion.designTip}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("apply_flavor_suggestion_$index")
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(applyButtonLabel)
            }
        }
    }
}
