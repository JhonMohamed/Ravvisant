package com.proyect.ravvisant.core.firebase

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.CartItem
import kotlinx.coroutines.tasks.await

/**
 * Servicio para validar stock antes de operaciones del carrito
 */
object StockValidationService {
    private val TAG = "StockValidationService"
    private val firestore = FirebaseFirestore.getInstance()
    
    /**
     * Resultado de la validación de stock
     */
    data class StockValidationResult(
        val isValid: Boolean,
        val availableStock: Int,
        val requestedQuantity: Int,
        val currentCartQuantity: Int,
        val message: String
    )
    
    /**
     * Valida si se puede agregar una cantidad específica al carrito
     */
    suspend fun validateAddToCart(productId: String, quantityToAdd: Int, currentCartQuantity: Int = 0): StockValidationResult {
        return try {
            val productDoc = firestore.collection("products").document(productId).get().await()
            val availableStock = productDoc.getLong("stock")?.toInt() ?: 0
            val totalRequested = currentCartQuantity + quantityToAdd
            
            return when {
                availableStock <= 0 -> {
                    StockValidationResult(
                        isValid = false,
                        availableStock = availableStock,
                        requestedQuantity = quantityToAdd,
                        currentCartQuantity = currentCartQuantity,
                        message = "Producto sin stock disponible"
                    )
                }
                totalRequested > availableStock -> {
                    StockValidationResult(
                        isValid = false,
                        availableStock = availableStock,
                        requestedQuantity = quantityToAdd,
                        currentCartQuantity = currentCartQuantity,
                        message = "Stock insuficiente. Disponible: $availableStock, Solicitado: $totalRequested"
                    )
                }
                else -> {
                    StockValidationResult(
                        isValid = true,
                        availableStock = availableStock,
                        requestedQuantity = quantityToAdd,
                        currentCartQuantity = currentCartQuantity,
                        message = "Stock válido"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating stock for product $productId", e)
            StockValidationResult(
                isValid = false,
                availableStock = 0,
                requestedQuantity = quantityToAdd,
                currentCartQuantity = currentCartQuantity,
                message = "Error al verificar stock"
            )
        }
    }
    
    /**
     * Valida si se puede actualizar la cantidad en el carrito
     */
    suspend fun validateUpdateQuantity(productId: String, newQuantity: Int): StockValidationResult {
        return try {
            val productDoc = firestore.collection("products").document(productId).get().await()
            val availableStock = productDoc.getLong("stock")?.toInt() ?: 0
            
            return when {
                newQuantity <= 0 -> {
                    StockValidationResult(
                        isValid = true, // Permitir eliminar del carrito
                        availableStock = availableStock,
                        requestedQuantity = newQuantity,
                        currentCartQuantity = 0,
                        message = "Producto removido del carrito"
                    )
                }
                availableStock <= 0 -> {
                    StockValidationResult(
                        isValid = false,
                        availableStock = availableStock,
                        requestedQuantity = newQuantity,
                        currentCartQuantity = 0,
                        message = "Producto sin stock disponible"
                    )
                }
                newQuantity > availableStock -> {
                    StockValidationResult(
                        isValid = false,
                        availableStock = availableStock,
                        requestedQuantity = newQuantity,
                        currentCartQuantity = 0,
                        message = "Stock insuficiente. Disponible: $availableStock, Solicitado: $newQuantity"
                    )
                }
                else -> {
                    StockValidationResult(
                        isValid = true,
                        availableStock = availableStock,
                        requestedQuantity = newQuantity,
                        currentCartQuantity = 0,
                        message = "Stock válido"
                    )
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error validating stock update for product $productId", e)
            StockValidationResult(
                isValid = false,
                availableStock = 0,
                requestedQuantity = newQuantity,
                currentCartQuantity = 0,
                message = "Error al verificar stock"
            )
        }
    }
    
    /**
     * Obtiene el stock disponible de un producto
     */
    suspend fun getProductStock(productId: String): Int {
        return try {
            val productDoc = firestore.collection("products").document(productId).get().await()
            productDoc.getLong("stock")?.toInt() ?: 0
        } catch (e: Exception) {
            Log.e(TAG, "Error getting stock for product $productId", e)
            0
        }
    }
    
    /**
     * Verifica si un producto tiene stock disponible
     */
    suspend fun hasStock(productId: String, quantity: Int = 1): Boolean {
        val availableStock = getProductStock(productId)
        return availableStock >= quantity
    }
    
    /**
     * Obtiene información completa del stock de un producto
     */
    suspend fun getStockInfo(productId: String): StockInfo {
        return try {
            val productDoc = firestore.collection("products").document(productId).get().await()
            val stock = productDoc.getLong("stock")?.toInt() ?: 0
            val name = productDoc.getString("name") ?: "Producto desconocido"
            
            StockInfo(
                productId = productId,
                productName = name,
                availableStock = stock,
                hasStock = stock > 0
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error getting stock info for product $productId", e)
            StockInfo(
                productId = productId,
                productName = "Error",
                availableStock = 0,
                hasStock = false
            )
        }
    }
    
    /**
     * Información del stock de un producto
     */
    data class StockInfo(
        val productId: String,
        val productName: String,
        val availableStock: Int,
        val hasStock: Boolean
    )
} 