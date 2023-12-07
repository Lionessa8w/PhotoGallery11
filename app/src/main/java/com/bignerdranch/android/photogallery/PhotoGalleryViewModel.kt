package com.bignerdranch.android.photogallery

import androidx.lifecycle.LiveData

import androidx.lifecycle.ViewModel


class PhotoGalleryViewModel : ViewModel() {

    val galleryItemLiveData: LiveData<List<GalleryItem>>

    init {
        galleryItemLiveData = FlickrFetcher().searchPhotos("planets")
    }
//    private val listPhoto = listOf<String>(
//        "https://timeweb.com/ru/community/article/15/15d1c7d25e936e26523a725e00d64905.jpeg",
//        "https://nevi.ru/wp-content/uploads/2018/12/novye-fotografii.jpg",
//        "https://proprikol.ru/wp-content/uploads/2022/10/kartinki-s-mezhdunarodnym-dnem-gor-16-scaled.jpg",
//        "https://mysekret.ru/wp-content/uploads/2022/01/reinhart-julian-wxm465om4j4-unsplash.jpg",
//    )
//
//    private val _galleryItemLiveData: MutableLiveData<List<GalleryItem>> = MutableLiveData()
//    val galleryItemLiveData: LiveData<List<GalleryItem>> = _galleryItemLiveData
//
//    init {
//        _galleryItemLiveData.value = getMock()
//    }
//
//
//    private fun getMock(): MutableList<GalleryItem> {
//        val resultList = mutableListOf<GalleryItem>()
//        for (i in 0 until 100) {
//            resultList.add(
//                GalleryItem(
//                    title = "$i",
//                    id = "$i",
//                    listPhoto[Random.nextInt(0, listPhoto.size)]
//                )
//            )
//        }
//        return resultList
//    }
}