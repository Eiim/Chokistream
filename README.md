![Chokistream](banner.svg)

[![Codacy Badge](https://app.codacy.com/project/badge/Grade/19617caaedb14f799e8d0c3595119386)](https://app.codacy.com/gh/Eiim/Chokistream/dashboard)![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/Eiim/Chokistream/gradle.yml) ![GitHub commits since latest release](https://img.shields.io/github/commits-since/Eiim/Chokistream/latest)

Nintendo 3DS wireless video capture software, compatible with [NTR](https://github.com/Nanquitas/BootNTR), [NTR-HR](https://github.com/xzn/ntr-hr/releases), [HzMod](https://chainswordcs.com/horizon-by-sono.html), and [ChirunoMod](https://github.com/ChainSwordCS/ChirunoMod), and the only cross-platform video capture software to support HzMod.

A from-scratch reimplementation of [Snickerstream](https://github.com/RattletraPM/Snickerstream).

Notable features:
 * Pure Java - works on essentially any platform. Tested on Windows and Linux, and lightly tested on Mac and FreeBSD.
 * Various display output options to get it looking just right
 * Chokistream can also run entirely headless and stream straight to a video file, or output frames to image files.

Plus even more planned:
 * Better NTR support
 * Fullscreen support
 * [And more!](https://github.com/users/Eiim/projects/7)

Currently under development by [Eiim](https://github.com/Eiim) and [ChainSwordCS](https://github.com/ChainSwordCS). Previously developed by [herronjo](https://github.com/herronjo).

## Getting Started

Make sure NTR, NTR-HR, HzMod, or ChirunoMod is installed and running on the 3DS. NTR and NTR-HR only support the New 3DS family.

See the [basic usage](https://github.com/Eiim/Chokistream/wiki/Basic-Chokistream-usage) wiki page. See [Options](https://github.com/Eiim/Chokistream/wiki/Options) for more comprehensive documentation.

## Supported OSs
As we're pure Java 17 SE, a very broad range of OSs should work. We have different tiers of support:
* Windows and Linux - we develop on these, so we continuously test on them. However, we don't do every test on both, so there may still be OS-specific bugs. Please report these!
* Mac OS and FreeBSD - @herronjo has run Chokistream on these and it seemed fine, but they're not really tested. Can probably fix OS-specific bugs if they occur.
* Haiku, others - untested but theoretically should work fine. Let us know about OS-specific bugs, but they won't be a high priority.

Note: Old versions prior to 2.0 support Windows, Linux, and Mac, with OS-specific builds. Builds may not be available for all platforms for all releases.

## Build Instructions

Written in Java 17 with Gradle 7.5, requires Java 17 to be installed :)

No other requirements. Run `gradlew build` in Windows Command Prompt or `./gradlew build` in a Unix terminal. The jar is built to `build/libs/chokistream.jar`.

## Known issues

Besides GitHub issues:
 * Error "The muxer track has finished muxing" when ending file streaming. This is perfectly safe.

# Licensing

NTRClient.java includes work derived from [NTRClient](https://github.com/Nanquitas/NTRClient) and [NTR](https://github.com/44670/NTR), projects which are licensed under GPLv2. Therefore, this file is licensed under GPLv2-only. All other files are licensed under GPLv2-or-later.

Prior to commit [5ac6e58](https://github.com/Eiim/Chokistream/commit/5ac6e585446b7e2bd3d652351066ad1fe421b70e), HzModClient.java (and at times other files) contained work derived from [Snickerstream](https://github.com/RattletraPM/Snickerstream), which is licensed under GPLv3-only, and so the entire repository was licensed under GPLv3-only. However, this is no longer the case. As such, the repository as a whole is now licensed under GPLv2, and the repository excluding NTRClient.java is licensed under GPLv2-or-later. The GPLv2-licensed additions from NTRClient were added after the removal of GPLv3-licensed code from Snickstream.
