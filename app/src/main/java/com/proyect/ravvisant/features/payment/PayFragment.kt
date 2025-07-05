package com.proyect.ravvisant.features.payment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.fragment.app.activityViewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.proyect.ravvisant.R
import com.proyect.ravvisant.core.utils.PayPalUtils
import com.proyect.ravvisant.databinding.FragmentPayBinding
import com.proyect.ravvisant.domain.model.PaymentMethod
import com.proyect.ravvisant.features.payment.viewmodel.PaymentState
import com.proyect.ravvisant.features.payment.viewmodel.PaymentViewModel
import com.proyect.ravvisant.features.cart.viewmodel.CartViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.math.BigDecimal
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import androidx.lifecycle.Lifecycle

class PayFragment : Fragment() {

    private var _binding: FragmentPayBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PaymentViewModel by viewModels()
    private val cartViewModel: CartViewModel by activityViewModels()

    private var currentTransactionId: String? = null
    private var currentAmount: Double = 0.0
    private var currentPayPalOrderId: String? = null

    companion object {
        private const val TAG = "PayFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d(TAG, "PayFragment created")
        
        setupUI()
        setupObservers()
        setupClickListeners()

        // Observa los productos y el total del carrito
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.cartItems.collectLatest { items ->
                    val cantidad = items.sumOf { it.quantity }
                    binding.tvProductos.text = "Productos ($cantidad)"
                    Log.d(TAG, "Cart items updated: $cantidad items")
                }
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                cartViewModel.totalAmount.collectLatest { total ->
                    currentAmount = total
                    binding.tvTotalPay.text = "S/ ${String.format("%.2f", total)}"
                    binding.tvPayProducts.text = "S/ ${String.format("%.2f", total)}"
                    Log.d(TAG, "Total amount updated: $total")
                }
            }
        }

        // Configurar PayPal
        setupPayPal()
    }

    private fun setupUI() {
        // Configurar el monto total
        currentAmount = 9219.91
        binding.tvTotalPay.text = "S/ ${String.format("%.2f", currentAmount)}"
        binding.tvPayProducts.text = "S/ ${String.format("%.2f", currentAmount)}"
        
        Log.d(TAG, "UI setup completed with amount: $currentAmount")
    }

    private fun setupObservers() {
        viewModel.paymentState.observe(viewLifecycleOwner) { state ->
            Log.d(TAG, "Payment state changed: $state")
            
            when (state) {
                is PaymentState.Idle -> {
                    // Estado inicial, no hacer nada
                    Log.d(TAG, "Payment state: Idle")
                }
                is PaymentState.Loading -> {
                    showLoading(true)
                    Log.d(TAG, "Payment state: Loading")
                }
                is PaymentState.Success -> {
                    showLoading(false)
                    Log.d(TAG, "Payment state: Success - ${state.response.message}")
                    
                    if (state.response.paymentUrl != null) {
                        // Es una respuesta de PayPal, abrir en navegador
                        currentPayPalOrderId = state.response.transactionId
                        Log.d(TAG, "Opening PayPal URL: ${state.response.paymentUrl}")
                        openPayPalInBrowser(state.response.paymentUrl)
                    } else {
                        // Es una respuesta de otro método de pago
                        showPaymentDialog(state.response)
                    }
                }
                is PaymentState.PaymentPending -> {
                    showLoading(false)
                    Log.d(TAG, "Payment state: Pending")
                    Toast.makeText(context, "Pago pendiente", Toast.LENGTH_SHORT).show()
                }
                is PaymentState.PaymentCompleted -> {
                    showLoading(false)
                    Log.d(TAG, "Payment state: Completed")
                    showPaymentSuccessDialog()
                }
                is PaymentState.Error -> {
                    showLoading(false)
                    Log.e(TAG, "Payment state: Error - ${state.message}")
                    showErrorDialog(state.message)
                }
                is PaymentState.OpenPaymentApp -> {
                    Log.d(TAG, "Payment state: OpenPaymentApp - ${state.url}")
                    openPaymentApp(state.url)
                }
                is PaymentState.PayPalReady -> {
                    Log.d(TAG, "Payment state: PayPalReady")
                    createPayPalOrder(state.paymentRequest)
                }
            }
        }

        viewModel.selectedPaymentMethod.observe(viewLifecycleOwner) { method ->
            Log.d(TAG, "Selected payment method: $method")
            updatePaymentMethodSelection(method)
        }
    }

    private fun setupClickListeners() {
        // PayPal
        binding.cvPayPal.setOnClickListener {
            Log.d(TAG, "PayPal clicked")
            viewModel.selectPaymentMethod(PaymentMethod.PAYPAL)
            processPayment()
        }

        // Tarjeta de crédito/débito
        binding.cvCard.setOnClickListener {
            Log.d(TAG, "Credit card clicked")
            viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)
            processPayment()
        }

        // Yape
        binding.cvYape.setOnClickListener {
            Log.d(TAG, "Yape clicked")
            viewModel.selectPaymentMethod(PaymentMethod.YAPE)
            processPayment()
        }

        // Plin
        binding.cvPLin.setOnClickListener {
            Log.d(TAG, "Plin clicked")
            viewModel.selectPaymentMethod(PaymentMethod.PLIN)
            processPayment()
        }
    }

    private fun processPayment() {
        val customerName = "Cliente Ravvisant" // En producción vendría del perfil del usuario
        val customerPhone = "+51 999 999 999" // En producción vendría del perfil del usuario
        val description = "Compra en Ravvisant - ${binding.tvProductos.text}"

        Log.d(TAG, "Processing payment: amount=$currentAmount, description=$description")

        viewModel.processPayment(
            amount = currentAmount,
            description = description,
            customerName = customerName,
            customerPhone = customerPhone
        )
    }

    private fun showLoading(show: Boolean) {
        binding.progressBar.visibility = if (show) View.VISIBLE else View.GONE
        
        // Deshabilitar/habilitar botones durante el loading
        binding.cvPayPal.isEnabled = !show
        binding.cvCard.isEnabled = !show
        binding.cvYape.isEnabled = !show
        binding.cvPLin.isEnabled = !show
        
        Log.d(TAG, "Loading state: $show")
    }

    private fun updatePaymentMethodSelection(method: PaymentMethod) {
        // Reset all selections
        binding.cvPayPal.alpha = 0.7f
        binding.cvCard.alpha = 0.7f
        binding.cvYape.alpha = 0.7f
        binding.cvPLin.alpha = 0.7f

        // Highlight selected method
        when (method) {
            PaymentMethod.PAYPAL -> binding.cvPayPal.alpha = 1.0f
            PaymentMethod.CREDIT_CARD -> binding.cvCard.alpha = 1.0f
            PaymentMethod.YAPE -> binding.cvYape.alpha = 1.0f
            PaymentMethod.PLIN -> binding.cvPLin.alpha = 1.0f
        }
    }

    private fun showPaymentDialog(response: com.proyect.ravvisant.domain.model.PaymentResponse) {
        val message = response.message ?: "Pago procesado"
        
        AlertDialog.Builder(requireContext())
            .setTitle("Pago Procesado")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                // Navegar a la pantalla de confirmación
                navigateToSuccess()
            }
            .setCancelable(false)
            .show()
    }

    private fun showPaymentSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("¡Pago Exitoso!")
            .setMessage("Tu pago ha sido procesado correctamente. Gracias por tu compra.")
            .setPositiveButton("Continuar") { _, _ ->
                navigateToSuccess()
            }
            .setCancelable(false)
            .show()
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Error en el Pago")
            .setMessage(message)
            .setPositiveButton("OK") { _, _ ->
                // Reset payment state
                viewModel.resetPaymentState()
            }
            .setCancelable(true)
            .show()
    }

    private fun openPaymentApp(url: String) {
        try {
            Log.d(TAG, "Opening payment app with URL: $url")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening payment app", e)
            Toast.makeText(context, "No se pudo abrir la aplicación de pago", Toast.LENGTH_SHORT).show()
        }
    }

    private fun navigateToSuccess() {
        // Aquí navegarías a la pantalla de confirmación
        // Por ahora solo mostramos un mensaje
        Toast.makeText(context, "¡Gracias por tu compra!", Toast.LENGTH_LONG).show()

        // Limpiar el estado del pago
        viewModel.resetPaymentState()

        // Navegar de vuelta al home o a la pantalla de confirmación
        // findNavController().navigate(R.id.action_payFragment_to_homeFragment)
    }

    private fun setupPayPal() {
        // Validar configuración de PayPal
        val validation = PayPalUtils.validateConfiguration()
        Log.d(TAG, "PayPal validation: ${validation.getSummary()}")
        
        if (!validation.isValid) {
            // Deshabilitar PayPal si no está configurado
            binding.cvPayPal.isEnabled = false
            binding.cvPayPal.alpha = 0.5f
            Toast.makeText(context, "PayPal no está configurado correctamente", Toast.LENGTH_SHORT).show()

            // Log de errores para debugging
            Log.e(TAG, validation.getSummary())
        } else if (validation.hasWarnings()) {
            // Mostrar advertencias pero permitir uso
            Log.w(TAG, validation.getSummary())
        } else {
            Log.d(TAG, "PayPal configuration is valid")
        }
    }

    private fun createPayPalOrder(paymentRequest: com.proyect.ravvisant.domain.model.PaymentRequest) {
        // Validar el monto
        if (!PayPalUtils.isValidAmount(paymentRequest.amount)) {
            Log.e(TAG, "Invalid amount for PayPal: ${paymentRequest.amount}")
            Toast.makeText(context, "Monto inválido para PayPal", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Creating PayPal order for amount: ${paymentRequest.amount}")
        
        // Crear orden de PayPal usando el ViewModel
        viewModel.createPayPalOrder(paymentRequest)
    }

    private fun openPayPalInBrowser(url: String) {
        try {
            Log.d(TAG, "Opening PayPal in browser: $url")
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Error opening PayPal in browser", e)
            Toast.makeText(context, "No se pudo abrir PayPal en el navegador", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "PayFragment resumed")
        
        // Verificar si el usuario regresó de PayPal
        // Aquí podrías implementar la lógica para verificar el estado del pago
        // cuando el usuario regresa de PayPal
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "PayFragment destroyed")
        _binding = null
    }
} 