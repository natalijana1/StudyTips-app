# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.
# StudyTips Application

## Project Overview

StudyTips is an Android application built with Kotlin designed to help users share and discover study tips. It utilizes a modern Android architecture (MVVM) and integrates with Firebase for backend services and a local Room database for offline capability.

### Key Technologies

*   **Language:** Kotlin
*   **Architecture:** MVVM (Model-View-ViewModel) with Repository pattern.
*   **UI:** XML Layouts, Material Design Components, ViewPager2, SwipeRefreshLayout.
*   **Dependency Injection:** Manual DI (via `StudyTipsApplication`).
*   **Asynchronous Processing:** Kotlin Coroutines.
*   **Networking:** Retrofit with Gson (API: `https://api.quotable.io/`).
*   **Image Loading:** Glide.
*   **Database (Local):** Room Database.
*   **Backend (Remote):**
    *   **Authentication:** Firebase Auth.
    *   **Database:** Cloud Firestore.
    *   **Storage:** Firebase Storage.

### Project Structure

The source code is located in `app/src/main/java/com/natali/studytip/`.

*   `data/`: Data layer containing local (Room), remote (API/Firebase), repositories, and models.
    *   `local/`: Room database, DAOs, and entities.
    *   `remote/`: Retrofit API setup and Firebase managers.
    *   `repository/`: Repositories mediating between local and remote data sources.
    *   `models/`: Domain models.
*   `ui/`: UI layer organized by feature (auth, home, profile, tip).
    *   Contains Fragments and ViewModels.
*   `utils/`: Utility classes (e.g., ImageHelper, NetworkUtils).

## Building and Running

### Prerequisites

*   JDK 11
*   Android SDK (Compile SDK 36, Min SDK 26)
*   `google-services.json` must be present in the `app/` directory for Firebase integration.

### Commands

Use the Gradle Wrapper (`gradlew`) to build and run the application.

*   **Build Debug APK:**
    ```bash
    ./gradlew assembleDebug
    ```

*   **Build Release APK:**
    ```bash
    ./gradlew assembleRelease
    ```

*   **Run Unit Tests:**
    ```bash
    ./gradlew testDebugUnitTest
    ```

*   **Run Specific Test Class:**
    ```bash
    ./gradlew test --tests "com.natali.studytip.YourTestClass"
    ```

*   **Run Instrumented Tests:**
    ```bash
    ./gradlew connectedDebugAndroidTest
    ```

*   **Lint Check:**
    ```bash
    ./gradlew lintDebug
    ```

*   **Clean Project:**
    ```bash
    ./gradlew clean
    ```

*   **View Dependencies:**
    ```bash
    ./gradlew app:dependencies
    ```

## Development Conventions

*   **Architecture:** Strictly follow MVVM. Logic should reside in ViewModels or Repositories, not in Activities/Fragments.
*   **Data Flow:** `Repository` -> `ViewModel` -> `Fragment/Activity` (via LiveData/StateFlow).
*   **Dependency Injection:** Manual DI through `StudyTipsApplication`. Repositories and managers are initialized lazily as singletons. Access via `(requireActivity().application as StudyTipsApplication).repositoryName` in Fragments.
*   **ViewModel Instantiation:** Use `ViewModelFactory` to instantiate ViewModels with required dependencies. Pass repositories and managers from `StudyTipsApplication`.
*   **Coroutines:** Use `viewModelScope` for ViewModel operations and `suspend` functions for I/O operations in Repositories/DAOs.
*   **View Binding:** Used for accessing views in layouts. Always prefer ViewBinding over `findViewById`.
*   **Code Style:** Standard Kotlin coding conventions with KSP for annotation processing (Room, Glide).

## Configuration

*   **Package Name:** `com.natali.studytip`
*   **Application ID:** `com.natali.studytip`
*   **API Base URL:** Defined in `RetrofitInstance.kt` (uses `https://api.quotable.io/`).
*   **Firebase Storage Bucket:** Configurable in `StudyTipsApplication.kt` via `STORAGE_BUCKET_URL` constant (currently set to null for default bucket).

## Key Architecture Patterns

### Data Synchronization

The app implements an offline-first architecture with Firebase sync:

*   **Local-First:** All CRUD operations write to Room database first for immediate offline access.
*   **Sync Status:** `TipEntity` includes `isSynced` flag to track Firestore synchronization state.
*   **Bidirectional Sync:**
    *   `syncTipsFromFirestore()` pulls remote tips to local database.
    *   `syncUnsyncedTipsToFirestore()` pushes unsynced local tips to Firestore.
*   **Soft Deletes:** Tips are soft-deleted (marked as deleted) rather than hard-deleted to support sync.

### Repository Pattern

Repositories mediate between local (Room) and remote (Firestore/API) data sources:

*   **TipRepository:** Manages tips with Room + Firestore sync.
*   **UserRepository:** Manages local user data with Room.
*   **QuoteRepository:** Manages motivational quotes from Room + Retrofit API.
*   **AuthRepository:** Handles Firebase Authentication and user profile creation.

All repositories return `LiveData` for reactive UI updates or `Result<T>` for one-time operations.

### Entity Mapping

*   **Entities (`*Entity`):** Room database models with sync metadata.
*   **Models (`*Model`):** Domain models used in UI layer.
*   Repositories handle mapping between entities and models (e.g., `TipEntity.toTip()`).

## Navigation

*   Uses Navigation Component with Safe Args plugin for type-safe argument passing.
*   Navigation graph defined in `res/navigation/`.
*   Fragments use `findNavController()` for navigation actions.
