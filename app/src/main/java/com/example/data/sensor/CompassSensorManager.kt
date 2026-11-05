package com.example.data.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

data class CompassState(
    val azimuthDegrees: Float = 0f, // 0 = North, 90 = East, 180 = South, 270 = West
    val qiblaBearingDegrees: Float = 136f, // Bearing to Kaaba from Egypt (typically ~135° to 137°)
    val qiblaRelativeAngle: Float = 0f, // Angle relative to current phone heading
    val cardinalDirectionAr: String = "شمال"
)

class CompassSensorManager(context: Context) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
    private val rotationVectorSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)
    private val accelerometerSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val magneticSensor = sensorManager?.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)

    private val _compassState = MutableStateFlow(CompassState())
    val compassState: StateFlow<CompassState> = _compassState.asStateFlow()

    private val rotationMatrix = FloatArray(9)
    private val orientationAngles = FloatArray(3)
    private val lastAccelerometer = FloatArray(3)
    private val lastMagnetometer = FloatArray(3)
    private var lastAccelerometerSet = false
    private var lastMagnetometerSet = false

    // Coordinates of Holy Kaaba in Mecca
    private val kaabaLat = 21.4225
    private val kaabaLon = 39.8262

    // Default reference for Egypt (Cairo)
    private var userLat = 30.0444
    private var userLon = 31.2357

    fun updateLocation(lat: Double, lon: Double) {
        userLat = lat
        userLon = lon
        calculateQibla()
    }

    fun start() {
        if (rotationVectorSensor != null) {
            sensorManager?.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_UI)
        } else {
            accelerometerSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
            magneticSensor?.let { sensorManager?.registerListener(this, it, SensorManager.SENSOR_DELAY_UI) }
        }
    }

    fun stop() {
        sensorManager?.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event == null) return

        var azimuth = 0f

        if (event.sensor.type == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
            SensorManager.getOrientation(rotationMatrix, orientationAngles)
            azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
        } else if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, lastAccelerometer, 0, event.values.size)
            lastAccelerometerSet = true
        } else if (event.sensor.type == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, lastMagnetometer, 0, event.values.size)
            lastMagnetometerSet = true
        }

        if (event.sensor.type != Sensor.TYPE_ROTATION_VECTOR && lastAccelerometerSet && lastMagnetometerSet) {
            if (SensorManager.getRotationMatrix(rotationMatrix, null, lastAccelerometer, lastMagnetometer)) {
                SensorManager.getOrientation(rotationMatrix, orientationAngles)
                azimuth = Math.toDegrees(orientationAngles[0].toDouble()).toFloat()
            }
        }

        // Normalize azimuth to 0..360
        val normalizedAzimuth = ((azimuth % 360) + 360) % 360
        val qiblaBearing = calculateQiblaBearing(userLat, userLon)
        val qiblaRel = ((qiblaBearing - normalizedAzimuth + 360) % 360)

        _compassState.value = CompassState(
            azimuthDegrees = normalizedAzimuth,
            qiblaBearingDegrees = qiblaBearing,
            qiblaRelativeAngle = qiblaRel,
            cardinalDirectionAr = getCardinalDirection(normalizedAzimuth)
        )
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    private fun calculateQibla() {
        val qiblaBearing = calculateQiblaBearing(userLat, userLon)
        val qiblaRel = ((qiblaBearing - _compassState.value.azimuthDegrees + 360) % 360)
        _compassState.value = _compassState.value.copy(
            qiblaBearingDegrees = qiblaBearing,
            qiblaRelativeAngle = qiblaRel
        )
    }

    /**
     * Calculates the Qibla (direction towards Kaaba in Mecca) from given coordinates
     */
    private fun calculateQiblaBearing(lat: Double, lon: Double): Float {
        val phiK = Math.toRadians(kaabaLat)
        val lambdaK = Math.toRadians(kaabaLon)
        val phi = Math.toRadians(lat)
        val lambda = Math.toRadians(lon)

        val deltaLambda = lambdaK - lambda
        val y = sin(deltaLambda) * cos(phiK)
        val x = cos(phi) * sin(phiK) - sin(phi) * cos(phiK) * cos(deltaLambda)
        val bearingRad = atan2(y, x)
        val bearingDeg = Math.toDegrees(bearingRad)
        return (((bearingDeg % 360) + 360) % 360).toFloat()
    }

    private fun getCardinalDirection(azimuth: Float): String {
        return when (azimuth) {
            in 337.5..360.0, in 0.0..22.5 -> "شمال (N)"
            in 22.5..67.5 -> "شمال شرقي (NE)"
            in 67.5..112.5 -> "شرق (E)"
            in 112.5..157.5 -> "جنوب شرقي (SE) • اتجاه القبلة في مصر"
            in 157.5..202.5 -> "جنوب (S)"
            in 202.5..247.5 -> "جنوب غربي (SW)"
            in 247.5..292.5 -> "غرب (W)"
            in 292.5..337.5 -> "شمال غربي (NW)"
            else -> "شمال"
        }
    }
}
