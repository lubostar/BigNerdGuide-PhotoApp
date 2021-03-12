package sk.lubostar.bignerdguide.photogallery.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem
import sk.lubostar.bignerdguide.photogallery.model.PhotoDataSourceFactory
import java.util.concurrent.Executors

class PhotoGalleryViewModel : ViewModel() {
    private var pagedListLiveData: LiveData<PagedList<GalleryItem>>

    init {
        val photoDataSourceFactory = PhotoDataSourceFactory()
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(20)
            .setPageSize(100)
            .setPrefetchDistance(4)
            .build()

        val executor = Executors.newFixedThreadPool(5)
        pagedListLiveData = LivePagedListBuilder(photoDataSourceFactory, config)
            .setFetchExecutor(executor)
            .build()
    }

    fun getPagedListLiveData() = pagedListLiveData
}