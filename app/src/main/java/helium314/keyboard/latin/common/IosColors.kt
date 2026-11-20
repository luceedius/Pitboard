// SPDX-License-Identifier: GPL-3.0-only

package helium314.keyboard.latin.common

import android.content.Context
import android.graphics.Color
import java.util.EnumMap

/**
 * Colours for the iOS 26 look. Every key has the same fill, as on iOS 26, and there is no shadow under the keys.
 * The panel is passed to the window blur as its tint (see [helium314.keyboard.latin.utils.SamsungBlur]).
 * The key fills are translucent white, so a little of the blurred panel shows through each key.
 * The dark values come from an iOS 26 screenshot over a black app: panel #1F2126, keys #4B4E52 (white at about 20% over the panel).
 * The panel is glass: LatinIME first maps the blurred app into a dark band of brightness with a colour curve, and this
 * tint goes on top at a lower alpha. So the panel stays dark over a bright app but still shows its colours.
 */
object IosColors {
    private val accent = Color.rgb(10, 132, 255)

    @Suppress("UNUSED_PARAMETER")
    fun map(context: Context, isNight: Boolean): EnumMap<ColorType, Int> {
        val panel: Int
        val key: Int
        val text: Int
        val hint: Int
        val popup: Int
        if (isNight) {
            panel = Color.argb(150, 18, 19, 23)
            key = Color.argb(40, 255, 255, 255)
            text = Color.rgb(245, 245, 247)
            hint = Color.argb(150, 235, 235, 245)
            popup = Color.rgb(92, 94, 100)
        } else {
            panel = Color.argb(214, 210, 213, 220)
            key = Color.argb(250, 255, 255, 255)
            text = Color.BLACK
            hint = Color.argb(150, 60, 60, 67)
            popup = Color.WHITE
        }
        val clear = Color.TRANSPARENT
        val m = EnumMap<ColorType, Int>(ColorType::class.java)
        for (type in ColorType.entries) {
            m[type] = when (type) {
                ColorType.ACTION_KEY_ICON -> Color.WHITE
                ColorType.ACTION_KEY_BACKGROUND, ColorType.ACTION_KEY_POPUP_KEYS_BACKGROUND,
                    ColorType.CLIPBOARD_PIN, ColorType.EMOJI_CATEGORY_SELECTED,
                    ColorType.TOOL_BAR_KEY_ENABLED_BACKGROUND -> accent
                ColorType.GESTURE_TRAIL -> Color.argb(200, 10, 132, 255)
                ColorType.GESTURE_PREVIEW, ColorType.KEY_PREVIEW_BACKGROUND, ColorType.POPUP_KEYS_BACKGROUND,
                    ColorType.MORE_SUGGESTIONS_BACKGROUND, ColorType.MORE_SUGGESTIONS_WORD_BACKGROUND -> popup
                ColorType.AUTOFILL_BACKGROUND_CHIP, ColorType.CLIPBOARD_SUGGESTION_BACKGROUND,
                    ColorType.EMOJI_SEARCH_BACKGROUND, ColorType.KEY_BACKGROUND, ColorType.SPACE_BAR_BACKGROUND,
                    ColorType.FUNCTIONAL_KEY_BACKGROUND -> key
                ColorType.KEY_HINT_TEXT, ColorType.MORE_SUGGESTIONS_HINT -> hint
                ColorType.SPACE_BAR_TEXT -> hint
                ColorType.MAIN_BACKGROUND, ColorType.NAVIGATION_BAR -> panel
                ColorType.STRIP_BACKGROUND, ColorType.TOOL_BAR_EXPAND_KEY_BACKGROUND -> clear
                else -> text // all remaining types are text or icon colours
            }
        }
        return m
    }

    /**
     * Colours of a key while it is held down. The default (the key colour darkened a little) is invisible on a translucent key,
     * and without the key preview popup this is the only visual feedback. As on iOS, a dark key turns lighter and a white key turns grey.
     */
    fun pressed(isNight: Boolean): Map<ColorType, Int> {
        val pressedKey = if (isNight) Color.argb(110, 255, 255, 255) else Color.argb(250, 172, 177, 186)
        return mapOf(
            ColorType.KEY_BACKGROUND to pressedKey,
            ColorType.FUNCTIONAL_KEY_BACKGROUND to pressedKey,
            ColorType.SPACE_BAR_BACKGROUND to pressedKey,
        )
    }
}
