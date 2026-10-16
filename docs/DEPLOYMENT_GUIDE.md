# StoryStatus — Complete Deployment & CI/CD Master Guide

This master guide documents every step required to maintain, build, test, sign, and deploy **StoryStatus** using GitHub Actions and the WallApp-style automation framework.

---

## 1. Overview of Workflows

StoryStatus features 3 core GitHub Actions workflows located in `.github/workflows/`:
1. **`build-check.yml`**: Runs automatically on every push and pull request to `main` or `develop`. Compiles code, runs unit tests (`testDebugUnitTest`), and uploads a debug APK artifact.
2. **`release-build.yml`**: Triggered automatically when pushing a version tag (e.g., `v1.0.0`) or manually via workflow dispatch. Decodes your release keystore, builds signed release AABs & APKs, and publishes a GitHub Release with the build outputs attached.
3. **`deploy-playstore.yml`**: Triggered manually via workflow dispatch. Allows selecting a target track (`internal`, `alpha`, `beta`, `production`), builds the signed release AAB, and pushes it directly to the Google Play Console.

---

## 2. GitHub Secrets Reference (Keep for Later Reference)

To enable signed builds and Google Play publishing, configure these exact secrets in your GitHub repository (**Settings > Secrets and variables > Actions > New repository secret**):

### A. For Signed Releases & GitHub Releases:
* **`KEYSTORE_BASE64`**: The Base64-encoded string of your release `.jks` keystore file.
* **`KEYSTORE_PASSWORD`**: The password protecting the keystore file (`storystatus_secure_2025` if generated locally).
* **`KEY_ALIAS`**: The alias of the key inside the keystore (`upload`).
* **`KEY_PASSWORD`**: The password for the specific key alias (`storystatus_secure_2025`).

### B. For Google Play Console Publishing:
* **`PLAY_STORE_SERVICE_ACCOUNT_JSON`**: The Base64-encoded JSON key file of your Google Cloud Service Account with Release Management permissions on the Play Console.

---

## 3. Using the Deployment Assistant Script

We have included an interactive terminal helper modeled after WallApp:
```bash
./scripts/deployment-helper.sh
```

**Available Options in the Script:**
1. **Test build locally** — Runs `./gradlew assembleDebug` to verify local compilation.
2. **Commit and push changes** — Stages, commits, and pushes current changes to remote.
3. **Create a release tag** — Prompts for a version tag (e.g., `v1.0.0`) and pushes it to trigger `release-build.yml`.
4. **View CI/CD documentation** — Prints this guide or CI/CD reference.
5. **Check GitHub secrets checklist** — Displays the required secret names.
6. **Exit** — Closes the assistant.

---

## 4. Step-by-Step Instructions for Future Releases

### Step 1: Ensure Play Console Initial Setup is Done (One-Time Only)
1. Go to [Google Play Console](https://play.google.com/console).
2. Create the app manually once with package name: `com.sky.storystatus`.
3. Invite your Google Cloud Service Account email under **Users and permissions** with **Release management** rights.

### Step 2: Push Code & Verify CI
Whenever you push code to `main`, GitHub Actions will automatically run `build-check.yml` to verify tests and build a debug APK. You can inspect results under the **Actions** tab on GitHub.

### Step 3: Trigger a Signed GitHub Release
When you are ready to cut a release:
1. Run the helper script: `./scripts/deployment-helper.sh`
2. Select option **3) Create a release tag**.
3. Enter your version tag (e.g., `v1.0.0`).
4. The workflow will sign the app with `KEYSTORE_BASE64`, publish a GitHub Release, and attach `app-release.aab` and `app-release.apk`.

### Step 4: Deploy to Google Play (Internal Testing)
1. Go to GitHub **Actions** tab.
2. Select **Deploy to Google Play**.
3. Click **Run workflow**, choose your target track (`internal`), and click **Run workflow**.
4. The signed AAB will be uploaded straight to your Google Play Console internal testing track.
