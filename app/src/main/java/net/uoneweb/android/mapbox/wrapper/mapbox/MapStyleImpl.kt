package net.uoneweb.android.mapbox.wrapper.mapbox

data class MapStyleImpl(val uri: String) : net.uoneweb.android.mapbox.wrapper.MapStyle {

    companion object {
        private const val STYLE_DEFAULT_URI = "mapbox://styles/backflip/cl88dc0ag000r14o4kyd2dk98"

        val STYLE_DEFAULT = MapStyleImpl(STYLE_DEFAULT_URI)
    }
}
