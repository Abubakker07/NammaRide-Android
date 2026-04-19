<div align="center">

# 🛺 NammaRide
### Full-Stack Ride Simulator

![Kotlin](https://img.shields.io/badge/Kotlin-B125EA?style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack_Compose-4285F4?style=for-the-badge&logo=android&logoColor=white)
![PHP](https://img.shields.io/badge/PHP-777BB4?style=for-the-badge&logo=php&logoColor=white)
![MySQL](https://img.shields.io/badge/MySQL-005C84?style=for-the-badge&logo=mysql&logoColor=white)
![AlwaysData](https://img.shields.io/badge/AlwaysData-FF0055?style=for-the-badge&logo=linux&logoColor=white)
![Railway](https://img.shields.io/badge/Railway-0B0D0E?style=for-the-badge&logo=railway&logoColor=white)

**NammaRide** is a native Android ride-hailing simulator engineered to tackle real-world transit challenges across major transit hubs in Bangalore.

[Features](#-key-features) • [Architecture](#️-system-architecture--tech-stack) • [Gallery](#-app-gallery) • [Installation](#-local-development-setup) • [🔗 Backend Repository](https://github.com/Abubakker07/nammaride-backend)

</div>

---

## 🚀 Key Features

### 🔐 AI Smart QR Fare Guard
Leverages on-device **Google ML Kit (Vision API)** to scan driver UPI QR codes. It instantly parses payment intents and cross-verifies the requested amount against the backend's live database fare, blocking localized overcharging scams.

### ⚡ Dynamic Surge Pricing & Algorithm Sorting
Built for performance and real-world simulation:
* **Time-Aware Surge:** Dynamically applies localized surge multipliers (e.g., 1.25x during peak Bangalore traffic hours).
* **Tax Integration:** Automatically factors in legally mandated 5% aggregator GST.
* **Fast Sorting:** Utilizes a backend **O(n log n) QuickSort** algorithm to rank and return ride options in milliseconds.

### 📍 Intelligent Geofencing & Routing
* **Route Constraints:** Backend logic restricts specific vehicle types (e.g., auto-rickshaws and bikes) from being booked for restricted routes like Airport drop-offs.
* **OSRM Map Integration:** Dynamically draws accurate, real-world road polylines using the Open Source Routing Machine, calculating precise distance (km) and estimated time (min).

### 🚨 Hardware-Linked Emergency SOS
Prioritizes user safety by monitoring the Android device's hardware accelerometer. If severe, abnormal shaking is detected during a ride, it triggers an instant emergency SMS to a trusted contact or automatically dials **112**.

---

## 📸 App Gallery

<div align="center">
  <img src="ss/screen1.png" width="19%" alt="Home Dashboard" />
  <br>
  <img src="ss/screen2.png" width="19%" alt="OSRM Route Map" />
  <img src="ss/screen3.png" width="19%" alt="Fare Breakdown" />
  <img src="ss/screen4.png" width="19%" alt="AI QR Scam Alert" />
</div>

---

## 🛠️ System Architecture & Tech Stack

### 📱 Frontend (Android)
* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Declarative UI)
* **Architecture:** MVVM (Model-View-ViewModel)
* **Networking:** Retrofit2 + Gson
* **Maps & Routing:** Osmdroid + OSRM API
* **Machine Learning:** Google Play Services ML Kit

### 🌐 Backend (REST API)
* **Repository:** [Abubakker07/nammaride-backend](https://github.com/Abubakker07/nammaride-backend)
* **Language:** PHP 8.x
* **Database:** MySQL (Relational Data Modeling)
* **Primary Deployment:** AlwaysData (Permanent Cloud Host)
* **Failover Deployment:** Railway.app (TCP Proxy)
* **Database Management:** MySQL CLI / TablePlus

---

## ⚙️ How It Works (The Data Flow)

1.  **Request:** The Android client dispatches a location query to the PHP REST API via Retrofit.
2.  **Compute:** The backend calculates the base distance, evaluates vehicle availability, applies peak-hour surge multipliers, and adds GST.
3.  **Sort:** The custom QuickSort engine organizes the final vehicle lineup by price.
4.  **Response:** A sanitized, structured JSON array is returned to the client.
5.  **Render:** Jetpack Compose asynchronously updates the UI state, rendering the map polyline and the transparent fare breakdown.

---

## 💻 Local Development Setup

### 1️⃣ Clone Repository
```bash
git clone [https://github.com/Abubakker07/NammaRide-Android.git](https://github.com/Abubakker07/NammaRide-Android.git)
cd NammaRide-Android
```
### 2️⃣ Database Setup
* Clone the [backend repository](https://github.com/Abubakker07/nammaride-backend).
* Import the `nammaride_backup.sql` file into your local MySQL server (via XAMPP/MAMP or MySQL CLI).
* Update the environment credentials in `backend/db.php`.

### 3️⃣ Android Setup
* Open the project in **Android Studio**.
* Navigate to `com.example.nammaride.network.NammaApi.kt`.
* Uncomment the local IP configuration in your environment switcher:
  ```kotlin
  // Uncomment for Local XAMPP Testing:
  private const val BASE_URL = "[http://192.168.1.](http://192.168.1.)X/"
  ```

### 4️⃣ Run App
* Connect a physical Android device (Recommended for GPS & Camera ML performance) or launch an emulator.
* Sync Gradle and click Run.
* Note: Ensure your backend server (XAMPP/Apache) and MySQL service are actively running before launching the app to prevent BEGIN_ARRAY Retrofit crash errors.
