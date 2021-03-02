package sk.lubostar.bignerdguide.photogallery

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import sk.lubostar.bignerdguide.photogallery.api.FlickrFetchr

class PhotoGalleryViewModel : ViewModel() {
    val galleryItemLiveData: LiveData<List<GalleryItem>>

    init {
        galleryItemLiveData = FlickrFetchr().fetchPhotos()
    }
}