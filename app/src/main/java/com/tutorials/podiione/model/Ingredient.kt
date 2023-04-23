package com.tutorials.podiione.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Ingredient(
    val id:Int=0,
    val name:String = "",
    val img: String =""
):Parcelable
