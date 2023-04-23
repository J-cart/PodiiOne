package com.tutorials.podiione.util

import android.content.Context
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.tutorials.podiione.R
import com.tutorials.podiione.model.Response

fun parseSnacksResponse(context: Context): List<Response>? {

    val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()

    val type = Types.newParameterizedType(List::class.java, Response::class.java)
    val jsonAdapter: JsonAdapter<List<Response>> = moshi.adapter(type)
    val rawToText =
        context.resources.openRawResource(R.raw.snacks).bufferedReader().use { it.readText() }

    return jsonAdapter.fromJson(rawToText)
}