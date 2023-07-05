package com.mobileplus.dummytriluc.ui.main.practice.dialog

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.FragmentManager
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.bluetooth.BluetoothResponse
import com.mobileplus.dummytriluc.bluetooth.DataBluetooth
import com.mobileplus.dummytriluc.data.response.DataSubmitPracticeResponse
import com.mobileplus.dummytriluc.data.response.LevelPractice
import com.mobileplus.dummytriluc.ui.main.practice.adapter.WrapPagerAdapter
import com.mobileplus.dummytriluc.ui.main.practice.dialog.adapter.BaseItemIndexPractice
import com.mobileplus.dummytriluc.ui.main.practice.dialog.adapter.FixedFirstFragmentViewPagerAdapter
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.ext.clickWithDebounce
import com.utils.ext.onPageSelected
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.dialog_confirm_practice_test.*
import kotlin.Exception
import kotlin.concurrent.thread


/**
 * Created by KO Huyn on 1/11/2021.
 */
class ConfirmPracticeTestDialog(private val dataArr: List<BluetoothResponse>) : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_confirm_practice_test
    private var onSubmitPracticeTest: OnSubmitPracticeTest? = null
    private val fragInformation = ConfirmPracticeInformationFragment(dataArr)
    private val fragIndex = ConfirmPracticeIndexFragment()
    private var dataSubmit: DataSubmitPracticeResponse? = null
        set(value) {
            field = value
            fragInformation.dataSubmit = field
        }
    private var dataSample: List<DataBluetooth> = listOf()
    private var arrTitleCourse: MutableList<Pair<Int?, String?>>? = null
    private var isModeCourse = false
    private var levelPractice: LevelPractice? = null
        set(value) {
            field = value
            fragInformation.levelPractice = field
        }
    private var title: String? = null
    private var isShowButtonPlayAgain: Boolean = true

    private val adapterViewpager by lazy {
        FixedFirstFragmentViewPagerAdapter(
            childFragmentManager,
            listOf(fragInformation, fragIndex), emptyList()
        )
    }

    override fun isFullWidth(): Boolean {
        return true
    }

    fun runOnUiThread(block: () -> Unit) {
        if (this.isAdded) {
            activity?.runOnUiThread { block() } ?: Handler(Looper.getMainLooper()).post { block() }
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        txtTitlePractice.setTextNotNull(title)

        setupViewPager()
        btnConfirmPracticeTest.clickWithDebounce {
            dismiss()
            onSubmitPracticeTest?.setOnSubmitPracticeTest(true)
        }
        btnCancelPracticeTest.clickWithDebounce {
            dismiss()
            onSubmitPracticeTest?.setOnSubmitPracticeTest(false)
        }
        imgDotViewPager.setOnClickListener {
            viewpagerConfirmPractice.currentItem =
                if (viewpagerConfirmPractice.currentItem == 1) 0 else 1
        }
        btnCancelPracticeTest.setVisibility(isShowButtonPlayAgain)
        calculateData()
    }

    private fun calculateData() {
        thread(true) {
            val dataPassLevel = getHighTurnPassLevel(dataArr)
            runOnUiThread {
                txtTimePracticeTest?.text =
                    DateTimeUtil.convertTimeStampToDateTimeFull(dataPassLevel.startTime1)
            }
            val level: Float = (levelPractice?.value ?: 100) / 100F
            val dataItems = if (isModeCourse) BaseItemIndexPractice.transformToItemIndex(
                dataArr,
                dataSample,
                level
            ) else {
                try {
                    dataArr.map { data ->
                        if (data.mode == BluetoothResponse.MODE_FREE_FIGHT) {
                            data.data.map { it?.onTarget = 1 }
                        }
                    }
                } catch (e: Exception) {
                    e.logErr()
                }
                if (arrTitleCourse != null) {
                    BaseItemIndexPractice.transformToItemIndex(dataArr, arrTitleCourse!!)
                } else {
                    BaseItemIndexPractice.transformToItemIndex(dataArr)
                }
            }
            runOnUiThread {
                fragIndex.items = dataItems
            }
        }
    }

    class Builder(private val dataArr: List<BluetoothResponse> = emptyList()) {
        private val confirmDialog = ConfirmPracticeTestDialog(dataArr)
        fun setTitle(title: String): Builder {
            confirmDialog.title = title
            return this
        }

        fun setLevelPractice(level: LevelPractice?): Builder {
            confirmDialog.levelPractice = level
            return this
        }

        fun setDataSubmit(data: DataSubmitPracticeResponse?): Builder {
            confirmDialog.dataSubmit = data
            return this
        }

        fun setShowButtonPlayAgain(isShow: Boolean = true): Builder {
            confirmDialog.isShowButtonPlayAgain = isShow
            return this
        }

        fun setDataSample(dataSample: List<DataBluetooth>): Builder {
            confirmDialog.dataSample = dataSample
            return this
        }

        fun setPairListTitleCourse(arrTitle: MutableList<Pair<Int?, String?>>?): Builder {
            confirmDialog.arrTitleCourse = arrTitle
            return this
        }

        fun setCancelable(isCancelable: Boolean): Builder {
            confirmDialog.isCancelable = isCancelable
            return this
        }

        fun setModeCourse(isModeCourse: Boolean): Builder {
            confirmDialog.isModeCourse = isModeCourse
            return this
        }

        fun build(fm: FragmentManager): ConfirmPracticeTestDialog {
            confirmDialog.show(fm, ConfirmPracticeTestDialog::class.java.simpleName)
            return confirmDialog
        }
    }

    fun setSubmitPracticeCallback(isSubmit: (Boolean) -> Unit) {
        onSubmitPracticeTest = OnSubmitPracticeTest { isSubmit.invoke(it) }
    }

    private fun getHighTurnPassLevel(data: List<BluetoothResponse>): BluetoothResponse {
        return try {
            if (data.isEmpty()) return BluetoothResponse()
            var pairDataHigh: Pair<Float, BluetoothResponse> = Pair(0F, data[0])
            data.map { dataResponse ->
                var countPower = 0f
                dataResponse.data.forEach {
                    countPower += it?.force ?: 0f
                }
                if (pairDataHigh.first < countPower) {
                    pairDataHigh = Pair(countPower, dataResponse)
                }
            }
            return pairDataHigh.second
        } catch (e: Exception) {
            e.logErr()
            BluetoothResponse()
        }
    }

    private fun setupViewPager() {
        viewpagerConfirmPractice.isSwipePage = true
        viewpagerConfirmPractice.adapter = adapterViewpager
        viewpagerConfirmPractice.offscreenPageLimit = adapterViewpager.count
        viewpagerConfirmPractice.onPageSelected {
            when (it) {
                0 -> {
                    imgDotViewPager.setImageResource(R.drawable.ic_dot_first)
                }
                1 -> {
                    imgDotViewPager.setImageResource(R.drawable.ic_dot_second)
                }
            }
        }
    }

    private fun interface OnSubmitPracticeTest {
        fun setOnSubmitPracticeTest(isSubmit: Boolean)
    }

}