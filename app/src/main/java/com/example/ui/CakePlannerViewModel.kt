package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.CakeOrder
import com.example.data.CakeOrderRepository
import com.example.data.CakeShape
import com.example.data.CakeTierBreakdown
import com.example.data.CustomerProfile
import com.example.data.CutStyle
import com.example.data.DesignStylePreset
import com.example.data.FlavorPairingSuggestion
import com.example.data.FlavorSommelierUiState
import com.example.data.FulfillmentType
import com.example.data.GeminiFlavorSommelierClient
import com.example.data.IngredientItem
import com.example.data.OrderIngredientUsage
import com.example.data.OrderStatus
import com.example.data.PaymentStatus
import com.example.data.TierServingCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class PlannerTab(val route: String, val label: String) {
    ORDERS("orders_board", "Orders"),
    CUSTOMERS("customer_profiles", "Customers"),
    INVENTORY("ingredient_pantry", "Pantry"),
    TIER_STUDIO("tier_studio", "Tiers"),
    SCHEDULE("schedule_timeline", "Schedule"),
    PAYMENTS("payments_ledger", "Payments")
}

data class TierStudioState(
    val selectedShape: CakeShape = CakeShape.ROUND,
    val selectedCutStyle: CutStyle = CutStyle.WEDDING,
    val selectedStylePreset: DesignStylePreset = DesignStylePreset.LAMBETH_VINTAGE,
    val tierSizesInches: List<Int> = listOf(10, 8, 6),
    val tierHeightInches: Int = 4,
    val selectedColorsHex: List<String> = listOf("#FFF5E8", "#F4C2C2", "#E6C27A"),
    val inscriptionPreview: String = "Sweet Celebrations",
    val studioDesignNotes: String = ""
) {
    val breakdown: CakeTierBreakdown
        get() = TierServingCalculator.calculateCakeBreakdown(
            tierSizesInches = tierSizesInches,
            shape = selectedShape,
            cutStyle = selectedCutStyle,
            heightInches = tierHeightInches
        )
}

data class CakePlannerUiState(
    val allOrders: List<CakeOrder> = emptyList(),
    val filteredOrders: List<CakeOrder> = emptyList(),
    val allCustomers: List<CustomerProfile> = emptyList(),
    val allIngredients: List<IngredientItem> = emptyList(),
    val allIngredientUsages: List<OrderIngredientUsage> = emptyList(),
    val activeTab: PlannerTab = PlannerTab.ORDERS,
    val searchQuery: String = "",
    val selectedStatusFilter: OrderStatus? = null,
    val selectedPaymentFilter: PaymentStatus? = null,
    val tierStudio: TierStudioState = TierStudioState(),
    val activeOrderDetail: CakeOrder? = null,
    val summaryDialogOrder: CakeOrder? = null,
    val editingOrder: CakeOrder? = null,
    val isEditorOpen: Boolean = false,
    val paymentDialogOrder: CakeOrder? = null,
    val flavorSommelierState: FlavorSommelierUiState = FlavorSommelierUiState.Idle
) {
    val activeOrdersCount: Int
        get() = allOrders.count { it.statusEnum != OrderStatus.DELIVERED }

    val totalServingsScheduled: Int
        get() = allOrders.filter { it.statusEnum != OrderStatus.DELIVERED }
            .sumOf { it.estimatedServings }

    val totalBookedRevenue: Double
        get() = allOrders.sumOf { it.totalPrice }

    val totalCollectedRevenue: Double
        get() = allOrders.sumOf { it.amountPaid }

    val totalBalanceDue: Double
        get() = allOrders.sumOf { it.balanceDue }

    val lowStockIngredientsCount: Int
        get() = allIngredients.count { it.isLowStock }

    fun ordersForCustomer(customer: CustomerProfile): List<CakeOrder> {
        return allOrders.filter { order ->
            (order.customerId != null && order.customerId == customer.id) ||
                order.clientName.equals(customer.name, ignoreCase = true)
        }.sortedByDescending { it.deliveryDateMillis }
    }

    fun usagesForOrder(orderId: Long): List<OrderIngredientUsage> {
        return allIngredientUsages.filter { it.orderId == orderId }
    }
}

private data class FilterControlState(
    val activeTab: PlannerTab = PlannerTab.ORDERS,
    val searchQuery: String = "",
    val statusFilter: OrderStatus? = null,
    val paymentFilter: PaymentStatus? = null,
    val activeDetailId: Long? = null,
    val summaryOrderId: Long? = null,
    val editingOrder: CakeOrder? = null,
    val isEditorOpen: Boolean = false,
    val paymentDialogOrderId: Long? = null,
    val flavorSommelierState: FlavorSommelierUiState = FlavorSommelierUiState.Idle
)

class CakePlannerViewModel(
    private val repository: CakeOrderRepository
) : ViewModel() {

    private val filterState = MutableStateFlow(FilterControlState())
    private val tierStudioState = MutableStateFlow(TierStudioState())

    init {
        viewModelScope.launch {
            repository.ensureSeeded()
        }
    }

    private val dataBundleFlow = combine(
        repository.allOrders,
        repository.allCustomers,
        repository.allIngredients,
        repository.allUsages
    ) { orders, customers, ingredients, usages ->
        DataBundle(orders, customers, ingredients, usages)
    }

    val uiState: StateFlow<CakePlannerUiState> = combine(
        dataBundleFlow,
        filterState,
        tierStudioState
    ) { bundle, filter, studio ->
        val orders = bundle.orders
        val query = filter.searchQuery.trim().lowercase()
        val filtered = orders.filter { order ->
            val matchesStatus = filter.statusFilter == null || order.statusEnum == filter.statusFilter
            val matchesQuery = query.isEmpty() ||
                order.clientName.lowercase().contains(query) ||
                order.designTitle.lowercase().contains(query) ||
                order.cakeFlavor.lowercase().contains(query) ||
                order.eventType.lowercase().contains(query) ||
                order.inscriptionText.lowercase().contains(query) ||
                order.deliveryAddress.lowercase().contains(query)
            matchesStatus && matchesQuery
        }

        CakePlannerUiState(
            allOrders = orders,
            filteredOrders = filtered,
            allCustomers = bundle.customers,
            allIngredients = bundle.ingredients,
            allIngredientUsages = bundle.usages,
            activeTab = filter.activeTab,
            searchQuery = filter.searchQuery,
            selectedStatusFilter = filter.statusFilter,
            selectedPaymentFilter = filter.paymentFilter,
            tierStudio = studio,
            activeOrderDetail = orders.find { it.id == filter.activeDetailId },
            summaryDialogOrder = orders.find { it.id == filter.summaryOrderId },
            editingOrder = filter.editingOrder,
            isEditorOpen = filter.isEditorOpen,
            paymentDialogOrder = orders.find { it.id == filter.paymentDialogOrderId },
            flavorSommelierState = filter.flavorSommelierState
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CakePlannerUiState()
    )

    private data class DataBundle(
        val orders: List<CakeOrder>,
        val customers: List<CustomerProfile>,
        val ingredients: List<IngredientItem>,
        val usages: List<OrderIngredientUsage>
    )

    fun selectTab(tab: PlannerTab) {
        filterState.update { it.copy(activeTab = tab) }
    }

    fun updateSearchQuery(query: String) {
        filterState.update { it.copy(searchQuery = query) }
    }

    fun selectStatusFilter(status: OrderStatus?) {
        filterState.update {
            it.copy(statusFilter = if (it.statusFilter == status) null else status)
        }
    }

    fun selectPaymentFilter(status: PaymentStatus?) {
        filterState.update {
            it.copy(paymentFilter = if (it.paymentFilter == status) null else status)
        }
    }

    fun openOrderDetail(order: CakeOrder?) {
        filterState.update { it.copy(activeDetailId = order?.id) }
    }

    fun openOrderSummary(order: CakeOrder?) {
        filterState.update { it.copy(summaryOrderId = order?.id) }
    }

    fun openNewOrderEditor(prefillFromTierStudio: Boolean = false) {
        val studio = tierStudioState.value
        val breakdown = studio.breakdown
        val defaultNow = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
        val template = if (prefillFromTierStudio) {
            CakeOrder(
                id = 0,
                clientName = "",
                clientPhone = "",
                clientEmail = "",
                eventType = "Wedding",
                orderStatus = OrderStatus.INQUIRY.name,
                designTitle = "${studio.tierSizesInches.size}-Tier ${studio.selectedStylePreset.label}",
                designStylePreset = studio.selectedStylePreset.name,
                cakeFlavor = "Madagascar Vanilla Bean",
                fillingFlavor = "Raspberry Chambord Compote",
                frostingType = "Swiss Meringue Buttercream",
                colorPaletteCsv = studio.selectedColorsHex.joinToString(","),
                inscriptionText = studio.inscriptionPreview,
                dietaryTagsCsv = "",
                designNotes = studio.studioDesignNotes.ifBlank {
                    "Configured in Tier Studio (${studio.selectedShape.label}, ${studio.selectedCutStyle.label})."
                },
                cakeShape = studio.selectedShape.name,
                cutStyle = studio.selectedCutStyle.name,
                tierSizesCsv = studio.tierSizesInches.joinToString(","),
                tierHeightInches = studio.tierHeightInches,
                estimatedServings = breakdown.totalServings,
                fulfillmentType = "PICKUP",
                deliveryDateMillis = defaultNow,
                deliveryTimeWindow = "11:00 AM - 12:00 PM",
                deliveryAddress = "Home Bakery Studio Pickup",
                deliveryNotes = "",
                totalPrice = breakdown.suggestedBasePrice,
                depositRequired = (breakdown.suggestedBasePrice * 0.5).toInt().toDouble(),
                amountPaid = 0.0,
                paymentMethod = "Venmo"
            )
        } else null
        filterState.update { it.copy(editingOrder = template, isEditorOpen = true) }
    }

    fun openNewOrderForCustomer(customer: CustomerProfile) {
        val defaultNow = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
        val fulfillment = if (customer.address.isNotBlank() &&
            !customer.address.contains("Pickup", ignoreCase = true)
        ) {
            FulfillmentType.DELIVERY
        } else {
            FulfillmentType.PICKUP
        }
        val template = CakeOrder(
            id = 0,
            customerId = customer.id,
            clientName = customer.name,
            clientPhone = customer.phone,
            clientEmail = customer.email,
            eventType = "Birthday",
            orderStatus = OrderStatus.INQUIRY.name,
            designTitle = "",
            designStylePreset = DesignStylePreset.LAMBETH_VINTAGE.name,
            cakeFlavor = "Madagascar Vanilla Bean",
            fillingFlavor = "Raspberry Chambord Compote",
            frostingType = "Swiss Meringue Buttercream",
            colorPaletteCsv = "#FFF5E8,#F4C2C2,#E6C27A",
            inscriptionText = "",
            dietaryTagsCsv = "",
            designNotes = customer.notes,
            cakeShape = CakeShape.ROUND.name,
            cutStyle = CutStyle.PARTY.name,
            tierSizesCsv = "8,6",
            tierHeightInches = 4,
            estimatedServings = 38,
            fulfillmentType = fulfillment.name,
            deliveryDateMillis = defaultNow,
            deliveryTimeWindow = "11:00 AM - 12:00 PM",
            deliveryAddress = customer.address.ifBlank { "Home Bakery Studio Pickup" },
            deliveryNotes = "",
            totalPrice = 240.0,
            depositRequired = 120.0,
            amountPaid = 0.0,
            paymentMethod = "Venmo"
        )
        filterState.update { it.copy(editingOrder = template, isEditorOpen = true) }
    }

    fun openEditOrderEditor(order: CakeOrder) {
        filterState.update { it.copy(editingOrder = order, isEditorOpen = true) }
    }

    fun requestGeminiFlavorSuggestions(
        ingredientsOrTheme: String,
        eventType: String,
        dietaryTags: List<String>
    ) {
        filterState.update { it.copy(flavorSommelierState = FlavorSommelierUiState.Loading) }
        viewModelScope.launch {
            val pantryNames = uiState.value.allIngredients.map { it.name }
            val result = GeminiFlavorSommelierClient.suggestComplementaryFlavors(
                ingredientsOrThemePrompt = ingredientsOrTheme,
                eventType = eventType,
                dietaryTags = dietaryTags,
                inStockPantryNames = pantryNames
            )
            result.fold(
                onSuccess = { list ->
                    filterState.update {
                        it.copy(
                            flavorSommelierState = FlavorSommelierUiState.Success(
                                promptSummary = ingredientsOrTheme,
                                suggestions = list
                            )
                        )
                    }
                },
                onFailure = { err ->
                    filterState.update {
                        it.copy(
                            flavorSommelierState = FlavorSommelierUiState.Error(
                                message = err.message ?: "Unable to reach Gemini API."
                            )
                        )
                    }
                }
            )
        }
    }

    fun openNewOrderWithFlavorPairing(suggestion: FlavorPairingSuggestion) {
        val defaultNow = System.currentTimeMillis() + 3 * 24 * 60 * 60 * 1000L
        val colors = suggestion.recommendedColorsHex.filter { it.startsWith("#") }
            .ifEmpty { listOf("#FFF5E8", "#F4C2C2", "#E6C27A") }
        val template = CakeOrder(
            id = 0,
            clientName = "",
            clientPhone = "",
            clientEmail = "",
            eventType = "Wedding",
            orderStatus = OrderStatus.INQUIRY.name,
            designTitle = suggestion.pairingTitle,
            designStylePreset = DesignStylePreset.BOTANICAL_FLORAL.name,
            cakeFlavor = suggestion.spongeFlavor,
            fillingFlavor = suggestion.fillingFlavor,
            frostingType = suggestion.frostingType,
            colorPaletteCsv = colors.joinToString(","),
            inscriptionText = "",
            dietaryTagsCsv = "",
            designNotes = "${suggestion.tastingNotes} ${suggestion.designTip}".trim(),
            cakeShape = CakeShape.ROUND.name,
            cutStyle = CutStyle.PARTY.name,
            tierSizesCsv = "8,6",
            tierHeightInches = 4,
            estimatedServings = 38,
            fulfillmentType = FulfillmentType.PICKUP.name,
            deliveryDateMillis = defaultNow,
            deliveryTimeWindow = "11:00 AM - 12:00 PM",
            deliveryAddress = "Home Bakery Studio Pickup",
            deliveryNotes = "",
            totalPrice = 260.0,
            depositRequired = 130.0,
            amountPaid = 0.0,
            paymentMethod = "Venmo"
        )
        filterState.update { it.copy(editingOrder = template, isEditorOpen = true) }
    }

    fun closeOrderEditor() {
        filterState.update { it.copy(editingOrder = null, isEditorOpen = false) }
    }

    fun saveOrder(order: CakeOrder) {
        viewModelScope.launch {
            repository.insertOrUpdateOrderWithCustomerSync(order)
            filterState.update { it.copy(editingOrder = null, isEditorOpen = false) }
        }
    }

    fun deleteOrder(order: CakeOrder) {
        viewModelScope.launch {
            repository.delete(order)
            filterState.update {
                it.copy(
                    activeDetailId = if (it.activeDetailId == order.id) null else it.activeDetailId,
                    summaryOrderId = if (it.summaryOrderId == order.id) null else it.summaryOrderId
                )
            }
        }
    }

    fun advanceOrderStage(order: CakeOrder) {
        viewModelScope.launch {
            val next = order.statusEnum.nextStage()
            repository.update(order.copy(orderStatus = next.name))
        }
    }

    fun togglePrepStep(order: CakeOrder, stepKey: String) {
        viewModelScope.launch {
            val current = order.completedPrepSteps.toMutableSet()
            if (current.contains(stepKey)) {
                current.remove(stepKey)
            } else {
                current.add(stepKey)
            }
            repository.update(order.copy(prepChecklistCsv = current.joinToString(",")))
        }
    }

    fun openPaymentDialog(order: CakeOrder?) {
        filterState.update { it.copy(paymentDialogOrderId = order?.id) }
    }

    fun recordPayment(order: CakeOrder, newAmountPaid: Double, method: String, notes: String) {
        viewModelScope.launch {
            val clamped = newAmountPaid.coerceIn(0.0, order.totalPrice)
            val newStatus = if (order.statusEnum == OrderStatus.INQUIRY && clamped > 0.0) {
                OrderStatus.DEPOSIT_PAID.name
            } else {
                order.orderStatus
            }
            repository.update(
                order.copy(
                    amountPaid = clamped,
                    paymentMethod = method,
                    paymentNotes = notes,
                    orderStatus = newStatus
                )
            )
            filterState.update { it.copy(paymentDialogOrderId = null) }
        }
    }

    // Customer Profile Actions
    fun saveCustomerProfile(customer: CustomerProfile) {
        viewModelScope.launch {
            repository.saveCustomer(customer)
        }
    }

    fun deleteCustomerProfile(customer: CustomerProfile) {
        viewModelScope.launch {
            repository.deleteCustomer(customer)
        }
    }

    // Ingredient Inventory Actions
    fun saveIngredientItem(item: IngredientItem) {
        viewModelScope.launch {
            repository.saveIngredient(item)
        }
    }

    fun adjustIngredientStock(item: IngredientItem, delta: Double) {
        viewModelScope.launch {
            repository.adjustIngredientStock(item, delta)
        }
    }

    fun deleteIngredientItem(item: IngredientItem) {
        viewModelScope.launch {
            repository.deleteIngredient(item)
        }
    }

    fun recordIngredientUsage(orderId: Long, ingredient: IngredientItem, quantityUsed: Double) {
        viewModelScope.launch {
            repository.recordIngredientUsageForOrder(orderId, ingredient, quantityUsed)
        }
    }

    fun removeIngredientUsage(usage: OrderIngredientUsage) {
        viewModelScope.launch {
            repository.removeIngredientUsage(usage)
        }
    }

    fun autoAllocateRecipeForOrder(order: CakeOrder) {
        viewModelScope.launch {
            repository.autoAllocateRecipeIngredientsForOrder(
                order = order,
                availableIngredients = uiState.value.allIngredients
            )
        }
    }

    // Tier Studio actions
    fun updateStudioShape(shape: CakeShape) {
        tierStudioState.update { it.copy(selectedShape = shape) }
    }

    fun updateStudioCutStyle(cutStyle: CutStyle) {
        tierStudioState.update { it.copy(selectedCutStyle = cutStyle) }
    }

    fun updateStudioStylePreset(preset: DesignStylePreset) {
        tierStudioState.update { it.copy(selectedStylePreset = preset) }
    }

    fun updateStudioTierHeight(heightInches: Int) {
        tierStudioState.update { it.copy(tierHeightInches = heightInches.coerceIn(3, 7)) }
    }

    fun addTierToStudio(diameterInches: Int) {
        tierStudioState.update { state ->
            if (state.tierSizesInches.size >= 4) state
            else state.copy(
                tierSizesInches = (state.tierSizesInches + diameterInches).sortedDescending()
            )
        }
    }

    fun removeTierAtStudioIndex(index: Int) {
        tierStudioState.update { state ->
            if (state.tierSizesInches.size <= 1) state
            else {
                val mutable = state.tierSizesInches.toMutableList()
                if (index in mutable.indices) mutable.removeAt(index)
                state.copy(tierSizesInches = mutable.sortedDescending())
            }
        }
    }

    fun applyTierPreset(sizes: List<Int>) {
        tierStudioState.update { it.copy(tierSizesInches = sizes.sortedDescending()) }
    }

    fun toggleStudioColorHex(hex: String) {
        tierStudioState.update { state ->
            val current = state.selectedColorsHex.toMutableList()
            if (current.contains(hex) && current.size > 1) {
                current.remove(hex)
            } else if (!current.contains(hex) && current.size < 4) {
                current.add(hex)
            }
            state.copy(selectedColorsHex = current)
        }
    }

    fun updateStudioInscription(text: String) {
        tierStudioState.update { it.copy(inscriptionPreview = text) }
    }

    fun updateStudioDesignNotes(notes: String) {
        tierStudioState.update { it.copy(studioDesignNotes = notes) }
    }

    fun updateOrderDesignNotes(order: CakeOrder, updatedNotes: String) {
        viewModelScope.launch {
            repository.update(order.copy(designNotes = updatedNotes))
        }
    }

    class Factory(private val repository: CakeOrderRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CakePlannerViewModel(repository) as T
        }
    }
}
