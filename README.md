# Web2APK IDE 🚀
> **Mobile-First Website to Native Android Application Builder & IDE**

[![Build & Release Debug APK](https://github.com/tata125125tata-tech/web-to-apk/actions/workflows/build-and-release.yml/badge.svg)](https://github.com/tata125125tata-tech/web-to-apk/actions/workflows/build-and-release.yml)
[![Direct APK Download](https://img.shields.io/badge/Direct%20Download-Debug%20APK-00B4D8?style=flat-square&logo=android)](https://github.com/tata125125tata-tech/web-to-apk/releases/latest/download/Web2APK-IDE-debug.apk)

Web2APK IDE is a mobile development environment inspired by HopWeb. It allows users to import websites, manage HTML/CSS/JS files, test live in an interactive preview with an in-app JavaScript console, configure native Android application properties, and generate full Android Gradle projects.

---

## 📱 Direct Debug APK Download (GitHub Releases)

You can download the compiled Debug APK directly to your Android device without needing a GitHub account:

* 📥 **[Download Latest Web2APK-IDE-debug.apk](https://github.com/tata125125tata-tech/web-to-apk/releases/latest/download/Web2APK-IDE-debug.apk)**
* 🏷️ **[View All Releases & Changelogs](https://github.com/tata125125tata-tech/web-to-apk/releases)**
* ⚡ **[View GitHub Actions Build Runs](https://github.com/tata125125tata-tech/web-to-apk/actions)**

---

## 🚀 How to Push & Trigger Automated APK Builds

To push updates from your local workspace to `tata125125tata-tech/web-to-apk`:

```bash
git remote set-url origin https://github.com/tata125125tata-tech/web-to-apk.git
git add .
git commit -m "Deploy Web2APK IDE"
git push origin main
```

Whenever code is pushed to `main` (or when a version tag like `v1.0.0` is pushed), GitHub Actions automatically:
1. Sets up the JDK 17 environment.
2. Restores or provisions the Android debug keystore.
3. Compiles the Android Debug APK using Gradle.
4. Uploads the build artifact to GitHub Actions.
5. Publishes a new **GitHub Release** with the direct download link for `Web2APK-IDE-debug.apk`.

---

## ⚙️ Manual Build Trigger (Workflow Dispatch)

You can also trigger a fresh build manually at any time:
1. Navigate to: **[GitHub Actions — Build & Release Debug APK](https://github.com/tata125125tata-tech/web-to-apk/actions/workflows/build-and-release.yml)**
2. Click **Run workflow**.
3. (Optional) Enter a custom release tag or leave blank for automatic numbering.
4. Click **Run workflow**. In ~2-3 minutes, the new APK will be built and published to Releases!

---

## 📋 System Requirements for the APK
* **Android OS:** Android 7.0 (API Level 24) or newer.
* **Architecture:** ARM64, ARMv7, x86_64.
* **Permissions:** Internet (required for web preview and AI coding assistant), Camera/Mic (optional, requested on demand when enabled for WebRTC).
