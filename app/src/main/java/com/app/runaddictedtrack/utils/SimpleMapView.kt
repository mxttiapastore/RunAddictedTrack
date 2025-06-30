package com.app.runaddictedtrack.utils

import android.content.Context
import android.graphics.*
import android.location.Location
import android.util.AttributeSet
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import com.app.runaddictedtrack.R

class SimpleMapView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val routePoints = mutableListOf<PointF>()
    private var currentLocation: PointF? = null

    private val routePaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
        strokeWidth = 8f
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
        isAntiAlias = true
    }

    private val currentLocationPaint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.colorError)
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#F0F0F0")
        style = Paint.Style.FILL
    }

    private val gridPaint = Paint().apply {
        color = Color.parseColor("#E0E0E0")
        strokeWidth = 1f
        style = Paint.Style.STROKE
    }

    // Parametri per la trasformazione delle coordinate
    private var minLat = Double.MAX_VALUE
    private var maxLat = Double.MIN_VALUE
    private var minLng = Double.MAX_VALUE
    private var maxLng = Double.MIN_VALUE
    private var scaleFactor = 1f
    private var offsetX = 0f
    private var offsetY = 0f

    // Cache per le location originali
    private val originalLocations = mutableListOf<Location>()

    fun addLocationPoint(location: Location) {
        Log.d("SimpleMapView", "Adding location: ${location.latitude}, ${location.longitude}")
        Log.d("SimpleMapView", "View size: ${width} x ${height}")

        // Salva la location originale
        originalLocations.add(location)

        // Aggiorna i limiti
        if (location.latitude < minLat) minLat = location.latitude
        if (location.latitude > maxLat) maxLat = location.latitude
        if (location.longitude < minLng) minLng = location.longitude
        if (location.longitude > maxLng) maxLng = location.longitude

        // Ricalcola la trasformazione se la view è stata misurata
        if (width > 0 && height > 0) {
            calculateTransformation()
            rebuildPoints()
        }

        invalidate()
    }

    private fun rebuildPoints() {
        routePoints.clear()
        for (location in originalLocations) {
            val point = locationToPoint(location)
            routePoints.add(point)
        }

        // Aggiorna la posizione corrente
        if (originalLocations.isNotEmpty()) {
            currentLocation = routePoints.lastOrNull()
        }
    }

    private fun calculateTransformation() {
        if (minLat == Double.MAX_VALUE || width <= 0 || height <= 0) {
            Log.d("SimpleMapView", "Cannot calculate transformation - invalid bounds or view size")
            return
        }

        val latRange = maxLat - minLat
        val lngRange = maxLng - minLng

        val padding = 50f
        val availableWidth = width - 2 * padding
        val availableHeight = height - 2 * padding

        Log.d("SimpleMapView", "Lat range: $latRange, Lng range: $lngRange")
        Log.d("SimpleMapView", "Available size: ${availableWidth} x ${availableHeight}")

        if (latRange == 0.0 && lngRange == 0.0) {
            // Punto singolo - centrato
            scaleFactor = 1f
            offsetX = width / 2f
            offsetY = height / 2f
        } else if (latRange == 0.0) {
            // Solo longitudine varia
            scaleFactor = if (lngRange > 0) availableWidth.toFloat() / lngRange.toFloat() else 1f
            offsetX = padding
            offsetY = height / 2f
        } else if (lngRange == 0.0) {
            // Solo latitudine varia
            scaleFactor = if (latRange > 0) availableHeight.toFloat() / latRange.toFloat() else 1f
            offsetX = width / 2f
            offsetY = padding
        } else {
            // Entrambe le coordinate variano
            val scaleX = availableWidth / lngRange.toFloat()
            val scaleY = availableHeight / latRange.toFloat()
            scaleFactor = minOf(scaleX, scaleY)

            offsetX = padding + (availableWidth - lngRange.toFloat() * scaleFactor) / 2
            offsetY = padding + (availableHeight - latRange.toFloat() * scaleFactor) / 2
        }

        Log.d("SimpleMapView", "Scale factor: $scaleFactor, Offset: ($offsetX, $offsetY)")
    }

    private fun locationToPoint(location: Location): PointF {
        if (minLat == Double.MAX_VALUE) {
            // Fallback per il primo punto
            return PointF(width / 2f, height / 2f)
        }

        val x = offsetX + (location.longitude - minLng).toFloat() * scaleFactor
        val y = offsetY + (maxLat - location.latitude).toFloat() * scaleFactor // Inverti Y

        Log.d("SimpleMapView", "Location (${location.latitude}, ${location.longitude}) -> Point ($x, $y)")

        return PointF(x, y)
    }

    fun clearRoute() {
        routePoints.clear()
        originalLocations.clear()
        currentLocation = null
        minLat = Double.MAX_VALUE
        maxLat = Double.MIN_VALUE
        minLng = Double.MAX_VALUE
        maxLng = Double.MIN_VALUE
        invalidate()
        Log.d("SimpleMapView", "Route cleared")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d("SimpleMapView", "Size changed to: ${w} x ${h}")

        // Ricalcola tutto quando cambia la dimensione della view
        if (originalLocations.isNotEmpty() && w > 0 && h > 0) {
            calculateTransformation()
            rebuildPoints()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        Log.d("SimpleMapView", "Drawing - Points: ${routePoints.size}, View size: ${width}x${height}")

        // Sfondo
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), backgroundPaint)

        // Griglia semplice
        drawGrid(canvas)

        // Percorso
        if (routePoints.size > 1) {
            val path = Path()
            path.moveTo(routePoints[0].x, routePoints[0].y)

            for (i in 1 until routePoints.size) {
                path.lineTo(routePoints[i].x, routePoints[i].y)
            }

            canvas.drawPath(path, routePaint)
            Log.d("SimpleMapView", "Drew route with ${routePoints.size} points")
        }

        // Posizione corrente
        currentLocation?.let { point ->
            canvas.drawCircle(point.x, point.y, 12f, currentLocationPaint)
            canvas.drawCircle(point.x, point.y, 8f, Paint().apply {
                color = Color.WHITE
                style = Paint.Style.FILL
            })
            Log.d("SimpleMapView", "Drew current location at (${point.x}, ${point.y})")
        }

        // Punto di partenza
        if (routePoints.isNotEmpty()) {
            val startPoint = routePoints[0]
            canvas.drawCircle(startPoint.x, startPoint.y, 10f, Paint().apply {
                color = Color.GREEN
                style = Paint.Style.FILL
            })
            Log.d("SimpleMapView", "Drew start point at (${startPoint.x}, ${startPoint.y})")
        }

        // Messaggio di debug se non ci sono punti
        if (routePoints.isEmpty()) {
            val debugPaint = Paint().apply {
                color = Color.BLACK
                textSize = 32f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText(
                "In attesa del GPS...",
                width / 2f,
                height / 2f,
                debugPaint
            )
        }
    }

    private fun drawGrid(canvas: Canvas) {
        if (width <= 0 || height <= 0) return

        val gridSize = 50f

        // Linee verticali
        var x = 0f
        while (x <= width) {
            canvas.drawLine(x, 0f, x, height.toFloat(), gridPaint)
            x += gridSize
        }

        // Linee orizzontali
        var y = 0f
        while (y <= height) {
            canvas.drawLine(0f, y, width.toFloat(), y, gridPaint)
            y += gridSize
        }
    }
}