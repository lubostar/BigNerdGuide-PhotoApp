package sk.lubostar.bignerdguide.photogallery.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import sk.lubostar.bignerdguide.photogallery.QueryPreferences
import sk.lubostar.bignerdguide.photogallery.api.FlickrFetchr
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem

class PollWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object{
        private const val TAG = "PollWorker"
    }

    override fun doWork(): Result {
        val query = QueryPreferences.getStoredQuery(context)
        val lastResId = QueryPreferences.getLastResultId(context)
        val items: List<GalleryItem> = if (query.isEmpty()) {
            FlickrFetchr().fetchPhotosRequest().execute().body()?.galleryItems
        } else {
            FlickrFetchr().searchPhotosRequest(query).execute().body()?.galleryItems
        } ?: emptyList()

        if (items.isEmpty()) return Result.success()

        val resultId = items.first().id
        if (resultId == lastResId) {
            Log.i(TAG, "Got an old result id: $resultId")
        } else {
            Log.i(TAG, "Got a new result id: $resultId")
            QueryPreferences.setLastResultId(context, resultId)
        }

        return Result.success();
    }
}