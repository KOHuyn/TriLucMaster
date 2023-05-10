package com.mobileplus.dummytriluc.ui.login.register.stepbystep

import android.os.Bundle
import com.core.BaseFragment
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.databinding.FragmentRegisterStep5Binding
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.DateTimeUtil
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateProfile
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.*
import kotlinx.android.synthetic.main.fragment_register_step_5.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.sign

class RegisterStep5Fragment : BaseFragmentZ<FragmentRegisterStep5Binding>() {

    override fun getLayoutBinding(): FragmentRegisterStep5Binding =
        FragmentRegisterStep5Binding.inflate(layoutInflater)

    lateinit var dayOfMonthList: ArrayList<Int>
    private val monthList: ArrayList<Int> = ConfigWheelData.getMonthList()
    private val yearList: ArrayList<Int> = ConfigWheelData.getYearList()

    private val wheelDayOfMonth by lazy { wheelDayOfMonthStep5 }
    private val wheelMonth by lazy { wheelMonthStep5 }
    private val wheelYear by lazy { wheelYearStep5 }

    private var isEditUser = false

    lateinit var calendar: Calendar
    private var today = Calendar.getInstance()

    private var dayOfMonth: Int = today.get(Calendar.DAY_OF_MONTH)
    private var month: Int = today.get(Calendar.MONTH)
        set(value) {
            field = value
            wheelMonth?.setSelectedItemPosition(monthList.indexOf(field), false)
        }
    private var year: Int = yearList.last()
        set(value) {
            field = value
            wheelYear?.setSelectedItemPosition(yearList.indexOf(field), false)
        }

    companion object {
        const val ARG_UPDATE_INFORMATION_STEP_5 = "ARG_UPDATE_INFORMATION_STEP_5"
    }

    private val jsonRegister by argument(ARG_UPDATE_INFORMATION_STEP_5, "")
    private lateinit var updateInfo: UpdateInfo
    private val gson by inject<Gson>()

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        setupWheel()
        checkArg()
        initAction()
    }

    private fun checkArg() {
        if (arguments != null) {
            if (arguments?.containsKey(ARG_UPDATE_INFORMATION_STEP_5) == true) {
                isEditUser = false
                updateInfo = gson.fromJson(jsonRegister, UpdateInfo::class.java)
            }
            if (arguments?.containsKey(UserInfoFragment.ARG_EDIT_PROFILE) == true) {
                isEditUser = true
                updateInfo = gson.fromJson(
                    arguments?.getString(UserInfoFragment.ARG_EDIT_PROFILE),
                    UpdateInfo::class.java
                )
                val calendar =
                    DateTimeUtil.convertDate(updateInfo.birthday ?: "2000-01-01", "yyyy-MM-dd")
                year = calendar.get(Calendar.YEAR)
                month = calendar.get(Calendar.MONTH) + 1
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                updateDayOfMonth()
                wheelDayOfMonth?.setSelectedItemPosition(dayOfMonthList.indexOf(dayOfMonth), false)
            }
        }
    }

    private fun setupWheel() {
        wheelYear.data = yearList
        year = yearList.last()
        wheelYear.setOnItemSelectedListener { _, _, position ->
            year = yearList[position]
            updateDayOfMonth()
        }

        wheelMonth.data = monthList
        wheelMonth.setSelectedItemPosition(month, false)
        wheelMonth.setOnItemSelectedListener { _, _, position ->
            month = monthList[position]
            updateDayOfMonth()
        }

        wheelDayOfMonth.setOnItemSelectedListener { _, _, position ->
            try {
                dayOfMonth = dayOfMonthList[position]
            } catch (e: Exception) {
                e.logErr()
            }
        }
        updateDayOfMonth()
    }

    private fun initAction() {
        txtBackStep5.clickWithDebounce { onBackPressed() }
        txtNextStep5.clickWithDebounce {
            updateInfo.birthday =
                "$year-${String.format("%02d", month)}-${String.format("%02d", dayOfMonth)}"
            if (isEditUser) {
                postNormal(EventUpdateProfile(updateInfo))
                onBackPressed()
            } else {
                val bundle = Bundle().apply {
                    putString(
                        RegisterStep6Fragment.ARG_UPDATE_INFORMATION_STEP_6,
                        gson.toJson(updateInfo)
                    )
                }
                postNormal(
                    EventNextFragmentLogin(
                        RegisterStep6Fragment::class.java, bundle,
                        true
                    )
                )
            }
        }

    }

    private fun updateDayOfMonth() {
        calendar = today.clone() as Calendar
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.MONTH, month - 1)

        dayOfMonthList = arrayListOf()

        val lastDayOfSelectedMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        for (i in 1..lastDayOfSelectedMonth) {
            dayOfMonthList.add(i)
        }

        wheelDayOfMonth.data = dayOfMonthList

        if (dayOfMonth > lastDayOfSelectedMonth) {
            dayOfMonth = lastDayOfSelectedMonth
        }
        wheelDayOfMonth.setSelectedItemPosition(dayOfMonthList.indexOf(dayOfMonth), false)
    }

    object ConfigWheelData {
        private val calendar: Calendar = Calendar.getInstance()
        fun getYearList(): ArrayList<Int> {
            val arr = arrayListOf<Int>()
            for (i in 1950 until calendar.get(Calendar.YEAR) - 16) {
                arr.add(i)
            }
            return arr
        }

        fun getMonthList(): ArrayList<Int> {
            val arr = arrayListOf<Int>()
            for (i in 1..12) {
                arr.add(i)
            }
            return arr
        }
    }

    override fun onStart() {
        super.onStart()
        register(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
    }

    @Subscribe
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        binding.txtLabelBirthday.text = loadStringRes(R.string.label_date_of_birth)
        binding.txtNextStep5.text = loadStringRes(R.string.label_continue)
        binding.txtBackStep5.text = loadStringRes(R.string.label_back)
        binding.txtLabelDay.text = loadStringRes(R.string.label_day)
        binding.txtLabelMonth.text = loadStringRes(R.string.label_month)
        binding.txtLabelYear.text = loadStringRes(R.string.label_year)
    }

}