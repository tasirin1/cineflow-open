# CineFlow Open

Open-source recreation of the CineFlow video streaming application, built from reverse engineering of the original APK. Kotlin + modern Android architecture, dikelola mengikuti pola repo Tasirin Download Manager.

## Struktur

```
app/src/main/java/com/cineflow/app/
├── VideoStreamingApp.kt          # Application — init SessionManager
├── LaunchActivity.kt             # Splash — navigasi Login/Main berdasarkan token
├── LoginActivity.kt              # Google Sign-In + Device Pairing (kode TV) + salin info debug
├── MainActivity.kt               # Bottom navigation (HOME/UNDUHAN/AKUN) + HomeFragment
├── data/
│   ├── api/
│   │   ├── ApiClient.kt          # Singleton Retrofit (baseUrl + auth header)
│   │   ├── ApiService.kt         # Retrofit interface
│   │   └── SessionManager.kt     # Nonce, Google login, Device Pairing, token storage
│   ├── model/
│   │   ├── BaseResponse.kt / ApiModels.kt   # Wrapper respons + model API
│   │   ├── StreamingModel.kt     # Model sumber konten
│   │   ├── AuthModels.kt         # Nonce + Google login + Device Pairing + user
│   │   └── DownloadItem.kt / DownloadMetadata.kt / DrmInfoData.kt
│   └── repository/
│       └── DownloadManager.kt    # Singleton download (pola h6.r dari APK)
├── ui/home/
│   ├── HomeFragment.kt           # Fetch models dari API + tampilkan list
│   └── ModelAdapter.kt           # RecyclerView adapter (Coil)
└── util/
    └── AppLogger.kt              # Logging konsisten (tag "CineFlow/<class>")
```

## Alur auth (wajib)

Server CineFlow sudah memakai **auth v2** — endpoint lama `api/app/session` dihapus (410).

1. `POST /api/app/auth/nonce` — dapat nonce.
2. Google Sign-In (`idToken`).
3. `POST /api/app/auth/login/google-account` — tukar nonce + idToken jadi `accessToken`.
4. `accessToken` dipakai sebagai `Authorization: Bearer <token>` untuk semua API.

Alternatif tanpa Google (tidak butuh SHA-1 terdaftar) — **Device Pairing**:

1. `POST /api/app/auth/device/pairing` — dapat `user_code` + `verification_uri`.
2. User buka link & masukkan kode di browser.
3. Polling `GET /api/app/auth/device/status` sampai `is_authenticated=true`.
4. `POST /api/app/auth/device/exchange` — tukar `grant_token` jadi `accessToken`.

## Build

Build resmi hanya lewat GitHub Actions (jangan build lokal untuk rilis):

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew assembleDebug -PversionCodeOverride=100012
```

- `versionCode` di-bump otomatis oleh CI (`100000 + run_number`), jangan diubah manual.
- Release di-sign dari GitHub secrets (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`).
- Setiap perubahan kode wajib update `CHANGELOG.md` (di-guard CI).
- Setiap push ke `main`, CI otomatis membuat **GitHub Release** (debug + release APK).

## Status

- Halaman HOME: menampilkan daftar model/sumber streaming dari API.
- UNDUHAN & AKUN: masih placeholder (belum ada fragment).
- DownloadManager: pola singleton + StateFlow + JSON persistence (dari `h6.r`).

## Keamanan

- CodeQL + Gitleaks diwajibkan hijau di protected branch `main`.
- Jangan commit keystore/secret; jangan tempel secret di issue/PR/chat.
- Baca `AGENTS.md` untuk panduan lengkap ke AI.
