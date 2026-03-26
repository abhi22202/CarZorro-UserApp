# 🚗 CarZorro – User App

CarZorro is a modern Android application that allows users to seamlessly book car services, track their requests, and receive real-time updates.
This app is part of a full-stack ecosystem that includes a vendor app and a Laravel-based backend hosted on AWS.

---

## 📱 Overview

The CarZorro User App is designed with scalability, clean architecture, and modern Android development practices.
It provides a smooth and intuitive experience for users to manage their car service needs.

---

## ✨ Features

### 🔐 Authentication

* Mobile number login (OTP-based authentication)
* Google Sign-In integration
* Secure user session handling

### 🚗 Service Booking

* Browse and select car services
* Book services with ease
* Dynamic data fetched from backend APIs

### 📦 Order Management

* Track current service status
* View past bookings and history
* Real-time updates from backend

### 🔔 Notifications

* Push notifications using Firebase Cloud Messaging (FCM)
* Instant updates on booking status and service progress

---

## 🧠 Tech Stack

### 📱 Android

* **Kotlin**
* **Jetpack Compose**
* **MVVM + MVI Architecture**
* **Coroutines & Flow**
* **Hilt (Dependency Injection)**

### 🔗 Backend

* **Laravel (REST APIs)**
* Hosted on **AWS**

### 🔥 Firebase

* Firebase Authentication (OTP + Google)
* Firebase Cloud Messaging (FCM)

---

## 🏗 Architecture

The app follows a clean and scalable architecture:

* **MVVM + MVI hybrid approach**
* **Repository Pattern**
* Separation of concerns (UI, Domain, Data)
* Reactive state management using Flow

---

## 📂 Project Structure (Simplified)

```
app/
 ├── ui/                
 ├── viewmodel/         
 ├── repository/        
 ├── network/           
 ├── di/                
 └── utils/             
```

---

## ⚙️ Setup Instructions

1. Clone the repository:

   ```
   git clone https://github.com/your-username/CarZorro-UserApp.git
   ```

2. Open in Android Studio

3. Add Firebase configuration:

   * Place `google-services.json` in `/app` folder

4. Configure API base URL

5. Run the app 🚀

---

## 🔐 Security Notes

* Sensitive files like API keys, keystore files, and `.env` are excluded using `.gitignore`
* Do NOT commit:

  * `.jks` files
  * API keys
  * Firebase private configs

---

## 🚀 Future Improvements

* 💳 Payment Integration
* 📍 Real-time service tracking
* ⭐ Ratings & Reviews system
* 📊 Analytics integration

---

## 🤝 Contribution

This project is currently maintained by the author. Contributions and suggestions are welcome!

---

## 👨‍💻 Developer

**Abhishek Singh**

Android Developer | Kotlin | Jetpack Compose

---

## ⭐ Show Your Support

If you like this project, consider giving it a ⭐ on GitHub!
