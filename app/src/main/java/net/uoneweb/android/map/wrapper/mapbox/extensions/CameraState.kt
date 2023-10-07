package net.uoneweb.android.map.wrapper.mapbox.extensions

import com.mapbox.maps.CameraState

fun CameraState.wrapper(): net.uoneweb.android.map.wrapper.CameraState {
    return net.uoneweb.android.map.wrapper.CameraState(center.wrapper(), zoom)
}