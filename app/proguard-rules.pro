# 忽略警告
-ignorewarnings

-keep class vi.com.gdi.** { *; }

-dontwarn com.google.protobuf.**
-keep class com.google.protobuf.** { *; }
-keep interface com.google.protobuf.** { *; }

-dontwarn com.google.android.support.v4.**
-keep class com.google.android.support.v4.** { *; }
-keep interface com.google.android.support.v4.app.** { *; }
-keep public class * extends com.google.android.support.v4.**

# Android类
-dontwarn android.telephony.**
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Fragment
-keep public class * extends android.app.Service
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.preference.Preference
-keep public class * extends android.view.View
-keep public class com.android.vending.licensing.ILicensingService*

# support
-dontwarn android.support.**
-keep class android.support.** { *; }
-keep interface android.support.** { *; }

# support v4/7库
-keep public class * extends android.support.v4.**
-keep public class * extends android.support.v7.**
-keep public class * extends android.support.annotation.**

# androidx
-dontwarn androidx.**
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-keep public class * extends androidx.**

# 自定义控件类的 get/set 方法和构造函数
-keep public class * extends android.view.View {
    *** get*();
    void set*(***);
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# 资源
-keep class **.R$* { *; }

# layout中onclick方法（android:onclick="onClick"）
-keepclassmembers class * extends android.app.Activity {
    public void *(android.view.View);
}

# 回调函数 onXXEvent
-keepclassmembers class * {
    void *(*Event);
}

# 枚举类
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Parcelable
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Serializable接口的子类中指定的某些成员变量和方法
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

# 避免混淆泛型
-keepattributes Signature

# 注解
-keepattributes *Annotation*,InnerClasses

# 抛出异常时保留代码行号
-keepattributes SourceFile,LineNumberTable

# JNI的 Native 方法
-keepclasseswithmembernames class * {
    native <methods>;
}

# OkHttp3
-dontwarn com.squareup.okhttp3.**
-dontwarn okio.**
-dontwarn org.conscrypt.ConscryptHostnameVerifier
-keep class com.squareup.okhttp3.** { *; }

# Glide
-dontwarn com.bumptech.glide.**
-keep class com.bumptech.glide.** { *; }
-keep public class * extends com.bumptech.glide.** { *; }
-keep public class * implements com.bumptech.glide.** { *; }

# 百度地图
-dontwarn com.baidu.**
-keep class com.baidu.** { *; }
-keep interface com.baidu.** { *; }
-keep class mapsdkvi.com.** { *; }
