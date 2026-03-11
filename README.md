# Clara Challenge - Discogs Artist Explorer

A multi-modular Android application built with Jetpack Compose that allows users to search and explore artists from the Discogs database.

## Features

- **Artist Search**: Search for artists with debounced input and recent searches
- **Artist Details**: View detailed information about artists including biography, images, and members
- **Discography Browser**: Browse an artist's releases with pagination
- **Clean Architecture**: Fully modularized codebase with separation of concerns
- **Material Design 3**: Modern UI following Material Design guidelines

## Project Structure

ClaraChallenge/
├── app/ # Main application module
├── core/ # Core modules
│ ├── model/ # Domain models
│ ├── common/ # Shared utilities, constants
│ ├── domain/ # Use cases and repository interfaces
│ ├── data/ # Repository implementations
│ └── network/ # API client and DTOs
└── feature/ # Feature modules
├── search/ # Artist search feature
│ ├── api/ # Navigation contracts
│ └── impl/ # Implementation (UI, ViewModel)
├── artist/ # Artist details feature
│ ├── api/ # Navigation contracts
│ └── impl/ # Implementation
└── discography/ # Discography feature
├── api/ # Navigation contracts
└── impl/ # Implementation


## Architecture

### Clean Architecture with Multi-Module Approach

The project follows **Clean Architecture principles** with a multi-modular structure to ensure:

- **Separation of Concerns**: Each module has a single responsibility
- **Testability**: Business logic is isolated and easily testable
- **Maintainability**: Features are independent and can be developed in parallel
- **Scalability**: New features can be added without affecting existing code

### Layer Structure

┌─────────────────────────────────────────────────────────────┐
│ Presentation Layer                                           │
│ (Feature Modules - UI, ViewModel, Navigation) │
├─────────────────────────────────────────────────────────────┤
│ Domain Layer                                                 │
│ (Use Cases, Repository Interfaces, Domain Models) │
├─────────────────────────────────────────────────────────────┤
│ Data Layer                                                   │
│ (Repository Implementations, Data Sources) │
├─────────────────────────────────────────────────────────────┤
│ Network Layer                                                │
│ (API Client, DTOs, Network Models) │
└─────────────────────────────────────────────────────────────┘


### Navigation Pattern

Type-safe navigation using interface-based contracts:

1. **API Module**: Defines navigation interfaces and routes
2. **Impl Module**: Implements navigation interfaces
3. **Hilt Injection**: Navigators are injected via `@Binds`
4. **AppNavGraph**: Composes all feature graphs in one place

### Dependency Injection

- **Hilt** for dependency injection
- **Singleton scoped** network client and repositories
- **ViewModel injection** with `@HiltViewModel`

## Configuration
### Prerequisites

- Android Studio Hedgehog | 2023.1.1 or newer
- JDK 17
- Gradle 8.4+
- Android SDK 36

### Getting Started

1. **Clone the repository**
   ```bash
   git clone https://github.com/yourusername/ClaraChallenge.git
   cd ClaraChallenge

2. Get a Discogs API Token
Create an account at discogs.com
Go to Developer Settings
Generate a personal access token

3. Add your API token

Option A: Using gradle.properties (simpler)
# In gradle.properties (project root)
DISCOGS_TOKEN=your_token_here

Option B: Using local.properties (more secure)
# In local.properties (project root)
DISCOGS_TOKEN=your_token_here
sdk.dir=/path/to/your/sdk

4. Build the project
./gradlew clean assembleDebug

5. Run the app
   Connect a device/emulator
   Click Run in Android Studio or use:

./gradlew installDebug

## Version Catalog
The project uses a version catalog (gradle/libs.versions.toml) for centralized dependency management:

[versions]
kotlin = "2.0.21"
compose-bom = "2024.02.00"
hilt = "2.51.1"
ktor = "2.3.8"

[libraries]
androidx-compose-bom = { module = "androidx.compose:compose-bom", version.ref = "composeBom" }
hilt-android = { module = "com.google.dagger:hilt-android", version.ref = "hilt" }

[plugins]
android-application = { id = "com.android.application", version.ref = "agp" }
kotlin-android = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }

## Development Process
Analysis Phase
Requirements Analysis: Identified core features: artist search, details, and discography

API Research: Explored Discogs API documentation for endpoints and data structures

Architecture Planning: Decided on Clean Architecture with multi-module structure for scalability

Design Phase
Module Breakdown: Separated core (model, network, domain, data) from features

Navigation Design: Created type-safe navigation with interface-based contracts

State Management: Chose Unidirectional Data Flow with StateFlow and immutable state classes

Error Handling: Implemented Result wrapper and sealed classes for network errors

Implementation Phase
Core Modules: Built network layer with Ktor, domain models, and repository interfaces

Feature Modules: Implemented each feature independently with its own UI and ViewModel

Navigation Integration: Connected all features through AppNavGraph with Hilt injection

Testing: Added unit tests for ViewModels and use cases

## Challenges & Solutions
Challenge	Solution
Complex navigation across modules	Interface-based navigation contracts with Hilt injection
API response serialization	Kotlinx Serialization with @Serializable annotations
Pagination across multiple screens	Shared PaginatedResult model with loading states
Error handling consistency	Sealed class hierarchy with Result wrapper

## Key Libraries
Jetpack Compose - Modern UI toolkit
Ktor - HTTP client for networking
Kotlinx Serialization - JSON parsing
Hilt - Dependency injection
Coil - Image loading
Coroutines & Flow - Asynchronous programming

## Testing
Run tests with:

# Unit tests
./gradlew test

# Android tests
./gradlew connectedAndroidTest

## Screenshots
|                           Clara-Discogs Challenge                           |
|:---------------------------------------------------------------------------:|
| **Artist Search** <img src="resources/pics/screenshot_01.png" width="200">  |
| **Artist Details** <img src="resources/pics/screenshot_02.png" width="200"> | 
|  **Discography** <img src="resources/pics/screenshot_03.png" width="200">   |

## Contributing
Fork the repository
Create a feature branch
Commit your changes
Push to the branch
Open a Pull Request


## License


## Authors
Gerardo Roberto Carmona González

## Acknowledgments
Discogs API for providing the data
Jetpack Compose team for the amazing UI toolkit
Open source community for the libraries used


This README provides:
- Clear setup instructions with token configuration
- Architecture diagram and explanation
- Development process documentation
- Project structure overview
- Technology stack details

