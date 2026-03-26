package com.example.carzorrouserside.ui.theme.navigation

import java.net.URLEncoder


object Routes {
    const val SPLASH_SCREEN = "splash_screen"
    const val SPLASH_SCREEN_1 = "splash_screen_1"
    const val WELCOME_SCREEN = "welcome_screen"
    const val LOGIN_SCREEN = "login_screen"
    const val SIGN_UP_SCREEN = "sign_up_screen"
    const val TERMS_AND_CONDITIONS_SCREEN = "terms_and_conditions_screen"
    const val VERIFY_LOGIN_OTP_SCREEN = "verify_login_otp_screen"
    const val VERIFY_SIGNUP_OTP_SCREEN = "verify_signup_otp_screen"

    const val VERIFY_OTP_SCREEN = "verify_otp_screen"
    const val ADDRESS_LISTING_SCREEN = "address_listing_screen"
    const val HOME_SCREEN = "home_screen"
    const val PACKAGE_SCREEN = "package_screen"
    const val EDIT_CAR_SCREEN = "edit_car_screen"
    const val PACKAGE_DETAIL_SCREEN = "package_detail_screen"
    const val ADD_CAR_SCREEN = "add_car_screen"
    const val BOOKING_SCREEN = "booking_screen"
    const val BOOKING_SUMMARY_SCREEN = "booking_summary_screen"
    const val BOOKING_CONFIRMATION_SCREEN = "booking_confirmation_screen"
    const val BOOKING_HISTORY_SCREEN = "booking_history_screen"
    const val ORDER_DETAIL_SCREEN = "order_detail_screen"
    const val FAVOURITES_SCREEN = "favourites_screen"
    const val VENDOR_DETAIL_SCREEN = "vendor_detail_screen"
    const val PROFILE_SCREEN = "profile_screen"
    const val UPDATE_PROFILE_SCREEN = "update_profile_screen"
    const val COINS_SCREEN = "coins_screen"
    const val PRIVACY_SCREEN ="privacy_screen"
    const val HELP_SUPPORT_SCREEN ="Help_support_screen"

    const val NOTIFICATION_SCREEN = "notification_screen"

    const val ALL_PRODUCTS_SCREEN = "all_products_screen"
    const val PRODUCT_DETAIL_SCREEN = "product_detail_screen"
    fun productDetailScreen(productId: Int): String {
        return "$PRODUCT_DETAIL_SCREEN/$productId"
    }
    fun bookingSummaryScreen(vendorId: Int): String {
        return "$BOOKING_SUMMARY_SCREEN/$vendorId"
    }
    fun bookingSummaryScreenWithBookingId(bookingId: Int): String {
        return "$BOOKING_SUMMARY_SCREEN/booking/$bookingId"
    }
    fun bookingConfirmationScreen(paymentAmount: Double, paymentMethod: String): String {
        val encodedMethod = URLEncoder.encode(paymentMethod, "UTF-8")
        return "$BOOKING_CONFIRMATION_SCREEN?amount=$paymentAmount&method=$encodedMethod"
    }
    fun orderDetailScreen(orderId: Int): String {
        return "$ORDER_DETAIL_SCREEN/$orderId"
    }
    fun loginOtpScreen(phoneNumber: String, serverOtp: String? = null): String {
        return if (serverOtp != null && serverOtp.isNotEmpty()) {
            "$VERIFY_LOGIN_OTP_SCREEN/$phoneNumber/$serverOtp"
        } else {
            "$VERIFY_LOGIN_OTP_SCREEN/$phoneNumber"
        }
    }
    fun vendorDetailScreen(vendorId: Int): String {
        return "$VENDOR_DETAIL_SCREEN/$vendorId"
    }
    fun packageDetailScreen(packageId: Int): String {
        return "$PACKAGE_DETAIL_SCREEN/$packageId"
    }
    fun signupOtpScreen(
        phoneNumber: String,
        userId: Int,
        initialOtp: String,
        fullName: String,
        email: String,
        dob: String,
        gender: String
    ): String {
        val encodedFullName = java.net.URLEncoder.encode(fullName, "UTF-8")
        val encodedEmail = java.net.URLEncoder.encode(email, "UTF-8")
        val encodedDob = java.net.URLEncoder.encode(dob, "UTF-8")
        val encodedGender = java.net.URLEncoder.encode(gender, "UTF-8")

        return "$VERIFY_SIGNUP_OTP_SCREEN/$phoneNumber/$userId/$initialOtp/$encodedFullName/$encodedEmail/$encodedDob/$encodedGender"
    }

    fun verifyOtpScreen(phoneNumber: String, serverOtp: String? = null): String {
        return if (serverOtp != null) {
            "$VERIFY_OTP_SCREEN/$phoneNumber?serverOtp=$serverOtp"
        } else {
            "$VERIFY_OTP_SCREEN/$phoneNumber"
        }
    }
    const val BRAND_SELECTION_SCREEN = "brand_selection_screen"
    const val MODEL_SELECTION_SCREEN = "model_selection_screen"
    const val ADD_CAR_DETAIL_SCREEN = "add_car_detail_screen"
    const val CAR_DETAILS_SCREEN = "car_details_screen"


    fun modelSelectionRoute(brandId: Int, brandName: String): String {
        return "model_selection/$brandId/${java.net.URLEncoder.encode(brandName, "UTF-8")}"
    }

    fun addCarDetailRoute(
        brandId: Int,
        brandName: String,
        modelId: Int,
        modelName: String
    ): String {
        return "add_car_detail/$brandId/${java.net.URLEncoder.encode(brandName, "UTF-8")}/$modelId/${java.net.URLEncoder.encode(modelName, "UTF-8")}"
    }

    fun carDetailsRoute(
        brandName: String,
        modelName: String,
        imageUrl: String?
    ): String {
        val encodedBrand = URLEncoder.encode(brandName, "UTF-8")
        val encodedModel = URLEncoder.encode(modelName, "UTF-8")
        val encodedImageUrl = URLEncoder.encode(imageUrl ?: "no_image", "UTF-8")
        return "$CAR_DETAILS_SCREEN/$encodedBrand/$encodedModel/$encodedImageUrl"
    }
    fun bookingScreenWithSheet(
        sheet: String,
        isSending: Boolean = false
    ): String {
        return "$BOOKING_SCREEN?sheet=$sheet&isSending=$isSending"
    }

}

