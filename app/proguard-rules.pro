# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Kotlin Coroutines
-dontwarn kotlinx.coroutines.**

# Hilt
-dontwarn dagger.hilt.**

# Kotlin Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.lifeup.app.**$$serializer { *; }
-keepclassmembers class com.lifeup.app.** { *** Companion; }
-keepclasseswithmembers class com.lifeup.app.** { kotlinx.serialization.KSerializer serializer(...); }
