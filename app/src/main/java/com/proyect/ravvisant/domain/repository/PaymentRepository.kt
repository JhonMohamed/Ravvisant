package com.proyect.ravvisant.domain.repository

import com.proyect.ravvisant.domain.model.PaymentRequest
import com.proyect.ravvisant.domain.model.PaymentResponse
import com.proyect.ravvisant.domain.model.PaymentTransaction
import kotlinx.coroutines.flow.Flow

interface PaymentRepository {
    suspend fun processPayment(paymentRequest: PaymentRequest): Result<PaymentResponse>
    suspend fun checkPaymentStatus(transactionId: String): Result<PaymentResponse>
    suspend fun saveTransaction(transaction: PaymentTransaction): Result<String>
    suspend fun getTransaction(transactionId: String): Result<PaymentTransaction>
    suspend fun updateTransactionStatus(transactionId: String, status: String): Result<Boolean>
    fun getTransactionsFlow(): Flow<List<PaymentTransaction>>
} 