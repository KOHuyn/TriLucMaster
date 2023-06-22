package com.mobileplus.dummytriluc.ui.main.coach.session

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.lifecycle.lifecycleScope
import com.core.BaseFragmentZ
import com.core.OnItemClick
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.ActionConnection
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.request.BleSessionRequest
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.ItemDisciple
import com.mobileplus.dummytriluc.data.request.session.CoachSessionCreateRequest
import com.mobileplus.dummytriluc.data.response.session.CoachSessionOldResponse
import com.mobileplus.dummytriluc.data.response.session.DataCoachSessionCreatedResponse
import com.mobileplus.dummytriluc.databinding.FragmentCoachSessionBinding
import com.mobileplus.dummytriluc.transceiver.ITransceiverController
import com.mobileplus.dummytriluc.transceiver.observer.IObserverMachine
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.coach.session.exercise.CoachSessionExerciseFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.callback.OnValueChangeListener
import com.mobileplus.dummytriluc.ui.main.coach.session.practitioner.CoachSessionPractitionerFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.result.CoachSessionResultFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.replaceViToEn
import com.utils.applyClickShrink
import com.utils.ext.*
import com.widget.ViewPagerAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.subjects.PublishSubject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.*

/**
 * Created by KOHuyn on 4/21/2021
 */
class CoachSessionFragment : BaseFragmentZ<FragmentCoachSessionBinding>(), IObserverMachine {
    override fun getLayoutBinding(): FragmentCoachSessionBinding =
        FragmentCoachSessionBinding.inflate(layoutInflater)

    private val vm by viewModel<CoachSessionViewModel>()

    private val fragExercise by lazy { CoachSessionExerciseFragment() }
    private val fragPractitioner by lazy { CoachSessionPractitionerFragment() }
    private val fragResult by lazy { CoachSessionResultFragment() }
    val rxState: PublishSubject<STATE_PLAY> = PublishSubject.create()
    private val gson by inject<Gson>()
    private val sessionId: Int? by lazy {
        val idSession by argument(ARG_SESSION_ID, AppConstants.INTEGER_DEFAULT)
        if (idSession == AppConstants.INTEGER_DEFAULT) null else idSession
    }
    val isContinue: Boolean by argument(ARG_CONTINUE, false)
    val isGuest: Boolean by argument(ARG_GUEST, false)

    var dataSessionDetailResponse: CoachSessionOldResponse = CoachSessionOldResponse()
    private val transceiver by lazy { ITransceiverController.getInstance() }


    override fun onEventMachineSendData(data: List<BluetoothResponse>) {
        lifecycleScope.launchWhenStarted {
            if (data.isNotEmpty()) {
                fragResult.setRound(if (fragExercise.adapter.itemCount != 0) (data.size / fragExercise.adapter.itemCount) else 0)
                if (statePlay != STATE_PLAY.RELOAD) {
                    fragResult.setTimeEndHeader(System.currentTimeMillis() / 1000)
                    statePlay = STATE_PLAY.REPLAY
                } else {
                    statePlay =
                        if (prevState == STATE_PLAY.PLAY || prevState == STATE_PLAY.REPLAY) {
                            STATE_PLAY.PAUSE
                        } else {
                            prevState
                        }
                }
                vm.saveResultSession(data.toMutableList(), fragPractitioner.adapter)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        transceiver.registerObserver(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        transceiver.removeObserver(this)
    }
    private fun actionLongWrite(command: String): Boolean {
        return if (activity is MainActivity) {
            (activity as MainActivity).actionWriteBle(command.plus("~"))
        } else {
            toast(loadStringRes(R.string.feature_not_available))
            false
        }
    }

    private fun actionWrite(command: String): Boolean {
        return if (activity is MainActivity) {
            (activity as MainActivity).actionWriteBle(command)
        } else {
            toast(loadStringRes(R.string.feature_not_available))
            false
        }
    }

    private val adapterViewPager by lazy {
        ViewPagerAdapter(
            childFragmentManager, listOf(fragExercise, fragPractitioner, fragResult),
            emptyList()
        )
    }

    companion object {
        private const val ARG_SESSION_ID = "ARG_SESSION_ID"
        private const val ARG_CONTINUE = "ARG_CONTINUE"
        private const val ARG_GUEST = "ARG_GUEST"
        fun openFragment() {
            postNormal(EventNextFragmentMain(CoachSessionFragment::class.java, true))
        }

        fun openFragmentFromSessionOld(idSession: Int, isContinue: Boolean = false) {
            val bundle = Bundle().apply {
                putInt(ARG_SESSION_ID, idSession)
                putBoolean(ARG_CONTINUE, isContinue)
            }
            postNormal(EventNextFragmentMain(CoachSessionFragment::class.java, bundle, true))
        }

        fun openFragmentGuest(idSession: Int) {
            val bundle = Bundle().apply {
                putInt(ARG_SESSION_ID, idSession)
                putBoolean(ARG_GUEST, true)
            }
            postNormal(EventNextFragmentMain(CoachSessionFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        if (sessionId != null) {
            vm.getDetailSessionOld(sessionId!!)
        }
        setupViewPager()
        handleClick()
        setupLayoutControl()
    }

    private fun setRole() {
        val isDisableEditData = isGuest || isContinue
        fragExercise.adapter.isEnableChangeData = !isDisableEditData
        fragPractitioner.adapter.isEnableChangeData = !isDisableEditData
        fragPractitioner.adapter.isClickableItem = !isGuest
        fragExercise.setShowViewControl(!isGuest)
        fragPractitioner.setShowViewControl(!isGuest)
        fragResult.showTextDescriptionSwipeRefresh(!isGuest)
    }

    private fun setupLayoutControl() {
        var dataExercise = false
        var dataPractitioner = false
        val rxShowLayoutControl: PublishSubject<Boolean> = PublishSubject.create()

        fragPractitioner.adapter.onValueChangeListener = OnValueChangeListener { isNotEmpty ->
            dataPractitioner = isNotEmpty
            rxShowLayoutControl.onNext(dataExercise && dataPractitioner)
        }
        addDispose(fragExercise.adapter.onValueChangeListener.subscribe { isNotEmpty ->
            dataExercise = isNotEmpty
            rxShowLayoutControl.onNext(dataExercise && dataPractitioner)
        })
        addDispose(rxShowLayoutControl.subscribe {
            if (!isGuest) {
                if (it) {
                    if (statePlay == STATE_PLAY.PREPARE) {
                        statePlay = if (isContinue) STATE_PLAY.PAUSE else
                            STATE_PLAY.PLAY
                    }
                } else statePlay = STATE_PLAY.PREPARE
            }
            logErr("statePlay: $it - dataExercise:$dataExercise - dataPractitioner:$dataPractitioner")
        })
    }

    private fun disposableViewModel() {
        addDispose(vm.rxCreateSession.subscribe { response ->
            statePlay = STATE_PLAY.PAUSE
            playPractice(response)
        })
        addDispose(vm.rxGetSessionOld.subscribe { data ->
            dataSessionDetailResponse = data
            fragPractitioner.groupId = data.classId
            parseItemsForThreeScreen(data)
        })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.isLoading.subscribe { if (it) showDialog() else hideDialog() })
        addDispose(vm.rxPostResultSuccess.subscribe { data ->
            fragResult.adapter.items = data.result?.toMutableList() ?: mutableListOf()
            dataSessionDetailResponse.resultData = data.resultData
            dataSessionDetailResponse.result = data.result
        })
    }

    private fun String.cutName(): String {
        return try {
            this.substringAfterLast(" ")
        } catch (e: Exception) {
            this
        }
    }

    private fun playPractice(data: DataCoachSessionCreatedResponse) {
        fragResult.setTimeStartHeader(System.currentTimeMillis() / 1000)
        val requestBle by lazy { BleSessionRequest() }

        requestBle.id = data.id
        requestBle.arrDisciple =
            fragPractitioner.adapter.items.map {
                BleSessionRequest.BleArrDisciple(
                    it.studentId,
                    (it.fullName ?: "---").cutName().replaceViToEn()
                )
            }.toMutableList()
        requestBle.userCount = fragPractitioner.adapter.itemCount
        requestBle.lessonCount = fragExercise.adapter.itemCount
        requestBle.lessonData = data.practiceDetail.map {
            val mode = when (it.id) {
                1 -> 2
                2 -> 3
                else -> 4
            }
            BleSessionRequest.BleLessonDataOfSession(
                mode,
                it.id,
                if (mode != 4) 1 else it.round,
                if (mode != 4) ((it.round ?: 1) * 30) else null,
                it.transformToArrPosString(gson),
            )
        }.toMutableList()
        actionLongWrite(gson.toJson(requestBle))
        logErr(gson.toJson(requestBle))
    }

    private fun parseItemsForThreeScreen(data: CoachSessionOldResponse) {
        GlobalScope.launch(Dispatchers.Main) {
            fragResult.setTimeStartHeader(
                DateTimeUtil.convertDateToTimeStampServer(
                    data.createdAt ?: "", "yyyy-MM-dd HH:mm:ss"
                )
            )
            fragResult.setTimeEndHeader(
                DateTimeUtil.convertDateToTimeStampServer(
                    data.updatedAt ?: "", "yyyy-MM-dd HH:mm:ss"
                )
            )
            fragResult.setRound(data.result?.size ?: 0)
            fragPractitioner.adapter.items =
                data.usersDetail?.map {
                    ItemDisciple(
                        studentId = it.id,
                        avatarPath = it.avatarPath,
                        fullName = it.fullName
                    )
                }?.toMutableList() ?: mutableListOf()

            fragExercise.adapter.items = data.practicesDetail?.map {
                ItemCoachPractice(
                    id = it.id,
                    imagePath = it.imagePath,
                    title = it.title,
                    countRepeat = it.round ?: 1
                )
            }?.toMutableList() ?: mutableListOf()
            fragResult.adapter.items = data.result?.toMutableList() ?: mutableListOf()
        }
    }

    private fun createSession() {
        fragResult.adapter.clearData()
        val idsPractice = fragExercise.adapter.getIdsRound()
            .map { CoachSessionCreateRequest.PracticeIdsRound(it.first, it.second) }
        val idsDisciple = fragPractitioner.adapter.getAllIds()

        val getDate = DateTimeUtil.convertTimeStampToDate(
            Calendar.getInstance().time.time / 1000,
            "dd/MM/yyyy"
        )
        val getTime = DateTimeUtil.convertTimeStampToDate(
            Calendar.getInstance().time.time / 1000,
            "HH:mm"
        )
        vm.createSession(
            CoachSessionCreateRequest(
                String.format(getString(R.string.title_session_in_date), getTime, getDate),
                idsPractice,
                idsDisciple,
                fragPractitioner.groupId
            )
        )
    }

    fun playSession() {
        if (fragExercise.adapter.itemCount > 3 || fragPractitioner.adapter.itemCount > 10) {
            val errorMsg = mutableListOf<String>()
            if (fragExercise.adapter.itemCount > 3) {
                errorMsg.add(getString(R.string.require_no_more_than_3_exercise))
            }
            if (fragPractitioner.adapter.itemCount > 10) {
                errorMsg.add(getString(R.string.require_no_more_than_10_people))
            }
            toast(errorMsg.joinToString("\n"))
        } else {
            createSession()
        }
    }

    private fun handleClick() {
        binding.btnPlaySessionCoach.applyClickShrink()
        binding.btnPlayPrevSessionCoach.applyClickShrink()
        binding.btnPlayNextSessionCoach.applyClickShrink()
        binding.btnBackCoachSession.clickWithDebounce { onBackPressed() }
        binding.btnPlaySessionCoach.clickWithDebounce(1000) {
            if (statePlay == STATE_PLAY.RELOAD) statePlay = prevState
            when (statePlay) {
                STATE_PLAY.PLAY, STATE_PLAY.REPLAY -> {
                    if ((activity as MainActivity).isConnectedBle) {
                        playSession()
                    } else {
                        (activity as MainActivity).showDialogRequestConnect()
                    }
                }
                STATE_PLAY.PAUSE -> {
                    actionLongWrite(BleSessionRequest.ActionChange.PAUSE.writeBle())
                    statePlay = STATE_PLAY.RESUME
                }
                STATE_PLAY.RESUME -> {
                    actionLongWrite(BleSessionRequest.ActionChange.RESUME.writeBle())
                    statePlay = STATE_PLAY.PAUSE
                }
                else -> {
                }
            }
        }
        binding.btnPlayEndSessionCoach.clickWithDebounce {
            YesNoButtonDialog()
                .setTitle(getString(R.string.session))
                .setMessage(getString(R.string.question_quit_session))
                .setTextAccept(getString(R.string.yes))
                .setTextCancel(getString(R.string.no))
                .setDismissWhenClick(true)
                .setOnCallbackAcceptButtonListener {
                    actionLongWrite(BleSessionRequest.ActionChange.END.writeBle())
                    statePlay = STATE_PLAY.REPLAY
                }
                .showDialog(parentFragmentManager)
        }
        binding.btnPlayPrevSessionCoach.clickWithDebounce {
            actionLongWrite(BleSessionRequest.ActionChange.PREV.writeBle())
        }
        binding.btnPlayNextSessionCoach.clickWithDebounce {
            actionLongWrite(BleSessionRequest.ActionChange.NEXT.writeBle())
        }


        fragResult.onRefresh = CoachSessionResultFragment.OnRefresh {
//            if (statePlay == STATE_PLAY.PAUSE || statePlay == STATE_PLAY.RESUME || statePlay == STATE_PLAY.RELOAD) {
            if (statePlay == STATE_PLAY.RESUME || statePlay == STATE_PLAY.RELOAD) {
                if (statePlay != STATE_PLAY.RELOAD) {
                    prevState = statePlay
                }
                statePlay = STATE_PLAY.RELOAD
                logErr("REFRESH")
                actionLongWrite(BleSessionRequest.ActionChange.RELOAD.writeBle())
            } else {
                toast(getString(R.string.you_need_to_press_pause_to_be_able_to_use_this_mechanism))
            }
            fragResult.setRefresh(false)
        }
        fragPractitioner.adapter.onClickItem = OnItemClick { item, position ->
            if (item.studentId != null) {
                YesNoButtonDialog()
                    .setTitle(getString(R.string.session))
                    .setMessage(
                        String.format(
                            getString(R.string.do_you_want_fight_first),
                            item.fullName
                        )
                    )
                    .setTextAccept(getString(R.string.yes))
                    .setTextCancel(getString(R.string.no))
                    .setDismissWhenClick(true)
                    .setOnCallbackAcceptButtonListener {
                        actionLongWrite("""{"action":6,"user_id":${item.studentId}}""")
                        toast(
                            String.format(
                                getString(R.string.was_pioneered_in_the_episode),
                                item.fullName
                            )
                        )
                    }
                    .showDialog(parentFragmentManager)
            }
        }
    }

    private fun setupViewPager() {
        binding.viewPagerCoachSession.run {
            adapter = adapterViewPager
            offscreenPageLimit = adapterViewPager.count
            post { setRole() }
        }
        setupSwitchTab()
    }

    private fun setupSwitchTab() {
        binding.rgCoachSession.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.rbCoachSessionExercise -> {
                    binding.viewPagerCoachSession.currentItem = 0
                }
                R.id.rbCoachSessionPractitioner -> {
                    binding.viewPagerCoachSession.currentItem = 1
                }
                R.id.rbCoachSessionResult -> {
                    binding.viewPagerCoachSession.currentItem = 2
                }
            }
        }
        binding.viewPagerCoachSession.onPageSelected { pos ->
            (binding.rgCoachSession.getChildAt(pos ?: 0) as RadioButton).isChecked = true
        }
    }

    val arrTitleCourse: MutableList<Pair<Int?, String?>>
        get() = fragExercise.adapter.items.map { Pair(it.id, it.title) }.toMutableList()

    enum class STATE_PLAY { PREPARE, PLAY, PAUSE, RESUME, REPLAY, END, RELOAD }

    fun isDoingSessionPractice(): Boolean =
        statePlay != STATE_PLAY.PLAY && statePlay != STATE_PLAY.REPLAY && statePlay != STATE_PLAY.PREPARE

    private var prevState: STATE_PLAY = STATE_PLAY.PREPARE
    private var statePlay: STATE_PLAY = STATE_PLAY.PREPARE
        set(value) {
            rxState.onNext(value)
            vm.isContinueSession = (value != STATE_PLAY.REPLAY || value != STATE_PLAY.PLAY)
            logErr("STATE:${field.name}")
            val layoutControl by lazy { binding.layoutControlSessionCoach }
            val btnControlPlay by lazy { binding.btnPlaySessionCoach }
            val btnPrev by lazy { binding.btnPlayPrevSessionCoach }
            val btnNext by lazy { binding.btnPlayNextSessionCoach }
            val btnStop by lazy { binding.btnPlayEndSessionCoach }
            field = value
            binding.btnBackCoachSession?.setVisibility(!isDoingSessionPractice())
            fragResult.showTextDescriptionSwipeRefresh(isDoingSessionPractice())
            btnNext.isEnabled = value == STATE_PLAY.PAUSE
            layoutControl.setVisibility(value != STATE_PLAY.PREPARE)
            when (value) {
                STATE_PLAY.PLAY -> {
                    btnControlPlay.text = loadStringRes(R.string.start)
                    btnPrev.hide()
                    btnNext.hide()
                    btnStop.hide()
                }
                STATE_PLAY.PAUSE -> {
                    btnControlPlay.text = loadStringRes(R.string.resume)
                    btnPrev.show()
                    btnNext.show()
                    btnStop.hide()
                }
                STATE_PLAY.RESUME -> {
                    btnControlPlay.text = loadStringRes(R.string.label_continue)
                    btnPrev.hide()
                    btnNext.invisible()
                    btnStop.show()
                }
                STATE_PLAY.REPLAY -> {
                    btnControlPlay.text = loadStringRes(R.string.fight_back)
                    btnPrev.hide()
                    btnNext.hide()
                    btnStop.hide()
                }
                else -> {
                }
            }
        }
}