package com.mobileplus.dummytriluc.ui.dialog

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.core.BaseDialog
import com.core.BaseViewHolder
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.MarginItemDecoration
import com.mobileplus.dummytriluc.ui.utils.extensions.setTextNotNull
import com.mobileplus.dummytriluc.ui.utils.extensions.show
import com.mobileplus.dummytriluc.ui.utils.extensions.showFitXY
import com.utils.applyClickShrink
import com.utils.ext.clickWithDebounce
import com.utils.ext.inflateExt
import kotlinx.android.synthetic.main.dialog_choose_mode_practice.*
import kotlinx.android.synthetic.main.item_choose_mode_practice.view.*

/**
 * Created by KOHuyn on 3/29/2021
 */
class ChooseModePracticeDialog : BaseDialog() {
    override fun getLayoutId(): Int = R.layout.dialog_choose_mode_practice

    private val adapterChooseMode by lazy { ChooseModeAdapter() }

    override fun updateUI(savedInstanceState: Bundle?) {
        adapterChooseMode.items = getData().toMutableList()
        rcvChooseModePractice.run {
            adapter = adapterChooseMode
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
//            addItemDecoration(MarginItemDecoration(resources.getDimension(R.dimen.space_8).toInt()))
        }
    }

    fun show(fm: FragmentManager): ChooseModePracticeDialog {
        show(fm, this::class.java.simpleName)
        return this
    }

    fun setCallbackChooseMode(callback: (ChooseModePracticeDialog, PracticeType) -> Unit) {
        adapterChooseMode.chooseModeCallback = ChooseModeCallback { type ->
            if (type is CompositePracticeMode) {
                adapterChooseMode.items = type.getChild()
            } else {
                dismiss()
                callback.invoke(this, type.type)
            }
        }
    }

    class ChooseModeAdapter : RecyclerView.Adapter<BaseViewHolder>() {

        var items = mutableListOf<PracticeMode>()
            set(value) {
                field = value
                notifyDataSetChanged()
            }

        var chooseModeCallback: ChooseModeCallback? = null

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder =
            BaseViewHolder(parent.inflateExt(R.layout.item_choose_mode_practice))

        override fun onBindViewHolder(holder: BaseViewHolder, position: Int) {
            with(holder.itemView) {
                val item = items[position]
                imgThumbChooseModePractice.showFitXY(item.drawableRes)
                txtTitleChooseModePractice.setTextNotNull(context.getString(item.labelRes))
                txtDescriptionChooseModePractice.setTextNotNull(context.getString(item.labelRawRes))
                applyClickShrink()
                clickWithDebounce { chooseModeCallback?.setOnChooseModeCallback(item) }
            }
        }

        override fun getItemCount(): Int = items.size
    }

    fun interface ChooseModeCallback {
        fun setOnChooseModeCallback(type: PracticeMode)
    }

    abstract class PracticeMode(
        val drawableRes: Int,
        val labelRes: Int,
        val labelRawRes: Int,
        val type: PracticeType
    )

    interface IPracticeMode {
        fun add(mode: PracticeMode)
        fun remove(mode: PracticeMode)
    }

    class CompositePracticeMode(
        drawableRes: Int,
        labelRes: Int,
        labelRawRes: Int,
        type: PracticeType
    ) : PracticeMode(drawableRes, labelRes, labelRawRes, type), IPracticeMode {

        private val practiceModeList = mutableListOf<PracticeMode>()
        override fun add(mode: PracticeMode) {
            practiceModeList.add(mode)
        }

        override fun remove(mode: PracticeMode) {
            practiceModeList.remove(mode)
        }

        fun getChild() = practiceModeList
    }

    class LeafPracticeMode(
        drawableRes: Int,
        labelRes: Int,
        labelRawRes: Int,
        type: PracticeType
    ) : PracticeMode(drawableRes, labelRes, labelRawRes, type)

    private fun getData(): List<PracticeMode> {
        val course = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_according_to_course,
            labelRes = R.string.according_to_course,
            labelRawRes = R.string.according_to_course_raw,
            type = PracticeType.COURSE
        )

        val forFunExercise = CompositePracticeMode(
            drawableRes = R.drawable.img_thumb_according_to_course,
            labelRes = R.string.for_fun_exercise,
            labelRawRes = R.string.for_fun_exercise_raw,
            type = PracticeType.FOR_FUN
        )
        val freeFight = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_free_fight,
            labelRes = R.string.free_fight,
            labelRawRes = R.string.free_fight_raw,
            type = PracticeType.FREE_FIGHT
        )
        val accordingLed = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_according_to_led,
            labelRes = R.string.according_to_led,
            labelRawRes = R.string.according_to_led_raw,
            type = PracticeType.ACCORDING_LED
        )
        val accordingMusic = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_free_fight,
            labelRes = R.string.according_to_music,
            labelRawRes = R.string.according_to_music_raw,
            type = PracticeType.ACCORDING_MUSIC
        )
        val relaxExercise = CompositePracticeMode(
            drawableRes = R.drawable.img_thumb_free_fight,
            labelRes = R.string.relax_exercise,
            labelRawRes = R.string.relax_exercise_raw,
            type = PracticeType.RELAX
        )
        val beatHusband = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_free_fight,
            labelRes = R.string.beat_husband,
            labelRawRes = R.string.beat_husband_raw,
            type = PracticeType.BEAT_HUSBAND
        )
        val beatLoveEnemy = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_free_fight,
            labelRes = R.string.beat_love_enemy,
            labelRawRes = R.string.beat_love_enemy_raw,
            type = PracticeType.BEAT_LOVE_ENEMY
        )
        val beatEx = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_free_fight,
            labelRes = R.string.beat_ex,
            labelRawRes = R.string.beat_ex_raw,
            type = PracticeType.BEAT_EX
        )
        val beatBoss = LeafPracticeMode(
            drawableRes = R.drawable.img_thumb_free_fight,
            labelRes = R.string.beat_boss,
            labelRawRes = R.string.beat_boss_raw,
            type = PracticeType.BEAT_BOSS
        )

        forFunExercise.add(freeFight)
        forFunExercise.add(accordingLed)
        forFunExercise.add(accordingMusic)
        relaxExercise.add(beatHusband)
        relaxExercise.add(beatLoveEnemy)
        relaxExercise.add(beatEx)
        relaxExercise.add(beatBoss)
        forFunExercise.add(relaxExercise)
        return listOf(course, forFunExercise)
    }

    enum class PracticeType {
        COURSE, FOR_FUN, FREE_FIGHT, ACCORDING_LED, ACCORDING_MUSIC, RELAX, BEAT_HUSBAND, BEAT_LOVE_ENEMY, BEAT_EX, BEAT_BOSS
    }

}