package sk.lubostar.bignerdguide.photogallery.model

import androidx.paging.DataSource

class PhotoDataSourceFactory : DataSource.Factory<Long, GalleryItem>() {

    private lateinit var photoDataSource: PhotoDataSource

    private var searchQuery: String = ""

    override fun create(): DataSource<Long, GalleryItem> {
        photoDataSource = PhotoDataSource(searchQuery)
        return photoDataSource
    }

    fun setSearchQuery(query: String) {
        searchQuery = query
        photoDataSource.invalidate()
    }
}