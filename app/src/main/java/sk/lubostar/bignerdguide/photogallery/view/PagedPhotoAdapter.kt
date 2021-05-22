package sk.lubostar.bignerdguide.photogallery.view

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.RecyclerView
import sk.lubostar.bignerdguide.photogallery.R
import sk.lubostar.bignerdguide.photogallery.api.ThumbnailDownloader
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem

class PagedPhotoAdapter(private val thumbnailDownloader: ThumbnailDownloader<PhotoHolder>,
                        private val placeholder: Drawable):
    PagedListAdapter<GalleryItem, PagedPhotoAdapter.PhotoHolder>(GalleryItem.DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
        val root = LayoutInflater.from(parent.context).inflate(
            R.layout.list_item_gallery,
            parent, false) as ImageView
        return PhotoHolder(root)
    }

    override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
        getItem(position)?.let {
            thumbnailDownloader.queueThumbnail(holder, it.url)
        }

        holder.bindImage(placeholder)
    }

    class PhotoHolder(rootView: ImageView) : RecyclerView.ViewHolder(rootView) {
        val bindImage: (Drawable) -> Unit = rootView::setImageDrawable
    }
}

