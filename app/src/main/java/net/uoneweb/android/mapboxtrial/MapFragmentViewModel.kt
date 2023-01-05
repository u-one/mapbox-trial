package net.uoneweb.android.mapboxtrial

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MapFragmentViewModel : ViewModel() {
    private val _trackingMode = MutableLiveData(false)
    val trackingMode: LiveData<Boolean> = _trackingMode

    fun toggleTrackingMode() {
        val nextMode = _trackingMode.value != true
        _trackingMode.postValue(nextMode)
    }


}