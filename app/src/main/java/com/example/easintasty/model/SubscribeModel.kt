package com.example.easintasty.model

// SubscribeModel.kt
data class SubscribeModel(
    val method: String,
    val amount: Double,
    val status: StatusModel,
    val subscriptionTime: String // New field
)

data class StatusModel(
    val active: Boolean
)
