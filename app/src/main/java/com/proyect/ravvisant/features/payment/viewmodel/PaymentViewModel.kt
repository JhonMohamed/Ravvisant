package com.proyect.ravvisant.features.payment.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyect.ravvisant.core.firebase.PaymentService
import com.proyect.ravvisant.domain.model.*
import kotlinx.coroutines.launch
import java.util.*

class PaymentViewModel : ViewModel() {
    
    private val paymentService = PaymentService()
    
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
    
    private fun generateOrderId(): String {
        return "ORD_${System.currentTimeMillis()}_${Random().nextInt(1000)}"
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
} 