package com.mobileplus.dummytriluc.ui.login.main

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.view.View
import androidx.core.content.res.ResourcesCompat
import com.core.BaseFragmentZ
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.mobileplus.dummytriluc.DummyTriLucApplication
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.data.request.SocialLoginRequest
import com.mobileplus.dummytriluc.databinding.FragmentLoginBinding
import com.mobileplus.dummytriluc.ui.login.register.SignUpFragment
import com.mobileplus.dummytriluc.ui.login.register.stepbystep.RegisterStep1Fragment
import com.mobileplus.dummytriluc.ui.login.signin.SignInFragment
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.utils.language.SPUtil
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentLogin
import com.mobileplus.dummytriluc.ui.utils.extensions.deviceId
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.utils.language.LanguageEnum
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.viewmodel.ext.android.viewModel

class LoginFragment : BaseFragmentZ<FragmentLoginBinding>() {

    override fun getLayoutBinding(): FragmentLoginBinding =
        FragmentLoginBinding.inflate(layoutInflater)

    private val loginViewModel: LoginViewModel by viewModel()
    private var googleSignInClient: GoogleSignInClient? = null
    private var googleSignInAccount: GoogleSignInAccount? = null
    val facebookCallback: CallbackManager by lazy { CallbackManager.Factory.create() }
    private val socialRequest by lazy { SocialLoginRequest() }

    companion object {
        const val RC_SIGN_IN_GG = 1000
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val gso =
            GoogleSignInOptions
                .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.server_client_id))
                .requestEmail()
                .build()
        googleSignInClient =
            GoogleSignIn.getClient(
                requireActivity(),
                gso
            )
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        setupLanguage()
        disposeViewModel()
        initAction()
    }

    private fun disposeViewModel() {
        loginViewModel.run {
            addDispose(
                isLoading.subscribe {
                    if (it) showDialog()
                    else hideDialog()
                },
                rxMessage.subscribe { toast(it) },
                rxLoginSuccess.subscribe {
                    if (it) {
                        startActivity(MainActivity::class.java)
                        finish()
                    }
                }, rxShouldUpdateInfo.subscribe {
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


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        facebookCallback.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN_GG /*&& resultCode == RESULT_OK*/) {
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInGGResult(task)
            } catch (e: ApiException) {
                logErr("TAG", "signInResult:failed code=" + e.statusCode)
            }
        }
    }

    private fun isLoggedInGoogle(): Boolean {
        googleSignInAccount =
            GoogleSignIn.getLastSignedInAccount(DummyTriLucApplication.getInstance())
        return googleSignInAccount != null && !googleSignInAccount!!.isExpired
    }

    private fun checkGoogleState() {
//        if (isLoggedInGoogle()) {
//            googleLogin()
//        } else {
        googleLogin(googleSignInClient)
//        }
    }

    private fun googleLogin() {
        socialRequest.apply {
            socialToken = googleSignInAccount?.idToken ?: ""
            uuid = deviceId
            type = ApiConstants.SOCIAL_TYPE_GOOGLE
        }
        showDialog()
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            hideDialog()
            socialRequest.tokenPush = if (task.isSuccessful) task.result else "fake_token"
            loginViewModel.doSocialLogin(socialRequest)
        }.addOnFailureListener { e ->
            hideDialog()
            socialRequest.tokenPush = "fake_token"
            loginViewModel.doSocialLogin(socialRequest)
            e.logErr()
        }
    }


    private fun handleSignInGGResult(task: Task<GoogleSignInAccount>) {
        googleSignInAccount = task.getResult(ApiException::class.java)
        googleLogin()
    }

    private fun googleLogin(googleSignInClient: GoogleSignInClient?) {
        googleSignInClient?.let {
            startActivityForResult(googleSignInClient.signInIntent, RC_SIGN_IN_GG)
        }
    }

    private fun initAction() {
        binding.tvLogin.clickWithDebounce {
            postNormal(
                EventNextFragmentLogin(
                    SignInFragment::class.java, true
                )
            )
        }

        binding.buttonFaceBookLogin.clickWithDebounce {
            loginViaFacebook(requireActivity())
        }

        binding.buttonGoogleLogin.clickWithDebounce {
            checkGoogleState()
        }
    }

    private fun setupLanguage() {
        binding.spLanguage.text = LanguageEnum.values().find {
            it.code == SPUtil.getInstance(requireContext())?.getSelectLanguage()
        }?.countryName ?: LanguageEnum.VI.countryName
        binding.spLanguage.clickWithDebounce { view ->
            val dataSource = LanguageEnum.values()
                .map { language -> CustomSpinner.SpinnerItem(language.countryName, language.code) }
            CustomSpinner(view, requireContext())
                .setWidthWindow(resources.getDimension(R.dimen._100sdp))
                .setDataSource(dataSource)
                .build()
                .setOnSelectedItemCallback { item ->
                    val languageEnum =
                        LanguageEnum.values().find { it.code == item.id } ?: LanguageEnum.VI
                    LocalManageUtil.saveSelectLanguage(
                        requireContext(),
                        languageEnum
                    )
                    postNormal(EventChangeLanguage(languageEnum.locale))
                }
        }
    }

    private fun isLoggedInFacebook(): Boolean {
        val accessTokenFacebook = AccessToken.getCurrentAccessToken()
        return accessTokenFacebook != null && !accessTokenFacebook.isExpired
    }

    private fun loginViaFacebook(activity: Activity) {
        if (isLoggedInFacebook()) {
            LoginManager.getInstance().logOut()
        } else {
            LoginManager.getInstance().logInWithReadPermissions(
                activity,
                listOf("email", "public_profile")
            )
            LoginManager.getInstance().registerCallback(facebookCallback,
                object : FacebookCallback<LoginResult> {
                    override fun onSuccess(result: LoginResult?) {
                        if (result != null) {
                            socialRequest.apply {
                                socialToken = result.accessToken.token
                                uuid = deviceId
                                type = ApiConstants.SOCIAL_TYPE_FACEBOOK
                            }
                            showDialog()
                            FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                                hideDialog()
                                socialRequest.tokenPush =
                                    if (task.isSuccessful) task.result else "fake_token"
                                loginViewModel.doSocialLogin(socialRequest)
                            }.addOnFailureListener { e ->
                                hideDialog()
                                socialRequest.tokenPush = "fake_token"
                                loginViewModel.doSocialLogin(socialRequest)
                                e.logErr()
                            }
                        } else {
                            toast(loadStringRes(R.string.can_not_login_with_facebook))
                        }
                    }

                    override fun onCancel() {
                        toast(loadStringRes(R.string.login_with_facebook_has_been_canceled))
                    }

                    override fun onError(error: FacebookException?) {
                        error?.logErr()
                    }
                })
        }
    }


    private fun setTextRegister() {
        val str = loadStringRes(R.string.question_navigate_register)
        val wordToSpan: Spannable = SpannableString(str)

        wordToSpan.setSpan(
            ForegroundColorSpan(
                ResourcesCompat.getColor(resources, R.color.white, null)
            ), 0, str.indexOf("?") + 1,
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
        binding.tvLogin.text = loadStringRes(R.string.login)
        binding.tvOr.text = loadStringRes(R.string.label_or)
        binding.txtLabelLoginWithFacebook.text = loadStringRes(R.string.label_sign_in_with_facebook)
        binding.txtLabelLoginWithGoogle.text = loadStringRes(R.string.label_sign_in_with_google)
        setTextRegister()
    }

}