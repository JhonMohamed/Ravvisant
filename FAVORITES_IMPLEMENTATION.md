# Implementación de Favoritos - Ravvisant

## Descripción General

Se ha implementado una funcionalidad completa de favoritos que permite a los usuarios guardar y gestionar sus productos favoritos usando Firebase Firestore como base de datos.

## Arquitectura

### 1. Capa de Datos (Data Layer)

#### FavoriteService (Singleton)
- **Ubicación**: `app/src/main/java/com/proyect/ravvisant/core/firebase/FavoriteService.kt`
- **Propósito**: Servicio global que maneja todas las operaciones de favoritos con Firebase
- **Funcionalidades**:
  - `getFavorites()`: Obtiene todos los favoritos del usuario
  - `addToFavorites(product)`: Agrega un producto a favoritos
  - `removeFromFavorites(productId)`: Elimina un producto de favoritos
  - `isFavorite(productId)`: Verifica si un producto está en favoritos
  - `toggleFavorite(product)`: Alterna el estado de favorito
  - `updateProductFavoriteStatus(products)`: Actualiza el estado de favoritos para una lista de productos

#### FavoriteRepository
- **Ubicación**: `app/src/main/java/com/proyect/ravvisant/domain/repository/FavoriteRepository.kt`
- **Propósito**: Capa de abstracción que utiliza FavoriteService
- **Funcionalidades**: Mismas que FavoriteService pero con interfaz más limpia

### 2. Capa de Presentación (Presentation Layer)

#### FavoriteViewModel
- **Ubicación**: `app/src/main/java/com/proyect/ravvisant/features/favorites/viewmodel/FavoriteViewModel.kt`
- **Propósito**: Maneja la lógica de negocio para la pantalla de favoritos
- **Estados**:
  - `favorites`: Lista de productos favoritos
  - `isLoading`: Estado de carga
  - `error`: Mensajes de error

#### FavoriteFragment
- **Ubicación**: `app/src/main/java/com/proyect/ravvisant/features/favorites/FavoriteFragment.kt`
- **Propósito**: Pantalla principal de favoritos
- **Características**:
  - RecyclerView con GridLayoutManager (2 columnas)
  - Estado de carga con ProgressBar
  - Estado vacío con mensaje y icono
  - Manejo de errores con Toast

#### FavoriteAdapter
- **Ubicación**: `app/src/main/java/com/proyect/ravvisant/features/favorites/adapter/FavoriteAdapter.kt`
- **Propósito**: Adaptador específico para la lista de favoritos
- **Características**:
  - Reutiliza el layout `item_product_grid.xml`
  - El botón de favorito elimina el producto de la lista
  - Mantiene funcionalidad de agregar al carrito y navegación

### 3. Integración con Otras Pantallas

#### HomeViewModel
- **Actualización**: Integra la funcionalidad de favoritos
- **Métodos**:
  - `loadProductsWithFavorites()`: Carga productos con estado de favoritos
  - `toggleFavorite(product)`: Alterna favoritos con Firebase
  - `refreshProducts()`: Recarga productos con estado actualizado

#### ProductViewModel
- **Actualización**: Similar a HomeViewModel
- **Características**: Mantiene sincronización con Firebase en tiempo real

## Estructura de Datos en Firebase

```
users/
  {userId}/
    favorites/
      {productId}/
        - id: "productId"
        - name: "Product Name"
        - brand: "Brand"
        - price: 99.99
        - rating: 4.5
        - stock: 10
        - imageUrls: ["url1", "url2"]
        - description: "Description"
        - categoryId: "categoryId"
        - isFavorite: true
```

## Uso

### 1. Agregar/Quitar Favoritos
```kotlin
// En cualquier ViewModel
viewModel.toggleFavorite(product)
```

### 2. Verificar Estado de Favorito
```kotlin
// En el layout XML
android:src="@{product.isFavorite ? @drawable/ic_heart_fill : @drawable/ic_heart_outline}"
```

### 3. Navegar a Favoritos
```kotlin
// Usando Navigation Component
findNavController().navigate(R.id.favoriteFragment)
```

## Características Implementadas

✅ **Funcionalidad Completa**: Agregar/quitar favoritos
✅ **Persistencia**: Datos guardados en Firebase Firestore
✅ **Sincronización**: Estado actualizado en todas las pantallas
✅ **UI Responsiva**: Estados de carga, vacío y error
✅ **Navegación**: Integración con Navigation Component
✅ **Arquitectura Limpia**: Separación de responsabilidades
✅ **Reutilización**: Adaptadores y layouts compartidos

## Próximas Mejoras Sugeridas

1. **Notificaciones**: Notificar cuando un producto favorito cambie de precio
2. **Sincronización Offline**: Cache local para funcionamiento sin internet
3. **Filtros**: Filtrar favoritos por categoría o precio
4. **Compartir**: Compartir lista de favoritos
5. **Backup**: Exportar/importar favoritos

## Notas Técnicas

- **Autenticación**: Requiere usuario autenticado para funcionar
- **Concurrencia**: Usa coroutines para operaciones asíncronas
- **Error Handling**: Manejo robusto de errores de red
- **Performance**: Consultas optimizadas a Firebase 