package de.mamakow.dienstplanapotheke.model

import com.google.gson.annotations.SerializedName

data class UserData(
    @SerializedName("id")
    val id: Int,
    @SerializedName("employee_key")
    val employeeKey: Int?,
    @SerializedName("user_name")
    val userName: String,
    @SerializedName("email")
    val email: String?,
    @SerializedName("privileges")
    val privileges: List<String>?
)
