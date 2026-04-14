package com.example.myapplication

import android.graphics.PointF
import java.util.UUID

data class Pin(val name: String, val type: String = "GPIO")

data class Device(
    val name: String,
    val pins: List<Pin>,
    val type: DeviceType = DeviceType.MODULE,
    val preferredWidth: Float = 200f,
    val preferredHeight: Float = 300f
)

enum class DeviceType {
    BOARD, MODULE, POWER, ACTUATOR, SENSOR
}

data class Connection(
    val id: String = UUID.randomUUID().toString(),
    val fromDeviceId: String,
    val fromPinName: String,
    val toDeviceId: String,
    val toPinName: String,
    val path: MutableList<PointF> = mutableListOf(),
    var color: Int? = null
)
