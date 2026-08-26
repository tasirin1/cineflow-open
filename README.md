# CineFlow Open

Open-source recreation of the CineFlow video streaming application, based on reverse engineering of the original APK. Built with Kotlin and modern Android architecture patterns.

## Architecture Overview

The project follows the same repository management patterns found in the CineFlow APK decompilation:

```
com.cineflow.app/
├── VideoStreamingApp.kt          # Application class (singleton init)
├── data/
│   ├── api/
│   │   ├── ServerApiService.kt        # Main REST API interface (25+ endpoints)
│   │   ├── SessionApiService.kt       # Device session API
│   │   ├── ServerApiClient.kt         # Singleton Retrofit client with lazy instances
│   │   ├── AuthInterceptor.kt         # Token refresh + 401 retry logic
│   │   └── ApiRequestHeadersInterceptor.kt
│   ├── model/
│   │   ├── BaseResponse.kt            # Generic API response wrapper
│   │   ├── StreamingModel.kt          # Content source model (movie_tv, live_tv, short_drama)
│   │   ├── StreamingModelCatalog.kt   # Model registry with search-all support
│   │   ├── VideoItem.kt / Video.kt    # Legacy + unified video items
│   │   ├── UnifiedDetailResponse.kt   # Series detail with seasons/episodes
│   │   ├── UnifiedStream.kt           # Stream URL + DRM + format info
│   │   ├── UnifiedEpisode.kt          # Episode with subtitles + access control
│   │   ├── DownloadItem.kt            # Active download state object
│   │   ├── DownloadMetadata.kt        # Compact persistence format for downloads
│   │   └── Auth/Payment/App models
│   ├── repository/
│   │   ├── ContentRepository.kt       # Videos, categories, search, stream sources
│   │   ├── AuthRepository.kt          # Login, device linking, account status
│   │   ├── DownloadRepository.kt      # Download queue, persistence, progress tracking
│   │   ├── FavoriteRepository.kt      # Favorites with JSON persistence
│   │   └── HistoryRepository.kt       # Watch history with position tracking
│   ├── local/
│   │   ├── TokenManager.kt            # JWT token storage + refresh
│   │   └── PreferenceManager.kt       # App preferences (device ID, widevine, etc.)
│   └── service/
│       └── MyDownloadService.kt       # Media3 DownloadService for background downloads
├── domain/
│   ├── repository/                    # Repository interfaces (DI boundary)
│   └── usecase/                       # Business logic use cases
└── util/
    ├── StreamHelper.kt               # HLS/DASH stream detection utilities
    └── NotificationHelper.kt         # Download notification builder
```

## Key Patterns from CineFlow APK

### Repository Management (Singleton Pattern)
The original CineFlow uses a large singleton class (`h6.r`, mapped to `DownloadRepository`) that manages:
- **ConcurrentHashMap** for thread-safe active downloads and pending queue
- **StateFlow** for reactive UI updates
- **JSON file persistence** for pending downloads and metadata
- **DownloadMetadata** as compact persistence format (saves bandwidth vs full DownloadItem)

### Download Queue Architecture
1. **Pending phase**: Downloads queued as `pending_{bookId}_{episodeId}` before ExoPlayer is ready
2. **Active phase**: Promoted to real content URL when DownloadManager picks up the task
3. **Progress tracking**: Status mapped from ExoPlayer states (QUEUED→Antrean, DOWNLOADING→Mengunduh%, COMPLETED→Selesai, FAILED→Gagal)
4. **Metadata caching**: Detail responses cached per-series for offline access

### API Layer
- Retrofit + Gson with lazy singleton instances
- Separate public/authorized/feedback API clients
- Auth interceptor with automatic 401 token refresh
- Legacy TLS trust for backward compatibility

### Stream Detection
- HLS detection via URL patterns (.m3u8), master URL presence, and format field
- DASH detection via format field
- DRM support with Widevine scheme detection
- Audio/video track separation for segmented downloads

## Data Flow

```
UI (Fragment/Activity)
  ↓ observes StateFlow
ViewModel
  ↓ calls suspend functions
Repository
  ↓ uses API service + local storage
API (Retrofit) / Local (SharedPreferences/Files)
```

## Content Types
- `movie_tv` — Movies and TV series with seasons/episodes
- `live_tv` — Live streaming content
- `short_drama` — Short drama content with segmented episodes
