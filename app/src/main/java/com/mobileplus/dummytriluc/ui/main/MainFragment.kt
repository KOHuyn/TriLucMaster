package com.mobileplus.dummytriluc.ui.main

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.BuildConfig
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.UserInfo
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.databinding.FragmentMainBinding
import com.mobileplus.dummytriluc.service.MessageService
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.login.main.LoginActivity
import com.mobileplus.dummytriluc.ui.login.password.UpdatePasswordFragment
import com.mobileplus.dummytriluc.ui.main.adapter.LeftMenuAdapter
import com.mobileplus.dummytriluc.ui.main.challenge.ChallengeFragment
import com.mobileplus.dummytriluc.ui.main.challenge.detail.ChallengeDetailFragment
import com.mobileplus.dummytriluc.ui.main.chat.chatmessage.ChatMessageFragment
import com.mobileplus.dummytriluc.ui.main.chat.chatroom.ChatRoomFragment
import com.mobileplus.dummytriluc.ui.main.coach.CoachMainFragment
import com.mobileplus.dummytriluc.ui.main.coach.assigned_exercise.AssignedExerciseFragment
import com.mobileplus.dummytriluc.ui.main.coach.check.CoachNotActivatedFragment
import com.mobileplus.dummytriluc.ui.main.coach.check.CoachUnqualifiedFragment
import com.mobileplus.dummytriluc.ui.main.coach.disciple.DiscipleFragment
import com.mobileplus.dummytriluc.ui.main.coach.group.CoachGroupFragment
import com.mobileplus.dummytriluc.ui.main.coach.register.CoachRegisterLauncherFragment
import com.mobileplus.dummytriluc.ui.main.home.HomeFragment
import com.mobileplus.dummytriluc.ui.main.news.NewsFragment
import com.mobileplus.dummytriluc.ui.main.news.detail.NewsDetailFragment
import com.mobileplus.dummytriluc.ui.main.notification.NotificationFragment
import com.mobileplus.dummytriluc.ui.main.notification.detail.NotificationDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.PracticeFragment
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.language.SPUtil
import com.mobileplus.dummytriluc.ui.utils.TypeCoach
import com.mobileplus.dummytriluc.ui.utils.eventbus.*
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.utils.language.LanguageEnum
import com.mobileplus.dummytriluc.ui.utils.language.LocalManageUtil
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.ext.*
import com.widget.ViewPagerAdapter
import io.reactivex.android.schedulers.AndroidSchedulers
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.sharedViewModel

class MainFragment : BaseFragmentZ<FragmentMainBinding>() {
    override fun getLayoutBinding(): FragmentMainBinding =
        FragmentMainBinding.inflate(layoutInflater)

    private val vm by sharedViewModel<MainViewModel>()
    private val leftMenuAdapter by lazy { LeftMenuAdapter() }
    private val homeFragmentMain by lazy { HomeFragment() }
    private val practiceFragment by lazy { PracticeFragment() }
    private val challengeFragment by lazy { ChallengeFragment() }
    private val newsFragment by lazy { NewsFragment() }
    private val notificationFragment by lazy { NotificationFragment() }

    private val adapterViewPager by lazy {
        ViewPagerAdapter(
            childFragmentManager,
            listOf<Fragment>(
                homeFragmentMain,
                practiceFragment,
                challengeFragment,
                newsFragment,
                notificationFragment
            ), emptyList()
        )
    }

    var currViewPager = AppConstants.INTEGER_DEFAULT
        set(value) {
            field = value
            logErr("currViewPager:$field")
            runOnUiThread {
                if (currViewPager < adapterViewPager.count) {
                    binding.bottomNavMain.selectedItemId = when (field) {
                        0 -> R.id.home
                        1 -> R.id.practice
                        2 -> R.id.challenge
                        3 -> R.id.news
                        4 -> R.id.notification
                        else -> R.id.home
                    }
                    if (binding.homeViewPager.currentItem != field) {
                        binding.homeViewPager.setCurrentItem(field, false)
                    }
                }
            }
        }
        get() = binding.homeViewPager.currentItem

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        setupLanguage()
        binding.txtVersionApp.text = BuildConfig.VERSION_NAME
        setupViewPagerHome()
        setupNavigationView()
        vm.user?.let { updateUser(it) }
        disposeViewModel()
        handleClick()
    }

    private fun handleClick() {
        homeFragmentMain.onSwitchToPractice = HomeFragment.CallbackToFragment {
            currViewPager = 1
        }

    }

    private fun setupLanguage() {
        binding.spLanguage.text = LanguageEnum.values().find {
            it.code == SPUtil.getInstance(requireContext()).getSelectLanguage()
        }?.countryName ?: LanguageEnum.VI.countryName
        binding.spLanguage.clickWithDebounce { view ->
            val dataSource = LanguageEnum.values()
                .map { language -> CustomSpinner.SpinnerItem(language.countryName, language.code) }
            CustomSpinner(view, requireContext())
                .setWidthWindow(resources.getDimension(R.dimen._100sdp))
                .setDataSource(dataSource)
                .build()
                .setOnSelectedItemCallback { item ->
                    if (item.id != SPUtil.getInstance(requireContext()).getSelectLanguage()) {
                        disconnectWhenChangeLanguageBle {
                            changeLanguageLocal(item.id)
                        }
                    }
                }
        }
    }

    private fun changeLanguageLocal(codeLanguage: String) {
        val languageEnum =
            LanguageEnum.values().find { it.code == codeLanguage } ?: LanguageEnum.VI
        LocalManageUtil.saveSelectLanguage(
            requireContext(),
            languageEnum
        )
        postNormal(EventChangeLanguage(languageEnum.locale))
    }

    private fun disconnectWhenChangeLanguageBle(action: () -> Unit) {
        if ((activity as? MainActivity)?.isConnectedBle == true) {
            YesNoButtonDialog()
                .setMessage(getString(R.string.warning_disconnect_ble_when_change_language))
                .showDialog(parentFragmentManager)
                .setOnCallbackAcceptButtonListener {
                    action()
                }
                .setOnCallbackCancelButtonListener {
                    binding.spLanguage.text = LanguageEnum.values().find {
                        it.code == SPUtil.getInstance(requireContext()).getSelectLanguage()
                    }?.countryName ?: LanguageEnum.VI.countryName
                }
        } else {
            action()
        }
    }

    private fun disposeViewModel() {
        vm.apply {
            addDispose(
                isLoading.subscribe {
                    if (it) {
                        showDialog()
                    } else {
                        hideDialog()
                    }
                },
                rxMessage.observeOn(AndroidSchedulers.mainThread()).subscribe {
                    toast(it)
                },
                logoutSuccess.subscribe {
                    startActivity(LoginActivity::class.java)
                    finish()
                },
                rxCoachType.subscribe {
                    val typeCoach = it.first
                    val data = it.second
                    when (typeCoach) {
                        TypeCoach.MEMBER -> {
                            val isAcceptIsMaster: Boolean =
                                data?.get(ApiConstants.ACCEPT_TO_MASTER)?.asBoolean ?: false
                            if (isAcceptIsMaster) {
                                postNormal(EventNextFragmentMain(CoachRegisterLauncherFragment::class.java))
                            } else {
                                val titleMaster =
                                    data?.getAsJsonObject(ApiConstants.LEVEL_TO_MASTER)
                                        ?.get(ApiConstants.TITLE)?.asString
                                val bundle = Bundle().apply {
                                    putString(
                                        CoachUnqualifiedFragment.RANK_MASTER_ARG,
                                        titleMaster ?: ""
                                    )
                                }
                                postNormal(
                                    EventNextFragmentMain(
                                        CoachUnqualifiedFragment::class.java,
                                        bundle,
                                        true
                                    )
                                )
                            }
                        }
                        TypeCoach.REVIEW -> {
                            val timeRequest = data?.get(ApiConstants.TIME_REQUEST)?.asString
                            val bundle = Bundle().apply {
                                putString(CoachNotActivatedFragment.TIME_REQUEST_ARG, timeRequest)
                            }
                            postNormal(
                                EventNextFragmentMain(
                                    CoachNotActivatedFragment::class.java,
                                    bundle,
                                    true
                                )
                            )
                        }
                        TypeCoach.MASTER -> {
                            postNormal(EventNextFragmentMain(CoachMainFragment::class.java))
                        }
                        TypeCoach.UNKNOWN -> {
                            toast(loadStringRes(R.string.error_can_not_open_coach_mode))
                        }
                    }
                }
            )
        }
    }

    private fun setupViewPagerHome() {
        binding.homeViewPager.adapter = adapterViewPager
        binding.homeViewPager.offscreenPageLimit = adapterViewPager.count
        setupBottomAppBar()
    }

    private fun setupNavigationView() {
        binding.frameProfile.setOnClickListener {
            UserInfoFragment.openInfoUser()
        }
        binding.drawerLayoutMain.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
        binding.btnCloseNav.clickWithDebounce { closeDrawer() }

        setUpRcv(binding.rcvMenuLeft, leftMenuAdapter)
        configDataLeftMenu()

        binding.btnLogout.setOnClickListener {
            YesNoButtonDialog()
                .setTitle(loadStringRes(R.string.label_alert))
                .setMessage(loadStringRes(R.string.question_logout))
                .setTextAccept(loadStringRes(R.string.label_accept))
                .setTextCancel(loadStringRes(R.string.cancel))
                .showDialog(parentFragmentManager, YesNoButtonDialog.TAG)
                .setOnCallbackAcceptButtonListener {
                    vm.dataManager.password = ""
                    vm.logout()
                }
        }
    }

    fun isOpenDrawer(): Boolean {
        return binding.drawerLayoutMain.isDrawerOpen(GravityCompat.START)
    }

    fun closeDrawer() {
        binding.drawerLayoutMain.closeDrawers()
    }

    private fun setupBottomAppBar() {
        binding.bottomNavMain.itemIconTintList = null
        binding.bottomNavMain.setOnNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.home -> {
                    if (currViewPager != 0) {
                        binding.homeViewPager.setCurrentItem(0, false)
                    } else {
                        homeFragmentMain.clickToSelf()
                    }
                }
                R.id.practice -> {
                    binding.homeViewPager.setCurrentItem(1, false)
                    practiceFragment.initView()
                }
                R.id.challenge -> {
                    binding.homeViewPager.setCurrentItem(2, false)
                    challengeFragment.initView()
                }
                R.id.news -> {
                    binding.homeViewPager.setCurrentItem(3, false)
                    newsFragment.initView()
                }
                R.id.notification -> {
                    binding.homeViewPager.setCurrentItem(4, false)
                    notificationFragment.initView()
                }
            }
            true
        }
    }

    private fun configDataLeftMenu() {
        val itemsLeftMenu = mutableListOf<LeftMenuAdapter.ItemLeftMenu>()
        itemsLeftMenu.add(
            LeftMenuAdapter.ItemLeftMenu(
                R.drawable.ic_message,
                loadStringRes(R.string.message),
                LeftMenuAdapter.LeftMenu.MESSAGE
            )
        )
        itemsLeftMenu.add(
            LeftMenuAdapter.ItemLeftMenu(
                R.drawable.ic_coach_mode,
                loadStringRes(R.string.coach_mode),
                LeftMenuAdapter.LeftMenu.COACH_MODE
            )
        )
        itemsLeftMenu.add(
            LeftMenuAdapter.ItemLeftMenu(
                R.drawable.ic_my_group,
                loadStringRes(R.string.my_group),
                LeftMenuAdapter.LeftMenu.MY_GROUP
            )
        )
        itemsLeftMenu.add(
            LeftMenuAdapter.ItemLeftMenu(
                R.drawable.ic_assignments,
                loadStringRes(R.string.assignments),
                LeftMenuAdapter.LeftMenu.ASSIGNMENTS
            )
        )
        itemsLeftMenu.add(
            LeftMenuAdapter.ItemLeftMenu(
                R.drawable.ic_change_password,
                loadStringRes(R.string.change_password),
                LeftMenuAdapter.LeftMenu.CHANGE_PASSWORD
            )
        )
//        itemsLeftMenu.add(
//            LeftMenuAdapter.ItemLeftMenu(
//                R.drawable.ic_information,
//                loadStringRes(R.string.information),
//                LeftMenuAdapter.LeftMenu.INFORMATION
//            )
//        )

//        itemsLeftMenu.add(
//            LeftMenuAdapter.ItemLeftMenu(
//                R.drawable.ic_connecting_ble,
//                "Develop Bluetooth",
//                LeftMenuAdapter.LeftMenu.DEVELOP_BLE
//            )
//        )
        leftMenuAdapter.items = itemsLeftMenu
        leftMenuAdapter.onClickItem = LeftMenuAdapter.OnClickItem { type ->
            when (type) {
                LeftMenuAdapter.LeftMenu.FRIEND -> {
                    toast(loadStringRes(R.string.feature_developing))
                }
                LeftMenuAdapter.LeftMenu.MESSAGE -> {
                    ChatRoomFragment.openFragment()
                }
                LeftMenuAdapter.LeftMenu.MY_GROUP -> {
                    CoachGroupFragment.openFragment(isGuest = true)
                }
                LeftMenuAdapter.LeftMenu.ASSIGNMENTS -> {
                    AssignedExerciseFragment.openFragment()
                }
                LeftMenuAdapter.LeftMenu.COACH_MODE -> {
                    vm.checkScreenCoach()
                }
                LeftMenuAdapter.LeftMenu.CHANGE_PASSWORD -> {
                    UpdatePasswordFragment.openFragment()
                }
                LeftMenuAdapter.LeftMenu.INFORMATION -> {
//                    CoachSaveDraftFragment.openFragment(149)
                }
                else -> {
                }
            }
            Handler(Looper.getMainLooper()).postDelayed({ closeDrawer() }, 100)
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
    fun onHandleNotification(ev: EventHandleNotification) {
        logErr("onHandleNotification")
        vm.readNotification(ev.notification.id)
        when (ev.notification.type) {
            MessageService.TYPE_DETAIL -> {
                if (currViewPager != 4)
                    currViewPager = 4
                else
                    NotificationDetailFragment.openFragment(ev.notification.id)
            }
            MessageService.TYPE_MESSAGE -> {
                ChatRoomFragment.openFragment()
                if (ev.notification.itemChat != null) {
                    val gson by inject<Gson>()
                    Handler(Looper.getMainLooper()).postDelayed({
                        ChatMessageFragment.openFragment(
                            ev.notification.itemChat!!,
                            gson
                        )
                    }, 200)
                }
            }
            MessageService.TYPE_NEWS -> {
                currViewPager = 3
                ev.notification.linkId?.let { NewsDetailFragment.openFragment(it) }
            }
            MessageService.TYPE_CHALLENGES -> {
                currViewPager = 2
                ev.notification.linkId?.let { ChallengeDetailFragment.openFragment(it) }
            }
            MessageService.TYPE_RECEIVE_MASTER -> {
                DiscipleFragment.openFragment(DiscipleFragment.TAB_WAITING)
            }
            MessageService.TYPE_ASSIGN -> {
                AssignedExerciseFragment.openFragment()
            }
            MessageService.TYPE_ACCEPT_BAI_SU -> {
                NotificationDetailFragment.openFragment(ev.notification.id)
            }
        }
    }

    @Subscribe
    fun onNavigateDeepLink(ev: EventNavigateDeepLink) {
        if (ev.id != AppConstants.INTEGER_DEFAULT) {
            when (ev.type) {
                ApiConstants.CHALLENGES -> {
                    currViewPager = 2
                    ev.id.let { ChallengeDetailFragment.openFragment(it) }
                }
                ApiConstants.PRACTICE -> {
                    currViewPager = 1
                    ev.id.let { PracticeDetailFragment.openFragment(it) }
                }
                ApiConstants.NEWS -> {
                    currViewPager = 3
                    NewsDetailFragment.openFragment(ev.id)
                }
                "" -> {
                }
            }
        }
    }

    @Subscribe
    fun onUpdateInfo(e: EventUpdateInfo) {
        updateUser(e.user)
    }

    private fun updateUser(user: UserInfo) {
        binding.imgAvatarUserLeftMenu.show(user.avatarPath)
        binding.txtNameUserLeftMenu.text = user.fullName ?: "----"
        binding.txtIdUserLeftMenu.text = "${user.id ?: -1}"
        logErr("updateUser")
    }

    @Subscribe
    fun onClickNavBar(e: EventClickNavBar) {
        binding.drawerLayoutMain.openDrawer(GravityCompat.START)
    }

    @Subscribe
    fun onLanguageChange(ev: EventChangeLanguage) {
        loadLanguageResource()
    }

    private fun loadLanguageResource() {
        configDataLeftMenu()
        binding.btnLogout.text = loadStringRes(R.string.logout)
        binding.bottomNavMain.menu.findItem(R.id.home).title = loadStringRes(R.string.home)
        binding.bottomNavMain.menu.findItem(R.id.practice).title = loadStringRes(R.string.practice)
        binding.bottomNavMain.menu.findItem(R.id.challenge).title =
            loadStringRes(R.string.challenge)
        binding.bottomNavMain.menu.findItem(R.id.news).title = loadStringRes(R.string.news)
        binding.bottomNavMain.menu.findItem(R.id.notification).title =
            loadStringRes(R.string.notification)
    }
}