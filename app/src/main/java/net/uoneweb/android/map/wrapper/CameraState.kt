package net.uoneweb.android.map.wrapper

data class CameraState(val center: Point, val zoom: Double) {
    companion object {
        val DEFAULT = CameraState(Point(0.0, 0.0), 0.0)
    }
}