![Barber shop App Banner](https://i.imgur.com/wmCYywa.png)

# Barber Shop App

A comprehensive Android application for barbershop appointment booking and management. This app provides separate interfaces for customers and barbershop owners, featuring real-time appointment scheduling, service selection, and business management.

## Features

### For Customers
- **User Authentication**: Create accounts and securely login
- **Service Browsing**: View all available haircut services with details and pricing
- **Appointment Booking**: Schedule appointments with preferred date and time
- **Appointment Management**: View, modify, and cancel existing appointments
- **Profile Management**: Update personal information and preferences

### For Barbershop Owners
- **Admin Dashboard**: Comprehensive view of all appointments and business metrics
- **Schedule Management**: Configure working hours, breaks, and holidays
- **Service Management**: Add, edit, and remove services offered
- **Appointment Oversight**: Accept, reject, or reschedule customer appointments
- **Business Settings**: Update shop details, location, and policies

## Screenshots

<div align="center">
  <img src="https://i.imgur.com/DCcjBkI.png" alt="Login" width="200"/>
  <img src="https://i.imgur.com/rtZYWii.png" alt="Services" width="200"/>
  <img src="https://i.imgur.com/wiPb4xe.png" alt="Booking" width="200"/>
  <img src="https://i.imgur.com/5Qa3lZo.png" alt="Appointments" width="200"/>
  <img src="https://i.imgur.com/1JaquI0.png" alt="Owner Appointments" width="200"/>
  <img src="https://i.imgur.com/CrqfnLm.png" alt="Shop Schedule" width="200"/>
  <img src="https://i.imgur.com/TjLLv3y.png" alt="Owner Profile" width="200"/>
</div>

## Technologies Used

- **Android Studio**: Primary development environment
- **Java**: Main programming language
- **MVVM Architecture**: For clean separation of UI, business logic, and data
- **Material Design Components**: For modern and consistent UI
- **Firebase**:
  - Authentication: For user management
  - Firestore: For data storage and real-time updates
  - Cloud Messaging (optional): For notifications and reminders

## Installation

1. Clone this repository
```bash
git clone https://github.com/yourusername/barbershop-app.git
```

2. Open the project in Android Studio

3. Connect your Firebase project:
   - Create a new Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Add an Android app to your Firebase project
   - Download the `google-services.json` file and place it in the app directory
   - Follow the Firebase console instructions to complete setup

4. Build and run the application

## Project Structure

```
app/
├── src/
│   ├── main/
│   │   ├── java/com/yourusername/barbershop/
│   │   │   ├── activities/           # Main UI containers
│   │   │   ├── adapters/             # RecyclerView adapters
│   │   │   ├── fragments/            # UI components
│   │   │   ├── models/               # Data classes
│   │   │   └── utils/                # Helper classes
│   │   └── res/                      # Resources (layouts, drawables, etc.)
│   └── test/                         # Unit and UI tests
└── build.gradle                      # App-level build configuration
```

### Key Components

#### Authentication
- `LoginActivity.java`: Email/password authentication for customers
- `RegisterActivity.java`: New user registration with profile creation
- `AdminLoginActivity.java`: Shop owner authentication with admin verification
- `AdminRegisterActivity.java`: Shop owner registration with shop details

#### Customer UI
- `MainActivity.java`: Main navigation hub with bottom navigation
- `ServiceSelectionFragment.java`: Displays available services with images and prices
- `BookingFragment.java`: Handles appointment scheduling
- `AppointmentsFragment.java`: Lists user's scheduled appointments
- `ProfileFragment.java`: User profile management and settings

#### Admin UI
- `AdminMainActivity.java`: Main navigation for shop owners
- `AdminAppointmentsFragment.java`: Appointment management for shop owners
- `ScheduleManagementFragment.java`: Working hours and holiday configuration
- `AdminSettingsFragment.java`: Shop settings and service management

#### Data Models
- `Service.java`: Service details (name, price, duration)
- `Appointment.java`: Appointment information (date, service, status)
- `BarberShop.java`: Shop information and availability
- `WorkingHours.java`: Shop schedule configuration
- `TimeSlot.java`: Available appointment times
- `Holiday.java`: Shop closure dates

## Features In Detail

### Dynamic Scheduling System
- Time slots generated based on service duration
- Respects working hours and breaks
- Respects days off and holiday breaks

### Real-time Updates
- Appointment status changes reflect immediately
- Shop owners receive instant notification of new bookings
- Customers see real-time availability

### Dual User Roles
- Separate flows for customers and shop owners
- Different permissions and UI based on user type

### App Design
- Dark mode available!

## License

This project is licensed under the MIT License.

## Acknowledgements

- [Firebase](https://firebase.google.com/) for backend services
- [Material Components for Android](https://material.io/develop/android) for UI components


# In order to run the app

# Adding google-services.json from Firebase to Android Studio

This tutorial explains how to integrate Firebase into an Android project while requiring each user to add their own `google-services.json` file manually.

## 1. Generate google-services.json from Firebase

Each user must generate their own `google-services.json` from Firebase:

1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Select your Firebase project (or create a new one).
3. Click on the gear icon ⚙️ > **Project settings**.
4. In the **General** tab, scroll down to **Your apps** and select **Android**.
5. Register your app by providing the package name (ensure it matches your app's package name).
6. Download the `google-services.json` file.

## 2. Add google-services.json to Android Studio

Each user must place their `google-services.json` file manually:

1. Open Android Studio.
2. Copy `google-services.json`.
3. Paste it inside the `app/` directory (not the root directory of the project).

## 3. Ignore google-services.json in Version Control

Since each user needs their own Firebase configuration, `google-services.json` should not be committed to version control.

1. Open your project's `.gitignore` file.
2. Add the following line if not already present:
   ```gitignore
   android/app/google-services.json
   ```

This ensures that `google-services.json` is ignored when pushing code to a Git repository.

## 4. Modify build.gradle Files

Ensure that Firebase is correctly set up in your project by updating the necessary Gradle files.

### **Project-Level build.gradle (Root-level)**

Modify the `build.gradle` file in the **root project** directory:

```gradle
buildscript {
    dependencies {
        classpath 'com.google.gms:google-services:4.3.10' // Use the latest version
    }
}
```

### **App-Level build.gradle (Inside `app/` Directory)**

Modify the `build.gradle` file in the `app` directory:

```gradle
plugins {
    id 'com.android.application'
    id 'com.google.gms.google-services' // Apply Google Services plugin
}
```

## 5. Sync Gradle & Run the App

1. Click **Sync Now** in Android Studio to ensure Firebase dependencies are loaded correctly.
2. Run the app and verify that Firebase is working properly.
