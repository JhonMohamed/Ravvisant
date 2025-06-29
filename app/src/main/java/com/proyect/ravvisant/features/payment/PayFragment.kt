package com.proyect.ravvisant.features.payment

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.bumptech.glide.Glide
import com.proyect.ravvisant.R
import com.proyect.ravvisant.databinding.FragmentPayBinding
import com.proyect.ravvisant.domain.model.PaymentMethod
import com.proyect.ravvisant.features.payment.viewmodel.PaymentState
import com.proyect.ravvisant.features.payment.viewmodel.PaymentViewModel
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class PayFragment : Fragment() {
    
    private var _binding: FragmentPayBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: PaymentViewModel by viewModels()
    
    private var currentTransactionId: String? = null
    private var currentAmount: Double = 0.0
    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPayBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupUI()
        setupObservers()
        setupClickListeners()
        
        // Simular datos de carrito (en producción vendrían del ViewModel del carrito)
        setupCartData()
    }
    
    private fun setupUI() {
        // Configurar el monto total
        currentAmount = 9219.91
        binding.tvTotalPay.text = "S/ ${String.format("%.2f", currentAmount)}"
        binding.tvPayProducts.text = "S/ ${String.format("%.2f", currentAmount)}"
    }
    
    private fun setupObservers() {
        viewModel.paymentState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is PaymentState.Idle -> {
                    // Estado inicial, no hacer nada
                }
                is PaymentState.Loading -> {
                    showLoading(true)
                }
                is PaymentState.Success -> {
                    showLoading(false)
                    showPaymentDialog(state.response)
                }
                is PaymentState.PaymentPending -> {
                    showLoading(false)
                    Toast.makeText(context, "Pago pendiente", Toast.LENGTH_SHORT).show()
                }
                is PaymentState.PaymentCompleted -> {
                    showLoading(false)
                    showPaymentSuccessDialog()
                }
                is PaymentState.Error -> {
                    showLoading(false)
                    Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                }
                is PaymentState.OpenPaymentApp -> {
                    openPaymentApp(state.url)
                }
            }
        }
        
        viewModel.selectedPaymentMethod.observe(viewLifecycleOwner) { method ->
            updatePaymentMethodSelection(method)
        }
    }
    
    private fun setupClickListeners() {
        // PayPal
        binding.cvPayPal.setOnClickListener {
            viewModel.selectPaymentMethod(PaymentMethod.PAYPAL)
            startPayPalCheckout(currentAmount)
        }
        
        // Tarjeta de crédito/débito
        binding.cvCard.setOnClickListener {
            viewModel.selectPaymentMethod(PaymentMethod.CREDIT_CARD)
            processPayment()
        }
        
        // Yape
        binding.cvYape.setOnClickListener {
            viewModel.selectPaymentMethod(PaymentMethod.YAPE)
            processPayment()
        }
        
        // Plin
        binding.cvPLin.setOnClickListener {
            viewModel.selectPaymentMethod(PaymentMethod.PLIN)
            processPayment()
        }
    }
    
    private fun setupCartData() {
        // En producción, estos datos vendrían del ViewModel del carrito
        binding.tvProductos.text = "Productos (9)"
        binding.tvPayProducts.text = "S/ ${String.format("%.2f", currentAmount)}"
        binding.tvPayEnvio.text = "Gratis"
        binding.tvTotalPay.text = "S/ ${String.format("%.2f", currentAmount)}"
    }
    
    private fun processPayment() {
        val customerName = "Cliente Ravvisant" // En producción vendría del perfil del usuario
        val customerPhone = "+51 999 999 999" // En producción vendría del perfil del usuario
        val description = "Compra en Ravvisant - ${binding.tvProductos.text}"
        
        viewModel.processPayment(
            amount = currentAmount,
            description = description,
            customerName = customerName,
            customerPhone = customerPhone
        )
    }
    
    private fun updatePaymentMethodSelection(selectedMethod: PaymentMethod) {
        // Resetear todos los estilos
        binding.cvPayPal.alpha = 0.7f
        binding.cvCard.alpha = 0.7f
        binding.cvYape.alpha = 0.7f
        binding.cvPLin.alpha = 0.7f
        
        // Resaltar el método seleccionado
        when (selectedMethod) {
            PaymentMethod.PAYPAL -> binding.cvPayPal.alpha = 1.0f
            PaymentMethod.CREDIT_CARD -> binding.cvCard.alpha = 1.0f
            PaymentMethod.YAPE -> binding.cvYape.alpha = 1.0f
            PaymentMethod.PLIN -> binding.cvPLin.alpha = 1.0f
        }
    }
    
    private fun showPaymentDialog(response: com.proyect.ravvisant.domain.model.PaymentResponse) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_yape_qr, null)
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .setCancelable(false)
            .create()

        // Campo de texto para el código de aprobación
        val etCodigoAprobacion = dialogView.findViewById<android.widget.EditText>(R.id.etCodigoAprobacion)
        val btnConfirmarPago = dialogView.findViewById<android.widget.Button>(R.id.btnConfirmarPago)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)

        btnConfirmarPago.setOnClickListener {
            val codigo = etCodigoAprobacion.text.toString().trim()
            if (codigo.isEmpty()) {
                Toast.makeText(context, "Por favor ingresa el código de aprobación", Toast.LENGTH_SHORT).show()
            } else {
                dialog.dismiss()
                showPaymentSuccessDialog()
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
            viewModel.resetPaymentState()
        }

        dialog.show()
    }
    
    private fun showPaymentSuccessDialog() {
        AlertDialog.Builder(requireContext())
            .setTitle("¡Pago Exitoso!")
            .setMessage("Tu pago ha sido procesado correctamente. Recibirás una confirmación por email.")
            .setPositiveButton("Aceptar") { _, _ ->
                // Navegar a la pantalla de confirmación o limpiar el carrito
                navigateToSuccess()
            }
            .setCancelable(false)
            .show()
    }
    
    private fun showLoading(show: Boolean) {
        // Aquí podrías mostrar un ProgressBar o similar
        if (show) {
            Toast.makeText(context, "Procesando pago...", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun openPaymentApp(url: String) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
            startActivity(intent)
        } catch (e: Exception) {
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
    
    private fun startPayPalCheckout(amount: Double) {
        val url = "http://192.168.224.188:3000/create-order"
        val json = JSONObject()
        json.put("amount", String.format("%.2f", amount))
        json.put("currency", "USD")

        val requestBody = json.toString().toRequestBody("application/json".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity?.runOnUiThread {
                    Toast.makeText(context, "Error de conexión con PayPal: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                if (response.isSuccessful && responseBody != null) {
                    try {
                        val approvalUrl = JSONObject(responseBody).getString("approvalUrl")
                        activity?.runOnUiThread {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(approvalUrl))
                            startActivity(intent)
                        }
                    } catch (ex: Exception) {
                        activity?.runOnUiThread {
                            Toast.makeText(context, "Respuesta inesperada de PayPal", Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    activity?.runOnUiThread {
                        Toast.makeText(context, "Error al crear la orden de PayPal", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
} 