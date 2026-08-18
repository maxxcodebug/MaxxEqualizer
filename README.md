<h1><img width="100" src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="MaxxEqualizer" align="absmiddle"> MaxxEqualizer</h1>

<img src="https://img.shields.io/badge/Requires-Android%209.0%2B%20(API%2028)-3DDC84?logo=android&logoColor=white" alt="Requires Android 9.0+ (API 28)">
<img src="https://img.shields.io/badge/License-GPL--3.0-blue" alt="GPL-3.0">

<a href="https://github.com/MaxxPixel-OS">
  <img src="https://img.shields.io/badge/Part%20of-MaxxPixel%20OS-8A2BE2" alt="Part of MaxxPixel OS">
</a>


⚠️ This app is a fork, currently in **ALPHA**. Expect rough edges and missing polish while the rebrand and MaxxPixel OS integration work is ongoing. Issues and feedback are welcome.

## Fork Notice

MaxxEqualizer is a fork of **[Equalizer314](https://github.com/bearinmindcat/Equalizer314)** by **bearinmindcat**. All of the original DSP engineering, the DynamicsProcessing/Visualizer architecture, the AutoEQ fitting algorithm, and the core audio pipeline design in this app are bearinmindcat's work — full credit belongs to them. This fork exists to rebrand, restyle, and eventually integrate the app as the default system equalizer for **MaxxPixel OS**.

Maintained by **Anshuman X**.

## About

There's no true free/open-source alternative to apps like Wavelet and Poweramp EQ that pairs a full parametric EQ with real audio-visual feedback — that gap is what the original Equalizer314 project set out to fill, and this fork continues that goal. The app is built on Android's `DynamicsProcessing` API ([docs](https://developer.android.com/reference/android/media/audiofx/DynamicsProcessing)) — the same API Poweramp EQ and Wavelet use — paired with the `Visualizer` API for real-time audio-visual feedback, so both the main EQ and the Limiter/Multiband Compression tools give you a genuine audio-visual feedback loop instead of knobs you have to interpret blind.

<p align="center">
  <img width="48%" alt="Screenshot 1" src="https://github.com/user-attachments/assets/2449b96f-8306-4319-9eb0-ddead8ea84e5" />
  <img width="48%" alt="Screenshot 2" src="https://github.com/user-attachments/assets/720725c4-6205-456c-ad83-c2667757927d" />
</p>

Controls that don't use the Visualizer API — like the compressor attack/release curve — still give direct visual feedback the same way DAWs and VST plugins do: drag the slider, or drag directly on the curve itself.

<p align="center">
  <img width="48%" alt="Screenshot 3" src="https://github.com/user-attachments/assets/7c38d5b5-ca3e-4585-aad0-b35c8f57c610" />
  <img width="48%" alt="Screenshot 4" src="https://github.com/user-attachments/assets/261c4359-4f0d-44af-a199-068f570ff3ea" />
</p>

## Why DynamicsProcessing & Visualizer APIs?

A quick rundown of the audio-effect approaches available on Android, and why this app is built the way it is:

**Android's built-in `Equalizer` class** — fixed band count, the "lazy" option most non-audio apps fall back to. Attaches to an audio session.

**`AudioEffect` subclasses** ([docs](https://developer.android.com/reference/android/media/audiofx/AudioEffect)) — stronger than the built-in class but still short of DynamicsProcessing. Attaches to an audio session.

**AudioPlaybackCapture** (used by RootlessJamesDSP) — deeper framework access and a more accurate visualizer/spectrum, but requires ADB permissions via something like Shizuku, forces `RECORD_AUDIO`, adds latency, and some apps (Spotify included) block internal capture outright.

**AudioFlinger** (used by JamesDSP & ViPER4Android) — the most capable option with no latency downside, but requires root.

DynamicsProcessing + Visualizer was chosen as the best balance: minimal permissions, no root requirement, and enough headroom to build a genuinely powerful parametric EQ.

## Presets, EQ Generation & AutoEQ

The app runs the [AutoEQ](https://github.com/jaakkopasanen/AutoEq/wiki/How-Does-AutoEq-Work%3F) fitting algorithm under "Generate Custom EQ" — you supply a measurement and a target (both available from sources like squig.link), and it fits a parametric EQ curve to match. Presets export as EqualizerAPO-compatible files rather than an app-specific format, so they carry over cleanly to desktop EqualizerAPO or other APO-compatible tools without a conversion step.

<p align="center">
  <img width="48%" alt="Screenshot 5" src="https://github.com/user-attachments/assets/8150ad20-6170-42ae-aa02-4c6298f536ea" />
  <img width="48%" alt="Screenshot 6" src="https://github.com/user-attachments/assets/896cdd5f-a483-4eeb-8bb8-c378c6788bf4" />
</p>

## Known Issues

The app targets audio session 0 rather than attaching to individual sessions like Wavelet or Poweramp EQ do. Only one app can hold session 0 at a time; an auto-reclaim feature attempts to take it back if another app grabs it, but this can occasionally cause brief audio glitches/dropouts. This limitation is shared with Wavelet, RootlessJamesDSP, Poweramp EQ, and most other session-0 EQ apps.

## Screenshots

<p align="center">
  <img width="19%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/1.jpg" />
  <img width="19%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/2.jpg" />
  <img width="19%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/3.jpg" />
  <img width="19%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/4.jpg" />
  <img width="19%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/5.jpg" />
</p>

<p align="center">
  <img width="24%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/6.jpg" />
  <img width="24%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/7.jpg" />
  <img width="24%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/8.jpg" />
  <img width="24%" alt="Screenshot" src="fastlane/metadata/android/en-US/images/phoneScreenshots/9.jpg" />
</p>

*(Screenshots above are from upstream Equalizer314 and will be replaced with MaxxEqualizer's own — new floating glass nav bar, About page, and updated theming — as those land.)*

## What's Different in This Fork

- Full rebrand: package, namespace, app name, and UI strings moved from `com.bearinmind.equalizer314` to `com.maxxcodebug.maxxequalizer`
- Floating "liquid glass" pill navigation bar (Material 3 Expressive style) replacing the flat bottom bar
- New About page with credits, version info, and roadmap
- (In progress) AMOLED true-black theme option
- (In progress) MaxxPixel OS system integration

## Roadmap

- System-level integration as MaxxPixel OS's default equalizer
- Liquid glass UI pass across all DSP screens (Limiter, MBC, AutoEQ, etc.)
- AMOLED true-black theme
- Shared preset sync with other MaxxPixel OS apps

## Acknowledgment/Resources

Credited in code comments throughout, and listed here as good further reading:

- [Audio EQ Cookbook](https://www.w3.org/TR/audio-eq-cookbook/) — biquad math for the parametric EQ
- [Matched Second Order Digital Filters](https://www.vicanek.de/articles/BiquadFits.pdf) — bell filter math for the parametric EQ
- [AutoEq](https://github.com/jaakkopasanen/AutoEq) — target curve/measurement fitting + AutoEQ presets
- [*Digital Dynamic Range Compressor Design*](https://www.eecs.qmul.ac.uk/~josh/documents/2012/GiannoulisMassbergReiss-dynamicrangecompression-JAES2012.pdf) — hard/soft knee transfer function for the multiband compressor
- [ITU-R BS.1770](https://www.itu.int/rec/R-REC-BS.1770) — LUFS measurement standard
- [Linkwitz–Riley crossover](https://en.wikipedia.org/wiki/Linkwitz%E2%80%93Riley_filter) — crossover math for multiband compression

## License

MaxxEqualizer, like upstream Equalizer314, is released under the **GNU General Public License v3.0**. See [LICENSE](LICENSE) for the full text.

You are free to use, modify, and redistribute this software under the terms of the GPL v3. If you distribute a modified version, you must release the source under the same license.
