package com.example.carzorrouserside.data.model



import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class BookingData(
    val vendorId: Int,
    val serviceType: String = "Doorstep service",
    val carType: String = "Audi",
    val serviceName: String = "Full Car Wash Service",
    val serviceDescription: String = "A combination of dry wash, Vacuum Cleaning, AC Cleaning...",
    val rating: Float = 4.7f,
    val duration: String = "35mins",
    val originalPrice: Double = 5200.0,
    val discountedPrice: Double = 3456.0,
    val discountPercentage: Int = 50,
    val dateTime: String = "12 Nov 2024 At 11:30 AM",
    val vendorName: String = "James Williams",
    val vendorRating: Float = 4.8f,
    val vendorReviews: Int = 45,
    val vendorDistance: String = "6.5Km",
    val vendorLocation: String = "New Delhi",
    val customerName: String = "James Williams",
    val customerAddress: String = "Rohini Nagar, New Delhi",
    val customerPhone: String = "+91 9865890032",
    val addOnServices: List<AddOnService> = emptyList(),
    val priceDetails: PriceDetails = PriceDetails(),
    val totalAmount: Double = 255.12,
    val savings: Double = 2000.0
) : Parcelable

@Parcelize
data class AddOnService(
    val name: String,
    val price: Double,
    val isAdded: Boolean = false
) : Parcelable

@Parcelize
data class PriceDetails(
    val basePrice: Double = 120.0,
    val discount: Double = -15.12,
    val taxes: Double = 15.12,
    val platformFee: Double = 100.0,
    val couponDiscount: Double = -10.0
) : Parcelable
data class BookingStartRequest(
    @SerializedName("service_type") val serviceType: String = "",
    @SerializedName("feature_service_id") val featureServiceId: Int? = null,
    @SerializedName("date_time") val dateTime: String? = null,
    @SerializedName("amount") val amount: Int? = null,
    @SerializedName("user_address_id") val userAddressId: Int? = null,
    @SerializedName("vehicle_id") val vehicleId: Int? = null,
    @SerializedName("water_available_50_m") val waterAvailable50m: Int = 0,
    @SerializedName("electricity_available_50_m") val electricityAvailable50m: Int = 0
)
