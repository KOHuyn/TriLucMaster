package com.mobileplus.dummytriluc.ui.main.coach.session.folder

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemCoachPractice
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentCoachSessionChooseFolderBinding
import com.mobileplus.dummytriluc.ui.main.coach.adapter.CoachAssignExerciseAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPostExerciseSession
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import com.utils.ext.setVisibility
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 4/21/2021
 */
class CoachSessionChooseFolderFragment : BaseFragmentZ<FragmentCoachSessionChooseFolderBinding>() {
    override fun getLayoutBinding(): FragmentCoachSessionChooseFolderBinding =
        FragmentCoachSessionChooseFolderBinding.inflate(layoutInflater)

    private val vm by viewModel<CoachSessionChooseFolderViewModel>()
    private val adapterAssign by lazy { CoachAssignExerciseAdapter() }
    private val gson by inject<Gson>()

    private val rcv by lazy { binding.rcvCoachSessionChooseFolder }
    private val page by lazy { Page() }
    private var isReload = true

    private val argFolderInside: DataFolderArg by lazy {
        gson.fromJson(
            argument(
                FOLDER_ID_ARG,
                ""
            ).value, DataFolderArg::class.java
        )
    }

    companion object {
        private const val FOLDER_ID_ARG = "FOLDER_ID_ARG"
        fun openFragmentFolder(arg: DataFolderArg, gson: Gson) {
            val bundle = Bundle().apply {
                putString(FOLDER_ID_ARG, gson.toJson(arg))
            }
            postNormal(
                EventNextFragmentMain(
                    CoachSessionChooseFolderFragment::class.java,
                    bundle,
                    true
                )
            )
        }
    }

    data class DataFolderArg(
        @Expose
        var idFolder: Int?,
        @Expose
        var itemsSelected: MutableList<ItemCoachPractice> = mutableListOf(),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSideText(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        vm.getTrainerList(idFolder = argFolderInside.idFolder)
        configRcv()
        handleClick()
    }

    private fun handleClick() {
        binding.btnBackCoachSessionChooseFolder.clickWithDebounce { onBackPressed() }

        adapterAssign.onSelectedFolder = CoachAssignExerciseAdapter.OnSelectedFolder { item ->
            openFragmentFolder(
                DataFolderArg(
                    item.id,
                    argFolderInside.itemsSelected
                ), gson
            )
        }

        adapterAssign.onClickItem = CoachAssignExerciseAdapter.OnClickItem { item, isSelected ->
            val itemFind = argFolderInside.itemsSelected.find { it.id == item.id }
            if (isSelected) {
                argFolderInside.itemsSelected.add(item)
            } else {
                argFolderInside.itemsSelected.remove(itemFind)
            }
        }
        binding.btnSendCoachSessionChooseFolder.clickWithDebounce {
            if (argFolderInside.itemsSelected.isEmpty()) {
                toast(getString(R.string.require_choose_exercise_assign))
            } else {
                postNormal(EventPostExerciseSession(argFolderInside.itemsSelected))
                removeAllFragAssign()
            }
        }
    }

    private fun configRcv() {
        rcv.getRcv().run {
            adapter = adapterAssign
            layoutManager = GridLayoutManager(requireContext(), 3)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_4).toInt(),
                    isGridLayout = true,
                    spanCount = 3
                )
            )
        }
        rcv.onCustomSwipeListener = CustomSwipeRcv.OnCustomSwipeListener {
            isReload = true
            vm.getTrainerList(idFolder = argFolderInside.idFolder)
        }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getTrainerList(currPage, idFolder = argFolderInside.idFolder)
                }
            }
        }
    }


    private fun callbackViewModel() {
        vm.apply {
            addDispose(rxItems.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.currPage = currPage
                    page.totalPage = totalPage
                    page.isLoading = false
                }
                binding.noDataCoachSessionChooseFolder.root.setVisibility(items.isEmpty())

                val ids = argFolderInside.itemsSelected.mapNotNull { item -> item.id }
                items.map { item -> item.isSelected = ids.contains(item.id) == true }

                if (adapterAssign.items.isEmpty() || isReload)
                    adapterAssign.items = items
                else
                    adapterAssign.items.addAll(items)

                rcv.getRcv().requestLayout()
            })
            addDispose(isLoading.subscribe {
                if (page.isLoading) {
                    binding.loadMoreCoachSessionChooseFolder.root.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
            })
            addDispose(rxMessage.subscribe { toast(it) })
        }
    }

    private fun removeAllFragAssign() {
        parentFragmentManager.fragments.map {
            if (it.tag == CoachSessionChooseFolderFragment::class.java.simpleName) {
                parentFragmentManager.beginTransaction().remove(it).commit()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()
}