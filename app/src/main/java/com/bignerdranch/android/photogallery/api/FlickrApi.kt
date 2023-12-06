package com.bignerdranch.android.photogallery.api

import android.provider.ContactsContract.Contacts.Photo
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.Url
import java.net.URI

interface FlickrApi {

    //"&api_key=в8c2e712a8521d1fa2ff1dbfdd64099b5" +
    @GET("services/rest? method=flikr.interestingness.getList")
    fun fetchPhotos(): Call<FlickrResponse>

    @GET
    fun fetchUrlBytes(@Url url: String):Call<ResponseBody>

    @GET("services/rest? method=flikr.photos.search")
    fun searchPhoto(@Query("text") query: String): Call<FlickrResponse>

}
