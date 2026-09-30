package com.example.data

import androidx.compose.ui.graphics.Color
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlin.math.PI
import kotlin.math.roundToInt

enum class OrderStatus(val label: String, val stepIndex: Int) {
    INQUIRY("Inquiry", 0),
    DEPOSIT_PAID("Deposit Paid", 1),
    BAKING("Baking", 2),
    DECORATING("Decorating", 3),
    READY("Ready for Box", 4),
    DELIVERED("Completed", 5);

    fun nextStage(): OrderStatus {
        val all = entries
        val nextIdx = (ordinal + 1).coerceAtMost(all.lastIndex)
        return all[nextIdx]
    }
}

enum class DesignStylePreset(val label: String, val subtitle: String) {
    LAMBETH_VINTAGE("Vintage Lambeth Piping", "Intricate ruffled swags, shells & cherries"),
    BOTANICAL_FLORAL("Botanical Pressed Florals", "Textured Swiss meringue & edible blooms"),
    MODERN_ARCH("Modern Sculpted & Gold Leaf", "Palette-knife stucco & 24k gold leaf"),
    RUSTIC_SEMI_NAKED("Rustic Semi-Naked Berry", "Sheer crumb coat with fresh berry compote")
}

enum class CakeShape(val label: String) {
    ROUND("Round"),
    SQUARE("Square"),
    HEART("Vintage Heart")
}

enum class CutStyle(val label: String, val sliceDimensions: String) {
    PARTY("Party Cut", "1.5\" × 2\" generous slices"),
    WEDDING("Event / Wedding Cut", "1\" × 2\" tall fingers")
}

enum class FulfillmentType(val label: String) {
    DELIVERY("Courier / Venue Delivery"),
    PICKUP("Home Bakery Pickup")
}

enum class PaymentStatus(val label: String) {
    UNPAID("Unpaid"),
    DEPOSIT_PAID("Deposit Paid"),
    PAID_IN_FULL("Paid in Full")
}

enum class PrepStep(val key: String, val label: String) {
    BAKE_SPONGE("BAKE_SPONGE", "Bake Sponges"),
    MAKE_FILLING("MAKE_FILLING", "Simmer Compote / Curd"),
    CRUMB_COAT("CRUMB_COAT", "Crumb Coat & Chill"),
    FINAL_COAT("FINAL_COAT", "Final Buttercream Coat"),
    PIPING_DECOR("PIPING_DECOR", "Piping, Florals & Topper"),
    BOXED("BOXED", "Boarded, Boxed & Chilled")
}

data class FrostingSwatch(val name: String, val hex: String, val color: Color)

object PatisserieCatalog {
    val flavors = listOf(
        "Madagascar Vanilla Bean",
        "Belgian 70% Dark Chocolate",
        "Lemon Elderflower",
        "Pistachio Rosewater",
        "Red Velvet Cocoa",
        "Brown Butter Salted Caramel",
        "Earl Grey Lavender",
        "Strawberry Champagne"
    )

    val fillings = listOf(
        "Raspberry Chambord Compote",
        "Meyer Lemon Curd",
        "Salted Caramel Ganache",
        "Whipped Mascarpone Cream",
        "Roasted Pistachio Praline",
        "Dark Chocolate Fudge Ganache",
        "Fresh Strawberry Preserve",
        "Passionfruit Curd"
    )

    val frostings = listOf(
        "Swiss Meringue Buttercream",
        "Italian Meringue Buttercream",
        "Whipped White Chocolate Ganache",
        "American Vintage Piping Buttercream",
        "Cream Cheese Ermine Frosting",
        "Satin Rolled Fondant"
    )

    val eventTypes = listOf(
        "Wedding",
        "Birthday",
        "Baby Shower",
        "Anniversary",
        "Bridal Shower",
        "Graduation",
        "Engagement"
    )

    val paymentMethods = listOf(
        "Venmo",
        "Zelle",
        "Credit Card",
        "Cash",
        "Bank Transfer"
    )

    val dietaryTags = listOf(
        "Gluten-Free",
        "Nut-Free",
        "Eggless",
        "Dairy-Free",
        "Vegan",
        "Organic Berries"
    )

    val ingredientCategories = listOf(
        "Flour & Dry",
        "Dairy & Butter",
        "Sugar & Sweeteners",
        "Chocolate & Extracts",
        "Fruit & Compotes",
        "Boards & Packaging"
    )

    val ingredientUnits = listOf("g", "kg", "ml", "pcs", "oz", "lb")

    val colorSwatches = listOf(
        FrostingSwatch("Buttercream Ivory", "#FFF5E8", Color(0xFFFFF5E8)),
        FrostingSwatch("Blush Rose", "#F4C2C2", Color(0xFFF4C2C2)),
        FrostingSwatch("Terracotta Peach", "#E28B76", Color(0xFFE28B76)),
        FrostingSwatch("Pistachio Sage", "#B8CEB8", Color(0xFFB8CEB8)),
        FrostingSwatch("Champagne Gold", "#E6C27A", Color(0xFFE6C27A)),
        FrostingSwatch("French Lavender", "#D3C4E3", Color(0xFFD3C4E3)),
        FrostingSwatch("Dusty Parisian Blue", "#B5C9D8", Color(0xFFB5C9D8)),
        FrostingSwatch("Maraschino Ruby", "#B83333", Color(0xFFB83333))
    )
}

data class SingleTierSpec(
    val diameterInches: Int,
    val servings: Int,
    val batterGrams: Int,
    val buttercreamGrams: Int,
    val dowelsNeeded: Int,
    val boardSizeInches: Int
)

data class MonthlyRevenuePoint(
    val yearMonthKey: String, // e.g. "2026-09"
    val monthLabel: String, // e.g. "Sep"
    val fullMonthLabel: String, // e.g. "Sep 2026"
    val completedRevenue: Double,
    val completedOrdersCount: Int,
    val completedServings: Int,
    val pipelineRevenue: Double
)

object MonthlyRevenueCalculator {
    fun buildMonthlyTrends(
        orders: List<CakeOrder>,
        referenceTimeMillis: Long = System.currentTimeMillis(),
        monthCount: Int = 6
    ): List<MonthlyRevenuePoint> {
        val cal = java.util.Calendar.getInstance().apply {
            timeInMillis = referenceTimeMillis
            set(java.util.Calendar.DAY_OF_MONTH, 1)
            set(java.util.Calendar.HOUR_OF_DAY, 0)
            set(java.util.Calendar.MINUTE, 0)
            set(java.util.Calendar.SECOND, 0)
            set(java.util.Calendar.MILLISECOND, 0)
        }

        val keyFormat = java.text.SimpleDateFormat("yyyy-MM", java.util.Locale.US)
        val shortMonthFormat = java.text.SimpleDateFormat("MMM", java.util.Locale.US)
        val fullMonthFormat = java.text.SimpleDateFormat("MMM yyyy", java.util.Locale.US)

        val monthBuckets = mutableListOf<Triple<String, String, String>>()
        val tempCal = cal.clone() as java.util.Calendar
        tempCal.add(java.util.Calendar.MONTH, -(monthCount - 1))

        for (i in 0 until monthCount) {
            val date = tempCal.time
            monthBuckets.add(
                Triple(
                    keyFormat.format(date),
                    shortMonthFormat.format(date),
                    fullMonthFormat.format(date)
                )
            )
            tempCal.add(java.util.Calendar.MONTH, 1)
        }

        return monthBuckets.map { (ymKey, shortLabel, fullLabel) ->
            val monthOrders = orders.filter { order ->
                keyFormat.format(java.util.Date(order.deliveryDateMillis)) == ymKey
            }
            val completed = monthOrders.filter { it.statusEnum == OrderStatus.DELIVERED }
            val pipeline = monthOrders.filter { it.statusEnum != OrderStatus.DELIVERED }

            MonthlyRevenuePoint(
                yearMonthKey = ymKey,
                monthLabel = shortLabel,
                fullMonthLabel = fullLabel,
                completedRevenue = completed.sumOf { it.totalPrice },
                completedOrdersCount = completed.size,
                completedServings = completed.sumOf { it.estimatedServings },
                pipelineRevenue = pipeline.sumOf { it.totalPrice }
            )
        }
    }
}

data class CakeTierBreakdown(
    val tiers: List<SingleTierSpec>,
    val totalServings: Int,
    val totalBatterGrams: Int,
    val totalButtercreamGrams: Int,
    val totalDowels: Int,
    val baseBoardDiameterInches: Int,
    val suggestedBasePrice: Double
)

object TierServingCalculator {
    val availableSizes = listOf(4, 6, 8, 10, 12, 14)

    fun calculateTierSpec(
        diameterInches: Int,
        shape: CakeShape,
        cutStyle: CutStyle,
        isSupportingUpperTier: Boolean,
        heightInches: Int = 4
    ): SingleTierSpec {
        val heightFactor = heightInches / 4.0
        val areaSqIn = when (shape) {
            CakeShape.ROUND -> PI * (diameterInches / 2.0) * (diameterInches / 2.0)
            CakeShape.SQUARE -> (diameterInches * diameterInches).toDouble()
            CakeShape.HEART -> (diameterInches * diameterInches) * 0.78
        }
        val sliceArea = when (cutStyle) {
            CutStyle.PARTY -> 2.4
            CutStyle.WEDDING -> 1.55
        }
        val servings = ((areaSqIn / sliceArea) * (0.85 + 0.15 * heightFactor)).roundToInt().coerceAtLeast(4)
        val batterGrams = (areaSqIn * 24.5 * heightFactor).roundToInt()
        val buttercreamGrams = (areaSqIn * 14.0 * heightFactor).roundToInt()
        val dowels = if (isSupportingUpperTier) {
            when {
                diameterInches >= 12 -> 8
                diameterInches >= 10 -> 6
                diameterInches >= 8 -> 5
                else -> 4
            }
        } else 0
        val boardSize = diameterInches + 2
        return SingleTierSpec(
            diameterInches = diameterInches,
            servings = servings,
            batterGrams = batterGrams,
            buttercreamGrams = buttercreamGrams,
            dowelsNeeded = dowels,
            boardSizeInches = boardSize
        )
    }

    fun calculateCakeBreakdown(
        tierSizesInches: List<Int>,
        shape: CakeShape,
        cutStyle: CutStyle,
        heightInches: Int = 4
    ): CakeTierBreakdown {
        val sortedBottomToTop = tierSizesInches.sortedDescending()
        val specs = sortedBottomToTop.mapIndexed { index, diameter ->
            val hasUpperTier = index < sortedBottomToTop.lastIndex
            calculateTierSpec(
                diameterInches = diameter,
                shape = shape,
                cutStyle = cutStyle,
                isSupportingUpperTier = hasUpperTier,
                heightInches = heightInches
            )
        }
        val totalServings = specs.sumOf { it.servings }
        val totalBatter = specs.sumOf { it.batterGrams }
        val totalButtercream = specs.sumOf { it.buttercreamGrams }
        val totalDowels = specs.sumOf { it.dowelsNeeded }
        val baseBoard = (sortedBottomToTop.firstOrNull() ?: 8) + 2
        val suggestedPrice = (totalServings * 6.5) + ((specs.size - 1).coerceAtLeast(0) * 30.0)
        return CakeTierBreakdown(
            tiers = specs,
            totalServings = totalServings,
            totalBatterGrams = totalBatter,
            totalButtercreamGrams = totalButtercream,
            totalDowels = totalDowels,
            baseBoardDiameterInches = baseBoard,
            suggestedBasePrice = suggestedPrice
        )
    }

    fun parseTierSizes(csv: String): List<Int> {
        return csv.split(",")
            .mapNotNull { it.trim().toIntOrNull() }
            .filter { it in 4..18 }
            .sortedDescending()
            .ifEmpty { listOf(8) }
    }
}

@Entity(tableName = "customer_profiles")
data class CustomerProfile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val phone: String,
    val email: String,
    val address: String,
    val notes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "ingredient_items")
data class IngredientItem(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val category: String,
    val currentStock: Double,
    val unit: String, // e.g., "g", "pcs", "ml"
    val reorderPoint: Double,
    val costPerUnit: Double = 0.0,
    val updatedAtMillis: Long = System.currentTimeMillis()
) {
    val isLowStock: Boolean
        get() = currentStock <= reorderPoint

    val stockRatio: Float
        get() {
            val target = (reorderPoint * 2.5).coerceAtLeast(1.0)
            return (currentStock / target).toFloat().coerceIn(0f, 1f)
        }
}

@Entity(tableName = "order_ingredient_usages")
data class OrderIngredientUsage(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderId: Long,
    val ingredientId: Long,
    val ingredientName: String,
    val quantityUsed: Double,
    val unit: String,
    val loggedAtMillis: Long = System.currentTimeMillis()
)

@Entity(tableName = "cake_orders")
data class CakeOrder(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val customerId: Long? = null,
    val clientName: String,
    val clientPhone: String,
    val clientEmail: String = "",
    val eventType: String,
    val orderStatus: String, // OrderStatus.name
    // Design Specs
    val designTitle: String,
    val designStylePreset: String, // DesignStylePreset.name
    val customPhotoUri: String? = null,
    val cakeFlavor: String,
    val fillingFlavor: String,
    val frostingType: String,
    val colorPaletteCsv: String, // e.g. "#FFF5E8,#F4C2C2,#E6C27A"
    val inscriptionText: String,
    val dietaryTagsCsv: String,
    val designNotes: String,
    // Serving Sizes & Tiers
    val cakeShape: String, // CakeShape.name
    val cutStyle: String, // CutStyle.name
    val tierSizesCsv: String, // e.g. "10,8,6"
    val tierHeightInches: Int = 4,
    val estimatedServings: Int,
    // Delivery & Logistics
    val fulfillmentType: String, // FulfillmentType.name
    val deliveryDateMillis: Long,
    val deliveryTimeWindow: String,
    val deliveryAddress: String,
    val deliveryNotes: String,
    val prepChecklistCsv: String = "",
    // Payments
    val totalPrice: Double,
    val depositRequired: Double,
    val amountPaid: Double,
    val paymentMethod: String,
    val paymentNotes: String = "",
    val createdAtMillis: Long = System.currentTimeMillis()
) {
    val statusEnum: OrderStatus
        get() = OrderStatus.entries.find { it.name == orderStatus } ?: OrderStatus.INQUIRY

    val stylePresetEnum: DesignStylePreset
        get() = DesignStylePreset.entries.find { it.name == designStylePreset }
            ?: DesignStylePreset.LAMBETH_VINTAGE

    val shapeEnum: CakeShape
        get() = CakeShape.entries.find { it.name == cakeShape } ?: CakeShape.ROUND

    val cutStyleEnum: CutStyle
        get() = CutStyle.entries.find { it.name == cutStyle } ?: CutStyle.PARTY

    val fulfillmentEnum: FulfillmentType
        get() = FulfillmentType.entries.find { it.name == fulfillmentType }
            ?: FulfillmentType.PICKUP

    val tierSizesList: List<Int>
        get() = TierServingCalculator.parseTierSizes(tierSizesCsv)

    val balanceDue: Double
        get() = (totalPrice - amountPaid).coerceAtLeast(0.0)

    val paymentStatus: PaymentStatus
        get() = when {
            amountPaid >= totalPrice - 0.01 && totalPrice > 0 -> PaymentStatus.PAID_IN_FULL
            amountPaid > 0.01 -> PaymentStatus.DEPOSIT_PAID
            else -> PaymentStatus.UNPAID
        }

    val paymentProgress: Float
        get() = if (totalPrice <= 0.0) 1f else (amountPaid / totalPrice).toFloat().coerceIn(0f, 1f)

    val completedPrepSteps: Set<String>
        get() = prepChecklistCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet()

    val dietaryTagsList: List<String>
        get() = dietaryTagsCsv.split(",").map { it.trim() }.filter { it.isNotEmpty() }

    val colorHexList: List<String>
        get() = colorPaletteCsv.split(",").map { it.trim() }.filter { it.startsWith("#") }
            .ifEmpty { listOf("#FFF5E8", "#F4C2C2") }

    fun generateFormattedSummary(usages: List<OrderIngredientUsage> = emptyList()): String {
        val tierSpecText = if (tierSizesList.size == 1) {
            "Single ${tierSizesList.first()}\" ${shapeEnum.label} (${tierHeightInches}\" tall)"
        } else {
            "${tierSizesList.size} Tiers (${tierSizesList.joinToString("\" + ")}\") ${shapeEnum.label} • ${tierHeightInches}\" tall/tier"
        }
        val dateFormat = java.text.SimpleDateFormat("EEE, MMM d, yyyy", java.util.Locale.US)
        val dateStr = dateFormat.format(java.util.Date(deliveryDateMillis))
        val currency = java.text.NumberFormat.getCurrencyInstance(java.util.Locale.US).apply {
            maximumFractionDigits = 2
        }

        return buildString {
            appendLine("========================================")
            appendLine("   CRUMB & TIER — CAKE ORDER SUMMARY    ")
            appendLine("========================================")
            appendLine("Order ID: #${id.toString().padStart(4, '0')} (${statusEnum.label})")
            appendLine()
            appendLine("CLIENT & EVENT DETAILS")
            appendLine("• Customer: $clientName")
            if (clientPhone.isNotBlank()) appendLine("• Phone: $clientPhone")
            if (clientEmail.isNotBlank()) appendLine("• Email: $clientEmail")
            appendLine("• Celebration: $eventType")
            appendLine()
            appendLine("CAKE DESIGN & FLAVOR SPECIFICATION")
            appendLine("• Design: $designTitle (${stylePresetEnum.label})")
            appendLine("• Sponge Flavor: $cakeFlavor")
            appendLine("• Filling / Compote: $fillingFlavor")
            appendLine("• Frosting Finish: $frostingType")
            if (inscriptionText.isNotBlank()) appendLine("• Inscription: \"$inscriptionText\"")
            if (dietaryTagsList.isNotEmpty()) appendLine("• Dietary / Allergens: ${dietaryTagsList.joinToString(", ")}")
            if (designNotes.isNotBlank()) appendLine("• Design Notes: $designNotes")
            appendLine()
            appendLine("SIZE & SERVING ARCHITECTURE")
            appendLine("• Tier Dimensions: $tierSpecText")
            appendLine("• Serving Count: $estimatedServings servings (${cutStyleEnum.label} ${cutStyleEnum.sliceDimensions})")
            appendLine()
            appendLine("DELIVERY / PICKUP LOGISTICS")
            appendLine("• Method: ${fulfillmentEnum.label}")
            appendLine("• Date & Time: $dateStr • $deliveryTimeWindow")
            appendLine("• Location: $deliveryAddress")
            if (deliveryNotes.isNotBlank()) appendLine("• Logistics Note: $deliveryNotes")
            appendLine()
            appendLine("PAYMENT & BALANCE SUMMARY")
            appendLine("• Total Order Cost: ${currency.format(totalPrice)}")
            appendLine("• Deposit Required: ${currency.format(depositRequired)}")
            appendLine("• Payment Received: ${currency.format(amountPaid)} via $paymentMethod")
            appendLine("• Remaining Balance: ${currency.format(balanceDue)} (${paymentStatus.label})")
            if (paymentNotes.isNotBlank()) appendLine("• Payment Note: $paymentNotes")
            if (usages.isNotEmpty()) {
                appendLine()
                appendLine("ALLOCATED PANTRY INGREDIENTS")
                usages.forEach { u ->
                    appendLine("• ${u.ingredientName}: ${u.quantityUsed.toInt()} ${u.unit}")
                }
            }
            appendLine("========================================")
        }
    }
}
