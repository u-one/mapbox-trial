package net.uoneweb.android.map.wrapper

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
}
