package com.mobileplus.dummytriluc.ui.main.practice.test

import android.os.Bundle
import android.os.CountDownTimer
import android.os.Handler
import android.os.Looper
import com.core.BaseFragment
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.CommandBle
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.data.response.PracticeAvgResponse
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.command.FinishCommand
import com.mobileplus.dummytriluc.transceiver.command.ICommand
import com.mobileplus.dummytriluc.transceiver.command.LessonCommand
import com.mobileplus.dummytriluc.transceiver.command.PracticeFreePunchCommand
import com.mobileplus.dummytriluc.transceiver.command.PracticeLedPunchCommand
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.practice.dialog.ConfirmPracticeTestDialog
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadPracticeItem
import com.mobileplus.dummytriluc.ui.utils.extensions.fillGradientPrimary
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.test.MODE_COURSE_TEST
import com.mobileplus.dummytriluc.ui.utils.test.MODE_FREE_TEST
import com.mobileplus.dummytriluc.ui.utils.test.testNextResponseBluetooth
import com.utils.applyClickShrink
import com.utils.ext.*
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_practice_test.*
import org.json.JSONObject
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit
import kotlin.math.abs

class PracticeTestFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_practice_test
    private val testViewModel by viewModel<PracticeTestViewModel>()

    private val transceiver by lazy { ITransceiverController.getInstance() }
    private fun commandRequestBle(command: ICommand): Boolean {
        transceiver.send(command)
        return true
    }

    private lateinit var countDownStart: CountDownTimer
    private val rxRequestPostFragment: PublishSubject<Boolean> = PublishSubject.create()
    private var dataRequest: List<BluetoothResponse> = emptyList()
    private var dataDetail: DetailPracticeResponse? = null
    private var choiceLevelPractice: LevelPractice? = null
    private val gson by inject<Gson>()
    var positionArr: String = ""
    var delayPositionArr: String = ""
    private var countTimesDisposable: Disposable? = null
    private var isRetry = false
        set(value) {
            field = value
            btnBackPracticeTest.setVisibility(value)
            if (field) {
                btnActionPracticeTest.text = loadStringRes(R.string.retry)
            } else {
                btnActionPracticeTest.text = loadStringRes(R.string.end)
            }
        }
    private val typeRecord: Int by argument(ARG_TYPE_RECORD, AppConstants.INTEGER_DEFAULT)
    private val idPractice: Int by argument(ARG_PRACTICE_ID, AppConstants.INTEGER_DEFAULT)

    private var avgResponse: PracticeAvgResponse? = null

    fun isDoingPractice(): Boolean = !isRetry

    companion object {
        private const val KEY_DATA_LESSON_ARG_TEST = "KEY_DATA_LESSON_ARG_TEST"
        private const val KEY_LEVEL_PRACTICE = "KEY_LEVEL_PRACTICE"
        private const val ARG_TYPE_RECORD = "ARG_TYPE_RECORD"
        private const val ARG_PRACTICE_ID = "ARG_PRACTICE_ID"
        private const val TYPE_COURSE = 1
        private const val TYPE_FREE_FIGHT = 2
        private const val TYPE_ACCORDING_TO_LED = 3
        fun openFragmentMode4(
            request: DetailPracticeResponse,
            choiceLevel: LevelPractice?,
            gson: Gson
        ) {
            val bundle = Bundle().apply {
                putInt(ARG_TYPE_RECORD, TYPE_COURSE)
                putInt(ARG_PRACTICE_ID, request.id ?: AppConstants.INTEGER_DEFAULT)
                putString(
                    KEY_DATA_LESSON_ARG_TEST,
                    gson.toJson(request)
                )
                if (choiceLevel != null) {
                    putString(KEY_LEVEL_PRACTICE, gson.toJson(choiceLevel))
                }
            }
            postNormal(
                EventNextFragmentMain(PracticeTestFragment::class.java, bundle, true)
            )
        }

        fun openFragmentFreeFight() {
            val bundle = Bundle().apply {
                putInt(ARG_TYPE_RECORD, TYPE_FREE_FIGHT)
                putInt(ARG_PRACTICE_ID, ApiConstants.ID_MODE_FREE_PUNCH)
            }
            postNormal(
                EventNextFragmentMain(PracticeTestFragment::class.java, bundle, true)
            )
        }

        fun openFragmentAccordingToLed() {
            val bundle = Bundle().apply {
                putInt(ARG_TYPE_RECORD, TYPE_ACCORDING_TO_LED)
                putInt(ARG_PRACTICE_ID, ApiConstants.ID_MODE_LED)
            }
            postNormal(
                EventNextFragmentMain(PracticeTestFragment::class.java, bundle, true)
            )
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
//        Handler(Looper.getMainLooper()).postDelayed({ fakeDataBleMode4() }, 3000)
        disposableSubscribe()
        checkArg()
        getAvg()
        handleClick()
    }

    private fun getAvg() {
        testViewModel.getAvgPractice(idPractice)
    }

    private fun handleClick() {
        btnActionPracticeTest.applyClickShrink()
        btnActionPracticeTest.fillGradientPrimary()
        btnActionPracticeTest.clickWithDebounce {
            if (isRetry) {
                startPractice()
            } else {
                commandRequestBle(FinishCommand)
                countTimesDisposable?.dispose()
                countTimesDisposable = null
                isRetry = true
            }
        }

        btnBackPracticeTest.clickWithDebounce {
            onBackPressed()
        }
    }


    private fun fakeDataBleMode4() {
        (activity as MainActivity).testNextResponseBluetooth(MODE_COURSE_TEST)
    }

    fun startPractice() {
        isRetry = false
        try {
            countDownStart = object : CountDownTimer(3200, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    btnActionPracticeTest?.hide()
                    if (millisUntilFinished > 1000) {
                        txtTimeCountPracticeTest?.text = (millisUntilFinished / 1000).toString()
                    }
                }

                override fun onFinish() {
                    txtTimeCountPracticeTest?.text = "00:00s"
                    btnActionPracticeTest?.show()
                    startRecord()
                    var count = 0
                    Observable.timer(1000, TimeUnit.MILLISECONDS)
                        .repeat()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe {
                            count++
                            val s =
                                if (count % 60 < 10) "0${count % 60}" else "${count % 60}"
                            val m =
                                if (count / 60 < 10) "0${count / 60}" else "${count / 60}"
                            txtTimeCountPracticeTest?.text = String.format("%s:%ss", m, s)
                        }.let { countTimesDisposable = it }
                }
            }
            countDownStart.start()
        } catch (e: Exception) {
            e.logErr()
        }
    }

    private fun startRecord(): Boolean {
        val timeL = System.currentTimeMillis() / 1000
        return when (typeRecord) {
            TYPE_COURSE -> {
                if (positionArr.isNotEmpty()) {
                    commandRequestBle(
                        LessonCommand(
                            lessonId = listOf(dataDetail?.id ?: -1),
                            round = 2,
                            fullName = "",
                            level = JSONObject().apply {
                                put("name", "level1")
                                put("value", 0.75)
                            }, avgPower = avgResponse?.avgPower ?: 50,
                            avgHit = avgResponse?.avgHit ?: 20
                        )
//                                String . format (
//                                CommandBle.LESSON_MODE,
//                        timeL,
//                        dataDetail?.id ?: -1,
//                        positionArr,
//                        delayPositionArr,
//                        avgResponse?.avgPower ?: 0,
//                        avgResponse?.avgHit ?: 0,
//                    )
                    )
                } else false
            }
            TYPE_FREE_FIGHT -> {
                commandRequestBle(
                    PracticeFreePunchCommand(
                        avgPower = avgResponse?.avgPower ?: 50,
                        avgHit = avgResponse?.avgHit ?: 50
                    )
                )
            }
            TYPE_ACCORDING_TO_LED -> {
                commandRequestBle(
                    PracticeLedPunchCommand(
                        avgPower = avgResponse?.avgPower ?: 50,
                        avgHit = avgResponse?.avgHit ?: 50
                    )
                )
            }
            else -> false
        }
    }

    private fun checkArg() {
        if (arguments != null && arguments?.containsKey(KEY_DATA_LESSON_ARG_TEST) == true) {
            try {
                dataDetail =
                    gson.fromJson(
                        argument(KEY_DATA_LESSON_ARG_TEST, "").value,
                        DetailPracticeResponse::class.java
                    )
                if (arguments?.containsKey(KEY_LEVEL_PRACTICE) == true) {
                    choiceLevelPractice = gson.fromJson(
                        argument(KEY_LEVEL_PRACTICE, "").value,
                        LevelPractice::class.java
                    )
                }
                val arrBle: List<DataBluetooth>? = dataDetail!!.data?.let { gson.toList(it) }
                val arrBlePunch = arrBle?.sortedBy { it.time }
                if (arrBlePunch != null) {
                    for (i in arrBlePunch.indices) {
                        positionArr += arrBlePunch[i].position
                        if (arrBlePunch[i].time != null) {
                            val delayTimer = if (i == 0) {
                                if (dataDetail!!.startTime2 != null)
                                    abs(arrBlePunch[i].time!! - dataDetail!!.startTime2!!) else 0L
                            } else {
                                abs(arrBlePunch[i].time!! - arrBlePunch[i - 1].time!!)
                            }
                            if (choiceLevelPractice != null) {
                                val delayTimeLevel =
                                    if (choiceLevelPractice!!.value != null) {
                                        delayTimer * (200 - choiceLevelPractice!!.value!!) / 100
                                    } else delayTimer
                                delayPositionArr += "-$delayTimeLevel"
                            } else {
                                delayPositionArr += "-$delayTimer"
                            }
                        }
                    }
                }
                logErr("positions:$positionArr-delay:$delayPositionArr")
            } catch (e: JsonSyntaxException) {
                e.logErr()
                Handler(Looper.getMainLooper()).postDelayed({
                    onBackPressed()
                }, 500)
                toast(loadStringRes(R.string.data_practice_not_available))
            }
        }
    }


    private fun disposableSubscribe() {
        addDispose(testViewModel.rxAvgResponse.subscribe {
            val (isSuccess, avg) = it
            avgResponse = avg
            startPractice()
        })
        addDispose(rxRequestPostFragment.subscribe {
            if (dataRequest.isNotEmpty()) {
                if (typeRecord == TYPE_COURSE) {
                    testViewModel.submit(dataRequest, choiceLevelPractice)
                } else {
                    isRetry = true
                }
                countTimesDisposable?.dispose()
                countTimesDisposable = null
            } else {
                toast(getString(R.string.you_are_not_practing))
                onBackPressed()
            }
        })
        addDispose(
            testViewModel.isLoading.observeOn(AndroidSchedulers.mainThread())
                .subscribe { if (it) showDialog() else hideDialog() })
        addDispose(testViewModel.rxMessage.subscribe { toast(it) })
        addDispose(testViewModel.isSuccessSubmit.subscribe {
            val isSuccess: Boolean = it.first
            val data = it.second
            if (isSuccess) {
                postNormal(EventReloadPracticeItem())
                toast(getString(R.string.upload_practice_success))
            }
            isRetry = true
            ConfirmPracticeTestDialog.Builder(dataRequest)
                .setTitle(dataDetail?.title ?: "")
                .setCancelable(false).apply {
                    dataDetail?.data?.let { setDataSample(gson.toList(dataDetail?.data ?: "[]")) }
                }.setLevelPractice(choiceLevelPractice)
                .setDataSubmit(data)
                .build(parentFragmentManager)
                .setSubmitPracticeCallback { isSubmit ->
                    if (isSubmit) {
//                        onBackPressed()
                    } else {
                        startPractice()
                    }
                }
        })
        addDispose(
            (requireActivity() as MainActivity).rxCallbackDataBle.observeOn(AndroidSchedulers.mainThread())
                .subscribe {
                    val isSuccess = it.first
                    val data = it.second
                    if (isSuccess) {
                        if (data.isNotEmpty()) {
                            dataRequest = data
                            rxRequestPostFragment.onNext(true)
                        }
                    } else {
                        toast(getString(R.string.please_try_again))
                    }
//            commandRequestBle(CommandBle.END)
                })
    }

    override fun onDestroy() {
        countTimesDisposable?.dispose()
        if (this::countDownStart.isInitialized) countDownStart.cancel()
        super.onDestroy()
    }
}