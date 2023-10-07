package net.uoneweb.android.mapboxtrial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import net.uoneweb.android.map.wrapper.CameraState
import net.uoneweb.android.map.wrapper.Point
import javax.inject.Inject

@HiltViewModel
class MapFragmentViewModel @Inject constructor() : ViewModel() {
    private val _trackingMode = MutableLiveData(false)
    val trackingMode: LiveData<Boolean> = _trackingMode

    fun setTrackingMode(enable: Boolean) {
        _trackingMode.postValue(enable)
    }

    fun toggleTrackingMode() {
        val nextMode = _trackingMode.value != true
        _trackingMode.postValue(nextMode)
    }

    private fun isTrackingMode(): Boolean = _trackingMode.value == true

    private val _indicatorBearingFlow: MutableStateFlow<Double> = MutableStateFlow(0.0)
    private val _indicatorPositionFlow: MutableStateFlow<Point> = MutableStateFlow(Point(0.0, 0.0))
    private val _cameraStateFlow = MutableStateFlow<CameraState>(CameraState.DEFAULT)

    val indicatorBearingFlow: Flow<Double> = _indicatorBearingFlow.filter { isTrackingMode() }
    val indicatorPositionFlow: Flow<Point> = _indicatorPositionFlow.filter { isTrackingMode() }

    fun setIndicatorBearing(it: Double) {
        viewModelScope.launch {
            _indicatorBearingFlow.emit(it)
        }
    }

    fun setIndicatorPosition(it: Point) {
        viewModelScope.launch {
            _indicatorPositionFlow.emit(it)
        }
    }

    fun setCameraState(it: CameraState) {
        viewModelScope.launch {
            _cameraStateFlow.emit(it)
        }
    }

    fun centerPosition(): StateFlow<String> = _cameraStateFlow.map {
        String.format("Center lat=%.6f lon=%.6f", it.center.lat, it.center.lon)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")

    fun zoomText() = _cameraStateFlow.map {
        String.format("Zoom=%.2f", it.zoom)
    }.stateIn(viewModelScope, SharingStarted.Lazily, "")
}