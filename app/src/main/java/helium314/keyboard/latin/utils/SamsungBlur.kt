// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin.utils

import android.util.Log
import android.view.View

/**
 * Samsung's own background blur. Samsung switches off the standard `Window.setBackgroundBlurRadius`,
 * but One UI still ships `android.view.SemBlurInfo` and `View.semSetBlurInfo()`.
 * They are hidden APIs, so every call goes through reflection and a failure means "no blur".
 */
object SamsungBlur {
    private const val TAG = "SamsungBlur"

    /**
     * A colour curve for the blurred background, before the tint goes on top. Samsung's ranges: [saturation] -1 to 1,
     * [bias] -100 to 100, the levels 0 to 255. Input brightness from [minX] to [maxX] is mapped to output brightness
     * from [minY] to [maxY]. A narrow, dark output range keeps a bright app behind the keys from making the panel loud,
     * while its colours still show through.
     */
    class Curve(val saturation: Float, val bias: Float, val minX: Float, val maxX: Float, val minY: Float, val maxY: Float)

    /** Returns true if the blur was applied to [view]. [topCornerPx] rounds the two top corners of the blurred area. */
    fun apply(view: View, radiusPx: Int, tintArgb: Int, topCornerPx: Float = 0f, curve: Curve? = null): Boolean = try {
        val info = Class.forName("android.view.SemBlurInfo")
        val builder = Class.forName("android.view.SemBlurInfo\$Builder")
        val windowMode = info.getField("BLUR_MODE_WINDOW").getInt(null)
        val b = builder.getConstructor(Int::class.javaPrimitiveType).newInstance(windowMode)
        builder.getMethod("setRadius", Int::class.javaPrimitiveType).invoke(b, radiusPx)
        builder.getMethod("setBackgroundColor", Int::class.javaPrimitiveType).invoke(b, tintArgb)
        // corners and curve are optional: without them the blur still works, just with square corners or a plain tint
        val f = Float::class.javaPrimitiveType
        if (topCornerPx > 0f) {
            runCatching { builder.getMethod("setBackgroundCornerRadius", f, f, f, f).invoke(b, topCornerPx, topCornerPx, 0f, 0f) }
                .onFailure { Log.d(TAG, "no blur corner radius: $it") }
        }
        if (curve != null) {
            runCatching {
                builder.getMethod("setColorCurve", f, f, f, f, f, f)
                    .invoke(b, curve.saturation, curve.bias, curve.minX, curve.maxX, curve.minY, curve.maxY)
            }.onFailure { Log.d(TAG, "no blur colour curve: $it") }
        }
        val built = builder.getMethod("build").invoke(b)
        View::class.java.getMethod("semSetBlurInfo", info).invoke(view, built)
        true
    } catch (t: Throwable) {
        Log.d(TAG, "SemBlurInfo not available: $t")
        false
    }
}
