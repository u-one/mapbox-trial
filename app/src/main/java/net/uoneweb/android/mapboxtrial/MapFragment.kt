package net.uoneweb.android.mapboxtrial

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.mapbox.android.gestures.RotateGestureDetector
import com.mapbox.geojson.Point
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.*
import com.mapbox.maps.plugin.gestures.OnRotateListener
import com.mapbox.maps.plugin.gestures.gestures
import com.mapbox.maps.plugin.scalebar.scalebar
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.maps.plugin.viewport.viewport
import dagger.hilt.android.AndroidEntryPoint
import net.uoneweb.android.mapboxtrial.databinding.FragmentMapBinding
import net.uoneweb.android.mapboxtrial.wrapper.MapWrapper
import javax.inject.Inject

@AndroidEntryPoint
class MapFragment : Fragment() {

    @Inject
    lateinit var mapWrapeer: MapWrapper

    private val fragmentViewModel: MapFragmentViewModel by viewModels()
    private var _binding: FragmentMapBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMapBinding.inflate(inflater).apply {
            mapWrapeer.registerCore(mapView as Any, viewLifecycleOwner.lifecycleScope)
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
        // TODO: create MapViewWrapper
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


    private var circleAnnotation: CircleAnnotation? = null

    private fun initializeMapView(mapView: MapView) {

        setTrackingMode(false)
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

    private var trackingMode: Boolean = false

    private fun setTrackingMode(enable: Boolean) {
        trackingMode = enable == true
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


}