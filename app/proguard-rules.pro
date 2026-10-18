# Proguard rules for Smart IR Remote
-keepattributes *Annotation*
-dontwarn javax.annotation.**
-keepclassmembers class * {
    @com.squareup.moshi.* <methods>;
    @com.squareup.moshi.* <fields>;
}
