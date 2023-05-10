package com.mobileplus.dummytriluc.ui.main.challenge

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.BaseItemChallenge
import com.mobileplus.dummytriluc.data.model.ItemAppellation
import com.mobileplus.dummytriluc.data.response.DataChallengeResponse
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import java.lang.Exception


/**
 * Created by KO Huyn on 12/22/2020.
 */
class ChallengeViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxMyReward: PublishSubject<List<ItemAppellation>> = PublishSubject.create()
    val rxMyChallenge: PublishSubject<List<BaseItemChallenge>> = PublishSubject.create()
    val rxChallengeCommunity: PublishSubject<List<BaseItemChallenge>> = PublishSubject.create()
    val rxNoData: PublishSubject<Boolean> = PublishSubject.create()

    fun getChallenge(): Disposable {
        isLoading.onNext(true)
        return dataManager.getChallenge()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    try {
                        val data =
                            gson.fromJson(response.dataObject(), DataChallengeResponse::class.java)
                        val arrMyChallenge = mutableListOf<BaseItemChallenge>()
                        data.challengesJoin?.forEach {
                            arrMyChallenge.add(it)
                        }
                        val arrChallengeCommunity = mutableListOf<BaseItemChallenge>()
                        data.challengePublish?.forEach {
                            arrChallengeCommunity.add(it)
                        }
                        rxMyReward.onNext(data.myReward ?: listOf())
                        rxMyChallenge.onNext(arrMyChallenge)
                        rxChallengeCommunity.onNext(arrChallengeCommunity)
                        rxNoData.onNext(data.isEmpty())
                    } catch (e: Exception) {
                        e.logErr()
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
                logErr("$response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }
}