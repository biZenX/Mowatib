package com.etrisad.zenith.ui.widget

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialShapes
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.*
import androidx.glance.state.GlanceStateDefinition
import androidx.glance.state.PreferencesGlanceStateDefinition
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.etrisad.zenith.MainActivity
import com.etrisad.zenith.R

class MowatibPomodoroWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact
    override val stateDefinition: GlanceStateDefinition<*> = PreferencesGlanceStateDefinition

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            GlanceTheme {
                val intent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("navigate_to", "pomodoro")
                }
                val launchAction = actionStartActivity(intent)

                Box(
                    modifier = GlanceModifier
                        .fillMaxSize()
                        .cornerRadius(28.dp)
                        .clickable(launchAction),
                    contentAlignment = Alignment.Center
                ) {
                    PomodoroWidgetContent(context)
                }
            }
        }
    }

    @OptIn(ExperimentalMaterial3ExpressiveApi::class)
    @SuppressLint("RestrictedApi")
    @Composable
    private fun PomodoroWidgetContent(context: Context) {
        val size = LocalSize.current
        val squareSize = minOf(size.width, size.height)
        val scale = (squareSize.value / 120f).coerceIn(0.7f, 2.0f)

        val cookieBitmap = MowatibWidgetShapes.getShapeBitmap(
            context = context,
            sizeDp = squareSize.value.toInt(),
            shape = MaterialShapes.Cookie12Sided
        )
        val sunnyBitmap = MowatibWidgetShapes.getShapeBitmap(
            context = context,
            sizeDp = (50 * scale).toInt(),
            shape = MaterialShapes.Sunny
        )

        Box(
            modifier = GlanceModifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Image(
                provider = ImageProvider(cookieBitmap),
                contentDescription = null,
                modifier = GlanceModifier.size(squareSize),
                colorFilter = ColorFilter.tint(GlanceTheme.colors.widgetBackground)
            )

            Column(
                modifier = GlanceModifier.fillMaxSize().padding((12 * scale).dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = GlanceModifier.size((34 * scale).dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        provider = ImageProvider(sunnyBitmap),
                        contentDescription = null,
                        modifier = GlanceModifier.fillMaxSize(),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.primaryContainer)
                    )
                    Image(
                        provider = ImageProvider(R.drawable.ic_timer),
                        contentDescription = null,
                        modifier = GlanceModifier.size((16 * scale).dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onPrimaryContainer)
                    )
                }

                Spacer(modifier = GlanceModifier.height((4 * scale).dp))

                Text(
                    text = "25:00",
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontSize = (26 * scale).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = "جلسة تركيز مواظب",
                    style = TextStyle(
                        color = GlanceTheme.colors.primary,
                        fontSize = (11 * scale).sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = GlanceModifier.height((3 * scale).dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        provider = ImageProvider(R.drawable.ic_play_arrow),
                        contentDescription = null,
                        modifier = GlanceModifier.size((12 * scale).dp),
                        colorFilter = ColorFilter.tint(GlanceTheme.colors.onSurfaceVariant)
                    )
                    Spacer(modifier = GlanceModifier.width((4 * scale).dp))
                    Text(
                        text = "ابدأ الآن",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = (10 * scale).sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    )
                }
            }
        }
    }
}

class MowatibPomodoroWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MowatibPomodoroWidget()
}
