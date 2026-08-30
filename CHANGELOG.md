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
- CI: publish APK (debug + release + `mapping.txt`) ke GitHub Releases setiap push ke `main` — muncul di kolom "Rilis".
- Login device pairing: masuk tanpa Google Sign-In (tidak butuh SHA-1 terdaftar) — tampilkan kode + link verifikasi, polling status, tukar `grant_token` jadi `access_token`, plus tombol "Salin info debug" di halaman login.
- Aktifkan branch protection `main` (required checks Build APK/CodeQL/Gitleaks, 1 approval PR, enforce_admins).
- Docs: `AGENTS.md` & `README.md` sinkron — alur device pairing, tombol salin info debug, dan publish rilis otomatis ke GitHub Releases.
- CI: scan VirusTotal otomatis setiap build (submit APK, tunggu hasil, ringkasan deteksi) pakai secret `VT_API_KEY`.

### Fixed
- `ApiService.kt` dan `SessionManager.kt`: method device pairing dipindah ke dalam `interface`/`object` (sebelumnya berada di luar kurung tutup sehingga gagal compile).
- `SessionManager.exchangeDeviceLink`: baca `token_info` dari respons server (respons exchange device pairing membungkus token di `data.token_info.access_token` dan `data.token_info.expires_in`, bukan langsung di level `data`; karena itu `accessToken` selalu `null` dan login gagal meski server sudah menyetujui). Tambah model `DeviceLinkExchangeResponseData` + `DeviceLinkTokenInfo`. Simpan `refresh_token` ke prefs.

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
