package com.mobileplus.dummytriluc.ui.main.challenge.detail

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.BaseFragment
import com.google.android.exoplayer2.ExoPlayerFactory
import com.google.android.exoplayer2.PlaybackParameters
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import com.google.android.exoplayer2.util.Util
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.ActionConnection
import com.mobileplus.dummytriluc.bluetooth.BluetoothStatus
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.model.ItemProgressPower
import com.mobileplus.dummytriluc.data.model.ItemRankingChallenge
import com.mobileplus.dummytriluc.data.response.DataChallengeDetailResponse
import com.mobileplus.dummytriluc.transceiver.command.MachineChallengeCommand
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.challenge.detail.adapter.NewJoinAdapter
import com.mobileplus.dummytriluc.ui.main.challenge.detail.adapter.PositionHitAdapter
import com.mobileplus.dummytriluc.ui.main.challenge.detail.adapter.RankingChallengeAdapter
import com.mobileplus.dummytriluc.ui.main.power.adapter.ProgressPowerAdapter
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.ExoUtils
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventOverlayCameraView
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPostVideoSuccess
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.video.fullscreen.VideoFullScreenActivity
import com.mobileplus.dummytriluc.ui.video.record.VideoRecordFragment
import com.mobileplus.dummytriluc.ui.video.result.VideoResultActivity
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.applyClickShrink
import com.utils.ext.*
import kotlinx.android.synthetic.main.exo_playback_control_view.view.*
import kotlinx.android.synthetic.main.fragment_challenge_detail.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class ChallengeDetailFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_challenge_detail
    private val challengeDetailViewModel by viewModel<ChallengeDetailViewModel>()
    private val playerView: PlayerView by lazy { videoViewChallengeDetail }
    private var exoPlayer: SimpleExoPlayer? = null
    private var uriVideo: Uri? = null
    private val mediaDataSource: DataSource.Factory by lazy {
        DefaultDataSourceFactory(
            requireContext(),
            Util.getUserAgent(
                requireContext(),
                DummyTriLucApplication.getInstance().applicationContext.applicationInfo.packageName
            )
        )
    }

    private val gson by inject<Gson>()

    //save state
    private var startPosition: Long = 0
    private var speedParameter: Float = 1f
    private var autoStart: Boolean = false

    private val positionHitAdapter by lazy { PositionHitAdapter() }
    private val progressAdapter by lazy { ProgressPowerAdapter(isChallenge = true) }
    private val rankingAdapter by lazy { RankingChallengeAdapter() }
    private val newJoinAdapter by lazy { NewJoinAdapter() }
    private var responseDetail: DataChallengeDetailResponse? = null
    private val idDetailChallenge by argument(ID_DETAIL_CHALLENGE_ARG, -1)
    private var isUpdateScoreChallenge: Boolean = false
    private var isJoin: Boolean = false
        set(value) {
            field = value
            layoutProfileJoinedChallenge?.setVisibility(field)
            if (field) {
                btnActionJoinChallenge?.text = loadStringRes(R.string.start)
            } else {
                btnActionJoinChallenge?.text = loadStringRes(R.string.join)
            }
        }
    private var positionArr: String = ""

    companion object {
        const val ID_DETAIL_CHALLENGE_ARG = "ID_DETAIL_CHALLENGE_ARG"

        //request code
        private const val KEY_REQUEST_CODE_FULL_SCREEN = 25

        fun openFragment(id: Int) {
            val bundle = Bundle().apply {
                putInt(ID_DETAIL_CHALLENGE_ARG, id)
            }
            postNormal(EventNextFragmentMain(ChallengeDetailFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(challengeDetailViewModel)
        challengeDetailViewModel.getDetailChallenge(idDetailChallenge)
        configView()
        handleClick()
    }

    private fun callbackViewModel(vm: ChallengeDetailViewModel) {
        addDispose(vm.rxDetailResponse.subscribe {
            responseDetail = it
//            if (isUpdateScoreChallenge) {
//                getDataDetail(it)
//                setupWhenJoin(it)
//            } else {
            getDataDetail(it)
            setupWhenJoin(it)
            parsePositionHuman(it)
//            }
        })
        addDispose(vm.rxFailedResponse.subscribe {
            YesNoButtonDialog()
                .setMessage(it.second)
                .setTextAccept(getString(R.string.back))
                .showDialog(parentFragmentManager)
                .setShowCancelButton(false)
                .setOnCallbackAcceptButtonListener { onBackPressed() }
        })
        addDispose(vm.rxJoinSuccess.subscribe {
            isJoin = it
            isUpdateScoreChallenge = true
            vm.getDetailChallenge(idDetailChallenge)
        })
        addDispose(vm.isLoading.subscribe {
//            if (!isUpdateScoreChallenge) {
//            if (it) showDialog() else hideDialog()
//            }
            setRefresh(it)
        })
    }

    private fun setRefresh(isRefresh: Boolean) {
        swipeRefreshChallengeDetail.isRefreshing = isRefresh
    }

    @SuppressLint("SetTextI18n")
    private fun getDataDetail(data: DataChallengeDetailResponse) {
        cbViewMoreContentChallenge.setVisibility(data.description != null)
        btnShareChallenge.setVisibility(data.linkShare != null)
        txtContentDetailChallenge.setVisibility(data.description != null)
        imgCoverChallengeDetail.setVisibility(data.coverPath != null)
        iconHeaderChallengeDetail.setVisibility(data.avatarPath != null)
        rootChallengeDetail.setBackgroundColor(Color.parseColor(data.backgroundColor))
        imgCoverChallengeDetail.show(data.coverPath)
        iconHeaderChallengeDetail.show(data.avatarPath)
        nameHeaderChallengeDetail.text = data.title ?: "--------"
        data.description?.let { txtContentDetailChallenge.text = it }
        //Điểm thưởng-Kiểu thách đấu-Vị trí đánh
        txtRewardPoints.text = "+${data.reward ?: 0}"
        txtTypeChallenge.text = data.challengesTypeText
        txtDataTypeChallenge.text = data.getChallengeDataType() ?: "---"
        //video introduction
        if (data.introVideo != null) {
            frameVideoChallenge.show()
            uriVideo = Uri.parse(data.introVideo)
            setUpExoPlayer(uriVideo)
        } else {
            frameVideoChallenge.hide()
            uriVideo = null
        }

        //just joined
        txtTitleJustJoinedChallenge.setVisibility(data.newJoin?.isNotEmpty() == true)
        rcvJustJoinedChallenge.setVisibility(data.newJoin?.isNotEmpty() == true)
        val arrNewJoin = data.newJoin?.map { it.user?.avatarPath }
        newJoinAdapter.items = arrNewJoin?.toMutableList() ?: mutableListOf()
        //rank
        txtNumberPeopleJoinChallenge.setTextNotNull(data.getNumberPeopleJoin())
        if (data.challengesJoined?.isNotEmpty() == true) {
            val itemsRank = mutableListOf<ItemRankingChallenge>()
            data.challengesJoined.forEach { rank ->
                val rankCurr = rank.rank ?: -1
                val rankUp = rank.rankUp ?: -1
                val point: Int = rank.point ?: 0
                val pointBonus: Int = rank.pointBonus ?: 0
                val name = rank.user?.fullName ?: "---"
                val avatar = rank.user?.avatarPath
                val userId = challengeDetailViewModel.userInfo?.id
                val isCurrent = rank.user?.id == userId
                itemsRank.add(
                    ItemRankingChallenge(
                        rank = rankCurr,
                        rankUp = rankUp,
                        point = point,
                        pointBonus = pointBonus,
                        fullName = name,
                        avatarPath = avatar,
                        isCurrRank = isCurrent
                    )
                )
            }
            rankingAdapter.items = itemsRank
        }
    }

    private fun parsePositionHuman(data: DataChallengeDetailResponse) {
        val arrPos = mutableListOf<BlePosition>()
        if (data.positionLimit != null) {
            data.positionLimit.map {
                when (it.toString()) {
                    BlePosition.LEFT_CHEEK.key -> {
                        arrPos.add(BlePosition.LEFT_CHEEK)
                    }
                    BlePosition.RIGHT_CHEEK.key -> {
                        arrPos.add(BlePosition.RIGHT_CHEEK)
                    }
                    BlePosition.FACE.key -> {
                        arrPos.add(BlePosition.FACE)
                    }
                    BlePosition.LEFT_CHEST.key -> {
                        arrPos.add(BlePosition.LEFT_CHEST)
                    }
                    BlePosition.RIGHT_CHEST.key -> {
                        arrPos.add(BlePosition.RIGHT_CHEST)
                    }
                    BlePosition.ABDOMEN_UP.key -> {
                        arrPos.add(BlePosition.ABDOMEN_UP)
                    }
                    BlePosition.LEFT_ABDOMEN.key -> {
                        arrPos.add(BlePosition.LEFT_ABDOMEN)
                    }
                    BlePosition.ABDOMEN.key -> {
                        arrPos.add(BlePosition.ABDOMEN)
                    }
                    BlePosition.RIGHT_ABDOMEN.key -> {
                        arrPos.add(BlePosition.RIGHT_ABDOMEN)
                    }
                    BlePosition.LEFT_LEG.key -> {
                        arrPos.add(BlePosition.LEFT_LEG)
                    }
                    BlePosition.RIGHT_LEG.key -> {
                        arrPos.add(BlePosition.RIGHT_LEG)
                    }
                    else -> {
                    }
                }
            }
            if (arrPos.isEmpty()) positionArr = "0"
            else {
                for (pos in arrPos) {
                    positionArr += pos.key
                }
            }
        } else {
            positionArr = "0"
        }
        when (data.getTypeHitting()) {
            DataChallengeDetailResponse.TypeDataChallenge.LIMIT_TIME, DataChallengeDetailResponse.TypeDataChallenge.LIMIT_PUNCH, DataChallengeDetailResponse.TypeDataChallenge.RANDOM -> {
                rcvPositionHit.show()
                txtTypeHitText.hide()
                positionHitAdapter.items = arrPos.map { it.title }.toMutableList()
            }
            DataChallengeDetailResponse.TypeDataChallenge.FREEDOM_PUNCH, DataChallengeDetailResponse.TypeDataChallenge.FREEDOM_TIME -> {
                rcvPositionHit.hide()
                txtTypeHitText.show()
                txtTypeHitText.text = data.hittingTypeText
            }
            DataChallengeDetailResponse.TypeDataChallenge.SAMPLE -> {
                rcvPositionHit.hide()
                txtTypeHitText.show()
                txtTypeHitText.text = data.hittingTypeText
                try {
                    data.hitData?.let {
                        val dataBle: List<DataBluetooth> = gson.toList(it)
                        dataBle.forEach { ble -> positionArr += ble.position }
                    }
                } catch (e: Exception) {
                    e.logErr()
                }
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupWhenJoin(data: DataChallengeDetailResponse) {
        //button tham gia-bắt đầu
        isJoin = data.joined ?: false
        if (data.timeEnd != null) {
            val currMs = System.currentTimeMillis() / 1000
            val timeEnd = try {
                DateTimeUtil.convertDateToTimeStampServer(data.timeEnd, "yyyy-MM-dd hh:mm:ss")
            } catch (e: Exception) {
                0L
            }
            if (currMs > timeEnd) {
                btnActionJoinChallenge?.text = loadStringRes(R.string.challenge_ended)
                btnActionJoinChallenge.isEnabled = false
                btnActionJoinChallenge.setBackgroundResource(R.drawable.background_circle_button_grey)
            }
            logErr("currMs:$currMs - timeEnd:$timeEnd")
        }
        val userInfo = challengeDetailViewModel.userInfo
        imgProfileAvatarChallenge.show(userInfo?.avatarPath)
        txtProfileNameChallenge.text = userInfo?.fullName
        if (data.joined == true) {
            //thông tin sau khi tham gia
            data.dataRank?.let { dataRank ->
                if (dataRank.myRank != null) {
                    dataRank.setRankTop(txtProfileRankCurrentChallenge)
                }
                if (dataRank.myRankUp != null) {
                    dataRank.setRankingUp(txtProfileRankUpChallenge)
                }
                txtPowerChallenge.text = "${dataRank.point ?: 0}"
                txtProfileTimeAchievedChallenge.text =
                    DateTimeUtil.convertTimeStampToMMSS(dataRank.timeDuration)
                //chi tiết đòn đánh
                cbShowDetailPowerChallenge.setVisibility(dataRank.position != null && dataRank.position.isNotEmpty())
                if (dataRank.position != null) {
                    var totalScore = 0
                    dataRank.position.forEach { totalScore += it.score ?: 0 }
                    val itemsProgress = mutableListOf<ItemProgressPower>()
                    dataRank.position.map { posHuman ->
                        val percentScore =
                            if (totalScore != 0 && posHuman.score != null)
                                posHuman.score * 100 / totalScore
                            else 0
                        itemsProgress.add(
                            ItemProgressPower(
                                percentScore,
                                Color.parseColor(posHuman.color ?: "#0F1838"),
                                posHuman.title ?: "--"
                            )
                        )
                    }
                    progressAdapter.items = itemsProgress
                }
            }
        }
    }

    private fun handleClick() {
        btnShareChallenge.clickWithDebounce {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, responseDetail?.description)
            shareIntent.putExtra(
                Intent.EXTRA_TEXT,
                "Hãy cùng thách đấu với tôi:\n ${responseDetail?.linkShare}"
            )
            startActivity(Intent.createChooser(shareIntent, "Send to:"))
        }
        swipeRefreshChallengeDetail.setOnRefreshListener {
            challengeDetailViewModel.getDetailChallenge(idDetailChallenge)
        }
        btnBackChallengeDetail.clickWithDebounce { onBackPressed() }
        cbShowDetailPowerChallenge.setOnCheckedChangeListener { _, isChecked ->
            rcvChallengeDetailProgress.setVisibility(!isChecked)
            cbShowDetailPowerChallenge.text =
                if (isChecked) loadStringRes(R.string.see_detail) else loadStringRes(R.string.collapse)
        }
        btnReplayPractice.clickWithDebounce {
            if (responseDetail?.dataPractice != null && responseDetail?.dataPractice?.video_path != null) {
                val bundle = Bundle().apply {
                    putString(
                        VideoResultActivity.DATA_REPLAY_CHALLENGE,
                        gson.toJson(responseDetail?.dataPractice!!)
                    )
                }
                startActivity(VideoResultActivity::class.java, bundle)
            } else {
                toast(getString(R.string.there_are_no_records_yet))
            }
        }
        btnActionJoinChallenge.clickWithDebounce {
            if (isJoin) {
                nextFragmentRecord()
            } else {
                challengeDetailViewModel.joinChallenge(idDetailChallenge)
            }
        }

        cbViewMoreContentChallenge.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                txtContentDetailChallenge.text = responseDetail?.description
                cbViewMoreContentChallenge.text = loadStringRes(R.string.more)
            } else {
                txtContentDetailChallenge.setHtmlWithImage(
                    responseDetail?.content ?: "",
                    requireContext()
                )
                cbViewMoreContentChallenge.text = loadStringRes(R.string.collapse)
            }
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
        postNormal(EventOverlayCameraView(true))
//        val request = RequestBleChallenge(
//            challengeId = idDetailChallenge,
//            hittingType = responseDetail?.getTypeHitting()?.dataBle,
//            hitData = responseDetail?.getHitDataBle(),
//            hitLimit = responseDetail?.hitLimit ?: 0,
//            timeLimit = responseDetail?.timeLimit ?: 0,
//            positionLimit = positionArr ?: "0",
//            weight = responseDetail?.weight ?: 0,
//            minPower = responseDetail?.minPower ?: 0,
//            randomDelayTime = responseDetail?.randomDelayTime ?: 0
//        )
        val data = responseDetail
        if (data != null) {
            val command = MachineChallengeCommand(
                challengeId = idDetailChallenge,
                challengeType = data.getTypeHitting().dataBle,
                hitData = data.getHitDataBle(),
                hitLimit = data.hitLimit ?: 0,
                timeLimit = data.timeLimit ?: 0,
                positionLimit = positionArr,
                weight = data.weight ?: 0,
                minPower = data.minPower ?: 0,
                randomDelayTime = data.randomDelayTime ?: 0
            )
            VideoRecordFragment.openFragment(command)
        }
    }

    data class RequestBleChallenge(
        @Expose
        val challengeId: Int? = null,
        @Expose
        val hittingType: Int? = null,
        @Expose
        var hitData: String? = "0",
        @Expose
        var hitLimit: Int? = 0,
        @Expose
        var timeLimit: Int? = 0,
        @Expose
        val positionLimit: String? = "0",
        @Expose
        val weight: Int? = 0,
        @Expose
        val minPower: Int? = 0,
        @Expose
        val randomDelayTime: Int? = 0,
    )

    private fun configView() {
        txtRewardPoints.fillGradientPrimary()
        txtPowerChallenge.fillGradientPrimary()
        btnActionJoinChallenge.applyClickShrink()
        swipeRefreshChallengeDetail.applyColorRefresh()
        rcvPositionHit.run {
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = positionHitAdapter
        }
        rcvJustJoinedChallenge.run {
            setHasFixedSize(true)
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt(),
                    true
                )
            )
            adapter = newJoinAdapter
        }
        setUpRcv(rcvChallengeDetailProgress, progressAdapter)
        setUpRcv(rcvRankingChallenge, rankingAdapter)
        rcvRankingChallenge.addItemDecoration(
            MarginItemDecoration(
                resources.getDimension(R.dimen.space_4).toInt()
            )
        )
    }

    private fun releasePlayer() {
        if (exoPlayer != null) {
            startPosition = exoPlayer!!.currentPosition
            speedParameter = exoPlayer!!.playbackParameters.speed
            autoStart = exoPlayer!!.playWhenReady
            exoPlayer!!.release()
            exoPlayer = null
        }
    }

    @Subscribe
    fun reloadChallenge(ev: EventPostVideoSuccess) {
        logErr("reload")
        isUpdateScoreChallenge = true
        challengeDetailViewModel.getDetailChallenge(idDetailChallenge)
    }

    @Subscribe
    fun createOverlayCameraView(ev: EventOverlayCameraView) {
        videoViewChallengeDetail.setVisibility(!ev.isOverlay)
        logErr("overlay:${ev.isOverlay}")
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        register(this)
        logErr("onAttach")
    }

    override fun onDestroy() {
        super.onDestroy()
        unregister(this)
        logErr("onDestroy")
    }

    override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            setUpExoPlayer(uriVideo)
            playerView.onResume()
        }
    }

    override fun onResume() {
        super.onResume()
        if (Util.SDK_INT <= 23 || exoPlayer == null) {
            setUpExoPlayer(uriVideo)
            playerView.onResume()
        }
    }


    override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            playerView.onPause()
            releasePlayer()
        }
    }

    private fun setUpExoPlayer(uri: Uri?) {
        if (exoPlayer == null && uri != null) {
            val mediaSource =
                ProgressiveMediaSource.Factory(mediaDataSource).createMediaSource(uri)
            exoPlayer = ExoPlayerFactory.newSimpleInstance(requireContext())
            exoPlayer?.let {
                it.playWhenReady = autoStart
                it.playbackParameters = PlaybackParameters(speedParameter)
                it.seekTo(startPosition)
            }
            playerView.player = exoPlayer
            exoPlayer?.prepare(mediaSource, false, false)
            playerView.requestFocus()
            with(playerView) {
                btnTimeSpeed.text = when (speedParameter) {
                    1f -> "1x"
                    0.5f -> "0.5x"
                    0.25f -> "0.25x"
                    0.125f -> "0.125x"
                    else -> ""
                }
                btnTimeSpeed.clickWithDebounce {
                    CustomSpinner(it, requireContext())
                        .setBackGroundSpinner(Color.parseColor("#2C2C2C"))
                        .setShowUp(true)
                        .setTextSize(resources.getDimension(R.dimen.text_10))
                        .setWidthWindow(resources.getDimension(R.dimen._50sdp))
                        .setDataSource(ExoUtils.listSpeedParameter).build()
                        .setOnSelectedItemCallback { item ->
                            exoPlayer?.playbackParameters = PlaybackParameters(item.id.toFloat())
                        }
                }
                btnFullScreen.clickWithDebounce {
                    val intent =
                        Intent(requireContext(), VideoFullScreenActivity::class.java).apply {
                            putExtra(
                                VideoFullScreenActivity.ARG_POSITION_SCREEN_FULL,
                                exoPlayer!!.currentPosition
                            )
                            putExtra(
                                VideoFullScreenActivity.ARG_SPEED_SCREEN_FULL,
                                exoPlayer!!.playbackParameters.speed
                            )
                            putExtra(
                                VideoFullScreenActivity.ARG_AUTO_START_SCREEN_FULL,
                                exoPlayer!!.playWhenReady
                            )
                        }
                    VideoFullScreenActivity.videoResult = uriVideo
                    startActivityForResult(intent, KEY_REQUEST_CODE_FULL_SCREEN)
                }
            }

            exoPlayer?.addListener(object : Player.EventListener {
                override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                    loadingVideoChallengeDetail.setVisibility(playbackState == Player.STATE_BUFFERING)
                }
            })
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == KEY_REQUEST_CODE_FULL_SCREEN) {
            if (resultCode == Activity.RESULT_OK) {
                startPosition =
                    data?.getLongExtra(VideoFullScreenActivity.ARG_POSITION_SCREEN_FULL, 0L) ?: 0L
                autoStart =
                    data?.getBooleanExtra(VideoFullScreenActivity.ARG_AUTO_START_SCREEN_FULL, false)
                        ?: false
                speedParameter =
                    data?.getFloatExtra(VideoFullScreenActivity.ARG_SPEED_SCREEN_FULL, 1f) ?: 1f
            }
        }
    }
}