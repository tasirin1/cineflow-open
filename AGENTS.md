# Panduan pengelolaan repo (untuk AI)

Baca file ini SEBELUM mengubah, memperbaiki, atau mengelola repository ini.

## Struktur repository

```
.
├── .github/workflows/build.yml       # CI: build APK
├── .github/workflows/codeql.yml       # CodeQL: analisis keamanan statis
├── .github/workflows/gitleaks.yml     # Deteksi secret ter-commit
├── gradle/libs.versions.toml          # Version catalog — pusat versi dependensi
├── app/build.gradle.kts              # minSdk 21 / targetSdk 35
├── app/src/main/
│   ├── AndroidManifest.xml
│   └── java/com/cineflow/app/
│       ├── CineFlowApp.kt            # Application — inisialisasi engine download
│       ├── MainActivity.kt           # UI utama
│       ├── data/
│       │   ├── DownloadItem.kt       # Model + state download
│       │   ├── DownloadItemCodec.kt  # Serialisasi JSON
│       │   ├── DownloadRepository.kt # Persistensi daftar download
│       │   ├── ApiModels.kt          # Model API CineFlow
│       │   ├── ApiService.kt         # Retrofit interface
│       │   └── ApiClient.kt          # Singleton Retrofit client
│       ├── download/
│       │   ├── DownloadEngine.kt     # Inti unduhan
│       │   └── DownloadService.kt    # Foreground service + notifikasi
│       └── util/
│           └── NotificationHelper.kt # Notifikasi unduhan
└── AGENTS.md
```

## Aturan penting

- **Jangan ubah `versionName`/`versionCode` manual** — diatur CI.
- **Satu PR, satu tujuan.** Jangan campur banyak hal dalam satu PR.
- **Wajib update `CHANGELOG.md`** pada perubahan kode.
- **Jangan commit keystore** (`*.jks`, `keystore.b64`).
- **Jangan tempel token/secret** di issue, PR, atau chat.

## Pola repository management

Mengikuti pola Tasirin Download Manager:
- **DownloadRepository** — Persistensi dengan SharedPreferences + JSON codec
- **DownloadItemCodec** — Serialisasi/deserialisasi JSON (murni JVM, bisa unit test)
- **DownloadEngine** — Singleton dengan StateFlow untuk reactive UI
- **DownloadService** — Foreground service dengan wake lock

## Cara build

```bash
./gradlew assembleDebug    # Build debug
./gradlew assembleRelease  # Build release
```
