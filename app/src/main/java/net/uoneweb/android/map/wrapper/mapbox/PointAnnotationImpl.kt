package net.uoneweb.android.map.wrapper.mapbox

import net.uoneweb.android.map.wrapper.PointAnnotation

class PointAnnotationImpl(private val id: Long) : PointAnnotation {
    override fun id(): Long = id
}