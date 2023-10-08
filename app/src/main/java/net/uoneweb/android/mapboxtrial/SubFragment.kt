package net.uoneweb.android.mapboxtrial

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import net.uoneweb.android.map.wrapper.Point
import net.uoneweb.android.map.wrapper.mapbox.MapStyleImpl
import net.uoneweb.android.map.wrapper.mapbox.MapViewWrapperView

class SubFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                MaterialTheme {
                    MainView()
                }
            }
        }
    }

    @Preview
    @Composable
    fun MainView() {
        Column(Modifier.fillMaxSize().padding(16.dp)) {
            Text(
                text = "Compose with MapBox Sample", fontSize = 24.sp
            )
            Card(
                modifier = Modifier.shadow(8.dp)
            ) {
                AndroidView(
                    modifier = Modifier
                        .height(300.dp)
                        .fillMaxWidth(),
                    factory = { context ->
                        val customAttrs =
                            MapViewWrapperView.CustomAttributes(Point(34.70244, 135.49496), 12.0)
                        MapViewWrapperView(context, customAttrs = customAttrs).apply {
                            initializeMapView(this)
                        }
                    }
                )
            }
        }
    }

    private fun initializeMapView(mapView: MapViewWrapperView) {
        lifecycleScope.launch {
            mapView.loadStyle(MapStyleImpl.STYLE_DEFAULT)
            mapView.updateCurrentLocationSetting(true, false, true, true)
        }
    }
}