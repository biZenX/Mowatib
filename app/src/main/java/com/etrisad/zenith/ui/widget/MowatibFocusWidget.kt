package com.etrisad.zenith.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.ColorFilter
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.graphics.shapes.RoundedPolygon
import androidx.graphics.shapes.toPath
import com.etrisad.zenith.MainActivity
import com.etrisad.zenith.R
import com.etrisad.zenith.ZenithApplication
import com.etrisad.zenith.data.local.MowatibTaskManager
import kotlinx.coroutines.flow.first

class MowatibFocusWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    companion object {
        private val bitmapCache = android.util.LruCache<String, Bitmap>(20)
        fun clearCache() { try { bitmapCache.evictAll() } catch (_: Exception) {} }
    }

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val app = context.applicationContext as ZenithApplication
        val prefsRepo = app.userPreferencesRepository
        val prefs = try { prefsRepo.userPreferencesFlow.first() } catch (_: Exception) { null }
        val currentStreak = prefs?.globalCurrentStreak ?: 0

        MowatibTaskManager.init(context)
        val tasks = MowatibTaskManager.tasks.value
        val completedTasks = tasks.count { it.isCompleted }
        val totalTasks = tasks.size

        provideContent {
            val uiMode = context.resources.configuration.uiMode
            val flowerBitmap = remember(uiMode) { createShapeBitmap(context, 120, MaterialShapes.Flower) }
            val sunnyBitmap = remember(uiMode) { createShapeBitmap(context, 80, MaterialShapes.Sunny) }

            GlanceTheme {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val action = actionStartActivity(intent)
                Box(
                    modifier = GlanceModifier.fillMaxSize().cornerRadius(32.dp).clickable(action),
                    contentAlignment = Alignment.Center
                ) {
                    FocusWidgetContent(
                        currentStreak = currentStreak,
                        completedTasks = completedTasks,
                        totalTasks = totalTasks,
                        flowerBitmap = flowerBitmap,
                        sunnyBitmap = sunnyBitmap
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @SuppressLint("RestrictedApi")
    @Composable
    private fun FocusWidgetContent(
        currentStreak: Int,
        completedTasks: Int,
        totalTasks: Int,
        flowerBitmap: Bitmap,
        sunnyBitmap: Bitmap
    ) {
        val size = LocalSize.current
        val squareSize = minOf(size.width, size.height)
        val scaleFactor = squareSize.value / 100f

        val contentPadding = (10 * scaleFactor).dp
        val mainFontSize = (22 * scaleFactor).sp
        val labelFontSize = (11 * scaleFactor).sp

        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = GlanceModifier.size(squareSize),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    provider = ImageProvider(flowerBitmap),
                    contentDescription = null,
                    modifier = GlanceModifier.fillMaxSize(),
                    colorFilter = ColorFilter.tint(GlanceTheme.colors.widgetBackground)
                )

                Column(
                    modifier = GlanceModifier.fillMaxSize().padding(contentPadding),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier.size((36 * scaleFactor).dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            provider = ImageProvider(sunnyBitmap),
                            contentDescription = null,
                            modifier = GlanceModifier.fillMaxSize(),
                            colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer)
                        )
                        Text(
                            text = if (currentStreak > 0) "${currentStreak}d" else "⚡",
                            style = TextStyle(
                                color = GlanceTheme.colors.onPrimaryContainer,
                                fontSize = (12 * scaleFactor).sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Spacer(modifier = GlanceModifier.height((4 * scaleFactor).dp))

                    Text(
                        text = "مواظب",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurface,
                            fontSize = mainFontSize,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Text(
                        text = if (totalTasks > 0) "$completedTasks/$totalTasks مهام" else "ابدأ التركيز",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = labelFontSize,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    private fun createShapeBitmap(
        context: Context,
        sizeDp: Int,
        shape: RoundedPolygon
    ): Bitmap {
        val density = context.resources.displayMetrics.density
        val sizePx = (sizeDp * density).toInt()

        val cacheKey = "focus_${shape.hashCode()}_${sizePx}"
        bitmapCache.get(cacheKey)?.let { return it }

        val path = shape.toPath()
        val bounds = android.graphics.RectF()
        path.computeBounds(bounds, true)

        val matrix = Matrix()
        matrix.postScale(sizePx / bounds.width(), sizePx / bounds.height())
        path.transform(matrix)

        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            this.color = Color.WHITE
            style = Paint.Style.FILL
        }
        canvas.drawPath(path, paint)

        bitmapCache.put(cacheKey, bitmap)
        return bitmap
    }
}
