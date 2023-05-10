package com.mobileplus.dummytriluc.ui.main.practice.filter

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.inputmethod.EditorInfo
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.jakewharton.rxbinding2.widget.RxTextView
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemTitlePractice
import com.mobileplus.dummytriluc.data.remote.ApiConstants
import com.mobileplus.dummytriluc.databinding.FragmentPracticeFilterBinding
import com.mobileplus.dummytriluc.ui.main.practice.filter.filtercoach.PracticeFilterCoachFragment
import com.mobileplus.dummytriluc.ui.main.practice.filter.filterpractice.PracticeFilterContentFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.KeyboardUtils
import com.utils.applyClickShrink
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.setVisibility
import com.widget.ViewPagerAdapter
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel
import java.util.concurrent.TimeUnit

class PracticeFilterFragment : BaseFragmentZ<FragmentPracticeFilterBinding>() {

    override fun getLayoutBinding(): FragmentPracticeFilterBinding =
        FragmentPracticeFilterBinding.inflate(layoutInflater)

    val practiceFilterViewModel by viewModel<PracticeFilterViewModel>()
    private var isShowFilter: Boolean = true
    private val gson by inject<Gson>()
    private var isSearch: Boolean = true
    private val fragFilterContent = PracticeFilterContentFragment()
    private val fragFilterCoach = PracticeFilterCoachFragment()
    private var isSelectedPage: Int = TAB_PRACTICE
        set(value) {
            field = value
            runOnUiThread {
                binding.vpPracticeFilter.currentItem = value
            }
        }
        get() = binding.vpPracticeFilter.currentItem
    private val adapterPagerFilter by lazy {
        ViewPagerAdapter(
            childFragmentManager,
            listOf(fragFilterContent, fragFilterCoach),
            listOf(getString(R.string.exercise), getString(R.string.coach_sort))
        )
    }
    private var keySubject: Int? = null
        set(value) {
            field = value
            if (field == null) {
                binding.spSectFilter.text = loadStringRes(R.string.sect)
            }
        }
    private var keyLevel: Int? = null
        set(value) {
            field = value
            if (field == null) {
                binding.spLevelFilter.text = loadStringRes(R.string.level)
            }
        }
    private var keySort: String? = null
        set(value) {
            field = value
            if (field == null) {
                binding.spSortFilter.text = loadStringRes(R.string.sort)
            }
        }
    private var keyword: String? = null
    private var itemFilter: ItemTitlePractice? = null

    companion object {
        const val KEY_ITEM_FILTER = "bundle_item_practice_filter"
        const val TAB_PRACTICE = 0
        const val TAB_MASTER = 1
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        loadAsset()
        setupPager()
        callbackViewModel()
        getArg()
        configView()
        handleClick()
    }

    private fun launch(onLaunch: () -> Unit) {
        Handler(Looper.getMainLooper()).postDelayed({
            onLaunch()
        }, 100)
    }

    private fun getArg() {
        if (arguments != null && arguments?.containsKey(KEY_ITEM_FILTER) == true) {
            isSearch = false
            val jsonItem by argument(KEY_ITEM_FILTER, "")
            itemFilter = gson.fromJson(jsonItem, ItemTitlePractice::class.java)
            if (itemFilter?.type == ItemTitlePractice.TYPE_MASTER) {
                isSelectedPage = TAB_MASTER
                launch {
                    practiceFilterViewModel.getDataMasterMore(itemFilter!!.id)
                }
            } else {
                isSelectedPage = TAB_PRACTICE
                launch {
                    practiceFilterViewModel.getDataPracticeMore(itemFilter!!.id)
                }
            }
            binding.txtHeaderPracticeFilter.text = itemFilter!!.title
        } else {
            isSearch = true
            binding.txtHeaderPracticeFilter.text = loadStringRes(R.string.search)
            launch {
                practiceFilterViewModel.searchPractice(keySubject, keyLevel, keySort, keyword)
                practiceFilterViewModel.searchMaster(keySubject, keyLevel, keySort, keyword)
            }
            Handler(Looper.getMainLooper()).postDelayed({
                KeyboardUtils.forceShowKeyboard(
                    binding.edtSearchPracticeFilter
                )
            }, 300)
        }
        binding.tabLayoutFilter.setVisibility(isSearch)
    }

    private fun hideKeyBoardWhenClick(vararg view: View) {
        for (vieww in view) {
            vieww.setOnTouchListener { v, _ ->
                v.performClick()
                hideKeyboard()
                if (binding.edtSearchPracticeFilter.isFocused) {
                    binding.edtSearchPracticeFilter.clearFocus()
                }
                false
            }
        }
    }

    private fun setupPager() {
        binding.vpPracticeFilter.adapter = adapterPagerFilter
        binding.tabLayoutFilter.setupWithViewPager(binding.vpPracticeFilter)
    }

    private fun loadAsset() {
        practiceFilterViewModel.getAllLevelDb()
        practiceFilterViewModel.getAllSubjectDb()
    }

    private fun searchingPractice() {
        if (isSearch) {
            practiceFilterViewModel.searchPractice(keySubject, keyLevel, keySort, keyword)
            practiceFilterViewModel.searchMaster(keySubject, keyLevel, keySort, keyword)
        } else {
            itemFilter?.let { item ->
                when (isSelectedPage) {
                    TAB_MASTER -> {
                        practiceFilterViewModel.getDataMasterMore(
                            item.id,
                            keySubject,
                            keyLevel,
                            keySort,
                            keyword,
                        )
                    }
                    TAB_PRACTICE -> {
                        practiceFilterViewModel.getDataPracticeMore(
                            item.id,
                            keySubject,
                            keyLevel,
                            keySort,
                            keyword,
                        )
                    }
                    else -> {
                    }
                }
            }
        }
    }

    private fun handleClick() {
        binding.btnBackPracticeFilter.clickWithDebounce { onBackPressed() }
        binding.btnFilterPracticeFilter.clickWithDebounce {
            isShowFilter = !isShowFilter
            binding.viewGroupFilterFilter.setVisibility(isShowFilter)
        }

        binding.edtSearchPracticeFilter.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (keyword?.isNotBlank() == true) {
                    searchingPractice()
                }
                true
            } else false
        }

        binding.edtSearchPracticeFilter.setOnFocusChangeListener { v, hasFocus ->
            if (hasFocus) {
                KeyboardUtils.forceShowKeyboard(v)
            } else {
                KeyboardUtils.hideKeyBoardWhenClickOutSide(v, requireActivity())
            }
        }

        binding.spSortFilter.clickWithDebounce {
            CustomSpinner(it, requireContext())
                .setBackGroundSpinner(Color.parseColor("#0F1838"))
                .setDataSource(
                    mutableListOf(
                        CustomSpinner.SpinnerItem(loadStringRes(R.string.all), ApiConstants.ALL),
                        CustomSpinner.SpinnerItem(
                            loadStringRes(R.string.popular),
                            ApiConstants.POPULAR
                        ),
                        CustomSpinner.SpinnerItem(loadStringRes(R.string.time), ApiConstants.TIME)
                    )
                )
                .build()
                .setOnSelectedItemCallback { item ->
                    keySort = if (item.id == ApiConstants.ALL) {
                        null
                    } else item.id
                    searchingPractice()
                }
        }
        binding.btnSearchPracticeFilter.clickWithDebounce {
            searchingPractice()
        }
        fragFilterContent.onFilterContentCallback = object :
            PracticeFilterContentFragment.OnFilterContentCallback {
            override fun setOnLoadMoreListener(currPage: Int) {
                if (isSearch) {
                    practiceFilterViewModel.searchPractice(
                        keySubject,
                        keyLevel,
                        keySort,
                        keyword,
                        currPage
                    )
                } else {
                    itemFilter?.let { item ->
                        practiceFilterViewModel.getDataPracticeMore(
                            item.id,
                            keySubject,
                            keyLevel,
                            keySort,
                            keyword,
                            currPage
                        )
                    }
                }
            }

            override fun refresh() {
                searchingPractice()
            }
        }
        fragFilterCoach.onFilterMasterCallback =
            object : PracticeFilterCoachFragment.OnFilterMasterCallback {
                override fun setOnLoadMoreListener(currPage: Int) {
                    if (isSearch) {
                        practiceFilterViewModel.searchMaster(
                            keySubject,
                            keyLevel,
                            keySort,
                            keyword,
                            currPage
                        )
                    } else {
                        itemFilter?.let { item ->
                            practiceFilterViewModel.getDataMasterMore(
                                item.id,
                                keySubject,
                                keyLevel,
                                keySort,
                                keyword,
                                currPage
                            )
                        }
                    }
                }

                override fun refresh() {
                    searchingPractice()
                }
            }
    }

    private fun configView() {
        binding.btnSearchPracticeFilter.applyClickShrink()
        addDispose(
            RxTextView.textChanges(binding.edtSearchPracticeFilter)
                .skipInitialValue()
                .debounce(200, TimeUnit.MILLISECONDS)
                .subscribe({
                    keyword = if (it.isBlank()) null
                    else it.toString()
                }, { it.logErr() })
        )
    }

    private fun callbackViewModel() {
        practiceFilterViewModel.apply {
            addDispose(rxAllLevel.subscribe { levels ->
                binding.spLevelFilter.clickWithDebounce {
                    CustomSpinner(it, requireContext())
                        .setBackGroundSpinner(Color.parseColor("#0F1838"))
                        .setDataSource(levels)
                        .build()
                        .setOnSelectedItemCallback { item ->
                            keyLevel = item.id.toInt()
                            if (item.id.toInt() == AppConstants.INTEGER_DEFAULT) keyLevel = null
                            searchingPractice()
                        }
                }
            })
            addDispose(rxAllSubject.subscribe { subjects ->
                binding.spSectFilter.clickWithDebounce {
                    CustomSpinner(it, requireContext())
                        .setBackGroundSpinner(Color.parseColor("#0F1838"))
                        .setDataSource(subjects)
                        .build()
                        .setOnSelectedItemCallback { item ->
                            keySubject = item.id.toInt()
                            if (item.id.toInt() == AppConstants.INTEGER_DEFAULT) keySubject = null
                            searchingPractice()
                        }
                }
            })
        }
    }

    fun clearFocus() {
        if (binding.edtSearchPracticeFilter.isFocused) {
            binding.edtSearchPracticeFilter.clearFocus()
        }
    }

}