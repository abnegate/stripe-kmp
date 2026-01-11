# BUILD-02: ProGuard rules for Stripe KMP library

# Keep all Stripe SDK classes
-keep class com.jakebarnby.stripe.** { *; }

# Keep Stripe Android SDK classes (they have their own rules, but be explicit)
-keep class com.stripe.android.** { *; }

# Keep data classes and their properties (for serialization/reflection)
-keepclassmembers class com.jakebarnby.stripe.** {
    public <init>(...);
}

# Keep sealed classes and their subclasses
-keep class * extends com.jakebarnby.stripe.PaymentSheetResult { *; }

# Preserve annotations
-keepattributes *Annotation*

# Preserve line numbers for debugging
-keepattributes SourceFile,LineNumberTable

# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep suspend functions (for coroutines)
-keepclassmembers class * {
    *** invoke*(...);
}

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# Remove logging in release builds (optional - comment out to keep logs)
# -assumenosideeffects class android.util.Log {
#     public static *** d(...);
#     public static *** v(...);
#     public static *** i(...);
# }
