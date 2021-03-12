package sk.lubostar.bignerdguide.photogallery.model

import androidx.lifecycle.MutableLiveData
import androidx.paging.DataSource

class PhotoDataSourceFactory : DataSource.Factory<Long, GalleryItem>() {

    private val mutableLiveData = MutableLiveData<PhotoDataSource>()
    private lateinit var photoDataSource: PhotoDataSource

    override fun create(): DataSource<Long, GalleryItem> {
        photoDataSource = PhotoDataSource()
        mutableLiveData.postValue(photoDataSource)
        return photoDataSource
    }

    fun getMutableLiveData() = mutableLiveData
}