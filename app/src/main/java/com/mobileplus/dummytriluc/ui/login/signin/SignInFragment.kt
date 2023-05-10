package com.mobileplus.dummytriluc.ui.login.signin

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.*
import android.text.method.HideReturnsTransformationMethod
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import android.widget.EditText
import androidx.core.content.res.ResourcesCompat
import com.core.BaseFragmentZ
import com.google.firebase.messaging.FirebaseMessaging
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.LoginRequest
import com.mobileplus.dummytriluc.databinding.FragmentSigninBinding
import com.mobileplus.dummytriluc.ui.login.password.forgot.ForgotPasswordFragment
import com.mobileplus.dummytriluc.ui.login.register.SignUpFragment
import com.mobileplus.dummytriluc.ui.login.register.stepbystep.RegisterStep1Fragment
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.utils.KeyboardUtils
import com.utils.ext.*
import com.utils.ext.value
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class SignInFragment : BaseFragmentZ<FragmentSigninBinding>() {

    override fun getLayoutBinding(): FragmentSigninBinding =
        FragmentSigninBinding.inflate(layoutInflater)

    private val viewModel by viewModel<SignInViewModel>()
    private val request by lazy { LoginRequest() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        fillUserName()
        loadLanguageResource()
        disposableViewModel()
        initAction()
    }

    fun fillUserName() {
        binding.editEmail.setValue(viewModel.userName)
        binding.editPassword.setValue(viewModel.password)
    }

    private fun disposableViewModel() {
        viewModel.apply {
            addDispose(
                isLoading.subscribe {
                    if (it) {
                        showDialog()
                    } else {
                        hideDialog()
                    }
                },
                rxMessage.subscribe {
                    toast(it)
                },
                rxLoginSuccess.subscribe {
                    if (it) {
                        startActivity(MainActivity::class.java)
                        finish()
                    }
                },
                rxShouldUpdateInfo.subscribe {
                    if (it) {
                        postNormal(
                            EventNextFragmentLogin(
                                RegisterStep1Fragment::class.java,
                                true
                            )
                        )
                    }
                }
            )
        }
    }

    private fun initAction() {
        binding.tvForgot.clickWithDebounce {
            postNormal(
                EventNextFragmentLogin(
                    ForgotPasswordFragment::class.java, true
                )
            )
        }

        binding.tvLogin.clickWithDebounce {
            validData()
        }
        binding.cbShowPassword.setOnCheckedChangeListener { _, isChecked ->
            binding.editPassword.transformationMethod =
                if (isChecked) HideReturnsTransformationMethod.getInstance() else PasswordTransformationMethod.getInstance()
            binding.editPassword.setSelection(binding.editPassword.length())
        }
    }

    private fun validData() {
        val email = binding.editEmail.text.toString()
        val password = binding.editPassword.text.toString()
        if (email.isBlank()) {
            toast(loadStringRes(R.string.error_empty_email))
            showKeyBoard(binding.editEmail)
            return
        } else {
            if (!email.isEmail()) {
                toast(loadStringRes(R.string.error_wrong_email))
                showKeyBoard(binding.editEmail)
                return
            }
        }
        if (password.isBlank()) {
            toast(loadStringRes(R.string.error_empty_password))
            showKeyBoard(binding.editPassword)
            return
        }
        request.apply {
            this.email = email
            this.password = password
            uuid = deviceId
        }
        hideKeyboard()
        showDialog()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            hideDialog()
            request.tokenPush = if (task.isSuccessful) task.result else "fake_token"
            viewModel.login(request)
        }.addOnFailureListener { e ->
            hideDialog()
            request.tokenPush = "fake_token"
            viewModel.login(request)
            e.logErr()
        }
    }

    private fun showKeyBoard(editText: EditText) {
        KeyboardUtils.showKeyboard(requireActivity())
        editText.requestFocus()
    }

    private fun configRegisterText() {
        val str = loadStringRes(R.string.question_navigate_register)

        val wordToSpan: Spannable = SpannableString(str)

        wordToSpan.setSpan(
            ForegroundColorSpan(
                ResourcesCompat.getColor(
                    resources,
                    R.color.white,
                    null
                )
            ),
            0,
            str.indexOf("?") + 1,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        wordToSpan.setSpan(
            StyleSpan(Typeface.BOLD),
            str.indexOf("?") + 1,
            str.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        val clickableSpan: ClickableSpan = object : ClickableSpan() {
            override fun onClick(textView: View) {
                postNormal(
                    EventNextFragmentLogin(
                        SignUpFragment::class.java, true
                    )
                )
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
                ds.color = Color.parseColor("#E32828")
            }
        }

        wordToSpan.setSpan(
            clickableSpan,
            str.indexOf("?") + 1,
            str.length,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        binding.tvRegister.text = wordToSpan
        binding.tvRegister.movementMethod = LinkMovementMethod.getInstance()
        binding.tvRegister.highlightColor = Color.TRANSPARENT
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
        configRegisterText()
        binding.tvHeader.text = loadStringRes(R.string.welcome_back)
        binding.tvHeader2.text = loadStringRes(R.string.login_with_your_account)
        binding.editEmail.hint = loadStringRes(R.string.email)
        binding.editPassword.hint = loadStringRes(R.string.password)
        binding.tvLogin.text = loadStringRes(R.string.login)
        binding.tvForgot.text = loadStringRes(R.string.forgot_password)
    }
}