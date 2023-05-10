package com.mobileplus.dummytriluc.ui.main.coach.session.result

import android.os.Bundle
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.databinding.FragmentCoachSessionResultBinding
import com.mobileplus.dummytriluc.ui.main.coach.session.CoachSessionFragment
import com.mobileplus.dummytriluc.ui.main.coach.session.result.adapter.CoachSessionResultAdapter
import com.mobileplus.dummytriluc.ui.main.practice.dialog.ConfirmPracticeTestDialog
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.OnClickItemAdapter
import com.mobileplus.dummytriluc.ui.utils.extensions.applyColorRefresh
import com.mobileplus.dummytriluc.ui.utils.extensions.fromJsonSafe
import com.utils.ext.setVisibility
import org.koin.android.ext.android.inject

/**
 * Created by KOHuyn on 4/21/2021
 */
class CoachSessionResultFragment : BaseFragmentZ<FragmentCoachSessionResultBinding>() {
    override fun getLayoutBinding(): FragmentCoachSessionResultBinding =
        FragmentCoachSessionResultBinding.inflate(layoutInflater)

    val adapter by lazy { CoachSessionResultAdapter() }

    private val rcv by lazy { binding.rcvCoachSessionResult }
    private val gson by inject<Gson>()

    var onRefresh: OnRefresh? = null

    override fun updateUI(savedInstanceState: Bundle?) {
        setUpRcv(rcv, adapter)
        binding.swipeToRefreshSession.applyColorRefresh()
        rcv.addItemDecoration(MarginItemDecoration(resources.getDimensionPixelOffset(R.dimen.space_8)))
        adapter.onItemClick = OnClickItemAdapter { v, pos ->
            val item = adapter.items[pos]
            val dataBluetooth =
                (parentFragment as CoachSessionFragment).dataSessionDetailResponse?.resultData?.filter {
                    item.userId == it.userId
                }?.map { result ->
                    BluetoothResponse(
                        mode = result.mode,
                        userId = result.userId,
                        sessionId = result.sessionId,
                        practiceId = result.practiceId,
                        machineId = result.machineId,
                        startTime1 = result.startTime1,
                        endTime = result.endTime,
                        startTime2 = result.startTime2,
                        data = gson.fromJsonSafe<MutableList<DataBluetooth?>>(result.data ?: "")
                            ?: mutableListOf()
                    )
                }

            ConfirmPracticeTestDialog.Builder(dataBluetooth ?: emptyList())
                .setTitle(item.userCreated?.fullName ?: "unknown")
                .setCancelable(false)
                .setPairListTitleCourse((parentFragment as? CoachSessionFragment)?.arrTitleCourse)
                .setModeCourse(false)
                .setShowButtonPlayAgain(false)
                .build(parentFragmentManager)
        }
        binding.swipeToRefreshSession.setOnRefreshListener {
            onRefresh?.setOnRefreshListener()
        }
    }

    fun interface OnRefresh {
        fun setOnRefreshListener()
    }

    fun setTimeStartHeader(timeStart: Long) {
        activity?.runOnUiThread {
            binding.txtSessionResultTimeStart?.text =
                DateTimeUtil.convertTimeStampToDate(timeStart, "HH:mm")
        }
    }

    fun setTimeEndHeader(timeStart: Long) {
        activity?.runOnUiThread {
            binding.txtSessionResultTimeEnd?.text =
                DateTimeUtil.convertTimeStampToDate(timeStart, "HH:mm")
        }
    }

    fun setRound(round: Int) {
        activity?.runOnUiThread {
            binding.txtSessionResultTotalRound?.text = round.toString()
        }
    }

    fun setRefresh(isRefresh: Boolean) {
        runOnUiThread {
            binding.swipeToRefreshSession?.isRefreshing = isRefresh
        }
    }

    fun showTextDescriptionSwipeRefresh(isShow: Boolean) {
        activity?.runOnUiThread {
            binding.txtDescriptionSwipeRefreshCoachSession?.setVisibility(isShow)
            binding.swipeToRefreshSession?.isEnabled = isShow
        }
    }
}