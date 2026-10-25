package com.etrisad.zenith.ui.widget

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
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
import androidx.glance.appwidget.GlanceAppWidgetReceiver
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.etrisad.zenith.MainActivity
import com.etrisad.zenith.R
import com.etrisad.zenith.util.MowatibAmbientSoundManager

class MowatibAmbientAudioWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val currentSound = MowatibAmbientSoundManager.currentSound.value
        val isPlaying = MowatibAmbientSoundManager.isPlaying.value

        provideContent {
            val uiMode = context.resources.configuration.uiMode
            val sunnyBitmap = remember(uiMode) {
                MowatibWidgetShapes.getShapeBitmap(context, 160, MaterialShapes.Sunny)
            }

            GlanceTheme {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    putExtra("route", "pomodoro")
                }
                val clickAction = actionStartActivity(intent)

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(32.dp)
                        .clickable(clickAction),
                    contentAlignment = Alignment.Center
                ) {
                    AudioWidgetContent(
                        soundName = if (currentSound.id == "none") "أصوات الهدوء" else currentSound.nameAr,
                        isPlaying = isPlaying,
                        sunnyBitmap = sunnyBitmap
                    )
                }
            }
        }
    }

    @Composable
    private fun AudioWidgetContent(
        soundName: String,
        isPlaying: Boolean,
        sunnyBitmap: Bitmap
    ) {
        val size = LocalSize.current
        val squareSize = minOf(size.width, size.height)
        val scale = (squareSize.value / 120f).coerceIn(0.7f, 1.8f)

        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(sunnyBitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(squareSize),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.widgetBackground)
            )

            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .padding((12 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_headphones),
                        contentDescription = null,
                        modifier = GlanceModifier.size((14 * scale).dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.primary)
                    )
                    Spacer(modifier = GlanceModifier.width((4 * scale).dp))
                    Text(
                        text = if (isPlaying) "صوت نشط" else "بيئة التركيز",
                        style = TextStyle(
                            color = GlanceTheme.colors.primary,
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }

                Spacer(modifier = GlanceModifier.height((6 * scale).dp))

                Text(
                    text = soundName,
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = (14 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = GlanceModifier.height((6 * scale).dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play_arrow),
                        contentDescription = null,
                        modifier = GlanceModifier.size((12 * scale).dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                    )
                    Spacer(modifier = GlanceModifier.width((4 * scale).dp))
                    Text(
                        text = if (isPlaying) "إيقاف مؤقت" else "تشغيل الصوت",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = (11 * scale).sp,
                            fontWeight = FontWeight.Medium,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

class MowatibAmbientAudioWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MowatibAmbientAudioWidget()
}
