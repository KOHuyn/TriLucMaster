package com.mobileplus.dummytriluc.ui.main.challenge.detail

import com.mobileplus.dummytriluc.data.response.DataChallengeDetailResponse
import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.lang.Exception


/**
 * Created by KO Huyn on 12/22/2020.
 */
class ChallengeDetailViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxDetailResponse: PublishSubject<DataChallengeDetailResponse> = PublishSubject.create()

    val userInfo = dataManager.getUserInfo()

    val rxFailedResponse: PublishSubject<Pair<Boolean, String>> = PublishSubject.create()

    val rxJoinSuccess: PublishSubject<Boolean> = PublishSubject.create()

    fun getDetailChallenge(id: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getChallengeDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                try {
                    if (response.isSuccess()) {
                        val data = gson.fromJson(
                            response.dataObject(),
                            DataChallengeDetailResponse::class.java
                        )
                        rxDetailResponse.onNext(data)
                    } else {
                        rxFailedResponse.onNext(Pair(true, response.message()))
                    }
                } catch (e: Exception) {
                    e.logErr()
                    rxFailedResponse.onNext(Pair(true, loadStringRes(R.string.data_not_available)))
                }
                logErr("$response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxFailedResponse.onNext(Pair(true, it.getErrorMsg()))
            })
    }

    fun joinChallenge(id: Int): Disposable {
        return dataManager.postJoinChallenge(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                rxMessage.onNext(response.message())
                rxJoinSuccess.onNext(response.isSuccess())
            }, {
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }

}