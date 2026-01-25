# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve line number information for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ===== ZeroID App Rules =====

# Keep JavascriptInterface for WebView communication
-keepclassmembers class com.zero.id.app.zkp.ZKProver$WebAppInterface {
    @android.webkit.JavascriptInterface <methods>;
}

# Keep all model classes - used in JSON serialization
-keep class com.zero.id.library.model.** { *; }
-keepclassmembers class com.zero.id.library.model.** { *; }

# Keep security classes - reflection and KeyStore access
-keep class com.zero.id.app.security.** { *; }
-keepclassmembers class com.zero.id.app.security.** { *; }

# Keep ZK proof classes
-keep class com.zero.id.app.zkp.** { *; }
-keepclassmembers class com.zero.id.app.zkp.** {
    public <methods>;
}

# Gson specific rules
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# OkHttp specific rules
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# Kotlin serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Android KeyStore classes
-keep class android.security.keystore.** { *; }
-keep class java.security.** { *; }
-keep interface java.security.** { *; }

# Keep Compose classes
-keep class androidx.compose.** { *; }
-keepclassmembers class androidx.compose.** { *; }

# Keep ViewModel classes
-keep class androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.ViewModel {
    <init>();
}

# Keep Navigation classes
-keep class androidx.navigation.** { *; }

# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}
