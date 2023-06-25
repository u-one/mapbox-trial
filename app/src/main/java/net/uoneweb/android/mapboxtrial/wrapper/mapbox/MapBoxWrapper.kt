package net.uoneweb.android.mapboxtrial.wrapper.mapbox

import android.util.Log
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapEvents
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
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
    private val mapboxMap get() = mapView.getMapboxMap()
    private val locationComponentPlugin get() = mapView.location
    private val locationComponentPlugin2 get() = mapView.location2

    private val cameraStateFlow = MutableStateFlow<CameraState>(CameraState.DEFAULT)
    private val indicatorBearingFlow = MutableStateFlow(0.0)
    private val indicatorPositionFlow = MutableStateFlow(Point(0.0, 0.0))

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
        mapboxMap.loadStyleUri(STYLE_DEFAULT, ::onStyleLoaded)
        mapboxMap.addOnCameraChangeListener(::onCameraChanged)
        initializeIndicatorListener()
        registerEventObserver()
    }

    private fun initializeIndicatorListener() {
        locationComponentPlugin.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )
        locationComponentPlugin.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
    }

    private fun registerEventObserver() {
        mapboxMap.subscribe(
            { event -> Log.d("MapEvents", event.type) },
            listOf(
                MapEvents.MAP_LOADED,
                MapEvents.MAP_IDLE,
                //MapEvents.RENDER_FRAME_STARTED,
                //MapEvents.RENDER_FRAME_FINISHED,
                MapEvents.CAMERA_CHANGED,
                MapEvents.STYLE_DATA_LOADED,
                MapEvents.SOURCE_DATA_LOADED
            )
        )
    }


    private fun onStyleLoaded(style: Style) {
        locationComponentPlugin.updateSettings {
            enabled = true
            this.pulsingEnabled = false
        }
        locationComponentPlugin2.updateSettings2 {
            showAccuracyRing = true
            puckBearingEnabled = true
        }
        mapboxMap.flyTo(
            CameraOptions.Builder()
                .bearing(0.0)
                .center(com.mapbox.geojson.Point.fromLngLat(139.76269, 35.67991))
                .zoom(12.0)
                .build()
        )
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener { bearing ->
        scope.launch {
            indicatorBearingFlow.emit(bearing)
        }
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        scope.launch {
            indicatorPositionFlow.emit(
                ObjectConverter.toPoint(point)
            )
        }
    }


    private fun onCameraChanged(eventData: CameraChangedEventData) {
        updateCameraState()
    }

    private fun updateCameraState() {
        val cameraState = mapboxMap.cameraState
        scope.launch {
            cameraStateFlow.emit(
                ObjectConverter.toCameraState(cameraState)
            )
        }
    }

    fun getCurrentZoom(): Double {
        return mapboxMap.cameraState.zoom
    }

    override fun getCenterPosition(): Point {
        val center = mapboxMap.cameraState.center
        return ObjectConverter.toPoint(center)
    }

    override fun indicatorBearing(): Flow<Double> = indicatorBearingFlow

    override fun indicatorPosition(): Flow<Point> = indicatorPositionFlow

    override fun setCameraBearing(bearing: Double) {
        mapboxMap.setCamera(CameraOptions.Builder().bearing(bearing).build())
    }

    override fun setCameraPosition(position: Point) {
        val point = com.mapbox.geojson.Point.fromLngLat(position.lon, position.lat)
        mapboxMap.setCamera(CameraOptions.Builder().center(point).build())
    }

    companion object {
        private const val STYLE_DEFAULT = "mapbox://styles/backflip/cl88dc0ag000r14o4kyd2dk98"
    }

}

