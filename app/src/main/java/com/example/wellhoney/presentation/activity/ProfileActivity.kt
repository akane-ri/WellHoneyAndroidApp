package com.example.wellhoney.presentation.activity

import OrdersAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.wellhoney.data.HoneyRepository
import com.example.wellhoney.data.models.HoneyProduct
import com.example.wellhoney.data.models.Order
import com.example.wellhoney.databinding.ActivityProfileBinding
import com.example.wellhoney.presentation.ui.OrderDetailsFragment
import com.example.wellhoney.presentation.viewmodel.AuthViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var ordersAdapter: OrdersAdapter
    private val ordersList = mutableListOf<Order>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repository = HoneyRepository(this)
        val productsMap = repository.loadHoneyProducts().associateBy { it.id }

        binding.rvOrders.layoutManager = LinearLayoutManager(this)

        ordersAdapter = OrdersAdapter(ordersList) { order ->
            val bottomSheet = OrderDetailsFragment.newInstance(order.items, productsMap)
            bottomSheet.show(supportFragmentManager, "OrderDetails")
        }
        binding.rvOrders.adapter = ordersAdapter

        loadOrdersFromFirebase()

        binding.buttonLogout.setOnClickListener {
            authViewModel.logout()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
//переделать
    private fun loadOrdersFromFirebase() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val ref = FirebaseDatabase.getInstance(
            "https://wellhoney-41b11-default-rtdb.europe-west1.firebasedatabase.app"
        ).getReference("orders").child(uid)

        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                ordersList.clear()

                for (orderSnapshot in snapshot.children) {
                    val key = orderSnapshot.key ?: continue
                    val timestamp = orderSnapshot.child("timestamp").getValue(Long::class.java) ?: 0L
                    val orderNumber = orderSnapshot.child("orderNumber").getValue(Long::class.java) ?: 0L
                    val totalPrice = orderSnapshot.child("totalPrice").getValue(Long::class.java)?.toInt() ?: 0

                    val rawItems = orderSnapshot.child("items").value

                    val items: Map<String, Map<String, Int>> = when(rawItems) {
                        is Map<*, *> -> {
                            val firstValue = rawItems.values.firstOrNull()
                            if (firstValue is Map<*, *>) {
                                // новые заказы с volume
                                rawItems.mapNotNull { (productId, volumesAny) ->
                                    val id = productId?.toString() ?: return@mapNotNull null
                                    val volumeMap = volumesAny as? Map<*, *> ?: return@mapNotNull null
                                    val safeMap = volumeMap.mapNotNull { (vol, qty) ->
                                        val volStr = vol?.toString() ?: return@mapNotNull null
                                        val qtyInt = (qty as? Long)?.toInt() ?: return@mapNotNull null
                                        volStr to qtyInt
                                    }.toMap()
                                    id to safeMap
                                }.toMap()
                            } else {
                                // старые заказы без volume
                                rawItems.mapNotNull { (productId, qtyAny) ->
                                    val id = productId?.toString() ?: return@mapNotNull null
                                    val qtyInt = (qtyAny as? Long)?.toInt() ?: return@mapNotNull null
                                    id to mapOf("500ml" to qtyInt)
                                }.toMap()
                            }
                        }
                        else -> emptyMap()
                    }

                    val order = Order(
                        orderId = key,
                        orderNumber = orderNumber,
                        items = items,
                        totalPrice = totalPrice,
                        timestamp = timestamp
                    )
                    ordersList.add(order)
                }

                ordersAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProfileActivity", "Error loading orders: ${error.message}")
            }
        })
    }

}