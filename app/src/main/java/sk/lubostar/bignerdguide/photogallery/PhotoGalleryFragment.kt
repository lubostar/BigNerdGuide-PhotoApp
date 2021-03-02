package sk.lubostar.bignerdguide.photogallery

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import kotlinx.android.synthetic.main.fragment_photo_gallery.*

class PhotoGalleryFragment : Fragment() {
    companion object{
        private const val TAG = "PhotoGalleryFragment"

        fun newInstance() = PhotoGalleryFragment()
    }

    private val photoGalleryViewModel: PhotoGalleryViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_photo_gallery, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        photo_recycler_view.layoutManager = GridLayoutManager(context, 3)

        photoGalleryViewModel.galleryItemLiveData.observe(viewLifecycleOwner, { galleryItems ->
            Log.d(TAG, "Have gallery items from ViewModel $galleryItems")
        })
    }
}
