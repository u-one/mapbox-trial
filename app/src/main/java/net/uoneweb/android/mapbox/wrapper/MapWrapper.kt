package net.uoneweb.android.mapbox.wrapper

import android.graphics.drawable.Drawable
import kotlinx.coroutines.flow.StateFlow

interface MapWrapper {
    /**
     * 最終的にはMapViewもWrapperを作成しDI可能にする
     */
    fun getCenterPosition(): Point
    fun currentZoom(): Double

    val indicatorBearingFlow: StateFlow<Double>
    val indicatorPositionFlow: StateFlow<Point>
    val cameraStateFlow: StateFlow<CameraState>
    fun setCameraBearing(bearing: Double)
    fun setCameraPosition(position: Point)

    fun setupScaleBar(position: Int, marginBottom: Float)
    fun setupGestures(drawable: Drawable?)
}

data class CameraState(val center: Point, val zoom: Double) {
    companion object {
        val DEFAULT = CameraState(Point(0.0, 0.0), 0.0)
    }
}
