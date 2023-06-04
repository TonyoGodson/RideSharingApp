package com.godston.rideshareapp.utils

import com.godston.rideshareapp.DriverInfoModel

object Common {
    fun buildWelcomeMessage(): String {
        return StringBuilder("Welcome, ")
            .append(currentUser?.name)
            .toString()
    }
    val DRIVER_INFO_REFERENCE: String = "DriverInfo"
    val currentUser: DriverInfoModel? = null
    val DRIVERS_LOCATION_REFERENCES: String = "DriversLocation"
}