package com.core

import androidx.lifecycle.ViewModel
import com.utils.SchedulerProvider
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.subjects.PublishSubject

abstract class BaseViewModel<DATA>(
    val dataManager: DATA,
    val schedulerProvider: SchedulerProvider
) : ViewModel() {
    val rxMessage: PublishSubject<String> = PublishSubject.create()
    val isLoading: PublishSubject<Boolean> = PublishSubject.create()
    protected val disposable = CompositeDisposable()

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}