package net.uoneweb.android.mapboxtrial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import net.uoneweb.android.mapbox.wrapper.MapWrapper
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor(private val map: MapWrapper) : ViewModel() {
    private val _trackingMode = MutableLiveData(false)
    val trackingMode: LiveData<Boolean> = _trackingMode

    fun toggleTrackingMode() {
        val nextMode = _trackingMode.value != true
        _trackingMode.postValue(nextMode)
    }

    init {
        viewModelScope.launch {
            map.indicatorBearing().collect {
                if (_trackingMode.value == true) {
                    map.setCameraBearing(it)
                }
            }
            map.indicatorPosition().collect {
                if (_trackingMode.value == true) {
                    map.setCameraPosition(it)
                }
            }
        }
    }

    fun centerPosition(): StateFlow<String> = map.cameraStateFlow().map {
        String.format("Center lat=%.6f lon=%.6f", it.center.lat, it.center.lon)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun zoom() = map.cameraStateFlow().map {
        String.format("Zoom=%.2f", it.zoom)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")


}