package com.mobileplus.dummytriluc.ui.main.practice.detail

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.core.BaseFragmentZ
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.model.ChatType
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.ChatSendRequest
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.data.response.MediaPractice
import com.mobileplus.dummytriluc.databinding.FragmentPracticeDetailBinding
import com.mobileplus.dummytriluc.transceiver.command.MachineFreePunchCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineLedPunchCommand
import com.mobileplus.dummytriluc.transceiver.command.MachineLessonCommand
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.practice.adapter.WrapPagerAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.adapter.LevelPracticeAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.info.PracticeDetailInfoBottomFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.info.PracticeDetailInfoTopFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.main.PracticeDetailMainBottomFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.main.PracticeDetailMainTopFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.question.PracticeDetailQuestionBottomFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.question.PracticeDetailQuestionTopFragment
import com.mobileplus.dummytriluc.ui.main.practice.test.PracticeTestFragment
import com.mobileplus.dummytriluc.ui.main.practice.test.dialog.SelectMethodPracticeDialog
import com.mobileplus.dummytriluc.ui.main.practice.video.PracticeWithVideoFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadPracticeItem
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventSendVideoPractice
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.video.record.VideoRecordFragment
import com.utils.ext.*
import com.widget.ViewPagerAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit
import kotlin.math.abs


class PracticeDetailFragment : BaseFragmentZ<FragmentPracticeDetailBinding>() {
    override fun getLayoutBinding(): FragmentPracticeDetailBinding =
        FragmentPracticeDetailBinding.inflate(layoutInflater)

    private val practiceDetailViewModel by viewModel<PracticeDetailViewModel>()
    private var dataDetailPracticeResponse: DetailPracticeResponse? = null
    private var roundNumber: Int = 1
    private val gson by inject<Gson>()
    private var isEmptyPractices = false
    private val roundNumberAdapter by lazy { LevelPracticeAdapter() }
    private val mainTopFragment by lazy { PracticeDetailMainTopFragment() }
    private val mainBottomFragment by lazy { PracticeDetailMainBottomFragment() }
    private val informationTopFragment by lazy { PracticeDetailInfoTopFragment() }
    private val informationBottomFragment by lazy { PracticeDetailInfoBottomFragment() }
    private val questionTopFragment by lazy { PracticeDetailQuestionTopFragment() }
    private val questionBottomFragment by lazy { PracticeDetailQuestionBottomFragment() }
    val isFreedom: Boolean
        get() = idLessonPractice == ApiConstants.ID_MODE_FREE_PUNCH
                || idLessonPractice == ApiConstants.ID_MODE_LED
    private val adapterTop by lazy {
        WrapPagerAdapter(
            childFragmentManager, listOf<Fragment>(
                mainTopFragment,
                informationTopFragment,
                questionTopFragment
            ), listOf(
                loadStringRes(R.string.title_practice),
                loadStringRes(R.string.information_sort),
                loadStringRes(R.string.title_question_answer)
            )
        )
    }
    private val adapterBottom by lazy {
        ViewPagerAdapter(
            childFragmentManager, listOf<Fragment>(
                mainBottomFragment,
                informationBottomFragment,
                questionBottomFragment
            ), listOf(
                loadStringRes(R.string.title_practice),
                loadStringRes(R.string.title_information),
                loadStringRes(R.string.title_question_answer)
            )
        )
    }

    companion object {
        private const val ID_PRACTICE_LESSON_ARG = "ID_PRACTICE_LESSON_ARG"
        fun openFragment(id: Int) {
            val bundle = Bundle().apply {
                putInt(ID_PRACTICE_LESSON_ARG, id)
            }
            postNormal(EventNextFragmentMain(PracticeDetailFragment::class.java, bundle, true))
        }
    }

    private val idLessonPractice by argument(ID_PRACTICE_LESSON_ARG, AppConstants.INTEGER_DEFAULT)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyBoardWhenClick(
            binding.btnSharePractice,
            binding.btnBackPracticeDetail,
            binding.tabLayoutPractice
        )
//        hideKeyboardOutSide(view)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(practiceDetailViewModel)
        if (idLessonPractice != AppConstants.INTEGER_DEFAULT) {
            practiceDetailViewModel.getDataDetailPractice(idLessonPractice)
            mainBottomFragment.setDataMainBottom(idLessonPractice)
        } else {
            toast(loadStringRes(R.string.feature_not_available))
            Handler(Looper.getMainLooper()).postDelayed({
                onBackPressed()
            }, 500)
            return
        }
        configViewPager()
        setupView()
        handleClick()
        configSendFAQ()
        callbackFragment()
    }

    private fun setupView() {
        binding.avatarUserPractice.show((activity as MainActivity).userInfo?.avatarPath)
        roundNumberAdapter.items = mutableListOf(1, 2, 3, 4, 5)
        setUpRcv(binding.rcvLevelPractice, roundNumberAdapter)
        binding.rcvLevelPractice.isGone = listOf(
            ApiConstants.ID_MODE_LED,
            ApiConstants.ID_MODE_FREE_PUNCH
        ).contains(idLessonPractice)
    }

    private fun handleClick() {
        binding.btnBackPracticeDetail.clickWithDebounce { onBackPressed() }
        binding.btnSharePractice.clickWithDebounce {
            if (dataDetailPracticeResponse != null) {
                val shareIntent = Intent()
                shareIntent.action = Intent.ACTION_SEND
                shareIntent.type = "text/plain"
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, dataDetailPracticeResponse!!.title)
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    "${dataDetailPracticeResponse!!.title}\n ${dataDetailPracticeResponse!!.linkShare}"
                )
                startActivity(Intent.createChooser(shareIntent, "Send to:"))
            } else {
                toast(getString(R.string.wait_a_moment))
            }
        }
        binding.btnStartPracticeMain.setOnClickListener {
            roundNumber = roundNumberAdapter.getLevelCurrent() ?: 1
            nextFragmentTestPractice(dataDetailPracticeResponse)
        }
        binding.btnRecordVideoPractice.clickWithDebounce {
            nextFragmentRecord()
        }
    }

    private fun nextFragmentTestPractice(data: DetailPracticeResponse?) {
        if ((requireActivity() as MainActivity).isConnectedBle) {
            when (idLessonPractice) {
                ApiConstants.ID_MODE_FREE_PUNCH -> {
                    practiceDetailViewModel.getAvgPractice(idLessonPractice) { avgPower, avgHit ->
                        PracticeTestFragment.openFragment(MachineFreePunchCommand(avgPower, avgHit))
                    }
                }

                ApiConstants.ID_MODE_LED -> {
                    practiceDetailViewModel.getAvgPractice(idLessonPractice) { avgPower, avgHit ->
                        PracticeTestFragment.openFragment(MachineLedPunchCommand(avgPower, avgHit))
                    }
                }

                else -> {
                    if (data != null) {
                        val level = dataDetailPracticeResponse
                            ?.levelPractice
                            ?.firstOrNull()
                            ?.let { it.level to it.value }
                            ?.let { (level, value) ->
                                if (level == null || value == null) null
                                else level to value
                            }
                        val dataIdLesson =
                            dataDetailPracticeResponse?.let { detail ->
                                val id = detail.id
                                val path = detail.videoPath ?: detail.media?.find {
                                    it.mediaPath != null && it.type == MediaPractice.TYPE_VIDEO
                                }?.let { it.mediaPath ?: it.mediaPathOrigin }
                                id to path
                            }.let {
                                val id = it?.first ?: -1
                                val path = it?.second
                                listOf(
                                    MachineLessonCommand.LessonWithVideoPath(lessonId = id, videoPath = path)
                                )
                            }
                        val command = MachineLessonCommand(
                            lessonId = dataIdLesson,
                            round = roundNumber,
                            courseId = null,
                            isPlayWithVideo = false,
                            level = level,
                            avgHit = 20,
                            avgPower = 50
                        )
                        SelectMethodPracticeDialog()
                            .onPracticeNormal {
                                PracticeTestFragment.openFragment(command.copy(isPlayWithVideo = false))
                            }
                            .onPracticeWithVideo {
                                PracticeWithVideoFragment.openFragment(command.copy(isPlayWithVideo = true))
                            }
                            .show(parentFragmentManager, "")
                    } else {
                        toast(loadStringRes(R.string.feature_not_available))
                    }
                }
            }
        } else {
            (activity as MainActivity).showDialogRequestConnect()
        }
    }


    private fun callbackFragment() {
        mainBottomFragment.callbackToTopMain =
            PracticeDetailMainBottomFragment.CallbackToTopMain { data ->
                mainTopFragment.setupMainTop(data)
            }
        mainBottomFragment.onEmptyPracticesListener =
            PracticeDetailMainBottomFragment.OnEmptyPracticesListener {
                if (it) {
                    if (dataDetailPracticeResponse != null) {
                        if (!isFreedom) {
                            binding.viewPagerTopPractice?.currentItem = 1
                        }
                    } else {
                        isEmptyPractices = true
                    }
                }
            }
    }

    private fun callbackViewModel(viewModel: PracticeDetailViewModel) {
        addDispose(viewModel.isLoading.subscribe {
            if (it) showDialog() else hideDialog()
        })
        addDispose(viewModel.rxStatusSendMessage.subscribe {
            val idChat = it.first
            val status = it.second
            logErr("$idChat - ${status.name}")
            questionBottomFragment.adapter.changeStatusChat(idChat, status)
        })
        addDispose(viewModel.rxDetailResponse.subscribe { response ->
            binding.btnSharePractice.setVisibility(response.linkShare != null)
            dataDetailPracticeResponse = response
            //mainTop
            mainTopFragment.setTitleMainTop(response.title)
            //informationTop
            informationTopFragment.loadDataInfoTop(response)
            //informationBottom
            informationBottomFragment.setDataInfoBottom(response)
            //FAQTop
            questionTopFragment.setDataQuestionTop(response)
            //FAQBottom
            response.roomId?.let { questionBottomFragment.roomId = it }
            response.roomKeyId?.let { questionBottomFragment.roomKeyId = it }
            if (isEmptyPractices) {
                if (!isFreedom) {
                    binding.viewPagerTopPractice?.currentItem = 1
                }
            }
        })
    }

    private fun configViewPager() {
        binding.viewPagerTopPractice.isSwipePage = false
        binding.viewPagerTopPractice.run {
            adapter = adapterTop
            offscreenPageLimit = adapterTop.count
        }
        binding.viewPagerBottomPractice.run {
            adapter = adapterBottom
            offscreenPageLimit = adapterBottom.count
        }
        binding.tabLayoutPractice.setupWithViewPager(binding.viewPagerTopPractice)
        if (isFreedom) {
            binding.tabLayoutPractice.hide()
            binding.viewPagerBottomPractice.currentItem = 0
        }
        binding.viewPagerTopPractice.onPageSelected { pos ->
            hideKeyboard()
            binding.viewPagerBottomPractice.currentItem = pos ?: 0
            binding.layoutSendFAQ.setVisibility(pos == adapterBottom.count - 1)
            binding.lnStartPractice.setVisibility(pos == 0)
            when (pos) {
                0 -> {
                    informationTopFragment.stopVideo()
                }

                1 -> {
                    informationTopFragment.restartVideo()
                    informationTopFragment.initView()
                }

                2 -> {
                    informationTopFragment.stopVideo()
                    questionBottomFragment.initView()
                }
            }
        }
    }

    private fun configSendFAQ() {
        addDispose(
            RxTextView.textChanges(binding.txtSendContentQuestion)
                .debounce(200, TimeUnit.MICROSECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { content ->
                    binding.btnSendQuestion.isEnabled = content.isNotEmpty()
                    binding.btnSendQuestion.alpha = if (content.isNotEmpty()) 1f else 0.5f
                }
        )

        binding.btnSendQuestion.clickWithDebounce {
            val message = binding.txtSendContentQuestion.text.toString()
            val chat =
                ChatSendRequest(roomId = dataDetailPracticeResponse?.roomId ?: 0, message = message)
            val timeSendMessage = System.currentTimeMillis() / 1000
            practiceDetailViewModel.sendChat(chat, timeSendMessage)
            questionBottomFragment.addChatMessageLocalToList(message, timeSendMessage)
            binding.txtSendContentQuestion.editableText.clear()
        }
    }

    private fun nextFragmentRecord() {
        if ((requireActivity() as MainActivity).isConnectedBle) {
            openFragmentRecord()
        } else {
            (activity as MainActivity).showDialogRequestConnect()
        }
    }

    fun openFragmentRecord() {
        var positionArr = ""
        var delayPositionArr = ""
        val arrBle: List<DataBluetooth>? =
            dataDetailPracticeResponse!!.data?.let { gson.toList(it) }
        val arrBlePunch = arrBle?.sortedBy { it.time }
        if (arrBlePunch != null) {
            for (i in arrBlePunch.indices) {
                positionArr += arrBlePunch[i].position
                if (arrBlePunch[i].time != null) {
                    val delayTimer = if (i == 0) {
                        if (dataDetailPracticeResponse!!.startTime2 != null)
                            abs(arrBlePunch[i].time!! - dataDetailPracticeResponse!!.startTime2!!) else 0L
                    } else {
                        abs(arrBlePunch[i].time!! - arrBlePunch[i - 1].time!!)
                    }
                    delayPositionArr += "-$delayTimer"
                }
            }
        }
        if (dataDetailPracticeResponse != null) {
            val level = dataDetailPracticeResponse
                ?.levelPractice
                ?.firstOrNull()
                ?.let { it.level to it.value }
                ?.let { (level, value) ->
                    if (level == null || value == null) null
                    else level to value
                }
            val dataIdLesson =
                dataDetailPracticeResponse?.let { detail ->
                    val id = detail.id
                    val path = detail.videoPath ?: detail.media?.find {
                        it.mediaPath != null && it.type == MediaPractice.TYPE_VIDEO
                    }?.let { it.mediaPath ?: it.mediaPathOrigin }
                    id to path
                }.let {
                    val id = it?.first ?: -1
                    val path = it?.second
                    listOf(
                        MachineLessonCommand.LessonWithVideoPath(lessonId = id, videoPath = path)
                    )
                }
            val command = MachineLessonCommand(
                lessonId = dataIdLesson,
                round = 1,
                isPlayWithVideo = true,
                level = level,
                avgHit = 20,
                avgPower = 50,
                courseId = null
            )
            VideoRecordFragment.openFragment(command)
        } else {
            toast(loadStringRes(R.string.feature_not_available))
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun hideKeyBoardWhenClick(vararg view: View) {
        for (vieww in view) {
            vieww.setOnTouchListener { v, _ ->
                hideKeyboard()
                try {
                    v?.performClick()
                } catch (e: Exception) {
                    e.logErr()
                }
                if (binding.txtSendContentQuestion.isFocused) {
                    binding.txtSendContentQuestion.clearFocus()
                    true
                } else false
            }
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        register(this)
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) informationTopFragment.stopVideo()
    }

    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            informationTopFragment.stopVideo()
        }
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            informationTopFragment.restartVideo()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23) {
            informationTopFragment.restartVideo()
        }
    }

    override fun onDestroy() {
        informationTopFragment.releasePlayer()
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        unregister(this)
        super.onDestroy()
    }

    @Subscribe
    fun reloadItemPractice(ev: EventReloadPracticeItem) {
        if (idLessonPractice != AppConstants.INTEGER_DEFAULT) {
            mainBottomFragment.reloadDetailMain()
        } else {
            toast(loadStringRes(R.string.feature_not_available))
            Handler(Looper.getMainLooper()).postDelayed({
                onBackPressed()
            }, 500)
        }
    }

    @Subscribe
    fun sendVideoPractice(ev: EventSendVideoPractice) {
        logErr("sendVideoPractice ${ev.videoId}")
        val timeSendVideo = System.currentTimeMillis() / 1000
        practiceDetailViewModel.sendChat(
            ChatSendRequest(
                dataDetailPracticeResponse?.roomId ?: 0,
                getString(R.string.my_practicing_results),
                ChatType.USER_VIDEO,
                ev.videoId
            ), timeSendVideo
        )
        questionBottomFragment.addChatVideoToList(ev.dummyResult, timeSendVideo, ev.videoId)
    }


}