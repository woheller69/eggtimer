# Smart EggTimer

<img src="fastlane/metadata/android/en-US/images/phoneScreenshots/EggTimer.png" width="150"/> 
This application provides an egg timer which calculates the boiling time according to the formula derived by Dr. Charles D. H. Williams,
based on required consistency, altitude, start temperature, and egg weight.

The formula is taken from here: http://newton.ex.ac.uk/teaching/CDHW/egg/

[<img src="https://fdroid.gitlab.io/artwork/badge/get-it-on.png"
     alt="Get it on F-Droid"
     height="80">](https://f-droid.org/packages/org.woheller69.eggtimer/)

## Instructions

Place the phone at a position with GPS reception and wait until your altitude is shown.

Enter size of the egg. The egg sizes are as defined by the EU.

| EU size | S | M | L | XL |
| ------- | --- | --- |--- | --- |
| Weight (g) | <53 | 53..63 | 63..73 | >73 |

Enter temperature of the egg.

Select desired consistency or core temperature.

Pierce the egg and put into boiling water. The egg must be fully covered with water.
Let the water boil slightly.

Press the START button.

## Permissions

The app requires only authorization for GPS. This is needed to get the altitude which is used for calculation.
This information is used only within the app. Nothing is sent via the internet.

## Used third-party libraries

- AndroidX libraries (https://github.com/androidx/androidx) which is licensed under <a href='https://github.com/androidx/androidx/blob/androidx-main/LICENSE.txt'>Apache License Version 2.0</a>
- Material Components for Android (https://github.com/material-components/material-components-android) which is licensed under <a href='https://github.com/material-components/material-components-android/blob/master/LICENSE'>Apache License Version 2.0</a>
