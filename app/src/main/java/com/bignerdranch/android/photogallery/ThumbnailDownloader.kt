package com.bignerdranch.android.photogallery

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Handler
import android.os.HandlerThread
import android.os.Message
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import retrofit2.http.OPTIONS
import java.util.concurrent.ConcurrentHashMap

private const val TAG = "ThumbnailDownloader"
private const val MESSAGE_DOWNLOAD = 0

//целью ThumbnailDownloader является загрузка и передача изображений в PhotoGalleryFragment
class ThumbnailDownloader<in T>(
    private val responseHandler: Handler,
    private val onThumbnailDownloader: (T, Bitmap) -> Unit
) : HandlerThread(TAG), LifecycleObserver {

    val fragmentLifecycleObserver: LifecycleObserver= object : LifecycleObserver{
        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun setup(){
            Log.i(TAG,"Starting background thread")
            start()
            looper
        }
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun tearDown(){
            Log.i(TAG,"Destroying backgrond thread")
            quit()
        }
    }
    val viewLifecycleObserver:LifecycleObserver=object :LifecycleObserver{
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun clearQueue(){
            Log.i(TAG,"Clearing all requests from queue")
            requestHandler.removeMessages(MESSAGE_DOWNLOAD)
            requestMap.clear()
        }
    }

    private var hasQuit = false

    /**объект Handler, отвечающий за постановку в очередь запросов на загрузку
     *  в фоновом потоке ThumbnailDownloader. Этот объект также будет отвечать
     *  за обработку сообщений запросов на загрузку при извлечении их из очереди.
     */
    private lateinit var requestHandler: Handler
    private val requestMap = ConcurrentHashMap<T, String>()
    private val flickrFetcher = FlickrFetcher()

    /**Аннотация @Suppress("UNCHECKED_CAST") при проверке сообщает Lint,
     *  что вы приводите msg.obj к типу T
     * без предварительной проверки того, относится ли msg.obj к этому типу на самом деле.*/
    @Suppress("UNCHECRED_CAST")
    /** Проблемы тут получаются только в том случае,
     *  если обработчик прикреплен к объекту Looper основного потока.
     *  Предупреждение HandlerLeak убирается аннотацией @SuppressLint("HandlerLeak"),
     *  так как создаваемый обработчик прикреплен к looper фонового потока.
     *  Если вместо этого обработчик был прикреплен к looper основного потока,
     *  то он может и не собирать мусор. Если бы произошла утечка,
     *  так как он также содержит ссылку на ThumbnailDownloader,
     *  ваше приложение также утеряло бы экземпляр ThumbnailDownloader. */
    @SuppressLint("HandlerLeak")
    override fun onLooperPrepared() {
        requestHandler = object : Handler() {
            override fun handleMessage(msg: Message) {
                if (msg.what == MESSAGE_DOWNLOAD) {
                    val target = msg.obj as T
                    Log.i(TAG, "Got a request for URL:${requestMap[target]}")
                    handleRequest(target)
                }
            }
        }
    }

    override fun quit(): Boolean {
        hasQuit = true
        return super.quit()
    }

    /**постановка сообщений в очередь на отправку*/
    fun queueThumbnail(target: T, url: String) {
        Log.i(TAG, "Got is URL:$url")
        requestMap[target] = url
        requestHandler.obtainMessage(MESSAGE_DOWNLOAD, target).sendToTarget()

    }

    fun handleRequest(target: T) {
        //проверка на существование адреса
        val url = requestMap[target] ?: return
        val bitmap = flickrFetcher.fetchPhoto(url) ?: return
        /** А поскольку responseHandler связывается с Looper главного потока,
         *  весь код функции run() в Runnable будет выполнен в главном потоке.
        Что делает этот код? Сначала он проверяет requestMap.
        Такая проверка необходима, потому что RecyclerView заново использует свои представления.
        К тому времени, когда ThumbnailDownloader завершит загрузку Bitmap, может оказаться,
        что виджет RecyclerView уже переработал ImageView и запросил
        для него изображение с другого URL- адреса.
        Эта проверка гарантирует, что каждый объект
        PhotoHolder получит правильное изображение,
        даже если за прошедшее время был сделан другой запрос.
        Затем проверяется hasQuit. Если выполнение ThumbnailDownloader уже завершилось,
        выполнение каких- либо обратных вызовов небезопасно.
        Наконец, мы удаляем из requestMap связь «PhotoHolder —URL» и
        назначаем изображение для PhotoHolder.*/
        responseHandler.post(Runnable {
            if (requestMap[target] != url || hasQuit){
                return@Runnable
            }
            requestMap.remove(target)
            onThumbnailDownloader(target,bitmap)
        })
    }


}