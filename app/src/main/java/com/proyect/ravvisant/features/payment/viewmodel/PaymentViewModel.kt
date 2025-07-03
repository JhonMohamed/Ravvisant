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
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _selectedPaymentMethod.value = method
    }

    fun processPayment(
        amount: Double,
        description: String,
        customerName: String,
        customerPhone: String
    ) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            try {
                val paymentRequest = PaymentRequest(
                    amount = amount,
                    description = description,
                    orderId = generateOrderId(),
                    customerName = customerName,
                    customerPhone = customerPhone,
                    paymentMethod = _selectedPaymentMethod.value ?: PaymentMethod.YAPE
                )

                if (paymentRequest.paymentMethod == PaymentMethod.PAYPAL) {
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
                    val response = paymentService.processPayPalPayment(paymentRequest, transactionId)
                    if (response.success) {
                        _paymentUrl.value = response.paymentUrl
                        _currentTransaction.value = transaction.copy(id = transactionId, orderId = response.transactionId ?: paymentRequest.orderId)
                        _paymentState.value = PaymentState.Success(response)
                    } else {
                        _paymentState.value = PaymentState.Error(response.message ?: "Error en el pago PayPal")
                    }
                    return@launch
                }

                val result = paymentService.processPayment(paymentRequest)

                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _qrCodeUrl.value = response.qrCodeUrl
                            _paymentUrl.value = response.paymentUrl
                            _currentTransaction.value = PaymentTransaction(
                                id = response.transactionId ?: "",
                                orderId = paymentRequest.orderId,
                                amount = amount,
                                paymentMethod = paymentRequest.paymentMethod,
                                status = response.status,
                                customerName = customerName,
                                customerPhone = customerPhone,
                                description = description,
                                qrCodeUrl = response.qrCodeUrl,
                                paymentUrl = response.paymentUrl
                            )
                            _paymentState.value = PaymentState.Success(response)
                        } else {
                            _paymentState.value = PaymentState.Error(response.message ?: "Error en el pago")
                        }
                    },
                    onFailure = { exception ->
                        _paymentState.value = PaymentState.Error(exception.message ?: "Error desconocido")
                    }
                )
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Error en el procesamiento")
            }
        }
    }

    fun checkPaymentStatus(transactionId: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            try {
                val result = paymentService.checkPaymentStatus(transactionId)

                result.fold(
                    onSuccess = { response ->
                        when (response.status) {
                            PaymentStatus.COMPLETED -> {
                                _paymentState.value = PaymentState.PaymentCompleted(response)
                            }
                            PaymentStatus.FAILED -> {
                                _paymentState.value = PaymentState.Error("Pago fallido")
                            }
                            PaymentStatus.CANCELLED -> {
                                _paymentState.value = PaymentState.Error("Pago cancelado")
                            }
                            else -> {
                                _paymentState.value = PaymentState.PaymentPending(response)
                            }
                        }
                    },
                    onFailure = { exception ->
                        _paymentState.value = PaymentState.Error(exception.message ?: "Error al verificar estado")
                    }
                )
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Error en la verificación")
            }
        }
    }

    fun resetPaymentState() {
        _paymentState.value = PaymentState.Idle
        _qrCodeUrl.value = ""
        _paymentUrl.value = ""
        _currentTransaction.value = null
    }

    fun openPaymentApp() {
        val url = _paymentUrl.value
        if (!url.isNullOrEmpty()) {
            // En el Fragment se manejará la apertura de la app
            _paymentState.value = PaymentState.OpenPaymentApp(url)
        }
    }

    fun createPayPalOrder(paymentRequest: PaymentRequest) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            try {
                val result = paypalService.createPayPalOrder(paymentRequest)

                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _paymentState.value = PaymentState.Success(response)
                        } else {
                            _paymentState.value = PaymentState.Error(response.message ?: "Error al crear orden de PayPal")
                        }
                    },
                    onFailure = { exception ->
                        _paymentState.value = PaymentState.Error(exception.message ?: "Error desconocido en PayPal")
                    }
                )
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Error en el procesamiento de PayPal")
            }
        }
    }

    fun capturePayPalOrder(orderId: String) {
        viewModelScope.launch {
            _paymentState.value = PaymentState.Loading

            try {
                val result = paypalService.capturePayPalOrder(orderId)

                result.fold(
                    onSuccess = { response ->
                        if (response.success) {
                            _paymentState.value = PaymentState.PaymentCompleted(response)
                        } else {
                            _paymentState.value = PaymentState.Error(response.message ?: "Error al capturar orden de PayPal")
                        }
                    },
                    onFailure = { exception ->
                        _paymentState.value = PaymentState.Error(exception.message ?: "Error desconocido en PayPal")
                    }
                )
            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error(e.message ?: "Error en el procesamiento de PayPal")
            }
        }
    }

    fun isPayPalConfigured(): Boolean {
        return paypalService.validateConfiguration()
    }

    private fun generateOrderId(): String {
        return "ORD_${System.currentTimeMillis()}_${Random().nextInt(1000)}"
    }

    fun completePayPalTransaction(orderId: String) {
        Log.d("PAYMENT_DEBUG", "Llamando a updateTransactionStatusByOrderId con orderId: $orderId")
        viewModelScope.launch {
            val result = paymentService.updateTransactionStatusByOrderId(orderId, PaymentStatus.COMPLETED)
            if (result.isSuccess) {
                checkPaymentStatus(orderId)
            } else {
                _paymentState.value = PaymentState.Error("Error al actualizar estado del pago")
            }
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