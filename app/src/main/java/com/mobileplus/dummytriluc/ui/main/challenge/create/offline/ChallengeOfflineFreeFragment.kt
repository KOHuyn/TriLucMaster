package com.mobileplus.dummytriluc.ui.main.challenge.create.offline

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.core.BaseFragment
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.model.ItemInvitePeople
import com.mobileplus.dummytriluc.ui.dialog.InvitePeopleDialog
import com.mobileplus.dummytriluc.ui.main.challenge.adapter.InvitePeopleAdapter
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.utils.ext.clickWithDebounce
import com.utils.ext.hide
import com.utils.ext.setVisibility
import com.utils.ext.show
import io.reactivex.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_challenge_offline_free.*

class ChallengeOfflineFreeFragment : BaseFragment() {
    override fun getLayoutId(): Int = R.layout.fragment_challenge_offline_free

    private val adapterInviteChallenge by lazy { InvitePeopleAdapter(isDelete = true) }

    private val adapterInviteViewer by lazy { InvitePeopleAdapter(isDelete = true) }
    private val dialogInviteChallenge by lazy { InvitePeopleDialog(isInviteChallenge = true) }
    private val dialogInviteViewer by lazy { InvitePeopleDialog() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        hideKeyboardOutSide(view)
    }

    override fun updateUI(savedInstanceState: Bundle?) {
        fakeData()
        setupView()
        controlButton()
        setupRcvChallenge()
    }

    private fun controlButton() {
        btnBackChooseModeChallenge.clickWithDebounce { onBackPressed() }
        btnInviteChallengeOfflineFree.clickWithDebounce {
            dialogInviteChallenge.show(parentFragmentManager, dialogInviteChallenge.tag)
        }
        btnInviteViewerOfflineFree.clickWithDebounce {
            dialogInviteViewer.show(parentFragmentManager, dialogInviteViewer.tag)
        }
        dialogInviteChallenge.onCallbackPeopleInvite =
            InvitePeopleDialog.OnCallbackPeopleInvite { data ->
                adapterInviteChallenge.items = data
                noDataInviteChallengeOfflineFree.setVisibility(data.isEmpty())
            }
        dialogInviteViewer.onCallbackPeopleInvite =
            InvitePeopleDialog.OnCallbackPeopleInvite { data ->
                noDataInviteViewerOfflineFree.setVisibility(data.isEmpty())
                if (adapterInviteViewer.items.isEmpty())
                    adapterInviteViewer.items = data
                else
                    adapterInviteViewer.items.addAll(data)
                rcvInviteViewerOfflineFree.invalidate()
                rcvInviteViewerOfflineFree.requestLayout()
            }
    }

    private fun setupRcvChallenge() {
        rcvInviteViewerOfflineFree.run {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterInviteViewer
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
            itemAnimator = null
            swapAdapter(adapterInviteViewer, true)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(
                        R.dimen.space_8
                    ).toInt()
                )
            )
        }

        rcvInviteChallengeOfflineFree.run {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = adapterInviteChallenge
            isNestedScrollingEnabled = false
            setHasFixedSize(false)
            itemAnimator = null
            swapAdapter(adapterInviteChallenge, true)
            addItemDecoration(
                MarginItemDecoration(
                    resources.getDimension(
                        R.dimen.space_8
                    ).toInt()
                )
            )
        }

        adapterInviteChallenge.run {
            onDeleteChangeCallback = InvitePeopleAdapter.OnDeleteChangeCallback {
                noDataInviteChallengeOfflineFree.setVisibility(items.size == 0)
            }
        }
        adapterInviteViewer.run {
            onDeleteChangeCallback = InvitePeopleAdapter.OnDeleteChangeCallback {
                noDataInviteViewerOfflineFree.setVisibility(items.size == 0)
            }
        }
    }

    private fun setupView() {
        setupCountNumber(
            txtMinusScoreOfflineFree,
            txtPlusScoreOfflineFree,
            txtNumberScoreOfflineFree
        )
        setupCountNumber(
            txtMinusPunchOfflineFree,
            txtPlusPunchOfflineFree,
            txtNumberPunchOfflineFree
        )
        rgPositionFight.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbFreePositionFight -> {
                    layoutLimitPosition.hide()
                }
                R.id.rbLimitPositionFight -> {
                    layoutLimitPosition.show()
                }
            }
        }
    }

    private fun setupCountNumber(minus: View, plus: View, number: TextView, step: Int = 10) {
        val rxNumber: PublishSubject<Int> = PublishSubject.create()
        try {
            if (number.text.toString().isNotBlank())
                if (number.text.toString().toInt() == 0) {
                    minus.isEnabled = false
                    minus.alpha = 0.5f
                }
        } catch (e: Exception) {
            number.text = 0.toString()
        }
        addDispose(rxNumber.subscribe {
            minus.isEnabled = it > 0
            minus.alpha = if (it > 0) 1f else 0.5f
            number.text = it.toString()
        })
        minus.setOnClickListener {
            rxNumber.onNext(number.text.toString().toInt() - step)
        }
        plus.setOnClickListener { rxNumber.onNext(number.text.toString().toInt() + step) }
        minus.setOnLongClickListener {
            rxNumber.onNext(number.text.toString().toInt() - (step * 10))
            if (number.text.toString().toInt() < (step * 10)) {
                number.text = 0.toString()
            }
            true
        }
        plus.setOnLongClickListener {
            rxNumber.onNext(number.text.toString().toInt() + (step * 10))
            true
        }
    }

    private fun fakeData() {
        val itemsChallenge = mutableListOf<ItemInvitePeople>()
        itemsChallenge.add(
            ItemInvitePeople(
                "https://file.hstatic.net/1000045032/file/fm-e1433941678273.jpg",
                "BÁCH LÊ",
                300895
            )
        )
        adapterInviteChallenge.items = itemsChallenge
        val itemsViewer = mutableListOf<ItemInvitePeople>()
        itemsViewer.add(
            ItemInvitePeople(
                "https://top.chon.vn/wp-content/uploads/2020/03/vo-si-quyen-anh-noi-tieng-nhat-1.jpg",
                "Huy Lee",
                300898
            )
        )
        itemsViewer.add(
            ItemInvitePeople(
                "https://vtv1.mediacdn.vn/zoom/237_339/2020/4/4/skynews-anthony-yarde-boxer4962046-1585990405331625085085.jpg",
                "Duong Lee",
                300899
            )
        )
        adapterInviteViewer.items = itemsViewer
    }
}