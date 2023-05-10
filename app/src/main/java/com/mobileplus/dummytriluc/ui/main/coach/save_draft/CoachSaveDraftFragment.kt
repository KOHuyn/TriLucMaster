package com.mobileplus.dummytriluc.ui.main.coach.save_draft

import android.os.Bundle
import android.view.View
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.DataSendDraftResponse
import com.mobileplus.dummytriluc.databinding.FragmentCoachSaveDraftBinding
import com.mobileplus.dummytriluc.ui.dialog.SaveDraftSuccessDialog
import com.mobileplus.dummytriluc.ui.main.coach.draft.CoachDraftFragment
import com.mobileplus.dummytriluc.ui.main.editor_exercise.editor.EditVideoFragment
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventPopVideo
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventSendFolderId
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.utils.ext.*
import org.greenrobot.eventbus.Subscribe
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 3/25/2021
 */
class CoachSaveDraftFragment : BaseFragmentZ<FragmentCoachSaveDraftBinding>() {
    override fun getLayoutBinding(): FragmentCoachSaveDraftBinding =
        FragmentCoachSaveDraftBinding.inflate(layoutInflater)

    private val saveDraftViewModel by viewModel<CoachSaveDraftViewModel>()
    private val gson by inject<Gson>()
    private var idFolder: Int? = null
    private val draftResponse: DataSendDraftResponse? by lazy {
        if (arguments?.getString(ARG_COACH_SAVE) == null) null else gson.fromJson(
            arguments?.getString(ARG_COACH_SAVE),
            DataSendDraftResponse::class.java
        )
    }

    companion object {
        private const val ARG_COACH_SAVE = "ARG_ID_COACH_SAVE"
        fun openFragment(dataDraft: DataSendDraftResponse, gson: Gson) {
            val bundle = Bundle().apply {
                putString(ARG_COACH_SAVE, gson.toJson(dataDraft))
            }
            postNormal(EventNextFragmentMain(CoachSaveDraftFragment::class.java, bundle, true))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposableViewModel()
        handleClick()
    }

    private fun disposableViewModel() {
        saveDraftViewModel.run {
            addDispose(rxUpdateSuccess.subscribe { isSuccess ->
                binding.btnCreateExerciseCoach.isEnabled = !isSuccess
                binding.btnCreateExerciseCoach.alpha = if (isSuccess) 0.5F else 1F
                if (isSuccess) {
                    SaveDraftSuccessDialog()
                        .setCancelableDialog(false)
                        .setTitleSaved(loadStringRes(R.string.draft_exercise))
                        .setCallbackConfirm {
                            onBackPressed()
                            postNormal(EventPopVideo())
                        }.show(parentFragmentManager)
                }
            })
            addDispose(isLoading.subscribe { if (it) showDialog() else hideDialog() })
            addDispose(rxMessage.subscribe { toast(it) })
        }
    }

    private fun handleClick() {
        binding.btnBackCoachSaveDraft.clickWithDebounce { onBackPressed() }
        binding.txtSelectFolderCoach.clickWithDebounce {
            CoachDraftFragment.openFragment(
                isChooseFolder = true
            )
        }
        binding.btnAddToExerciseDraftCoach.clickWithDebounce {
            val title = binding.txtHeaderDraftCoach.text.toString()
            if (validSave(title, idFolder).isEmpty()) {
                draftResponse?.id?.let { idDraft ->
                    saveDraftViewModel.updateDraft(
                        idDraft,
                        title,
                        idFolder ?: 0
                    )
                }
            } else {
                toast(validSave(title, idFolder).joinToString("\n"))
            }
        }

        binding.btnCreateExerciseCoach.clickWithDebounce {
            if (draftResponse != null) {
                if (draftResponse!!.id != null) {
                    EditVideoFragment.openFragment(draftResponse!!.id!!)
                } else {
                    toast(loadStringRes(R.string.error_unknown_error))
                }
            }
        }
    }

    private fun validSave(title: String, folderId: Int?): List<String> {
        val arrMsg = arrayListOf<String>()
        if (title.isBlank()) {
            arrMsg.add(getString(R.string.require_title))
        }
        if (folderId == null) {
            arrMsg.add(getString(R.string.require_folder))
        }
        return arrMsg
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
    fun sendFolderId(ev: EventSendFolderId) {
        logErr("${ev.idFolder}-${ev.nameFolder}")
        idFolder = ev.idFolder
        binding.txtSelectFolderCoach.text = ev.nameFolder
    }
}
