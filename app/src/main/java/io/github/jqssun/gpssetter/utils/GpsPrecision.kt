package io.github.jqssun.gpssetter.utils

import android.content.SharedPreferences
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Single place that defines how many digits after the decimal point a spoofed
 * coordinate may have. 7 decimals is about 1.1 cm, which is already finer than
 * any real GPS receiver can deliver.
 *
 * This object is also used from the Xposed hook (system_server / target apps),
 * so it must stay free of Android UI / Context dependencies.
 */
object GpsPrecision {

    /** Number of digits after the decimal point. */
    const val DECIMALS = 7

    /**
     * SharedPreferences keys for coordinates stored as full-precision text.
     * The old keys ("latitude"/"longitude") held a Float, which only has ~7
     * significant digits and produced tails such as -6.917500019073486.
     */
    const val KEY_LAT = "latitude_d"
    const val KEY_LNG = "longitude_d"
    const val LEGACY_KEY_LAT = "latitude"
    const val LEGACY_KEY_LNG = "longitude"

    private fun scaled(value: Double): BigDecimal =
        // Going through toString() avoids binary artefacts (e.g. 2.675 -> 2.67499999...).
        BigDecimal(value.toString()).setScale(DECIMALS, RoundingMode.HALF_UP)

    /** Rounds [value] to [DECIMALS] decimals. NaN / Infinity are returned unchanged. */
    fun round(value: Double): Double =
        if (value.isNaN() || value.isInfinite()) value else scaled(value).toDouble()

    /**
     * Plain text with exactly [DECIMALS] decimals, e.g. "-6.9175234".
     * Locale independent (always uses '.'), never scientific notation.
     */
    fun toPlainString(value: Double): String =
        if (value.isNaN() || value.isInfinite()) value.toString() else scaled(value).toPlainString()

    /**
     * Reads a coordinate from [prefs]. Prefers the new text key; falls back to the
     * legacy Float key so values saved by older versions keep working.
     */
    fun read(prefs: SharedPreferences, key: String, legacyKey: String, default: Double): Double {
        try {
            prefs.getString(key, null)?.toDoubleOrNull()?.let { return round(it) }
        } catch (_: ClassCastException) {
            // key exists with another type; fall through to legacy handling
        }
        return try {
            if (prefs.contains(legacyKey)) round(prefs.getFloat(legacyKey, default.toFloat()).toDouble())
            else default
        } catch (_: ClassCastException) {
            default
        }
    }
}

/** Rounds this coordinate to 7 decimals. */
fun Double.toGpsPrecision(): Double = GpsPrecision.round(this)
