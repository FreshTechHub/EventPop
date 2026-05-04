package com.android.example.eventpop.ui.screens

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.content.Context
import com.android.example.eventpop.data.EventCategory
import org.maplibre.android.annotations.Icon
import org.maplibre.android.annotations.IconFactory
import kotlin.math.min

/**
 * Canvas-drawn map pins (40×48 dp): category fill, 2dp white ring, white location glyph, bottom pointer.
 */
internal object MapMarkerBitmaps {

    private val cache = mutableMapOf<EventCategory, Bitmap>()

    fun iconForCategory(context: Context, category: EventCategory): Icon {
        val density = context.resources.displayMetrics.density
        val wPx = (40f * density).toInt().coerceAtLeast(40)
        val hPx = (48f * density).toInt().coerceAtLeast(48)
        val bitmap = cache.getOrPut(category) {
            createMarkerBitmap(wPx, hPx, category.markerColorHex.toInt(), density)
        }
        return IconFactory.getInstance(context).fromBitmap(bitmap)
    }

    private fun createMarkerBitmap(w: Int, h: Int, fillColor: Int, density: Float): Bitmap {
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val cx = w / 2f
        val bodyRadius = min(w, h) * 0.38f
        val cy = bodyRadius + 3f * density
        val stroke = 2f * density

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }
        val ringPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
            strokeWidth = stroke
            color = android.graphics.Color.WHITE
        }
        val whitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = android.graphics.Color.WHITE
        }

        val tipY = h - 2f * density
        val baseY = cy + bodyRadius * 0.55f
        val halfW = bodyRadius * 0.55f
        val pointer = Path().apply {
            moveTo(cx - halfW, baseY)
            lineTo(cx + halfW, baseY)
            lineTo(cx, tipY)
            close()
        }
        canvas.drawPath(pointer, fillPaint)
        canvas.drawPath(pointer, ringPaint)

        canvas.drawCircle(cx, cy, bodyRadius, fillPaint)
        canvas.drawCircle(cx, cy, bodyRadius - stroke / 2f, ringPaint)

        val glyphR = 9f * density
        canvas.drawCircle(cx, cy - bodyRadius * 0.12f, glyphR, whitePaint)
        val inner = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            color = fillColor
        }
        canvas.drawCircle(cx, cy - bodyRadius * 0.12f, glyphR * 0.45f, inner)

        val stem = Path().apply {
            moveTo(cx, cy + bodyRadius * 0.08f)
            lineTo(cx - glyphR * 0.55f, cy - bodyRadius * 0.55f)
            lineTo(cx + glyphR * 0.55f, cy - bodyRadius * 0.55f)
            close()
        }
        canvas.drawPath(stem, whitePaint)

        return bitmap
    }
}
