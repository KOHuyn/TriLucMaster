package com.mobileplus.dummytriluc.ui.main

import com.core.BaseViewModel
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.bluetooth.request.BleErrorRequest
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.request.TransferBluetoothData
import com.mobileplus.dummytriluc.data.DataManager
import com.mobileplus.dummytriluc.data.model.MachineInfo
import com.mobileplus.dummytriluc.data.model.entity.DataBluetoothRetryEntity
import com.mobileplus.dummytriluc.data.model.entity.TableConfig
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.SubmitModeFreedomPractice
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.TypeCoach
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.SchedulerProvider
import com.utils.ext.toList
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.subjects.PublishSubject
import org.json.JSONArray
import org.json.JSONObject

class MainViewModel(
    dataManager: DataManager,
    schedulerProvider: SchedulerProvider,
    private val gson: Gson
) :
    BaseViewModel<DataManager>(dataManager, schedulerProvider) {

    private val compositeDisposable = CompositeDisposable()

    private var errorBle: BleErrorRequest = BleErrorRequest()

    val logoutSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val rxCoachType: PublishSubject<Pair<TypeCoach, JsonObject?>> = PublishSubject.create()
    val user = dataManager.getUserInfo()
    val hotline = dataManager.numberHotLine
    val rxPostModeFreedomSuccess: PublishSubject<Boolean> = PublishSubject.create()
    val rxShowPopupUpdateVersionApp: PublishSubject<Triple<String, Boolean, String?>> =
        PublishSubject.create()
    val rxMachineInfo: PublishSubject<MachineInfo> = PublishSubject.create()
    val rxForceConnect: PublishSubject<String> = PublishSubject.create()

    var isDataSecurity: Boolean
        set(value) {
            dataManager.isDataSecurityBle = value
        }
        get() = dataManager.isDataSecurityBle

    var versionUpdateApp: String
        set(value) {
            dataManager.versionUpdateApp = value
        }
        get() = dataManager.versionUpdateApp

    val machineInfoCache: MachineInfo? get() = dataManager.machineCodeConnectLasted

    fun logout(): Disposable {
        isLoading.onNext(true)
        return dataManager.logoutServer(uuid = deviceId)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({
                isLoading.onNext(false)
                logoutSuccess.onNext(true)
                dataManager.logout()
            }, {
                isLoading.onNext(false)
                logoutSuccess.onNext(true)
                dataManager.logout()
                it.logErr()
            })
    }
    fun readNotification(id: Int): Disposable {
        return dataManager.getNotificationDetail(id)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe()
    }

    fun checkScreenCoach(): Disposable {
        isLoading.onNext(true)
        return dataManager.checkMaster()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    when (response.dataObject().get(ApiConstants.IS_MASTER).asString) {
                        TypeCoach.MEMBER.value -> {
                            rxCoachType.onNext(Pair(TypeCoach.MEMBER, response.dataObject()))
                        }
                        TypeCoach.REVIEW.value -> {
                            rxCoachType.onNext(Pair(TypeCoach.REVIEW, response.dataObject()))
                        }
                        TypeCoach.MASTER.value -> {
                            rxCoachType.onNext(Pair(TypeCoach.MASTER, response.dataObject()))
                        }
                        else -> {
                            rxCoachType.onNext(Pair(TypeCoach.UNKNOWN, null))
                        }
                    }
                } else {
                    rxCoachType.onNext(Pair(TypeCoach.UNKNOWN, null))
                }
            }, {
                isLoading.onNext(false)
                rxCoachType.onNext(Pair(TypeCoach.UNKNOWN, null))
                rxMessage.onNext(it.getErrorMsg())
            })
    }

    fun getConfigApp(): Disposable {
        return dataManager.getConfig()
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                if (response.isSuccess()) {
                    try {
                        val listConfig: List<TableConfig> =
                            gson.toList(response.dataArray())
//                        saveConfigApp(listConfig)
                        checkVersionApp(listConfig)
                    } catch (e: Exception) {
                        e.logErr()
                    }
                }
            }, {
                it.logErr()
            })
    }

    private fun checkVersionApp(arrConfig: List<TableConfig>) {
        var lastedVersionName: String? = null
        var minVersionName: String? = null
        var contentVersion: String? = null
        arrConfig.forEach { config ->
            when (config.name) {
                "lastedVersionAndroid" -> lastedVersionName = config.value
                "minVersionAndroid" -> minVersionName = config.value
                "contentVersionAndroid" -> contentVersion = config.value
                "hotLineNumber" -> {
                    config.value?.let { dataManager.numberHotLine = it }
                }
            }
        }
        if (lastedVersionName == null || minVersionName == null) return
        val lastedVersionCode = lastedVersionName.transformVersionCode()
        val minVersionCode = minVersionName.transformVersionCode()
        logErr("lastedVersionName:$lastedVersionName lastedVersionCode:$lastedVersionCode minVersionName:$minVersionName minVersionCode:$minVersionCode")
        if (lastedVersionCode == BuildConfig.VERSION_CODE || lastedVersionName == BuildConfig.VERSION_NAME)
            return
        else {
            if (lastedVersionCode > minVersionCode) {
                if (versionUpdateApp == lastedVersionName) return
            }
            if (lastedVersionCode < BuildConfig.VERSION_CODE) return

            rxShowPopupUpdateVersionApp.onNext(
                Triple(
                    lastedVersionName ?: BuildConfig.VERSION_NAME,
                    BuildConfig.VERSION_CODE < minVersionCode,
                    contentVersion
                )
            )
        }
    }

    private fun String?.transformVersionCode(): Int {
        if (this == null) return 0
        val arrVersion = split(".").map { it.replaceStringToIntOrNull() }
        val versionMajor = (arrVersion.getOrNull(0) ?: 0) * 1000000
        val versionMinor = (arrVersion.getOrNull(1) ?: 0) * 1000
        val versionPatch = (arrVersion.getOrNull(2) ?: 0) * 100
        return versionMajor + versionMinor + versionPatch
    }

    private fun saveConfigApp(arrConfig: List<TableConfig>): Disposable {
        return dataManager.saveConfigs(arrConfig)
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({
                logErr("Save Config success")
            }, {
                it.logErr()
            })
    }

    fun logErrorBleMachineJSON(content: String) {
        if (content.isBlank()) return
        if (content.contains(AppConstants.SUCCESS_BLE)) return
        try {
            val idMachine =
                content.substringAfter("\"machine_id\":").substring(0, 5).replaceStringToIntOrNull()
            errorBle.machineId = idMachine
        } catch (e: Exception) {
            e.logErr()
        }
        errorBle.content = content
        dataManager.logErrorMachine(errorBle)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({
                logErr(it.toString())
            }, {
                logErr(it.getErrorMsg())
                saveDataJSONErrorWhenPushServerFail(errorBle)
            }).addTo(compositeDisposable)
    }
    private fun saveDataJSONErrorWhenPushServerFail(data: BleErrorRequest) {
        dataManager.saveBluetoothDataError(data)
            .compose(schedulerProvider.ioToMainObservableScheduler())
            .subscribe({
                logErr("saveDataJSONErrorWhenPushServerFail:$data")
            }, {
                logErr(it.getErrorMsg())
            }).addTo(compositeDisposable)
    }

    fun connectByMachineCode(machineCode: String) {
        isLoading.onNext(true)
        dataManager.connectMachine(machineCode)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val data = gson.fromJsonSafe<MachineInfo>(response.dataObject())
                    if (data != null) {
                        rxMachineInfo.onNext(data)
                    }
                } else {
                    if (response.code() == 401) {
                        rxForceConnect.onNext(machineCode)
                    } else {
                        rxMessage.onNext(response.message())
                    }
                }
            }, { error ->
                isLoading.onNext(false)
                rxMessage.onNext(error.getErrorMsg())
            }).addTo(compositeDisposable)
    }

    override fun onCleared() {
        compositeDisposable.clear()
        super.onCleared()
    }

    fun forceConnect(machineEncode: String) {
        isLoading.onNext(true)
        dataManager.forceConnectMachine(machineEncode)
            .compose(schedulerProvider.ioToMainSingleScheduler())
            .subscribe({ response ->
                isLoading.onNext(false)
                if (response.isSuccess()) {
                    val data = gson.fromJsonSafe<MachineInfo>(response.dataObject())
                    if (data != null) {
                        rxMachineInfo.onNext(data)
                    }
                } else {
                    rxMessage.onNext(response.message())
                }
            }, { error ->
                isLoading.onNext(false)
                rxMessage.onNext(error.getErrorMsg())
            }).addTo(compositeDisposable)
    }
}