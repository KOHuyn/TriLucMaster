package com.mobileplus.dummytriluc.ui.main.practice

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.*
import com.mobileplus.dummytriluc.databinding.FragmentPracticeBinding
import com.mobileplus.dummytriluc.ui.main.practice.adapter.OnItemClickPractice
import com.mobileplus.dummytriluc.ui.main.practice.adapter.PracticeAdapter
import com.mobileplus.dummytriluc.ui.main.practice.detail.PracticeDetailFragment
import com.mobileplus.dummytriluc.ui.main.practice.filter.PracticeFilterFragment
import com.mobileplus.dummytriluc.ui.main.practice.folder.PracticeFolderFragment
import com.mobileplus.dummytriluc.ui.main.user.UserInfoFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.language.EventChangeLanguage
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class PracticeFragment : BaseFragmentZ<FragmentPracticeBinding>() {

    override fun getLayoutBinding(): FragmentPracticeBinding =
        FragmentPracticeBinding.inflate(layoutInflater)

    private val vm by viewModel<PracticeMainViewModel>()
    private val adapterPractice by lazy { PracticeAdapter() }
    private val rcvPractice by lazy { binding.rcvPracticeMain }
    private val gson by inject<Gson>()
    private var initialized = false

    fun initView() {
        if (!initialized) {
            vm.getDataPractice()
            initialized = true
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadLanguageResource()
        callbackViewModel()
        configView()
        handleClick()
    }

    private fun handleClick() {
        binding.layoutSearchPractice.clickWithDebounce {
            postNormal(EventNextFragmentMain(PracticeFilterFragment::class.java, true))
        }
    }

    private fun configView() {
        val paddingRcv = resources.getDimensionPixelOffset(R.dimen.space_8)
        rcvPractice.getRcv().setPadding(0, paddingRcv, 0, paddingRcv)
        rcvPractice.getRcv().clipToPadding = false
        rcvPractice.setUpRcv(adapterPractice)
        rcvPractice.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            vm.getDataPractice()
        }
        rcvPractice.getRcv().addOnItemTouchListener(object : RecyclerView.OnItemTouchListener {
            override fun onInterceptTouchEvent(rv: RecyclerView, e: MotionEvent): Boolean {
                if (e.action == MotionEvent.ACTION_DOWN && rv.scrollState == RecyclerView.SCROLL_STATE_SETTLING) {
                    rv.stopScroll()
                }
                return false
            }

            override fun onTouchEvent(rv: RecyclerView, e: MotionEvent) {}

            override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {}
        })
        adapterPractice.onItemChildClick = OnItemClickPractice { item, viewType ->
            when (viewType) {
                BasePracticeItem.TYPE_HEADER -> {
                    val bundle = Bundle().apply {
                        putString(
                            PracticeFilterFragment.KEY_ITEM_FILTER,
                            gson.toJson(item as ItemTitlePractice)
                        )
                    }
                    postNormal(
                        EventNextFragmentMain(
                            PracticeFilterFragment::class.java,
                            bundle,
                            true
                        )
                    )
                }
                BasePracticeItem.TYPE_SINGLE_PRACTICE -> {
                    PracticeDetailFragment.openFragment((item as ItemPracticeItemContent).id)
                }
                BasePracticeItem.TYPE_FOLDER_PRACTICE -> {
                    PracticeFolderFragment.openFragment((item as ItemPracticeItemContent).id)
                }
                BasePracticeItem.TYPE_CHILD_MASTER -> {
                    (item as ItemPracticeItemMaster).id?.let { idMaster ->
                        UserInfoFragment.openInfoGuest(idMaster)
                    }
                }
                BasePracticeItem.TYPE_CHILD_LOCAL -> {
                    PracticeDetailFragment.openFragment((item as ItemPracticeLocal).id)
                }
            }
        }
    }

    private fun enableRefresh() = rcvPractice.enableRefresh()
    private fun cancelRefresh() = rcvPractice.cancelRefresh()

    private fun callbackViewModel() {
        addDispose(vm.isLoading.subscribe { if (it) enableRefresh() else cancelRefresh() })
        addDispose(vm.rxItemPracticeMain.subscribe {
            binding.noDataPracticeMain.root.setVisibility(it.isEmpty())
            adapterPractice.items = it
            rcvPractice.getRcv().setItemViewCacheSize(it.size)
            rcvPractice.getRcv().requestLayout()
        })
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
        binding.txtTitle.text = loadStringRes(R.string.practice)
        binding.edtSearch.hint = loadStringRes(R.string.search)
        adapterPractice.notifyDataSetChanged()
    }

}