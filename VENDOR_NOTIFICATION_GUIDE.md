# Vendor FCM Notification Implementation Guide

## 🎯 Goal
Send FCM notifications to vendors when:
1. User clicks "Find Vendors" button
2. `POST v1/user/auth/booking/start` API is called

---

## ✅ **RECOMMENDED: Backend Handles FCM** 

**You DON'T need vendor app code for this approach!**

### How It Works:
When your backend receives `POST v1/user/auth/booking/start`:

```php
// Backend pseudo-code (Laravel/PHP example)
public function startBooking(Request $request) {
    // 1. Create booking
    $booking = Booking::create($request->all());
    
    // 2. Find eligible vendors (by location, service type, etc.)
    $vendors = Vendor::findEligibleVendors($booking);
    
    // 3. Send FCM to each vendor
    foreach ($vendors as $vendor) {
        if ($vendor->fcm_token) {
            $this->sendFcmNotification(
                $vendor->fcm_token,
                [
                    'title' => 'New Booking Request',
                    'body' => "A customer needs your service. Amount: ₹{$booking->amount}",
                    'type' => 'BookingRequest',
                    'bookingId' => (string)$booking->id,
                    'customerName' => $booking->customer_name,
                    'serviceType' => $booking->service_type,
                    'amount' => (string)$booking->amount,
                    'dateTime' => $booking->date_time,
                    'location' => $booking->location
                ]
            );
        }
    }
    
    return response()->json(['success' => true, 'data' => $booking]);
}
```

### FCM Payload Format:
```json
{
  "data": {
    "type": "BookingRequest",
    "title": "New Booking Request",
    "body": "A customer needs your service. Amount: ₹1500",
    "bookingId": "123",
    "customerName": "John Doe",
    "serviceType": "doorstep",
    "amount": "1500",
    "dateTime": "2024-11-12 14:30:00",
    "location": "New Delhi"
  }
}
```

### Backend FCM Implementation:
Use Firebase Admin SDK to send notifications:

```php
// PHP/Laravel example with firebase/php-jwt
use Kreait\Firebase\Factory;
use Kreait\Firebase\Messaging\CloudMessage;
use Kreait\Firebase\Messaging\Notification;

$factory = (new Factory)->withServiceAccount('/path/to/service-account.json');
$messaging = $factory->createMessaging();

$message = CloudMessage::withTarget('token', $vendorFcmToken)
    ->withNotification(Notification::create(
        'New Booking Request',
        'A customer needs your service'
    ))
    ->withData([
        'type' => 'BookingRequest',
        'bookingId' => '123',
        'title' => 'New Booking Request',
        'body' => 'A customer needs your service'
    ]);

$messaging->send($message);
```

**✅ User App Changes:** NONE! Backend handles everything.

---

## 🔧 **ALTERNATIVE: Client-Side Trigger** 

If your backend doesn't send FCM automatically, you can trigger it from the client.

### Step 1: Backend Needs This Endpoint
```
POST /v1/user/auth/booking/notify-vendors
Headers: Authorization: Bearer <token>
Body: {
  "booking_id": 123,
  "service_type": "doorstep",
  "amount": 1500,
  "date_time": "2024-11-12 14:30:00"
}
```

### Step 2: I've Added Client-Side Code
✅ Created: `VendorNotificationApiService.kt`
✅ Ready to integrate into `PostBookingViewModel`

### Step 3: Integration (if needed)

Update `PostBookingViewModel.kt` to call vendor notification after successful booking:

```kotlin
fun startBooking() {
    // ... existing code ...
    viewModelScope.launch {
        val result = bookingRepository.startBooking(request)
        
        if (result is Resource.Success) {
            // Optional: Trigger vendor notification
            // bookingRepository.notifyVendors(bookingId, request)
        }
        
        _bookingStartState.value = result
    }
}
```

**Note:** This approach is less ideal. Backend should handle it automatically.

---

## 📱 **Vendor App Requirements**

**You don't need to share vendor app code!** Here's what the vendor app needs:

### 1. FCM Setup (Same as User App)
- Firebase Cloud Messaging configured
- `FirebaseMessagingService` class
- Notification channel created
- FCM token registration

### 2. Handle `BookingRequest` Notification

In vendor app's `FirebaseMessagingService`:

```kotlin
override fun onMessageReceived(remoteMessage: RemoteMessage) {
    val notificationType = remoteMessage.data["type"]
    val bookingId = remoteMessage.data["bookingId"]
    
    when (notificationType) {
        "BookingRequest" -> {
            // Show notification
            sendNotification(
                title = remoteMessage.data["title"],
                body = remoteMessage.data["body"],
                bookingId = bookingId
            )
            
            // Optional: Update UI if booking screen is open
            sendBroadcast(Intent("VENDOR_BOOKING_UPDATE").apply {
                putExtra("bookingId", bookingId)
                putExtra("type", "BookingRequest")
            })
        }
    }
}
```

### 3. Deep Linking (Optional)
When vendor taps notification, navigate to booking details screen:

```kotlin
val intent = Intent(this, BookingDetailsActivity::class.java).apply {
    putExtra("bookingId", bookingId)
}
```

---

## 🔍 **Which Approach to Use?**

### ✅ Use Backend Approach If:
- You control the backend
- Backend can access vendor FCM tokens
- You want reliable, centralized notification logic

### ⚠️ Use Client-Side Approach If:
- Backend doesn't support FCM yet
- You need a temporary workaround
- You want to trigger notifications immediately after booking

---

## 📝 **Summary**

1. **Best Practice:** Backend sends FCM when `startBooking` API is called
2. **No User App Changes Needed:** If backend is configured
3. **Vendor App Needs:** FCM service to receive `BookingRequest` notifications
4. **You Don't Need Vendor Code:** Just implement FCM receiver similar to user app

---

## 🚀 **Next Steps**

1. ✅ Check if backend already sends FCM on `startBooking`
2. ✅ If not, configure backend to send FCM (recommended)
3. ✅ OR use client-side trigger (I've prepared the code)
4. ✅ Vendor app should handle `BookingRequest` notifications

---

## 💡 **Questions?**

- **Q: Do I need vendor app code?**  
  A: No! Just implement FCM receiver similar to user app.

- **Q: Should backend or client send FCM?**  
  A: Backend! More secure and reliable.

- **Q: When to trigger notification?**  
  A: Right after booking is created in `startBooking` API.


