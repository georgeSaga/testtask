package com.example.myapplication.utils.data

import com.google.gson.annotations.SerializedName

data class Attendee (
    @SerializedName("firstName") val firstName: String,
    @SerializedName("lastName") val lastName: String,
    @SerializedName("email") val email: String,
    @SerializedName("country") val country: String,
    @SerializedName("availableDates") var availableDates: List<String>
)