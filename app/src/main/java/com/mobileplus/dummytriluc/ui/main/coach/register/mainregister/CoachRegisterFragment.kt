package com.mobileplus.dummytriluc.ui.main.coach.register.mainregister

import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.CoachRegisterRequest
import com.mobileplus.dummytriluc.ui.main.coach.register.CoachRegisterSuccessFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.popBackStackDelay
import com.mobileplus.dummytriluc.ui.utils.extensions.setValue
import com.utils.KeyboardUtils
import com.utils.StringUtils
import com.utils.ext.*
import kotlinx.android.synthetic.main.fragment_coach_register.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class CoachRegisterFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_register
    private val coachRegisterViewModel by viewModel<CoachRegisterViewModel>()
    private val request by lazy { CoachRegisterRequest() }
    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel(coachRegisterViewModel)
        setupView()
        handleClick()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    private fun setupView() {
        val user = coachRegisterViewModel.getUserInfo
        user?.fullName?.let { edtInputFullNameCoach.setValue(it) }
        user?.email?.let { edtInputEmailCoach.setValue(it) }
    }

    private fun callbackViewModel(vm: CoachRegisterViewModel) {
        addDispose(vm.isLoading.subscribe { if (it) showDialog() else hideDialog() })
        addDispose(vm.rxMessage.subscribe { toast(it) })
        addDispose(vm.rxRegisterSuccess.subscribe {
            if (it) postNormal(
                EventNextFragmentMain(CoachRegisterSuccessFragment::class.java)
            )
        })
    }

    private fun handleClick() {
        btnBackCoachRegister.clickWithDebounce { onBackPressed() }
        btnRegisterCoach.clickWithDebounce {
            if (validateInputText()) {
                coachRegisterViewModel.registerCoach(request)
            }
        }
    }

    private fun validateInputText(): Boolean {
        val fullName = edtInputFullNameCoach.text.toString()
        val email = edtInputEmailCoach.text.toString()
        val phone = edtInputYourPhoneCoach.text.toString()

//        edtInputYourPhoneCoach.showKeyBoardWhenError(phone.isBlank()) {
//            toast(loadStringRes(R.string.error_empty_phone))
//        }
//        edtInputEmailCoach.showKeyBoardWhenError(email.isBlank()) {
//            toast(loadStringRes(R.string.error_empty_email))
//        }
        edtInputFullNameCoach.showKeyBoardWhenError(fullName.isBlank()) {
            toast(loadStringRes(R.string.error_empty_fullname))
        }
//        if (fullName.isBlank() || email.isBlank() || phone.isBlank()) return false
        if (fullName.isBlank())  return false

        edtInputEmailCoach.showKeyBoardWhenError(
            email.isNotEmpty() && !StringUtils.isValidEmail(email)
        ) {
            toast(loadStringRes(R.string.error_wrong_email))
        }
        if (email.isNotEmpty() && !StringUtils.isValidEmail(email)) return false

        edtInputYourPhoneCoach.showKeyBoardWhenError(
            phone.isNotEmpty() && !StringUtils.isValidPhoneNumber(phone)
        ) {
            toast(loadStringRes(R.string.error_wrong_phone))
        }
        if (phone.isNotEmpty() && !StringUtils.isValidPhoneNumber(phone)) return false
        if (!cbAgreeTermOfServiceCoach.isChecked) {
            toast(getString(R.string.require_term_of_service))
            return false
        }
        request.fullName = fullName
        request.email = email
        request.phone = phone
        return true
    }

    private fun EditText.showKeyBoardWhenError(
        isError: Boolean = false,
        action: (Unit) -> Unit
    ) {
        if (isError) {
            this.setBackgroundResource(R.drawable.background_stroke_red)
            KeyboardUtils.forceShowKeyboard(this)
            action.invoke(Unit)
        } else {
            this.setBackgroundColorz(R.color.white)
        }
    }
}