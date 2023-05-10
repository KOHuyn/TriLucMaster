package com.mobileplus.dummytriluc.ui.login.register.stepbystep

import android.os.Bundle
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.databinding.FragmentRegisterStep4Binding
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateProfile
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import kotlin.math.abs

class RegisterStep4Fragment : BaseFragmentZ<FragmentRegisterStep4Binding>() {

    override fun getLayoutBinding(): FragmentRegisterStep4Binding =
        FragmentRegisterStep4Binding.inflate(layoutInflater)

    private val wheelNatural by lazy { binding.wheelNaturalStep4 }
    private val wheelDecimal by lazy { binding.wheelDecimalStep4 }
    private val wheelUnit by lazy { binding.wheelUnitStep4 }

    private val naturalList = ConfigWheelData.getListNatural()
    private val decimalList = ConfigWheelData.getListDecimal()
    private val unitList = ConfigWheelData.getListUnit()

    private var isEditUser = false

    private var selectedNatural = 50
        set(value) {
            field = value
            runOnUiThread {
                wheelNatural.setSelectedItemPosition(naturalList.indexOf(field), false)
            }
        }
    private var selectedDecimal = 0
        set(value) {
            field = value
            runOnUiThread {
                wheelDecimal.setSelectedItemPosition(
                    decimalList.indexOf(field), false
                )
            }
        }
    private var selectedUnit = UpdateInfo.UNIT_KILOGRAM
        set(value) {
            field = value
            runOnUiThread {
                wheelUnit.setSelectedItemPosition(unitList.indexOf(field), false)
            }
        }

    companion object {
        const val ARG_UPDATE_INFORMATION_STEP_4 = "ARG_UPDATE_INFORMATION_STEP_4"
    }

    private val jsonRegister by argument(ARG_UPDATE_INFORMATION_STEP_4, "")
    private lateinit var updateInfo: UpdateInfo
    private val gson by inject<Gson>()

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        initData()
        checkArg()
        initAction()
    }

    private fun checkArg() {
        if (arguments != null) {
            if (arguments?.containsKey(ARG_UPDATE_INFORMATION_STEP_4) == true) {
                isEditUser = false
                updateInfo = gson.fromJson(jsonRegister, UpdateInfo::class.java)
            }
            if (arguments?.containsKey(UserInfoFragment.ARG_EDIT_PROFILE) == true) {
                isEditUser = true
                updateInfo = gson.fromJson(
                    arguments?.getString(UserInfoFragment.ARG_EDIT_PROFILE),
                    UpdateInfo::class.java
                )
                if (updateInfo.weight != null) {
                    val nature = updateInfo.weight!!.toInt()
                    val decimal = abs(updateInfo.weight!! - nature) * 10
                    selectedNatural = nature
                    selectedDecimal = decimal.toInt()
                    selectedUnit = if (updateInfo.weightUnit == UpdateInfo.UNIT_POUND) {
                        unitList[unitList.indexOf(UpdateInfo.UNIT_POUND_SORT)]
                    } else {
                        unitList[unitList.indexOf(UpdateInfo.UNIT_KILOGRAM)]
                    }
                }
            }
        }
    }

    private fun initData() {
        wheelNatural.data = naturalList
        selectedNatural = naturalList[naturalList.indexOf(50)]
        wheelNatural.setOnItemSelectedListener { _, _, position ->
            selectedNatural = naturalList[position]
        }

        wheelDecimal.data = decimalList
        selectedDecimal = decimalList[0]
        wheelDecimal.setOnItemSelectedListener { _, _, position ->
            selectedDecimal = decimalList[position]
        }

        wheelUnit.data = unitList
        selectedUnit = unitList[unitList.indexOf(UpdateInfo.UNIT_KILOGRAM)]
        wheelUnit.setOnItemSelectedListener { _, _, position ->
            selectedUnit = unitList[position]
        }
    }

    private fun initAction() {
        binding.txtBackStep4.clickWithDebounce { onBackPressed() }
        binding.btnNextStep4.clickWithDebounce {
            when (selectedUnit) {
                UpdateInfo.UNIT_KILOGRAM -> {
                    updateInfo.saveWeightKg(
                        selectedNatural.toDouble() + selectedDecimal.toDouble().div(10)
                    )
                }
                UpdateInfo.UNIT_POUND_SORT -> {
                    updateInfo.saveWeightLbs(
                        selectedNatural.toDouble() + selectedDecimal.toDouble().div(10)
                    )
                }
            }
            if (isEditUser) {
                postNormal(EventUpdateProfile(updateInfo))
                onBackPressed()
            } else {
                val bundle = Bundle().apply {
                    putString(
                        RegisterStep5Fragment.ARG_UPDATE_INFORMATION_STEP_5,
                        gson.toJson(updateInfo)
                    )
                }
                postNormal(
                    EventNextFragmentLogin(
                        RegisterStep5Fragment::class.java,
                        bundle, true
                    )
                )
            }
        }
    }

    private object ConfigWheelData {
        fun getListDecimal(): ArrayList<Int> {
            val arr = arrayListOf<Int>()
            for (i in 0..9) {
                arr.add(i)
            }
            return arr
        }

        fun getListNatural(): ArrayList<Int> {
            val arr = arrayListOf<Int>()
            for (i in 1..200) {
                arr.add(i)
            }
            return arr
        }

        fun getListUnit(): ArrayList<String> {
            val arr = arrayListOf<String>()
            arr.add(UpdateInfo.UNIT_KILOGRAM)
            arr.add(UpdateInfo.UNIT_POUND_SORT)
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
        binding.txtLabelWeight.text = loadStringRes(R.string.label_weight)
        binding.btnNextStep4.text = loadStringRes(R.string.label_continue)
        binding.txtBackStep4.text = loadStringRes(R.string.label_back)
    }

}
