package net.uoneweb.android.map.wrapper.mapbox.extensions

import com.mapbox.maps.plugin.annotation.generated.PointAnnotation
import net.uoneweb.android.map.wrapper.mapbox.PointAnnotationImpl

fun PointAnnotation.wrapper(): net.uoneweb.android.map.wrapper.PointAnnotation {
    return PointAnnotationImpl(this.id)
}