# General
-keepattributes Signature, Annotation

# ORMLite
-keepclassmembers class com.j256.** { *; }
-keepclassmembers enum com.j256.** { *; }
-keepclassmembers interface com.j256.** { *; }
-keep class com.j256.**
-keep enum com.j256.**
-keep interface com.j256.**

-keepclassmembers class com.basilfx.bierapp.data.models.** {
	private <fields>;
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}

# Google Gson
-keep class sun.misc.Unsafe { *; }
-keepclassmembers class com.basilfx.bierapp.data.Api* {
	public <fields>;
}

# Warnings to be removed.
-dontwarn com.sun.xml.internal.**
-dontwarn com.sun.istack.internal.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.springframework.**
-dontwarn java.awt.**
-dontwarn javax.security.**
-dontwarn java.beans.**
-dontwarn javax.xml.**
-dontwarn java.util.**
-dontwarn org.w3c.dom.**
-dontwarn com.google.common.**