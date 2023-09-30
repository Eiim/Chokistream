![Chokistream](banner.svg)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/19617caaedb14f799e8d0c3595119386)](https://app.codacy.com/gh/Eiim/Chokistream/dashboard)

Nintendo 3DS wireless video capture software, compatible with [ChirunoMod](https://github.com/ChainSwordCS/ChirunoMod), [BootNTR](https://github.com/44670/BootNTR) and [HzMod](https://chainswordcs.com/horizon-by-sono.html) and the only cross-platform video capture software to support HzMod.

A from-scratch re-implementation of [Snickerstream](https://github.com/RattletraPM/Snickerstream), with TARGA support in part from [TGAHz](https://github.com/ChainSwordCS/TGAHz-Parsing) and [HorizonScreen](hps://github.com/gamingaddictionz03/HorizonM)

Notable features:
 * Only client to support ChirunoMod!
 * Pure Java - works on essentially any platform. Tested on Windows and Linux, and lightly tested on Mac and FreeBSD.
   * Versions < 2.0 support Windows, Linux, and Mac, with OS-specific builds. Builds may not be available for all platforms for all releases.
 * TGA (lossless compression) support for HzMod, the only application other than HorizonScreen with such!
 * Dual-Screen HzMod support for version 2017-05-05 (one of two dual-screen versions)
 * Various display output options to get it looking just right on your monitor
 * Chokistream can also run entirely headless and stream straight to a video file, or output frames to image files.

Plus even more planned:
 * Better controls and control options
 * Better video file streaming and image sequences
 * Better NTR support
 * Better CLI
 * And more!

Currently under development by [Eiim](https://github.com/Eiim) and [ChainSwordCS](https://github.com/ChainSwordCS). Previously developed by [herronjo](https://github.com/herronjo).

## Getting Started

See the [Using Chokistream](https://github.com/Eiim/Chokistream/wiki/Using-Chokistream) wiki page. See [Options](https://github.com/Eiim/Chokistream/wiki/Options) for more comprehensive documentation.

## Supported OSs
As we're pure Java 17 SE, a very broad range of OSs should work. We have different tiers of support:
* Windows and Linux - we develop on these, so we continuously test on them. However, we don't do every test on both, so there may still be OS-specific bugs. Please report these!
* Mac OS and FreeBSD - @herronjo has run Chokistream on these and it seemed fine, but they're not really tested. Can probably fix OS-specific bugs if they occur.
* Haiku, others - untested but theoretically should work fine. Let us know about OS-specific bugs, but they won't be a high priority.

## Build Instructions

Written in Java 17 with Gradle 7.5, requires Java 17 to be installed :)

No other requirements. Run `gradlew build` in Windows Command Prompt or `./gradlew build` in a Unix terminal. The jar is built to `build/libs/chokistream.jar`.

## Known issues

Besides GitHub issues:
 * Layout doesn't work for file streaming or image sequence
 * Error "The muxer track has finished muxing" when ending file streaming. This is perfectly safe.
 * Similarly, "Socket closed" error when closing NTR is perfectly safe.

# Licensing

NTRClient.java and NTRUDPThread.java include work derived from [NTRClient](https://github.com/Nanquitas/NTRClient), which is licensed under GPLv2. Therefore, those files are licensed under GPLv2-only. All other files are licensed under GPLv2-or-later.

Prior to commit [5ac6e58](https://github.com/Eiim/Chokistream/commit/5ac6e585446b7e2bd3d652351066ad1fe421b70e), HzModClient.java (and at times other files) contained work derived from [Snickerstream](https://github.com/RattletraPM/Snickerstream), which is licensed under GPLv3-only, and so the entire repository was licensed under GPLv3-only. However, this is not the case. As such, the repository as a whole is now licensed under GPLv2, and the repository excluding NTRClient.java and NTRUDPThread.java is licensed under GPLv2-or-later. The GPLv2-licensed additions from NTRClient were added after the removal of GPLv3-licensed code from Snickstream.
