package com.bignerdranch.android.photogallery.api

import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Url
import java.net.URI

interface FlickrApi {
    @GET(
        "services/rest/? method=flickr.interestingness.getList" +
                "&api_key=в8c2e712a8521d1fa2ff1dbfdd64099b5" + "&format=json" +
                "&nojsoncallback=1" + "&extras=url_s"
    )
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String):Call<ResponseBody>

}