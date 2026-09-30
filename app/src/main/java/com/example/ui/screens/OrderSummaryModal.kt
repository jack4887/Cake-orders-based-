package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.CakeOrder
import com.example.data.OrderIngredientUsage
import com.example.ui.components.parseHexColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderSummaryModalSheet(
    order: CakeOrder,
    usages: List<OrderIngredientUsage>,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var copiedFeedback by remember { mutableStateOf(false) }
    val formattedSummaryText = remember(order, usages) {
        order.generateFormattedSummary(usages)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 36.dp)
                .testTag("order_summary_sheet"),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Top Title & Order #
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "COMPREHENSIVE ORDER SUMMARY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Order #${order.id.toString().padStart(4, '0')} — ${order.clientName}",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                StatusBadge(status = order.statusEnum)
            }

            // 1. Customer & Celebration Block
            SummaryBlockCard(title = "Customer & Event Details") {
                SummaryKeyValueRow("Customer Name", order.clientName)
                if (order.clientPhone.isNotBlank()) {
                    SummaryKeyValueRow("Phone", order.clientPhone)
                }
                if (order.clientEmail.isNotBlank()) {
                    SummaryKeyValueRow("Email", order.clientEmail)
                }
                SummaryKeyValueRow("Celebration", order.eventType)
            }

            // 2. Cake Design, Flavor & Size Block
            SummaryBlockCard(title = "Cake Design, Flavor, Size & Serving Count") {
                SummaryKeyValueRow("Cake Design", "${order.designTitle} (${order.stylePresetEnum.label})")
                SummaryKeyValueRow("Sponge Flavor", order.cakeFlavor)
                SummaryKeyValueRow("Filling / Compote", order.fillingFlavor)
                SummaryKeyValueRow("Frosting Finish", order.frostingType)
                SummaryKeyValueRow(
                    "Tier Size & Shape",
                    "${order.tierSizesList.joinToString("\" + ")}\" ${order.shapeEnum.label} (${order.tierHeightInches}\" tall/tier)"
                )
                SummaryKeyValueRow(
                    "Serving Count",
                    "${order.estimatedServings} Servings (${order.cutStyleEnum.label})"
                )
                if (order.inscriptionText.isNotBlank()) {
                    SummaryKeyValueRow("Inscription", "“${order.inscriptionText}”")
                }
                if (order.dietaryTagsList.isNotEmpty()) {
                    SummaryKeyValueRow("Dietary / Allergens", order.dietaryTagsList.joinToString(" • "))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Color Palette",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        order.colorHexList.forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(hex))
                                    .border(0.5.dp, Color.Gray, CircleShape)
                            )
                        }
                    }
                }
                if (order.designNotes.isNotBlank()) {
                    Text(
                        text = "Design Notes: ${order.designNotes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 3. Delivery Date, Time & Location Block
            SummaryBlockCard(title = "Delivery Date, Time & Location") {
                SummaryKeyValueRow("Fulfillment", order.fulfillmentEnum.label)
                SummaryKeyValueRow("Delivery Date", formatOrderDate(order.deliveryDateMillis))
                SummaryKeyValueRow("Time Window", order.deliveryTimeWindow)
                SummaryKeyValueRow("Location / Address", order.deliveryAddress)
                if (order.deliveryNotes.isNotBlank()) {
                    SummaryKeyValueRow("Logistics Notes", order.deliveryNotes)
                }
            }

            // 4. Financial Breakdown: Total Cost, Payment Received, Remaining Balance
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                )
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Financial & Payment Breakdown",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    SummaryKeyValueRow("Total Cake Cost", formatCurrency(order.totalPrice))
                    SummaryKeyValueRow("Deposit Required (50%)", formatCurrency(order.depositRequired))
                    SummaryKeyValueRow(
                        "Payment Received (${order.paymentMethod})",
                        formatCurrency(order.amountPaid)
                    )
                    HorizontalDivider()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Remaining Balance Due",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = formatCurrency(order.balanceDue),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            PaymentStatusBadge(
                                status = order.paymentStatus,
                                balanceDue = order.balanceDue
                            )
                        }
                    }
                }
            }

            // 5. Ingredient Usage Logged for this Order
            if (usages.isNotEmpty()) {
                SummaryBlockCard(title = "Tracked Ingredient Usage (${usages.size} Items)") {
                    usages.forEach { u ->
                        SummaryKeyValueRow(
                            u.ingredientName,
                            "${u.quantityUsed.toInt()} ${u.unit}"
                        )
                    }
                }
            }

            if (copiedFeedback) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Comprehensive Order Summary copied to clipboard!",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Action Buttons: Copy Summary & Share Summary
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(
                            ClipData.newPlainText("Cake Order Summary", formattedSummaryText)
                        )
                        copiedFeedback = true
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_order_summary_button")
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy Summary")
                }

                Button(
                    onClick = {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "Cake Order Summary — ${order.clientName}")
                            putExtra(Intent.EXTRA_TEXT, formattedSummaryText)
                        }
                        val chooser = Intent.createChooser(sendIntent, "Share Cake Order Summary")
                        context.startActivity(chooser)
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_order_summary_button")
                ) {
                    Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Invoice")
                }
            }
        }
    }
}

@Composable
private fun SummaryBlockCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            content()
        }
    }
}

@Composable
private fun SummaryKeyValueRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(0.58f)
        )
    }
}
