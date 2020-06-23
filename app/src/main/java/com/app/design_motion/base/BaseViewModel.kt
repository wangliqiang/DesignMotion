package com.app.design_motion.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.*

open class BaseViewModel : ViewModel() {

    fun <T> request(
        onError: (t: Throwable) -> Unit = {},
        onExecute: suspend CoroutineScope.() -> T,
        onComplete: () -> Unit = {}
    ) {
        viewModelScope.launch(errorHandler { onError.invoke(it) }) {
            withContext(Dispatchers.IO) {
                try {
                    onExecute()
                } finally {
                    onComplete()
                }
            }
        }
    }

    private fun errorHandler(onError: (error: Throwable) -> Unit): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            onError.invoke(throwable)
        }
    }
}