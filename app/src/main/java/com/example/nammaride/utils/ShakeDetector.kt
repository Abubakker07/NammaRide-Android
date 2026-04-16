package com.example.nammaride.utils

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlin.math.sqrt

class ShakeDetector(
    context: Context,
    private val onShake: () -> Unit
) : SensorEventListener {

    private var sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private var accelerometer: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    private var lastShakeTime: Long = 0
    // The force required to trigger the shake. 2.7 is a good balance (not too sensitive)
    private val shakeThresholdGravity = 2.7f
    // Minimum time (in milliseconds) between allowed shakes to prevent spam
    private val shakeSlopTimeMs = 1000

    fun startListening() {
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    fun stopListening() {
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event != null) {
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Normalize the values to Earth's gravity
            val gX = x / SensorManager.GRAVITY_EARTH
            val gY = y / SensorManager.GRAVITY_EARTH
            val gZ = z / SensorManager.GRAVITY_EARTH

            // Calculate the total G-force applied to the phone
            val gForce = sqrt((gX * gX + gY * gY + gZ * gZ).toDouble()).toFloat()

            if (gForce > shakeThresholdGravity) {
                val now = System.currentTimeMillis()
                // Only trigger if enough time has passed since the last shake
                if (now - lastShakeTime > shakeSlopTimeMs) {
                    lastShakeTime = now
                    onShake() // Trigger the SOS action!
                }
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Not needed for simple shake detection
    }
}