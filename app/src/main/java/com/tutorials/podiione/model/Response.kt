package com.tutorials.podiione.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Response(
   val id: Int,
   val name: String,
   val images:String,
   val desc: String,
   val price: String,
   val veg: Boolean,
   val isFavorite:Boolean = false
):Parcelable