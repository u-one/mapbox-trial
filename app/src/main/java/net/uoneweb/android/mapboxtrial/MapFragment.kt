package net.uoneweb.android.mapboxtrial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.mapbox.maps.CameraOptions
import com.mapbox.maps.EdgeInsets
import com.mapbox.maps.MapView
import com.mapbox.maps.Style
import com.mapbox.maps.plugin.locationcomponent.OnIndicatorBearingChangedListener
import com.mapbox.maps.plugin.locationcomponent.location
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

    private val onIndicatorBearingChangedListener = OnIndicatorBearingChangedListener { bearing ->
        binding?.mapView?.getMapboxMap()?.setCamera(
            CameraOptions.Builder().bearing(bearing).build()
        )
    }


    private fun initializeMapView(mapView: MapView) {
        mapView.getMapboxMap().loadStyleUri(Style.MAPBOX_STREETS) {
            mapView.location.updateSettings {
                enabled = true
            }
        }

        val viewportPlugin = mapView?.viewport
        val followPuckViewportState: FollowPuckViewportState =
            viewportPlugin.makeFollowPuckViewportState(
                FollowPuckViewportStateOptions.Builder()
                    .bearing(FollowPuckViewportStateBearing.Constant(0.0))
                    .padding(EdgeInsets(200.0 * resources.displayMetrics.density, 0.0, 0.0, 0.0))
                    .build()
            )

        viewportPlugin.transitionTo(followPuckViewportState) { success ->
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