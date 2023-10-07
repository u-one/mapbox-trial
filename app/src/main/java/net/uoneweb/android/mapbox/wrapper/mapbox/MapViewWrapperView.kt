package net.uoneweb.android.mapbox.wrapper.mapbox

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.MapEvents
import com.mapbox.maps.extension.observable.eventdata.MapLoadingErrorEventData
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotation
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager
import com.mapbox.maps.plugin.delegates.listeners.OnMapLoadErrorListener
import com.mapbox.maps.plugin.gestures.OnRotateListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.maps.plugin.scalebar.scalebar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import net.uoneweb.android.mapbox.wrapper.CameraState
import net.uoneweb.android.mapbox.wrapper.MapWrapper
import net.uoneweb.android.mapboxtrial.R
import net.uoneweb.android.mapboxtrial.databinding.ViewMapviewwrapperBinding
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class MapViewWrapperView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
    defStyleRes: Int = 0
) : FrameLayout(context, attrs, defStyleAttr, defStyleRes), MapWrapper {


    private val _binding: ViewMapviewwrapperBinding
    private val binding get() = _binding

    private val locationComponentPlugin get() = binding.mapView.location
    private val locationComponentPlugin2 get() = binding.mapView.location2

    private val mapboxMap get() = binding.mapView.getMapboxMap()

    private val _indicatorBearingFlow = MutableStateFlow(0.0)
    private val _indicatorPositionFlow =
        MutableStateFlow(net.uoneweb.android.mapbox.wrapper.Point(0.0, 0.0))
    private val _cameraStateFlow = MutableStateFlow(CameraState.DEFAULT)

    override val indicatorBearingFlow: StateFlow<Double> = _indicatorBearingFlow

    override val indicatorPositionFlow: StateFlow<net.uoneweb.android.mapbox.wrapper.Point> =
        _indicatorPositionFlow

    override val cameraStateFlow: StateFlow<CameraState> = _cameraStateFlow

    override fun currentZoom(): Double {
        return mapboxMap.cameraState.zoom
    }

    override fun getCenterPosition(): net.uoneweb.android.mapbox.wrapper.Point {
        val center = mapboxMap.cameraState.center
        return ObjectConverter.toPoint(center)
    }

    override fun setCameraBearing(bearing: Double) {
        mapboxMap.setCamera(CameraOptions.Builder().bearing(bearing).build())
    }

    override fun setCameraPosition(position: net.uoneweb.android.mapbox.wrapper.Point) {
        val point = com.mapbox.geojson.Point.fromLngLat(position.lon, position.lat)
        mapboxMap.setCamera(CameraOptions.Builder().center(point).build())
    }

    fun setCameraPosition(position: net.uoneweb.android.mapbox.wrapper.Point, bearing: Double) {
        val point = com.mapbox.geojson.Point.fromLngLat(position.lon, position.lat)
        mapboxMap.setCamera(
            CameraOptions.Builder()
                .center(point)
                .bearing(bearing)
                .build()
        )
    }


    private var circleAnnotation: CircleAnnotation? = null

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
                        .center(Point.fromLngLat(lng.toDouble(), lat.toDouble()))
                        .zoom(zoom.toDouble())
                        .build()
                )
            } finally {
                recycle()
            }
        }

        initializeMapBox()
    }

    private fun initializeMapBox() {
        initializeIndicatorListener()
        initializeCameraListener()
        registerEventObserver()
    }

    private fun initializeIndicatorListener() {
        locationComponentPlugin.addOnIndicatorBearingChangedListener { bearing ->
            findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                _indicatorBearingFlow.emit(bearing)
            }

        }
        locationComponentPlugin.addOnIndicatorPositionChangedListener { point ->
            findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                _indicatorPositionFlow.emit(
                    ObjectConverter.toPoint(point)
                )
            }
        }
    }

    private fun initializeCameraListener() {
        mapboxMap.addOnCameraChangeListener {
            findViewTreeLifecycleOwner()?.lifecycleScope?.launch {
                _cameraStateFlow.emit(
                    ObjectConverter.toCameraState(mapboxMap.cameraState)
                )
            }
        }
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

    override fun setupGestures(drawable: Drawable?) {
        drawable ?: return
        val gesturesPlugin = binding.mapView.gestures
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

    private fun addPointAnnotationToMap(drawable: Drawable, point: com.mapbox.geojson.Point) {
        convertDrawableToBitmap(drawable)?.let {
            val annotationApi = binding.mapView.annotations
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

    fun flyTo(bearing: Double, center: net.uoneweb.android.mapbox.wrapper.Point, zoom: Double) {
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