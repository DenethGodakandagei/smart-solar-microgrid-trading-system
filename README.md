# Smart Solar Microgrid Trading System

> **Distributed Solar Energy Trading Platform** — Empowering prosumers to schedule, reserve, and trade clean solar energy through local microgrid stations with cryptographic QR code validation and interactive GPS station mapping.

---

## 👥 Member Contributions & Role Overview

This repository contains the **Member 2** component of the Smart Solar Microgrid Trading System:

- **Member 2 Focus**: **Native Android Mobile Application** (Java / Material 3) for Prosumers and Grid Station Operators, integrating with the centralized C# .NET Web API and SQLite local storage.

---

## 📱 Member 2: Native Android Application Overview

The Smart Solar Android mobile application provides an end-to-end mobile interface for solar energy prosumers and station operators.

### 🌟 Key Feature Modules

1. **Authentication & Role-Based Navigation**:
   - National Identity Card (NIC) based prosumer registration and login.
   - Dynamic routing based on role: Prosumers land on `DashboardActivity`, Operators land on `OperatorHomeActivity`.
   - JWT authentication persistence via `SessionManager` and encrypted headers.

2. **Profile & Account Management**:
   - Comprehensive profile view with account status, contact info, and registration metadata.
   - Full profile editing capabilities with live server validation.
   - Secure account deactivation workflow.

3. **Energy Slot Reservation & Booking Engine**:
   - Create new energy slot reservations with station selection, dynamic calendar picker, and kWh input.
   - Enforces business logic: bookings strictly within 7-day future window.
   - Reschedule/update and cancel reservations with automated 12-hour advance notice verification.
   - Rich interactive confirmation summaries (`BookingSummaryActivity`).

4. **Dashboard, Real-Time History & Search**:
   - Executive prosumer dashboard with live pending and approved reservation counts.
   - Current active bookings list and historical completed/cancelled archives.
   - Dynamic multi-parameter search (filter by text keyword, date, status, and grid node).

5. **Cryptographic QR Code Transaction Workflow**:
   - **Prosumer**: Instant generation of secure ZXing QR codes embedding booking token, NIC, and timestamp for approved slots.
   - **Operator**: High-performance camera QR scanner with auto-focus and flashlight controls.
   - Server-side verification and one-tap finalization of energy dispatch transactions (`VerifyTransactionActivity`).

6. **Google Maps Interactive Station Locator**:
   - Full-screen `MapView` with customized station markers color-coded by slot availability.
   - Real-time station search and filtering (All Stations, Available Slots, High Capacity $\ge$ 200 kWh).
   - Interactive bottom station drawer displaying available battery slots, total kWh capacity, and direct "Book Energy Slot Here" action.
   - GPS user location tracking with permission handling.

7. **Offline-First SQLite Caching & Reliability**:
   - Local database layer (`DatabaseHelper`, `UserDao`, `BookingCacheDao`, `NodeCacheDao`).
   - Seamless offline caching allowing instant app launch and graceful fallback when offline.

---

## 🏗️ Architecture & Technology Stack

| Layer | Technologies / Libraries |
|-------|--------------------------|
| **Platform** | Android 7.0+ (API 24 to 34), Java 17 |
| **UI & Theming** | Material Design 3 (MaterialComponents), Custom XML layouts, Dynamic Dark Mode |
| **Networking** | Retrofit 2.11.0, OkHttp 4.12.0 with Logging & Auth Interceptors |
| **Local Persistence** | SQLite (`SQLiteOpenHelper`), SharedPreferences (`SessionManager`) |
| **Mapping & Location** | Google Maps SDK (`play-services-maps:19.0.0`), Google Location Services (`play-services-location:21.3.0`) |
| **QR Code Engine** | ZXing Core 3.5.3, ZXing Android Embedded 4.3.0 |
| **Image Loading** | Bumptech Glide 4.16.0 |
| **JSON Serialization** | Google Gson 2.11.0 |

---

## 📋 Project Directory Structure

```
smart-solar-microgrid-trading-system/
├── README.md
├── a.md
├── implementation_plan.md
└── android/
    └── SmartSolarApp/
        ├── build.gradle.kts
        ├── settings.gradle.kts
        ├── gradle.properties
        ├── gradlew
        ├── gradlew.bat
        ├── local.defaults.properties
        ├── README.md
        └── app/
            ├── build.gradle.kts
            ├── src/main/
                ├── AndroidManifest.xml
                ├── java/com/smartsolar/app/
                │   ├── SmartSolarApplication.java
                │   ├── account/          # Profile, EditProfile, Deactivate
                │   ├── api/              # Retrofit ApiClient, ApiService, DTO Models
                │   ├── auth/             # Login, Register, SessionManager
                │   ├── booking/          # Create, Update, Cancel, List, History, Search
                │   ├── dashboard/        # Dashboard stats and prosumer home
                │   ├── db/               # SQLite DatabaseHelper & DAOs
                │   ├── map/              # Google Maps station finder & markers
                │   ├── operator/         # Operator console & QR verification
                │   ├── qr/               # QR generator and camera scanner
                │   └── utils/            # Constants, Network, DateTime, Validation
                └── res/
                    ├── layout/           # 19 XML UI Layouts
                    ├── values/           # Colors, Themes, Strings, Dimens
                    ├── drawable/         # Custom button, card, and gradient drawables
                    └── menu/             # Bottom navigation menus
```

---

## 🚀 Setup & Execution Guide

### 1. Prerequisites
- **Android Studio** (Hedgehog, Iguana, Jellyfish, Koala or newer).
- **JDK 17** (configured in Android Studio under *Settings → Build, Execution, Deployment → Build Tools → Gradle*).
- **Android SDK API 34** and build tools.
- (Optional) Google Maps API Key for live map tiles.

---

### 2. Configuration

#### A. Backend API Endpoint Configuration
Configure the base URL in [`Constants.java`](file:///f:/smart-solar-microgrid-trading-system/android/SmartSolarApp/app/src/main/java/com/smartsolar/app/utils/Constants.java):

```java
// For Android Emulator (maps to localhost on host machine):
public static final String BASE_URL = "http://10.0.2.2:5000/api/";

// For Physical Android Device (replace with your machine's LAN IP):
// public static final String BASE_URL = "http://192.168.1.100:5000/api/";
```

#### B. Google Maps API Key (Optional)
Add your key to `android/SmartSolarApp/local.properties`:
```properties
MAPS_API_KEY=YOUR_GOOGLE_MAPS_API_KEY_HERE
```
*(If no key is provided, the application will use the mock fallback coordinates for local development).*

---

### 3. Build & Run via Android Studio

1. Open Android Studio.
2. Select **Open** and browse to:
   ```
   <workspace_root>/android/SmartSolarApp
   ```
3. Allow Gradle to sync dependencies automatically.
4. Select an Android Virtual Device (AVD) or connected physical device.
5. Click **Run ▶** (or press `Shift + F10`).

---

### 4. Build via Command Line (CLI)

Navigate to the Android app directory:
```bash
cd android/SmartSolarApp
```

**On Windows (PowerShell / Command Prompt):**
```cmd
.\gradlew.bat assembleDebug
```

**On macOS / Linux:**
```bash
./gradlew assembleDebug
```

The compiled APK will be generated at:
```
android/SmartSolarApp/app/build/outputs/apk/debug/app-debug.apk
```

---

## 🔑 Demo & Test Credentials

| Role | NIC (Username) | Password | Target Screen |
|------|----------------|----------|---------------|
| **Prosumer** | `199012345678` | `Pass@123` | `DashboardActivity` |
| **Grid Operator** | `198598765432` | `Operator@123` | `OperatorHomeActivity` |

*New prosumers can also register directly via the **Create Account** button on the login screen.*

---

## 📊 Marking Rubric Coverage (Member 2 — ~38+ Marks)

| Part | Feature Module | Rubric Scope | Covered Marks |
|:---:|----------------|--------------|:-------------:|
| **Part 1–2** | Gradle & Theming | Project scaffolding, AndroidX, Material 3, Manifest | Scaffold |
| **Part 3–4** | API Layer & SQLite | Retrofit/OkHttp, SQLite offline caching, Session management | **3 Marks** (Persistence) |
| **Part 5** | Authentication | NIC validation, Login with role-based routing, Register | **5 Marks** (Auth) |
| **Part 6** | Account Management | Profile view, Profile update, Deactivation workflow | **2 Marks** (Account) |
| **Part 7** | Reservation Engine | Create, Update (12h notice), Cancel, Dynamic Summaries | **9 Marks** (Reservations) |
| **Part 8** | Dashboard & History | Current/pending list, History, Search filter, Stats counts | **10 Marks** (Dashboard/Views) |
| **Part 9** | QR Code Workflow | Prosumer QR generation, Operator scanner, Verification | **4 Marks** (QR System) |
| **Part 10**| Maps & Operator Mode | Google Maps station markers, Search/filter, Operator Home | **5+ Marks** (Maps & Operator) |
| **Integration**| API Integration Quality | Resilient error handling, Clear error messages, Interceptors | **Full Integration** |

---

## 📹 Video Demonstration Link

- **Video Walkthrough URL**: *[Insert YouTube / Google Drive Demo Video Link Here]*
