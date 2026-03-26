package com.example.carzorrouserside.data.model.homescreen

import com.google.gson.annotations.SerializedName

// This is the main data class for a single testimonial item from the API
data class Testimonial(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("image")
    val image: String?, // The image URL from the API

    @SerializedName("date")
    val date: String,

    @SerializedName("review")
    val review: String, // The main review text

    @SerializedName("rating")
    val rating: Double
)

// This class represents the top-level structure of the API response
data class TestimonialResponse(
    @SerializedName("success")
    val success: Boolean,

    @SerializedName("message")
    val message: String,

    @SerializedName("data")
    val data: List<Testimonial>
)