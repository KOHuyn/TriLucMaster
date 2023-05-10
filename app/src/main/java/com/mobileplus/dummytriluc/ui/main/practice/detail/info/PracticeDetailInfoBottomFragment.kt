package com.mobileplus.dummytriluc.ui.main.practice.detail.info

import android.os.Bundle
import com.core.BaseFragmentZ
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.data.response.DetailPracticeResponse
import com.mobileplus.dummytriluc.databinding.LayoutPracticeDetailInfoBottomBinding
import com.mobileplus.dummytriluc.ui.utils.extensions.setHtmlWithImage
import com.utils.ext.setVisibility
import kotlinx.android.synthetic.main.layout_practice_detail_info_bottom.*

class PracticeDetailInfoBottomFragment : BaseFragmentZ<LayoutPracticeDetailInfoBottomBinding>() {
    override fun getLayoutBinding(): LayoutPracticeDetailInfoBottomBinding =
        LayoutPracticeDetailInfoBottomBinding.inflate(layoutInflater)

    fun setDataInfoBottom(data: DetailPracticeResponse) {
        txtTitleInfoBottom.text = String.format(getString(R.string.value_of_exercise), data.title)
        if (data.userCreate != null) {
            txtNameCoachInfoBottom.text =
                String.format(getString(R.string.value_of_coach), data.userCreate.fullName)
        }
        txtNameLevelInfoBottom.text = String.format(getString(R.string.value_of_level), data.level?.title ?: "----")
        txtNoteInfoBottom.text = String.format(getString(R.string.value_of_note), data.note)
        txtTutorialInfoBottom.setHtmlWithImage(data.content ?: "", requireContext())
        txtNoteInfoBottom.setVisibility(data.note != null)
        txtTutorialInfoBottom.setVisibility(data.content != null)
        txtNameCoachInfoBottom.setVisibility(data.userCreate != null)
    }

    override fun updateUI(savedInstanceState: Bundle?) {}
}