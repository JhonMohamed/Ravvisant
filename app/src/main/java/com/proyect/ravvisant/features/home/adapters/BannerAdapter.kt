import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.proyect.ravvisant.core.utils.diff.ProductDiffCallback
import com.proyect.ravvisant.databinding.ItemBannerBinding
import com.proyect.ravvisant.domain.model.Product

class  BannerAdapter(
    private val context: Context,
    private val onBannerClick: (Product) -> Unit
) : ListAdapter<Product, BannerAdapter.BannerViewHolder>(ProductDiffCallback()) {

    inner class BannerViewHolder(val binding: ItemBannerBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BannerViewHolder {
        val binding = ItemBannerBinding.inflate(LayoutInflater.from(context), parent, false)
        return BannerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        val product = getItem(position)
        Glide.with(context).load(product.imageUrls.firstOrNull()).into(holder.binding.bannerImage)

        holder.itemView.setOnClickListener {
            onBannerClick(product)
        }
    }
}