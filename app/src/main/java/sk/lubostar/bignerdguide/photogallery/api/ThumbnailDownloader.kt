package sk.lubostar.bignerdguide.photogallery.api

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import android.util.LruCache
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

class ThumbnailDownloader<in T>(
    private val responseHandler: Handler, private val lifecycle: Lifecycle,
    private val onThumbnailDownloaded: (T, Bitmap) -> Unit) : HandlerThread(TAG){

    companion object{
        private const val TAG = "ThumbnailDownloader"
        private const val MESSAGE_DOWNLOAD = 0
    }

    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetchr = FlickrFetchr()
    private var hasQuit = false

    private lateinit var memoryCache: LruCache<String, Bitmap>

    val fragmentLifecycleObserver: LifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup() {
            Log.i(TAG, "Starting background thread")
            start()
            initCache()
            looper
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown() {
            Log.i(TAG, "Destroying background thread")
            lifecycle.removeObserver(this)
            quit()
        }
    }

    val viewLifecycleObserver: LifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue() {
            Log.i(TAG, "Clearing all requests from queue")
            lifecycle.removeObserver(this)
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {

            @Suppress("UNCHECKED_CAST")
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target : T = msg.obj as T
                    Log.i(TAG, "Got request for URL: ${requestMap[target!!]}")
                    handleRequest(target)
                }
            }
        }
    }

    private fun handleRequest(target: T) {
        val url = requestMap[target!!] ?: return

        val cachedBitmap: Bitmap? = memoryCache.get(url)
        if (cachedBitmap != null) {
            responseHandler.post(Runnable {
                requestMap.remove(target)
                onThumbnailDownloaded(target, cachedBitmap)
            })
        } else {
            val bitmap = flickrFetchr.fetchPhoto(url) ?: return

            responseHandler.post(Runnable {
                if (requestMap[target] != url || hasQuit) {
                    return@Runnable
                }

                requestMap.remove(target)
                memoryCache.put(url, bitmap)
                onThumbnailDownloaded(target, bitmap)
            })
        }
    }

    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got a URL: $url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    private fun initCache() {
        val maxMemory = (Runtime.getRuntime().maxMemory() / 1024).toInt()
        val cacheSize = maxMemory / 8
        memoryCache = object : LruCache<String, Bitmap>(cacheSize) {

            override fun sizeOf(key: String, bitmap: Bitmap): Int {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.byteCount / 1024
            }
        }
    }
}