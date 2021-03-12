package sk.lubostar.bignerdguide.photogallery.model

import androidx.recyclerview.widget.DiffUtil
import com.google.gson.annotations.SerializedName

data class GalleryItem(
    var title: String = "",
    var id: String = "",
    @SerializedName("url_s") var url: String = ""){

    companion object{
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryItem>() {

            override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem.url == newItem.url
            }
        }
    }
}