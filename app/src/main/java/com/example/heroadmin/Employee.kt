package com.example.heroadmin

import kotlinx.serialization.Serializable

@Serializable
data class Employee(
    val Employee_ID: String,
    val Phone_Nr: String?,
    val Mail: String?,
    val Name: String?,
    val Last_Name: String?,
    val Role: String?
)