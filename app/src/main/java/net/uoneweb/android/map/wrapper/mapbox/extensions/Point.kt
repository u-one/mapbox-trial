package net.uoneweb.android.map.wrapper.mapbox.extensions

import com.mapbox.geojson.Point


fun Point.wrapper(): net.uoneweb.android.map.wrapper.Point {
    return net.uoneweb.android.map.wrapper.Point(latitude(), longitude())
}