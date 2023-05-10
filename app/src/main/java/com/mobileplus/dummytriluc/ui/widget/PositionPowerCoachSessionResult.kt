package com.mobileplus.dummytriluc.ui.widget

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.widget.TextView
import com.core.BaseCustomLayout
import com.mobileplus.dummytriluc.R

/**
 * Created by KOHuyn on 4/23/2021
 */
class PositionPowerCoachSessionResult :
    BaseCustomLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attr: AttributeSet) : super(context, attr)

    private var position: String? = null
    private var score: Int? = null

    override fun getLayoutId(): Int = R.layout.item_coach_session_result_position_power

    override fun getStyleableId(): IntArray? = R.styleable.PositionPowerCoachSessionResult

    override fun initDataFromStyleable(a: TypedArray) {
        position = a.getString(R.styleable.PositionPowerCoachSessionResult_title_position)
        score = a.getInt(R.styleable.PositionPowerCoachSessionResult_score, 0)
        super.initDataFromStyleable(a)
    }

    override fun updateUI() {
        findViewById<TextView>(R.id.txtTitlePosition).text = String.format("%s:", position ?: "---")
        findViewById<TextView>(R.id.txtScorePower).text = "${score ?: 0}"
    }

    fun setTitlePosition(title: String) {
        findViewById<TextView>(R.id.txtTitlePosition).text = String.format("%s:", title ?: "---")
    }

    fun setScore(score: Int) {
        findViewById<TextView>(R.id.txtScorePower).text = "$score"
    }

}