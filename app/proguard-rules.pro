# App-specific R8 rules. AndroidX, Compose, Coil, OkHttp, and Media3 ship their own
# consumer rules, so only RedX-specific needs are declared here.

# Keep line numbers for readable crash reports, but hide the original file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# RedX parses Reddit JSON reflectively through org.json only, so no model keeps are
# required. Kotlin metadata and coroutine internals are handled by the consumer rules
# bundled with kotlin-stdlib and kotlinx-coroutines.

# OkHttp / Okio optional platform integrations referenced but never bundled.
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-dontwarn org.slf4j.**

# WebView JavaScript bridge is not used; keep nothing extra for it.
