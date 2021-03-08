package sk.lubostar.bignerdguide.photogallery.api

import android.util.Log
import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import sk.lubostar.bignerdguide.photogallery.GalleryItem
import java.lang.reflect.Type

class PhotoDeserializer : JsonDeserializer<PhotoResponse> {
    companion object{
        private const val TAG = "PhotoDeserializer"
    }

    override fun deserialize(json: JsonElement?, typeOfT: Type?,
                             context: JsonDeserializationContext?): PhotoResponse {
        val list = ArrayList<GalleryItem>()
        val response = PhotoResponse()

        json?.asJsonObject?.let {
            val photosJson = it.getAsJsonObject("photos")
            val photosArray = photosJson.getAsJsonArray("photo")

            for (element in photosArray) {
                Log.d(TAG, "parsing $element")
                with(element.asJsonObject) {
                    val galleryItem = GalleryItem(get("title").asString, get("id").asString, get("url_s").asString)
                    list.add(galleryItem)
                }
            }
        }

        response.galleryItems = list
        return response
    }
}