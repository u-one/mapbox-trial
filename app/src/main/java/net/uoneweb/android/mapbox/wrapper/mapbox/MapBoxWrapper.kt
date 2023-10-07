package net.uoneweb.android.mapbox.wrapper.mapbox

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapEvents
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.extension.observable.eventdata.CameraChangedEventData
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnRotateListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.uoneweb.android.mapbox.wrapper.CameraState
import net.uoneweb.android.mapbox.wrapper.MapWrapper
import net.uoneweb.android.mapbox.wrapper.Point


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

    private var circleAnnotation: CircleAnnotation? = null

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

    override fun setupScaleBarPlugin(position: Int, marginBottom: Float) {
        val scaleBarPlugin = mapView.scalebar
        scaleBarPlugin.updateSettings {
            this.enabled = true
            this.position = position
            this.marginBottom = marginBottom
        }
    }

    override fun setupGesturesPlugin(drawable: Drawable?) {
        drawable ?: return
        val gesturesPlugin = mapView.gestures
        gesturesPlugin.addOnMapClickListener { point ->
            //addCircleAnnotation(mapView, point)
            addPointAnnotationToMap(drawable, point)
            true
        }
        gesturesPlugin.addOnRotateListener(object : OnRotateListener {
            override fun onRotate(detector: RotateGestureDetector) {
            }

            override fun onRotateBegin(detector: RotateGestureDetector) {
            }

            override fun onRotateEnd(detector: RotateGestureDetector) {
            }

        })
    }

    private fun addCircleAnnotation(
        point: com.mapbox.geojson.Point
    ) {
        val annotationApi = mapView.annotations
        val circleAnnotationManager = annotationApi.createCircleAnnotationManager()
        val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(point)
            .withCircleRadius(10.0)
        circleAnnotationManager.create(circleAnnotationOptions)
    }

    private fun addPointAnnotationToMap(drawable: Drawable, point: com.mapbox.geojson.Point) {
        convertDrawableToBitmap(drawable)?.let {
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnntationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
            pointAnnotationManager.create(pointAnntationOptions)
        }
    }

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth,
                drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    /*
    fun setupViewportPlugin(mapView: MapView) {
        val viewportPlugin = mapView.viewport
        val followPuckViewportState: FollowPuckViewportState =
            viewportPlugin.makeFollowPuckViewportState(
                FollowPuckViewportStateOptions.Builder()
                    .bearing(FollowPuckViewportStateBearing.Constant(0.0))
                    .padding(EdgeInsets(100.0 * resources.displayMetrics.density, 0.0, 0.0, 0.0))
                    .pitch(0.0)
                    .build()
            )

        viewportPlugin.transitionTo(followPuckViewportState) { success ->
        }
    }

 */


    companion object {
        private const val STYLE_DEFAULT = "mapbox://styles/backflip/cl88dc0ag000r14o4kyd2dk98"
    }

}
