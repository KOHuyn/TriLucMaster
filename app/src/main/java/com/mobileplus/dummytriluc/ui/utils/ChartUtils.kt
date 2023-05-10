package com.mobileplus.dummytriluc.ui.utils

import android.graphics.Color
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.mobileplus.dummytriluc.R
import com.mobileplus.dummytriluc.ui.utils.extensions.loadStringRes

object ChartUtils {
    fun createBarChart(chart: BarChart) {
        chart.apply {
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            setMaxVisibleValueCount(100)
            setScaleEnabled(true)
            isDoubleTapToZoomEnabled = false
            setDrawGridBackground(false)
            setNoDataTextColor(Color.parseColor("#FFFFFF"))
            setNoDataText(loadStringRes(R.string.nodata_chart))
            legend.isEnabled = false
            setExtraOffsets(10f, 10f, 10f, 10f)
            isScaleYEnabled = false
        }
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            granularity = 1f // only intervals of 1 day
            textColor = Color.WHITE
        }

        chart.axisLeft.apply {
            setPosition(YAxis.YAxisLabelPosition.OUTSIDE_CHART)
            spaceTop = 20f
            textColor = Color.WHITE
            axisMinimum = 0f // this replaces setStartAtZero(true)
        }
        chart.axisRight.isEnabled = false
    }

    fun createPieChart(chart: PieChart) {
        chart.apply {
            setUsePercentValues(true)
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.TRANSPARENT)
            setNoDataTextColor(Color.parseColor("#FFFFFF"))
            setNoDataText(loadStringRes(R.string.nodata_chart))
            holeRadius = 92f
            setDrawCenterText(false)
            isRotationEnabled = true
            isHighlightPerTapEnabled = true
            animateY(1000, Easing.Linear)
            legend.isEnabled = false
        }
    }

    fun createRadarChart(chart: RadarChart, titles: Array<String>) {
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            yAxis.setDrawLabels(false)
            yAxis.axisMinimum = 0F
            setNoDataTextColor(Color.parseColor("#FFFFFF"))
            setNoDataText(loadStringRes(R.string.nodata_chart))
        }
        chart.xAxis.apply {
            textSize = 8f
            yOffset = 2f
            xOffset = 2f
            valueFormatter = IndexAxisValueFormatter(titles)
            textColor = Color.WHITE
        }
    }
}