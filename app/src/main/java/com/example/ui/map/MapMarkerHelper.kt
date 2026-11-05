package com.example.ui.map

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import com.example.data.model.ServiceType

object MapMarkerHelper {

    private val cache = mutableMapOf<String, Drawable>()

    /**
     * Generates a modern teardrop map pin with a category symbol.
     */
    fun createTeardropPin(
        context: Context,
        bgColor: Int,
        label: String? = null,
        iconSymbol: String = ""
    ): Drawable {
        val key = "pin_${bgColor}_${label ?: ""}_$iconSymbol"
        cache[key]?.let { return it }

        val width = 84
        val height = 108
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Drop shadow at bottom
        paint.color = 0x44000000
        canvas.drawOval(RectF(18f, height - 20f, width - 18f, height - 4f), paint)

        // Pin body
        val path = Path()
        val centerX = width / 2f
        val radius = 34f
        val centerY = radius + 6f

        path.moveTo(centerX, height - 16f)
        path.quadTo(centerX - 10f, centerY + 24f, centerX - radius, centerY)
        path.arcTo(RectF(centerX - radius, centerY - radius, centerX + radius, centerY + radius), 180f, 180f)
        path.quadTo(centerX + 10f, centerY + 24f, centerX, height - 16f)
        path.close()

        // Outer pin
        paint.color = bgColor
        canvas.drawPath(path, paint)

        // Border stroke
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 3f
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawPath(path, paint)

        // Inner white circle
        paint.style = Paint.Style.FILL
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(centerX, centerY, radius * 0.62f, paint)

        // Center symbol or icon
        paint.color = bgColor
        paint.textAlign = Paint.Align.CENTER
        paint.isFakeBoldText = true

        if (iconSymbol.isNotBlank()) {
            paint.textSize = 28f
            canvas.drawText(iconSymbol, centerX, centerY + 10f, paint)
        } else {
            paint.color = bgColor
            canvas.drawCircle(centerX, centerY, radius * 0.38f, paint)
        }

        val drawable = BitmapDrawable(context.resources, bitmap)
        cache[key] = drawable
        return drawable
    }

    /**
     * User GPS Location dot with pulsing accuracy ring and bearing direction arrow.
     */
    fun createUserLocationMarker(context: Context, bearing: Float = 0f): Drawable {
        val size = 96
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        val center = size / 2f

        // Outer halo ring
        paint.color = 0x331A73E8
        canvas.drawCircle(center, center, center - 6f, paint)

        // Direction arrow if moving
        if (bearing != 0f) {
            val arrowPath = Path()
            arrowPath.moveTo(center, 4f)
            arrowPath.lineTo(center - 12f, center - 16f)
            arrowPath.lineTo(center + 12f, center - 16f)
            arrowPath.close()

            canvas.save()
            canvas.rotate(bearing, center, center)
            paint.color = 0xFF1A73E8.toInt()
            canvas.drawPath(arrowPath, paint)
            canvas.restore()
        }

        // White border
        paint.color = 0xFFFFFFFF.toInt()
        canvas.drawCircle(center, center, 22f, paint)

        // Blue core
        paint.color = 0xFF1A73E8.toInt()
        canvas.drawCircle(center, center, 16f, paint)

        return BitmapDrawable(context.resources, bitmap)
    }

    fun getServiceMarker(context: Context, type: ServiceType): Drawable {
        return when (type) {
            ServiceType.GAS_STATION -> createTeardropPin(context, 0xFF0288D1.toInt(), iconSymbol = "⛽")
            ServiceType.PARKING -> createTeardropPin(context, 0xFF1976D2.toInt(), iconSymbol = "P")
            ServiceType.RESTAURANT -> createTeardropPin(context, 0xFFE65100.toInt(), iconSymbol = "🍴")
            ServiceType.CAFE -> createTeardropPin(context, 0xFF795548.toInt(), iconSymbol = "☕")
            ServiceType.HOSPITAL -> createTeardropPin(context, 0xFFC2185B.toInt(), iconSymbol = "➕")
        }
    }
}
