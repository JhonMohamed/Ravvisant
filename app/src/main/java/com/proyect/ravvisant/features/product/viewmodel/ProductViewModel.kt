package com.proyect.ravvisant.features.product.viewmodel

import androidx.lifecycle.ViewModel
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.Product
import android.content.Context
import android.util.Log
import android.widget.Toast
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class ProductViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products

    private val firestore = FirebaseFirestore.getInstance()

    init {
        loadProducts()
    }

    // Carga inicial de productos
    private fun loadProducts() {
        firestore.collection("products")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("ProductViewModel", "Error al cargar productos", error)
                    return@addSnapshotListener
                }

                if (snapshot != null && !snapshot.isEmpty) {
                    val productList = snapshot.toObjects(Product::class.java)
                    _products.value = productList
                }
            }
    }

    fun toggleFavorite(product: Product) {
        val currentProducts = _products.value.toMutableList()
        val index = currentProducts.indexOfFirst { it.id == product.id }

        if (index != -1) {
            val updatedProduct = product.copy(isFavorite = !product.isFavorite)
            currentProducts[index] = updatedProduct
            _products.value = currentProducts
        }
    }
//    private val sampleProducts = listOf(
//        Product(
//            id = "1",
//            name = "Nautilus Cara Azul",
//            brand = "Patek Philippe",
//            price = 79999.99,
//            rating = 4.9f,
//            stock = 5,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_patek_azul_2_jrli4f.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609107/reloj_patek_azul_b59ygx.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609107/reloj_patek_azul_3_halvou.webp"
//            ),
//            description = "Reloj Patek Philippe Nautilus con carátula azul y correa de acero. Elegancia suiza atemporal.",
//            categoryId = "2"
//        ),
//        Product(
//            id = "2",
//            name = "Cosmograph Daytona Verde",
//            brand = "Rolex",
//            price = 45999.99,
//            rating = 4.8f,
//            stock = 3,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609106/reloj_cronos_verde_ad2ejo.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609107/reloj_crono_verde_2_yvwjyj.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/reloj_crono_verde_3_xl8s6w.jpg"
//            ),
//            description = "Rolex Daytona verde con cronógrafo de alta precisión. Un clásico deportivo y lujoso.",
//            categoryId = "2"
//        ),
//        Product(
//            id = "3",
//            name = "RM 055 White Skeleton",
//            brand = "Richard Mille",
//            price = 279999.99,
//            rating = 4.9f,
//            stock = 2,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609960/reloj_transparente_xq9c8l.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609959/reloj_transparente_2_tvztgy.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609964/reloj_transparente_3_jm2mku.jpg"
//            ),
//            description = "Richard Mille con caja blanca y diseño esqueletado. Alta tecnología y exclusividad.",
//            categoryId = "2"
//        ),
//        Product(
//            id = "4",
//            name = "Submariner Starbucks",
//            brand = "Rolex",
//            price = 18999.99,
//            rating = 4.7f,
//            stock = 6,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615153/rolex_starbucks3_djf9vt.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615155/rolex_starbucks_w0f2co.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615159/rolex_starbucks2_dmrjwk.jpg"
//            ),
//            description = "Rolex Submariner con bisel verde cerámico. Estilo icónico con resistencia al agua.",
//            categoryId = "2"
//        ),
//        Product(
//            id = "5",
//            name = "Royal Oak Offshore",
//            brand = "Audemars Piguet",
//            price = 33999.99,
//            rating = 4.8f,
//            stock = 4,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615156/reloj_ap_royal3_je9qhm.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615158/reloj_ap_royal2_iwkaxb.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749615160/reloj_ap_royal_urkk6v.jpg"
//            ),
//            description = "Audemars Piguet Royal Oak Offshore. Diseño robusto y deportivo con clase.",
//            categoryId = "2"
//        ),
//        Product(
//            id = "6",
//            name = "Cadena Cubana Dorada",
//            brand = "Golden Link",
//            price = 89.99,
//            rating = 4.6f,
//            stock = 12,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609109/img_collar_dorado_ugo2r6.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609108/img_colar_dorado_2_srcvb2.jpg"
//            ),
//            description = "Cadena tipo cubana con baño dorado. Perfecta para un look llamativo y moderno.",
//            categoryId = "1"
//        ),
//        Product(
//            id = "7",
//            name = "Pulsera Acero Gitana",
//            brand = "Steel Charm",
//            price = 64.99,
//            rating = 4.5f,
//            stock = 8,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609109/pulsera_india_3_dgxxgc.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609110/pulsera_gruesa_india_2_iprlbb.webp"
//            ),
//            description = "Pulsera de acero estilo gitano. Robusta, única y con fuerte presencia.",
//            categoryId = "1"
//        ),
//        Product(
//            id = "8",
//            name = "Aretes Circulares de Plata",
//            brand = "Luna Plata",
//            price = 39.99,
//            rating = 4.7f,
//            stock = 20,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609963/aretes_plata_2_brik4q.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609965/aretes_plata_og1j5i.webp"
//            ),
//            description = "Aretes circulares en plata con diseño minimalista. Elegancia discreta para cualquier outfit.",
//            categoryId = "1"
//        ),
//        Product(
//            id = "9",
//            name = "Pulsera de Cuarzo Negra",
//            brand = "Element Quartz",
//            price = 29.99,
//            rating = 4.4f,
//            stock = 15,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749609966/pulsera_cuarzo_negra2_ffs2zm.webp",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749614778/pulsera_cuarzo3_innbhv.webp"
//            ),
//            description = "Pulsera con cuentas de cuarzo negro. Estilo relajado y con energía.",
//            categoryId = "1"
//        ),
//        Product(
//            id = "10",
//            name = "Cadena con Cruz Dorada",
//            brand = "Divine Gold",
//            price = 74.99,
//            rating = 4.6f,
//            stock = 10,
//            imageUrls = listOf(
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749616912/collar_cruz3_a8qnxb.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749616913/collar_cruz_xl3oak.jpg",
//                "https://res.cloudinary.com/dljanm8ai/image/upload/v1749616915/collar_cruz2_jwf9wb.webp"
//            ),
//            description = "Cadena dorada con cruz detallada. Estilo clásico con toque espiritual moderno.",
//            categoryId = "1"
//        )
//    )
//
//    fun uploadSampleProductsToFirebase(context: Context) {
//        val firestore = FirebaseFirestore.getInstance()
//        val batch = firestore.batch()
//
//        for (product in sampleProducts) {
//            val documentRef = firestore.collection("products").document(product.id)
//            batch.set(documentRef, product)
//        }
//
//        batch.commit().addOnSuccessListener {
//            Toast.makeText(context, "Productos subidos correctamente", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener { exception ->
//            Log.e("ProductViewModel", "Error al subir productos", exception)
//            Toast.makeText(context, "Error al subir productos", Toast.LENGTH_SHORT).show()
//        }
//    }
}