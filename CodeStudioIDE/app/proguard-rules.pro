# Add project specific ProGuard rules here.

# Keep Sora Editor
-keep class io.github.rosemoe.** { *; }

# Keep Room entities
-keep class com.codestudio.ide.data.** { *; }

# Keep Gson classes
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.google.gson.** { *; }

# Keep our models
-keep class com.codestudio.ide.model.** { *; }
