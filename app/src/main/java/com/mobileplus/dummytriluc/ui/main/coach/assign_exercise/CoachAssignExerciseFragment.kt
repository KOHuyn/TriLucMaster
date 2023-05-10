package com.mobileplus.dummytriluc.ui.main.coach.assign_exercise

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.recyclerview.widget.GridLayoutManager
import com.core.BaseFragment
import com.google.gson.Gson
import com.google.gson.annotations.Expose
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.Page
import com.mobileplus.dummytriluc.data.request.CoachAssignExerciseRequest
import com.mobileplus.dummytriluc.ui.main.coach.adapter.CoachAssignExerciseAdapter
import com.mobileplus.dummytriluc.ui.utils.AppConstants
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.eventbus.EventNextFragmentMain
import com.mobileplus.dummytriluc.ui.utils.extensions.logErr
import com.mobileplus.dummytriluc.ui.widget.CustomSwipeRcv
import com.utils.ext.*
import kotlinx.android.synthetic.main.fragment_coach_assign_exercise.*
import org.koin.android.ext.android.inject
import org.koin.android.viewmodel.ext.android.viewModel

/**
 * Created by KOHuyn on 3/15/2021
 */
class CoachAssignExerciseFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_coach_assign_exercise
    private val vm by viewModel<CoachAssignExerciseViewModel>()
    private val adapterAssign by lazy { CoachAssignExerciseAdapter() }
    private val gson by inject<Gson>()

    private val rcv by lazy { rcvCoachAssignExercise }
    private val page by lazy { Page() }
    private var isReload = true

    private val classId: Int by argument(CLASS_ID_ARG, AppConstants.INTEGER_DEFAULT)
    private val studentId: Int by argument(STUDENT_ID_ARG, AppConstants.INTEGER_DEFAULT)
    private val argFolderInside: DataFolderArg? by lazy {
        if (arguments?.containsKey(FOLDER_ID_ARG) == true)
            gson.fromJson(
                argument(
                    FOLDER_ID_ARG,
                    ""
                ).value, DataFolderArg::class.java
            ) else null
    }
    private var query: String? = null
        set(value) {
            field = if (value?.isBlank() == true)
                null
            else value
        }

    companion object {
        private const val CLASS_ID_ARG = "CLASS_ID_ARG"
        private const val STUDENT_ID_ARG = "STUDENT_ID_ARG"
        private const val FOLDER_ID_ARG = "FOLDER_ID_ARG"
        fun openFragmentClass(idClass: Int) {
            val bundle = Bundle().apply {
                putInt(CLASS_ID_ARG, idClass)
            }
            postNormal(EventNextFragmentMain(CoachAssignExerciseFragment::class.java, bundle, true))
        }

        fun openFragmentStudent(idStudent: Int) {
            val bundle = Bundle().apply {
                putInt(STUDENT_ID_ARG, idStudent)
            }
            postNormal(EventNextFragmentMain(CoachAssignExerciseFragment::class.java, bundle, true))
        }

        fun openFragmentFolder(arg: DataFolderArg, gson: Gson) {
            val bundle = Bundle().apply {
                putString(FOLDER_ID_ARG, gson.toJson(arg))
                logErr(gson.toJson(arg))
                if (arg.isClass) {
                    putInt(CLASS_ID_ARG, arg.idAssign)
                } else {
                    putInt(STUDENT_ID_ARG, arg.idAssign)
                }
            }
            postNormal(EventNextFragmentMain(CoachAssignExerciseFragment::class.java, bundle, true))
        }

        data class DataFolderArg(
            @Expose
            var idFolder: Int?,
            @Expose
            var isClass: Boolean,
            @Expose
            var idAssign: Int,
            @Expose
            var idsSelected: List<Int> = arrayListOf(),
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSideText(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        callbackViewModel()
        vm.getTrainerList(idFolder = argFolderInside?.idFolder)
        configRcv()
        handleClick()
    }

    private fun handleClick() {
        btnBackCoachAssignExercise.clickWithDebounce { onBackPressed() }
        edtSearchCoachAssign.onTextChanged { query = it }
        edtSearchCoachAssign.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                vm.getTrainerList(idFolder = argFolderInside?.idFolder)
                hideKeyboard()
            }
            false
        }

        adapterAssign.onSelectedFolder = CoachAssignExerciseAdapter.OnSelectedFolder { item ->
            val isClass = getTypeAssignment() == CoachAssignExerciseRequest.CLASS
            val arrIds: List<Int> =
                if (argFolderInside != null) argFolderInside!!.idsSelected.plus(adapterAssign.getIdsSelected())
                else adapterAssign.getIdsSelected()
            logErr("Bundle", "size:${arrIds.size}")
            openFragmentFolder(
                DataFolderArg(
                    item.id,
                    isClass,
                    if (isClass) classId else studentId,
                    arrIds
                ), gson
            )
        }
        btnSendAssignExercise.clickWithDebounce {
            val isEmptyIds = adapterAssign.getIdsSelected()
                .isEmpty() && argFolderInside?.idsSelected == null
            if (isEmptyIds) {
                toast("Bạn chưa chọn bài nào để giao")
            } else {
                val ids = "[${
                    adapterAssign.getIdsSelected().plus(argFolderInside?.idsSelected ?: emptyList())
                        .joinToString(",")
                }]"
                logErr(ids)
                when (getTypeAssignment()) {
                    CoachAssignExerciseRequest.CLASS -> {
                        vm.sendAssignExercise(
                            CoachAssignExerciseRequest(
                                practiceId = ids,
                                assignObjectId = classId,
                                assignType = CoachAssignExerciseRequest.CLASS
                            )
                        )
                    }

                    CoachAssignExerciseRequest.STUDENT -> {
                        vm.sendAssignExercise(
                            CoachAssignExerciseRequest(
                                practiceId = ids,
                                assignObjectId = studentId,
                                assignType = CoachAssignExerciseRequest.STUDENT
                            )
                        )
                    }
                }
            }
        }
    }

    private fun getTypeAssignment(): String {
        return when {
            arguments?.containsKey(CLASS_ID_ARG) == true -> CoachAssignExerciseRequest.CLASS
            arguments?.containsKey(STUDENT_ID_ARG) == true -> CoachAssignExerciseRequest.STUDENT
            else -> CoachAssignExerciseRequest.FOLDER
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
            vm.getTrainerList(idFolder = argFolderInside?.idFolder)
        }

        rcv.onLoadMoreListener = CustomSwipeRcv.OnLoadMoreListener {
            isReload = false
            page.run {
                if (currPage < totalPage && !isLoading) {
                    isLoading = true
                    currPage++
                    vm.getTrainerList(currPage, idFolder = argFolderInside?.idFolder)
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
                noDataCoachAssignExercise.setVisibility(items.isEmpty())
                if (adapterAssign.items.isEmpty() || isReload)
                    adapterAssign.items = items
                else
                    adapterAssign.items.addAll(items)
                rcv.getRcv().requestLayout()
            })
            addDispose(isLoading.subscribe {
                if (page.isLoading) {
                    loadMoreCoachAssignExercise.setVisibility(it)
                } else {
                    if (it) enableRefresh() else cancelRefresh()
                }
            })
            addDispose(rxMessage.subscribe { toast(it) })
            addDispose(rxSendLoading.subscribe { if (it) showDialog() else hideDialog() })
            addDispose(rxSendSuccess.subscribe { if (it) removeAllFragAssign() })
        }
    }

    private fun removeAllFragAssign() {
        parentFragmentManager.fragments.map {
            if (it.tag == CoachAssignExerciseFragment::class.java.simpleName) {
                parentFragmentManager.beginTransaction().remove(it).commit()
                parentFragmentManager.popBackStack()
            }
        }
    }

    private fun enableRefresh() = rcv.enableRefresh()
    private fun cancelRefresh() = rcv.cancelRefresh()
}