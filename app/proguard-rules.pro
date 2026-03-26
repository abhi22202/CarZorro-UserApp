# Add project specific ProGuard rules here.

# General Android Rules (consolidated)
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes InnerClasses
-keepattributes EnclosingMethod
-keepattributes RuntimeVisibleAnnotations
-keepattributes RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Razorpay ProGuard Rules
-keep class com.razorpay.** { *; }
-keep class com.razorpay.BuildConfig { *; }
-keep class org.json.** { *; }
-keep class com.google.gson.** { *; }
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn com.squareup.okhttp3.**
-dontwarn okio.**

# Google Pay related classes
-dontwarn com.google.android.apps.nbu.paisa.inapp.client.api.**
-keep class com.google.android.apps.nbu.paisa.inapp.client.api.** { *; }

# Keep ProGuard annotations
-dontwarn proguard.annotation.*
-keep class proguard.annotation.** { *; }
-keepclassmembers class * {
    @proguard.annotation.Keep *;
}

# Firebase
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**

# Hilt Rules
-dontwarn dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper
-keep class dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Compose Rules
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Location Services
-keep class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# OSMDroid Rules
-keep class org.osmdroid.** { *; }
-dontwarn org.osmdroid.**

# Kotlinx Datetime
-keep class kotlinx.datetime.** { *; }
-dontwarn kotlinx.datetime.**

# Coil Image Loading
-keep class coil.** { *; }
-dontwarn coil.**

# Retrofit - Comprehensive rules
-keep class retrofit2.Retrofit { *; }
-keep class retrofit2.converter.gson.GsonConverterFactory { *; }
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Keep interface methods with HTTP annotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep your API service interfaces (UPDATE PACKAGE NAME IF NEEDED)
-keep interface com.example.carzorrouserside.** { *; }
-keep class com.example.carzorrouserside.data.** { *; }
-keep class com.example.carzorrouserside.model.** { *; }
-keep class com.example.carzorrouserside.network.** { *; }

# Retrofit warnings
-dontwarn org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement
-dontwarn javax.annotation.**
-dontwarn kotlin.Unit
-dontwarn retrofit2.KotlinExtensions
-dontwarn retrofit2.KotlinExtensions$*