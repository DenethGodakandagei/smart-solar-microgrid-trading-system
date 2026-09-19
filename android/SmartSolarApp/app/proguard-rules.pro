# Smart Solar Microgrid Trading System
# ProGuard Rules
#
# Member 2 - Native Android Prosumer Application
#
# Add project-specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in the Android SDK tools proguard configuration.

# ---------------------------------------------------------------
# Retrofit - Keep API interface methods
# ---------------------------------------------------------------
-keepattributes Signature
-keepattributes Exceptions
-keepattributes *Annotation*

# Keep Retrofit interfaces
-keep,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Retrofit does reflection on generic parameters
-keepattributes Signature, InnerClasses, EnclosingMethod

# Retrofit does reflection on method and parameter annotations
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations

# ---------------------------------------------------------------
# OkHttp
# ---------------------------------------------------------------
-dontwarn okhttp3.**
-dontwarn okio.**

# ---------------------------------------------------------------
# Gson - Keep model classes for serialization/deserialization
# ---------------------------------------------------------------
-keep class com.smartsolar.app.api.models.** { *; }

# ---------------------------------------------------------------
# ZXing QR
# ---------------------------------------------------------------
-keep class com.google.zxing.** { *; }
-dontwarn com.google.zxing.**

# ---------------------------------------------------------------
# Glide
# ---------------------------------------------------------------
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
    <init>(...);
}
