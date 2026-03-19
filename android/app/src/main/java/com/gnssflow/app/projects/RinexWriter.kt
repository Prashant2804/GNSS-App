package com.gnssflow.app.projects

import com.gnssflow.app.db.ObservationEpochEntity
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

private data class SatObs(
    val gnss_id: String,
    val sv_id: Int,
    val signal: String,
    val pseudorange_m: Double?,
    val carrier_phase_cycles: Double?,
    val doppler_hz: Double?,
    val cno_dbhz: Double,
)

object RinexWriter {
    private const val VERSION = "3.04"

    private val SIGNAL_TO_RINEX = mapOf(
        ("GPS" to "L1C") to mapOf("C" to "C1C", "L" to "L1C", "D" to "D1C", "S" to "S1C"),
        ("GPS" to "L2W") to mapOf("C" to "C2W", "L" to "L2W", "D" to "D2W", "S" to "S2W"),
        ("GAL" to "E1C") to mapOf("C" to "C1C", "L" to "L1C", "D" to "D1C", "S" to "S1C"),
    )

    private val GNSS_LETTER = mapOf("GPS" to "G", "GLO" to "R", "GAL" to "E", "BDS" to "C")

    private val moshi = Moshi.Builder().addLast(KotlinJsonAdapterFactory()).build()
    private val satListType = Types.newParameterizedType(List::class.java, SatObs::class.java)
    private val satAdapter = moshi.adapter<List<SatObs>>(satListType)

    fun generate(
        epochs: List<ObservationEpochEntity>,
        markerName: String = "GNSSFLOW",
        approxLat: Double = 0.0,
        approxLon: Double = 0.0,
        approxAlt: Double = 0.0,
    ): String {
        if (epochs.isEmpty()) return ""

        val allSats = epochs.flatMap { parseSats(it.satellitesJson) }
        val sysObs = collectObsTypes(allSats)

        val sb = StringBuilder()
        sb.append(buildHeader(epochs, sysObs, markerName, approxLat, approxLon, approxAlt))

        for (ep in epochs) {
            sb.append(buildEpoch(ep, sysObs))
        }

        return sb.toString()
    }

    private fun parseSats(json: String): List<SatObs> {
        return try {
            satAdapter.fromJson(json) ?: emptyList()
        } catch (_: Exception) {
            emptyList()
        }
    }

    private fun collectObsTypes(sats: List<SatObs>): Map<String, List<String>> {
        val result = mutableMapOf<String, MutableSet<String>>()
        for (s in sats) {
            val letter = GNSS_LETTER[s.gnss_id] ?: continue
            val codes = SIGNAL_TO_RINEX[s.gnss_id to s.signal] ?: continue
            val set = result.getOrPut(letter) { mutableSetOf() }
            set.addAll(codes.values)
        }
        return result.mapValues { it.value.sorted() }.toSortedMap()
    }

    private fun buildHeader(
        epochs: List<ObservationEpochEntity>,
        sysObs: Map<String, List<String>>,
        markerName: String,
        lat: Double,
        lon: Double,
        alt: Double,
    ): String {
        val sb = StringBuilder()
        sb.appendLine("     $VERSION           OBSERVATION DATA    M                   RINEX VERSION / TYPE")
        sb.appendLine("GNSS-Flow           gnss-flow                                   PGM / RUN BY / DATE")
        sb.appendLine("%-60sMARKER NAME".format(markerName))
        sb.appendLine("%-20s%-40sOBSERVER / AGENCY".format("operator", "GNSS-Flow"))
        sb.appendLine("%-20s%-20s%-20sREC # / TYPE / VERS".format("001", "ZED-X20P", "1.0"))
        sb.appendLine("%-20s%-20s%-20sANT # / TYPE".format("001", "INTERNAL", ""))

        val (x, y, z) = llaToEcef(lat, lon, alt)
        sb.appendLine("%14.4f%14.4f%14.4f                  APPROX POSITION XYZ".format(x, y, z))
        sb.appendLine("%14.4f%14.4f%14.4f                  ANTENNA: DELTA H/E/N".format(0.0, 0.0, 0.0))

        for ((sysLetter, obsList) in sysObs) {
            val obsStr = obsList.joinToString("") { " %3s".format(it) }
            sb.appendLine("$sysLetter  %3d%-52sSYS / # / OBS TYPES".format(obsList.size, obsStr))
        }

        val firstTs = epochs.first().timestampUtc
        val parts = parseTimestamp(firstTs)
        if (parts != null) {
            sb.appendLine("  %4d    %2d    %2d    %2d    %2d   %2d.0000000     GPS         TIME OF FIRST OBS".format(
                parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]
            ))
        }

        sb.appendLine("%-60sEND OF HEADER".format(""))
        return sb.toString()
    }

    private fun buildEpoch(epoch: ObservationEpochEntity, sysObs: Map<String, List<String>>): String {
        val sats = parseSats(epoch.satellitesJson)
        val parts = parseTimestamp(epoch.timestampUtc)
        if (parts == null || sats.isEmpty()) return ""

        val satMap = mutableMapOf<String, MutableMap<String, Double>>()
        for (s in sats) {
            val letter = GNSS_LETTER[s.gnss_id] ?: continue
            val satKey = "%s%02d".format(letter, s.sv_id)
            val codes = SIGNAL_TO_RINEX[s.gnss_id to s.signal] ?: continue
            val vals = satMap.getOrPut(satKey) { mutableMapOf() }
            if (s.pseudorange_m != null) vals[codes["C"]!!] = s.pseudorange_m
            if (s.carrier_phase_cycles != null) vals[codes["L"]!!] = s.carrier_phase_cycles
            if (s.doppler_hz != null) vals[codes["D"]!!] = s.doppler_hz
            if (s.cno_dbhz > 0) vals[codes["S"]!!] = s.cno_dbhz
        }

        val sb = StringBuilder()
        sb.appendLine("> %4d %02d %02d %02d %02d%11.7f  0%3d      %15.12f".format(
            parts[0], parts[1], parts[2], parts[3], parts[4], parts[5].toDouble(),
            satMap.size, epoch.receiverClockBiasS
        ))

        for (satKey in satMap.keys.sorted()) {
            val sysLetter = satKey.substring(0, 1)
            val obsTypes = sysObs[sysLetter] ?: continue
            val vals = satMap[satKey]!!
            val row = StringBuilder(satKey)
            for (obs in obsTypes) {
                val v = vals[obs]
                if (v != null) {
                    row.append("%14.3f ".format(v))
                } else {
                    row.append("               ")
                }
            }
            sb.appendLine(row.toString())
        }

        return sb.toString()
    }

    private fun parseTimestamp(ts: String): IntArray? {
        return try {
            // "2024-01-15T12:30:45.123Z"
            val clean = ts.replace("Z", "").replace("T", "-").replace(":", "-").replace(".", "-")
            val p = clean.split("-")
            intArrayOf(p[0].toInt(), p[1].toInt(), p[2].toInt(), p[3].toInt(), p[4].toInt(), p[5].toInt())
        } catch (_: Exception) {
            null
        }
    }

    private fun llaToEcef(latDeg: Double, lonDeg: Double, alt: Double): Triple<Double, Double, Double> {
        val latR = Math.toRadians(latDeg)
        val lonR = Math.toRadians(lonDeg)
        val a = 6378137.0
        val f = 1.0 / 298.257223563
        val e2 = 2 * f - f * f
        val n = a / sqrt(1 - e2 * sin(latR) * sin(latR))
        val x = (n + alt) * cos(latR) * cos(lonR)
        val y = (n + alt) * cos(latR) * sin(lonR)
        val z = (n * (1 - e2) + alt) * sin(latR)
        return Triple(x, y, z)
    }
}
