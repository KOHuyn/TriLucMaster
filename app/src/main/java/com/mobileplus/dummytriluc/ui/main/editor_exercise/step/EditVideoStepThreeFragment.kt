package com.mobileplus.dummytriluc.ui.main.editor_exercise.step

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import com.core.BaseFragment
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.SaveExerciseRequest
import com.mobileplus.dummytriluc.databinding.FragmentEditVideoThreeBinding
import com.mobileplus.dummytriluc.ui.main.editor_exercise.editor.EditVideoViewModel
import com.mobileplus.dummytriluc.ui.utils.PickerImageUtils
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

class EditVideoStepThreeFragment : BaseFragmentZ<FragmentEditVideoThreeBinding>() {
    override fun getLayoutBinding(): FragmentEditVideoThreeBinding =
        FragmentEditVideoThreeBinding.inflate(layoutInflater)
    private val viewModel: EditVideoViewModel by viewModel()

    private var subjectArr = mutableListOf<CustomSpinner.SpinnerItem>()
    private var levelArr = mutableListOf<CustomSpinner.SpinnerItem>()

    private val gson by inject<Gson>()
    private val request: SaveExerciseRequest by lazy {
        val json = argument(ARG_DATA_STEP_3, "").value
        if (json.isBlank()) SaveExerciseRequest() else gson.fromJson(
            json,
            SaveExerciseRequest::class.java
        )
    }
    private val isUpdateExercise by argument(ARG_UPDATE_EXERCISE, false)

    companion object {
        private const val ARG_DATA_STEP_3 = "ARG_DATA_STEP_3"
        private const val ARG_UPDATE_EXERCISE = "ARG_UPDATE_EXERCISE1"
        fun openFragment(request: SaveExerciseRequest, gson: Gson, isEditExercise: Boolean) {
            val bundle = Bundle()
            bundle.putString(ARG_DATA_STEP_3, gson.toJson(request))
            bundle.putBoolean(ARG_UPDATE_EXERCISE, isEditExercise)
            postNormal(
                EventNextFragmentMain(
                    EditVideoStepThreeFragment::class.java,
                    bundle,
                    true
                )
            )
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        disposeViewModel()
        handleClick()
        setupView()
    }

    private fun setupView() {
        if (isUpdateExercise) {
            dataSourceStatus().find { it.id == SaveExerciseRequest.STATUS_ACTIVE.toString() }
                ?.let { status ->
                    binding.spStatusEditVideoThree.text = status.name
                    request.status = status.id.toIntOrNull()
                }
            binding.spSubjectEditVideoThree.text = request.moreInformation.subject?.title ?: ""
            binding.spLevelEditVideoThree.text = request.moreInformation.level?.title ?: ""
            binding.btnUploadPhotoEditVideoThree.show(request.imagePath)
        }
    }

    private fun disposeViewModel() {
        viewModel.run {
            addDispose(
                isLoading.subscribe { if (it) showDialog() else hideDialog() },
                rxMessage.subscribe { toast(it) },
            )
            addDispose(rxLevelArr.subscribe {
                levelArr = it.toMutableList()
                openSpinnerLevel()
            })
            addDispose(rxSubjectArr.subscribe {
                subjectArr = it.toMutableList()
                openSpinnerSubject()
            })
        }
    }

    private fun handleClick() {
        binding.  btnBackToHomeStep3.clickWithDebounce {
            onBackPressed()
        }

        binding. btnBackToStep2.clickWithDebounce {
            onBackPressed()
        }

        binding.btnUploadPhotoEditVideoThree.setOnClickListener {
            PickerImageUtils.createSingleImageGallery(this)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }
        binding.btnNextToStepFour.clickWithDebounce {
            if (validData()) {
                EditVideoStepFourFragment.openFragment(request, gson, isUpdateExercise)
            }
        }
        binding.spStatusEditVideoThree.clickWithDebounce {
            CustomSpinner(it, requireContext())
                .setTextSize(resources.getDimension(R.dimen.text_14))
                .setTextColor(R.color.clr_tab)
                .setBackGroundSpinner(Color.WHITE)
                .setDataSource(dataSourceStatus())
                .build()
                .setOnSelectedItemCallback { item ->
                    request.status = item.id.toInt()
                }
        }
        binding.spSubjectEditVideoThree.clickWithDebounce {
            if (subjectArr.isEmpty()) {
                viewModel.getSubjectArr()
            } else {
                openSpinnerSubject()
            }
        }
        binding.spLevelEditVideoThree.clickWithDebounce {
            if (levelArr.isEmpty()) {
                viewModel.getLevelArr()
            } else {
                openSpinnerLevel()
            }
        }
    }

    private fun dataSourceStatus() = mutableListOf(
        CustomSpinner.SpinnerItem(
            loadStringRes(R.string.video_status_active),
            SaveExerciseRequest.STATUS_ACTIVE.toString()
        ),
        CustomSpinner.SpinnerItem(
            loadStringRes(R.string.video_status_unactive),
            SaveExerciseRequest.STATUS_INACTIVE.toString()
        )
    )

    private fun validData(): Boolean {
        val errorMsg = mutableListOf<String>()
        if (request.status == null) {
            errorMsg.add(getString(R.string.require_select_status))
        }
        if (request.subjectId == null) {
            errorMsg.add(getString(R.string.require_select_subject))
        }
        if (request.levelRequire == null) {
            errorMsg.add(getString(R.string.require_select_level))
        }
        if (isUpdateExercise) {
            if (request.imagePath == null) {
                errorMsg.add(getString(R.string.require_upload_image))
            }
        } else {
            if (request.moreInformation.imagePath == null) {
                errorMsg.add(getString(R.string.require_upload_image))
            }
        }
        if (errorMsg.isNotEmpty()) {
            toast(errorMsg.joinToString("\n"))
        }
        return errorMsg.isEmpty()
    }

    private fun openSpinnerLevel() {
        CustomSpinner(binding.spLevelEditVideoThree, requireContext())
            .setTextSize(resources.getDimension(R.dimen.text_14))
            .setTextColor(R.color.clr_tab)
            .setBackGroundSpinner(Color.WHITE)
            .setDataSource(levelArr)
            .build()
            .setOnSelectedItemCallback { item ->
                request.levelRequire = item.id.toInt()
            }
    }

    private fun openSpinnerSubject() {
        CustomSpinner(binding.spSubjectEditVideoThree, requireContext())
            .setTextSize(resources.getDimension(R.dimen.text_14))
            .setTextColor(R.color.clr_tab)
            .setBackGroundSpinner(Color.WHITE)
            .setDataSource(subjectArr)
            .build()
            .setOnSelectedItemCallback { item ->
                request.subjectId = item.id.toInt()
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList: MutableList<LocalMedia>? =
                        PictureSelector.obtainMultipleResult(data)
                    selectList?.let {
                        val media = selectList[0]
                        request.moreInformation.imagePath = PickerImageUtils.getPathImage(media)
                        binding.btnUploadPhotoEditVideoThree.show(request.moreInformation.imagePath)
                    }
                }
                else -> {
                }
            }
        }
    }

}