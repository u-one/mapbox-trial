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
import net.uoneweb.android.mapbox.wrapper.MapWrapper
import net.uoneweb.android.mapboxtrial.databinding.FragmentMapBinding
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
        setTrackingMode(false)
        mapWrapeer.setupScaleBarPlugin(
            Gravity.BOTTOM or Gravity.START,
            100.0f * resources.displayMetrics.density
        )
        val drawable = AppCompatResources.getDrawable(requireContext(), R.drawable.red_marker)
        mapWrapeer.setupGesturesPlugin(drawable)

    }


    private var trackingMode: Boolean = false

    private fun setTrackingMode(enable: Boolean) {
        trackingMode = enable == true
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