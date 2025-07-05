package com.proyect.ravvisant.features.payment.viewmodel


import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.core.firebase.PaymentService
import com.proyect.ravvisant.core.firebase.PayPalRestService
import com.proyect.ravvisant.domain.model.*
import kotlinx.coroutines.launch
import java.util.*


class PaymentViewModel : ViewModel() {

    companion object {
        const val PAYPAL_REQUEST_CODE = 123
        private const val TAG = "PaymentViewModel"
    }

    private val paymentService = PaymentService()
    private val paypalService = PayPalRestService()

    private val _paymentState = MutableLiveData<PaymentState>()
    val paymentState: LiveData<PaymentState> = _paymentState

    private val _selectedPaymentMethod = MutableLiveData<PaymentMethod>()
    val selectedPaymentMethod: LiveData<PaymentMethod> = _selectedPaymentMethod

    private val _qrCodeUrl = MutableLiveData<String>()
    val qrCodeUrl: LiveData<String> = _qrCodeUrl

    private val _paymentUrl = MutableLiveData<String>()
    val paymentUrl: LiveData<String> = _paymentUrl

    private val _currentTransaction = MutableLiveData<PaymentTransaction>()
    val currentTransaction: LiveData<PaymentTransaction> = _currentTransaction

    init {
        _paymentState.value = PaymentState.Idle
        _selectedPaymentMethod.value = PaymentMethod.YAPE
        Log.d(TAG, "PaymentViewModel initialized")
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        Log.d(TAG, "Selected payment method: $method")
        _selectedPaymentMethod.value = method
    }

    fun processPayment(
        amount: Double,
        description: String,
        customerName: String,
        customerPhone: String
    ) {
        Log.d(TAG, "Processing payment: amount=$amount, method=${_selectedPaymentMethod.value}")
        
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            try {
                // Validar el monto
                if (amount <= 0) {
                    _paymentState.value = PaymentState.Error("El monto debe ser mayor a 0")
                    return@launch
                }

                val paymentRequest = PaymentRequest(
                    amount = amount,
                    description = description,
                    orderId = generateOrderId(),
                    customerName = customerName,
                    customerPhone = customerPhone,
                    paymentMethod = _selectedPaymentMethod.value ?: PaymentMethod.YAPE
                )

                Log.d(TAG, "Created payment request: ${paymentRequest.orderId}")

                when (paymentRequest.paymentMethod) {
                    PaymentMethod.PAYPAL -> {
                        processPayPalPayment(paymentRequest)
                    }
                    PaymentMethod.YAPE -> {
                        processYapePayment(paymentRequest)
                    }
                    PaymentMethod.PLIN -> {
                        processPlinPayment(paymentRequest)
                    }
                    PaymentMethod.CREDIT_CARD -> {
                        processCreditCardPayment(paymentRequest)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing payment", e)
                _paymentState.value = PaymentState.Error("Error al procesar el pago: ${e.message}")
            }
        }
    }

    private suspend fun processPayPalPayment(paymentRequest: PaymentRequest) {
        Log.d(TAG, "Processing PayPal payment")
        
        try {
            // Validar configuración de PayPal
            if (!paypalService.validateConfiguration()) {
                _paymentState.value = PaymentState.Error("PayPal no está configurado correctamente")
                return
            }

            // Crear transacción en Firestore
            val transaction = PaymentTransaction(
                id = "TXN_${System.currentTimeMillis()}_${(0..999).random()}",
                orderId = paymentRequest.orderId,
                amount = paymentRequest.amount,
                currency = paymentRequest.currency,
                paymentMethod = paymentRequest.paymentMethod,
                status = PaymentStatus.PENDING,
                customerName = paymentRequest.customerName,
                customerPhone = paymentRequest.customerPhone,
                description = paymentRequest.description,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            val transactionId = paymentService.saveTransaction(transaction).getOrThrow()
            Log.d(TAG, "Transaction saved with ID: $transactionId")

            // Crear orden de PayPal
            val result = paypalService.createPayPalOrder(paymentRequest)
            
            result.fold(
                onSuccess = { response ->
                    if (response.success) {
                        Log.d(TAG, "PayPal order created successfully: ${response.transactionId}")
                        
                        // Actualizar transacción con la información de PayPal
                        val updatedTransaction = transaction.copy(
                            id = transactionId,
                            orderId = response.transactionId ?: paymentRequest.orderId,
                            paymentUrl = response.paymentUrl
                        )
                        _currentTransaction.value = updatedTransaction
                        
                        _paymentUrl.value = response.paymentUrl
                        _paymentState.value = PaymentState.Success(response)
                    } else {
                        Log.e(TAG, "PayPal order creation failed: ${response.message}")
                        _paymentState.value = PaymentState.Error(response.message ?: "Error al crear orden de PayPal")
                    }
                },
                onFailure = { exception ->
                    Log.e(TAG, "PayPal order creation exception", exception)
                    _paymentState.value = PaymentState.Error("Error de PayPal: ${exception.message}")
                }
            )
            
        } catch (e: Exception) {
            Log.e(TAG, "Exception in PayPal payment processing", e)
            _paymentState.value = PaymentState.Error("Error al procesar pago de PayPal: ${e.message}")
        }
    }

    private suspend fun processYapePayment(paymentRequest: PaymentRequest) {
        Log.d(TAG, "Processing Yape payment")
        
        try {
            val transaction = PaymentTransaction(
                id = "TXN_${System.currentTimeMillis()}_${(0..999).random()}",
                orderId = paymentRequest.orderId,
                amount = paymentRequest.amount,
                currency = paymentRequest.currency,
                paymentMethod = paymentRequest.paymentMethod,
                status = PaymentStatus.PENDING,
                customerName = paymentRequest.customerName,
                customerPhone = paymentRequest.customerPhone,
                description = paymentRequest.description,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            val transactionId = paymentService.saveTransaction(transaction).getOrThrow()
            val response = paymentService.processYapePayment(paymentRequest, transactionId)
            
            if (response.success) {
                _qrCodeUrl.value = response.qrCodeUrl
                _paymentUrl.value = response.paymentUrl
                _currentTransaction.value = transaction.copy(id = transactionId)
                _paymentState.value = PaymentState.Success(response)
            } else {
                _paymentState.value = PaymentState.Error(response.message ?: "Error al procesar pago Yape")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in Yape payment processing", e)
            _paymentState.value = PaymentState.Error("Error al procesar pago Yape: ${e.message}")
        }
    }

    private suspend fun processPlinPayment(paymentRequest: PaymentRequest) {
        Log.d(TAG, "Processing Plin payment")
        
        try {
            val transaction = PaymentTransaction(
                id = "TXN_${System.currentTimeMillis()}_${(0..999).random()}",
                orderId = paymentRequest.orderId,
                amount = paymentRequest.amount,
                currency = paymentRequest.currency,
                paymentMethod = paymentRequest.paymentMethod,
                status = PaymentStatus.PENDING,
                customerName = paymentRequest.customerName,
                customerPhone = paymentRequest.customerPhone,
                description = paymentRequest.description,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            val transactionId = paymentService.saveTransaction(transaction).getOrThrow()
            val response = paymentService.processPlinPayment(paymentRequest, transactionId)
            
            if (response.success) {
                _qrCodeUrl.value = response.qrCodeUrl
                _paymentUrl.value = response.paymentUrl
                _currentTransaction.value = transaction.copy(id = transactionId)
                _paymentState.value = PaymentState.Success(response)
            } else {
                _paymentState.value = PaymentState.Error(response.message ?: "Error al procesar pago Plin")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception in Plin payment processing", e)
            _paymentState.value = PaymentState.Error("Error al procesar pago Plin: ${e.message}")
        }
    }

    private suspend fun processCreditCardPayment(paymentRequest: PaymentRequest) {
        Log.d(TAG, "Processing Credit Card payment")
        
        // Por ahora, simulamos el procesamiento de tarjeta de crédito
        _paymentState.value = PaymentState.Error("Pago con tarjeta de crédito no implementado aún")
    }

    fun createPayPalOrder(paymentRequest: PaymentRequest) {
        Log.d(TAG, "Creating PayPal order directly")
        
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            try {
                val result = paypalService.createPayPalOrder(paymentRequest)

                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            Log.d(TAG, "PayPal order created successfully: ${response.transactionId}")
                            _paymentState.value = PaymentState.Success(response)
                        } else {
                            Log.e(TAG, "PayPal order creation failed: ${response.message}")
                            _paymentState.value = PaymentState.Error(response.message ?: "Error al crear orden de PayPal")
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "PayPal order creation exception", exception)
                        _paymentState.value = PaymentState.Error(exception.message ?: "Error desconocido en PayPal")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in PayPal order creation", e)
                _paymentState.value = PaymentState.Error(e.message ?: "Error en el procesamiento de PayPal")
            }
        }
    }

    fun capturePayPalOrder(orderId: String) {
        Log.d(TAG, "Capturing PayPal order: $orderId")
        
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            try {
                val result = paypalService.capturePayPalOrder(orderId)

                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            Log.d(TAG, "PayPal order captured successfully: ${response.transactionId}")
                            _paymentState.value = PaymentState.PaymentCompleted(response)
                        } else {
                            Log.e(TAG, "PayPal order capture failed: ${response.message}")
                            _paymentState.value = PaymentState.Error(response.message ?: "Error al capturar orden de PayPal")
                        }
                    },
                    onFailure = { exception ->
                        Log.e(TAG, "PayPal order capture exception", exception)
                        _paymentState.value = PaymentState.Error(exception.message ?: "Error desconocido en PayPal")
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Exception in PayPal order capture", e)
                _paymentState.value = PaymentState.Error(e.message ?: "Error en el procesamiento de PayPal")
            }
        }
    }

    fun isPayPalConfigured(): Boolean {
        val isConfigured = paypalService.validateConfiguration()
        Log.d(TAG, "PayPal configuration check: $isConfigured")
        return isConfigured
    }

    private fun generateOrderId(): String {
        return "ORD_${System.currentTimeMillis()}_${Random().nextInt(1000)}"
    }

    fun completePayPalTransaction(orderId: String) {
        Log.d(TAG, "Completing PayPal transaction: $orderId")
        
        viewModelScope.launch {
            try {
                val result = paymentService.updateTransactionStatusByOrderId(orderId, PaymentStatus.COMPLETED)
                if (result.isSuccess) {
                    Log.d(TAG, "Transaction status updated successfully")
                    checkPaymentStatus(orderId)
                } else {
                    Log.e(TAG, "Failed to update transaction status")
                    _paymentState.value = PaymentState.Error("Error al actualizar estado del pago")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Exception completing PayPal transaction", e)
                _paymentState.value = PaymentState.Error("Error al completar transacción: ${e.message}")
            }
        }
    }

    private suspend fun checkPaymentStatus(orderId: String) {
        Log.d(TAG, "Checking payment status for order: $orderId")
        
        try {
            val transaction = paymentService.getTransactionByOrderId(orderId)
            if (transaction != null) {
                Log.d(TAG, "Payment status: ${transaction.status}")
                when (transaction.status) {
                    PaymentStatus.COMPLETED -> {
                        _paymentState.value = PaymentState.PaymentCompleted(
                            PaymentResponse(
                                success = true,
                                transactionId = transaction.id,
                                qrCodeUrl = transaction.qrCodeUrl,
                                paymentUrl = transaction.paymentUrl,
                                message = "Pago completado exitosamente",
                                status = PaymentStatus.COMPLETED
                            )
                        )
                    }
                    PaymentStatus.PENDING -> {
                        _paymentState.value = PaymentState.PaymentPending(
                            PaymentResponse(
                                success = true,
                                transactionId = transaction.id,
                                qrCodeUrl = transaction.qrCodeUrl,
                                paymentUrl = transaction.paymentUrl,
                                message = "Pago pendiente",
                                status = PaymentStatus.PENDING
                            )
                        )
                    }
                    else -> {
                        _paymentState.value = PaymentState.Error("Estado de pago inesperado: ${transaction.status}")
                    }
                }
            } else {
                Log.e(TAG, "Transaction not found for order: $orderId")
                _paymentState.value = PaymentState.Error("Transacción no encontrada")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception checking payment status", e)
            _paymentState.value = PaymentState.Error("Error al verificar estado del pago: ${e.message}")
        }
    }

    fun resetPaymentState() {
        Log.d(TAG, "Resetting payment state")
        _paymentState.value = PaymentState.Idle
        _qrCodeUrl.value = null
        _paymentUrl.value = null
        _currentTransaction.value = null
    }

    fun openPaymentApp() {
        val url = _paymentUrl.value
        if (!url.isNullOrEmpty()) {
            // En el Fragment se manejará la apertura de la app
            _paymentState.value = PaymentState.OpenPaymentApp(url)
        }
    }
}

sealed class PaymentState {
    object Idle : PaymentState()
    object Loading : PaymentState()
    data class Success(val response: PaymentResponse) : PaymentState()
    data class PaymentPending(val response: PaymentResponse) : PaymentState()
    data class PaymentCompleted(val response: PaymentResponse) : PaymentState()
    data class Error(val message: String) : PaymentState()
    data class OpenPaymentApp(val url: String) : PaymentState()
    data class PayPalReady(val paymentRequest: PaymentRequest) : PaymentState()
} 