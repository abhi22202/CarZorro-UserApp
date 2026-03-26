package com.example.carzorrouserside.domain.repository

import com.example.carzorrouserside.data.model.booking.BookingDetailsData
import com.example.carzorrouserside.data.model.booking.BookingListingData
import com.example.carzorrouserside.data.model.booking.CancelBookingRequest
import com.example.carzorrouserside.data.model.booking.CancelReason
import com.example.carzorrouserside.data.model.booking.OfferActionRequest
import com.example.carzorrouserside.data.model.booking.CheckCouponRequest
import com.example.carzorrouserside.data.model.booking.CouponData
import com.example.carzorrouserside.data.model.booking.BookingPaymentRequest
import com.example.carzorrouserside.data.model.booking.BookingPaymentData
import com.example.carzorrouserside.data.model.booking.BookingPaymentConfirmRequest

interface BookingRepository {
    suspend fun getBookingListing(status: String?): Result<BookingListingData>
    suspend fun getBookingCancelReasons(): Result<List<CancelReason>>
    suspend fun cancelBooking(request: CancelBookingRequest): Result<Unit>
    suspend fun acceptBookingOffer(request: OfferActionRequest): Result<Unit>
    suspend fun declineBookingOffer(request: OfferActionRequest): Result<Unit>
    suspend fun getBookingDetails(bookingId: Int): Result<BookingDetailsData>
    suspend fun checkCoupon(request: CheckCouponRequest): Result<CouponData>
    suspend fun makeBookingPayment(request: BookingPaymentRequest): Result<BookingPaymentData>
    suspend fun confirmBookingPayment(request: BookingPaymentConfirmRequest): Result<Unit>
}

