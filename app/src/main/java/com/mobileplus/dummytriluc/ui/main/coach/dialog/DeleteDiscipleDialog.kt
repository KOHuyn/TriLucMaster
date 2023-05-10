package com.mobileplus.dummytriluc.ui.main.coach.dialog

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import androidx.core.content.res.ResourcesCompat
import com.core.BaseDialog
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.CustomTypefaceSpan
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.utils.ext.clickWithDebounce
import kotlinx.android.synthetic.main.dialog_delete_disciple.*

class DeleteDiscipleDialog : BaseDialog() {
    var listener: OnDeleteListener? = null
    private var content: Spannable? = null
    var title: String? = null

    override fun getLayoutId(): Int = R.layout.dialog_delete_disciple

    override fun updateUI(savedInstanceState: Bundle?) {
        txtContentDeleteDialog?.text =
            if (content != null) content else loadStringRes(R.string.label_delete)

        txtTitleGroupDialog?.setTextNotNull(title)

        btnDeleteGroupDialog?.clickWithDebounce {
            listener?.setOnDeleteListener()
            dismiss()
        }
        btnSkipGroupDialog?.clickWithDebounce {
            dismiss()
        }
    }

    fun setUp(name: String?, format: String, context: Context, title: String?): DeleteDiscipleDialog {
        this.title = title
        val str =
            String.format(format, name)
        if (!name.isNullOrBlank()) {
            val index = str.indexOf(name, 0, false)
            if (index > 0) {
                val wordToSpan: Spannable = SpannableString(str)
                wordToSpan.setSpan(
                    ForegroundColorSpan(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.clr_tab,
                            null
                        )
                    ), 0, index - 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                wordToSpan.setSpan(
                    CustomTypefaceSpan(
                        Typeface.createFromAsset(
                            context.assets,
                            "fonts/anton_regular.ttf"
                        )
                    ),
                    index,
                    index + name.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                wordToSpan.setSpan(
                    ForegroundColorSpan(
                        ResourcesCompat.getColor(
                            context.resources,
                            R.color.clr_tab,
                            null
                        )
                    ), index, index + name.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                content = wordToSpan
            }
        }
        return this
    }

    fun interface OnDeleteListener {
        fun setOnDeleteListener()
    }
}