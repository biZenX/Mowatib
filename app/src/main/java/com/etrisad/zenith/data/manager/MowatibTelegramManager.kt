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
        val isAr = Locale.getDefault().language.startsWith("ar")

        if (chatId.isBlank()) {
            val err = if (isAr) "يرجى إدخال معرّف الحساب الرقمي أولاً" else "Please enter your chat ID first"
            return@withContext Result.failure(IllegalArgumentException(err))
        }

        if (token.isBlank()) {
            val err = if (isAr) "يرجى إدخال رمز مفتاح البوت أولاً" else "Please enter your bot token first"
            return@withContext Result.failure(IllegalArgumentException(err))
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
                val okMsg = if (isAr) "تم إرسال الرسالة بنجاح عبر تيليجرام!" else "Message sent successfully via Telegram!"
                Result.success(okMsg)
            } else {
                val errorMsg = try {
                    val json = JSONObject(responseBody)
                    json.optString("description", if (isAr) "خطأ غير معروف من تيليجرام" else "Unknown Telegram error")
                } catch (_: Exception) {
                    if (isAr) "رمز الخطأ: ${response.code}" else "Error code: ${response.code}"
                }
                Result.failure(Exception(errorMsg))
            }
        } catch (e: Exception) {
            val failMsg = if (isAr) "فشل الاتصال بخوادم تيليجرام" else "Failed to connect to Telegram servers"
            Result.failure(Exception(e.localizedMessage ?: failMsg))
        }
    }

    /**
     * Send test connection message
     */
    suspend fun testConnection(chatId: String, token: String): Result<String> {
        val isAr = Locale.getDefault().language.startsWith("ar")
        val timeStr = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        val message = if (isAr) {
            """
                <b>تطبيق مواظب</b>
                
                تم التحقق من الاتصال بنجاح!
                تم ربط حسابك في تيليجرام مع تطبيق مواظب على هاتفك.
                
                <b>معرّف المحادثة:</b> <code>$chatId</code>
                <b>توقيت الربط:</b> $timeStr
                
                ستصلك التنبيهات المحددة وتقارير الإنجاز اليومية مباشرة هنا لمساعدتك على الانضباط والمواظبة.
            """.trimIndent()
        } else {
            """
                <b>Mowatib Focus</b>
                
                Connection verified successfully!
                Your Telegram account is now connected with Mowatib on your phone.
                
                <b>Chat ID:</b> <code>$chatId</code>
                <b>Connected At:</b> $timeStr
                
                You will receive scheduled alerts and daily progress summaries directly here.
            """.trimIndent()
        }

        return sendMessage(message, targetChatId = chatId, targetToken = token)
    }

    /**
     * Send Pomodoro focus session completion notification
     */
    suspend fun sendPomodoroCompleted(sessionMinutes: Int, taskTitle: String? = null) {
        val config = getConfig()
        if (!config.enabled || !config.notifyPomodoro) return

        val isAr = Locale.getDefault().language.startsWith("ar")
        val message = if (isAr) {
            val taskDesc = if (!taskTitle.isNullOrBlank()) "\n<b>المهمة:</b> $taskTitle" else ""
            """
                <b>مواظب | جلسة تركيز ناجحة</b>
                
                أحسنت! أتممت جلسة تركيز بنجاح.
                <b>المدة:</b> $sessionMinutes دقيقة$taskDesc
                
                استمر في التقدم والمواظبة على أهدافك!
            """.trimIndent()
        } else {
            val taskDesc = if (!taskTitle.isNullOrBlank()) "\n<b>Task:</b> $taskTitle" else ""
            """
                <b>Mowatib | Focus Session Completed</b>
                
                Great job! You completed a focus session.
                <b>Duration:</b> $sessionMinutes minutes$taskDesc
                
                Keep up the great discipline!
            """.trimIndent()
        }

        sendMessage(message)
    }

    /**
     * Send shield / screen time limit breach alert
     */
    suspend fun sendShieldBreachAlert(appName: String, limitMinutes: Int) {
        val config = getConfig()
        if (!config.enabled || !config.notifyShieldBreach) return

        val isAr = Locale.getDefault().language.startsWith("ar")
        val message = if (isAr) {
            """
                <b>مواظب | تنبيه انضباط ووقت الشاشة</b>
                
                تنبيه: تم تجاوز الحد المسموح لتطبيق <b>$appName</b> ($limitMinutes دقيقة).
                تطبيق مواظب قام بتفعيل درع الحماية لمساعدتك على استعادة تركيزك.
                
                تذكّر: الانضباط اليوم هو حريتك وراحتك غداً.
            """.trimIndent()
        } else {
            """
                <b>Mowatib | Screen Time Alert</b>
                
                Alert: The daily usage limit for <b>$appName</b> has been reached ($limitMinutes minutes).
                Mowatib shield has been activated to help you refocus.
                
                Remember: Daily discipline creates tomorrow's freedom.
            """.trimIndent()
        }

        sendMessage(message)
    }

    /**
     * Send habit completed notification
     */
    suspend fun sendHabitCompleted(habitTitle: String, currentStreak: Int) {
        val config = getConfig()
        if (!config.enabled || !config.notifyHabits) return

        val isAr = Locale.getDefault().language.startsWith("ar")
        val message = if (isAr) {
            """
                <b>مواظب | إنجاز عادة يومية</b>
                
                تم تسجيل إنجاز عادة: <b>$habitTitle</b>
                <b>سلسلة الالتزام:</b> $currentStreak أيام متتالية!
                
                قليلٌ دائمٌ خيرٌ من كثيرٍ منقطع.
            """.trimIndent()
        } else {
            """
                <b>Mowatib | Habit Completed</b>
                
                Habit registered: <b>$habitTitle</b>
                <b>Current Streak:</b> $currentStreak consecutive days!
                
                Consistency is the key to mastery.
            """.trimIndent()
        }

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

        val isAr = Locale.getDefault().language.startsWith("ar")
        val message = if (isAr) {
            val dateStr = SimpleDateFormat("EEEE، d MMMM yyyy", Locale.forLanguageTag("ar")).format(Date())
            """
                <b>مواظب | التقرير اليومي للإنجاز</b>
                <b>التاريخ:</b> $dateStr
                
                <b>وقت استخدام الشاشة:</b> $screenTimeFormatted
                <b>المهام المنجزة:</b> $tasksCompleted
                <b>العادات المكتملة:</b> $habitsCompleted
                
                عظيم! كل خطوة صغيرة اليوم تصنع فارقاً كبيراً غداً.
            """.trimIndent()
        } else {
            val dateStr = SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.ENGLISH).format(Date())
            """
                <b>Mowatib | Daily Progress Summary</b>
                <b>Date:</b> $dateStr
                
                <b>Screen Time:</b> $screenTimeFormatted
                <b>Tasks Completed:</b> $tasksCompleted
                <b>Habits Completed:</b> $habitsCompleted
                
                Every small daily step builds long-term success.
            """.trimIndent()
        }

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
