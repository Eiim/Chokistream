![Chokistream](banner.svg)

Nintendo 3DS wireless video capture software, compatible with [ChirunoMod](https://github.com/ChainSwordCS/ChirunoMod), [BootNTR](https://github.com/44670/BootNTR) and [HzMod](https://chainswordcs.com/horizon-by-sono.html) and the only cross-platform video capture software to support HzMod.

A from-scratch re-implementation of [Snickerstream](https://github.com/RattletraPM/Snickerstream), with TARGA support in part from [TGAHz](https://github.com/ChainSwordCS/TGAHz-Parsing) and [HorizonScreen](hps://github.com/gamingaddictionz03/HorizonM)

Notable features:
 * Only client to support ChirunoMod!
 * Pure Java - works on essentially any platform. Tested on Windows and Linux, and lightly tested on Mac and FreeBSD.
   * Versions < 2.0 support Windows, Linux, and Mac, with OS-specific builds. Builds may not be available for all platforms for all releases.
 * TGA (lossless compression) support for HzMod, the only application other than HorizonScreen to do so!
 * Various display output options to get it looking just right on your monitor
 * No monitor? No problem! Chokistream can run entirely headless and stream straight to a video file (currently very buggy), or output frames to image files.
 * Dual-Screen HzMod support for version 2017-05-05 (one of two dual-screen versions)

Plus even more planned:
 * Better controls and control options
 * Better CLI
 * More and better video file streaming

Currently under development by [Eiim](https://github.com/Eiim) and [ChainSwordCS](https://github.com/ChainSwordCS). Previously developed by [herronjo](https://github.com/herronjo).

## Getting Started

See the [Using Chokistream](https://github.com/Eiim/Chokistream/wiki/Using-Chokistream) wiki page.

## Build Instructions

Written in Java 17 with Gradle 7.5, requires Java 17 to be installed :)

No other requirements. Run `gradlew build` in Windows Command Prompt or `./gradlew build` in a Unix terminal. The jar is built to `build/libs/chokistream.jar`.

## Known issues

Besides GitHub issues:
 * Layout doesn't work for file streaming or image sequence
 * Error "The muxer track has finished muxing" when ending file streaming. This is perfectly safe.
 * Top frames in 24bpp games are broken for ChirunoMod v0.2 in JPEG mode. This is a ChirunoMod bug, not fixable on this side.
