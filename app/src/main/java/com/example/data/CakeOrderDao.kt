package com.example.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CakeOrderDao {
    // Orders
    @Query("SELECT * FROM cake_orders ORDER BY deliveryDateMillis ASC")
    fun getAllOrders(): Flow<List<CakeOrder>>

    @Query("SELECT COUNT(*) FROM cake_orders")
    suspend fun getOrderCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: CakeOrder): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(orders: List<CakeOrder>)

    @Update
    suspend fun updateOrder(order: CakeOrder)

    @Delete
    suspend fun deleteOrder(order: CakeOrder)

    @Query("DELETE FROM cake_orders WHERE id = :id")
    suspend fun deleteOrderById(id: Long)

    // Customer Profiles
    @Query("SELECT * FROM customer_profiles ORDER BY name ASC")
    fun getAllCustomers(): Flow<List<CustomerProfile>>

    @Query("SELECT COUNT(*) FROM customer_profiles")
    suspend fun getCustomerCount(): Int

    @Query("SELECT * FROM customer_profiles WHERE LOWER(name) = LOWER(:name) LIMIT 1")
    suspend fun findCustomerByName(name: String): CustomerProfile?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomer(customer: CustomerProfile): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCustomers(customers: List<CustomerProfile>): List<Long>

    @Update
    suspend fun updateCustomer(customer: CustomerProfile)

    @Delete
    suspend fun deleteCustomer(customer: CustomerProfile)

    // Ingredient Inventory
    @Query("SELECT * FROM ingredient_items ORDER BY category ASC, name ASC")
    fun getAllIngredients(): Flow<List<IngredientItem>>

    @Query("SELECT COUNT(*) FROM ingredient_items")
    suspend fun getIngredientCount(): Int

    @Query("SELECT * FROM ingredient_items WHERE id = :id LIMIT 1")
    suspend fun getIngredientById(id: Long): IngredientItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredient(item: IngredientItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllIngredients(items: List<IngredientItem>): List<Long>

    @Update
    suspend fun updateIngredient(item: IngredientItem)

    @Delete
    suspend fun deleteIngredient(item: IngredientItem)

    // Order Ingredient Usage
    @Query("SELECT * FROM order_ingredient_usages ORDER BY loggedAtMillis DESC")
    fun getAllIngredientUsages(): Flow<List<OrderIngredientUsage>>

    @Query("SELECT * FROM order_ingredient_usages WHERE orderId = :orderId ORDER BY loggedAtMillis DESC")
    suspend fun getUsagesForOrderOnce(orderId: Long): List<OrderIngredientUsage>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertIngredientUsage(usage: OrderIngredientUsage): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllUsages(usages: List<OrderIngredientUsage>)

    @Delete
    suspend fun deleteIngredientUsage(usage: OrderIngredientUsage)
}
