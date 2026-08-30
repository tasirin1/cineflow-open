# Panduan pengelolaan repo CineFlow Open (untuk AI)

Baca file ini SEBELUM mengubah, memperbaiki, atau mengelola repository ini.
Repo ini adalah rekonstruksi open-source dari aplikasi CineFlow (APK di-decompile), dikelola dengan pola yang sama seperti repo Tasirin Download Manager.

## Struktur repository (kondisi aktual — jangan sampai basi)

```
.
├── .github/workflows/
│   ├── build.yml          # CI utama: guard changelog, lint, unit test, assembleDebug/Release
│   ├── codeql.yml         # Analisis keamanan statis (java-kotlin)
│   └── gitleaks.yml       # Deteksi secret yang ter-commit
├── gradle/libs.versions.toml    # Version catalog — pusat versi dependensi
├── app/build.gradle.kts  # minSdk 21 / targetSdk 35; versionCode di-override CI
├── app/src/main/
│   ├── AndroidManifest.xml
│   ├── res/              # layout, drawable (vector), menu, values, color
│   └── java/com/cineflow/app/
│       ├── VideoStreamingApp.kt   # Application — init SessionManager
│       ├── LaunchActivity.kt      # Splash — navigasi Login/Main berdasarkan token
│       ├── LoginActivity.kt       # Google Sign-In (nonce + login v2)
│       ├── MainActivity.kt        # Bottom navigation + HomeFragment
│       ├── data/api/
│       │   ├── ApiClient.kt       # Singleton Retrofit (baseUrl + auth header)
│       │   ├── ApiService.kt      # Retrofit interface
│       │   └── SessionManager.kt  # Nonce, Google login, token storage
│       ├── data/model/            # BaseResponse, StreamingModel, Upload/Download, Auth models
│       ├── data/repository/
│       │   └── DownloadManager.kt # Singleton download (pola h6.r dari APK)
│       ├── ui/home/
│       │   ├── HomeFragment.kt    # Fetch models dari API + tampilkan list
│       │   └── ModelAdapter.kt    # RecyclerView adapter (Coil)
│       └── util/
│           └── AppLogger.kt       # Logging konsisten (tag "CineFlow/<class>")
└── CHANGELOG.md
```

## Aturan penting (pola Tasirin Download Manager)

- **Satu commit/PR = satu tujuan.** Style commit: `type(scope): deskripsi`.
  Tipe: `feat`, `fix`, `ui`, `perf`, `refactor`, `docs`, `chore`, `rebrand`.
- **Wajib update `CHANGELOG.md`** pada setiap perubahan kode (`app/src`, `build file`, `scripts/`).
  CI punya guard otomatis: perubahan di `app/src` tanpa entri `CHANGELOG.md` = build gagal.
- **Jangan ubah `versionName`/`versionCode` manual.** CI yang set `versionCode`
  (pola Tasirin: `100000 + run_number`) via `-PversionCodeOverride`.
- **Jangan build lokal untuk rilis.** Build resmi lewat GitHub Actions.
  Setiap push ke `main` wajib dipantau sampai workflow sukses
  (`gh run watch <id> --exit-status`), dan APK benar-benar ter-publish.
- **Jangan commit keystore/secret.** Release di-sign dari GitHub secrets:
  `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- **Jangan install Android SDK lokal.** Andalkan CI; untuk cek lokal cukup guard ringan (skrip Python), bukan build penuh.

## Keputusan historis (hal yang sengaja dilakukan/dihapus)

- **Auth v2 wajib.** Endpoint lama `POST /api/app/session` sudah dihapus server (410
  `legacy_session_removed_use_google_auth_v2`). Jangan dipakai lagi. Alur yang benar:
  `POST /api/app/auth/nonce` → Google Sign-In `idToken` → `POST /api/app/auth/login/google-account`.
- **Base URL API:** `https://ngintipya2.cineflow.my.id/` (di-decode dari `a7.p` APK).
  Jangan ganti ke domain lain.
- **Tanpa mipmap PNG.** Semua icon pakai vector drawable (`res/drawable/`). Referensi
  `@mipmap/` dilarang (bug historis: mipmap XML di folder density membuat build gagal).
- **minify/R8 release off** (sementara). `isMinifyEnabled = false` untuk mencegah
  crash tak terduga; `mapping.txt` tetap di-upload bila dihasilkan.
- **Halaman UNDUHAN/AKUN** di bottom nav masih placeholder (belum ada fragment).
  Hanya HOME yang berfungsi menampilkan model.

## Bug yang pernah terjadi + guard-nya

| Bug | Guard |
|-----|-------|
| Crash di `VideoStreamingApp` karena init `DownloadManager.getInstance` saat app start | Jangan eager-init di `Application`; init `SessionManager` saja |
| App mentok logo / force close karena `MainActivity` stub tanpa UI | Jangan menghapus `HomeFragment`/`MainActivity` yang sudah jalan |
| `Base URL` API salah → SSL/TLS `UNRECOGNIZED_NAME` | Pakai BASE_URL di `ApiClient` (jangan ganti) |
| `DUPLICATE AuthLoginResponseData` (dua file) → build gagal | Satu definisi model di satu file; guard CI = `compile` |
| `@mipmap` XML di folder density → AAPT2 error | Pakai `@drawable` vector |
| `ic_launcher_foreground.xml` ada atribut `fillColor` ganda → AAPT2 error | Jangan duplikasi atribut dalam satu elemen `<path>` |
| Build release gagal karena tidak ada signing config | Signing diambil dari env/secret; release di-skip jika secret kosong |
| Lint/unit test tidak jalan | CI wajib `lintDebug` + `testDebugUnitTest` |

## Cara build (hanya di CI)

```bash
./gradlew lintDebug
./gradlew testDebugUnitTest
./gradlew assembleDebug -PversionCodeOverride=100012
./gradlew assembleRelease -PversionCodeOverride=100012   # perlu env keystore
```

## Keamanan

- CodeQL + Gitleaks wajib hijau; keduanya **required** di protected branch `main`.
- Pintu masuk ke `main` lewat PR, bukan push langsung. Branch protection `main` SUDAH AKTIF:
  required checks `Build APK`, `Analyze (java-kotlin)` (CodeQL), `Scan secret` (Gitleaks),
  1 approval PR, `enforce_admins` ON, strict-up-to-date.
  Push langsung ke `main` akan ditolak GitHub.
- Jangan pernah menulis isi secret/keystore/password di issue, PR, chat, atau README.
