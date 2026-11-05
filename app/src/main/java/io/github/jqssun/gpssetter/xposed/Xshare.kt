package io.github.jqssun.gpssetter.xposed
import de.robv.android.xposed.XSharedPreferences
import io.github.jqssun.gpssetter.BuildConfig
import io.github.jqssun.gpssetter.utils.AppLocationProfile
import io.github.jqssun.gpssetter.utils.AppLocationProfileCodec
import io.github.jqssun.gpssetter.utils.GpsPrecision

class Xshare {

    private val xPref: XSharedPreferences by lazy {
        XSharedPreferences(BuildConfig.APPLICATION_ID, "${BuildConfig.APPLICATION_ID}_prefs")
    }

    private fun pref(): XSharedPreferences = xPref

    fun reload() {
        pref().reload()
    }

    val isStarted : Boolean
    get() = pref().getBoolean(
        "start",
        false
    )

    val isSeparateControlEnabled: Boolean
        get() = pref().getBoolean("separate_control", false)

    fun appLocationProfile(packageName: String): AppLocationProfile? =
        if (!isSeparateControlEnabled) {
            null
        } else {
            AppLocationProfileCodec.decode(pref().getString("app_location_profiles", null))
                .firstOrNull { it.packageName == packageName && it.enabled && it.started }
        }

    val getLat: Double
    get() = GpsPrecision.read(pref(), GpsPrecision.KEY_LAT, GpsPrecision.LEGACY_KEY_LAT, 45.0)


    val getLng : Double
    get() = GpsPrecision.read(pref(), GpsPrecision.KEY_LNG, GpsPrecision.LEGACY_KEY_LNG, 0.0)

    val isHookedSystem : Boolean
    get() = pref().getBoolean(
        "system_hooked",
        true
    )

    val isRandomPosition :Boolean
    get() = pref().getBoolean(
        "random_position",
        false
    )

    val accuracy : String?
    get() = pref().getString("accuracy_level","10")

}