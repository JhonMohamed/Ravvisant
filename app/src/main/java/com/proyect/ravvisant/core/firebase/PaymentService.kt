package com.proyect.ravvisant.core.firebase

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.proyect.ravvisant.domain.model.*
import com.proyect.ravvisant.domain.repository.PaymentRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.*
import android.util.Log
import com.proyect.ravvisant.core.firebase.PayPalRestService
class PaymentService : PaymentRepository {

    private val firestore = FirebaseFirestore.getInstance()
    private val transactionsCollection = firestore.collection("transactions")

    // Simulación de API de Yape (en producción usarías la API real de Yape)
    private val yapeApiUrl = "https://api.yape.com.pe/v1" // URL ficticia

    private val payPalRestService = PayPalRestService()

    override suspend fun processPayment(paymentRequest: PaymentRequest): Result<PaymentResponse> {
        return try {
            // Crear transacción en Firebase
            val transaction = PaymentTransaction(
                id = generateTransactionId(),
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

            // Guardar transacción
            val transactionId = saveTransaction(transaction).getOrThrow()

            // Procesar pago según el método
            val paymentResponse = when (paymentRequest.paymentMethod) {
                PaymentMethod.YAPE -> processYapePayment(paymentRequest, transactionId)
                PaymentMethod.PLIN -> processPlinPayment(paymentRequest, transactionId)
                PaymentMethod.PAYPAL -> processPayPalPayment(paymentRequest, transactionId)
                PaymentMethod.CREDIT_CARD -> processCreditCardPayment(paymentRequest, transactionId)
            }

            Result.success(paymentResponse)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun processYapePayment(
        paymentRequest: PaymentRequest,
        transactionId: String
    ): PaymentResponse {
        // En producción, aquí harías la llamada real a la API de Yape
        // Por ahora simulamos la respuesta

        val qrCodeUrl = generateYapeQRCode(paymentRequest.amount, transactionId)
        val paymentUrl = generateYapePaymentUrl(transactionId)

        // Actualizar transacción con QR y URL
        updateTransactionWithPaymentInfo(transactionId, qrCodeUrl, paymentUrl)

        return PaymentResponse(
            success = true,
            transactionId = transactionId,
            qrCodeUrl = qrCodeUrl,
            paymentUrl = paymentUrl,
            message = "Pago Yape generado exitosamente",
            status = PaymentStatus.PENDING
        )
    }

    private suspend fun processPlinPayment(
        paymentRequest: PaymentRequest,
        transactionId: String
    ): PaymentResponse {
        // Implementar lógica para Plin
        val qrCodeUrl = generatePlinQRCode(paymentRequest.amount, transactionId)
        val paymentUrl = generatePlinPaymentUrl(transactionId)

        updateTransactionWithPaymentInfo(transactionId, qrCodeUrl, paymentUrl)

        return PaymentResponse(
            success = true,
            transactionId = transactionId,
            qrCodeUrl = qrCodeUrl,
            paymentUrl = paymentUrl,
            message = "Pago Plin generado exitosamente",
            status = PaymentStatus.PENDING
        )
    }

    suspend fun processPayPalPayment(
        paymentRequest: PaymentRequest,
        transactionId: String
    ): PaymentResponse {
        // Lógica real con PayPalRestService
        val result = payPalRestService.createPayPalOrder(paymentRequest)
        return result.getOrElse {
            PaymentResponse(
                success = false,
                transactionId = null,
                qrCodeUrl = null,
                paymentUrl = null,
                message = it.message,
                status = PaymentStatus.FAILED
            )
        }
    }

    private suspend fun processCreditCardPayment(
        paymentRequest: PaymentRequest,
        transactionId: String
    ): PaymentResponse {
        // Implementar lógica para tarjeta de crédito
        val paymentUrl = generateCreditCardPaymentUrl(paymentRequest.amount, transactionId)

        updateTransactionWithPaymentInfo(transactionId, null, paymentUrl)

        return PaymentResponse(
            success = true,
            transactionId = transactionId,
            qrCodeUrl = null,
            paymentUrl = paymentUrl,
            message = "Pago con tarjeta generado exitosamente",
            status = PaymentStatus.PENDING
        )
    }

    override suspend fun checkPaymentStatus(transactionId: String): Result<PaymentResponse> {
        return try {
            val transaction = getTransaction(transactionId).getOrThrow()

            // En producción, aquí verificarías el estado real con la API de Yape
            val status = checkPaymentStatusWithProvider(transaction)

            Result.success(PaymentResponse(
                success = status == PaymentStatus.COMPLETED,
                transactionId = transactionId,
                qrCodeUrl = transaction.qrCodeUrl,
                paymentUrl = transaction.paymentUrl,
                message = "Estado verificado",
                status = status
            ))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveTransaction(transaction: PaymentTransaction): Result<String> {
        return try {
            val docRef = transactionsCollection.add(transaction).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun getTransaction(transactionId: String): Result<PaymentTransaction> {
        return try {
            val document = transactionsCollection.document(transactionId).get().await()
            if (document.exists()) {
                val transaction = document.toObject(PaymentTransaction::class.java)
                Result.success(transaction ?: PaymentTransaction())
            } else {
                Result.failure(Exception("Transacción no encontrada"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateTransactionStatus(
        transactionId: String,
        status: String
    ): Result<Boolean> {
        return try {
            val statusEnum = PaymentStatus.valueOf(status.uppercase())
            val updates = mapOf(
                "status" to statusEnum,
                "updatedAt" to Date()
            )

            transactionsCollection.document(transactionId)
                .update(updates)
                .await()

            Result.success(true)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getTransactionsFlow(): Flow<List<PaymentTransaction>> = callbackFlow {
        val subscription = transactionsCollection
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                val transactions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(PaymentTransaction::class.java)
                } ?: emptyList()

                trySend(transactions)
            }

        awaitClose { subscription.remove() }
    }

    // Métodos auxiliares
    private fun generateTransactionId(): String {
        return "TXN_${System.currentTimeMillis()}_${Random().nextInt(1000)}"
    }

    private fun generateYapeQRCode(amount: Double, transactionId: String): String {
        val phone = "978318805" // Tu número Yape
        val data = "yape://pay?phone=$phone&amount=$amount"
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=$data"
    }

    private fun generateYapePaymentUrl(transactionId: String): String {
        return "yape://pay?id=$transactionId"
    }

    private fun generatePlinQRCode(amount: Double, transactionId: String): String {
        return "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=plin://pay?amount=$amount&id=$transactionId"
    }

    private fun generatePlinPaymentUrl(transactionId: String): String {
        return "plin://pay?id=$transactionId"
    }

    private fun generatePayPalPaymentUrl(amount: Double, transactionId: String): String {
        return "https://www.paypal.com/pay?amount=$amount&transaction_id=$transactionId"
    }

    private fun generateCreditCardPaymentUrl(amount: Double, transactionId: String): String {
        return "https://payment.gateway.com/pay?amount=$amount&transaction_id=$transactionId"
    }

    private suspend fun updateTransactionWithPaymentInfo(
        transactionId: String,
        qrCodeUrl: String?,
        paymentUrl: String?
    ) {
        val updates = mutableMapOf<String, Any>(
            "updatedAt" to Date()
        )

        qrCodeUrl?.let { updates["qrCodeUrl"] = it }
        paymentUrl?.let { updates["paymentUrl"] = it }

        transactionsCollection.document(transactionId).update(updates).await()
    }

    private suspend fun checkPaymentStatusWithProvider(transaction: PaymentTransaction): PaymentStatus {
        // En producción, aquí verificarías el estado real con la API del proveedor
        // Por ahora simulamos que el pago está pendiente
        return PaymentStatus.PENDING
    }

    suspend fun updateTransactionStatusByOrderId(orderId: String, status: PaymentStatus): Result<Boolean> {
        return try {
            val querySnapshot = transactionsCollection.whereEqualTo("orderId", orderId).get().await()
            if (!querySnapshot.isEmpty) {
                for (document in querySnapshot.documents) {
                    transactionsCollection.document(document.id)
                        .update("status", status.name, "updatedAt", Date())
                        .await()
                }
                Result.success(true)
            } else {
                Result.failure(Exception("No se encontró transacción con orderId: $orderId"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 