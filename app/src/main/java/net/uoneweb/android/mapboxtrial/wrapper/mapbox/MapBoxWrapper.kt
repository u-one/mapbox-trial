package net.uoneweb.android.mapboxtrial.wrapper.mapbox

import com.mapbox.maps.MapView
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.uoneweb.android.mapboxtrial.wrapper.CameraState
import net.uoneweb.android.mapboxtrial.wrapper.MapWrapper
import net.uoneweb.android.mapboxtrial.wrapper.Point


/**
 * Mapboxを隠蔽するための移植層
 * 段階的にまずはMapboxMapを隠蔽する
 */

class MapBoxWrapper : MapWrapper {
    private var _mapView: MapView? = null
    private var _scope: CoroutineScope? = null

    private val mapView get() = _mapView!!
    private val scope get() = _scope!!

    private val cameraStateFlow = MutableStateFlow<CameraState>(CameraState.DEFAULT)

    override fun registerCore(coreObject: Any, scope: CoroutineScope) {
        if (coreObject is MapView) {
            registerMapView(coreObject, scope)
        }
    }

    /**
     * 最終的にはMapViewもWrapperを作成しDI可能にする
     */
    private fun registerMapView(mapView: MapView, scope: CoroutineScope) {
        _mapView = mapView
        _scope = scope

        initialize()
    }

    override fun cameraStateFlow(): StateFlow<CameraState> = cameraStateFlow

    private fun initialize() {
        mapView.getMapboxMap().addOnCameraChangeListener(::onCameraChanged)
    }


    private fun onCameraChanged(eventData: CameraChangedEventData) {
        updateCameraState()
    }

    private fun updateCameraState() {
        val cameraState = mapView.getMapboxMap().cameraState
        scope.launch {
            cameraStateFlow.emit(
                ObjectConverter.toCameraState(cameraState)
            )
        }
    }

    override fun getCenterPosition(): Point {
        val center = mapView.getMapboxMap().cameraState.center
        return ObjectConverter.toPoint(center)
    }

    fun getCurrentZoom(): Double {
        return mapView.getMapboxMap().cameraState.zoom
    }

}

