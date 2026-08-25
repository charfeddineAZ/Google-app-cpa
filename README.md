# CPA Automator — Android APK Workflow

[![Android APK Build & Error Detection](https://github.com/charfeddineAZ/Google-app-cpa/actions/workflows/android-apk.yml/badge.svg)](https://github.com/charfeddineAZ/Google-app-cpa/actions/workflows/android-apk.yml)

هذا المستودع هو مشروع **Kotlin/Compose مُصدَّر من Google AI Studio**، ويحوّله الـ workflow في
`.github/workflows/android-apk.yml` تلقائيًا إلى **تطبيق أندرويد APK جاهز للتثبيت** (نسخة debug + نسخة release موقّعة)
مع نظام **كشف أخطاء محسّن** يعرض كل مشكلة مباشرة داخل صفحة الـ Pull Request / الـ Actions.

---

## 🔄 ماذا يفعل الـ workflow؟

يُشتغل تلقائيًا عند كل push على `main`، وكل tag يبدأ بـ `v`، وعند كل Pull Request، ويدويًا من
تبويب **Actions → Android APK Build & Error Detection → Run workflow**.

### الوظيفة 1: `Build APK`
| الخطوة | التفاصيل |
|---|---|
| تجهيز البيئة | JDK 21 (Temurin) + Gradle مع تخزين مؤقت للتبعيات |
| توليد Gradle Wrapper | تصدير AI Studio لا يتضمن `gradlew` — يُولَّد تلقائيًا بالإصدار الموجود في `gradle-wrapper.properties` |
| مفاتيح التوقيع | يولّد `debug.keystore`، ومفتاح release إمّا من Secrets الخاصة بك أو مفتاح CI مؤقّت |
| مفتاح Gemini API | إذا وُضع Secret باسم `GEMINI_API_KEY` يُحشى داخل `.env` قبل البناء |
| البناء | `assembleDebug` + `assembleRelease` مع `--continue` لجمع **كل** الأخطاء دفعة واحدة |
| المخرجات | Artifact باسم **`android-apk`** يحتوي ملفا APK + ملخص بالأحجام وبصمة SHA-256 |

### الوظيفة 2: `Error detection` (تعمل بالتوازي)
| الفحص | الوصف |
|---|---|
| **Android Lint** (`lintDebug`) | يفشل البناء عند وجود أخطاء Lint، ويرفع تقارير XML/HTML |
| **اختبارات الوحدة** (`testDebugUnitTest`) | تشمل اختبارات Robolectric وRoborazzi — كل فشل يظهر كـ annotation على السطر المعني |
| **detekt** (تحليل ساكن) | استشاري وغير معطِّل للبناء، مع تقرير HTML/TXT/XML |

---

## 🕵️ نظام كشف الأخطاء المحسّن

1. **Problem Matchers** — ملفات في `.github/problem-matchers/` تحوّل سطور الأخطاء الخام إلى
   **annotations ملوّنة** داخل الـ PR:
   - `kotlin.json` — أخطاء وتحذيرات مترجم Kotlin (`e:` / `w: file://…:السطر:العمود`) وأخطاء KSP
   - `java-xml.json` — أخطاء javac وأخطاء موارد AAPT2/XML
   - `gradle.json` — `Execution failed for task …` وأسباب الفشل (`Could not …` / `Failed to …`)
2. **`--continue` + `--stacktrace --warning-mode all`** — لا يتوقف عند أول خطأ؛ يجمع كل الأخطاء
   والتوبيخات (deprecations) في تشغيلة واحدة.
3. **سكربت `.github/scripts/quality_report.py`** — يحلّل تقارير Lint وJUnit وdetekt ويولّد:
   - annotations تشير إلى **الملف والسطر** لكل مشكلة
   - جدول ملخص في صفحة الـ Summary
4. **Artifacts للتقارير** — `quality-reports` (Lint/اختبارات/detekt) و`build-failure-reports`
   تُرفع دائمًا حتى عند الفشل لتسهيل التشخيص.

---

## 🔑 التوقيع بنفسك (اختياري)

بدون أي إعداد، يُبنى release APK بمفتاح CI مؤقّت (**قابل للتثبيت** لكن غير صالح للنشر على
Google Play). للتوقيع بمفتاحك الحقيقي أضف ثلاثة Secrets:

| Secret | القيمة |
|---|---|
| `KEYSTORE_BASE64` | ملف الـ keystore مُحوّل base64: `base64 -w0 my-upload-key.jks` |
| `STORE_PASSWORD` | كلمة مرور مخزن المفاتيح |
| `KEY_PASSWORD` | كلمة مرور المفتاح |

> المفتاح يجب أن يكون بأسم مستعار **`upload`** كما في `app/build.gradle.kts`. لتوليد keystore جديد:
> ```bash
> keytool -genkeypair -keystore my-upload-key.jks -alias upload \
>   -keyalg RSA -keysize 2048 -validity 10000
> ```

Secret إضافي اختياري: `GEMINI_API_KEY` ليُحشى داخل الـ APK عند البناء.

---

## 📥 الحصول على الـ APK

1. تبويب **Actions** ← آخر تشغيل ناجح لـ **Android APK Build & Error Detection**
2. أسفل الصفحة قسم **Artifacts** ← حمّل **`android-apk`**
3. فك الضغط ← `app-debug.apk` أو `app-release.apk` ← ثبّته على جهازك (فعّل "التثبيت من مصادر غير معروفة")

---

## 🧪 تشغيل محلي

```bash
# التصدير لا يتضمن gradlew — ولّده أولًا ثم ابنِ
gradle wrapper
./gradlew assembleDebug

# الفحوصات نفسها التي تعمل في CI
./gradlew lintDebug testDebugUnitTest --continue --warning-mode all
```

---

## 📁 هيكل ملفات الـ CI

```
.github/
├── workflows/android-apk.yml        # الـ workflow الرئيسي (وظيفتان)
├── actions/android-setup/action.yml # إعداد مشترك: JDK + Gradle + Wrapper + Matchers
├── problem-matchers/                # تحويل سطور الأخطاء إلى annotations
│   ├── kotlin.json
│   ├── java-xml.json
│   └── gradle.json
└── scripts/quality_report.py        # تحليل التقارير → annotations + ملخص
```
