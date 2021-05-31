package sk.lubostar.bignerdguide.photogallery.viewmodel

import android.app.Application
import androidx.lifecycle.*
import androidx.paging.LivePagedListBuilder
import androidx.paging.PagedList
import sk.lubostar.bignerdguide.photogallery.QueryPreferences
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem
import sk.lubostar.bignerdguide.photogallery.model.PhotoDataSourceFactory
import java.util.concurrent.Executors

class PhotoGalleryViewModel(private val app: Application) : AndroidViewModel(app) {
    private var pagedListLiveData: LiveData<PagedList<GalleryItem>>

    private val photoDataSourceFactory = PhotoDataSourceFactory()

    private val mutableSearchTerm = MutableLiveData<String>()

    val searchTerm: String get() = mutableSearchTerm.value ?: ""

    init {
        val config = PagedList.Config.Builder()
            .setEnablePlaceholders(true)
            .setInitialLoadSizeHint(20)
            .setPageSize(100)
            .setPrefetchDistance(4)
            .build()

        mutableSearchTerm.value = QueryPreferences.getStoredQuery(app)

        val executor = Executors.newFixedThreadPool(5)

        pagedListLiveData = Transformations.switchMap(mutableSearchTerm) { searchTerm ->
            if (searchTerm.isNotBlank()) {
                photoDataSourceFactory.setSearchQuery(searchTerm)
            }

            LivePagedListBuilder(photoDataSourceFactory, config)
                .setFetchExecutor(executor)
                .build()
        }
    }

    fun fetchPhotos(query: String = "") {
        QueryPreferences.setStoredQuery(app, query)
        photoDataSourceFactory.setSearchQuery(query)
    }

    fun getPagedListLiveData() = pagedListLiveData
}