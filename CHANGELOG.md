# Changelog

## [Unreleased]

### Added
- Logging error yang detail lewat `AppLogger` di seluruh alur (splash, session, login, API) untuk memudahkan diagnosa masalah.
- CI: job lint (`lintDebug`, abortOnError) + unit test (`testDebugUnitTest`).
- CI: guard changelog otomatis — perubahan di `app/src` wajib menyertakan entri `CHANGELOG.md`.
- CI: bump `versionCode` otomatis dari run number (pola `100000 + run_number`).
- CI: build release ditandatangani keystore dari GitHub secrets
- Perbaiki syntax YAML `build.yml` (quote nama step berisi `:`, pakai `run: |` untuk bump versionCode). (KEYSTORE_BASE64, KEYSTORE_PASSWORD, KEY_ALIAS, KEY_PASSWORD) bila tersedia; ikut upload `mapping.txt`.
- Unit test murni JVM untuk serialisasi `DownloadMetadata` via Gson.
- `AGENTS.md` dan `README.md` disinkronkan dengan struktur aktual (bug historis + keputusan archiving dicatat).
<<<<<<< HEAD
- Login device pairing: masuk tanpa Google Sign-In (tidak butuh SHA-1 terdaftar) — tampilkan kode + link verifikasi, polling status, tukar `grant_token` jadi `access_token`, plus tombol "Salin info debug" di halaman login.

### Fixed
- `ApiService.kt` dan `SessionManager.kt`: method device pairing dipindah ke dalam `interface`/`object` (sebelumnya berada di luar kurung tutup sehingga gagal compile).
=======
- Aktifkan branch protection `main` (required checks Build APK/CodeQL/Gitleaks, 1 approval PR, enforce_admins).
>>>>>>> origin/main

### Changed
- `app/build.gradle.kts`: `versionCode` kini di-override CI via property `versionCodeOverride`; signing release diambil dari variabel env keystore bila tersedia; aktifkan lint `abortOnError`.

## [0.2.7] - 2026-08-26

### Added
- Struktur repository mengikuti pola Tasirin Download Manager
- DownloadItem + DownloadItemCodec + DownloadRepository
- DownloadEngine singleton dengan StateFlow
- DownloadService foreground service
- API models dan Retrofit client
- CI workflows: build, CodeQL, Gitleaks
