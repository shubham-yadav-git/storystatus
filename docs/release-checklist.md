# Google Play Publishing & Release Checklist

This document covers tasks required to publish **StoryStatus** to the Google Play Store, distinguishing between repository tasks and Google Play Console requirements.

---

## Part 1: Tasks Completed in Code Repository
- [x] **Branding & Identity:** Updated all user-facing strings, UI headers, and launcher assets to **StoryStatus**.
- [x] **Package & Application ID:** Updated package name and application ID to `com.sky.storystatus`.
- [x] **Scoped Storage / MediaStore:** Implemented modern Scoped Storage for exporting high-res 1080x1920 PNG files on Android 10+ without broad permissions.
- [x] **Offline Architecture:** Ensured 100% offline-first operation with bundled local assets and no mandatory network dependencies.
- [x] **Privacy Policy Draft:** Created `docs/privacy-policy.md` detailing local data handling and zero third-party tracking.
- [x] **Store Listing Copy:** Created `docs/store-listing.md` with descriptions, screenshot plans, and keywords.
- [x] **Unit Testing:** Verified all core domain logic, JSON parsing, and state flows with 25 passing unit tests.

---

## Part 2: Tasks to be Completed in Google Play Console
- [ ] **Developer Account:** Verify Google Play Developer Account (identity verification, payment setup).
- [ ] **App Creation:** Create a new app in Play Console named `StoryStatus`.
- [ ] **Store Listing Setup:**
  - Enter App Name (`StoryStatus: Status Maker`), Short Description, and Full Description (from `docs/store-listing.md`).
  - Upload Store Graphic Assets:
    - App Icon (512x512 PNG)
    - Feature Graphic (1024x500 PNG)
    - Phone Screenshots (minimum 2 portrait screenshots)
- [ ] **Privacy Policy URL:** Host `docs/privacy-policy.md` on a public URL and link it in the Play Console Store Settings.
- [ ] **Content Rating Questionnaire:** Complete the IARC content rating questionnaire (will rate as "Everyone").
- [ ] **Target Audience & Content Declaration:**
  - Declare target age group (e.g., 13+ or Everyone).
  - Complete Data Safety form (Declare: *No data collected or shared; data stored locally on device only*).
  - Declare app does not contain ads.
- [ ] **App Access / Login Requirements:** Declare that all functionality is available without restrictions or login.
- [ ] **App Signing:** Enable Play App Signing and upload the signed release AAB (Android App Bundle).

---

## Part 3: External Review & Release
- [ ] **Internal Testing Track:** Deploy the release build to an Internal Testing track and perform device verification on physical hardware.
- [ ] **Production Release:** Promote the tested artifact to Production and submit for Google Play review.
