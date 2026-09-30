package com.example.data

import kotlinx.coroutines.flow.Flow
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

class CakeOrderRepository(private val dao: CakeOrderDao) {
    val allOrders: Flow<List<CakeOrder>> = dao.getAllOrders()
    val allCustomers: Flow<List<CustomerProfile>> = dao.getAllCustomers()
    val allIngredients: Flow<List<IngredientItem>> = dao.getAllIngredients()
    val allUsages: Flow<List<OrderIngredientUsage>> = dao.getAllIngredientUsages()

    suspend fun insertOrUpdateOrderWithCustomerSync(order: CakeOrder): Long {
        // Ensure a corresponding CustomerProfile exists or is updated
        val existingCustomer = if (order.customerId != null && order.customerId > 0L) {
            null
        } else {
            dao.findCustomerByName(order.clientName.trim())
        }

        val resolvedCustomerId = when {
            order.customerId != null && order.customerId > 0L -> order.customerId
            existingCustomer != null -> {
                dao.updateCustomer(
                    existingCustomer.copy(
                        phone = order.clientPhone.ifBlank { existingCustomer.phone },
                        email = order.clientEmail.ifBlank { existingCustomer.email },
                        address = if (order.fulfillmentEnum == FulfillmentType.DELIVERY && order.deliveryAddress.isNotBlank()) {
                            order.deliveryAddress
                        } else {
                            existingCustomer.address
                        }
                    )
                )
                existingCustomer.id
            }
            else -> {
                dao.insertCustomer(
                    CustomerProfile(
                        name = order.clientName.trim(),
                        phone = order.clientPhone.trim(),
                        email = order.clientEmail.trim(),
                        address = order.deliveryAddress.trim(),
                        notes = "Preferred flavor: ${order.cakeFlavor}"
                    )
                )
            }
        }

        val syncedOrder = order.copy(customerId = resolvedCustomerId)
        return if (syncedOrder.id == 0L) {
            dao.insertOrder(syncedOrder)
        } else {
            dao.updateOrder(syncedOrder)
            syncedOrder.id
        }
    }

    suspend fun update(order: CakeOrder) = dao.updateOrder(order)

    suspend fun delete(order: CakeOrder) = dao.deleteOrder(order)

    // Customer Profile Operations
    suspend fun saveCustomer(customer: CustomerProfile): Long {
        return if (customer.id == 0L) {
            dao.insertCustomer(customer)
        } else {
            dao.updateCustomer(customer)
            customer.id
        }
    }

    suspend fun deleteCustomer(customer: CustomerProfile) {
        dao.deleteCustomer(customer)
    }

    // Ingredient Inventory Operations
    suspend fun saveIngredient(item: IngredientItem): Long {
        return if (item.id == 0L) {
            dao.insertIngredient(item.copy(updatedAtMillis = System.currentTimeMillis()))
        } else {
            dao.updateIngredient(item.copy(updatedAtMillis = System.currentTimeMillis()))
            item.id
        }
    }

    suspend fun adjustIngredientStock(item: IngredientItem, delta: Double) {
        val updatedStock = (item.currentStock + delta).coerceAtLeast(0.0)
        dao.updateIngredient(
            item.copy(
                currentStock = updatedStock,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteIngredient(item: IngredientItem) {
        dao.deleteIngredient(item)
    }

    // Ingredient Usage per Order
    suspend fun recordIngredientUsageForOrder(
        orderId: Long,
        ingredient: IngredientItem,
        quantityUsed: Double
    ) {
        if (quantityUsed <= 0.0) return
        dao.insertIngredientUsage(
            OrderIngredientUsage(
                orderId = orderId,
                ingredientId = ingredient.id,
                ingredientName = ingredient.name,
                quantityUsed = quantityUsed,
                unit = ingredient.unit
            )
        )
        val remaining = (ingredient.currentStock - quantityUsed).coerceAtLeast(0.0)
        dao.updateIngredient(
            ingredient.copy(
                currentStock = remaining,
                updatedAtMillis = System.currentTimeMillis()
            )
        )
    }

    suspend fun removeIngredientUsage(usage: OrderIngredientUsage) {
        dao.deleteIngredientUsage(usage)
        val ingredient = dao.getIngredientById(usage.ingredientId)
        if (ingredient != null) {
            dao.updateIngredient(
                ingredient.copy(
                    currentStock = ingredient.currentStock + usage.quantityUsed,
                    updatedAtMillis = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun autoAllocateRecipeIngredientsForOrder(
        order: CakeOrder,
        availableIngredients: List<IngredientItem>
    ) {
        val breakdown = TierServingCalculator.calculateCakeBreakdown(
            tierSizesInches = order.tierSizesList,
            shape = order.shapeEnum,
            cutStyle = order.cutStyleEnum,
            heightInches = order.tierHeightInches
        )
        // Standard pastry formula from batter & buttercream weights
        val flourGrams = (breakdown.totalBatterGrams * 0.32).roundToInt().toDouble()
        val sugarGrams = ((breakdown.totalBatterGrams * 0.28) + (breakdown.totalButtercreamGrams * 0.35)).roundToInt().toDouble()
        val butterGrams = ((breakdown.totalBatterGrams * 0.22) + (breakdown.totalButtercreamGrams * 0.48)).roundToInt().toDouble()
        val vanillaMl = (order.tierSizesList.size * 15.0)

        val mappings = listOf(
            "Cake Flour" to flourGrams,
            "Caster Sugar" to sugarGrams,
            "Unsalted Cultured Butter" to butterGrams,
            "Madagascar Vanilla Paste" to vanillaMl
        )

        mappings.forEach { (keyword, qty) ->
            val matched = availableIngredients.find {
                it.name.contains(keyword, ignoreCase = true)
            }
            if (matched != null) {
                recordIngredientUsageForOrder(
                    orderId = order.id,
                    ingredient = matched,
                    quantityUsed = qty
                )
            }
        }
    }

    suspend fun ensureSeeded() {
        val now = System.currentTimeMillis()
        val dayMs = TimeUnit.DAYS.toMillis(1)

        val customerIds = if (dao.getCustomerCount() == 0) {
            dao.insertAllCustomers(
                listOf(
                    CustomerProfile(
                        name = "Clara & Julian Vance",
                        phone = "(555) 234-8910",
                        email = "clara.vance@example.com",
                        address = "Conservatory Greenhouse, 412 Botanical Way",
                        notes = "Loves botanical florals, citrus curds, and nut-free recipes. Ordered engagement cake previously."
                    ),
                    CustomerProfile(
                        name = "Elena Rostova",
                        phone = "(555) 876-5412",
                        email = "elena.rostova@example.com",
                        address = "742 Rosewood Terrace, Apt 4B",
                        notes = "Repeat birthday client; adores vintage Lambeth piping with Maraschino cherries."
                    ),
                    CustomerProfile(
                        name = "Maya Lin & Marcus Cole",
                        phone = "(555) 390-1122",
                        email = "maya.lin@example.com",
                        address = "Sunnyside Courtyard Bistro, 88 Oak St",
                        notes = "Requires eggless sponge options for family gatherings."
                    ),
                    CustomerProfile(
                        name = "Sophia Sterling",
                        phone = "(555) 601-4498",
                        email = "sophia.sterling@example.com",
                        address = "19 Crescent Hill Drive",
                        notes = "Celiac-friendly gluten-free almond-oat sponge required."
                    )
                )
            )
        } else {
            emptyList()
        }

        val ingredientIds = if (dao.getIngredientCount() == 0) {
            dao.insertAllIngredients(
                listOf(
                    IngredientItem(
                        name = "Bleached Swan Cake Flour",
                        category = "Flour & Dry",
                        currentStock = 4200.0,
                        unit = "g",
                        reorderPoint = 2000.0,
                        costPerUnit = 0.004
                    ),
                    IngredientItem(
                        name = "Unsalted Cultured Butter",
                        category = "Dairy & Butter",
                        currentStock = 1350.0, // Below reorder point (1800g) to highlight Low Stock alert
                        unit = "g",
                        reorderPoint = 1800.0,
                        costPerUnit = 0.014
                    ),
                    IngredientItem(
                        name = "Superfine Caster Sugar",
                        category = "Sugar & Sweeteners",
                        currentStock = 5500.0,
                        unit = "g",
                        reorderPoint = 2500.0,
                        costPerUnit = 0.003
                    ),
                    IngredientItem(
                        name = "Belgian 70% Dark Chocolate Callets",
                        category = "Chocolate & Extracts",
                        currentStock = 650.0, // Low stock!
                        unit = "g",
                        reorderPoint = 1000.0,
                        costPerUnit = 0.024
                    ),
                    IngredientItem(
                        name = "Madagascar Vanilla Bean Paste",
                        category = "Chocolate & Extracts",
                        currentStock = 180.0,
                        unit = "ml",
                        reorderPoint = 60.0,
                        costPerUnit = 0.18
                    ),
                    IngredientItem(
                        name = "Raspberry Chambord Compote",
                        category = "Fruit & Compotes",
                        currentStock = 900.0,
                        unit = "g",
                        reorderPoint = 500.0,
                        costPerUnit = 0.019
                    ),
                    IngredientItem(
                        name = "12\" Heavy Cake Drum Boards",
                        category = "Boards & Packaging",
                        currentStock = 3.0, // Low stock!
                        unit = "pcs",
                        reorderPoint = 5.0,
                        costPerUnit = 4.50
                    )
                )
            )
        } else {
            emptyList()
        }

        if (dao.getOrderCount() == 0) {
            val c1 = customerIds.getOrNull(0) ?: 1L
            val c2 = customerIds.getOrNull(1) ?: 2L
            val c3 = customerIds.getOrNull(2) ?: 3L
            val c4 = customerIds.getOrNull(3) ?: 4L

            val starterOrders = listOf(
                CakeOrder(
                    customerId = c1,
                    clientName = "Clara & Julian Vance",
                    clientPhone = "(555) 234-8910",
                    clientEmail = "clara.vance@example.com",
                    eventType = "Wedding",
                    orderStatus = OrderStatus.DECORATING.name,
                    designTitle = "3-Tier Pressed Pansy & Sage Meadow",
                    designStylePreset = DesignStylePreset.BOTANICAL_FLORAL.name,
                    cakeFlavor = "Lemon Elderflower",
                    fillingFlavor = "Meyer Lemon Curd",
                    frostingType = "Swiss Meringue Buttercream",
                    colorPaletteCsv = "#FFF5E8,#B8CEB8,#E6C27A",
                    inscriptionText = "C & J • Forever",
                    dietaryTagsCsv = "Organic Berries,Nut-Free",
                    designNotes = "Horizontal palette-knife texture on bottom 10\" tier, pressed edible violas and fresh sage leaves cascading diagonally, 24k gold leaf flecks on top edge.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.WEDDING.name,
                    tierSizesCsv = "10,8,6",
                    tierHeightInches = 5,
                    estimatedServings = 98,
                    fulfillmentType = FulfillmentType.DELIVERY.name,
                    deliveryDateMillis = now + dayMs * 1,
                    deliveryTimeWindow = "1:30 PM - 2:30 PM",
                    deliveryAddress = "Conservatory Greenhouse, 412 Botanical Way",
                    deliveryNotes = "Coordinate with florist Mia on arrival; keep cake chilled at 38°F until 1 hour before reception.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING,CRUMB_COAT,FINAL_COAT",
                    totalPrice = 680.0,
                    depositRequired = 340.0,
                    amountPaid = 340.0,
                    paymentMethod = "Zelle",
                    paymentNotes = "50% retainer received; balance due on wedding morning."
                ),
                CakeOrder(
                    customerId = c2,
                    clientName = "Elena Rostova",
                    clientPhone = "(555) 876-5412",
                    clientEmail = "elena.rostova@example.com",
                    eventType = "Birthday",
                    orderStatus = OrderStatus.BAKING.name,
                    designTitle = "Vintage Heart Coquette Lambeth",
                    designStylePreset = DesignStylePreset.LAMBETH_VINTAGE.name,
                    cakeFlavor = "Madagascar Vanilla Bean",
                    fillingFlavor = "Raspberry Chambord Compote",
                    frostingType = "American Vintage Piping Buttercream",
                    colorPaletteCsv = "#F4C2C2,#FFF5E8,#B83333",
                    inscriptionText = "Happy 30th Elena!",
                    dietaryTagsCsv = "Nut-Free",
                    designNotes = "2-tier heart cake with double drop-string Lambeth ruffles in blush rose and ivory, topped with stemmed Maraschino cherries and satin bows.",
                    cakeShape = CakeShape.HEART.name,
                    cutStyle = CutStyle.PARTY.name,
                    tierSizesCsv = "8,6",
                    tierHeightInches = 4,
                    estimatedServings = 28,
                    fulfillmentType = FulfillmentType.PICKUP.name,
                    deliveryDateMillis = now + dayMs * 2,
                    deliveryTimeWindow = "11:00 AM - 12:00 PM",
                    deliveryAddress = "Home Bakery Studio Pickup",
                    deliveryNotes = "Include tall window cake box and storage care card.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING",
                    totalPrice = 215.0,
                    depositRequired = 100.0,
                    amountPaid = 215.0,
                    paymentMethod = "Venmo",
                    paymentNotes = "Paid in full at booking."
                ),
                CakeOrder(
                    customerId = c3,
                    clientName = "Maya Lin & Marcus Cole",
                    clientPhone = "(555) 390-1122",
                    clientEmail = "maya.lin@example.com",
                    eventType = "Baby Shower",
                    orderStatus = OrderStatus.DEPOSIT_PAID.name,
                    designTitle = "Terracotta & Chamomile Arch Tier",
                    designStylePreset = DesignStylePreset.MODERN_ARCH.name,
                    cakeFlavor = "Pistachio Rosewater",
                    fillingFlavor = "Whipped Mascarpone Cream",
                    frostingType = "Italian Meringue Buttercream",
                    colorPaletteCsv = "#E28B76,#FFF5E8,#E6C27A",
                    inscriptionText = "Welcome Baby Cole",
                    dietaryTagsCsv = "Eggless",
                    designNotes = "Warm terracotta stucco arches with dried chamomile clusters and gold acrylic script topper.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.PARTY.name,
                    tierSizesCsv = "9,6",
                    tierHeightInches = 4,
                    estimatedServings = 42,
                    fulfillmentType = FulfillmentType.DELIVERY.name,
                    deliveryDateMillis = now + dayMs * 4,
                    deliveryTimeWindow = "10:00 AM - 11:00 AM",
                    deliveryAddress = "Sunnyside Courtyard Bistro, 88 Oak St",
                    deliveryNotes = "Bring 12\" brass cake stand (refundable $40 stand deposit included).",
                    prepChecklistCsv = "BAKE_SPONGE",
                    totalPrice = 310.0,
                    depositRequired = 150.0,
                    amountPaid = 150.0,
                    paymentMethod = "Credit Card",
                    paymentNotes = "Deposit paid via invoice link."
                ),
                CakeOrder(
                    customerId = c4,
                    clientName = "Sophia Sterling",
                    clientPhone = "(555) 601-4498",
                    clientEmail = "sophia.sterling@example.com",
                    eventType = "Anniversary",
                    orderStatus = OrderStatus.INQUIRY.name,
                    designTitle = "Belgian Ganache & Fig Semi-Naked",
                    designStylePreset = DesignStylePreset.RUSTIC_SEMI_NAKED.name,
                    cakeFlavor = "Belgian 70% Dark Chocolate",
                    fillingFlavor = "Salted Caramel Ganache",
                    frostingType = "Whipped White Chocolate Ganache",
                    colorPaletteCsv = "#FFF5E8,#E6C27A,#B83333",
                    inscriptionText = "10 Sweet Years",
                    dietaryTagsCsv = "Gluten-Free",
                    designNotes = "Single tall 8\" barrel tier with sheer semi-naked frosting, fresh halved mission figs, blackberries, and caramel drip.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.PARTY.name,
                    tierSizesCsv = "8",
                    tierHeightInches = 6,
                    estimatedServings = 24,
                    fulfillmentType = FulfillmentType.PICKUP.name,
                    deliveryDateMillis = now + dayMs * 6,
                    deliveryTimeWindow = "4:00 PM - 5:00 PM",
                    deliveryAddress = "Home Bakery Studio Pickup",
                    deliveryNotes = "Gluten-free almond-oat sponge; sanitize mixer bowls separately.",
                    prepChecklistCsv = "",
                    totalPrice = 165.0,
                    depositRequired = 80.0,
                    amountPaid = 0.0,
                    paymentMethod = "Venmo",
                    paymentNotes = "Awaiting $80 deposit confirmation."
                ),
                // Past completed orders across the last 6 months for rich Monthly Revenue Trend visualization
                CakeOrder(
                    customerId = c1,
                    clientName = "Clara & Julian Vance",
                    clientPhone = "(555) 234-8910",
                    clientEmail = "clara.vance@example.com",
                    eventType = "Engagement",
                    orderStatus = OrderStatus.DELIVERED.name,
                    designTitle = "Champagne & Pressed Viola Single Tier",
                    designStylePreset = DesignStylePreset.BOTANICAL_FLORAL.name,
                    cakeFlavor = "Strawberry Champagne",
                    fillingFlavor = "Fresh Strawberry Preserve",
                    frostingType = "Swiss Meringue Buttercream",
                    colorPaletteCsv = "#FFF5E8,#F4C2C2,#E6C27A",
                    inscriptionText = "She Said Yes!",
                    dietaryTagsCsv = "Nut-Free",
                    designNotes = "8\" round engagement party cake with edible pansies.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.PARTY.name,
                    tierSizesCsv = "8",
                    tierHeightInches = 4,
                    estimatedServings = 24,
                    fulfillmentType = FulfillmentType.PICKUP.name,
                    deliveryDateMillis = now - dayMs * 145,
                    deliveryTimeWindow = "2:00 PM - 3:00 PM",
                    deliveryAddress = "Home Bakery Studio Pickup",
                    deliveryNotes = "Delivered on time; client loved the berry notes.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING,CRUMB_COAT,FINAL_COAT,PIPING_DECOR,BOXED",
                    totalPrice = 420.0,
                    depositRequired = 210.0,
                    amountPaid = 420.0,
                    paymentMethod = "Zelle",
                    paymentNotes = "Paid in full."
                ),
                CakeOrder(
                    customerId = c2,
                    clientName = "Elena Rostova",
                    clientPhone = "(555) 876-5412",
                    clientEmail = "elena.rostova@example.com",
                    eventType = "Bridal Shower",
                    orderStatus = OrderStatus.DELIVERED.name,
                    designTitle = "2-Tier Parisian Blue Lambeth Swag",
                    designStylePreset = DesignStylePreset.LAMBETH_VINTAGE.name,
                    cakeFlavor = "Earl Grey Lavender",
                    fillingFlavor = "Meyer Lemon Curd",
                    frostingType = "American Vintage Piping Buttercream",
                    colorPaletteCsv = "#B5C9D8,#FFF5E8,#E6C27A",
                    inscriptionText = "Bride to Be",
                    dietaryTagsCsv = "Nut-Free",
                    designNotes = "Dusty Parisian blue Lambeth overpiping with pearl dragees.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.WEDDING.name,
                    tierSizesCsv = "9,6",
                    tierHeightInches = 4,
                    estimatedServings = 54,
                    fulfillmentType = FulfillmentType.DELIVERY.name,
                    deliveryDateMillis = now - dayMs * 115,
                    deliveryTimeWindow = "11:00 AM - 12:00 PM",
                    deliveryAddress = "742 Rosewood Terrace, Apt 4B",
                    deliveryNotes = "Delivered to bridal brunch.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING,CRUMB_COAT,FINAL_COAT,PIPING_DECOR,BOXED",
                    totalPrice = 580.0,
                    depositRequired = 290.0,
                    amountPaid = 580.0,
                    paymentMethod = "Venmo",
                    paymentNotes = "Paid in full."
                ),
                CakeOrder(
                    customerId = c3,
                    clientName = "Maya Lin & Marcus Cole",
                    clientPhone = "(555) 390-1122",
                    clientEmail = "maya.lin@example.com",
                    eventType = "Anniversary",
                    orderStatus = OrderStatus.DELIVERED.name,
                    designTitle = "Gold Leaf & Pistachio Rose Tier",
                    designStylePreset = DesignStylePreset.MODERN_ARCH.name,
                    cakeFlavor = "Pistachio Rosewater",
                    fillingFlavor = "Roasted Pistachio Praline",
                    frostingType = "Italian Meringue Buttercream",
                    colorPaletteCsv = "#B8CEB8,#FFF5E8,#E6C27A",
                    inscriptionText = "5 Years of Joy",
                    dietaryTagsCsv = "Eggless",
                    designNotes = "2-tier sculpted pistachio buttercream with Iranian rose petals.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.PARTY.name,
                    tierSizesCsv = "10,7",
                    tierHeightInches = 4,
                    estimatedServings = 62,
                    fulfillmentType = FulfillmentType.DELIVERY.name,
                    deliveryDateMillis = now - dayMs * 85,
                    deliveryTimeWindow = "3:00 PM - 4:00 PM",
                    deliveryAddress = "Sunnyside Courtyard Bistro, 88 Oak St",
                    deliveryNotes = "Completed delivery.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING,CRUMB_COAT,FINAL_COAT,PIPING_DECOR,BOXED",
                    totalPrice = 740.0,
                    depositRequired = 370.0,
                    amountPaid = 740.0,
                    paymentMethod = "Credit Card",
                    paymentNotes = "Paid in full."
                ),
                CakeOrder(
                    customerId = c4,
                    clientName = "Sophia Sterling",
                    clientPhone = "(555) 601-4498",
                    clientEmail = "sophia.sterling@example.com",
                    eventType = "Graduation",
                    orderStatus = OrderStatus.DELIVERED.name,
                    designTitle = "Salted Caramel & Dark Ganache Barrel",
                    designStylePreset = DesignStylePreset.RUSTIC_SEMI_NAKED.name,
                    cakeFlavor = "Brown Butter Salted Caramel",
                    fillingFlavor = "Dark Chocolate Fudge Ganache",
                    frostingType = "Swiss Meringue Buttercream",
                    colorPaletteCsv = "#FFF5E8,#E6C27A,#B83333",
                    inscriptionText = "Class of 2026",
                    dietaryTagsCsv = "Gluten-Free",
                    designNotes = "2-tier caramel drip cake with gold macarons.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.PARTY.name,
                    tierSizesCsv = "8,6",
                    tierHeightInches = 5,
                    estimatedServings = 46,
                    fulfillmentType = FulfillmentType.PICKUP.name,
                    deliveryDateMillis = now - dayMs * 55,
                    deliveryTimeWindow = "1:00 PM - 2:00 PM",
                    deliveryAddress = "Home Bakery Studio Pickup",
                    deliveryNotes = "Picked up on schedule.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING,CRUMB_COAT,FINAL_COAT,PIPING_DECOR,BOXED",
                    totalPrice = 650.0,
                    depositRequired = 325.0,
                    amountPaid = 650.0,
                    paymentMethod = "Zelle",
                    paymentNotes = "Paid in full."
                ),
                CakeOrder(
                    customerId = c1,
                    clientName = "Clara & Julian Vance",
                    clientPhone = "(555) 234-8910",
                    clientEmail = "clara.vance@example.com",
                    eventType = "Bridal Shower",
                    orderStatus = OrderStatus.DELIVERED.name,
                    designTitle = "3-Tier Botanical Chamomile & Lemon",
                    designStylePreset = DesignStylePreset.BOTANICAL_FLORAL.name,
                    cakeFlavor = "Lemon Elderflower",
                    fillingFlavor = "Passionfruit Curd",
                    frostingType = "Swiss Meringue Buttercream",
                    colorPaletteCsv = "#FFF5E8,#B8CEB8,#E6C27A",
                    inscriptionText = "Soon to be Mrs. Vance",
                    dietaryTagsCsv = "Nut-Free,Organic Berries",
                    designNotes = "3-tier garden party centerpiece cake.",
                    cakeShape = CakeShape.ROUND.name,
                    cutStyle = CutStyle.WEDDING.name,
                    tierSizesCsv = "10,8,6",
                    tierHeightInches = 4,
                    estimatedServings = 92,
                    fulfillmentType = FulfillmentType.DELIVERY.name,
                    deliveryDateMillis = now - dayMs * 26,
                    deliveryTimeWindow = "12:00 PM - 1:00 PM",
                    deliveryAddress = "Conservatory Greenhouse, 412 Botanical Way",
                    deliveryNotes = "Completed with rave reviews.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING,CRUMB_COAT,FINAL_COAT,PIPING_DECOR,BOXED",
                    totalPrice = 890.0,
                    depositRequired = 445.0,
                    amountPaid = 890.0,
                    paymentMethod = "Zelle",
                    paymentNotes = "Paid in full."
                ),
                CakeOrder(
                    customerId = c2,
                    clientName = "Elena Rostova",
                    clientPhone = "(555) 876-5412",
                    clientEmail = "elena.rostova@example.com",
                    eventType = "Birthday",
                    orderStatus = OrderStatus.DELIVERED.name,
                    designTitle = "Ruby Cherry & Blush Ruffle Tier",
                    designStylePreset = DesignStylePreset.LAMBETH_VINTAGE.name,
                    cakeFlavor = "Red Velvet Cocoa",
                    fillingFlavor = "Whipped Mascarpone Cream",
                    frostingType = "American Vintage Piping Buttercream",
                    colorPaletteCsv = "#F4C2C2,#B83333,#FFF5E8",
                    inscriptionText = "Cheers to Autumn!",
                    dietaryTagsCsv = "Nut-Free",
                    designNotes = "Completed early this month.",
                    cakeShape = CakeShape.HEART.name,
                    cutStyle = CutStyle.PARTY.name,
                    tierSizesCsv = "9,6",
                    tierHeightInches = 5,
                    estimatedServings = 52,
                    fulfillmentType = FulfillmentType.PICKUP.name,
                    deliveryDateMillis = now - dayMs * 3,
                    deliveryTimeWindow = "2:00 PM - 3:00 PM",
                    deliveryAddress = "Home Bakery Studio Pickup",
                    deliveryNotes = "Completed and picked up.",
                    prepChecklistCsv = "BAKE_SPONGE,MAKE_FILLING,CRUMB_COAT,FINAL_COAT,PIPING_DECOR,BOXED",
                    totalPrice = 960.0,
                    depositRequired = 480.0,
                    amountPaid = 960.0,
                    paymentMethod = "Venmo",
                    paymentNotes = "Paid in full."
                )
            )
            dao.insertAll(starterOrders)

            // Seed initial ingredient usage records for the first two orders
            val fId = ingredientIds.getOrNull(0) ?: 1L
            val bId = ingredientIds.getOrNull(1) ?: 2L
            val sId = ingredientIds.getOrNull(2) ?: 3L
            val rId = ingredientIds.getOrNull(5) ?: 6L

            dao.insertAllUsages(
                listOf(
                    OrderIngredientUsage(
                        orderId = 1L,
                        ingredientId = fId,
                        ingredientName = "Bleached Swan Cake Flour",
                        quantityUsed = 980.0,
                        unit = "g"
                    ),
                    OrderIngredientUsage(
                        orderId = 1L,
                        ingredientId = bId,
                        ingredientName = "Unsalted Cultured Butter",
                        quantityUsed = 1250.0,
                        unit = "g"
                    ),
                    OrderIngredientUsage(
                        orderId = 1L,
                        ingredientId = sId,
                        ingredientName = "Superfine Caster Sugar",
                        quantityUsed = 1100.0,
                        unit = "g"
                    ),
                    OrderIngredientUsage(
                        orderId = 2L,
                        ingredientId = fId,
                        ingredientName = "Bleached Swan Cake Flour",
                        quantityUsed = 460.0,
                        unit = "g"
                    ),
                    OrderIngredientUsage(
                        orderId = 2L,
                        ingredientId = rId,
                        ingredientName = "Raspberry Chambord Compote",
                        quantityUsed = 320.0,
                        unit = "g"
                    )
                )
            )
        }
    }
}
