# PeaceApp

PeaceApp is an Android mobile application designed to provide users with a streamlined interface 
for reporting incidents, sharing locations, and staying informed about various events. Built 
using modern Android development tools, PeaceApp leverages Kotlin and Jetpack Compose for a 
responsive and intuitive user experience.

## Features
- **User Authentication**: Secure login functionality to authenticate users.
- **Incident Reporting**: Easily report and track incidents with real-time updates.
- **Geolocation**: View and share user locations on an interactive map.
- **Profile Management**: Manage user profile details and preferences.
- **Alert Notifications**: Receive notifications for critical alerts and updates.

## Project Structure
The project follows a standard Android app architecture with a clear separation of concerns:

- **Java Source Files**: Located in `src/main/java/com/innovatech/peaceapp/`
  - `MainActivity.kt`: The entry point of the application.
  - `Adapter.kt`: Manages data binding and views within lists or grids.
  - **Beans**: Holds data classes such as `User.kt`.
  - **DAO and DB**: Manage data persistence and access (future implementation).
  - **Interfaces**: Define key contracts within the app.
- **Resources** (`src/main/res`):
  - **Drawable**: Images and icons used in the application.
  - **Layouts**: XML files for UI components and screens (expected but not directly listed).
- **Android Manifest**: Defines app permissions, activities, and services.

## Getting Started

### Prerequisites
- **Android Studio** (latest version recommended)
- **Gradle** (integrated with Android Studio)

### Installation
1. **Clone the Repository**:
   ```bash
   git clone https://github.com/username/PeaceApp.git
   cd PeaceApp
   ```
2. **Open in Android Studio**:
   - Open Android Studio, click on "Open an Existing Project," and select the project directory.
3. **Build and Run**:
   - Let Android Studio sync Gradle and resolve dependencies.
   - Connect a device or use an emulator and click the "Run" button to build and deploy the app.

## Usage
1. **Login**: Use the default credentials to log in or create a new account.
2. **Navigate**: Explore the app's features, including incident reporting, maps, and alerts.
3. **Manage Profile**: Update personal details in the user profile section.
