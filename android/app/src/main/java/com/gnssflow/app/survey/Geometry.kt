package com.gnssflow.app.survey

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.hypot

object Geometry {
    fun distance(a: SurveyPoint, b: SurveyPoint): Double {
        return hypot(b.east - a.east, b.north - a.north)
    }

    fun azimuthRad(a: SurveyPoint, b: SurveyPoint): Double {
        return atan2(b.east - a.east, b.north - a.north)
    }

    fun polygonArea(vertices: List<SurveyPoint>): Double {
        if (vertices.size < 3) return 0.0
        var sum = 0.0
        for (i in vertices.indices) {
            val j = (i + 1) % vertices.size
            sum += vertices[i].east * vertices[j].north
            sum -= vertices[j].east * vertices[i].north
        }
        return abs(sum) / 2.0
    }
}
