package com.etrisad.zenith.ui.widget

import android.content.Context
import android.graphics.*
import android.util.LruCache
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath

object MowatibWidgetShapes {
    private val cache = LruCache<String, Bitmap>(40)

    fun clearCache() {
        try { cache.evictAll() } catch (_: Exception) {}
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    fun getShapeBitmap(
        context: Context,
        sizeDp: Int,
        shape: RoundedPolygon,
        fillColor: Int = Color.WHITE
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt().coerceAtLeast(1)
        val cacheKey = "shape_${shape.hashCode()}_${sizePx}_${fillColor}"
        cache.get(cacheKey)?.let { return it }

        val path = shape.toPath()
        val bounds = RectF()
        path.computeBounds(bounds, true)

        val matrix = Matrix()
        val width = bounds.width().coerceAtLeast(1f)
        val height = bounds.height().coerceAtLeast(1f)
        matrix.postScale(sizePx / width, sizePx / height)
        path.transform(matrix)

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = fillColor
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, paint)
        cache.put(cacheKey, bitmap)
        return bitmap
    }
}
