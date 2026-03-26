# Vendor App FCM - Quick Start Guide ⚡

Since your backend is already configured, you just need to **receive and display** notifications in the vendor app.

---

## 🎯 What You Need

Your vendor app needs to:
1. ✅ Receive FCM messages from backend
2. ✅ Display notifications when `BookingRequest` arrives
3. ✅ (Optional) Open booking details when notification is tapped

---

## 📦 Files to Create/Update

### 1. **VendorFirebaseMessagingService.kt** 
📁 Location: `app/src/main/java/com/yourpackage/services/`
- Handles incoming FCM messages
- Shows notifications
- Template: `vendor-app-quick-setup/VendorFirebaseMessagingService.kt`

### 2. **VendorApplication.kt**
📁 Location: `app/src/main/java/com/yourpackage/`
- Creates notification channel
- Template: `vendor-app-quick-setup/VendorApplication.kt`

### 3. **VendorAppConstants.kt**
📁 Location: `app/src/main/java/com/yourpackage/util/`
- Notification type constants
- Template: `vendor-app-quick-setup/VendorAppConstants.kt`

### 4. **AndroidManifest.xml**
- Add FCM service registration
- Template: `vendor-app-quick-setup/AndroidManifest_Additions.xml`

### 5. **build.gradle.kts**
- Add Firebase dependencies
- Template: `vendor-app-quick-setup/build.gradle_additions.kt`

### 6. **strings.xml**
- Add notification channel ID
```xml
<string name="default_notification_channel_id" translatable="false">vendor_booking_channel</string>
```

---

## 🔧 Quick Implementation Steps

### Step 1: Copy Templates
Copy files from `vendor-app-quick-setup/` folder to your vendor app:
- `VendorFirebaseMessagingService.kt` → `services/`
- `VendorApplication.kt` → root package
- `VendorAppConstants.kt` → `util/`

### Step 2: Update Package Names
Replace `com.yourvendorpackage` with your actual package name in all files.

### Step 3: Update AndroidManifest
Add the service and metadata from `AndroidManifest_Additions.xml`

### Step 4: Add Dependencies
Add Firebase dependencies from `build.gradle_additions.kt`

### Step 5: Create Notification Icon
Create `ic_notification` drawable (or update the reference in service)

---

## 📱 What Happens When

**User clicks "Find Vendors" in user app:**
1. User app calls `POST v1/user/auth/booking/start`
2. Backend processes booking and finds eligible vendors
3. Backend sends FCM to each vendor with `type: "BookingRequest"`
4. **Vendor app receives FCM** → `onMessageReceived()` called
5. **Notification displayed** → Vendor sees "New Booking Request"
6. Vendor taps notification → Opens booking details (if deep linking configured)

---

## 🧪 Testing

1. **Setup:**
   - Install vendor app on device
   - Login as vendor
   - Register FCM token with backend (should happen automatically)

2. **Test:**
   - Open user app
   - Click "Find Vendors"
   - Check vendor app → Should receive notification

3. **Verify:**
   ```bash
   # Check Logcat for FCM logs
   adb logcat | grep "VendorFCMService"
   ```

---

## 🔍 Expected FCM Payload from Backend

Your backend sends this to vendor's FCM token:

```json
{
  "data": {
    "type": "BookingRequest",
    "title": "New Booking Request",
    "body": "A customer needs your service. Amount: ₹1500",
    "bookingId": "123",
    "customerName": "John Doe",
    "serviceType": "doorstep",
    "amount": "1500"
  }
}
```

---

## ✅ Checklist

- [ ] Firebase configured (`google-services.json` in `app/`)
- [ ] `VendorFirebaseMessagingService` created
- [ ] `VendorApplication` created
- [ ] `AndroidManifest.xml` updated
- [ ] Dependencies added to `build.gradle.kts`
- [ ] Notification channel ID in `strings.xml`
- [ ] Notification icon drawable exists
- [ ] Package names updated
- [ ] Test notification received

---

## 🐛 Troubleshooting

### Notifications not showing?
1. Check Logcat: `adb logcat | grep "VendorFCMService"`
2. Verify FCM token registered with backend
3. Check notification permissions in device settings
4. Verify notification channel created (check Logcat on app start)

### Not receiving FCM?
1. Check if backend has correct vendor FCM token
2. Verify backend is sending to correct FCM token
3. Check Firebase console for delivery status

---

## 📚 Full Documentation

See `VENDOR_APP_FCM_IMPLEMENTATION.md` for detailed implementation guide.

---

## 💡 Key Points

- ✅ **Backend is ready** → Just implement receiver in vendor app
- ✅ **Template files provided** → Copy and customize
- ✅ **Similar to user app** → Same FCM pattern
- ✅ **Notification types**: `BookingRequest` and `BookingAccepted`

---

**That's it!** Once you implement these files, vendors will see notifications when users create bookings. 🎉


