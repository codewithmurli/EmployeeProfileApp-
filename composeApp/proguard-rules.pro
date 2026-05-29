# Keep Room entities
-keep class com.employeeapp.data.db.** { *; }
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep Koin
-keep class org.koin.** { *; }
-dontwarn org.koin.**

# Keep Kotlin serialization
-keep class kotlinx.serialization.** { *; }
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Keep Coil
-keep class coil3.** { *; }
-dontwarn coil3.**

# Keep coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** { volatile <fields>; }

# General Android
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
