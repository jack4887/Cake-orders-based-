package com.example.ui.screens

import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.RestaurantMenu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.CakeOrder
import com.example.data.DesignStylePreset
import com.example.data.FulfillmentType
import com.example.data.OrderStatus
import com.example.data.PaymentStatus
import com.example.data.PrepStep
import com.example.data.TierServingCalculator
import com.example.ui.CakePlannerUiState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.data.FlavorPairingSuggestion
import com.example.ui.components.GeminiFlavorSommelierCard
import com.example.ui.components.MonthlyRevenueChartSection
import com.example.ui.components.PosFilterKeypadButton
import com.example.ui.components.PosMetricBox
import com.example.ui.components.PosTouchButton
import com.example.ui.components.TierBlueprintCanvas
import com.example.ui.components.VoiceDictationNotesCard
import com.example.ui.components.parseHexColor
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun formatCurrency(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale.US)
    format.maximumFractionDigits = 0
    return format.format(amount)
}

fun formatOrderDate(millis: Long): String {
    val sdf = SimpleDateFormat("EEE, MMM d", Locale.US)
    return sdf.format(Date(millis))
}

@Composable
fun CakeDesignThumbnail(
    order: CakeOrder,
    modifier: Modifier = Modifier
) {
    val drawableRes = when (order.stylePresetEnum) {
        DesignStylePreset.LAMBETH_VINTAGE -> R.drawable.img_cake_lambeth
        DesignStylePreset.BOTANICAL_FLORAL -> R.drawable.img_cake_botanical
        DesignStylePreset.MODERN_ARCH -> R.drawable.img_hero_patisserie
        DesignStylePreset.RUSTIC_SEMI_NAKED -> R.drawable.img_cake_botanical
    }

    if (!order.customPhotoUri.isNullOrBlank()) {
        AsyncImage(
            model = Uri.parse(order.customPhotoUri),
            contentDescription = order.designTitle,
            contentScale = ContentScale.Crop,
            error = painterResource(id = drawableRes),
            placeholder = painterResource(id = drawableRes),
            modifier = modifier
        )
    } else {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = order.designTitle,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrdersBoardScreen(
    uiState: CakePlannerUiState,
    onSearchQueryChange: (String) -> Unit,
    onStatusFilterSelect: (OrderStatus?) -> Unit,
    onOpenNewOrder: () -> Unit,
    onOpenOrderDetail: (CakeOrder) -> Unit,
    onCloseOrderDetail: () -> Unit,
    onEditOrder: (CakeOrder) -> Unit,
    onDeleteOrder: (CakeOrder) -> Unit,
    onAdvanceStage: (CakeOrder) -> Unit,
    onOpenPaymentDialog: (CakeOrder) -> Unit,
    onOpenOrderSummary: (CakeOrder) -> Unit,
    onTogglePrepStep: (CakeOrder, String) -> Unit,
    onRequestFlavorSuggestions: (String, String, List<String>) -> Unit = { _, _, _ -> },
    onStartOrderWithPairing: (FlavorPairingSuggestion) -> Unit = {},
    onUpdateOrderDesignNotes: (CakeOrder, String) -> Unit = { _, _ -> },
    onOpenLaunchGuide: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showRevenueTrendsOnBoard by remember { mutableStateOf(false) }
    var showFlavorSommelierOnBoard by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("orders_board_list"),
        contentPadding = PaddingValues(bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Editorial Hero Banner with overlapping KPI metrics
        item {
            PatisserieHeroHeader(
                activeOrdersCount = uiState.activeOrdersCount,
                totalServings = uiState.totalServingsScheduled,
                balanceDue = uiState.totalBalanceDue,
                onNewOrderClick = onOpenNewOrder,
                onOpenLaunchGuide = onOpenLaunchGuide
            )
        }

        // 1a. Structured POS Quick Tool Keypad (AI Flavor Sommelier & Revenue Chart toggles)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    PosFilterKeypadButton(
                        label = if (showFlavorSommelierOnBoard) "Hide Flavor Studio" else "Flavor Pairing Studio",
                        selected = showFlavorSommelierOnBoard,
                        onClick = { showFlavorSommelierOnBoard = !showFlavorSommelierOnBoard },
                        icon = Icons.Default.AutoAwesome,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_board_flavor_sommelier_chip")
                    )

                    PosFilterKeypadButton(
                        label = if (showRevenueTrendsOnBoard) "Hide Revenue" else "Revenue Trends",
                        selected = showRevenueTrendsOnBoard,
                        onClick = { showRevenueTrendsOnBoard = !showRevenueTrendsOnBoard },
                        icon = Icons.Default.TrendingUp,
                        activeColor = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("toggle_board_revenue_chart_chip")
                    )
                }

                AnimatedVisibility(visible = showFlavorSommelierOnBoard) {
                    GeminiFlavorSommelierCard(
                        sommelierState = uiState.flavorSommelierState,
                        eventType = "Custom Celebration",
                        dietaryTags = emptyList(),
                        pantryIngredients = uiState.allIngredients,
                        onRequestSuggestions = onRequestFlavorSuggestions,
                        onApplySuggestion = onStartOrderWithPairing,
                        applyButtonLabel = "Start New Order with This Pairing",
                        onOpenSecurityGuide = onOpenLaunchGuide
                    )
                }

                AnimatedVisibility(visible = showRevenueTrendsOnBoard) {
                    MonthlyRevenueChartSection(orders = uiState.allOrders)
                }
            }
        }

        // 2. Search & POS Status Filter Keypad
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = uiState.searchQuery,
                    onValueChange = onSearchQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_orders_input"),
                    placeholder = {
                        Text(
                            text = stringResource(R.string.search_orders_hint),
                            style = MaterialTheme.typography.bodyLarge
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search orders"
                        )
                    },
                    trailingIcon = {
                        if (uiState.searchQuery.isNotEmpty()) {
                            IconButton(onClick = { onSearchQueryChange("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear search"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PosFilterKeypadButton(
                        label = "All (${uiState.allOrders.size})",
                        selected = uiState.selectedStatusFilter == null,
                        onClick = { onStatusFilterSelect(null) },
                        modifier = Modifier.testTag("filter_chip_all")
                    )
                    OrderStatus.entries.forEach { status ->
                        val count = uiState.allOrders.count { it.statusEnum == status }
                        PosFilterKeypadButton(
                            label = "${status.label} ($count)",
                            selected = uiState.selectedStatusFilter == status,
                            onClick = { onStatusFilterSelect(status) },
                            modifier = Modifier.testTag("filter_chip_${status.name.lowercase()}")
                        )
                    }
                }
            }
        }

        // 3. Orders List or Empty State
        if (uiState.filteredOrders.isEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Cake,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(44.dp)
                        )
                        Text(
                            text = stringResource(R.string.empty_orders_title),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = stringResource(R.string.empty_orders_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Button(
                            onClick = onOpenNewOrder,
                            modifier = Modifier.testTag("empty_state_new_order_button")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.action_new_order))
                        }
                    }
                }
            }
        } else {
            items(uiState.filteredOrders, key = { it.id }) { order ->
                CakeOrderCard(
                    order = order,
                    onClick = { onOpenOrderDetail(order) },
                    onAdvanceStage = { onAdvanceStage(order) },
                    onRecordPayment = { onOpenPaymentDialog(order) },
                    onOpenSummary = { onOpenOrderSummary(order) },
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .testTag("order_card_${order.id}")
                )
            }
        }
    }

    // Order Detail & Blueprint Sheet
    val detailOrder = uiState.activeOrderDetail
    if (detailOrder != null) {
        ModalBottomSheet(
            onDismissRequest = onCloseOrderDetail,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            containerColor = MaterialTheme.colorScheme.surface
        ) {
            OrderDetailBottomSheetContent(
                order = detailOrder,
                onEdit = {
                    onCloseOrderDetail()
                    onEditOrder(detailOrder)
                },
                onDelete = {
                    onDeleteOrder(detailOrder)
                },
                onRecordPayment = {
                    onOpenPaymentDialog(detailOrder)
                },
                onOpenSummary = {
                    onCloseOrderDetail()
                    onOpenOrderSummary(detailOrder)
                },
                onTogglePrepStep = { stepKey ->
                    onTogglePrepStep(detailOrder, stepKey)
                },
                onUpdateDesignNotes = { updatedNotes ->
                    onUpdateOrderDesignNotes(detailOrder, updatedNotes)
                }
            )
        }
    }
}

@Composable
private fun PatisserieHeroHeader(
    activeOrdersCount: Int,
    totalServings: Int,
    balanceDue: Double,
    onNewOrderClick: () -> Unit,
    onOpenLaunchGuide: () -> Unit = {}
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        shadowElevation = 0.dp
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
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_hero_patisserie),
                        contentDescription = "Bakery workbench",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
                    )
                    Column {
                        Text(
                            text = "Bakery Order Desk",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Custom cakes, tier sizing & production queue",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Button(
                    onClick = onNewOrderClick,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                    modifier = Modifier.testTag("hero_new_order_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "New Order",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PosMetricBox(
                    label = "Active Cakes",
                    value = activeOrdersCount.toString(),
                    accentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                PosMetricBox(
                    label = "Total Servings",
                    value = "$totalServings slices",
                    accentColor = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                PosMetricBox(
                    label = "Balance Due",
                    value = formatCurrency(balanceDue),
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }

            Surface(
                onClick = onOpenLaunchGuide,
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("open_play_launch_guide_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.VerifiedUser,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Play Store Launch Guide & Firebase AI App Check",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = "View →",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun CakeOrderCard(
    order: CakeOrder,
    onClick: () -> Unit,
    onAdvanceStage: () -> Unit,
    onRecordPayment: () -> Unit,
    onOpenSummary: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val tierList = order.tierSizesList
    val tierSummary = if (tierList.size == 1) {
        "Single ${tierList.first()}\" ${order.shapeEnum.label}"
    } else {
        "${tierList.size} Tiers (${tierList.joinToString("\" + ")}\") ${order.shapeEnum.label}"
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.6f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: Thumbnail + Client & Design Header + Status Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    CakeDesignThumbnail(
                        order = order,
                        modifier = Modifier.fillMaxSize()
                    )
                    // Frosting Swatches Overlay at bottom-left of image
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                            .background(
                                Color.Black.copy(alpha = 0.45f),
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 6.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        order.colorHexList.take(3).forEach { hex ->
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(parseHexColor(hex))
                                    .border(0.5.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface,
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f))
                        ) {
                            Text(
                                text = order.eventType,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }

                        StatusBadge(status = order.statusEnum)
                    }

                    Text(
                        text = order.designTitle,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${order.clientName} • ${order.cakeFlavor}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    Text(
                        text = "${order.fillingFlavor} • ${order.frostingType}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // Middle Spec Pills: Serving Sizes & Delivery Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = "${order.estimatedServings} Servings (${order.cutStyleEnum.label})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = tierSummary,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Surface(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = if (order.fulfillmentEnum == FulfillmentType.DELIVERY) {
                                Icons.Default.LocalShipping
                            } else {
                                Icons.Default.Storefront
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Column {
                            Text(
                                text = formatOrderDate(order.deliveryDateMillis),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = order.deliveryTimeWindow,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f))

            // Payment Tracker Row + Quick Actions (clean white surface, no colored box fill)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Payments,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${formatCurrency(order.amountPaid)} / ${formatCurrency(order.totalPrice)} Paid",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }

                    PaymentStatusBadge(status = order.paymentStatus, balanceDue = order.balanceDue)
                }

                LinearProgressIndicator(
                    progress = { order.paymentProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(50)),
                    color = if (order.paymentStatus == PaymentStatus.PAID_IN_FULL) {
                        MaterialTheme.colorScheme.secondary
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PosTouchButton(
                        text = "Summary",
                        icon = Icons.Default.Event,
                        onClick = onOpenSummary,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface,
                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                        minHeight = 46.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("order_summary_chip_${order.id}")
                    )

                    PosTouchButton(
                        text = if (order.balanceDue > 0) "Pay" else "Paid",
                        icon = Icons.Default.Payments,
                        onClick = onRecordPayment,
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.secondary,
                        borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f),
                        minHeight = 46.dp,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("record_payment_chip_${order.id}")
                    )

                    if (order.statusEnum != OrderStatus.DELIVERED) {
                        PosTouchButton(
                            text = order.statusEnum.nextStage().label,
                            icon = Icons.Default.CheckCircle,
                            onClick = onAdvanceStage,
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            minHeight = 46.dp,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("advance_stage_chip_${order.id}")
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: OrderStatus) {
    val containerColor = when (status) {
        OrderStatus.INQUIRY -> MaterialTheme.colorScheme.tertiaryContainer
        OrderStatus.DEPOSIT_PAID -> MaterialTheme.colorScheme.primaryContainer
        OrderStatus.BAKING -> MaterialTheme.colorScheme.primaryContainer
        OrderStatus.DECORATING -> MaterialTheme.colorScheme.secondaryContainer
        OrderStatus.READY -> MaterialTheme.colorScheme.secondaryContainer
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when (status) {
        OrderStatus.INQUIRY -> MaterialTheme.colorScheme.onTertiaryContainer
        OrderStatus.DEPOSIT_PAID -> MaterialTheme.colorScheme.onPrimaryContainer
        OrderStatus.BAKING -> MaterialTheme.colorScheme.onPrimaryContainer
        OrderStatus.DECORATING -> MaterialTheme.colorScheme.onSecondaryContainer
        OrderStatus.READY -> MaterialTheme.colorScheme.onSecondaryContainer
        OrderStatus.DELIVERED -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = containerColor
    ) {
        Text(
            text = status.label,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun PaymentStatusBadge(status: PaymentStatus, balanceDue: Double) {
    val label = when (status) {
        PaymentStatus.PAID_IN_FULL -> "Paid in Full"
        PaymentStatus.DEPOSIT_PAID -> "${formatCurrency(balanceDue)} Due"
        PaymentStatus.UNPAID -> "Unpaid (${formatCurrency(balanceDue)})"
    }
    val bgColor = when (status) {
        PaymentStatus.PAID_IN_FULL -> MaterialTheme.colorScheme.secondaryContainer
        PaymentStatus.DEPOSIT_PAID -> MaterialTheme.colorScheme.tertiaryContainer
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.errorContainer
    }
    val txtColor = when (status) {
        PaymentStatus.PAID_IN_FULL -> MaterialTheme.colorScheme.onSecondaryContainer
        PaymentStatus.DEPOSIT_PAID -> MaterialTheme.colorScheme.onTertiaryContainer
        PaymentStatus.UNPAID -> MaterialTheme.colorScheme.onErrorContainer
    }
    Surface(
        shape = RoundedCornerShape(50),
        color = bgColor
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = txtColor,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun OrderDetailBottomSheetContent(
    order: CakeOrder,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onRecordPayment: () -> Unit,
    onOpenSummary: () -> Unit = {},
    onTogglePrepStep: (String) -> Unit,
    onUpdateDesignNotes: (String) -> Unit = {}
) {
    val breakdown = remember(order.tierSizesCsv, order.cakeShape, order.cutStyle, order.tierHeightInches) {
        TierServingCalculator.calculateCakeBreakdown(
            tierSizesInches = order.tierSizesList,
            shape = order.shapeEnum,
            cutStyle = order.cutStyleEnum,
            heightInches = order.tierHeightInches
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 36.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${order.eventType.uppercase()} • ${order.clientName}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = order.designTitle,
                    style = MaterialTheme.typography.headlineMedium
                )
                if (order.clientPhone.isNotBlank()) {
                    Text(
                        text = order.clientPhone,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            StatusBadge(status = order.statusEnum)
        }

        // Visual Tier Blueprint
        TierBlueprintCanvas(
            tierSizesInches = order.tierSizesList,
            tierHeightInches = order.tierHeightInches,
            shape = order.shapeEnum,
            stylePreset = order.stylePresetEnum,
            colorHexes = order.colorHexList,
            inscriptionText = order.inscriptionText
        )

        // Baker's Scaling & Recipe Quantities Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "BAKER'S SCALING & SERVING BREAKDOWN",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    ScalingStatItem("Servings", "${order.estimatedServings} (${order.cutStyleEnum.label})")
                    ScalingStatItem("Sponge Batter", "${breakdown.totalBatterGrams} g")
                    ScalingStatItem("Buttercream", "${breakdown.totalButtercreamGrams} g")
                    ScalingStatItem("Dowels / Board", "${breakdown.totalDowels} rods • ${breakdown.baseBoardDiameterInches}\"")
                }
            }
        }

        // Flavor, Inscription & Design Notes
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.RestaurantMenu,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Flavor Profile & Design Spec",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Text(
                    text = "• Sponge: ${order.cakeFlavor}\n• Filling: ${order.fillingFlavor}\n• Frosting: ${order.frostingType}",
                    style = MaterialTheme.typography.bodyMedium
                )
                if (order.inscriptionText.isNotBlank()) {
                    Text(
                        text = "Inscription: “${order.inscriptionText}”",
                        style = MaterialTheme.typography.bodyMedium,
                        fontStyle = FontStyle.Italic,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                if (order.dietaryTagsList.isNotEmpty()) {
                    Text(
                        text = "Dietary: ${order.dietaryTagsList.joinToString(" • ")}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                VoiceDictationNotesCard(
                    notesText = order.designNotes,
                    onNotesChange = onUpdateDesignNotes,
                    title = "Voice-to-Text Design & Client Feedback",
                    subtitle = "Dictate bench updates or client feedback directly into this cake order."
                )
            }
        }

        // Interactive Production Checklist
        Text(
            text = "Production & Bake Checklist",
            style = MaterialTheme.typography.titleMedium
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            PrepStep.entries.forEach { step ->
                val isDone = order.completedPrepSteps.contains(step.key)
                FilterChip(
                    selected = isDone,
                    onClick = { onTogglePrepStep(step.key) },
                    label = { Text(step.label) },
                    leadingIcon = if (isDone) {
                        {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null
                )
            }
        }

        // Delivery & Payment Info
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${order.fulfillmentEnum.label}: ${formatOrderDate(order.deliveryDateMillis)} (${order.deliveryTimeWindow})",
                        style = MaterialTheme.typography.titleSmall
                    )
                }
                Text(
                    text = order.deliveryAddress,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (order.deliveryNotes.isNotBlank()) {
                    Text(
                        text = order.deliveryNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Paid: ${formatCurrency(order.amountPaid)} of ${formatCurrency(order.totalPrice)} (${order.paymentMethod})",
                            style = MaterialTheme.typography.titleSmall
                        )
                        if (order.paymentNotes.isNotBlank()) {
                            Text(
                                text = order.paymentNotes,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Button(onClick = onRecordPayment) {
                        Text("Update Payment")
                    }
                }
            }
        }

        // Bottom Actions: Order Summary, Edit & Delete
        Button(
            onClick = onOpenSummary,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("detail_order_summary_button")
        ) {
            Icon(Icons.Default.Event, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Generate Comprehensive Order Summary")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onDelete,
                modifier = Modifier
                    .weight(1f)
                    .testTag("delete_order_button")
            ) {
                Icon(Icons.Default.DeleteOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.action_delete_order))
            }
            Button(
                onClick = onEdit,
                modifier = Modifier
                    .weight(1f)
                    .testTag("edit_order_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text(stringResource(R.string.action_edit_order))
            }
        }
    }
}

@Composable
private fun ScalingStatItem(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
