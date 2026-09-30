package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.CakeOrder
import com.example.data.CakeShape
import com.example.data.CustomerProfile
import com.example.data.CutStyle
import com.example.data.DesignStylePreset
import com.example.data.FlavorSommelierUiState
import com.example.data.FulfillmentType
import com.example.data.IngredientItem
import com.example.data.OrderStatus
import com.example.data.PatisserieCatalog
import com.example.data.TierServingCalculator
import com.example.ui.components.GeminiFlavorSommelierCard
import com.example.ui.components.TierBlueprintCanvas
import com.example.ui.components.VoiceDictationNotesCard
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun OrderEditorScreen(
    initialOrder: CakeOrder?,
    savedCustomers: List<CustomerProfile> = emptyList(),
    pantryIngredients: List<IngredientItem> = emptyList(),
    sommelierState: FlavorSommelierUiState = FlavorSommelierUiState.Idle,
    onRequestFlavorSuggestions: (String, String, List<String>) -> Unit = { _, _, _ -> },
    onCancel: () -> Unit,
    onSave: (CakeOrder) -> Unit
) {
    BackHandler(onBack = onCancel)

    val isEditing = initialOrder != null && initialOrder.id != 0L
    val now = remember { System.currentTimeMillis() }
    val dayMs = TimeUnit.DAYS.toMillis(1)

    var selectedCustomerId by remember { mutableStateOf(initialOrder?.customerId) }
    var clientName by remember { mutableStateOf(initialOrder?.clientName ?: "") }
    var clientPhone by remember { mutableStateOf(initialOrder?.clientPhone ?: "") }
    var clientEmail by remember { mutableStateOf(initialOrder?.clientEmail ?: "") }
    var eventType by remember { mutableStateOf(initialOrder?.eventType ?: "Birthday") }
    var orderStatus by remember {
        mutableStateOf(initialOrder?.statusEnum ?: OrderStatus.INQUIRY)
    }

    // Design State
    var designTitle by remember { mutableStateOf(initialOrder?.designTitle ?: "") }
    var stylePreset by remember {
        mutableStateOf(initialOrder?.stylePresetEnum ?: DesignStylePreset.LAMBETH_VINTAGE)
    }
    var customPhotoUri by remember { mutableStateOf(initialOrder?.customPhotoUri) }
    var cakeFlavor by remember {
        mutableStateOf(initialOrder?.cakeFlavor ?: PatisserieCatalog.flavors.first())
    }
    var fillingFlavor by remember {
        mutableStateOf(initialOrder?.fillingFlavor ?: PatisserieCatalog.fillings.first())
    }
    var frostingType by remember {
        mutableStateOf(initialOrder?.frostingType ?: PatisserieCatalog.frostings.first())
    }
    val selectedColors = remember {
        mutableStateListOf<String>().apply {
            addAll(initialOrder?.colorHexList ?: listOf("#FFF5E8", "#F4C2C2", "#E6C27A"))
        }
    }
    var inscriptionText by remember { mutableStateOf(initialOrder?.inscriptionText ?: "") }
    val selectedDietaryTags = remember {
        mutableStateListOf<String>().apply {
            addAll(initialOrder?.dietaryTagsList ?: emptyList())
        }
    }
    var designNotes by remember { mutableStateOf(initialOrder?.designNotes ?: "") }

    // Serving Size & Tiers State
    var cakeShape by remember { mutableStateOf(initialOrder?.shapeEnum ?: CakeShape.ROUND) }
    var cutStyle by remember { mutableStateOf(initialOrder?.cutStyleEnum ?: CutStyle.PARTY) }
    val tierSizes = remember {
        mutableStateListOf<Int>().apply {
            addAll(initialOrder?.tierSizesList ?: listOf(8, 6))
        }
    }
    var tierHeightInches by remember { mutableIntStateOf(initialOrder?.tierHeightInches ?: 4) }

    val breakdown = remember(tierSizes.toList(), cakeShape, cutStyle, tierHeightInches) {
        TierServingCalculator.calculateCakeBreakdown(
            tierSizesInches = tierSizes.toList(),
            shape = cakeShape,
            cutStyle = cutStyle,
            heightInches = tierHeightInches
        )
    }

    var customServingsOverride by remember {
        mutableStateOf(initialOrder?.estimatedServings?.toString() ?: "")
    }

    // Delivery & Schedule State
    var fulfillmentType by remember {
        mutableStateOf(initialOrder?.fulfillmentEnum ?: FulfillmentType.PICKUP)
    }
    var deliveryDateMillis by remember {
        mutableLongStateOf(initialOrder?.deliveryDateMillis ?: (now + dayMs * 3))
    }
    var deliveryTimeWindow by remember {
        mutableStateOf(initialOrder?.deliveryTimeWindow ?: "11:00 AM - 12:00 PM")
    }
    var deliveryAddress by remember {
        mutableStateOf(initialOrder?.deliveryAddress ?: "Home Bakery Studio Pickup")
    }
    var deliveryNotes by remember { mutableStateOf(initialOrder?.deliveryNotes ?: "") }

    // Pricing & Payments State
    var totalPriceText by remember {
        mutableStateOf(
            initialOrder?.totalPrice?.toInt()?.toString()
                ?: breakdown.suggestedBasePrice.toInt().toString()
        )
    }
    var depositRequiredText by remember {
        mutableStateOf(
            initialOrder?.depositRequired?.toInt()?.toString()
                ?: (breakdown.suggestedBasePrice * 0.5).toInt().toString()
        )
    }
    var amountPaidText by remember {
        mutableStateOf(initialOrder?.amountPaid?.toInt()?.toString() ?: "0")
    }
    var paymentMethod by remember {
        mutableStateOf(initialOrder?.paymentMethod ?: "Venmo")
    }
    var paymentNotes by remember { mutableStateOf(initialOrder?.paymentNotes ?: "") }

    // Zero-permission Android Photo Picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            customPhotoUri = uri.toString()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditing) "Edit Cake Order" else "New Cake Order",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onCancel) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Cancel"
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = {
                            val finalClient = clientName.trim().ifEmpty { "Walk-In Bakery Client" }
                            val finalDesign = designTitle.trim()
                                .ifEmpty { "${tierSizes.size}-Tier ${stylePreset.label}" }
                            val finalServings = customServingsOverride.toIntOrNull()
                                ?: breakdown.totalServings
                            val finalTotal = totalPriceText.toDoubleOrNull()
                                ?: breakdown.suggestedBasePrice
                            val finalDeposit = depositRequiredText.toDoubleOrNull()
                                ?: (finalTotal * 0.5)
                            val finalPaid = (amountPaidText.toDoubleOrNull() ?: 0.0)
                                .coerceIn(0.0, finalTotal)

                            onSave(
                                CakeOrder(
                                    id = initialOrder?.id ?: 0L,
                                    customerId = selectedCustomerId,
                                    clientName = finalClient,
                                    clientPhone = clientPhone.trim(),
                                    clientEmail = clientEmail.trim(),
                                    eventType = eventType,
                                    orderStatus = orderStatus.name,
                                    designTitle = finalDesign,
                                    designStylePreset = stylePreset.name,
                                    customPhotoUri = customPhotoUri,
                                    cakeFlavor = cakeFlavor,
                                    fillingFlavor = fillingFlavor,
                                    frostingType = frostingType,
                                    colorPaletteCsv = selectedColors.joinToString(","),
                                    inscriptionText = inscriptionText.trim(),
                                    dietaryTagsCsv = selectedDietaryTags.joinToString(","),
                                    designNotes = designNotes.trim(),
                                    cakeShape = cakeShape.name,
                                    cutStyle = cutStyle.name,
                                    tierSizesCsv = tierSizes.sortedDescending().joinToString(","),
                                    tierHeightInches = tierHeightInches,
                                    estimatedServings = finalServings,
                                    fulfillmentType = fulfillmentType.name,
                                    deliveryDateMillis = deliveryDateMillis,
                                    deliveryTimeWindow = deliveryTimeWindow,
                                    deliveryAddress = deliveryAddress.trim()
                                        .ifEmpty { fulfillmentType.label },
                                    deliveryNotes = deliveryNotes.trim(),
                                    prepChecklistCsv = initialOrder?.prepChecklistCsv ?: "",
                                    totalPrice = finalTotal,
                                    depositRequired = finalDeposit,
                                    amountPaid = finalPaid,
                                    paymentMethod = paymentMethod,
                                    paymentNotes = paymentNotes.trim()
                                )
                            )
                        },
                        modifier = Modifier
                            .padding(end = 12.dp)
                            .testTag("save_order_button")
                    ) {
                        Text(stringResource(R.string.action_save_order))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // SECTION 1: Client & Event Details
            item {
                EditorSectionCard(title = "1. Client & Celebration") {
                    if (savedCustomers.isNotEmpty()) {
                        Text(
                            text = "Quick-Fill from Saved Customer Profiles",
                            style = MaterialTheme.typography.labelLarge
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            savedCustomers.forEach { customer ->
                                FilterChip(
                                    selected = selectedCustomerId == customer.id,
                                    onClick = {
                                        selectedCustomerId = customer.id
                                        clientName = customer.name
                                        clientPhone = customer.phone
                                        clientEmail = customer.email
                                        if (customer.address.isNotBlank()) {
                                            deliveryAddress = customer.address
                                        }
                                    },
                                    label = { Text(customer.name) }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = clientName,
                        onValueChange = { clientName = it },
                        label = { Text("Client Name") },
                        placeholder = { Text("e.g., Clara Vance") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_client_name")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = clientPhone,
                            onValueChange = { clientPhone = it },
                            label = { Text("Phone Number") },
                            placeholder = { Text("(555) 234-5678") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = clientEmail,
                            onValueChange = { clientEmail = it },
                            label = { Text("Email Address") },
                            placeholder = { Text("clara@example.com") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_client_email")
                        )
                    }

                    Text(
                        text = "Celebration Type",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PatisserieCatalog.eventTypes.forEach { evt ->
                            FilterChip(
                                selected = eventType == evt,
                                onClick = { eventType = evt },
                                label = { Text(evt) }
                            )
                        }
                    }

                    Text(
                        text = "Production Stage",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OrderStatus.entries.forEach { st ->
                            FilterChip(
                                selected = orderStatus == st,
                                onClick = { orderStatus = st },
                                label = { Text(st.label) }
                            )
                        }
                    }
                }
            }

            // SECTION 2: Cake Design, Flavors & Frosting Palette
            item {
                EditorSectionCard(title = "2. Cake Design, Flavors & Palette") {
                    GeminiFlavorSommelierCard(
                        sommelierState = sommelierState,
                        eventType = eventType,
                        dietaryTags = selectedDietaryTags.toList(),
                        pantryIngredients = pantryIngredients,
                        onRequestSuggestions = onRequestFlavorSuggestions,
                        onApplySuggestion = { suggestion ->
                            if (designTitle.isBlank()) {
                                designTitle = suggestion.pairingTitle
                            }
                            cakeFlavor = suggestion.spongeFlavor
                            fillingFlavor = suggestion.fillingFlavor
                            frostingType = suggestion.frostingType
                            val validHexes = suggestion.recommendedColorsHex.filter { it.startsWith("#") }
                            if (validHexes.isNotEmpty()) {
                                selectedColors.clear()
                                selectedColors.addAll(validHexes.take(4))
                            }
                            val combinedNote = listOf(suggestion.tastingNotes, suggestion.designTip)
                                .filter { it.isNotBlank() }
                                .joinToString(" • ")
                            if (combinedNote.isNotBlank()) {
                                designNotes = combinedNote
                            }
                        },
                        applyButtonLabel = "Apply to This Cake Order"
                    )

                    OutlinedTextField(
                        value = designTitle,
                        onValueChange = { designTitle = it },
                        label = { Text("Cake Design Title") },
                        placeholder = { Text("e.g., Vintage Heart Coquette Lambeth") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_design_title")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Design Style & Reference Photo",
                            style = MaterialTheme.typography.labelLarge
                        )
                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier.testTag("pick_reference_photo_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (customPhotoUri != null) "Change Photo" else stringResource(R.string.action_pick_photo)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DesignStylePreset.entries.forEach { preset ->
                            FilterChip(
                                selected = stylePreset == preset,
                                onClick = { stylePreset = preset },
                                label = { Text(preset.label) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = cakeFlavor,
                        onValueChange = { cakeFlavor = it },
                        label = { Text("Sponge Flavor") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_cake_flavor")
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PatisserieCatalog.flavors.forEach { flavor ->
                            FilterChip(
                                selected = cakeFlavor == flavor,
                                onClick = { cakeFlavor = flavor },
                                label = { Text(flavor) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = fillingFlavor,
                        onValueChange = { fillingFlavor = it },
                        label = { Text("Filling / Compote / Curd") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_filling_flavor")
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PatisserieCatalog.fillings.forEach { fill ->
                            FilterChip(
                                selected = fillingFlavor == fill,
                                onClick = { fillingFlavor = fill },
                                label = { Text(fill) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = frostingType,
                        onValueChange = { frostingType = it },
                        label = { Text("Frosting Finish") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_frosting_type")
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PatisserieCatalog.frostings.forEach { frost ->
                            FilterChip(
                                selected = frostingType == frost,
                                onClick = { frostingType = frost },
                                label = { Text(frost) }
                            )
                        }
                    }

                    Text(
                        text = "Frosting Color Swatches",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PatisserieCatalog.colorSwatches.forEach { swatch ->
                            val isSelected = selectedColors.contains(swatch.hex)
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(swatch.color)
                                    .border(
                                        width = if (isSelected) 2.5.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.Gray,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        if (isSelected && selectedColors.size > 1) {
                                            selectedColors.remove(swatch.hex)
                                        } else if (!isSelected && selectedColors.size < 4) {
                                            selectedColors.add(swatch.hex)
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = swatch.name,
                                        tint = Color(0xFF241916),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = inscriptionText,
                        onValueChange = { inscriptionText = it },
                        label = { Text("Piped Inscription / Topper Text") },
                        placeholder = { Text("e.g., Happy 30th Elena!") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_inscription")
                    )

                    Text(
                        text = "Dietary & Allergen Tags",
                        style = MaterialTheme.typography.labelLarge
                    )
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        PatisserieCatalog.dietaryTags.forEach { tag ->
                            val selected = selectedDietaryTags.contains(tag)
                            FilterChip(
                                selected = selected,
                                onClick = {
                                    if (selected) selectedDietaryTags.remove(tag)
                                    else selectedDietaryTags.add(tag)
                                },
                                label = { Text(tag) }
                            )
                        }
                    }

                    VoiceDictationNotesCard(
                        notesText = designNotes,
                        onNotesChange = { designNotes = it },
                        title = "Voice-to-Text Design Requirements & Client Feedback",
                        subtitle = "Tap 'Dictate Note' to speak piping details, color tweaks, or client feedback hands-free."
                    )
                }
            }

            // SECTION 3: Serving Size & Tier Architect
            item {
                EditorSectionCard(title = "3. Tier Architecture & Serving Size") {
                    TierBlueprintCanvas(
                        tierSizesInches = tierSizes.toList(),
                        tierHeightInches = tierHeightInches,
                        shape = cakeShape,
                        stylePreset = stylePreset,
                        colorHexes = selectedColors.toList(),
                        inscriptionText = inscriptionText
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CakeShape.entries.forEach { sh ->
                            FilterChip(
                                selected = cakeShape == sh,
                                onClick = { cakeShape = sh },
                                label = { Text(sh.label) }
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CutStyle.entries.forEach { cut ->
                            FilterChip(
                                selected = cutStyle == cut,
                                onClick = { cutStyle = cut },
                                label = { Text(cut.label) }
                            )
                        }
                    }

                    Text(
                        text = "Add Tier Diameter (Max 4 Tiers)",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TierServingCalculator.availableSizes.forEach { inch ->
                            AssistChip(
                                onClick = {
                                    if (tierSizes.size < 4) {
                                        tierSizes.add(inch)
                                        val newCalc = TierServingCalculator.calculateCakeBreakdown(
                                            tierSizes.toList(), cakeShape, cutStyle, tierHeightInches
                                        )
                                        customServingsOverride = newCalc.totalServings.toString()
                                    }
                                },
                                label = { Text("+ ${inch}\"") }
                            )
                        }
                    }

                    // Active Tiers Chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        tierSizes.sortedDescending().forEachIndexed { idx, sizeInch ->
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.secondaryContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "Tier ${idx + 1}: ${sizeInch}\"",
                                        style = MaterialTheme.typography.labelLarge
                                    )
                                    if (tierSizes.size > 1) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove tier",
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clickable {
                                                    tierSizes.remove(sizeInch)
                                                    val newCalc = TierServingCalculator.calculateCakeBreakdown(
                                                        tierSizes.toList(), cakeShape, cutStyle, tierHeightInches
                                                    )
                                                    customServingsOverride = newCalc.totalServings.toString()
                                                }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    OutlinedTextField(
                        value = customServingsOverride.ifEmpty { breakdown.totalServings.toString() },
                        onValueChange = { customServingsOverride = it },
                        label = { Text("Estimated Servings (Auto-computed: ${breakdown.totalServings})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_servings")
                    )
                }
            }

            // SECTION 4: Delivery Time & Payment Quote
            item {
                EditorSectionCard(title = "4. Delivery Schedule & Payments") {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FulfillmentType.entries.forEach { type ->
                            FilterChip(
                                selected = fulfillmentType == type,
                                onClick = {
                                    fulfillmentType = type
                                    if (type == FulfillmentType.PICKUP && deliveryAddress.isBlank()) {
                                        deliveryAddress = "Home Bakery Studio Pickup"
                                    }
                                },
                                label = { Text(type.label) }
                            )
                        }
                    }

                    Text(
                        text = "Select Event / Delivery Date",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(1, 2, 3, 5, 7, 14).forEach { daysAhead ->
                            val targetMillis = now + dayMs * daysAhead
                            val label = formatOrderDate(targetMillis)
                            val isSelected = kotlin.math.abs(deliveryDateMillis - targetMillis) < dayMs / 2
                            FilterChip(
                                selected = isSelected,
                                onClick = { deliveryDateMillis = targetMillis },
                                label = { Text("In $daysAhead d ($label)") }
                            )
                        }
                    }

                    Text(
                        text = "Delivery / Pickup Time Window",
                        style = MaterialTheme.typography.labelLarge
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf(
                            "9:30 AM - 10:30 AM",
                            "11:00 AM - 12:00 PM",
                            "1:30 PM - 2:30 PM",
                            "4:00 PM - 5:00 PM"
                        ).forEach { slot ->
                            FilterChip(
                                selected = deliveryTimeWindow == slot,
                                onClick = { deliveryTimeWindow = slot },
                                label = { Text(slot) }
                            )
                        }
                    }

                    OutlinedTextField(
                        value = deliveryAddress,
                        onValueChange = { deliveryAddress = it },
                        label = { Text("Venue Address or Pickup Instructions") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_delivery_address")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = totalPriceText,
                            onValueChange = { totalPriceText = it },
                            label = { Text("Total Price ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_total_price")
                        )
                        OutlinedTextField(
                            value = depositRequiredText,
                            onValueChange = { depositRequiredText = it },
                            label = { Text("Deposit Req ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = amountPaidText,
                            onValueChange = { amountPaidText = it },
                            label = { Text("Paid ($)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("input_amount_paid")
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        PatisserieCatalog.paymentMethods.forEach { method ->
                            FilterChip(
                                selected = paymentMethod == method,
                                onClick = { paymentMethod = method },
                                label = { Text(method) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditorSectionCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            content()
        }
    }
}
