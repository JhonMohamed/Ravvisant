package com.proyect.ravvisant.features.product.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.proyect.ravvisant.domain.model.Product
import com.proyect.ravvisant.domain.model.CartItem
import com.proyect.ravvisant.domain.repository.FavoriteRepository
import com.proyect.ravvisant.domain.repository.CartRepository
import android.content.Context
import android.util.Log
import android.widget.Toast
import com.proyect.ravvisant.domain.model.Category
import com.proyect.ravvisant.features.categories.adapter.CategoryAdapter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {

    private val _products = MutableStateFlow<List<Product>>(emptyList())
    val products: StateFlow<List<Product>> = _products
    private var allProducts = emptyList<Product>()


    private val _categories = MutableStateFlow<List<Category>>(emptyList())
    val categories: StateFlow<List<Category>> = _categories

    private val firestore = FirebaseFirestore.getInstance()
    private val favoriteRepository = FavoriteRepository()
    private val cartRepository = CartRepository()

    init {
        loadCategoriesFromFirebase()
        loadProducts()
    }

    private fun loadCategoriesFromFirebase() {
        firestore.collection("categories")
            .get()
            .addOnSuccessListener { result ->
                val categoryList = result.toObjects(Category::class.java)
                _categories.value = categoryList
            }
            .addOnFailureListener { exception ->
                Log.e("HomeViewModel", "Error al cargar categorías", exception)
            }
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
                    allProducts = productList
                    _products.value = productList
                }
            }
            }
    fun filterProductsByCategory(categoryId: String) {
        viewModelScope.launch {
            if (categoryId.isEmpty() || categoryId == "all") {
                _products.value = allProducts
            } else {
                val filteredProducts = allProducts.filter { it.categoryId == categoryId }
                _products.value = filteredProducts
            }
        }
    }


    fun toggleFavorite(product: Product) {
        viewModelScope.launch {
            try {
                val success = favoriteRepository.toggleFavorite(product)
                if (success) {
                    // Actualizar la lista local
                    val currentProducts = _products.value.toMutableList()
                    val index = currentProducts.indexOfFirst { it.id == product.id }
                    if (index != -1) {
                        val updatedProduct = product.copy(isFavorite = !product.isFavorite)
                        currentProducts[index] = updatedProduct
                        _products.value = currentProducts
                    }
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al actualizar favoritos", e)
            }
        }
    }

    fun addToCart(product: Product, context: Context) {
        viewModelScope.launch {
            try {
                val cartItem = CartItem(
                    id = product.id,
                    name = product.name,
                    imageUrl = product.imageUrls.firstOrNull() ?: "",
                    price = product.price,
                    quantity = 1
                )
                
                val success = cartRepository.addToCart(cartItem)
                if (success) {
                    Toast.makeText(context, "${product.name} agregado al carrito", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("ProductViewModel", "Error al agregar al carrito", e)
                Toast.makeText(context, "Error al agregar al carrito", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Recargar productos con estado de favoritos actualizado
    fun refreshProducts() {
        viewModelScope.launch {
            val currentProducts = _products.value
            if (currentProducts.isNotEmpty()) {
                val productsWithFavorites = favoriteRepository.updateProductFavoriteStatus(currentProducts)
                _products.value = productsWithFavorites
            }
        }
    }

//        fun uploadSampleCategoriesToFirebase(context: Context) {
//        val batch = firestore.batch()
//
//        // Datos de prueba para categorías
//        val sampleCategories = listOf(
//            Category(
//                id = "6",
//                name = "Musica",
//                itemCount = 156,
//                iconUrl = "https://res.cloudinary.com/dljanm8ai/image/upload/v1751089392/categoria_joyas_s0ygjf.png"
//            ),
//        )
//        _categories.value = sampleCategories
//
//
//        for (category in sampleCategories) {
//            val documentRef = firestore.collection("categories").document(category.id)
//            batch.set(documentRef, category)
//        }
//
//        batch.commit().addOnSuccessListener {
//            Toast.makeText(context, "Categorías subidas correctamente", Toast.LENGTH_SHORT).show()
//        }.addOnFailureListener { exception ->
//            Toast.makeText(context, "Error al subir categorías", Toast.LENGTH_SHORT).show()
//            Log.e("HomeViewModel", "Error al subir categorías", exception)
//        }
//    }

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
