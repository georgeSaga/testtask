package com.example.myapplication.utils.data

import com.google.gson.annotations.SerializedName

data class Result (
    @SerializedName("country") val country: String,
    @SerializedName("startingDate") val startingDate: String,
    @SerializedName("emails") val emails: List<String>
)