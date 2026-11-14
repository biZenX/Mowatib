-keepclasseswithmembernames class * {
    @com.squareup.moshi.JsonQualifier <methods>;
}
-keep @com.squareup.moshi.JsonClass class *
-keep class com.abdulrhman.mowatib.data.model.** { *; }
-keep class com.abdulrhman.mowatib.ui.components.pausepoint.** { *; }
-keep class com.squareup.moshi.** { *; }
-dontwarn com.squareup.moshi.**
-dontwarn kotlin.reflect.**
-keep class com.abdulrhman.mowatib.data.local.entity.** { *; }
-keep class * extends androidx.room.RoomDatabase
