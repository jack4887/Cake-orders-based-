package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.CakeOrder
import com.example.data.CustomerProfile
import com.example.ui.CakePlannerUiState
import com.example.ui.components.PosMetricBox
import com.example.ui.components.PosTouchButton

@Composable
fun CustomerProfilesScreen(
    uiState: CakePlannerUiState,
    onSaveCustomer: (CustomerProfile) -> Unit,
    onDeleteCustomer: (CustomerProfile) -> Unit,
    onNewOrderForCustomer: (CustomerProfile) -> Unit,
    onOpenOrderDetail: (CakeOrder) -> Unit,
    onOpenOrderSummary: (CakeOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var editingCustomer by remember { mutableStateOf<CustomerProfile?>(null) }
    var isCustomerDialogOpen by remember { mutableStateOf(false) }

    val filteredCustomers = remember(uiState.allCustomers, searchQuery) {
        val q = searchQuery.trim().lowercase()
        if (q.isEmpty()) {
            uiState.allCustomers
        } else {
            uiState.allCustomers.filter { c ->
                c.name.lowercase().contains(q) ||
                    c.phone.lowercase().contains(q) ||
                    c.email.lowercase().contains(q) ||
                    c.address.lowercase().contains(q) ||
                    c.notes.lowercase().contains(q)
            }
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("customer_profiles_screen"),
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
                        text = "CLIENT DIRECTORY & ORDER HISTORY",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Customer Profiles",
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Text(
                        text = "Store contact info, delivery addresses, dietary notes, and complete cake order history.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                PosTouchButton(
                    text = "Add Client",
                    icon = Icons.Default.PersonAdd,
                    onClick = {
                        editingCustomer = null
                        isCustomerDialogOpen = true
                    },
                    minHeight = 46.dp,
                    modifier = Modifier.testTag("new_customer_button")
                )
            }
        }

        // Summary KPI Row (Pure White Boxes with Crisp Borders)
        item {
            val repeatCustomersCount = uiState.allCustomers.count {
                uiState.ordersForCustomer(it).size > 1
            }
            val totalSpendAll = uiState.allOrders.sumOf { it.totalPrice }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                PosMetricBox(
                    label = "Saved Profiles",
                    value = "${uiState.allCustomers.size} Clients",
                    modifier = Modifier.weight(1f)
                )
                PosMetricBox(
                    label = "Repeat Clients",
                    value = "$repeatCustomersCount VIPs",
                    accentColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
                PosMetricBox(
                    label = "Lifetime Orders",
                    value = formatCurrency(totalSpendAll),
                    accentColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Search input
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_customers_input"),
                placeholder = { Text("Search customer name, email, phone, or address…") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search customers")
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )
        }

        if (filteredCustomers.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No customer profiles found",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "Tap Add Client to create a customer profile with phone, email, and delivery address.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            items(filteredCustomers, key = { it.id }) { customer ->
                val customerOrders = uiState.ordersForCustomer(customer)
                CustomerProfileCard(
                    customer = customer,
                    orders = customerOrders,
                    onEditCustomer = {
                        editingCustomer = customer
                        isCustomerDialogOpen = true
                    },
                    onDeleteCustomer = { onDeleteCustomer(customer) },
                    onNewOrderForCustomer = { onNewOrderForCustomer(customer) },
                    onOpenOrderDetail = onOpenOrderDetail,
                    onOpenOrderSummary = onOpenOrderSummary,
                    modifier = Modifier.testTag("customer_card_${customer.id}")
                )
            }
        }
    }

    if (isCustomerDialogOpen) {
        CustomerEditorDialog(
            initialCustomer = editingCustomer,
            onDismiss = { isCustomerDialogOpen = false },
            onSave = { saved ->
                onSaveCustomer(saved)
                isCustomerDialogOpen = false
            }
        )
    }
}

@Composable
private fun CustomerSummaryStat(label: String, value: String) {
    Column {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
        )
    }
}

@Composable
private fun CustomerProfileCard(
    customer: CustomerProfile,
    orders: List<CakeOrder>,
    onEditCustomer: () -> Unit,
    onDeleteCustomer: () -> Unit,
    onNewOrderForCustomer: () -> Unit,
    onOpenOrderDetail: (CakeOrder) -> Unit,
    onOpenOrderSummary: (CakeOrder) -> Unit,
    modifier: Modifier = Modifier
) {
    var isHistoryExpanded by remember { mutableStateOf(true) }
    val totalSpent = orders.sumOf { it.totalPrice }
    val initials = customer.name
        .split(" ")
        .filter { it.isNotBlank() && it.first().isLetter() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifEmpty { "CB" }

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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Header: Avatar + Name + Contact Info + Edit/Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = initials,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        fontWeight = FontWeight.Bold
                    )
                }

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = customer.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    if (customer.phone.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Phone,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = customer.phone,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    if (customer.email.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = customer.email,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    if (customer.address.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = customer.address,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row {
                    IconButton(
                        onClick = onEditCustomer,
                        modifier = Modifier.testTag("edit_customer_${customer.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Customer"
                        )
                    }
                    IconButton(
                        onClick = onDeleteCustomer,
                        modifier = Modifier.testTag("delete_customer_${customer.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete Customer"
                        )
                    }
                }
            }

            if (customer.notes.isNotBlank()) {
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Client Notes: ${customer.notes}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Order History Header & Toggle
            Surface(
                onClick = { isHistoryExpanded = !isHistoryExpanded },
                shape = RoundedCornerShape(10.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Cake Order History (${orders.size} Orders • ${formatCurrency(totalSpent)})",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Icon(
                        imageVector = if (isHistoryExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = "Toggle order history"
                    )
                }
            }

            AnimatedVisibility(visible = isHistoryExpanded) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (orders.isEmpty()) {
                        Text(
                            text = "No cake orders recorded for ${customer.name} yet.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        )
                    } else {
                        orders.forEach { order ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surface,
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenOrderDetail(order) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                                        ) {
                                            Text(
                                                text = order.designTitle,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            StatusBadge(status = order.statusEnum)
                                        }
                                        Text(
                                            text = "${order.eventType} • ${order.cakeFlavor} • ${order.estimatedServings} srv",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = "${formatOrderDate(order.deliveryDateMillis)} • ${formatCurrency(order.totalPrice)} (${order.paymentStatus.label})",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                    PosTouchButton(
                                        text = "Summary",
                                        icon = Icons.Default.Description,
                                        onClick = { onOpenOrderSummary(order) },
                                        containerColor = MaterialTheme.colorScheme.surface,
                                        contentColor = MaterialTheme.colorScheme.onSurface,
                                        borderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.6f),
                                        minHeight = 44.dp,
                                        modifier = Modifier.testTag("customer_order_summary_${order.id}")
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Action Row: Book New Order for this Customer
            PosTouchButton(
                text = "New Order for ${customer.name.substringBefore(" ")}",
                icon = Icons.Default.Cake,
                onClick = onNewOrderForCustomer,
                minHeight = 46.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("new_order_for_customer_${customer.id}")
            )
        }
    }
}

@Composable
fun CustomerEditorDialog(
    initialCustomer: CustomerProfile?,
    onDismiss: () -> Unit,
    onSave: (CustomerProfile) -> Unit
) {
    var name by remember(initialCustomer) { mutableStateOf(initialCustomer?.name ?: "") }
    var phone by remember(initialCustomer) { mutableStateOf(initialCustomer?.phone ?: "") }
    var email by remember(initialCustomer) { mutableStateOf(initialCustomer?.email ?: "") }
    var address by remember(initialCustomer) { mutableStateOf(initialCustomer?.address ?: "") }
    var notes by remember(initialCustomer) { mutableStateOf(initialCustomer?.notes ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (initialCustomer == null) "New Customer Profile" else "Edit Customer Profile",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Customer Full Name *") },
                    placeholder = { Text("e.g., Camille Laurent") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_name_input")
                )
                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    placeholder = { Text("(555) 321-9876") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_phone_input")
                )
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    placeholder = { Text("camille@example.com") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_email_input")
                )
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Default Delivery Address") },
                    placeholder = { Text("124 Rosemont Ave") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_address_input")
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Dietary Preferences & Favorite Flavors") },
                    placeholder = { Text("e.g., Prefers Swiss meringue, nut-free kitchen") },
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("customer_notes_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val cleanName = name.trim().ifEmpty { "Bakery Client" }
                    onSave(
                        CustomerProfile(
                            id = initialCustomer?.id ?: 0L,
                            name = cleanName,
                            phone = phone.trim(),
                            email = email.trim(),
                            address = address.trim(),
                            notes = notes.trim()
                        )
                    )
                },
                modifier = Modifier.testTag("save_customer_button")
            ) {
                Text("Save Profile")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
