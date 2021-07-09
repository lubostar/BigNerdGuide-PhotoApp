package sk.lubostar.bignerdguide.photogallery.model

import android.net.Uri
import androidx.recyclerview.widget.DiffUtil
import com.google.gson.annotations.SerializedName

data class GalleryItem(var title: String = "",
                       var id: String = "",
                       @SerializedName("url_s") var url: String = "",
                       @SerializedName("owner") var owner: String) {

    companion object {
        val DIFF_CALLBACK = object : DiffUtil.ItemCallback<GalleryItem>() {

            override fun areItemsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: GalleryItem, newItem: GalleryItem): Boolean {
                return oldItem.url == newItem.url
            }
        }
    }

    val photoPageUri: Uri
        get() {
            return Uri.parse("https://www.flickr.com/photos/")
                .buildUpon()
                .appendPath(owner)
                .appendPath(id)
                .build()
        }
}