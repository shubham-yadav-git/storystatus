# StoryStatus CI/CD Pipeline Documentation

StoryStatus uses **GitHub Actions** and **Gradle** to automate testing, linting, debugging, signed release bundling, and optional Google Play Internal Testing deployments.

---

## 1. Workflows Overview

### A. Continuous Integration (`android-ci.yml`)
- **Triggers:** Pushes to `main` or `develop`, Pull Requests targeting `main` or `develop`, and manual dispatch (`workflow_dispatch`).
- **Actions Performed:**
  1. Checks out source code.
  2. Sets up JDK 17 & Android SDK.
  3. Caches Gradle dependencies for fast builds.
  4. Runs unit tests (`testDebugUnitTest`).
  5. Runs Android lint (`lintDebug`).
  6. Assembles debug APK (`assembleDebug`).
  7. Builds release AAB (`bundleRelease`).
  8. Uploads `debug-apk` and `release-aab` as downloadable GitHub Actions artifacts.

### B. Release & Deployment (`android-release.yml`)
- **Triggers:** Creation of version tags matching `v*.*.*` (e.g., `v1.0.0`) and manual dispatch (`workflow_dispatch`).
- **Actions Performed:**
  1. Checks out tagged source.
  2. Sets up JDK, SDK, and Gradle caching.
  3. Decodes the release keystore secret into a temporary signing keystore.
  4. Runs unit tests and lint checks.
  5. Builds a signed release Android App Bundle (`app-release.aab`).
  6. Uploads the signed AAB artifact.
  7. **Optional Google Play Deployment:** If Google Play service account credentials and keystore secrets are present, automatically publishes the signed bundle to the **Google Play Internal Testing** track.

---

## 2. GitHub Repository Secrets Configuration

To enable signed release builds and automated Google Play publishing, configure the following secrets in your GitHub Repository (`Settings > Secrets and variables > Actions`):

| Secret Name | Description |
| :--- | :--- |
| `ANDROID_KEYSTORE_BASE64` | Base64 encoded `.jks` release keystore file. |
| `ANDROID_KEYSTORE_PASSWORD` | Password for the release keystore file. |
| `ANDROID_KEY_ALIAS` | Key alias inside the keystore. |
| `ANDROID_KEY_PASSWORD` | Password for the specific key alias. |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Raw JSON key content of your Google Cloud Service Account with Play Console publishing permissions (Internal Testing track). |

### How to Encode Keystore to Base64 (macOS / Linux):
```bash
base64 -i your-release-key.jks | pbcopy
```
Paste the resulting clipboard string directly into `ANDROID_KEYSTORE_BASE64`.

---

## 3. Creating a Release Tag

To trigger a signed build and deployment:
```bash
git tag v1.0.0
git push origin v1.0.0
```
Alternatively, go to the **Actions** tab in GitHub, select **Android Release & Play Deployment**, and click **Run workflow**.

---

## 4. Troubleshooting & FAQ

- **Gradle Build Fails on Missing SDK:** The workflow automatically sets up the Android SDK using `android-actions/setup-android@v3`. Ensure compileSdk (34) tools are installed.
- **Signing Failure:** Verify that all four signing secrets (`ANDROID_KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`, `ANDROID_KEY_PASSWORD`) are correct and that `ANDROID_KEYSTORE_BASE64` was encoded cleanly without newline breaks.
- **Google Play Upload Fails:** Ensure that the service account JSON has been granted **Release Manager** or **Project Admin** permissions for `com.sky.storystatus` inside Google Play Console, and that an initial app entry was manually created in the Play Console beforehand.
