package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.EventNote
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.CakeOrderRepository
import com.example.data.FirebaseAiSecurityManager
import com.example.ui.CakePlannerUiState
import com.example.ui.CakePlannerViewModel
import com.example.ui.PlannerTab
import com.example.ui.components.PosTouchButton
import com.example.ui.components.PosTouchMenuTile
import com.example.ui.screens.CustomerProfilesScreen
import com.example.ui.screens.IngredientInventoryScreen
import com.example.ui.screens.OrderEditorScreen
import com.example.ui.screens.OrderSummaryModalSheet
import com.example.ui.screens.OrdersBoardScreen
import com.example.ui.screens.PlayStoreLaunchGuideModalSheet
import com.example.ui.screens.RecordPaymentDialog
import com.example.ui.screens.ScheduleAndPaymentsHubScreen
import com.example.ui.screens.TierCalculatorScreen
import com.example.ui.screens.formatCurrency
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseAiSecurityManager.initializeProductionSecurity(this)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val context = LocalContext.current
                val repository = remember(context) {
                    val db = AppDatabase.getDatabase(context)
                    CakeOrderRepository(db.cakeOrderDao())
                }
                val viewModel: CakePlannerViewModel = viewModel(
                    factory = CakePlannerViewModel.Factory(repository)
                )
                CrumbAndTierApp(viewModel = viewModel)
            }
        }
    }
}

private data class PosMenuTileSpec(
    val tab: PlannerTab,
    val title: String,
    val icon: ImageVector,
    val badgeProvider: (CakePlannerUiState) -> String,
    val isAlertProvider: (CakePlannerUiState) -> Boolean = { false }
)

@Composable
fun CrumbAndTierApp(viewModel: CakePlannerViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isLaunchGuideOpen by remember { mutableStateOf(false) }

    if (uiState.activeTab != PlannerTab.ORDERS && !uiState.isEditorOpen) {
        BackHandler {
            viewModel.selectTab(PlannerTab.ORDERS)
        }
    }

    if (uiState.isEditorOpen) {
        OrderEditorScreen(
            initialOrder = uiState.editingOrder,
            savedCustomers = uiState.allCustomers,
            pantryIngredients = uiState.allIngredients,
            sommelierState = uiState.flavorSommelierState,
            onRequestFlavorSuggestions = viewModel::requestGeminiFlavorSuggestions,
            onCancel = { viewModel.closeOrderEditor() },
            onSave = { order -> viewModel.saveOrder(order) }
        )
        return
    }

    val menuSpecs = remember {
        listOf(
            PosMenuTileSpec(
                tab = PlannerTab.ORDERS,
                title = "Orders",
                icon = Icons.Filled.Cake,
                badgeProvider = { "${it.activeOrdersCount}" }
            ),
            PosMenuTileSpec(
                tab = PlannerTab.CUSTOMERS,
                title = "Clients",
                icon = Icons.Filled.People,
                badgeProvider = { "${it.allCustomers.size}" }
            ),
            PosMenuTileSpec(
                tab = PlannerTab.INVENTORY,
                title = "Pantry",
                icon = Icons.Filled.Inventory2,
                badgeProvider = {
                    if (it.lowStockIngredientsCount > 0) {
                        "${it.lowStockIngredientsCount} low"
                    } else {
                        "${it.allIngredients.size}"
                    }
                },
                isAlertProvider = { it.lowStockIngredientsCount > 0 }
            ),
            PosMenuTileSpec(
                tab = PlannerTab.TIER_STUDIO,
                title = "Tiers",
                icon = Icons.Filled.Layers,
                badgeProvider = { "${it.tierStudio.tierSizesInches.size}T" }
            ),
            PosMenuTileSpec(
                tab = PlannerTab.SCHEDULE,
                title = "Schedule",
                icon = Icons.Filled.EventNote,
                badgeProvider = { "${it.activeOrdersCount}" }
            ),
            PosMenuTileSpec(
                tab = PlannerTab.PAYMENTS,
                title = "Payments",
                icon = Icons.Filled.Payments,
                badgeProvider = { formatCurrency(it.totalBalanceDue) }
            )
        )
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isExpandedScreen = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            contentWindowInsets = WindowInsets.safeDrawing,
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    CleanLightTopBar(
                        uiState = uiState,
                        onNewOrder = { viewModel.openNewOrderEditor(prefillFromTierStudio = false) },
                        onOpenLaunchGuide = { isLaunchGuideOpen = true }
                    )
                    HorizontalDivider(
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                    )
                }
            },
            bottomBar = {
                if (!isExpandedScreen) {
                    PosBottomTouchDock(
                        specs = menuSpecs,
                        uiState = uiState,
                        onSelectTab = viewModel::selectTab
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (isExpandedScreen) {
                    PosTabletSidebarKeypad(
                        specs = menuSpecs,
                        uiState = uiState,
                        onSelectTab = viewModel::selectTab,
                        onNewOrder = { viewModel.openNewOrderEditor(prefillFromTierStudio = false) }
                    )
                }

                Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                    when (uiState.activeTab) {
                        PlannerTab.ORDERS -> {
                            OrdersBoardScreen(
                                uiState = uiState,
                                onSearchQueryChange = viewModel::updateSearchQuery,
                                onStatusFilterSelect = viewModel::selectStatusFilter,
                                onOpenNewOrder = { viewModel.openNewOrderEditor(false) },
                                onOpenOrderDetail = viewModel::openOrderDetail,
                                onCloseOrderDetail = { viewModel.openOrderDetail(null) },
                                onEditOrder = viewModel::openEditOrderEditor,
                                onDeleteOrder = viewModel::deleteOrder,
                                onAdvanceStage = viewModel::advanceOrderStage,
                                onOpenPaymentDialog = viewModel::openPaymentDialog,
                                onOpenOrderSummary = viewModel::openOrderSummary,
                                onTogglePrepStep = viewModel::togglePrepStep,
                                onRequestFlavorSuggestions = viewModel::requestGeminiFlavorSuggestions,
                                onStartOrderWithPairing = viewModel::openNewOrderWithFlavorPairing,
                                onUpdateOrderDesignNotes = viewModel::updateOrderDesignNotes,
                                onOpenLaunchGuide = { isLaunchGuideOpen = true }
                            )
                        }

                        PlannerTab.CUSTOMERS -> {
                            CustomerProfilesScreen(
                                uiState = uiState,
                                onSaveCustomer = viewModel::saveCustomerProfile,
                                onDeleteCustomer = viewModel::deleteCustomerProfile,
                                onNewOrderForCustomer = viewModel::openNewOrderForCustomer,
                                onOpenOrderDetail = viewModel::openOrderDetail,
                                onOpenOrderSummary = viewModel::openOrderSummary
                            )
                        }

                        PlannerTab.INVENTORY -> {
                            IngredientInventoryScreen(
                                uiState = uiState,
                                onSaveIngredient = viewModel::saveIngredientItem,
                                onAdjustStock = viewModel::adjustIngredientStock,
                                onDeleteIngredient = viewModel::deleteIngredientItem,
                                onRecordOrderUsage = viewModel::recordIngredientUsage,
                                onRemoveOrderUsage = viewModel::removeIngredientUsage,
                                onAutoAllocateOrderRecipe = viewModel::autoAllocateRecipeForOrder
                            )
                        }

                        PlannerTab.TIER_STUDIO -> {
                            TierCalculatorScreen(
                                studioState = uiState.tierStudio,
                                onSelectShape = viewModel::updateStudioShape,
                                onSelectCutStyle = viewModel::updateStudioCutStyle,
                                onSelectStylePreset = viewModel::updateStudioStylePreset,
                                onUpdateTierHeight = viewModel::updateStudioTierHeight,
                                onAddTier = viewModel::addTierToStudio,
                                onRemoveTierAt = viewModel::removeTierAtStudioIndex,
                                onApplyPreset = viewModel::applyTierPreset,
                                onToggleColorHex = viewModel::toggleStudioColorHex,
                                onUpdateInscription = viewModel::updateStudioInscription,
                                onUpdateDesignNotes = viewModel::updateStudioDesignNotes,
                                onStartOrderFromStudio = { viewModel.openNewOrderEditor(true) }
                            )
                        }

                        PlannerTab.SCHEDULE -> {
                            ScheduleAndPaymentsHubScreen(
                                uiState = uiState,
                                initialSubTab = 0,
                                onTogglePrepStep = viewModel::togglePrepStep,
                                onAdvanceStage = viewModel::advanceOrderStage,
                                onSelectPaymentFilter = viewModel::selectPaymentFilter,
                                onOpenPaymentDialog = viewModel::openPaymentDialog,
                                onOpenOrderDetail = viewModel::openOrderDetail
                            )
                        }

                        PlannerTab.PAYMENTS -> {
                            ScheduleAndPaymentsHubScreen(
                                uiState = uiState,
                                initialSubTab = 2,
                                onTogglePrepStep = viewModel::togglePrepStep,
                                onAdvanceStage = viewModel::advanceOrderStage,
                                onSelectPaymentFilter = viewModel::selectPaymentFilter,
                                onOpenPaymentDialog = viewModel::openPaymentDialog,
                                onOpenOrderDetail = viewModel::openOrderDetail
                            )
                        }
                    }
                }
            }
        }
    }

    val paymentOrder = uiState.paymentDialogOrder
    if (paymentOrder != null) {
        RecordPaymentDialog(
            order = paymentOrder,
            onDismiss = { viewModel.openPaymentDialog(null) },
            onConfirmPayment = { newAmount, method, notes ->
                viewModel.recordPayment(paymentOrder, newAmount, method, notes)
            }
        )
    }

    val summaryOrder = uiState.summaryDialogOrder
    if (summaryOrder != null) {
        OrderSummaryModalSheet(
            order = summaryOrder,
            usages = uiState.usagesForOrder(summaryOrder.id),
            onDismiss = { viewModel.openOrderSummary(null) }
        )
    }

    if (isLaunchGuideOpen) {
        PlayStoreLaunchGuideModalSheet(
            securityStatus = FirebaseAiSecurityManager.getStatus(),
            onDismiss = { isLaunchGuideOpen = false }
        )
    }
}

@Composable
private fun CleanLightTopBar(
    uiState: CakePlannerUiState,
    onNewOrder: () -> Unit,
    onOpenLaunchGuide: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Crumb & Tier",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "${uiState.activeOrdersCount} active orders • ${formatCurrency(uiState.totalBalanceDue)} due",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                onClick = onOpenLaunchGuide,
                modifier = Modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("top_bar_launch_guide_button"),
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VerifiedUser,
                        contentDescription = "Launch & Security Guide",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(17.dp)
                    )
                    Text(
                        text = "Guide",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Button(
                onClick = onNewOrder,
                modifier = Modifier
                    .defaultMinSize(minHeight = 44.dp)
                    .testTag("fab_new_order"),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.action_new_order),
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "New Order",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

/**
 * Clean White 6-Button POS Bottom Bar with crisp light selection state.
 */
@Composable
private fun PosBottomTouchDock(
    specs: List<PosMenuTileSpec>,
    uiState: CakePlannerUiState,
    onSelectTab: (PlannerTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            specs.forEach { spec ->
                val selected = uiState.activeTab == spec.tab
                Surface(
                    onClick = { onSelectTab(spec.tab) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 54.dp)
                        .testTag("nav_tab_${spec.tab.route}"),
                    shape = RoundedCornerShape(10.dp),
                    color = if (selected) {
                        MaterialTheme.colorScheme.primaryContainer
                    } else {
                        MaterialTheme.colorScheme.surface
                    },
                    contentColor = if (selected) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    border = BorderStroke(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
                        }
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(vertical = 6.dp, horizontal = 2.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = spec.icon,
                            contentDescription = spec.tab.label,
                            tint = if (selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = spec.title,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

/**
 * Clean White Sidebar Keypad for Tablet / Expanded viewports (>= 600.dp).
 */
@Composable
private fun PosTabletSidebarKeypad(
    specs: List<PosMenuTileSpec>,
    uiState: CakePlannerUiState,
    onSelectTab: (PlannerTab) -> Unit,
    onNewOrder: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(210.dp)
            .fillMaxHeight(),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            specs.forEach { spec ->
                PosTouchMenuTile(
                    title = spec.title,
                    badgeText = spec.badgeProvider(uiState),
                    icon = spec.icon,
                    selected = uiState.activeTab == spec.tab,
                    onClick = { onSelectTab(spec.tab) },
                    alertBadge = spec.isAlertProvider(uiState),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("nav_rail_${spec.tab.route}")
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            PosTouchButton(
                text = "+ New Cake Order",
                icon = Icons.Default.Add,
                onClick = onNewOrder,
                minHeight = 50.dp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
