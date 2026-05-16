# Zaria ServiceConnect Android App

This is the Kotlin Android application for Zaria ServiceConnect.

Kotlin is the programming language used for the Android app. The app gives residents and providers mobile access to the same system that is available on the web.

## Why The Android App Exists Alongside The Web App

Some users prefer using a phone instead of a laptop. In a service marketplace, mobile access is important because residents may need quick help, and providers may need to respond while moving around.

The Android app makes the system easier to use on mobile phones.

## What The Android App Does

Residents can:

- Register and login.
- Search providers.
- Browse categories.
- View provider details.
- Create bookings.
- Track bookings.
- Confirm job completion.
- Submit reviews.
- Submit complaints.
- View complaint messages.
- View notifications.

Providers can:

- Register with verification documents.
- Login.
- View booking requests.
- Accept or decline bookings.
- Request completion.
- Set availability.
- Update provider profile.
- View complaint messages.
- View notifications.

## How It Communicates With The Backend

The Android app uses Retrofit.

Retrofit is a Kotlin/Android library that makes it easier to call backend APIs. Instead of manually building every internet request, the app defines functions like `login`, `searchProviders`, and `createBooking`.

The Retrofit setup is in:

```text
app/src/main/java/com/example/zariaserviceconnect/network/RetrofitClient.kt
```

The API list is in:

```text
app/src/main/java/com/example/zariaserviceconnect/network/ApiService.kt
```

The backend URL is:

```text
https://zaria-serviceconnect-backend-production.up.railway.app/
```

## Login Token

When a user logs in, the backend returns a token. The Android app saves that token using `TokenManager`.

After that, the app automatically adds this token to future requests. This proves to the backend that the user is logged in.

## Main App Layers

### Screens

Screens are the pages the user sees.

Examples:

- Login and registration screens.
- Resident screens.
- Provider screens.
- Notification screens.
- Complaint message screens.

### ViewModel

The `MainViewModel` connects the screens to the data operations.

Simple explanation:

The screen says, "I need providers." The ViewModel asks the repository to load providers. When the data comes back, the ViewModel gives it to the screen.

### Repository

The repository is in:

```text
app/src/main/java/com/example/zariaserviceconnect/repository/AppRepository.kt
```

It calls the API service and handles responses.

### Network Layer

The network layer sends requests to the backend using Retrofit.

## Key Features

### Login And Registration

Residents register with basic personal details.

Providers register with service details and uploaded documents. Admin must approve the provider before they become visible to residents.

### Provider Search

The app can search providers by service, category, name, description, or location. It can also send latitude and longitude so the backend can sort nearby providers first.

### Booking

The resident creates a booking. The provider can accept or decline. The provider can request completion, and the resident confirms completion.

### Availability

Providers can set themselves as available, busy, or offline. Offline providers cannot receive new bookings.

### Complaints

Residents can submit complaints about bookings. Admin can review the complaint and communicate with the resident or provider.

### Notifications

The app can load notifications from the backend. It also includes Firebase messaging service code for push notification support, with device tokens registered through the backend.

## Important Files

- `MainActivity.kt`: starts the Android app.
- `network/ApiService.kt`: list of backend API calls.
- `network/RetrofitClient.kt`: backend connection setup.
- `repository/AppRepository.kt`: handles data requests.
- `viewmodel/MainViewModel.kt`: connects UI screens to repository actions.
- `ui/resident/ResidentScreens.kt`: resident screens.
- `ui/provider/ProviderScreens.kt`: provider screens.
- `ui/auth/AuthScreens.kt`: login and registration screens.
- `notifications/AppFirebaseMessagingService.kt`: Android notification handling.
- `utils/TokenManager.kt`: saves and reads login token.
- `utils/LocationHelper.kt`: helps with location features.

## How To Run

Open the `ZariaServiceConnect` folder in Android Studio.

Then:

1. Let Gradle sync.
2. Connect an Android phone or start an emulator.
3. Click Run.

The app will connect to the hosted backend URL in `RetrofitClient.kt`.

