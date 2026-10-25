package com.etrisad.zenith.data.manager

import android.content.Context
import android.content.Intent
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

data class MowatibTelegramConfig(
    val enabled: Boolean = false,
    val chatId: String = "",
    val botToken: String = "",
    val notifyDailyRecap: Boolean = true,
    val notifyShieldBreach: Boolean = true,
    val notifyPomodoro: Boolean = true,
    val notifyHabits: Boolean = true
)

class MowatibTelegramManager(private val context: Context) {

    companion object {
        private const val PREFS_NAME = "mowatib_telegram_prefs"
        private const val KEY_ENABLED = "telegram_enabled"
        private const val KEY_CHAT_ID = "telegram_chat_id"
        private const val KEY_BOT_TOKEN = "telegram_bot_token"
        private const val KEY_NOTIFY_DAILY = "telegram_notify_daily"
        private const val KEY_NOTIFY_SHIELD = "telegram_notify_shield"
        private const val KEY_NOTIFY_POMODORO = "telegram_notify_pomodoro"
        private const val KEY_NOTIFY_HABITS = "telegram_notify_habits"

        // Default Mowatib Telegram bot username for deep linking
        const val DEFAULT_BOT_USERNAME = "MowatibAppBot"
    }

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun getConfig(): MowatibTelegramConfig {
        return MowatibTelegramConfig(
            enabled = prefs.getBoolean(KEY_ENABLED, false),
            chatId = prefs.getString(KEY_CHAT_ID, "") ?: "",
            botToken = prefs.getString(KEY_BOT_TOKEN, "") ?: "",
            notifyDailyRecap = prefs.getBoolean(KEY_NOTIFY_DAILY, true),
            notifyShieldBreach = prefs.getBoolean(KEY_NOTIFY_SHIELD, true),
            notifyPomodoro = prefs.getBoolean(KEY_NOTIFY_POMODORO, true),
            notifyHabits = prefs.getBoolean(KEY_NOTIFY_HABITS, true)
        )
    }

    fun saveConfig(config: MowatibTelegramConfig) {
        prefs.edit()
            .putBoolean(KEY_ENABLED, config.enabled)
            .putString(KEY_CHAT_ID, config.chatId.trim())
            .putString(KEY_BOT_TOKEN, config.botToken.trim())
            .putBoolean(KEY_NOTIFY_DAILY, config.notifyDailyRecap)
            .putBoolean(KEY_NOTIFY_SHIELD, config.notifyShieldBreach)
            .putBoolean(KEY_NOTIFY_POMODORO, config.notifyPomodoro)
            .putBoolean(KEY_NOTIFY_HABITS, config.notifyHabits)
            .apply()
    }

    /**
     * Send raw HTML formatted message to Telegram
     */
    suspend fun sendMessage(
        text: String,
        targetChatId: String? = null,
        targetToken: String? = null
    ): Result<String> = withContext(Dispatchers.IO) {
        val config = getConfig()
        val chatId = (targetChatId ?: config.chatId).trim()
        val token = (targetToken ?: config.botToken).trim()

        if (chatId.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("يرجى إدخال معرّف الحساب (Chat ID) أولاً"))
        }

        if (token.isBlank()) {
            return@withContext Result.failure(IllegalArgumentException("يرجى إدخال رمز البوت (Bot Token) من BotFather"))
        }

        try {
            val url = "https://api.telegram.org/bot$token/sendMessage"
            val payload = JSONObject().apply {
                put("chat_id", chatId)
                put("text", text)
                put("parse_mode", "HTML")
            }

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = payload.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = httpClient.newCall(request).execute()
            val responseBody = response.body?.string().orEmpty()

            if (response.isSuccessful) {
                Result.success("تم إرسال الرسالة بنجاح عبر تيليجرام!")
            } else {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optString("description", "خطأ غير معروف من تيليجرام")
                } catch (_: Exception) {
                    "رمز الخطأ: ${response.code}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            Result.failure(Exception(e.localizedMessage ?: "فشل الاتصال بخوادم تيليجرام"))
        }
    }

    /**
     * Send test connection message
     */
    suspend fun testConnection(chatId: String, token: String): Result<String> {
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val message = """
            <b>منصة وتطبيق مواظب | Mowatib</b>
            
            تم التحقق من الاتصال بنجاح!
            تم ربط حسابك في تيليجرام مع تطبيق مواظب على هاتفك.
            
            <b>معرّف المحادثة (Chat ID):</b> <code>$chatId</code>
            <b>توقيت الربط:</b> $timeStr
            
            ستصلك التنبيهات المحددة وتقارير الإنجاز اليومية مباشرة هنا لمساعدتك على الانضباط والمواظبة.
        """.trimIndent()

        return sendMessage(message, targetChatId = chatId, targetToken = token)
    }

    /**
     * Send Pomodoro focus session completion notification
     */
    suspend fun sendPomodoroCompleted(sessionMinutes: Int, taskTitle: String? = null) {
        val config = getConfig()
        if (!config.enabled || !config.notifyPomodoro) return

        val taskDesc = if (!taskTitle.isNullOrBlank()) "\n<b>المهمة:</b> $taskTitle" else ""
        val message = """
            <b>مواظب | جلسة تركيز ناجحة</b>
            
            أحسنت! أتممت جلسة تركيز بومودورو بنجاح.
            <b>المدة:</b> $sessionMinutes دقيقة$taskDesc
            
            استمر في التقدم والمواظبة على أهدافك!
        """.trimIndent()

        sendMessage(message)
    }

    /**
     * Send shield / screen time limit breach alert
     */
    suspend fun sendShieldBreachAlert(appName: String, limitMinutes: Int) {
        val config = getConfig()
        if (!config.enabled || !config.notifyShieldBreach) return

        val message = """
            <b>مواظب | تنبيه انضباط ووقت الشاشة</b>
            
            تنبيه: تم تجاوز الحد المسموح لتطبيق <b>$appName</b> ($limitMinutes دقيقة).
            تطبيق مواظب قام بتفعيل درع الحماية لمساعدتك على استعادة تركيزك.
            
            تذكّر: الانضباط اليوم هو حريتك وراحتك غداً.
        """.trimIndent()

        sendMessage(message)
    }

    /**
     * Send habit completed notification
     */
    suspend fun sendHabitCompleted(habitTitle: String, currentStreak: Int) {
        val config = getConfig()
        if (!config.enabled || !config.notifyHabits) return

        val message = """
            <b>مواظب | إنجاز عادة يومية</b>
            
            تم تسجيل إنجاز عادة: <b>$habitTitle</b>
            <b>سلسلة الالتزام (Streak):</b> $currentStreak أيام متتالية!
            
            قليلٌ دائمٌ خيرٌ من كثيرٍ منقطع.
        """.trimIndent()

        sendMessage(message)
    }

    /**
     * Send daily summary recap
     */
    suspend fun sendDailyRecap(
        screenTimeFormatted: String,
        tasksCompleted: Int,
        habitsCompleted: Int
    ) {
        val config = getConfig()
        if (!config.enabled || !config.notifyDailyRecap) return

        val dateStr = SimpleDateFormat("EEEE، d MMMM yyyy", Locale.forLanguageTag("ar")).format(Date())
        val message = """
            <b>مواظب | التقرير اليومي للإنجاز</b>
            <b>التاريخ:</b> $dateStr
            
            <b>وقت استخدام الشاشة:</b> $screenTimeFormatted
            <b>المهام المنجزة:</b> $tasksCompleted
            <b>العادات المكتملة:</b> $habitsCompleted
            
            عظيم! كل خطوة صغيرة اليوم تصنع فارقاً كبيراً غداً.
        """.trimIndent()

        sendMessage(message)
    }

    /**
     * Helper to open Telegram App or Web to the bot
     */
    fun openTelegramBot(botUsername: String = DEFAULT_BOT_USERNAME) {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=$botUsername")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/$botUsername")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }

    /**
     * Helper to open @userinfobot to find chat ID
     */
    fun openUserInfoBot() {
        try {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("tg://resolve?domain=userinfobot")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        } catch (_: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/userinfobot")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(webIntent)
        }
    }
}
