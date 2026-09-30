package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.TrendingUp
import com.example.ui.components.MonthlyRevenueChartSection
import com.example.ui.components.PosFilterKeypadButton
import com.example.ui.components.PosMetricBox
import com.example.ui.components.PosTouchButton
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.CakeOrder
import com.example.data.FulfillmentType
import com.example.data.PatisserieCatalog
import com.example.data.PaymentStatus
import com.example.data.PrepStep
import com.example.ui.CakePlannerUiState

@Composable
fun ScheduleAndPaymentsHubScreen(
    uiState: CakePlannerUiState,
    onTogglePrepStep: (CakeOrder, String) -> Unit,
    onAdvanceStage: (CakeOrder) -> Unit,
    onSelectPaymentFilter: (PaymentStatus?) -> Unit,
    onOpenPaymentDialog: (CakeOrder) -> Unit,
    onOpenOrderDetail: (CakeOrder) -> Unit,
    initialSubTab: Int = 0,
    modifier: Modifier = Modifier
) {
    var selectedSubTab by remember(initialSubTab) { mutableStateOf(initialSubTab) }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PosFilterKeypadButton(
                label = "Timeline",
                selected = selectedSubTab == 0,
                onClick = { selectedSubTab = 0 },
                icon = Icons.Default.Schedule,
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("subtab_schedule_timeline")
            )
            PosFilterKeypadButton(
                label = "Revenue",
                selected = selectedSubTab == 1,
                onClick = { selectedSubTab = 1 },
                icon = Icons.Default.TrendingUp,
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("subtab_revenue_trends")
            )
            PosFilterKeypadButton(
                label = "Payments",
                selected = selectedSubTab == 2,
                onClick = { selectedSubTab = 2 },
                icon = Icons.Default.Payments,
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .weight(1f)
                    .testTag("subtab_payments_ledger")
            )
        }

        when (selectedSubTab) {
            0 -> {
                ScheduleTimelineScreen(
                    uiState = uiState,
                    onTogglePrepStep = onTogglePrepStep,
                    onAdvanceStage = onAdvanceStage,
                    onOpenOrderDetail = onOpenOrderDetail,
                    modifier = Modifier.weight(1f)
                )
            }
            1 -> {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("revenue_trends_screen"),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item {
                        MonthlyRevenueChartSection(orders = uiState.allOrders)
                    }
                }
            }
            else -> {
                PaymentsLedgerScreen(
                    uiState = uiState,
                    onSelectPaymentFilter = onSelectPaymentFilter,
                    onOpenPaymentDialog = onOpenPaymentDialog,
                    onOpenOrderDetail = onOpenOrderDetail,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun ScheduleTimelineScreen(
    uiState: CakePlannerUiState,
    onTogglePrepStep: (CakeOrder, String) -> Unit,
    onAdvanceStage: (CakeOrder) -> Unit,
    onOpenOrderDetail: (CakeOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val sortedOrders = remember(uiState.allOrders) {
        uiState.allOrders.sortedBy { it.deliveryDateMillis }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("schedule_timeline_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "BAKE & DELIVERY TIMELINE",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Production Checklists & Handover Windows",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Track sponge baking, crumb coats, final piping, and courier or studio pickup times.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Logistics Summary Banner
        item {
            val deliveriesCount = sortedOrders.count { it.fulfillmentEnum == FulfillmentType.DELIVERY }
            val pickupsCount = sortedOrders.count { it.fulfillmentEnum == FulfillmentType.PICKUP }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalShipping,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Column {
                            Text(
                                text = "$deliveriesCount Venue Deliveries",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Chilled transport",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Storefront,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Column {
                            Text(
                                text = "$pickupsCount Studio Pickups",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Window cake box",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        items(sortedOrders, key = { it.id }) { order ->
            val completedCount = order.completedPrepSteps.size
            val totalSteps = PrepStep.entries.size
            val prepProgress = completedCount.toFloat() / totalSteps.toFloat()

            Card(
                onClick = { onOpenOrderDetail(order) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = formatOrderDate(order.deliveryDateMillis),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                            Text(
                                text = order.deliveryTimeWindow,
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        StatusBadge(status = order.statusEnum)
                    }

                    Text(
                        text = "${order.designTitle} • ${order.clientName}",
                        style = MaterialTheme.typography.titleLarge
                    )

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${order.fulfillmentEnum.label}: ${order.deliveryAddress}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    if (order.deliveryNotes.isNotBlank()) {
                        Text(
                            text = "Note: ${order.deliveryNotes}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Prep Progress Bar + Interactive Prep Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Kitchen Prep Checklist ($completedCount/$totalSteps Done)",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "${order.estimatedServings} servings (${order.tierSizesList.joinToString("\" + ")}\")",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    LinearProgressIndicator(
                        progress = { prepProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.secondary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PrepStep.entries.forEach { step ->
                            val done = order.completedPrepSteps.contains(step.key)
                            PosFilterKeypadButton(
                                label = step.label,
                                selected = done,
                                onClick = { onTogglePrepStep(order, step.key) },
                                icon = if (done) Icons.Default.CheckCircle else null,
                                activeColor = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentsLedgerScreen(
    uiState: CakePlannerUiState,
    onSelectPaymentFilter: (PaymentStatus?) -> Unit,
    onOpenPaymentDialog: (CakeOrder) -> Unit,
    onOpenOrderDetail: (CakeOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    val filteredByPayment = remember(uiState.allOrders, uiState.selectedPaymentFilter) {
        if (uiState.selectedPaymentFilter == null) {
            uiState.allOrders
        } else {
            uiState.allOrders.filter { it.paymentStatus == uiState.selectedPaymentFilter }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("payments_ledger_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "BAKERY REVENUE & RETAINERS",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Client Payments & Balance Ledger",
                    style = MaterialTheme.typography.headlineMedium
                )
                Text(
                    text = "Track 50% booking retainers, final delivery balances, and payment methods.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Financial Summary Cards (Pure White Surface with Crisp Border)
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
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PosMetricBox(
                            label = "Total Booked",
                            value = formatCurrency(uiState.totalBookedRevenue),
                            modifier = Modifier.weight(1f)
                        )
                        PosMetricBox(
                            label = "Collected",
                            value = formatCurrency(uiState.totalCollectedRevenue),
                            accentColor = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.weight(1f)
                        )
                        PosMetricBox(
                            label = "Balance Due",
                            value = formatCurrency(uiState.totalBalanceDue),
                            accentColor = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    val overallProgress = if (uiState.totalBookedRevenue <= 0.0) 1f
                    else (uiState.totalCollectedRevenue / uiState.totalBookedRevenue).toFloat().coerceIn(0f, 1f)

                    LinearProgressIndicator(
                        progress = { overallProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(50)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }
        }

        // Monthly Revenue Trends Chart inside Payments Ledger
        item {
            MonthlyRevenueChartSection(orders = uiState.allOrders)
        }

        // Filter Buttons by Payment Status
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PosFilterKeypadButton(
                    label = "All (${uiState.allOrders.size})",
                    selected = uiState.selectedPaymentFilter == null,
                    onClick = { onSelectPaymentFilter(null) }
                )
                PaymentStatus.entries.forEach { status ->
                    val count = uiState.allOrders.count { it.paymentStatus == status }
                    PosFilterKeypadButton(
                        label = "${status.label} ($count)",
                        selected = uiState.selectedPaymentFilter == status,
                        onClick = { onSelectPaymentFilter(status) }
                    )
                }
            }
        }

        items(filteredByPayment, key = { it.id }) { order ->
            Card(
                onClick = { onOpenOrderDetail(order) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = order.clientName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${order.designTitle} • Due ${formatOrderDate(order.deliveryDateMillis)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        PaymentStatusBadge(
                            status = order.paymentStatus,
                            balanceDue = order.balanceDue
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Quote: ${formatCurrency(order.totalPrice)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Deposit Req: ${formatCurrency(order.depositRequired)}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Paid: ${formatCurrency(order.amountPaid)} (${order.paymentMethod})",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    if (order.paymentNotes.isNotBlank()) {
                        Text(
                            text = order.paymentNotes,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    PosTouchButton(
                        text = if (order.balanceDue > 0) "RECORD PAYMENT (${formatCurrency(order.balanceDue)} DUE)" else "UPDATE PAYMENT DETAILS",
                        icon = Icons.Default.Payments,
                        onClick = { onOpenPaymentDialog(order) },
                        minHeight = 50.dp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ledger_record_payment_${order.id}")
                    )
                }
            }
        }
    }
}

@Composable
private fun LedgerMetricBlock(title: String, amount: String) {
    Column {
        Text(
            text = amount,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
fun RecordPaymentDialog(
    order: CakeOrder,
    onDismiss: () -> Unit,
    onConfirmPayment: (Double, String, String) -> Unit
) {
    var amountPaidText by remember(order) {
        mutableStateOf(order.amountPaid.toInt().toString())
    }
    var selectedMethod by remember(order) {
        mutableStateOf(order.paymentMethod.ifBlank { "Venmo" })
    }
    var paymentNotes by remember(order) {
        mutableStateOf(order.paymentNotes)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Record Client Payment",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "${order.clientName} — ${order.designTitle}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "Total Order Quote: ${formatCurrency(order.totalPrice)} • Required Deposit: ${formatCurrency(order.depositRequired)}",
                    style = MaterialTheme.typography.bodySmall
                )

                // Quick Preset Chips: 50% Deposit or Full Balance
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistChip(
                        onClick = {
                            amountPaidText = order.depositRequired.toInt().toString()
                            paymentNotes = "50% booking retainer received."
                        },
                        label = { Text("Log Deposit (${formatCurrency(order.depositRequired)})") },
                        modifier = Modifier.testTag("preset_deposit_chip")
                    )
                    AssistChip(
                        onClick = {
                            amountPaidText = order.totalPrice.toInt().toString()
                            paymentNotes = "Paid in full."
                        },
                        label = { Text("Paid in Full (${formatCurrency(order.totalPrice)})") },
                        modifier = Modifier.testTag("preset_paid_full_chip")
                    )
                }

                OutlinedTextField(
                    value = amountPaidText,
                    onValueChange = { amountPaidText = it },
                    label = { Text("Total Amount Paid ($)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_amount_input")
                )

                Text(
                    text = "Payment Method",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PatisserieCatalog.paymentMethods.forEach { method ->
                        FilterChip(
                            selected = selectedMethod == method,
                            onClick = { selectedMethod = method },
                            label = { Text(method) }
                        )
                    }
                }

                OutlinedTextField(
                    value = paymentNotes,
                    onValueChange = { paymentNotes = it },
                    label = { Text("Payment / Invoice Note") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val parsed = amountPaidText.toDoubleOrNull() ?: order.amountPaid
                    onConfirmPayment(parsed, selectedMethod, paymentNotes)
                },
                modifier = Modifier.testTag("confirm_payment_button")
            ) {
                Text("Save Payment")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
