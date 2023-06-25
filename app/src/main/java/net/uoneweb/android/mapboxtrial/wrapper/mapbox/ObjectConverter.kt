package net.uoneweb.android.mapboxtrial.wrapper.mapbox

import net.uoneweb.android.mapboxtrial.wrapper.CameraState
import net.uoneweb.android.mapboxtrial.wrapper.Point

object ObjectConverter {
    fun toPoint(point: com.mapbox.geojson.Point): Point {
        return Point(point.latitude(), point.longitude())
    }

    fun toCameraState(cameraState: com.mapbox.maps.CameraState): CameraState {
        return CameraState(toPoint(cameraState.center), cameraState.zoom)
    }
}