package com.example.wellhoney.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.wellhoney.data.models.CartItem
import com.example.wellhoney.data.models.HoneyProduct
import com.example.wellhoney.data.models.Order
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CartViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val userId: String? = auth.currentUser?.uid
    private val database = FirebaseDatabase.getInstance(
        "https://wellhoney-41b11-default-rtdb.europe-west1.firebasedatabase.app"
    )
    private val cartRef = database.getReference("cart")
    private val ordersRef = database.getReference("orders")

    private val _cartItems = MutableLiveData<List<CartItem>>()
    val cartItems: LiveData<List<CartItem>> = _cartItems

    private var products: List<HoneyProduct> = emptyList()

    fun setProducts(productsList: List<HoneyProduct>) {
        products = productsList
    }

    private val _orderResult = MutableLiveData<Result<Boolean>>()
    val orderResult: LiveData<Result<Boolean>> = _orderResult

    init {
        loadCart()
    }

    private fun getProductRef(productId: String, volume: String): DatabaseReference? {
        val uid = userId ?: return null
        return cartRef.child(uid).child(productId).child(volume)
    }

    private fun loadCart() {
        if (userId == null) {
            _cartItems.value = emptyList()
            return
        }
        cartRef.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val items = mutableListOf<CartItem>()
                for (child in snapshot.children) { // child = productId
                    for (volumeChild in child.children) { // volume = 500ml / 1l
                        val quantity = volumeChild.child("quantity").getValue(Int::class.java) ?: 0
                        if (quantity > 0) {
                            items.add(CartItem(child.key!!, quantity, volumeChild.key!!))
                        }
                    }
                }
                _cartItems.value = items
            }
            override fun onCancelled(error: DatabaseError) {
                _cartItems.value = emptyList()
            }
        })
    }

    fun addToCart(productId: String, volume: String) {
        val productRef = getProductRef(productId, volume) ?: return

        productRef.get().addOnSuccessListener { snapshot ->
            val currentQuantity = snapshot.child("quantity").getValue(Int::class.java) ?: 0
            updateQuantity(productId, volume, currentQuantity + 1)
        }
    }

    fun increaseQuantity(productId: String, volume: String) {
        val productRef = getProductRef(productId, volume) ?: return
        productRef.get().addOnSuccessListener { snapshot ->
            val currentQuantity = snapshot.child("quantity").getValue(Int::class.java) ?: 0
            updateQuantity(productId, volume, currentQuantity + 1)
        }
    }

    fun decreaseQuantity(productId: String, volume: String) {
        val productRef = getProductRef(productId, volume) ?: return
        productRef.get().addOnSuccessListener { snapshot ->
            val currentQuantity = snapshot.child("quantity").getValue(Int::class.java) ?: 0
            updateQuantity(productId, volume, currentQuantity - 1)
        }
    }

    fun removeFromCart(productId: String, volume: String) {
        val productRef = getProductRef(productId, volume) ?: return
        productRef.removeValue()
    }

    fun updateCartItem(productId: String, volume: String, quantity: Int) {
        updateQuantity(productId, volume, quantity)
    }

    fun getQuantity(productId: String, volume: String): Int? {
        return _cartItems.value
            ?.find { it.productId == productId && it.volume == volume }
            ?.quantity
    }

    private fun updateQuantity(productId: String, volume: String, newQuantity: Int) {
        val productRef = getProductRef(productId, volume) ?: return
        if (newQuantity > 0) {
            productRef.child("quantity").setValue(newQuantity)
        } else {
            productRef.removeValue()
        }
    }

    fun placeOrder() {
        if (userId == null) {
            _orderResult.postValue(Result.failure(Exception("Пользователь не авторизован")))
            return
        }

        cartRef.child(userId).get().addOnSuccessListener { snapshot ->
            if (!snapshot.exists()) {
                _orderResult.postValue(Result.failure(Exception("Корзина пуста")))
                return@addOnSuccessListener
            }

            val items = mutableMapOf<String, MutableMap<String, Int>>() // productId -> volume -> quantity
            var totalPrice = 0

            for (productSnapshot in snapshot.children) {
                val productId = productSnapshot.key ?: continue
                val product = products.find { it.id == productId } ?: continue

                for (volumeSnapshot in productSnapshot.children) {
                    val volume = volumeSnapshot.key ?: continue
                    val quantity = volumeSnapshot.child("quantity").getValue(Int::class.java) ?: 0
                    if (quantity <= 0) continue

                    val volumeMap = items.getOrPut(productId) { mutableMapOf() }
                    volumeMap[volume] = quantity

                    val price = when (volume) {
                        "500ml" -> product.price_500ml
                        "1l" -> product.price_1l
                        else -> 0
                    }
                    totalPrice += price * quantity
                }
            }

            val lastOrderNumberRef = database.reference.child("lastOrderNumber")
            lastOrderNumberRef.runTransaction(object : Transaction.Handler {
                override fun doTransaction(currentData: MutableData): Transaction.Result {
                    var currentNumber = currentData.getValue(Long::class.java) ?: 0L
                    currentNumber++
                    currentData.value = currentNumber
                    return Transaction.success(currentData)
                }

                override fun onComplete(error: DatabaseError?, committed: Boolean, currentData: DataSnapshot?) {
                    if (error != null || !committed) {
                        _orderResult.postValue(Result.failure(error?.toException() ?: Exception("Не удалось создать номер заказа")))
                        return
                    }

                    val newOrderNumber = currentData?.getValue(Long::class.java) ?: 0L
                    val ordersUserRef = ordersRef.child(userId)
                    val newOrderId = ordersUserRef.push().key ?: System.currentTimeMillis().toString()

                    // Создаём Map вместо объекта Order
                    val orderMap = mapOf(
                        "orderId" to newOrderId,
                        "orderNumber" to newOrderNumber,
                        "items" to items,
                        "totalPrice" to totalPrice,
                        "timestamp" to System.currentTimeMillis()
                    )

                    ordersUserRef.child(newOrderId).setValue(orderMap)
                        .addOnSuccessListener {
                            cartRef.child(userId).removeValue() // очищаем корзину
                            _orderResult.postValue(Result.success(true))
                        }
                        .addOnFailureListener { e ->
                            _orderResult.postValue(Result.failure(e))
                        }
                }
            })
        }.addOnFailureListener { e ->
            _orderResult.postValue(Result.failure(e))
        }
    }



}

