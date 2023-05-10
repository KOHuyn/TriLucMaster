package com.mobileplus.dummytriluc.ui.main.challenge.more

import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.BaseItemChallenge
import com.mobileplus.dummytriluc.data.model.ItemAppellation
import com.mobileplus.dummytriluc.data.model.ItemChallengeAchievement
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject


/**
 * Created by KO Huyn on 12/22/2020.
 */
class ChallengeMoreViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) : BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxChallengeArr: PublishSubject<Pair<List<BaseItemChallenge>, Page>> =
        PublishSubject.create()

    fun getMoreChallenge(type: String, page: Int? = null): Disposable {
        isLoading.onNext(true)
        return dataManager.getChallengeMore(type, page)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val data = gson.toList<ItemChallengeAchievement>(
                        response.dataObject().dataArray()
                    )
                    val arrData = mutableListOf<BaseItemChallenge>()
                    data.forEach {
                        arrData.add(it)
                    }
                    rxChallengeArr.onNext(Pair(arrData, response.dataObject().page()))
                }
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
                it.logErr()
            })
    }
}