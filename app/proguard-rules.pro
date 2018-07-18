# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in D:\Android\sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
#指定代码的压缩级别
-optimizationpasses 5
 #混淆时不会产生形形色色的类名  是否使用大小写混合
-dontusemixedcaseclassnames
 #指定不去忽略非公共的类库 是否混淆第三方jar
-dontskipnonpubliclibraryclasses
#不预校验  混淆时是否做预校验
-dontpreverify
#混淆时是否记录日志
-verbose
-ignorewarnings
#优化 混淆时所采用的算法
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
#不进行混淆、原样输出 保持哪些类不被混淆
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class com.android.vending.licensing.ILicensingService
#保护指定的类和类的成员的名称，如果所有指定的类成员出席（在压缩步骤之后）
# 保持 native 方法不被混淆
-keepclasseswithmembernames class * {
    native <methods>;
}
#保护指定的类和类的成员，但条件是所有指定的类和类成员是要存在。
#保持自定义控件类不被混淆
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context);
}
#保护指定类的成员，如果此类受到保护他们会保护的更好
-keepclassmembers class * extends android.app.Activity {
public void *(android.view.View);
}
#保持枚举 enum 类不被混淆
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#保护指定的类文件和类的成员
#保持 Parcelable 不被混淆
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** i(...);
}
-keepclassmembers class **.R$* {
    public static <fields>;
}


-keepattributes Signature, InnerClasses, LineNumberTable

# android-support
-dontwarn android.support.**
-keep class android.support.** { *; }

# app
-keep class me.wcy.music.utils.proguard.NoProGuard { *; }
-keep class * extends me.wcy.music.utils.proguard.NoProGuard { *; }

# okhttputils
-dontwarn com.zhy.http.**
-keep class com.zhy.http.** { *; }

# okhttp
-dontwarn okhttp3.**
-keep class okhttp3.** { *; }

# okio
-dontwarn okio.**
-keep class okio.** { *; }

# glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public enum com.bumptech.glide.load.resource.bitmap.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# amap
-dontwarn com.amap.api.**
-keep class com.amap.api.** { *; }

# bugly
-dontwarn com.tencent.bugly.**
-keep public class com.tencent.bugly.** { *; }

# jid3
-dontwarn org.blinkenlights.jid3.**

#geendao
-keep class org.greenrobot.greendao.**{*;}
-keepclassmembers class * extends org.greenrobot.greendao.AbstractDao {
public static java.lang.String TABLENAME;
}
-keep class **$Properties
