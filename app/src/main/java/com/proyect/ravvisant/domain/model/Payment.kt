package com.proyect.ravvisant.domain.model

import java.util.Date

data class PaymentRequest(
    val amount: Double,
    val currency: String = "PEN",
    val description: String,
    val orderId: String,
    val customerName: String,
    val customerPhone: String,
    val paymentMethod: PaymentMethod
)

data class PaymentResponse(
    val success: Boolean,
    val transactionId: String?,
    val qrCodeUrl: String?,
    val paymentUrl: String?,
    val message: String?,
    val status: PaymentStatus
)

enum class PaymentMethod {
    YAPE,
    PLIN,
    PAYPAL,
    CREDIT_CARD
}

enum class PaymentStatus {
    PENDING,
    PROCESSING,
    COMPLETED,
    FAILED,
    CANCELLED
}

data class PaymentTransaction(
    val id: String = "",
    val orderId: String = "",
    val amount: Double = 0.0,
    val currency: String = "PEN",
    val paymentMethod: PaymentMethod = PaymentMethod.YAPE,
    val status: PaymentStatus = PaymentStatus.PENDING,
    val customerName: String = "",
    val customerPhone: String = "",
    val description: String = "",
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val qrCodeUrl: String? = null,
    val paymentUrl: String? = null
) 