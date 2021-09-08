package fi.dvv.digiid.poc.wallet.ui.common

import androidx.annotation.MainThread
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

data class Event<out T : Any>(val content: T) {
    private var handled = false

    @MainThread
    fun getContentIfNotHandled() = content.takeUnless { handled }?.also {
        handled = true
    }

    fun peek(): T = content
}

fun <T : Any> LiveData<Event<T>>.observeEvent(
    lifecycleOwner: LifecycleOwner,
    observer: Observer<T>
) {
    observe(lifecycleOwner) { event ->
        event.getContentIfNotHandled()?.let {
            observer.onChanged(it)
        }
    }
}

fun <T : Any> MutableLiveData<Event<T>>.sendEvent(value: T) {
    postValue(Event(value))
}