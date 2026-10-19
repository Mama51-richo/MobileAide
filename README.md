# MobileAide — Android Kotlin foundation

**Tagline:** Turn your phone into an intelligent CCTV camera that detects, analyzes, and alerts in real time.

This is a source project foundation for Android Studio. It provides:
- CameraX live preview and front/back camera switching
- Basic frame-to-frame motion heuristic
- Motion-triggered snapshots stored encrypted using an Android Keystore AES-GCM key
- Encrypted local event timeline and deletion control
- Demo Owner/Admin/Operator/Viewer role selector
- Optional HTTPS backend event-sync client scaffold

## Important feature boundary

This MVP does **not** yet perform person/vehicle classification, face detection/recognition, plate OCR, distance estimation, speed measurement, cloud push, SMS/email/Telegram alerts, or 24/7 background camera recording. Motion events are based on pixel changes and may trigger due to lighting or camera movement. No confidence score is claimed for motion.

The demo role selector is not authentication. It is deliberately labeled as a demo; production authorization must be enforced by a server. The backend client is a scaffold and is not wired to the UI because no real API URL, identity provider, or access-token flow was supplied.

## Build in Android Studio

1. Install Android Studio with Android SDK Platform 36 and JDK 17.
2. Extract this ZIP.
3. In Android Studio choose **Open** and select the `MobileAide` folder.
4. Allow Gradle sync to finish. If prompted, install the requested SDK components.
5. Connect an Android device with USB debugging enabled, or create an emulator with camera support.
6. Press **Run**. Grant camera permission when Android asks.
7. Place the phone securely with adequate airflow and power. Keep the app visible while using the camera.

The project does not include a Gradle wrapper binary. Android Studio can sync it; alternatively generate wrapper files from a local Gradle 8.11-compatible installation, then use `./gradlew :app:assembleDebug`.

## Security and privacy notes

- Camera permission is requested at runtime; it is not bypassed.
- The app does not request microphone, location, SMS, contacts, or overlay permissions.
- App backup is disabled so encrypted event files are not casually copied through Android backup.
- Snapshots and event metadata are encrypted at rest with an AES key held by Android Keystore. Android app sandboxing is also relied upon.
- Deleting the app or clearing app data removes local event files and may invalidate the Keystore key.
- The sample stores up to 500 event metadata records; snapshot retention/automatic expiry should be added before production.
- Keep recording visible and comply with local law, notice/consent rules, and property policies.

## Project layout

```text
MobileAide/
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── res/values/styles.xml
        └── java/com/mobileaide/
            ├── MainActivity.kt
            ├── MotionAnalyzer.kt
            ├── SecureEventStore.kt
            ├── RoleManager.kt
            ├── BackendClient.kt
            └── SimpleSelectionListener.kt
```

## Next production increments

1. **Human/vehicle AI:** integrate a maintained on-device object detector with a compatible `.tflite` model and verify labels on target phones. The model file and licensing are not bundled.
2. **Plate OCR:** use ML Kit Text Recognition or another OCR model, plus a vehicle/plate detector and confidence thresholds. OCR alone does not locate plates.
3. **Push alerts:** configure Firebase Cloud Messaging, create a Firebase project, and implement server-side device registration.
4. **Backend:** deploy an HTTPS API with user login, server-side RBAC, device enrollment, audit logs, event ingestion, and database migrations. Configure a real endpoint and token provider; never ship a permanent API secret in the APK.
5. **Background recording:** implement a user-started foreground service with a persistent notification and the correct foreground-service types for the target Android version. Respect Android camera background-start restrictions; this sample intentionally does not claim background recording.
6. **Exports and retention:** add user-authorized export, configurable expiry, and deletion of snapshots alongside metadata.
7. **Testing:** validate heat, battery, low light, false positives, storage limits, permission denial, app lifecycle, and device-specific CameraX behavior.

## Backend API contract expected by `BackendClient`

`POST https://YOUR_HOST/api/events`

Headers:
- `Authorization: Bearer <short-lived user access token>`
- `Content-Type: application/json`

JSON:
```json
{
  "id": "event-uuid",
  "timestamp": 1790000000000,
  "type": "MOTION",
  "confidence": 0.0,
  "detail": "Motion heuristic event"
}
```

Use a real HTTPS host and an authenticated session. `YOUR_HOST` is documentation, not a working endpoint.
