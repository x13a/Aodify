# Aodify

Show AOD on demand.

<!-- [<img -->
<!--      src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png" -->
<!--      alt="Get it on F-Droid" -->
<!--      height="80">](https://f-droid.org/packages/me.lucky.aodify/) -->
[<img
     src="https://play.google.com/intl/en_us/badges/images/generic/en-play-badge.png"
     alt="Get it on Google Play"
     height="80">](https://play.google.com/store/apps/details?id=me.lucky.aodify)

<img
     src="https://raw.githubusercontent.com/x13a/Aodify/main/fastlane/metadata/android/en-US/images/phoneScreenshots/1.png"
     width="30%"
     height="30%">

Tiny app to show Always On Display on demand.  
Tested with Google Pixel.

You have to grant `WRITE_SECURE_SETTINGS` permission:
```sh
$ adb shell pm grant me.lucky.aodify android.permission.WRITE_SECURE_SETTINGS
```

## Permissions

* NOTIFICATION_LISTENER - turn on AOD on notification
* WRITE_SECURE_SETTINGS - control AOD

## License
[![GNU GPLv3 Image](https://www.gnu.org/graphics/gplv3-127x51.png)](https://www.gnu.org/licenses/gpl-3.0.en.html)

This application is Free Software: You can use, study share and improve it at your will.
Specifically you can redistribute and/or modify it under the terms of the
[GNU General Public License v3](https://www.gnu.org/licenses/gpl.html) as published by the Free
Software Foundation.
