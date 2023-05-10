package com.mobileplus.dummytriluc.ui.main.coach.create_course

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MotionEvent
import androidx.recyclerview.widget.GridLayoutManager
import com.core.BaseFragmentZ
import com.luck.picture.lib.PictureSelector
import com.luck.picture.lib.config.PictureConfig
import com.luck.picture.lib.entity.LocalMedia
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.CreateCourseRequest
import com.mobileplus.dummytriluc.databinding.FragmentCoachCreateCourseBinding
import com.mobileplus.dummytriluc.ui.main.MainActivity
import com.mobileplus.dummytriluc.ui.main.coach.create_course.adapter.CoachCreateCourseAdapter
import com.mobileplus.dummytriluc.ui.main.coach.create_course.dialog.CoachCreateCourseDialog
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.PickerImageUtils
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventReloadExerciseCoach
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setValue
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.widget.CustomSpinner
import com.utils.KeyboardUtils
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import org.koin.android.viewmodel.ext.android.viewModel
import java.io.File

/**
 * Created by KOHuyn on 4/5/2021
 */
class CoachCreateCourseFragment : BaseFragmentZ<FragmentCoachCreateCourseBinding>() {

    override fun getLayoutBinding(): FragmentCoachCreateCourseBinding =
        FragmentCoachCreateCourseBinding.inflate(layoutInflater)

    private var subjectArr = mutableListOf<CustomSpinner.SpinnerItem>()
    private val vm by viewModel<CoachCreateCourseViewModel>()
    private var pathUrlImage: String = ""
    private val request by lazy { CreateCourseRequest() }
    private val adapterExercise by lazy { CoachCreateCourseAdapter() }

    private val idCourseEditor: Int? by lazy {
        val idTemp by argument(ARG_ID_COURSE_EDIT, -1)
        if (idTemp != -1) idTemp else null
    }

    companion object {

        private const val ARG_ID_COURSE_EDIT = "ARG_ID_COURSE_EDIT"

        fun openFragment() {
            postNormal(EventNextFragmentMain(CoachCreateCourseFragment::class.java, true))
        }

        fun openFragmentEditor(idCourse: Int) {
            val bundle = Bundle().apply { putInt(ARG_ID_COURSE_EDIT, idCourse) }
            postNormal(EventNextFragmentMain(CoachCreateCourseFragment::class.java, bundle, true))
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        setupView()
        viewModelCallback()
        if (idCourseEditor != null) {
            vm.getDetailFolder(idCourseEditor!!)
            vm.getListExerciseCourseNoPage(idCourseEditor!!)
        }
        handleClick()
        setupRcvExercise()
        handleHideSoftKeyboard()
    }

    private fun setupView() {
        binding.txtHeaderCreateCourse.text =
            if (idCourseEditor == null) loadStringRes(R.string.create_course) else loadStringRes(R.string.edit_course)
        binding.btnCreateCourseNewCoach.text =
            if (idCourseEditor == null) loadStringRes(R.string.create_course) else loadStringRes(R.string.edit_course)
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleHideSoftKeyboard() {
        var mOldX = 0
        var mOldY = 0
        view?.setOnTouchListener { v, event ->
            hideKeyboard()
            false
        }
        binding.scrollViewCreateCourse?.setOnTouchListener { v, event ->
            val x = event.x.toInt()
            val y = event.y.toInt()
            if (event.action == MotionEvent.ACTION_DOWN) {
                mOldX = x
                mOldY = y
            } else if (event.action == MotionEvent.ACTION_UP) {
                if (x == mOldX || y == mOldY) {
                    KeyboardUtils.hideKeyboard(requireActivity())
                }
            }
            false
        }
    }

    private fun setupRcvExercise() {
        binding.rcvExerciseAddToCourse.run {
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(R.dimen.space_8).toInt(),
                    false,
                    isGridLayout = true,
                    3
                )
            )
            adapter = adapterExercise
            isNestedScrollingEnabled = false
            setHasFixedSize(true)
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun handleClick() {
       binding. btnBackCoachCreateCourse.clickWithDebounce { onBackPressed() }
       binding. spSubjectCreateCourse.clickWithDebounce {
            if (subjectArr.isEmpty()) {
                vm.getSubjectArr()
            } else {
                openSpinnerSubject()
            }
        }

        binding. spStatusCreateCourse.clickWithDebounce {
            CustomSpinner(it, requireContext())
                .setTextSize(resources.getDimension(R.dimen.text_14))
                .setTextColor(R.color.clr_tab)
                .setBackGroundSpinner(Color.WHITE)
                .setDataSource(arrActiveStatus())
                .build()
                .setOnSelectedItemCallback { item ->
                    request.status = item.id.toInt()
                }
        }
        binding. btnUploadPhotoNewCourse.setOnClickListener {
            PickerImageUtils.createSingleImageGallery(this)
                .forResult(PictureConfig.CHOOSE_REQUEST)
        }

        binding. btnAddExerciseCourseNewCoach.clickWithDebounce {
            CoachCreateCourseDialog()
                .setIdsSelected(adapterExercise.items.mapNotNull { it.id })
                .apply {
                    if (idCourseEditor != null) {
                        setMissingId(idCourseEditor!!)
                    }
                }
                .build(parentFragmentManager)
                .setOnCallbackExerciseListener { items ->
                    adapterExercise.items = items.toMutableList()
                }
        }
        binding.btnCreateCourseNewCoach.clickWithDebounce {
            if (validData()) {
                hideKeyboard()
                if (idCourseEditor == null) {
                    vm.createCourse(request, File(pathUrlImage))
                } else {
                    vm.editorCourse(
                        idCourseEditor!!,
                        request,
                        if (pathUrlImage.isBlank()) null else File(pathUrlImage)
                    )
                }
            }
        }
    }

    private fun viewModelCallback() {
        vm.run {
            addDispose(rxDetailCourseResponse.subscribe { data ->
               binding.edtNameCourse.setValue(data.title ?: "")
               binding.spSubjectCreateCourse.text = data.subject?.title ?: ""
               binding.spStatusCreateCourse.text = arrActiveStatus().first().name
                binding.edtIntroduceCreateCourse.setValue(data.content ?: "")
                binding.btnUploadPhotoNewCourse.show(data.imagePath)
                request.subjectId = data.subject?.id
                request.status = arrActiveStatus().first().id.toIntOrNull() ?: 1
                request.imagePath = data.imagePath
            })
            addDispose(rxItemsExerciseEditor.subscribe { adapterExercise.items = it })
            addDispose(rxErrorData.subscribe { removeFragmentByTag(CoachCreateCourseFragment::class.java.simpleName) })
            addDispose(
                isLoading.subscribe { if (it) showDialog() else hideDialog() },
                rxMessage.subscribe { toast(it) },
            )
            addDispose(rxSubjectArr.subscribe {
                subjectArr = it.toMutableList()
                openSpinnerSubject()
            })
            addDispose(rxCreateCourseSuccess.subscribe { isSuccess ->
                if (isSuccess) {
                    postNormal(EventReloadExerciseCoach())
                    (activity as MainActivity).popBackToTrainerMain()
                }
            })
        }
    }

    private fun validData(): Boolean {
        val msgError = mutableListOf<String>()
        val nameCourse = binding.edtNameCourse.text.toString()
        val introduce = binding.edtIntroduceCreateCourse.text.toString()
        if (nameCourse.isBlank()) {
            msgError.add(getString(R.string.require_name_course))
        }
        if (request.subjectId == null) {
            msgError.add(getString(R.string.require_subject_course))
        }
        if (request.status == null) {
            msgError.add(getString(R.string.require_status_course))
        }
        if (introduce.isBlank()) {
            msgError.add(getString(R.string.require_introduce_course))
        }
        if (request.imagePath == null) {
            if (pathUrlImage.isBlank()) {
                msgError.add(getString(R.string.require_image_course))
            }
        }
        if (adapterExercise.items.isEmpty()) {
            msgError.add(getString(R.string.require_exercise_course))
        }
        if (msgError.isEmpty()) {
            request.title = nameCourse
            request.content = introduce
            request.practiceIds = adapterExercise.items.mapNotNull { it.id }
        } else {
            toast(msgError.joinToString("\n"))
        }
        return msgError.isEmpty()
    }

    private fun openSpinnerSubject() {
        CustomSpinner(binding.spSubjectCreateCourse, requireContext())
            .setTextSize(resources.getDimension(R.dimen.text_14))
            .setTextColor(R.color.clr_tab)
            .setBackGroundSpinner(Color.WHITE)
            .setDataSource(subjectArr)
            .build()
            .setOnSelectedItemCallback { item ->
                request.subjectId = item.id.toInt()
            }
    }

    private fun arrActiveStatus(): MutableList<CustomSpinner.SpinnerItem> =
        mutableListOf(
            CustomSpinner.SpinnerItem(
                loadStringRes(R.string.video_status_active),
                CreateCourseRequest.STATUS_ACTIVE.toString()
            ),
            CustomSpinner.SpinnerItem(
                loadStringRes(R.string.video_status_unactive),
                CreateCourseRequest.STATUS_INACTIVE.toString()
            )
        )

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                PictureConfig.CHOOSE_REQUEST -> {
                    val selectList: MutableList<LocalMedia>? =
                        PictureSelector.obtainMultipleResult(data)
                    selectList?.let {
                        val media = selectList[0]
                        pathUrlImage = PickerImageUtils.getPathImage(media)
                        binding.btnUploadPhotoNewCourse.show(pathUrlImage)
                    }
                }
                else -> {
                }
            }
        }
    }

}