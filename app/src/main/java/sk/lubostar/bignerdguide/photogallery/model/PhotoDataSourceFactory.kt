package sk.lubostar.bignerdguide.photogallery.model

import androidx.paging.DataSource

class PhotoDataSourceFactory : DataSource.Factory<Long, GalleryItem>() {

    private var photoDataSource: PhotoDataSource? = null

    private var searchQuery: String = ""

    override fun create() = PhotoDataSource(searchQuery)

    fun setSearchQuery(query: String) {
        searchQuery = query
        photoDataSource?.apply {
            invalidate()
        }
    }
}