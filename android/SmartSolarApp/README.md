# Smart Solar Microgrid Trading System — Native Android Application

> **Member 2 Component:** Native Android Mobile Application for Solar Prosumers and Grid Operators.

---

## 🚀 How to Run in Android Studio

### 1. Open Project in Android Studio
1. Launch **Android Studio** (Hedgehog, Iguana, Jellyfish, or newer).
2. Click **Open** (or `File` → `Open...`).
3. Browse to and select the project directory:
   ```
   android/SmartSolarApp
   ```
4. Click **OK**.

---

### 2. Gradle Sync & Build
1. Android Studio will automatically trigger **Gradle Sync**.
2. If prompted, allow Android Studio to download dependencies (Retrofit, Material 3, ZXing, Google Maps, OkHttp).
3. If Gradle sync does not start automatically, click `File` → `Sync Project with Gradle Files` (or the Elephant sync icon in the toolbar).

---

### 3. Backend API URL Configuration
The API endpoint is configured in [`Constants.java`](file:///f:/smart-solar-microgrid-trading-system/android/SmartSolarApp/app/src/main/java/com/smartsolar/app/utils/Constants.java):

```java
// For Android Emulator (maps to localhost on PC):
public static final String BASE_URL = "http://10.0.2.2:5000/api/";

// For Physical Android Device over WiFi (use your machine's local IP):
// public static final String BASE_URL = "http://192.168.1.xxx:5000/api/";
```

---

### 4. Run on an Emulator or Device
1. In Android Studio's top toolbar, choose a device from the target selector:
   - **Android Emulator** (e.g., Pixel 7 / Pixel 8 with API 34).
   - **Physical Device** via USB debugging / WiFi.
2. Click the green **Run (▶)** button or press `Shift + F10`.
3. The app will compile, install, and launch the login screen (`LoginActivity`).

---

### 5. Build via CLI
```cmd
.\gradlew.bat assembleDebug
```
Output APK location: `app/build/outputs/apk/debug/app-debug.apk`

---

## 📱 Features & Architecture

- **Role-Based Authentication**:
  - Prosumer login → Routes to `DashboardActivity`
  - Operator login → Routes to `OperatorHomeActivity`
  - New prosumer signup with NIC validation (`RegisterActivity`)
- **Profile & Account Management**:
  - View & Edit profile (`ProfileActivity`, `EditProfileActivity`)
  - Request deactivation (`DeactivateAccountActivity`)
- **Booking & Energy Reservation**:
  - Create booking within 7-day window (`CreateBookingActivity`)
  - Update booking with 12-hour notice check (`UpdateBookingActivity`)
  - Cancel booking with confirmation (`CancelBookingActivity`)
  - Dynamic result summary cards (`BookingSummaryActivity`)
- **Booking Views & History**:
  - Current & pending bookings with status filter chips (`BookingListActivity`)
  - Historical completed/cancelled transactions (`BookingHistoryActivity`)
  - Real-time search by text, status, node, date (`BookingSearchActivity`)
- **QR Code Transactions**:
  - Prosumer transaction QR generation (`QrGeneratorActivity`)
  - Operator camera barcode scanner with flashlight (`QrScannerActivity`)
  - Server transaction verification & finalization (`VerifyTransactionActivity`)
- **Google Maps & Station Hubs**:
  - Interactive microgrid node map with capacity details, live search & filter bar, and direct booking trigger (`NearbyNodesMapActivity`)
- **Operator Console**:
  - Live operator dashboard with pending counts, station status, QR scan shortcut, and manual verification (`OperatorHomeActivity`)
- **Offline First**:
  - SQLite database caching (`UserDao`, `BookingCacheDao`, `NodeCacheDao`) with `SharedPreferences` session management.

---

## 🔑 Test Credentials

| Role | NIC | Password | Target Screen |
|------|-----|----------|---------------|
| Prosumer | `199012345678` | `Pass@123` | Dashboard |
| Operator | `198598765432` | `Operator@123` | Operator Home |

---

## 📹 Video Demonstration Link
- *[Insert Video Demo Link Here]*
