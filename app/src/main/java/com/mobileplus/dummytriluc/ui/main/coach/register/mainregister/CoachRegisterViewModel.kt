package com.mobileplus.dummytriluc.ui.main.coach.register.mainregister

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.request.CoachRegisterRequest
import com.mobileplus.dummytriluc.ui.utils.extensions.getErrorMsg
import com.mobileplus.dummytriluc.ui.utils.extensions.isSuccess
import com.mobileplus.dummytriluc.ui.utils.extensions.message
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject

class CoachRegisterViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val getUserInfo = dataManager.getUserInfo()

    val rxRegisterSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun registerCoach(request: CoachRegisterRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.registerCoach(request)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxRegisterSuccess.onNext(response.isSuccess())
                if (!response.isSuccess()) rxMessage.onNext(response.message())
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}