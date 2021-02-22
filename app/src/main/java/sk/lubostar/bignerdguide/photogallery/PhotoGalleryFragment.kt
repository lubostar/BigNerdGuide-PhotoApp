package sk.lubostar.bignerdguide.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_photo_gallery.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import sk.lubostar.bignerdguide.photogallery.api.FlickrApi

class PhotoGalleryFragment : Fragment() {
    companion object{
        private const val TAG = "PhotoGalleryFragment"

        fun newInstance() = PhotoGalleryFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val retrofit: Retrofit = Retrofit.Builder()
            .baseUrl("https://www.flickr.com/")
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()

        val flickrApi: FlickrApi = retrofit.create(FlickrApi::class.java)
        val flickrHomePageRequest = flickrApi.fetchContents()
        flickrHomePageRequest.enqueue(object : Callback<String> {

            override fun onResponse(call: Call<String>, response: Response<String>) {
                Log.d(TAG, "Response received: ${response.body()}")
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e(TAG, "Failed to fetch photos", t)
            }
        })

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_photo_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo_recycler_view.layoutManager = GridLayoutManager(context, 3)
    }
}
