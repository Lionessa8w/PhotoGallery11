package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

//целью ThumbnailDownloader является загрузка и передача изображений в PhotoGalleryFragment
class ThumbnailDownloader<in T> : HandlerThread(TAG), LifecycleObserver {
    private var hasQuit = false
    /**объект Handler, отвечающий за постановку в очередь запросов на загрузку
     *  в фоновом потоке ThumbnailDownloader. Этот объект также будет отвечать
     *  за обработку сообщений запросов на загрузку при извлечении их из очереди.
    */
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetcher = FlickrFetcher()

    @Suppress("UNCHECRED_CAST")
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler= object : Handler(){
            override fun handleMessage(msg: Message) {
                if (msg.what== MESSAGE_DOWNLOAD){
                    val target=msg.obj as T
                    Log.i(TAG,"Got a request for URL:${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }
    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun setup() {
        Log.i(TAG, "Starting background thread")
        start()
        looper
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun tearDown() {
        Log.i(TAG, "Destroying background thread")
        quit()
    }
    /**постановка сообщений в очередь на отправку*/
    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got is URL:$url")
        requestMap[target]=url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()

    }
    fun handleRequest(target: T){
        val url=requestMap[target] ?: return
        val bitmap=flickrFetcher.fetchPhoto(url) ?: return
    }


}