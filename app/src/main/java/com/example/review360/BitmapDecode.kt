package com.example.review360

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlin.math.max

fun decodeBitmapSampledFromUri(
    context: Context,
    uri: Uri,
    reqMaxWidth: Int,
    reqMaxHeight: Int
): Bitmap {
    val optsBounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    context.contentResolver.openInputStream(uri)?.use {
        BitmapFactory.decodeStream(it, null, optsBounds)
    }

    val srcW = optsBounds.outWidth
    val srcH = optsBounds.outHeight

    val sample = calculateInSampleSize(srcW, srcH, reqMaxWidth, reqMaxHeight)
    val optsDecode = BitmapFactory.Options().apply {
        inSampleSize = sample
        inPreferredConfig = Bitmap.Config.RGB_565
    }

    context.contentResolver.openInputStream(uri)?.use {
        return BitmapFactory.decodeStream(it, null, optsDecode)
            ?: throw IllegalStateException("Не удалось декодировать изображение")
    }
    throw IllegalArgumentException("Не удалось открыть поток для $uri")
}

private fun calculateInSampleSize(srcW: Int, srcH: Int, reqW: Int, reqH: Int): Int {
    var inSampleSize = 1
    if (srcH > reqH || srcW > reqW) {
        val heightRatio = (srcH.toFloat() / reqH).toInt().coerceAtLeast(1)
        val widthRatio = (srcW.toFloat() / reqW).toInt().coerceAtLeast(1)
        inSampleSize = max(heightRatio, widthRatio)
        var pow2 = 1
        while (pow2 * 2 <= inSampleSize) pow2 *= 2
        inSampleSize = pow2
    }
    return inSampleSize
}
