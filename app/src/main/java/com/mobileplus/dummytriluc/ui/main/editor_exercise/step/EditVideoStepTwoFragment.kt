package com.mobileplus.dummytriluc.ui.main.editor_exercise.step

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.MotionEvent
import com.core.BaseFragmentZ
import com.google.gson.Gson
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.request.SaveExerciseRequest
import com.mobileplus.dummytriluc.databinding.FragmentEditVideoSecondBinding
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.setValue
import com.utils.KeyboardUtils
import com.utils.ext.argument
import com.utils.ext.clickWithDebounce
import com.utils.ext.postNormal
import org.koin.android.ext.android.inject

class EditVideoStepTwoFragment : BaseFragmentZ<FragmentEditVideoSecondBinding>() {
    override fun getLayoutBinding(): FragmentEditVideoSecondBinding =
        FragmentEditVideoSecondBinding.inflate(layoutInflater)

    private val gson by inject<Gson>()
    private val request: SaveExerciseRequest by lazy {
        val json = argument(ARG_DATA_STEP_2, "").value
        if (json.isBlank()) SaveExerciseRequest() else gson.fromJson(
            json,
            SaveExerciseRequest::class.java
        )
    }

    private val isUpdateExercise by argument(ARG_UPDATE_EXERCISE, false)

    companion object {
        private const val ARG_DATA_STEP_2 = "ARG_DATA_STEP_2"
        private const val ARG_UPDATE_EXERCISE = "ARG_UPDATE_EXERCISE"
        fun openFragment(
            request: SaveExerciseRequest,
            gson: Gson,
            isEditExercise: Boolean = false
        ) {
            val bundle = Bundle()
            bundle.putString(ARG_DATA_STEP_2, gson.toJson(request))
            bundle.putBoolean(ARG_UPDATE_EXERCISE, isEditExercise)
            postNormal(
                EventNextFragmentMain(
                    EditVideoStepTwoFragment::class.java,
                    bundle,
                    true
                )
            )
        }
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        handleHideSoftKeyboard()
        handleClick()
        setupView()
    }

    private fun setupView() {
        if (isUpdateExercise) {
            binding.edtVideoTitleStep2.setValue(request.title ?: "")
            binding.txtVideoContentStep2.setValue(request.content ?: "")
            binding.edtVideoSummaryStep2.setValue(request.note ?: "")
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun handleHideSoftKeyboard() {
        var mOldX = 0
        var mOldY = 0
        view?.setOnTouchListener { v, event ->
            hideKeyboard()
            false
        }
        binding.nestedScrollEditVideoStep2?.setOnTouchListener { v, event ->
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

    private fun handleClick() {
        binding.btnBackEditVideoSecond.clickWithDebounce {
            hideKeyboard()
            onBackPressed()
        }

        binding.btnBackToBottomStep2.clickWithDebounce {
            hideKeyboard()
            onBackPressed()
        }

        binding.btnNextToStepThree.clickWithDebounce {
            if (validInput()) {
                hideKeyboard()
                EditVideoStepThreeFragment.openFragment(request, gson, isUpdateExercise)
            }
        }
    }

    private fun validInput(): Boolean {
        val title = binding.edtVideoTitleStep2.text.toString()
        val content = binding.txtVideoContentStep2.text.toString()
        val note = binding.edtVideoSummaryStep2.text.toString()

        val errorMsg = mutableListOf<String>()
        if (title.isBlank()) {
            errorMsg.add(getString(R.string.require_title))
        }
        if (content.isBlank()) {
            errorMsg.add(getString(R.string.require_content))
        }
        if (note.isBlank()) {
            errorMsg.add(getString(R.string.require_note))
        }
        if (errorMsg.isNotEmpty()) {
            toast(errorMsg.joinToString("\n"))
        } else {
            request.title = title
            request.content = content
            request.note = note
        }
        return errorMsg.isEmpty()
    }

}