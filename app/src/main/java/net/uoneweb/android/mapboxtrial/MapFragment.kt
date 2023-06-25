package net.uoneweb.android.mapboxtrial

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.*
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
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.maps.plugin.viewport.viewport
import net.uoneweb.android.mapboxtrial.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private val fragmentViewModel: MapFragmentViewModel by viewModels()
    private var _binding: FragmentMapBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater).apply {
            viewModel = fragmentViewModel
            lifecycleOwner = viewLifecycleOwner
        }
        return _binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.mapView.let {
            initializeMapView(it)
        }
        //setupFloatingActionButton()
    }

    /*
    private fun setupFloatingActionButton() {
        binding?.fab?.setOnClickListener { view ->
            val nextMode = !trackingMode
            setTrackingMode(nextMode)
            view.isSelected = nextMode
            view.backgroundTintList = ColorStateList(
                arrayOf(
                    intArrayOf(),
                    intArrayOf(android.R.attr.state_selected)
                ),
                intArrayOf(R.color.white, R.color.purple_200)
            )

        }
    }
     */

    private val STYLE_DEFAULT = "mapbox://styles/backflip/cl88dc0ag000r14o4kyd2dk98"

    private var circleAnnotation: CircleAnnotation? = null

    private fun initializeMapView(mapView: MapView) {
        mapView.getMapboxMap().loadStyleUri(STYLE_DEFAULT) { style ->
            mapView.location.updateSettings {
                enabled = true
                this.pulsingEnabled = false
            }
            mapView.location2.updateSettings2 {
                showAccuracyRing = true
                puckBearingEnabled = true
            }
            mapView.getMapboxMap().flyTo(
                CameraOptions.Builder()
                    .bearing(0.0)
                    .center(Point.fromLngLat(139.76269, 35.67991))
                    .zoom(12.0)
                    .build()
            )
        }

        initializeIndicatorListener(mapView)
        setTrackingMode(false)
        registerEventObserver(mapView)
        setupScaleBarPlugin(mapView)
        //setupViewportPlugin(mapView)
        setupGesturesPlugin(mapView)
    }

    private fun setupScaleBarPlugin(mapView: MapView) {
        val scaleBarPlugin = mapView.scalebar
        scaleBarPlugin.updateSettings {
            enabled = true
            position = Gravity.BOTTOM or Gravity.START
            marginBottom = 10.0f * resources.displayMetrics.density
        }
    }

    private fun setupViewportPlugin(mapView: MapView) {
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

    private fun setupGesturesPlugin(mapView: MapView) {
        val gesturesPlugin = mapView.gestures
        gesturesPlugin.addOnMapClickListener { point ->
            //addCircleAnnotation(mapView, point)
            addPointAnnotationToMap(mapView, point)
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

    private fun registerEventObserver(mapView: MapView) {
        mapView.getMapboxMap().subscribe(
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
        mapView.getMapboxMap().addOnCameraChangeListener(::onCameraChanged)
    }

    private fun onCameraChanged(eventData: CameraChangedEventData) {
        updateCurrentInfo()
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener { bearing ->
        if (trackingMode) {
            binding.mapView.getMapboxMap().setCamera(
                CameraOptions.Builder().bearing(bearing).build()
            )
        }
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        if (trackingMode) {
            binding.mapView.getMapboxMap()
                .setCamera(CameraOptions.Builder().center(point).build())
        }
    }

    private var trackingMode: Boolean = false

    private fun setTrackingMode(enable: Boolean) {
        trackingMode = enable == true
    }

    private fun initializeIndicatorListener(mapView: MapView) {
        mapView.location.addOnIndicatorBearingChangedListener(
            onIndicatorBearingChangedListener
        )

        mapView.location.addOnIndicatorPositionChangedListener(
            onIndicatorPositionChangedListener
        )
    }

    private fun addCircleAnnotation(
        mapView: MapView, point: Point
    ) {
        val annotationApi = mapView.annotations
        val circleAnnotationManager = annotationApi?.createCircleAnnotationManager()
        val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(point)
            .withCircleRadius(10.0)
        circleAnnotationManager?.create(circleAnnotationOptions)
    }

    private fun addPointAnnotationToMap(mapView: MapView, point: Point) {
        bitmapFromDrawableRes(requireContext(), R.drawable.red_marker)?.let {
            val annotationApi = mapView.annotations
            val pointAnnotationManager = annotationApi.createPointAnnotationManager()
            val pointAnntationOptions: PointAnnotationOptions = PointAnnotationOptions()
                .withPoint(point)
                .withIconImage(it)
            pointAnnotationManager.create(pointAnntationOptions)
        }


    }

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceid: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceid))

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

    override fun onStart() {
        super.onStart()
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun updateCurrentInfo() {
        val center = getCenterPosition()
        binding.currentPosition.text =
            String.format("Center lat=%.6f lon=%.6f", center.latitude(), center.longitude())
        binding.currentZoom.text = String.format("Zoom=%.2f", getCurrentZoom())

    }

    private fun getCenterPosition(): Point = getCameraState().center

    private fun getCurrentZoom(): Double = getCameraState().zoom

    private fun getCameraState(): CameraState =
        binding.mapView.getMapboxMap().cameraState

}