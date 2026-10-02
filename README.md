# Web2APK IDE 🚀
> **Mobile-First Website to Native Android Application Builder & IDE**

Web2APK IDE is a mobile development environment inspired by HopWeb. It allows users to import websites, manage HTML/CSS/JS files, test live in an interactive preview with an in-app JavaScript console, configure native Android application properties, and generate full Android Gradle projects.

---

## 📱 Automatic Debug APK Building & Direct Download

This repository is pre-configured with a **GitHub Actions CI/CD Pipeline** (`.github/workflows/build-and-release.yml`) for automatic building and direct downloading.

### 📥 How to Download the Built APK:

1. **GitHub Releases (Direct One-Click Download — No Account Needed):**
   - Go to the **Releases** tab of this repository: `https://github.com/<owner>/<repo>/releases`
   - Look for the latest release (e.g., `Web2APK IDE v1.0.x (Debug APK)`).
   - Under **Assets**, click **`Web2APK-IDE-debug.apk`** to download it directly to your Android device.

2. **GitHub Actions Artifacts:**
   - Go to the **Actions** tab of this repository.
   - Click on the latest workflow run: **"Build & Release Debug APK"**.
   - Scroll down to the **Artifacts** section at the bottom.
   - Click on **`Web2APK-IDE-debug-apk`** to download the ZIP containing the debug APK.

---

## ⚙️ CI/CD Workflow Triggers

The workflow runs automatically under any of the following conditions:
* **Push to `main` or `master`**: Automatically compiles a fresh debug APK and publishes a new release tagged `v1.0.<run_number>`.
* **Push of a Version Tag (e.g. `git tag v1.0.0 && git push origin v1.0.0`)**: Builds and publishes a named release under that version tag.
* **Pull Requests**: Validates that the APK compiles cleanly without publishing a release.
* **Manual Trigger (`workflow_dispatch`)**:
  - Go to **Actions** → **Build & Release Debug APK** → **Run workflow**.
  - Choose whether to publish a GitHub Release and enter an optional custom release tag.

---

## 🛠️ Build Workflow Details

The GitHub Actions workflow performs the following automated steps:
1. **Source Checkout**: Clones the repository with full git history.
2. **Java Toolchain Setup**: Prepares JDK 17 (Temurin) with automatic Gradle dependency caching.
3. **Execution Permissions**: Ensures `./gradlew` has executable flags.
4. **Keystore Handling**: Decodes `debug.keystore.base64` or provisions a valid standard Android debug keystore.
5. **Environment Configuration**: Prepares `.env` based on `.env.example` for the Secrets Gradle Plugin.
6. **Gradle Compilation**: Executes `./gradlew assembleDebug --stacktrace --no-daemon`.
7. **Artifact Packaging**: Renames the output to `Web2APK-IDE-debug.apk` and generates SHA-256 checksums.
8. **Release Publishing**: Publishes the APK to GitHub Releases with direct download links.

---

## 📋 System Requirements for the APK
* **Android OS:** Android 7.0 (API Level 24) or newer.
* **Architecture:** ARM64, ARMv7, x86_64.
* **Permissions:** Internet (required for web preview and AI coding assistant), Camera/Mic (optional, requested on demand when enabled for WebRTC).
