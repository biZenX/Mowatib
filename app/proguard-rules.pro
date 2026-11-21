# Moshi & JSON
-keepclasseswithmembernames class * {
    @com.squareup.moshi.JsonQualifier <methods>;
}
-keep @com.squareup.moshi.JsonClass class *
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-dontwarn kotlin.reflect.**

# Mowatib App Components & Models
-keep class com.abdulrhman.mowatib.** { *; }
-keepclassmembers class com.abdulrhman.mowatib.** { *; }
-keep interface com.abdulrhman.mowatib.** { *; }
-keep enum com.abdulrhman.mowatib.** { *; }

# WorkManager Workers (must keep constructors for reflection instantiation)
-keep class * extends androidx.work.ListenableWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.Worker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(android.content.Context, androidx.work.WorkerParameters);
}

# ViewModels
-keep class * extends androidx.lifecycle.ViewModel {
    public <init>(...);
}
-keep class * extends androidx.lifecycle.AndroidViewModel {
    public <init>(...);
}

# Glance & AppWidgets
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }

# Room Database
-keep class * extends androidx.room.RoomDatabase { *; }
-keep @androidx.room.Entity class * { *; }
-keep @androidx.room.Dao class * { *; }
-keep class * implements androidx.room.RoomDatabase { *; }

# DataStore & Protobuf
-keep class androidx.datastore.** { *; }
-keep class * extends com.google.protobuf.GeneratedMessageLite { *; }
