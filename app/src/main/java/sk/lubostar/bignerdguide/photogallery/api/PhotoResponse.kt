package sk.lubostar.bignerdguide.photogallery.api

import com.google.gson.annotations.SerializedName
import sk.lubostar.bignerdguide.photogallery.GalleryItem

class PhotoResponse {
    @SerializedName("photo")
    lateinit var galleryItems: List<GalleryItem>
}