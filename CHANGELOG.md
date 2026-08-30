# Changelog

## [Unreleased]

### Added
- Logging error yang detail lewat `AppLogger` di seluruh alur (splash, session, login, API) untuk memudahkan diagnosa masalah.

## [0.2.7] - 2026-08-26

### Added
- Struktur repository mengikuti pola Tasirin Download Manager
- DownloadItem + DownloadItemCodec + DownloadRepository
- DownloadEngine singleton dengan StateFlow
- DownloadService foreground service
- API models dan Retrofit client
- CI workflows: build, CodeQL, Gitleaks
