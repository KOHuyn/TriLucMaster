package com.mobileplus.dummytriluc.ui.main.editor_exercise.add_more_video

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemEditorExercise
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentAddMoreVideosBinding
import com.mobileplus.dummytriluc.ui.main.coach.draft.adapter.CoachDraftFolderAdapter
import com.mobileplus.dummytriluc.ui.main.editor_exercise.adapter.AddMoreVideoAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPushArrVideoPreview
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.ext.*
import com.widget.NestScrollListener
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by ThaiNV on 1/25/2021.
 * Copied from KO Huyn
 * edit by KOHuyn on 4/2/2021
 */
class AddMoreVideoFragment : BaseFragmentZ<FragmentAddMoreVideosBinding>() {
    override fun getLayoutBinding(): FragmentAddMoreVideosBinding =
        FragmentAddMoreVideosBinding.inflate(layoutInflater)

    private val addMoreVideoViewModel by viewModel<AddMoreVideoViewModel>()
    private val adapterVideo by lazy { AddMoreVideoAdapter() }
    private val page by lazy { Page() }
    private var isReload = true
    private val adapterDraftFolder by lazy { CoachDraftFolderAdapter() }
    private val rcvDraft by lazy { binding.rcvVideoDraft }
    private val rcvFolder by lazy { binding.rcvVideoDraftFolder }
    private val refreshDraft by lazy { binding.refreshVideoDraft }
    private val gson by inject<Gson>()
    private val selectedItems: MutableList<ItemEditorExercise> by lazy {
        val json = argument(ARG_SELECTED_DRAFT_LIST, "").value
        if (json.isBlank()) mutableListOf()
        else gson.toList<ItemEditorExercise>(json).toMutableList()
    }
    private val folderId: Int by lazy {
        val isFolder = arguments?.containsKey(ARG_FOLDER_ID_DRAFT) == true
        if (isFolder) arguments?.getInt(ARG_FOLDER_ID_DRAFT) ?: 0 else 0
    }

    companion object {
        private const val ARG_SELECTED_DRAFT_LIST = "ARG_SELECTED_DRAFT_LIST"
        private const val ARG_FOLDER_ID_DRAFT = "ARG_FOLDER_ID_DRAFT"
        fun openFragment(
            arrSelected: MutableList<ItemEditorExercise>,
            folderId: Int = 0,
            gson: Gson
        ) {
            val bundle = Bundle().apply {
                putString(
                    ARG_SELECTED_DRAFT_LIST,
                    gson.toJson(arrSelected)
                )
                putInt(ARG_FOLDER_ID_DRAFT, folderId)
            }

            postNormal(EventNextFragmentMain(AddMoreVideoFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        addMoreVideoViewModel.getTrainerDraft(folderId = folderId)
        addMoreVideoViewModel.getTrainerDraftFolder(folderId)
        handleClick()
        configView()
    }

    private fun handleClick() {
        binding.btnBackVideoDraft.clickWithDebounce { onBackPressed() }
        binding.imageDone.clickWithDebounce {
            if (selectedItems.isNotEmpty()) {
                postNormal(EventPushArrVideoPreview(selectedItems))
                removeAllFragDraft()
            } else {
                toast(loadStringRes(R.string.mesage_non_selected_draft))
            }
        }

        adapterVideo.onAllVideoSelected =
            AddMoreVideoAdapter.OnAllVideoSelected { item, isChecked ->
                if (item != null) {
                    if (isChecked) {
                        selectedItems.add(item)
                    } else {
                        val indexRemove =
                            selectedItems.indexOf(selectedItems.first { it.id == item.id })
                        selectedItems.removeAt(indexRemove)
                    }
                }
                adapterVideo.countItemSelected = selectedItems.size
                binding.txtSelectedCountVideoPreview.text =
                    String.format(getString(R.string.value_of_exercise_selected), selectedItems.size, 5)
            }

        adapterVideo.onMessageCallback = AddMoreVideoAdapter.OnMessageCallback { toast(it) }

        adapterDraftFolder.onItemClick = OnClickItemAdapter { _, position ->
            val item = adapterDraftFolder.items[position]
            item.id?.let {
                openFragment(selectedItems, it, gson)
            }
        }

        binding.nestedScrollDraftVideo.setOnScrollChangeListener(object : NestScrollListener() {
            override fun onLoadMore() {
                logErr("load more")
                isReload = false
                page.run {
                    if (currPage < totalPage && !isLoading) {
                        isLoading = true
                        currPage++
                        addMoreVideoViewModel.getTrainerDraft(currPage, folderId)
                    }
                }
            }
        })
    }

    private fun configView() {
        binding.txtSelectedCountVideoPreview.text =
            String.format(getString(R.string.value_of_exercise_selected), selectedItems.size, 5)
        adapterVideo.countItemSelected = selectedItems.size

        refreshDraft.applyColorRefresh()
        refreshDraft.setOnRefreshListener {
            isReload = true
            addMoreVideoViewModel.getTrainerDraft(folderId = folderId)
            addMoreVideoViewModel.getTrainerDraftFolder(folderId)
        }
        rcvDraft.run {
            adapter = adapterVideo
            layoutManager = GridLayoutManager(requireContext(), 3)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_4).toInt(),
                    isGridLayout = true,
                    spanCount = 3
                )
            )
        }

        rcvFolder.run {
            adapter = adapterDraftFolder
            layoutManager =
                GridLayoutManager(requireContext(), 3)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt(),
                    isGridLayout = true,
                    spanCount = 3
                )
            )
        }
    }

    private fun callbackViewModel() {
        addMoreVideoViewModel.run {
            addDispose(rxListDraftFolder.subscribe { items ->
                adapterDraftFolder.items = items.toMutableList()
                setVisibleViewWhen(
                    binding.txtTitleVideoDraftFolder,
                    rcvFolder
                ) { items.isNotEmpty() }
            })
            addDispose(rxListCoachMore.subscribe {
                val items = it.first.toMutableList()
                val pageResponse = it.second
                pageResponse?.run {
                    page.currPage = currPage
                    page.totalPage = totalPage
                    page.isLoading = false
                }
                binding.noDataVideoDraft.root.setVisibility(items.isEmpty())
                if (adapterVideo.items.isEmpty() || isReload)
                    adapterVideo.items = items
                else
                    adapterVideo.items.addAll(items)
                val arrIdsSelected = selectedItems.map { s -> s.id }
                adapterVideo.items.mapIndexed { index, item ->
                    if (arrIdsSelected.contains(item.id)) item.isSelected = true
                    adapterVideo.notifyItemChanged(index)
                }
                rcvDraft.requestLayout()
            })
            addDispose(isLoading.subscribe {
                if (page.isLoading) {
                    binding.loadMoreVideoDraft.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
                rcvDraft.requestLayout()
            })
            addDispose(rxMessage.subscribe { toast(it) })
        }
    }

    private fun removeAllFragDraft() {
        parentFragmentManager.fragments.map {
            if (it.tag == AddMoreVideoFragment::class.java.simpleName) {
                if (it is AddMoreVideoFragment) {
                    parentFragmentManager.beginTransaction().remove(it).commit()
                    parentFragmentManager.popBackStack()
                }
            }
        }
    }

    private fun enableRefresh() {
        refreshDraft.isRefreshing = true
    }

    private fun cancelRefresh() {
        refreshDraft.isRefreshing = false
    }
}