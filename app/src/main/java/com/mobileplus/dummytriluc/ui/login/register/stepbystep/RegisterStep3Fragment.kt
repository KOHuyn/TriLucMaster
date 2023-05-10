package com.mobileplus.dummytriluc.ui.login.register.stepbystep

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.UpdateInfo
import com.mobileplus.dummytriluc.databinding.FragmentRegisterStep3Binding
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventUpdateProfile
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import kotlin.math.abs

class RegisterStep3Fragment : BaseFragmentZ<FragmentRegisterStep3Binding>() {
    override fun getLayoutBinding(): FragmentRegisterStep3Binding =
        FragmentRegisterStep3Binding.inflate(layoutInflater)

    private val wheelUnit by lazy { binding.wheelUnitStep3 }
    private val wheelCentimeter by lazy { binding.wheelCentimeterStep3 }
    private val wheelFeetNatural by lazy { binding.wheelFeetNaturalStep3 }
    private val wheelFeetDecimal by lazy { binding.wheelFeetDecimalStep3 }

    private val listUnit = ConfigWheelData.getListUnit()
    private val listCentimeter = ConfigWheelData.getListCentimeter()
    private val listFeetNatural = ConfigWheelData.getListFeetNatural()
    private val listFeetDecimal = ConfigWheelData.getListFeetDecimal()

    private var isEditUser = false

    private var isFeetUnit = false
        set(value) {
            field = value
            runOnUiThread {
                if (field) {
                    wheelUnit.setSelectedItemPosition(
                        listUnit.indexOf(UpdateInfo.UNIT_FEET_SORT),
                        false
                    )
                } else {
                    wheelUnit.setSelectedItemPosition(
                        listUnit.indexOf(UpdateInfo.UNIT_CENTIMETER),
                        false
                    )
                }
            }
        }

    private var selectedCentimeter = 100
        set(value) {
            field = value
            Handler(Looper.getMainLooper()).post {
                wheelCentimeter.setSelectedItemPosition(listCentimeter.indexOf(field), false)
            }
        }
    private var selectedNaturalFeet = 1
        set(value) {
            field = value
            wheelFeetNatural.setSelectedItemPosition(listFeetNatural.indexOf(field), false)
        }
    private var selectedDecimalFeet = 0
        set(value) {
            field = value
            wheelFeetDecimal.setSelectedItemPosition(
                listFeetDecimal.indexOf(String.format("%02d", field)),
                false
            )
        }

    companion object {
        const val ARG_UPDATE_INFORMATION_STEP_3 = "ARG_UPDATE_INFORMATION_STEP_3"
    }

    private val jsonRegister by argument(ARG_UPDATE_INFORMATION_STEP_3, "")
    private lateinit var updateInfo: UpdateInfo
    private val gson by inject<Gson>()

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        initWheelsData()
        checkArg()
        initAction()
    }

    private fun checkArg() {
        if (arguments != null) {
            if (arguments?.containsKey(ARG_UPDATE_INFORMATION_STEP_3) == true) {
                isEditUser = false
                updateInfo = gson.fromJson(jsonRegister, UpdateInfo::class.java)
            }
            if (arguments?.containsKey(UserInfoFragment.ARG_EDIT_PROFILE) == true) {
                isEditUser = true
                updateInfo = gson.fromJson(
                    arguments?.getString(UserInfoFragment.ARG_EDIT_PROFILE),
                    UpdateInfo::class.java
                )
                if (updateInfo.heightUnit == UpdateInfo.UNIT_FEET) {
                    isFeetUnit = true
                    if (updateInfo.height != null) {
                        val nature = updateInfo.height!!.toInt()
                        val decimal = abs(updateInfo.height!! - nature) * 100
                        selectedNaturalFeet = nature
                        selectedDecimalFeet = decimal.toInt()
                    }
                    changeUnitHeight(UpdateInfo.UNIT_FEET_SORT)
                } else {
                    isFeetUnit = false
                    selectedCentimeter = updateInfo.height?.toInt() ?: 170
                    changeUnitHeight(UpdateInfo.UNIT_CENTIMETER)
                }
            }
        }
    }

    private fun initWheelsData() {
        setupWheelFeet()
        setupWheelCM()
        setupWheelUnit()
    }

    private fun setupWheelUnit() {
        //Data for measure unit
        wheelUnit.data = listUnit
        wheelUnit.setOnItemSelectedListener { _, _, position ->
            changeUnitHeight(listUnit[position])
        }
    }

    private fun setupWheelCM() {
        //Data for centimeter unit
        wheelCentimeter.data = listCentimeter
        wheelCentimeter.setOnItemSelectedListener { _, data, position ->
            selectedCentimeter = listCentimeter[position]
        }
    }

    private fun setupWheelFeet() {
        //Data for feet natural
        wheelFeetNatural.data = listFeetNatural
        wheelFeetNatural.setOnItemSelectedListener { _, data, position ->
            selectedNaturalFeet = listFeetNatural[position]
        }
        //Data for feet decimal
        wheelFeetDecimal.data = listFeetDecimal
        wheelFeetDecimal.setOnItemSelectedListener { _, data, position ->
            selectedDecimalFeet = listFeetDecimal[position].toInt()
        }
        /*Default centimeter is 5.6ft ~ 170cm- min height is 1ft*/
        selectedCentimeter = 170
        selectedNaturalFeet = 5
        selectedDecimalFeet = listFeetDecimal[6].toInt()
    }

    private fun changeUnitHeight(unit: String) {
        when (unit) {
            UpdateInfo.UNIT_FEET_SORT -> {
                isFeetUnit = true
                wheelCentimeter.hide()
                binding.viewFeetStep3.show()
            }
            UpdateInfo.UNIT_CENTIMETER -> {
                isFeetUnit = false
                wheelCentimeter.show()
                binding.viewFeetStep3.hide()
            }
            else -> {
                wheelCentimeter.hide()
                binding.viewFeetStep3.show()
            }
        }
    }

    private fun initAction() {
        binding.txtBackStep3.clickWithDebounce { onBackPressed() }
        binding.txtNextStep3.clickWithDebounce {
            if (isFeetUnit) {
                updateInfo.saveHeightFeet(
                    selectedNaturalFeet.toDouble() + selectedDecimalFeet.toDouble().div(100)
                )
            } else {
                updateInfo.saveHeightCentimeter(selectedCentimeter)
            }
            if (isEditUser) {
                postNormal(EventUpdateProfile(updateInfo))
                onBackPressed()
            } else {
                val bundle = Bundle().apply {
                    putString(
                        RegisterStep4Fragment.ARG_UPDATE_INFORMATION_STEP_4,
                        gson.toJson(updateInfo)
                    )
                }
                postNormal(
                    EventNextFragmentLogin(
                        RegisterStep4Fragment::class.java,
                        bundle, true
                    )
                )
            }
        }
    }

    private object ConfigWheelData {
        fun getListCentimeter(): ArrayList<Int> {
            val arr = arrayListOf<Int>()
            for (i in 100..250) {
                arr.add(i)
            }
            return arr
        }

        fun getListFeetDecimal(): ArrayList<String> {
            val arr = arrayListOf<String>()
            for (i in 0..99) {
                arr.add(String.format("%02d", i))
            }
            return arr
        }

        fun getListFeetNatural(): ArrayList<Int> {
            val arr = arrayListOf<Int>()
            for (i in 1..10) {
                arr.add(i)
            }
            return arr
        }

        fun getListUnit(): ArrayList<String> {
            val arr = arrayListOf<String>()
            arr.add(UpdateInfo.UNIT_CENTIMETER)
            arr.add(UpdateInfo.UNIT_FEET_SORT)
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
        binding.txtLabelHeight.text = loadStringRes(R.string.label_height)
        binding.txtNextStep3.text = loadStringRes(R.string.label_continue)
        binding.txtBackStep3.text = loadStringRes(R.string.label_back)
    }
}