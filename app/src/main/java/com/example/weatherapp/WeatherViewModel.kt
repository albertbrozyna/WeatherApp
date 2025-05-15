package com.example.weatherapp

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

class WeatherViewModel : ViewModel() {

    private var refreshJob: Job? = null
    private var lastResumeTime: Long = 0L
    private var accumulatedActiveTime: Long = 0L

    fun startAutoRefreshTimer(
        context: Context,
        refreshIntervalMs: Long,
        onRefresh: suspend () -> Unit
    ) {
        lastResumeTime = System.currentTimeMillis()
        refreshJob?.cancel()
        refreshJob = viewModelScope.launch {
            while (isActive) {
                val now = System.currentTimeMillis()
                val elapsed = accumulatedActiveTime + (now - lastResumeTime)
                val remaining = refreshIntervalMs - elapsed
                if (remaining <= 0) {
                    onRefresh()
                    lastResumeTime = System.currentTimeMillis()
                    accumulatedActiveTime = 0L
                } else {
                    delay(remaining)
                }
            }
        }
    }

    fun pauseAutoRefresh() {
        val now = System.currentTimeMillis()
        accumulatedActiveTime += now - lastResumeTime
        refreshJob?.cancel()
    }

    override fun onCleared() {
        refreshJob?.cancel()
        super.onCleared()
    }
}
