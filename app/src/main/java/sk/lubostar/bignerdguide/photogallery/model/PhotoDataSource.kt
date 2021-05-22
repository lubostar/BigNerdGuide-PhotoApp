package sk.lubostar.bignerdguide.photogallery.model

import androidx.paging.PageKeyedDataSource
import sk.lubostar.bignerdguide.photogallery.api.FlickrFetchr

class PhotoDataSource(private val searchQuery: String) : PageKeyedDataSource<Long, GalleryItem> () {
    companion object {
        private const val TAG = "PhotoDataSource"
    }

    private val dataService = FlickrFetchr()

    override fun loadInitial(params: LoadInitialParams<Long>, callback: LoadInitialCallback<Long, GalleryItem>) {
        if (searchQuery.isNotBlank()) {
            dataService.searchPhotos(searchQuery) { galleryItems ->
                callback.onResult(galleryItems, null, null)
            }
        } else {
            dataService.fetchPhotos(1) { galleryItems ->
                callback.onResult(galleryItems, null, 2)
            }
        }
    }

    override fun loadBefore(params: LoadParams<Long>, callback: LoadCallback<Long, GalleryItem>) {}

    override fun loadAfter(params: LoadParams<Long>, callback: LoadCallback<Long, GalleryItem>) {
        dataService.fetchPhotos(params.key) { galleryItems ->
            callback.onResult(galleryItems, params.key + 1)
        }
    }
}