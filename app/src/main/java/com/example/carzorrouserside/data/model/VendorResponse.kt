package com.example.carzorrouserside.data.model

import com.example.carzorrouserside.data.model.homescreen.Bid

data class VendorResponse(
    val id: Int,
    val name: String,
    val rating: Double,
    val reviews: Int,
    val serviceType: String,
    val price: Int,
    val distanceInMin: Int,
    val distanceInMeters: Int
) {
    companion object {
        fun fromBid(bid: Bid): VendorResponse {
            return VendorResponse(
                id = bid.vendorId, // using vendorId instead of bid.id
                name = bid.vendorName,
                rating = bid.vendorRating.toDouble(), // convert Int → Double
                reviews = bid.vendorReviews.toInt(),  // convert Double → Int
                serviceType = "Car Wash", // fallback since Bid doesn’t have this field
                price = bid.newAmount.toInt(), // Double → Int
                distanceInMin = 0, // not in Bid
                distanceInMeters = 0 // not in Bid
            )
        }
    }
}


