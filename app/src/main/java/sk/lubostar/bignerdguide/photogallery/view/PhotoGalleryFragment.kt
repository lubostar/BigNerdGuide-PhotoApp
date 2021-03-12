package sk.lubostar.bignerdguide.photogallery.view

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_photo_gallery.*
import sk.lubostar.bignerdguide.photogallery.viewmodel.PhotoGalleryViewModel
import sk.lubostar.bignerdguide.photogallery.R
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem

class PhotoGalleryFragment : Fragment() {
    companion object {
        private const val TAG = "PhotoGalleryFragment"

        fun newInstance() = PhotoGalleryFragment()
    }

    private val adapter = PhotoAdapter()
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?)
    : View = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

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

    private class PhotoHolder(itemTextView: TextView) : RecyclerView.ViewHolder(itemTextView) {
        val bindTitle: (CharSequence) -> Unit = itemTextView::setText
    }

    private class PhotoAdapter: PagedListAdapter<GalleryItem, PhotoHolder>(GalleryItem.DIFF_CALLBACK) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoHolder {
            val textView = TextView(parent.context)
            return PhotoHolder(textView)
        }

        override fun onBindViewHolder(holder: PhotoHolder, position: Int) {
            getItem(position)?.let {
                holder.bindTitle(it.title)
            }
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
