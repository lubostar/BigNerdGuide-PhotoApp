package sk.lubostar.bignerdguide.photogallery.view

import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.*
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.*
import kotlinx.android.synthetic.main.fragment_photo_gallery.*
import sk.lubostar.bignerdguide.photogallery.QueryPreferences
import sk.lubostar.bignerdguide.photogallery.viewmodel.PhotoGalleryViewModel
import sk.lubostar.bignerdguide.photogallery.R
import sk.lubostar.bignerdguide.photogallery.api.ThumbnailDownloader
import sk.lubostar.bignerdguide.photogallery.worker.PollWorker
import java.util.concurrent.TimeUnit

class PhotoGalleryFragment : VisibleFragment() {
    companion object {
        private const val TAG = "PhotoGalleryFragment"
        private const val POLL_WORK = "PW"

        fun newInstance() = PhotoGalleryFragment()
    }

    private lateinit var adapter : PagedPhotoAdapter
    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()
    private lateinit var thumbnailDownloader: ThumbnailDownloader<PagedPhotoAdapter.PhotoHolder>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true

        val responseHandler = Handler()
        thumbnailDownloader = ThumbnailDownloader(responseHandler, lifecycle) { photoHolder, bitmap ->
            val drawable = BitmapDrawable(resources, bitmap)
            photoHolder.bindImage(drawable)
        }

        val placeholder = ContextCompat.getDrawable(requireContext(), R.drawable.bill_up_close)
            ?: ColorDrawable()
        adapter = PagedPhotoAdapter(thumbnailDownloader, placeholder)

        lifecycle.addObserver(thumbnailDownloader.fragmentLifecycleObserver)

        setHasOptionsMenu(true)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View
    = inflater.inflate(R.layout.fragment_photo_gallery, container, false)

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

        viewLifecycleOwnerLiveData.observe(viewLifecycleOwner, {
            if (it != null) {
                viewLifecycleOwner.lifecycle.addObserver(thumbnailDownloader.viewLifecycleObserver)
            }
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.fragment_photo_gallery, menu)
        
        val searchItem: MenuItem = menu.findItem(R.id.menu_item_search)
        val searchView = searchItem.actionView as SearchView

        searchView.apply {
            setOnCloseListener {
                photoGalleryViewModel.fetchPhotos("")
                true
            }

            setOnSearchClickListener {
                searchView.setQuery(photoGalleryViewModel.searchTerm, false)
            }

            setOnQueryTextListener(object : SearchView.OnQueryTextListener{

                override fun onQueryTextSubmit(query: String): Boolean {
                    Log.d(TAG, "Query text submit: $query")
                    photoGalleryViewModel.fetchPhotos(query)
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    Log.d(TAG, "Query text change: $query")
                    return false
                }
            })
        }

        val toggleItem = menu.findItem(R.id.menu_item_toggle_polling)
        val isPolling = QueryPreferences.isPolling(requireContext())
        val toggleItemTitle = if (isPolling) {
            R.string.stop_polling
        } else {
            R.string.start_polling
        }
        toggleItem.setTitle(toggleItemTitle)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_clear -> {
                photoGalleryViewModel.fetchPhotos("")
                true
            }
            R.id.menu_item_toggle_polling -> {
                val isPolling = QueryPreferences.isPolling(requireContext())
                if (isPolling) {
                    WorkManager.getInstance().cancelUniqueWork(POLL_WORK)
                    QueryPreferences.setPolling(requireContext(), false)
                } else {
                    val constraints = Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.UNMETERED)
                        .build()
                    val periodicRequest = PeriodicWorkRequest
                        .Builder(PollWorker::class.java, 15, TimeUnit.MINUTES)
                        .setConstraints(constraints)
                        .build()
                    WorkManager.getInstance().enqueueUniquePeriodicWork(POLL_WORK,
                        ExistingPeriodicWorkPolicy.KEEP, periodicRequest)
                    QueryPreferences.setPolling(requireContext(), true)
                }
                activity?.invalidateOptionsMenu()
                true
            }
            else -> super.onOptionsItemSelected(item)
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
