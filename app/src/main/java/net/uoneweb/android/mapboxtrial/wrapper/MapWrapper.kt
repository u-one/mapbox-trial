package net.uoneweb.android.mapboxtrial.wrapper

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface MapWrapper {
    /**
     * 最終的にはMapViewもWrapperを作成しDI可能にする
     */
    fun registerCore(coreObject: Any, scope: CoroutineScope)
    fun cameraStateFlow(): StateFlow<CameraState>
    fun getCenterPosition(): Point

    fun indicatorBearing(): Flow<Double>
    fun indicatorPosition(): Flow<Point>
    fun setCameraBearing(bearing: Double)
    fun setCameraPosition(position: Point)
}

data class Point(val lat: Double, val lon: Double)

data class CameraState(val center: Point, val zoom: Double) {
    companion object {
        val DEFAULT = CameraState(Point(0.0, 0.0), 0.0)
    }
}
