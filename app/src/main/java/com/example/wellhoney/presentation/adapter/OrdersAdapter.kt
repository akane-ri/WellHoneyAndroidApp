import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wellhoney.data.models.Order
import com.example.wellhoney.databinding.ItemOrderBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OrdersAdapter(
    private val orders: List<Order>,
    private val onOrderClick: (Order) -> Unit
) : RecyclerView.Adapter<OrdersAdapter.OrderViewHolder>() {

    inner class OrderViewHolder(val binding: ItemOrderBinding) : RecyclerView.ViewHolder(binding.root)

    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val order = orders[position]
        holder.binding.apply {
            tvOrderNumber.text = "Заказ №${order.orderNumber}"
            tvOrderTotal.text = "${order.totalPrice} ₽"
            tvOrderDate.text = formatTimestamp(order.timestamp)
            tvItemsCount.text = "Товаров: ${order.items.size}"

            root.setOnClickListener {
                onOrderClick(order)
            }
        }
    }

    override fun getItemCount() = orders.size
}
