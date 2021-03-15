package sk.lubostar.bignerdguide.photogallery.view

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_photo_gallery.*
import sk.lubostar.bignerdguide.photogallery.viewmodel.PhotoGalleryViewModel
import sk.lubostar.bignerdguide.photogallery.R
import sk.lubostar.bignerdguide.photogallery.api.ThumbnailDownloader
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem

class PhotoGalleryFragment : Fragment() {
    companion object {
        private const val TAG = "PhotoGalleryFragment"

        fun newInstance() = PhotoGalleryFragment()
    }

    private val adapter = PhotoAdapter()
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindImage(drawable)
        }

        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
        return inflater.inflate(R.layout.fragment_photo_gallery, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo_recycler_view.adapter = adapter
        photo_recycler_view.afterMeasured {
            val columnCount = width / 360
            photo_recycler_view.layoutManager = GridLayoutManager(context, columnCount)
        }

        photoGalleryViewModel.getPagedListLiveData().observe(viewLifecycleOwner, { galleryItems ->
            Log.d(TAG, "Have gallery items from ViewModel $galleryItems")
            adapter.submitList(galleryItems)
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycle.removeObserver(thumbnailDownloader.viewLifecycleObserver)
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycle.removeObserver(thumbnailDownloader.fragmentLifecycleObserver)
    }

    private class PhotoHolder(rootView: ImageView) : RecyclerView.ViewHolder(rootView) {
//        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
        val bindImage: (Drawable) -> Unit = rootView::setImageDrawable
    }

    private inner class PhotoAdapter: PagedListAdapter<GalleryItem, PhotoHolder>(GalleryItem.DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val root = layoutInflater.inflate(R.layout.list_item_gallery,
                parent, false) as ImageView
            return PhotoHolder(root)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            getItem(position)?.let {
                thumbnailDownloader.queueThumbnail(holder, it.url)
            }
            val placeholder = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
                ?: ColorDrawable()
            holder.bindImage(placeholder)
        }
    }

    private inline fun RecyclerView.afterMeasured(crossinline f: View.() -> Unit) {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                if (measuredWidth > 0 && measuredHeight > 0) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                    f()
                }
            }
        })
    }
}
