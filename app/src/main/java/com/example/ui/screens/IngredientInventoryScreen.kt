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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.data.IngredientItem
import com.example.data.OrderIngredientUsage
import com.example.data.PatisserieCatalog
import com.example.ui.CakePlannerUiState
import com.example.ui.components.PosFilterKeypadButton
import com.example.ui.components.PosTouchButton

@Composable
fun IngredientInventoryScreen(
    uiState: CakePlannerUiState,
    onSaveIngredient: (IngredientItem) -> Unit,
    onAdjustStock: (IngredientItem, Double) -> Unit,
    onDeleteIngredient: (IngredientItem) -> Unit,
    onRecordOrderUsage: (Long, IngredientItem, Double) -> Unit,
    onRemoveOrderUsage: (OrderIngredientUsage) -> Unit,
    onAutoAllocateOrderRecipe: (CakeOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showLowStockOnly by remember { mutableStateOf(false) }
    var editingIngredient by remember { mutableStateOf<IngredientItem?>(null) }
    var isIngredientDialogOpen by remember { mutableStateOf(false) }
    var usageDialogIngredient by remember { mutableStateOf<IngredientItem?>(null) }
    var selectedOrderForUsage by remember(uiState.allOrders) {
        mutableStateOf(uiState.allOrders.firstOrNull())
    }

    val filteredIngredients = remember(
        uiState.allIngredients,
        selectedCategory,
        showLowStockOnly
    ) {
        uiState.allIngredients.filter { item ->
            val matchesCat = selectedCategory == null || item.category == selectedCategory
            val matchesLow = !showLowStockOnly || item.isLowStock
            matchesCat && matchesLow
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("ingredient_inventory_screen"),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 12.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "BAKERY PANTRY & STOCK LEDGER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ingredient Inventory",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Monitor current stock levels, reorder points, and track ingredient usage per cake order.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                PosTouchButton(
                    text = "Add Stock",
                    icon = Icons.Default.Add,
                    onClick = {
                        editingIngredient = null
                        isIngredientDialogOpen = true
                    },
                    minHeight = 46.dp,
                    modifier = Modifier.testTag("add_ingredient_button")
                )
            }
        }

        // Low-Stock & Pantry Summary Banner (Pure White Card with Crisp Border)
        item {
            val lowStockCount = uiState.lowStockIngredientsCount
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
                            Icon(
                                imageVector = if (lowStockCount > 0) {
                                    Icons.Default.WarningAmber
                                } else {
                                    Icons.Default.Inventory2
                                },
                                contentDescription = null,
                                tint = if (lowStockCount > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.secondary
                            )
                            Column {
                                Text(
                                    text = if (lowStockCount > 0) {
                                        "$lowStockCount Ingredients Below Reorder Point"
                                    } else {
                                        "All Pantry Stock Above Reorder Point"
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${uiState.allIngredients.size} tracked pantry items • ${uiState.allIngredientUsages.size} logged order deductions",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        PosFilterKeypadButton(
                            label = "Low Stock ($lowStockCount)",
                            selected = showLowStockOnly,
                            onClick = { showLowStockOnly = !showLowStockOnly },
                            activeColor = MaterialTheme.colorScheme.error,
                            modifier = Modifier.testTag("filter_low_stock_chip")
                        )
                    }
                }
            }
        }

        // Category Filter Row
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PosFilterKeypadButton(
                    label = "All Categories",
                    selected = selectedCategory == null,
                    onClick = { selectedCategory = null }
                )
                PatisserieCatalog.ingredientCategories.forEach { cat ->
                    PosFilterKeypadButton(
                        label = cat,
                        selected = selectedCategory == cat,
                        onClick = {
                            selectedCategory = if (selectedCategory == cat) null else cat
                        }
                    )
                }
            }
        }

        // Ingredient Cards
        items(filteredIngredients, key = { it.id }) { item ->
            IngredientStockCard(
                item = item,
                onAdjustStock = { delta -> onAdjustStock(item, delta) },
                onLogUsage = { usageDialogIngredient = item },
                onEdit = {
                    editingIngredient = item
                    isIngredientDialogOpen = true
                },
                onDelete = { onDeleteIngredient(item) },
                modifier = Modifier.testTag("ingredient_card_${item.id}")
            )
        }

        // Per-Order Ingredient Usage Tracker Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "INGREDIENT USAGE PER CAKE ORDER",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Order Recipe Allocation & Deduction Log",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Select an order to view its ingredient usage or auto-deduct flour, butter, sugar, and vanilla based on its tier sizes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.allOrders.forEach { order ->
                            val isSelected = selectedOrderForUsage?.id == order.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedOrderForUsage = order },
                                label = {
                                    Text("${order.clientName.substringBefore(" ")}: ${order.designTitle.take(18)}")
                                }
                            )
                        }
                    }

                    val targetOrder = selectedOrderForUsage ?: uiState.allOrders.firstOrNull()
                    if (targetOrder != null) {
                        val orderUsages = uiState.usagesForOrder(targetOrder.id)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${targetOrder.designTitle} (${targetOrder.estimatedServings} srv)",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${orderUsages.size} ingredient batches logged for this order",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { onAutoAllocateOrderRecipe(targetOrder) },
                                modifier = Modifier.testTag("auto_allocate_recipe_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoFixHigh,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-Log Batch")
                            }
                        }

                        if (orderUsages.isEmpty()) {
                            Text(
                                text = "No ingredients deducted for this order yet. Tap 'Log Usage' on any ingredient above or 'Auto-Log Batch'.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            orderUsages.forEach { usage ->
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(
                                                text = usage.ingredientName,
                                                style = MaterialTheme.typography.titleSmall
                                            )
                                            Text(
                                                text = "Used: ${usage.quantityUsed.toInt()} ${usage.unit}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                        IconButton(onClick = { onRemoveOrderUsage(usage) }) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Undo usage and restore stock"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (isIngredientDialogOpen) {
        IngredientEditorDialog(
            initialItem = editingIngredient,
            onDismiss = { isIngredientDialogOpen = false },
            onSave = { saved ->
                onSaveIngredient(saved)
                isIngredientDialogOpen = false
            }
        )
    }

    val targetIng = usageDialogIngredient
    if (targetIng != null) {
        RecordOrderUsageDialog(
            ingredient = targetIng,
            orders = uiState.allOrders,
            defaultOrder = selectedOrderForUsage,
            onDismiss = { usageDialogIngredient = null },
            onConfirm = { orderId, qty ->
                onRecordOrderUsage(orderId, targetIng, qty)
                usageDialogIngredient = null
            }
        )
    }
}

@Composable
private fun IngredientStockCard(
    item: IngredientItem,
    onAdjustStock: (Double) -> Unit,
    onLogUsage: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val stepAmount = when (item.unit) {
        "g", "ml" -> 250.0
        "kg", "lb" -> 1.0
        else -> 2.0
    }

    Card(
        modifier = modifier.fillMaxWidth(),
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
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = item.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        if (item.isLowStock) {
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = MaterialTheme.colorScheme.errorContainer
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Text(
                                        text = "REORDER NOW",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                }

                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Ingredient")
                    }
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.DeleteOutline, contentDescription = "Delete Ingredient")
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "In Stock: ${item.currentStock.toInt()} ${item.unit}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (item.isLowStock) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    }
                )
                Text(
                    text = "Reorder Point: ${item.reorderPoint.toInt()} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            LinearProgressIndicator(
                progress = { item.stockRatio },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(7.dp)
                    .clip(RoundedCornerShape(50)),
                color = if (item.isLowStock) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.secondary
                },
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PosTouchButton(
                    text = "-${stepAmount.toInt()} ${item.unit}",
                    icon = Icons.Default.Remove,
                    onClick = { onAdjustStock(-stepAmount) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.65f),
                    minHeight = 46.dp,
                    modifier = Modifier.weight(1f)
                )
                PosTouchButton(
                    text = "+${stepAmount.toInt()} ${item.unit}",
                    icon = Icons.Default.Add,
                    onClick = { onAdjustStock(stepAmount) },
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.secondary,
                    borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f),
                    minHeight = 46.dp,
                    modifier = Modifier.weight(1f)
                )
                PosTouchButton(
                    text = "Log Usage",
                    icon = Icons.Default.Restaurant,
                    onClick = onLogUsage,
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    minHeight = 46.dp,
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("log_usage_ingredient_${item.id}")
                )
            }
        }
    }
}

@Composable
private fun IngredientEditorDialog(
    initialItem: IngredientItem?,
    onDismiss: () -> Unit,
    onSave: (IngredientItem) -> Unit
) {
    var name by remember(initialItem) { mutableStateOf(initialItem?.name ?: "") }
    var category by remember(initialItem) {
        mutableStateOf(initialItem?.category ?: PatisserieCatalog.ingredientCategories.first())
    }
    var currentStockText by remember(initialItem) {
        mutableStateOf(initialItem?.currentStock?.toInt()?.toString() ?: "1000")
    }
    var reorderPointText by remember(initialItem) {
        mutableStateOf(initialItem?.reorderPoint?.toInt()?.toString() ?: "500")
    }
    var unit by remember(initialItem) {
        mutableStateOf(initialItem?.unit ?: "g")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialItem == null) "Add Pantry Ingredient" else "Edit Ingredient",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Ingredient Name *") },
                    placeholder = { Text("e.g., Sicilian Pistachio Paste") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("ingredient_name_input")
                )

                Text("Category", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PatisserieCatalog.ingredientCategories.forEach { cat ->
                        FilterChip(
                            selected = category == cat,
                            onClick = { category = cat },
                            label = { Text(cat) }
                        )
                    }
                }

                Text("Measurement Unit", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    PatisserieCatalog.ingredientUnits.forEach { u ->
                        FilterChip(
                            selected = unit == u,
                            onClick = { unit = u },
                            label = { Text(u) }
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = currentStockText,
                        onValueChange = { currentStockText = it },
                        label = { Text("Current Stock ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ingredient_stock_input")
                    )
                    OutlinedTextField(
                        value = reorderPointText,
                        onValueChange = { reorderPointText = it },
                        label = { Text("Reorder Point ($unit)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("ingredient_reorder_input")
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim().ifEmpty { "Bakery Ingredient" }
                    val stock = currentStockText.toDoubleOrNull() ?: 0.0
                    val reorder = reorderPointText.toDoubleOrNull() ?: 100.0
                    onSave(
                        IngredientItem(
                            id = initialItem?.id ?: 0L,
                            name = cleanName,
                            category = category,
                            currentStock = stock,
                            unit = unit,
                            reorderPoint = reorder
                        )
                    )
                },
                modifier = Modifier.testTag("save_ingredient_button")
            ) {
                Text("Save Ingredient")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RecordOrderUsageDialog(
    ingredient: IngredientItem,
    orders: List<CakeOrder>,
    defaultOrder: CakeOrder?,
    onDismiss: () -> Unit,
    onConfirm: (Long, Double) -> Unit
) {
    var selectedOrderId by remember(orders, defaultOrder) {
        mutableStateOf(defaultOrder?.id ?: orders.firstOrNull()?.id ?: 0L)
    }
    var quantityText by remember(ingredient) {
        val defaultQty = if (ingredient.unit == "g" || ingredient.unit == "ml") "350" else "1"
        mutableStateOf(defaultQty)
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Log Ingredient Usage",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "${ingredient.name} (Available: ${ingredient.currentStock.toInt()} ${ingredient.unit})",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )

                Text(
                    text = "Select Cake Order",
                    style = MaterialTheme.typography.labelLarge
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    orders.forEach { order ->
                        FilterChip(
                            selected = selectedOrderId == order.id,
                            onClick = { selectedOrderId = order.id },
                            label = { Text("${order.clientName}: ${order.designTitle.take(16)}") }
                        )
                    }
                }

                OutlinedTextField(
                    value = quantityText,
                    onValueChange = { quantityText = it },
                    label = { Text("Quantity Used (${ingredient.unit})") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("usage_quantity_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val qty = quantityText.toDoubleOrNull() ?: 0.0
                    if (selectedOrderId != 0L && qty > 0.0) {
                        onConfirm(selectedOrderId, qty)
                    }
                },
                modifier = Modifier.testTag("confirm_usage_button")
            ) {
                Text("Deduct & Log")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
