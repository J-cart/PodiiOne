package com.tutorials.podiione

import android.content.Context
import android.util.Log
import androidx.core.content.ContextCompat
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tutorials.podiione.model.Response

 fun parseSnacksResponse(context: Context): List<Response>? {

    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    val type = Types.newParameterizedType(List::class.java, Response::class.java)
    val jsonAdapter: JsonAdapter<List<Response>> = moshi.adapter(type)
// ERROR:       val myType = Gson().fromJson(R.raw.snacks.toString(),GeneralResponse::class.java)
    val rawToText =
        context.resources.openRawResource(R.raw.snacks).bufferedReader().use { it.readText() }

    val data = jsonAdapter.fromJson(rawToText)
    Log.d("JOE", "doSomething: ${jsonAdapter.fromJson(rawToText)} ")

    return data
}