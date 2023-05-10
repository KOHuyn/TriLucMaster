package com.mobileplus.dummytriluc.ui.main.coach.session

import android.annotation.SuppressLint
import com.core.BaseViewModel
import com.google.gson.Gson
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.request.session.CoachSessionCreateRequest
import com.mobileplus.dummytriluc.data.request.session.CoachSessionSavedListRequest
import com.mobileplus.dummytriluc.data.response.session.CoachSessionOldResponse
import com.mobileplus.dummytriluc.data.response.session.CoachSessionResultResponse
import com.mobileplus.dummytriluc.data.response.session.DataCoachSessionCreatedResponse
import com.mobileplus.dummytriluc.ui.main.coach.session.practitioner.adapter.CoachSessionPractitionerAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import org.json.JSONArray
import org.json.JSONObject

/**
 * Created by KOHuyn on 4/27/2021
 */
class CoachSessionViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    val rxCreateSession: PublishSubject<DataCoachSessionCreatedResponse> = PublishSubject.create()

    val rxGetSessionOld: PublishSubject<CoachSessionOldResponse> = PublishSubject.create()
    val rxPostResultSuccess: PublishSubject<CoachSessionOldResponse> = PublishSubject.create()

    var isContinueSession: Boolean = false
        set(value) {
            dataManager.isDataSecurityBle = value
            field = value
        }

    fun createSession(createRequest: CoachSessionCreateRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.createCoachSession(createRequest)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    gson.fromJsonSafe<DataCoachSessionCreatedResponse>(response.dataObject())?.let {
                        rxCreateSession.onNext(it)
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
                isLoading.onNext(false)
                logErr(response.toString())
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun savedPracticeList(request: CoachSessionSavedListRequest): Disposable {
        isLoading.onNext(true)
        return dataManager.savedCoachSessionListPractice(request)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                rxMessage.onNext(response.message())
                logErr("savedPracticeList:$response")
            }, {
                isLoading.onNext(false)
                it.logErr()
                rxMessage.onNext(it.getErrorMsg())
            })
    }


    fun getDetailSessionOld(idSession: Int): Disposable {
        isLoading.onNext(true)
        return dataManager.getSessionDetail(idSession)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    val dataSession =
                        gson.fromJsonSafe<CoachSessionOldResponse>(response.dataObject())
                    if (dataSession != null) {
                        rxGetSessionOld.onNext(dataSession)
                    }
                }
                isLoading.onNext(false)
                logErr("getDetailSessionOld: $response")
            }, {
                isLoading.onNext(false)
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    @SuppressLint("CheckResult")
    fun saveResultSession(
        data: MutableList<BluetoothResponse>,
        adapter: CoachSessionPractitionerAdapter
    ) {
        rxPostResultSuccess.onNext(transformToDataResultWhenServerError(data, adapter))
        val dataTransform = JSONArray(gson.toJson(data))
        val arrFail = mutableListOf<Int>()
        for (i in 0 until dataTransform.length()) {
            val dataBleObj: JSONObject = dataTransform.getJSONObject(i)
            if (dataBleObj.has("session_id")) {
                if (dataBleObj.has("lesson_id")) {
                    dataBleObj.put("practice_id", dataBleObj.getInt("lesson_id"))
                    dataBleObj.remove("lesson_id")
                }
                if (dataBleObj.has("data")) {
                    dataBleObj.remove("data")
                    if (data[i].data.isEmpty()) {
                        dataBleObj.put("data", "[]")
                        arrFail.add(i)
                    } else {
                        dataBleObj.put("data", gson.toJson(data[i].data.filterNotNull()))
                    }
                }
            } else {
                arrFail.add(i)
            }
        }
        arrFail.forEach {
            dataTransform.remove(it)
        }
        if (dataTransform.length() == 0) {
            rxMessage.onNext("Không có dữ liệu")
            return
        }
        isLoading.onNext(true)
        val offlineData = transformToDataResultWhenServerError(data, adapter)
        dataManager.saveResultSession(dataTransform.toString())
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                logErr("request: ${dataTransform.toString()}")
                logErr("response: $response")
                if (response.isSuccess()) {
                    try {
                        gson.fromJsonSafe<CoachSessionOldResponse>(response.dataObjectSafe())
                            ?.let {
                                if (it.result?.isNotEmpty() == true) {
                                    rxPostResultSuccess.onNext(it)
                                } else {
                                    rxPostResultSuccess.onNext(offlineData)
                                }
                            }
                            ?: rxPostResultSuccess.onNext(offlineData)
                    } catch (e: Exception) {
                        e.logErr()
                    }
                } else {
                    rxMessage.onNext(response.message())
                    rxPostResultSuccess.onNext(offlineData)
                }
            }, {
                isLoading.onNext(false)
                rxPostResultSuccess.onNext(offlineData)
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    private fun transformToDataResultWhenServerError(
        it: List<BluetoothResponse>,
        adapter: CoachSessionPractitionerAdapter
    ): CoachSessionOldResponse {
        val dataSessionDetailResponse = CoachSessionOldResponse()
        val dataResult = mutableListOf<CoachSessionResultResponse>()
        val mapResult = transformToDataResult(it, adapter).groupBy { groupBy -> groupBy.userId }
        adapter.getAllIds().map { idsUser ->
            var objLater: CoachSessionResultResponse? = null
            mapResult[idsUser]?.map { dataUserLarge ->
                if (objLater == null) objLater = dataUserLarge
                if (objLater?.score ?: 0 < dataUserLarge.score ?: 0) objLater =
                    dataUserLarge
            }
            if (objLater != null) {
                dataResult.add(objLater!!)
            }
        }
        dataResult.sortBy { it.score }
        dataResult.map { it.score = null }
        dataSessionDetailResponse.result = dataResult
        dataSessionDetailResponse.resultData = it.map { ble ->
            CoachSessionOldResponse.ItemCoachSessionResultData(
                gson.toJson(ble.data),
                ble.endTime,
                ble.machineId,
                ble.mode,
                ble.sessionId,
                ble.practiceId,
                ble.startTime1,
                ble.startTime2,
                ble.userId
            )
        }
        return dataSessionDetailResponse
    }

    private fun transformToDataResult(
        list: List<BluetoothResponse>,
        adapter: CoachSessionPractitionerAdapter
    ): MutableList<CoachSessionResultResponse> {
        val dataResult = list.map { ble ->
            var sum1 = 0
            var sum2 = 0
            var sum3 = 0
            var sum4 = 0
            var sum5 = 0
            var sum6 = 0
            var sum7 = 0
            var sum8 = 0
            var sum9 = 0
            var suma = 0
            var sumb = 0
            var totalScore = 0f
            try {
                for (data in ble.data) {
                    if (data != null) {
                        if (data.position == BlePosition.LEFT_CHEEK.key) sum1++
                        if (data.position == BlePosition.FACE.key) sum2++
                        if (data.position == BlePosition.RIGHT_CHEEK.key) sum3++
                        if (data.position == BlePosition.LEFT_CHEST.key) sum4++
                        if (data.position == BlePosition.RIGHT_CHEST.key) sum5++
                        if (data.position == BlePosition.ABDOMEN_UP.key) sum6++
                        if (data.position == BlePosition.LEFT_ABDOMEN.key) sum7++
                        if (data.position == BlePosition.ABDOMEN.key) sum8++
                        if (data.position == BlePosition.RIGHT_ABDOMEN.key) sum9++
                        if (data.position == BlePosition.LEFT_LEG.key) suma++
                        if (data.position == BlePosition.RIGHT_LEG.key) sumb++
                        totalScore += data.force ?: 0F
                    }
                }
            } catch (e: Exception) {
                e.logErr()
            }

            val user = adapter.findUserById(ble.userId ?: 0)
            CoachSessionResultResponse(
                sum1.toString(),
                sum2.toString(),
                sum3.toString(),
                sum4.toString(),
                sum5.toString(),
                sum6.toString(),
                sum7.toString(),
                sum8.toString(),
                sum9.toString(),
                suma.toString(),
                sumb.toString(),
                totalScore.toInt(),
                ble.userId,
                CoachSessionResultResponse.UserCreated(
                    ble.userId, user?.fullName, user?.avatarPath
                )
            )
        }.sortedBy { sort -> sort.score }
        return dataResult.toMutableList()
    }

}