package net.uoneweb.android.mapboxtrial

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.geojson.Point
import com.mapbox.maps.*
import com.mapbox.maps.plugin.animation.flyTo
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorPositionChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
import com.mapbox.maps.plugin.locationcomponent.location2
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateBearing
import com.mapbox.maps.plugin.viewport.data.FollowPuckViewportStateOptions
import com.mapbox.maps.plugin.viewport.state.FollowPuckViewportState
import com.mapbox.maps.plugin.viewport.viewport
import net.uoneweb.android.mapboxtrial.databinding.FragmentMapBinding

class MapFragment : Fragment() {

    private var binding: FragmentMapBinding? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMapBinding.inflate(inflater)
        return binding?.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.mapView?.let {
            initializeMapView(it)
        }
    }

    private fun initializeMapView(mapView: MapView) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
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
        setTrackingMode(false)
        registerEventObserver(mapView)

        //setupViewportPlugin(mapView)
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
    }

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener { bearing ->
        binding?.mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder().bearing(bearing).build()
        )
    }

    private val onIndicatorPositionChangedListener = OnIndicatorPositionChangedListener { point ->
        binding?.mapView?.getMapboxMap()?.setCamera(CameraOptions.Builder().center(point).build())
    }

    private fun setTrackingMode(enable: Boolean) {
        binding?.mapView?.let { mapView ->
            mapView.location.removeOnIndicatorBearingChangedListener(
                onIndicatorBearingChangedListener
            )
            mapView.location.removeOnIndicatorPositionChangedListener(
                onIndicatorPositionChangedListener
            )

            if (enable) {
                mapView.location.addOnIndicatorBearingChangedListener(
                    onIndicatorBearingChangedListener
                )

                mapView.location.addOnIndicatorPositionChangedListener(
                    onIndicatorPositionChangedListener
                )
            }
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