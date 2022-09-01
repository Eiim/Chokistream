![Chokistream](banner.svg)

Nintendo 3DS wireless video capture software, compatible with [BootNTR](https://github.com/44670/BootNTR) and [HzMod](https://chainswordcs.com/horizon-by-sono.html) and the only cross-platform video capture software to support HzMod.

A from-scratch re-implementation of [Snickerstream](https://github.com/RattletraPM/Snickerstream), with planned TARGA support from [TGAHz](https://github.com/ChainSwordCS/TGAHz-Parsing) and [HorizonScreen](hps://github.com/gamingaddictionz03/HorizonM)

Notable features:
  * Portable: it's just a standalone jar file!
  * TGA (lossless compression) support for HzMod, the only application other than HorizonScreen to do so!
  * Various display output options to get it looking just right on your monitor
  * No monitor? No problem! Chokistream can run entirely headless and stream straight to a video file. (currently very buggy)
  * Color correction modes to fix whatever weirdness HzMod may throw your way
  * Dual-Screen HzMod support for version 2017-05-05 (one of two dual-screen versions)

Plus even more planned:
  * NFC patching for NTR using various patch types (a single patch is currently implemented)
  * Better controls and control options
  * More HzMod TGA support

Currently under development by [Eiim](https://github.com/Eiim). Previously developed by [herronjo](https://github.com/herronjo) and [ChainSwordCS](https://github.com/ChainSwordCS).

## Getting Started

See the [Using Chokistream](https://github.com/Eiim/Chokistream/wiki/Using-Chokistream) wiki page.

## Build Instructions

Written in Java 17 with Gradle 7.5, requires Java 17 to be installed :)

No other requirements. Run `gradlew build` in Windows Command Prompt or `./gradlew build` in a Unix terminal. The jar is built to `app/build/libs/chokistream.jar`.
