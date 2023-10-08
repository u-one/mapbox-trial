package net.uoneweb.android.map.wrapper.mapbox

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapEvents
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.animation.easeTo
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.OnRotateListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.maps.plugin.scalebar.scalebar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.uoneweb.android.map.wrapper.*
import net.uoneweb.android.map.wrapper.mapbox.extensions.wrapper
import net.uoneweb.android.mapboxtrial.R
import net.uoneweb.android.mapboxtrial.databinding.ViewMapviewwrapperBinding
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


@AndroidEntryPoint
class MapViewWrapperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0,
    customAttrs: CustomAttributes? = null
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), MapWrapper {

    data class CustomAttributes(
        val cameraTarget: Point? = null,
        val cameraZoom: Double? = null
    )

    @Inject
    lateinit var bitmapFactory: BitmapFactory

    private val _binding: ViewMapviewwrapperBinding
    private val binding get() = _binding

    private val lifecycleOwner get() = findViewTreeLifecycleOwner()!!

    private val mapboxMap get() = binding.mapView.getMapboxMap()
    private val locationComponentPlugin get() = binding.mapView.location
    private val locationComponentPlugin2 get() = binding.mapView.location2
    private val pointAnnotationManager get() = binding.mapView.annotations.createPointAnnotationManager()

    private val _indicatorBearingFlow = MutableStateFlow(0.0)
    private val _indicatorPositionFlow =
        MutableStateFlow(Point(0.0, 0.0))
    private val _cameraStateFlow = MutableStateFlow(CameraState.DEFAULT)

    override val indicatorBearingFlow: StateFlow<Double> = _indicatorBearingFlow

    override val indicatorPositionFlow: StateFlow<Point> =
        _indicatorPositionFlow

    override val cameraStateFlow: StateFlow<CameraState> = _cameraStateFlow

    private val _mapClickFlow: MutableSharedFlow<Point> = MutableSharedFlow()
    val mapClickFlow: Flow<Point> = _mapClickFlow

    override fun currentZoom(): Double {
        return mapboxMap.cameraState.zoom
    }

    override fun getCenterPosition(): Point {
        val center = mapboxMap.cameraState.center
        return center.wrapper()
    }

    override fun setCameraBearing(bearing: Double) {
        mapboxMap.setCamera(CameraOptions.Builder().bearing(bearing).build())
    }

    override fun setCameraPosition(position: Point) {
        val point = com.mapbox.geojson.Point.fromLngLat(position.lon, position.lat)
        mapboxMap.setCamera(CameraOptions.Builder().center(point).build())
    }

    @Suppress("unused")
    fun setCameraPosition(position: Point, bearing: Double) {
        val point = com.mapbox.geojson.Point.fromLngLat(position.lon, position.lat)
        mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(point)
                .bearing(bearing)
                .build()
        )
    }

    init {
        _binding = ViewMapviewwrapperBinding.inflate(
            LayoutInflater.from(context),
            this,
            true
        )

        context.theme.obtainStyledAttributes(attrs, R.styleable.MapViewWrapperView, 0, 0).apply {
            try {
                val lat = getFloat(R.styleable.MapViewWrapperView_map_cameraTargetLat, 0f)
                val lng = getFloat(R.styleable.MapViewWrapperView_map_cameraTargetLng, 0f)
                val zoom = getFloat(R.styleable.MapViewWrapperView_map_cameraZoom, 0f)
                binding.mapView.getMapboxMap().setCamera(
                    CameraOptions.Builder()
                        .center(com.mapbox.geojson.Point.fromLngLat(lng.toDouble(), lat.toDouble()))
                        .zoom(zoom.toDouble())
                        .build()
                )
            } finally {
                recycle()
            }
        }

        if (customAttrs != null) {
            val builder = CameraOptions.Builder()
            customAttrs.cameraTarget?.let {
                builder.center(
                    com.mapbox.geojson.Point.fromLngLat(
                        it.lon,
                        it.lat
                    )
                )
            }
            customAttrs.cameraZoom?.let { builder.zoom(it) }
            binding.mapView.getMapboxMap().setCamera(builder.build())
        }

        initializeMapBox()
    }

    private fun initializeMapBox() {
        initializeIndicatorListener()
        initializeCameraListener()
        initializeGestureListeners()
        registerEventObserver()
    }

    private fun initializeIndicatorListener() {
        locationComponentPlugin.addOnIndicatorBearingChangedListener { bearing ->
            lifecycleOwner.lifecycleScope.launch {
                _indicatorBearingFlow.emit(bearing)
            }

        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener { point ->
            lifecycleOwner.lifecycleScope.launch {
                _indicatorPositionFlow.emit(
                    point.wrapper()
                )
            }
        }
    }

    private fun initializeCameraListener() {
        mapboxMap.addOnCameraChangeListener {
            lifecycleOwner.lifecycleScope.launch {
                _cameraStateFlow.emit(
                    mapboxMap.cameraState.wrapper()
                )
            }
        }
    }

    private fun initializeGestureListeners() {
        val gesturesPlugin = binding.mapView.gestures
        gesturesPlugin.addOnMapClickListener { point ->
            lifecycleOwner.lifecycleScope.launch {
                _mapClickFlow.emit(Point(point.latitude(), point.longitude()))
            }
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

    override fun setupScaleBar(position: Int, marginBottom: Float) {
        val scaleBarPlugin = binding.mapView.scalebar
        scaleBarPlugin.updateSettings {
            this.enabled = true
            this.position = position
            this.marginBottom = marginBottom
        }
    }

    fun addPointAnnotationToMap(
        drawable: Drawable,
        point: Point
    ): PointAnnotation? {
        return bitmapFactory.fromDrawable(drawable)?.let {
            val options = PointAnnotationOptions()
                .withPoint(com.mapbox.geojson.Point.fromLngLat(point.lon, point.lat))
                .withIconImage(it)

            pointAnnotationManager.create(options).wrapper()
        }
    }

    suspend fun loadStyle(style: MapStyleImpl) = suspendCoroutine { continuation ->
        binding.mapView.getMapboxMap().loadStyleUri(
            style.uri,
            { continuation.resume(MapStyleImpl(it.styleURI)) },
            object : OnMapLoadErrorListener {
                override fun onMapLoadError(eventData: MapLoadingErrorEventData) {
                    continuation.resumeWithException(Exception(eventData.message))
                }
            }
        )
    }


    fun updateCurrentLocationSetting(
        enable: Boolean,
        pulsing: Boolean = false,
        showAccuracyRing: Boolean = false,
        bearingEnable: Boolean = false
    ) {
        locationComponentPlugin.updateSettings {
            enabled = enable
            this.pulsingEnabled = pulsing
        }
        locationComponentPlugin2.updateSettings2 {
            this.showAccuracyRing = showAccuracyRing
            this.puckBearingEnabled = bearingEnable
        }
    }

    fun easeTo(bearing: Double, center: Point, zoom: Double) {
        mapboxMap.easeTo(
            CameraOptions.Builder()
                .bearing(bearing)
                .center(com.mapbox.geojson.Point.fromLngLat(center.lon, center.lat))
                .zoom(zoom)
                .build()
        )
    }

    fun flyTo(bearing: Double, center: Point, zoom: Double) {
        mapboxMap.flyTo(
            CameraOptions.Builder()
                .bearing(bearing)
                .center(com.mapbox.geojson.Point.fromLngLat(center.lon, center.lat))
                .zoom(zoom)
                .build()
        )
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

}