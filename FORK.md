# Pitboard

Pitboard is a personal fork of [HeliBoard](https://github.com/HeliBorg/HeliBoard) with an iOS 26 look. It changes how the keyboard
looks and adds a second layout for large screens. It is not affiliated with Apple, and it is not an official HeliBoard release.
The app itself still says HeliBoard in Android's settings.

## Read this first

- **AI-assisted.** Claude wrote most of the changes in this fork, working with me and testing on my phone.
  HeliBoard's [AI usage policy](AI_USAGE.md) asks people not to send AI-generated pull requests upstream.
  So please do not send these changes to HeliBoard. If you want something from here, ask them in your own words.
- **Tested on one device.** I only tried it on a Galaxy Z Fold (SM-F971B, Android 17), on both screens.
  Other phones, tablets and Android versions are untested.
- **Samsung-specific blur.** The glass panel uses Samsung's own blur call through reflection. On other phones the keyboard
  uses Android's standard blur when the device allows it. I have not tested what the panel looks like when neither works.
- **Tuned for my phone.** Many defaults (key height, key gap, which layout shows on which screen) are set for the Z Fold.
- **arm64 only.** The build produces an arm64 app, with targetSdk 36 and NDK 27.
- **Glide typing.** HeliBoard cannot bundle Google's closed-source glide library, and neither does this fork.
  See the upstream README for how HeliBoard loads it from a file that you supply.

## License

HeliBoard is GPL-3.0, so this fork is GPL-3.0 as well. The license files from upstream are unchanged.

## Build and install

You need JDK 21, the Android SDK (platform 36) and NDK 27.

```sh
./gradlew assembleDebugNoMinify
adb install -r app/build/outputs/apk/debugNoMinify/HeliBoard_4.3-beta2-debugNoMinify.apk
adb shell ime enable helium314.keyboard.debug/helium314.keyboard.latin.LatinIME
adb shell ime set helium314.keyboard.debug/helium314.keyboard.latin.LatinIME
```

## What changed

### Look

The style follows the iOS 26 keyboard. I sampled colours from an iOS 26 dark-mode screenshot.

- **Style and colours.** The new style is `STYLE_IOS` with `THEME_IOS` (`KeyboardTheme.kt`, `Defaults.kt`).
  Colours are in `latin/common/IosColors.kt`, and the drawables and theme are in `res/drawable/ios_*.xml` and `res/values/themes-ios.xml`.
  Every key has the same translucent white fill, so letter keys and function keys look alike.
  Light mode exists but has not been checked on the device.
- **Glass panel.** `LatinIME.applyWindowBlur` blurs the area behind the keyboard. A tint alone gave a muddy panel over bright apps.
  So the code first passes a colour curve to the blur, which keeps the blurred app inside a dark band (or a light band in light mode).
  The panel tint goes on top. `latin/utils/SamsungBlur.kt` calls Samsung's blur API when the standard cross-window blur is off.
  The panel has 22dp rounded top corners, and the navigation bar contrast scrim is off, so the strip under the keys matches the panel.
- **Pressed keys.** A held key turns lighter in dark mode and grey in light mode (`IosColors.pressed`). HeliBoard's default of
  darkening by 6% is invisible on a translucent key. The blue action key keeps the default.
- **Key shape.** One rounded rectangle with a 9dp radius and no shadow edge (`ios_key.xml`). The key preview has a 12dp radius.
- **Icons.** Line icons that I drew in the style of SF Symbols (`ios_*.xml`, mapped in `KeyboardIconsSet.keyboardIconsIos`).
  They are original drawings and not copies of Apple's symbols.
- **Return key.** Blue only when the app asks for an action such as send, go or search.
- **Labels.** Function keys use lowercase text and stay lowercase when shift is on. The second symbols page key says `#+=`.
  The space bar is blank, except that HeliBoard still shows the language name when more than one language is enabled.
- **Key widths.** `KeyboardParser.makeLetterWidthsUniform` gives every letter row the same key width. Upstream fits each row on its own,
  so rows with more keys end up with narrower letters. Shift, delete and the other end keys share the space that is left.
- **Language key.** The globe key switches between HeliBoard's own languages only, and not to other keyboards.

### Navigation bar

With targetSdk 36 the keyboard window reaches down behind the navigation bar. On the Z Fold, Android reports that bar to the window
as a caption bar inset (126 px on the cover screen), and the navigation bar inset is 0.
Upstream relies on `fitsSystemWindows` in each keyboard view. Android does not send insets again when the emoji or clipboard view is
created later, so those views sat behind the bar. In floating mode the handles ended up under the bar too.

`InputView.onApplyWindowInsets` now takes the inset of all system bars and pads `keyboard_view_wrapper` at the bottom.
The views inside never see the inset. A floating keyboard gets no padding, and `FloatingKeyboardUtils` subtracts all system bars from
the space it can use.

### Inner screen (unfolded) layouts

The unfolded screen counts as a tablet. It has two layouts, and a key switches between them.

- **Full layout** (default), modelled on the iPad Pro keyboard and a PC keyboard. It adds a number row, tab, esc, ctrl, alt, meta,
  arrow keys, a split toggle and an emoji key (`functional_keys_tablet.json`). Shift turns the number row into `~ ! @ # ...` and the arrows into
  home, page down, page up and end. In portrait it uses `functional_keys_tablet_portrait.json`, which gives the letters more of the row.
- **Mobile layout.** The normal HeliBoard layout (`functional_keys_mobile.json`) with one extra key, "full".

The switch is key code `TOGGLE_FULL_LAYOUT` (-110). `Settings.isFullLayout()` is true only on a tablet with the `full_layout` preference on.
The cover screen always keeps the phone layout.

### Other changes

- Cover-screen defaults (in `Defaults.kt`): key hints off, row height scale 0.78, vibration on, toolbar chevron off.
- Inner-screen defaults: keyboard height scale 0.82 in landscape and key gap scale 0.4.
- HeliBoard reads the hinge sensor to tell whether the phone is folded, so the cover screen uses size indexes 2 and 3.
- The extra `!` and `?` keys next to the comma are off on tablets.
- Inline autofill pills get an 8dp gap between them.

## Known gaps

- The emoji view and landscape on the inner screen have not been checked since the navigation bar fix.
- The floating panel has square bottom corners.
- The unit tests do not compile, because of `InputTest.kt` in upstream.
