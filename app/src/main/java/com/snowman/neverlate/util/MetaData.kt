package com.snowman.neverlate.util

import android.content.Context
import android.content.pm.PackageManager

fun getMetaData(name: String, context: Context?): String? {
    if(context == null){
        throw Exception("getMetaData: context is null")
    }
    return try {
        val applicationInfo = context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        )
        applicationInfo.metaData.getString(name)
    } catch (e: PackageManager.NameNotFoundException) {
        e.printStackTrace()
        null
    }
}