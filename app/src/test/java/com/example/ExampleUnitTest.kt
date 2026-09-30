package com.example

import com.example.data.CakeOrder
import com.example.data.CakeShape
import com.example.data.CustomerProfile
import com.example.data.CutStyle
import com.example.data.DesignStylePreset
import com.example.data.FulfillmentType
import com.example.data.GeminiFlavorSommelierClient
import com.example.data.IngredientItem
import com.example.data.MonthlyRevenueCalculator
import com.example.data.OrderIngredientUsage
import com.example.data.OrderStatus
import com.example.data.PaymentStatus
import com.example.data.TierServingCalculator
import com.example.data.FirebaseAiSecurityManager
import com.example.ui.CakePlannerUiState
import com.example.ui.components.DictationNoteTag
import com.example.ui.components.VoiceDictationFormatter
import com.example.ui.screens.PlayStoreLaunchGuideCatalog
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun tierServingCalculator_computesMultiTierBreakdownAndDowels() {
        val breakdown = TierServingCalculator.calculateCakeBreakdown(
            tierSizesInches = listOf(10, 8, 6),
            shape = CakeShape.ROUND,
            cutStyle = CutStyle.WEDDING,
            heightInches = 4
        )
        assertEquals(3, breakdown.tiers.size)
        assertTrue(breakdown.totalServings > 70)
        assertTrue(breakdown.totalBatterGrams > 2000)
        assertTrue(breakdown.totalDowels > 0)
        assertEquals(12, breakdown.baseBoardDiameterInches)
    }

    @Test
    fun cakeOrder_computesPaymentStatusAndFormattedOrderSummary() {
        val order = CakeOrder(
            id = 42L,
            customerId = 7L,
            clientName = "Clara Vance",
            clientPhone = "555-0199",
            clientEmail = "clara@example.com",
            eventType = "Wedding",
            orderStatus = OrderStatus.DEPOSIT_PAID.name,
            designTitle = "Pressed Floral Tier",
            designStylePreset = DesignStylePreset.BOTANICAL_FLORAL.name,
            cakeFlavor = "Lemon Elderflower",
            fillingFlavor = "Meyer Lemon Curd",
            frostingType = "Swiss Meringue Buttercream",
            colorPaletteCsv = "#FFF5E8,#B8CEB8",
            inscriptionText = "C & J",
            dietaryTagsCsv = "Nut-Free",
            designNotes = "",
            cakeShape = CakeShape.ROUND.name,
            cutStyle = CutStyle.WEDDING.name,
            tierSizesCsv = "10,8,6",
            estimatedServings = 90,
            fulfillmentType = FulfillmentType.DELIVERY.name,
            deliveryDateMillis = 1790000000000L,
            deliveryTimeWindow = "1:00 PM - 2:00 PM",
            deliveryAddress = "Botanical Conservatory",
            deliveryNotes = "",
            totalPrice = 500.0,
            depositRequired = 250.0,
            amountPaid = 200.0,
            paymentMethod = "Zelle"
        )
        assertEquals(300.0, order.balanceDue, 0.01)
        assertEquals(PaymentStatus.DEPOSIT_PAID, order.paymentStatus)

        val usages = listOf(
            OrderIngredientUsage(
                orderId = 42L,
                ingredientId = 1L,
                ingredientName = "Bleached Swan Cake Flour",
                quantityUsed = 950.0,
                unit = "g"
            )
        )
        val summary = order.generateFormattedSummary(usages)
        assertTrue(summary.contains("Clara Vance"))
        assertTrue(summary.contains("Pressed Floral Tier"))
        assertTrue(summary.contains("Lemon Elderflower"))
        assertTrue(summary.contains("90 servings"))
        assertTrue(summary.contains("Botanical Conservatory"))
        assertTrue(summary.contains("$500.00"))
        assertTrue(summary.contains("$200.00"))
        assertTrue(summary.contains("$300.00"))
        assertTrue(summary.contains("Bleached Swan Cake Flour: 950 g"))
    }

    @Test
    fun ingredientInventory_detectsReorderPointAndCustomerHistory() {
        val lowItem = IngredientItem(
            id = 1L,
            name = "Unsalted Butter",
            category = "Dairy & Butter",
            currentStock = 1200.0,
            unit = "g",
            reorderPoint = 1500.0
        )
        val stockedItem = IngredientItem(
            id = 2L,
            name = "Caster Sugar",
            category = "Sugar & Sweeteners",
            currentStock = 5000.0,
            unit = "g",
            reorderPoint = 2000.0
        )
        assertTrue(lowItem.isLowStock)
        assertFalse(stockedItem.isLowStock)

        val customer = CustomerProfile(
            id = 10L,
            name = "Elena Rostova",
            phone = "555-8765",
            email = "elena@example.com",
            address = "742 Rosewood Terrace"
        )
        val order = CakeOrder(
            id = 1L,
            customerId = 10L,
            clientName = "Elena Rostova",
            clientPhone = "555-8765",
            eventType = "Birthday",
            orderStatus = OrderStatus.DELIVERED.name,
            designTitle = "Vintage Heart Lambeth",
            designStylePreset = DesignStylePreset.LAMBETH_VINTAGE.name,
            cakeFlavor = "Madagascar Vanilla Bean",
            fillingFlavor = "Raspberry Compote",
            frostingType = "Buttercream",
            colorPaletteCsv = "#F4C2C2",
            inscriptionText = "Happy Birthday",
            dietaryTagsCsv = "",
            designNotes = "",
            cakeShape = CakeShape.HEART.name,
            cutStyle = CutStyle.PARTY.name,
            tierSizesCsv = "8,6",
            estimatedServings = 28,
            fulfillmentType = FulfillmentType.PICKUP.name,
            deliveryDateMillis = 1790000000000L,
            deliveryTimeWindow = "11:00 AM",
            deliveryAddress = "Studio Pickup",
            deliveryNotes = "",
            totalPrice = 215.0,
            depositRequired = 100.0,
            amountPaid = 215.0,
            paymentMethod = "Venmo"
        )
        val uiState = CakePlannerUiState(
            allOrders = listOf(order),
            allCustomers = listOf(customer),
            allIngredients = listOf(lowItem, stockedItem)
        )
        assertEquals(1, uiState.ordersForCustomer(customer).size)
        assertEquals(1, uiState.lowStockIngredientsCount)
    }

    @Test
    fun monthlyRevenueCalculator_aggregatesCompletedAndPipelineOrdersByMonth() {
        val refTime = 1790000000000L
        val completedOrder = CakeOrder(
            id = 10L,
            clientName = "Clara Vance",
            clientPhone = "555-0199",
            eventType = "Wedding",
            orderStatus = OrderStatus.DELIVERED.name,
            designTitle = "3-Tier Botanical",
            designStylePreset = DesignStylePreset.BOTANICAL_FLORAL.name,
            cakeFlavor = "Lemon Elderflower",
            fillingFlavor = "Lemon Curd",
            frostingType = "Buttercream",
            colorPaletteCsv = "#FFF5E8",
            inscriptionText = "C & J",
            dietaryTagsCsv = "",
            designNotes = "",
            cakeShape = CakeShape.ROUND.name,
            cutStyle = CutStyle.WEDDING.name,
            tierSizesCsv = "10,8,6",
            estimatedServings = 90,
            fulfillmentType = FulfillmentType.DELIVERY.name,
            deliveryDateMillis = refTime,
            deliveryTimeWindow = "1:00 PM",
            deliveryAddress = "Conservatory",
            deliveryNotes = "",
            totalPrice = 890.0,
            depositRequired = 445.0,
            amountPaid = 890.0,
            paymentMethod = "Zelle"
        )
        val trends = MonthlyRevenueCalculator.buildMonthlyTrends(
            orders = listOf(completedOrder),
            referenceTimeMillis = refTime,
            monthCount = 6
        )
        assertEquals(6, trends.size)
        assertEquals(890.0, trends.last().completedRevenue, 0.01)
        assertEquals(1, trends.last().completedOrdersCount)
    }

    @Test
    fun geminiFlavorSommelier_parsesStructuredJsonSuggestions() {
        val sampleJson = """
            [
              {
                "pairingTitle": "Spiced Fig & Brown Butter Hazelnut",
                "spongeFlavor": "Brown Butter Hazelnut Sponge",
                "fillingFlavor": "Roasted Mission Fig & Honey Compote",
                "frostingType": "Whipped Mascarpone Swiss Meringue",
                "tastingNotes": "Nutty toasted brown butter balances the jammy sweetness of figs.",
                "recommendedColorsHex": ["#FFF5E8", "#E28B76", "#E6C27A"],
                "designTip": "Garnish top tier with halved fresh figs and gold leaf."
              }
            ]
        """.trimIndent()
        val parsed = GeminiFlavorSommelierClient.parseFlavorSuggestionsJson(sampleJson)
        assertEquals(1, parsed.size)
        assertEquals("Spiced Fig & Brown Butter Hazelnut", parsed.first().pairingTitle)
        assertEquals("Brown Butter Hazelnut Sponge", parsed.first().spongeFlavor)
        assertEquals(3, parsed.first().recommendedColorsHex.size)
    }

    @Test
    fun voiceDictationFormatter_formatsAndAppendsTaggedNotes() {
        val initial = VoiceDictationFormatter.appendDictatedSegment(
            existingNotes = "",
            spokenTranscript = "add double drop string lambeth ruffles in blush rose",
            tag = DictationNoteTag.DESIGN_SPEC
        )
        assertEquals("[Design Spec] Add double drop string lambeth ruffles in blush rose.", initial)

        val second = VoiceDictationFormatter.appendDictatedSegment(
            existingNotes = initial,
            spokenTranscript = "client requested less sweet swiss meringue",
            tag = DictationNoteTag.CLIENT_FEEDBACK
        )
        assertTrue(second.contains("[Design Spec] Add double drop string lambeth ruffles in blush rose."))
        assertTrue(second.contains("[Client Feedback] Client requested less sweet swiss meringue."))
    }

    @Test
    fun firebaseAiSecurityAndPlayStoreCatalog_provideProductionSecurityAndAssets() {
        val status = FirebaseAiSecurityManager.getStatus()
        assertTrue(status.isClientKeyRemovedFromApk)
        assertEquals("gemini-3.5-flash", status.modelName)
        assertTrue(status.aiBackendName.contains("Firebase AI"))

        assertEquals(4, PlayStoreLaunchGuideCatalog.graphicAssetSpecs.size)
        assertEquals(5, PlayStoreLaunchGuideCatalog.screenshotStoryboard.size)

        val policy = PlayStoreLaunchGuideCatalog.generatePrivacyPolicyTemplate()
        assertTrue(policy.contains("com.aistudio.crumbtier.p8k4wm"))
        assertTrue(policy.contains("RECORD_AUDIO"))
        assertTrue(policy.contains("Firebase App Check"))
    }
}
