package com.proyect.ravvisant.features.payment


import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.content.Intent
import android.net.Uri

import androidx.fragment.app.viewModels
import com.proyect.ravvisant.core.utils.PayPalUtils
import com.proyect.ravvisant.databinding.FragmentPayBinding
import com.proyect.ravvisant.domain.model.PaymentMethod
import com.proyect.ravvisant.features.payment.viewmodel.PaymentState
import com.proyect.ravvisant.features.payment.viewmodel.PaymentViewModel

/**
 * Fragmento de prueba para verificar la integración de PayPal
 * Este fragmento se puede usar para probar la configuración antes de usar PayPal en producción
 */
class PayPalTestFragment : Fragment() {

    private var _binding: FragmentPayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupTestUI()
        runPayPalTests()

        // Observer para el estado del pago
        viewModel.paymentState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PaymentState.Loading -> {
                    // Puedes mostrar un loading si quieres
                }
                is PaymentState.Success -> {
                    // Abre la URL de pago si existe
                    state.response.paymentUrl?.let { url ->
                        val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url))
                        startActivity(intent)
                    }
                }
                is PaymentState.Error -> {
                    android.widget.Toast.makeText(context, state.message, android.widget.Toast.LENGTH_LONG).show()
                }
                is PaymentState.OpenPaymentApp -> {
                    val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(state.url))
                    startActivity(intent)
                }
                else -> {}
            }
        }
    }

    private fun setupTestUI() {
        // Configurar un monto de prueba pequeño
        val testAmount = 1.00
        binding.tvTotalPay.text = "S/ ${String.format("%.2f", testAmount)}"
        binding.tvPayProducts.text = "S/ ${String.format("%.2f", testAmount)}"
        binding.tvProductos.text = "Test Product (1)"
        binding.tvPayEnvio.text = "Gratis"

        // Configurar listeners solo para PayPal
        binding.cvPayPal.setOnClickListener {
            runPayPalTest()
        }

        // Deshabilitar otros métodos de pago para la prueba
        binding.cvCard.isEnabled = false
        binding.cvYape.isEnabled = false
        binding.cvPLin.isEnabled = false

        binding.cvCard.alpha = 0.5f
        binding.cvYape.alpha = 0.5f
        binding.cvPLin.alpha = 0.5f
    }

    private fun runPayPalTests() {
        Log.d("PayPalTest", "=== INICIANDO PRUEBAS DE PAYPAL ===")

        // Test 1: Validación de configuración
        val validation = PayPalUtils.validateConfiguration()
        Log.d("PayPalTest", validation.getSummary())

        // Test 2: Información de debug
        Log.d("PayPalTest", PayPalUtils.getDebugInfo())

        // Test 3: Validación de montos
        val testAmounts = listOf(0.0, 1.0, 10.0, 100.0, 10000.0, 15000.0)
        testAmounts.forEach { amount ->
            val isValid = PayPalUtils.isValidAmount(amount)
            Log.d("PayPalTest", "Amount $amount is valid: $isValid")
        }

        // Test 4: Creación de pagos (REST)
        val testPayment = PayPalUtils.createPayPalPaymentRequest(1.0, "Test Payment")
        if (testPayment != null) {
            Log.d("PayPalTest", "Test payment request created successfully")
        } else {
            Log.e("PayPalTest", "Failed to create test payment request")
        }

        // Test 5: Conversión de monedas
        val penAmount = 100.0
        val usdAmount = PayPalUtils.convertToUSD(penAmount, "PEN")
        Log.d("PayPalTest", "$penAmount PEN = $usdAmount USD")

        Log.d("PayPalTest", "=== PRUEBAS DE PAYPAL COMPLETADAS ===")
    }

    private fun runPayPalTest() {
        Log.d("PayPalTest", "Iniciando prueba de pago con PayPal")

        // Verificar configuración
        val validation = PayPalUtils.validateConfiguration()
        if (!validation.isValid) {
            Toast.makeText(context, "PayPal no está configurado correctamente", Toast.LENGTH_LONG).show()
            Log.e("PayPalTest", validation.getSummary())
            return
        }

        // Verificar disponibilidad (REST siempre está disponible)
        if (!PayPalUtils.isPayPalAvailable(requireContext())) {
            Toast.makeText(context, "PayPal no está disponible", Toast.LENGTH_LONG).show()
            return
        }

        // Iniciar pago de prueba
        viewModel.selectPaymentMethod(PaymentMethod.PAYPAL)

        val testAmount = 1.00
        val customerName = "Test User"
        val customerPhone = "+51 999 999 999"
        val description = "Test Payment - Ravvisant"

        viewModel.processPayment(
            amount = testAmount,
            description = description,
            customerName = customerName,
            customerPhone = customerPhone
        )

        Toast.makeText(context, "Iniciando pago de prueba con PayPal...", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}