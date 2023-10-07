package net.uoneweb.android.mapboxtrial

import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import net.uoneweb.android.mapbox.wrapper.Point
import net.uoneweb.android.mapbox.wrapper.mapbox.MapStyleImpl
import net.uoneweb.android.mapboxtrial.databinding.FragmentMapBinding

@AndroidEntryPoint
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

        initializeMapView()
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


    private fun initializeMapView() {
        lifecycleScope.launch {
            binding.mapView.loadStyle(MapStyleImpl.STYLE_DEFAULT)
            binding.mapView.updateCurrentLocationSetting(true, false, true, true)
            binding.mapView.flyTo(0.0, Point(35.67991, 139.76269), 12.0)
        }

        fragmentViewModel.setTrackingMode(false)
        binding.mapView.setupScaleBar(
            Gravity.BOTTOM or Gravity.START,
            100.0f * resources.displayMetrics.density
        )

        val drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.red_marker)
        binding.mapView.setupGestures(drawable)

        lifecycleScope.launch {
            binding.mapView.indicatorBearingFlow.collect {
                fragmentViewModel.setIndicatorBearing(it)
            }
        }
        lifecycleScope.launch {
            binding.mapView.indicatorPositionFlow.collect {
                fragmentViewModel.setIndicatorPosition(it)
            }
        }
        lifecycleScope.launch {
            binding.mapView.cameraStateFlow.collect {
                fragmentViewModel.setCameraState(it)
            }
        }
        lifecycleScope.launch {
            fragmentViewModel.indicatorBearingFlow.collect {
                binding.mapView.setCameraBearing(it)
            }
        }
        lifecycleScope.launch {
            fragmentViewModel.indicatorPositionFlow.collect {
                binding.mapView.setCameraPosition(it)
            }
        }
        lifecycleScope.launch {
            binding.mapView.mapClickFlow.collect {
                binding.mapView.addPointAnnotationToMap(drawable!!, it)
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