package com.mobileplus.dummytriluc.ui.main.coach.draft

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.databinding.FragmentCoachDraftBinding
import com.mobileplus.dummytriluc.ui.dialog.MenuOptionsDialog
import com.mobileplus.dummytriluc.ui.dialog.RenameDialog
import com.mobileplus.dummytriluc.ui.dialog.YesNoButtonDialog
import com.mobileplus.dummytriluc.ui.main.coach.draft.adapter.CoachDraftAdapter
import com.mobileplus.dummytriluc.ui.main.coach.draft.adapter.CoachDraftFolderAdapter
import com.mobileplus.dummytriluc.ui.main.editor_exercise.editor.EditVideoFragment
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventSendFolderId
import com.mobileplus.dummytriluc.ui.utils.extensions.*
import com.utils.ext.*
import com.widget.NestScrollListener
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 3/12/2021
 */
class CoachDraftFragment : BaseFragmentZ<FragmentCoachDraftBinding>() {
    override fun getLayoutBinding(): FragmentCoachDraftBinding =
        FragmentCoachDraftBinding.inflate(layoutInflater)

    private val coachDraftViewModel by viewModel<CoachDraftViewModel>()
    private val adapterDraft by lazy { CoachDraftAdapter() }
    private val adapterDraftFolder by lazy { CoachDraftFolderAdapter() }
    private val rcvDraft by lazy { binding.rcvCoachDraft }
    private val rcvFolder by lazy { binding.rcvCoachDraftFolder }
    private val refreshDraft by lazy { binding.refreshCoachDraft }
    private val gson by inject<Gson>()
    private val page by lazy { Page() }
    private var isReload = true
    private var deleteDraftAt: Int = AppConstants.INTEGER_DEFAULT
    private val folderId: Int by lazy {
        val isFolder = arguments?.containsKey(ARG_ID_FOLDER) == true
        if (isFolder) arguments?.getInt(ARG_ID_FOLDER) ?: 0 else 0
    }
    private var lastPositionDraft: Int = AppConstants.INTEGER_DEFAULT
    private val isChooseFolder by argument(ARG_CHOOSE_FOLDER, false)
    private val nameFolder by argument(ARG_NAME_FOLDER, loadStringRes(R.string.draft_exercise))

    companion object {
        private const val ARG_ID_FOLDER = "ID_FOLDER"
        private const val ARG_NAME_FOLDER = "ARG_NAME_FOLDER"
        private const val ARG_CHOOSE_FOLDER = "ARG_CHOOSE_FOLDER"
        fun openFragment(isChooseFolder: Boolean = false) {
            val bundle = Bundle().apply {
                putBoolean(ARG_CHOOSE_FOLDER, isChooseFolder)
            }
            postNormal(EventNextFragmentMain(CoachDraftFragment::class.java, bundle, true))
        }

        fun openFragmentInFolder(parentId: Int, name: String, isChooseFolder: Boolean = false) {
            val bundle = Bundle().apply {
                putInt(ARG_ID_FOLDER, parentId)
                putString(ARG_NAME_FOLDER, name)
                putBoolean(ARG_CHOOSE_FOLDER, isChooseFolder)
            }
            postNormal(EventNextFragmentMain(CoachDraftFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        coachDraftViewModel.getAllDraft(folderId)
        configView()
        handleClick()
    }

    private fun configView() {
        binding.txtHeaderCoachDraft.text =
            if (isChooseFolder) loadStringRes(R.string.select_folder) else nameFolder
        binding.btnSaveHereFolder.setVisibility(isChooseFolder)
        if (isChooseFolder) {
            adapterDraft.isDeleteItem = false
            adapterDraft.isClickableItem = false
        } else {
            adapterDraft.isDeleteItem = true
            adapterDraft.isClickableItem = true
        }
        refreshDraft.applyColorRefresh()
        refreshDraft.setOnRefreshListener {
            isReload = true
            coachDraftViewModel.getAllDraft(folderId)
        }
        rcvDraft.run {
            adapter = adapterDraft
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
        coachDraftViewModel.apply {
            addDispose(rxListDraftFolder.subscribe { items ->
                adapterDraftFolder.items = items.toMutableList()
                setVisibleViewWhen(
                    binding.txtTitleCoachDraftFolder,
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
                if (adapterDraft.items.isEmpty() || isReload)
                    adapterDraft.items = items
                else
                    adapterDraft.items.addAll(items)
                rcvDraft.requestLayout()
            })
            addDispose(isLoading.subscribe {
                if (page.isLoading) {
                    binding.loadMoreDraft.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
                rcvDraft.requestLayout()
            })
            addDispose(rxMessage.subscribe { toast(it) })
            addDispose(rxDeleteDraft.subscribe { isSuccess ->
                if (isSuccess && deleteDraftAt != AppConstants.INTEGER_DEFAULT) {
                    adapterDraft.delete(deleteDraftAt)
                    deleteDraftAt = AppConstants.INTEGER_DEFAULT
                    toast(getString(R.string.delete_successfully))
                }
            })
            addDispose(rxCreateFolderSuccess.subscribe { if (it) getTrainerDraftFolder(folderId) })
            addDispose(rxRenameFolderSuccess.subscribe { if (it) getTrainerDraftFolder(folderId) })
            addDispose(rxEmptyData.subscribe {
                binding.noDataCoachDraft.root.isVisible = it
                binding.layoutContentCoachDraft.isGone = it
            })
        }
    }

    private fun removeAllFragDraft() {
        parentFragmentManager.fragments.map {
            if (it.tag == CoachDraftFragment::class.java.simpleName) {
                if (it is CoachDraftFragment) {
                    if (it.isChooseFolder) {
                        parentFragmentManager.beginTransaction().remove(it).commit()
                        parentFragmentManager.popBackStack()
                    }
                }
            }
        }
    }

    private fun handleClick() {
        binding.btnBackCoachDraft.clickWithDebounce { onBackPressed() }
        binding.btnSaveHereFolder.clickWithDebounce {
            if (isChooseFolder) {
                postNormal(EventSendFolderId(folderId, nameFolder))
                removeAllFragDraft()
            }
        }
        binding.btnCreateDraftCoach.clickWithDebounce {
            RenameDialog.builder()
                .setTitle(getString(R.string.create_new_folder))
                .setDescription(getString(R.string.folder_name))
                .build(parentFragmentManager)
                .setListenerCallback { newName ->
                    coachDraftViewModel.createFolderDraft(newName, folderId)
                }
        }
        binding.nestedScrollDraftCoach.setOnScrollChangeListener(object : NestScrollListener() {
            override fun onLoadMore() {
                isReload = false
                page.run {
                    if (currPage < totalPage && !isLoading) {
                        isLoading = true
                        currPage++
                        coachDraftViewModel.getTrainerDraft(currPage, folderId)
                    }
                }
            }
        })

        adapterDraft.onDeleteItem = CoachDraftAdapter.OnDeleteCoach { _, position ->
            YesNoButtonDialog()
                .setTitle(getString(R.string.delete_draft_exercise))
                .setMessage(getString(R.string.do_you_want_delete_draft_exercise))
                .setTextAccept(getString(R.string.yes))
                .setTextCancel(getString(R.string.no))
                .showDialog(parentFragmentManager, YesNoButtonDialog.TAG)
                .setOnCallbackAcceptButtonListener {
                    deleteDraftAt = position
                    adapterDraft.items[position].id?.let { id -> coachDraftViewModel.deleteDraft(id) }
                }
        }

        adapterDraft.clickItem = CoachDraftAdapter.OnClickCoachDraft { item ->
            if (item.id != null) {
                EditVideoFragment.openFragment(item.id)
            } else {
                toast(loadStringRes(R.string.error_unknown_error))
            }
        }

        adapterDraft.longClickItem = CoachDraftAdapter.OnLongClickCoachDraft { item, pos ->
            lastPositionDraft = pos
            MenuOptionsDialog.builder()
                .addMenuOption(
                    R.drawable.ic_pen_edit,
                    getString(R.string.rename),
                    MenuOptionsDialog.MenuOption.RENAME_DRAFT
                )
                .addMenuOption(
                    R.drawable.ic_up,
                    getString(R.string.move_to_folder),
                    MenuOptionsDialog.MenuOption.MOVE_FOLDER
                )
                .build(parentFragmentManager)
                .setOnCallbackListener { type ->
                    when (type) {
                        MenuOptionsDialog.MenuOption.RENAME_DRAFT -> {
                            Handler(Looper.getMainLooper()).post {
                                RenameDialog.builder()
                                    .setTitle(getString(R.string.rename_draft_exercise))
                                    .setDescription(getString(R.string.name_draft_exercise))
                                    .setRenameOld(item.title ?: "")
                                    .build(parentFragmentManager)
                                    .setListenerCallback { newName ->
                                        item.id?.let {
                                            coachDraftViewModel.updateDraft(
                                                it,
                                                newName,
                                                item.folderId ?: 0
                                            )
                                        }
                                        adapterDraft.updateName(pos, newName)
                                    }
                            }
                        }
                        MenuOptionsDialog.MenuOption.MOVE_FOLDER -> {
                            openFragment(isChooseFolder = true)
                        }
                        else -> Unit
                    }
                }
        }

        adapterDraftFolder.onItemClick = OnClickItemAdapter { _, position ->
            val item = adapterDraftFolder.items[position]
            item.id?.let {
                openFragmentInFolder(
                    it,
                    item.name ?: getString(R.string.draft_exercise),
                    isChooseFolder
                )
            }
        }

        adapterDraftFolder.onLongClick = OnClickItemAdapter { _, position ->
            MenuOptionsDialog.builder()
                .addMenuOption(
                    R.drawable.ic_pen_edit,
                    getString(R.string.rename),
                    MenuOptionsDialog.MenuOption.RENAME_FOLDER
                )
                .build(parentFragmentManager)
                .setOnCallbackListener { type ->
                    Handler(Looper.getMainLooper()).post {
                        val itemFolder = adapterDraftFolder.items[position]
                        when (type) {
                            MenuOptionsDialog.MenuOption.RENAME_FOLDER -> {
                                RenameDialog.builder()
                                    .setTitle(getString(R.string.rename_folder))
                                    .setDescription(getString(R.string.name_folder))
                                    .setRenameOld(itemFolder.name ?: "")
                                    .build(parentFragmentManager)
                                    .setListenerCallback { newName ->
                                        adapterDraftFolder.items[position].id?.let {
                                            coachDraftViewModel.renameFolderDraft(newName, it)
                                        }
                                    }
                            }
                        }
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

    override fun onStart() {
        super.onStart()
        register(this)
    }

    override fun onStop() {
        super.onStop()
        unregister(this)
    }

    @Subscribe
    fun chooseFolderCallback(ev: EventSendFolderId) {
        if (lastPositionDraft != AppConstants.INTEGER_DEFAULT) {
            val item = adapterDraft.items[lastPositionDraft]
            item.id?.let {
                coachDraftViewModel.updateDraft(
                    it,
                    item.title ?: "unknown",
                    ev.idFolder
                )
            }
            adapterDraft.removeFolder(lastPositionDraft, ev.idFolder)
        }
    }
}