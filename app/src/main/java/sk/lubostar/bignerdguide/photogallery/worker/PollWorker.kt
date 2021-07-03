package sk.lubostar.bignerdguide.photogallery.worker

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters

class PollWorker(val context: Context, workerParams: WorkerParameters) :
    Worker(context, workerParams) {

    companion object{
        private const val TAG = "PollWorker"
    }

    override fun doWork(): Result {
        Log.i(TAG, "Work request triggered");
        return Result.success();
    }
}