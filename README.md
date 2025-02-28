# Medicine Reminder App

A modern Android application for managing medicine schedules and reminders, built with the latest Android development technologies and best practices.

## Features

- Create and manage medicine schedules
- Set up customizable reminders with full-screen notifications
- Works offline with local data storage
- Import/Export functionality for data backup
- Material Design 3 UI with dynamic theming
- Dark mode support
- Supports Android 5.0 (API 21) and above

## Architecture & Technical Details

### Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: Clean Architecture with Domain-Driven Design
- **Dependency Injection**: Hilt
- **Local Database**: Room
- **Asynchronous Programming**: Coroutines & Flow
- **Navigation**: Jetpack Navigation Compose
- **Background Processing**: WorkManager
- **Testing**: JUnit, Mockito

### Project Structure

The project follows Clean Architecture principles with the following layers:

- **Domain Layer**: Contains business logic and domain models
- **Data Layer**: Implements data access and storage
- **Presentation Layer**: Handles UI and user interactions

### Design Patterns

- Repository Pattern
- MVVM (Model-View-ViewModel)
- Use Cases
- Dependency Injection

## Getting Started

### Prerequisites

- Android Studio Arctic Fox or newer
- JDK 17 or newer
- Android SDK with minimum API level 21

### Building the Project

1. Clone the repository
2. Open the project in Android Studio
3. Sync project with Gradle files
4. Run the app on an emulator or physical device

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Acknowledgments

- Material Design 3 Guidelines
- Android Architecture Components
- Clean Architecture by Robert C. Martin 