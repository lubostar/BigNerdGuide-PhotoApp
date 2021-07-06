package sk.lubostar.bignerdguide.photogallery.worker

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import sk.lubostar.bignerdguide.photogallery.QueryPreferences
import sk.lubostar.bignerdguide.photogallery.R
import sk.lubostar.bignerdguide.photogallery.api.FlickrFetchr
import sk.lubostar.bignerdguide.photogallery.model.GalleryItem
import sk.lubostar.bignerdguide.photogallery.view.MainActivity

class PollWorker(private val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object{
        private const val TAG = "PollWorker"
        private const val NOTIFICATION_ID = "pollNotif"

        const val ACTION_SHOW_NOTIFICATION = "sk.lubostar.bignerdguide.photogallery.SHOW_NOTIFICATION"
        const val PERMISSION_PRIVATE = "sk.lubostar.bignerdguide.photogallery.PRIVATE"

         const val REQUEST_CODE = "REQUEST_CODE"
        const val NOTIFICATION = "NOTIFICATION"
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

            val intent = MainActivity.newIntent(context)
            val pendingIntent = PendingIntent.getActivity(context, 0, intent, 0)

            val resources = context.resources
            val notification = NotificationCompat.Builder(context, NOTIFICATION_ID)
                .setTicker(resources.getString(R.string.new_pictures_title))
                .setSmallIcon(android.R.drawable.ic_menu_report_image)
                .setContentTitle(resources.getString(R.string.new_pictures_title))
                .setContentText(resources.getString(R.string.new_pictures_text))
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build()

            showBackgroundNotification(0, notification)
        }

        return Result.success()
    }

    private fun showBackgroundNotification(requestCode: Int, notification: Notification) {
        val intent = Intent(ACTION_SHOW_NOTIFICATION).apply {
            putExtra(REQUEST_CODE, requestCode)
            putExtra(NOTIFICATION, notification)
        }

        context.sendOrderedBroadcast(intent, PERMISSION_PRIVATE)

    }
}