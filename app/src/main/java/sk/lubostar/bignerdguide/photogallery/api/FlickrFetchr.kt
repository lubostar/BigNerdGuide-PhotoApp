package sk.lubostar.bignerdguide.photogallery.api

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.annotation.WorkerThread
import com.google.gson.GsonBuilder
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem

class FlickrFetchr {
    companion object{
        private const val TAG = "FlickrFetchr"
    }

    private val flickrApi: FlickrApi

    init {
        val gson = GsonBuilder()
            .registerTypeAdapter(PhotoResponse::class.java, PhotoDeserializer())
            .create()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.flickr.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        flickrApi = retrofit.create(FlickrApi::class.java)
    }

    fun fetchPhotosCall(page: Long, function: (List<GalleryItem>) -> Unit) {
        val fetchPhotos = flickrApi.fetchPhotos(page)
        fetchPhotos.enqueue(object : Callback<PhotoResponse> {

            override fun onResponse(call: Call<PhotoResponse>, response: Response<PhotoResponse>) {
                val photoResponse: PhotoResponse? = response.body()
                val galleryItems: List<GalleryItem> = photoResponse?.galleryItems ?: mutableListOf()
                galleryItems.filterNot { it.url.isBlank() }
                function.invoke(galleryItems)
            }

            override fun onFailure(call: Call<PhotoResponse>, t: Throwable) {
                Log.e(TAG, "Error during loading ", t)
            }
        })
    }

    @WorkerThread
    fun fetchPhoto(url: String): Bitmap? {
        val response: Response<ResponseBody> = flickrApi.fetchUrlBytes(url).execute()
        val bitmap = response.body()?.byteStream()?.use(BitmapFactory::decodeStream)
        Log.i(TAG, "Decoded bitmap=$bitmap from Response=$response")
        return bitmap
    }
}