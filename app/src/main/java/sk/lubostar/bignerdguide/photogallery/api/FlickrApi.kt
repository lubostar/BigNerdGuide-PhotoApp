package sk.lubostar.bignerdguide.photogallery.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface FlickrApi {

    @GET("services/rest/?method=flickr.interestingness.getList" +
            "&api_key=4476a9a226d4d78dd3ef180d92c58e6f" +
            "&format=json" +
            "&nojsoncallback=1" +
            "&extras=url_s")
    fun fetchPhotos(@Query("page") page: Long = 1): Call<PhotoResponse>
}