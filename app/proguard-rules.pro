# Keep all model data classes used by Gson — R8 must not rename their fields
-keep class com.sky.storystatus.model.** { *; }
-keepclassmembers class com.sky.storystatus.model.** { *; }

# Gson internals
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Gson uses generic type information stored in a class file when working with
# fields; keep TypeToken and its subclasses
-keep class com.google.gson.reflect.TypeToken { *; }
-keep class * extends com.google.gson.reflect.TypeToken

# Keep the CatalogParser deserializer
-keep class com.sky.storystatus.data.BackgroundDeserializer { *; }
-keep class com.sky.storystatus.data.CatalogParser { *; }

# Keep enum classes intact
-keepclassmembers enum com.sky.storystatus.** {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# AndroidX / Compose — keep navigation type-safe args and Parcelable
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator CREATOR;
}
