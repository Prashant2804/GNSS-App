package com.gnssflow.app.projects

import kotlin.math.abs

data class AreaPerimeter(
    val areaM2: Double,
    val perimeterM: Double,
)

object PolygonMath {
    fun areaPerimeter(latLon: List<Pair<Double, Double>>): AreaPerimeter {
        if (latLon.size < 3) return AreaPerimeter(0.0, 0.0)
        val origin = latLon.first()
        val local = latLon.map { (lat, lon) -> Geo.deltaNorthEastMeters(origin.first, origin.second, lat, lon) } // (N,E)

        var twiceArea = 0.0
        var perimeter = 0.0
        for (i in local.indices) {
            val j = (i + 1) % local.size
            val (n1, e1) = local[i]
            val (n2, e2) = local[j]
            twiceArea += e1 * n2 - e2 * n1
            perimeter += Geo.distanceMeters(latLon[i].first, latLon[i].second, latLon[j].first, latLon[j].second)
        }
        return AreaPerimeter(areaM2 = abs(twiceArea) / 2.0, perimeterM = perimeter)
    }
}

