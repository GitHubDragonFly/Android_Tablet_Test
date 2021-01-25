# TabletTest
Android tablet app for communication with Allen Bradley and/or Modbus programmable logic controllers (PLC).

Intended to be used solely as a testing tool (not fit for any production environment).
Could also represent a great resource for educational purposes.

Minimum requirement is Android 4.1 (API level 16) while targeting Android 11 (API level 30). This should cover lots of old Android tablets as well as new.

Designed for landscape orientation due to the number of components used.
Also designed to hide the status bar and keep the screen turned on.
In order to work as designed, it uses permission to access Internet and Network State (see app/src/main/AndroidManifest.xml file).

It is using the following open source libraries, added to the app in the form of AAR modules:

- [libplctag](https://github.com/libplctag/libplctag) v2.2.0
- [jna](https://github.com/java-native-access/jna) v5.6.0

It is also using a modified version of the Tag.java wrapper, part of the libplctag project, so a tag_id for every tag created could be mapped and used in the software.
Other modifications would be related to added methods for unsigned integers, previously mentioned in the related libplctag4android project (see below) as well as an additional experimental 128-bit support. All the modifications can be seen just by openning this project, navigating to the "libplctag" project, openning the AAR file and navigating through classes.jar.

Related project: [libplctag4android](https://github.com/libplctag/libplctag4android)

This app is as experimental as the above mentioned related project but a bit more elaborate.
The above mentioned related project can be used to compile the latest prerelease version of the libplctag library.

See [this link](https://github.com/libplctag/libplctag4android/issues/1) for instructions on how to create libplctag AAR.

# Functionality
- Only a single value will be displayed per tag entered, either of string/char/integer/float...etc.
- It provides automated READ while, during this operation, unused tag spots can be populated and used to write in parallel.
- The left half of the screen is for a PLC utilizing AB protocol while the right half is for Modbus (simultaneous use of both).
- The "Get Tags" button will fetch ControlLogix tags, both Controller and Program, and by selecting any of the fetched tags it will be copied to the clipboard.
- You can specify the name of the Program to get tags for (the default is set to MainProgram).
- "Write" buttons are only functional when tag is present AND value to write is present. Only a single "Write" process per PLC can be running at the time.
- The "Screen Clean" button allows for freezing and cleaning the screen without exiting the app.
- The "Gauge" screens have the custom [AngleIndicator](https://github.com/GitHubDragonFly/AndroidUserControls) control which can respond to multiple data types.
- The custom [LEDLight & RoundGauge](https://github.com/GitHubDragonFly/AndroidUserControls) controls are present but currently not used. You can add them if you have a need for it. Check the related [PhoneTest](https://github.com/GitHubDragonFly/PhoneTest) project for how it could be setup.
- As for AB tags, you will need to specify the Custom String Length when the "custom string" data type is selected.
- As for Modbus tags, you will need to specify the String Length when the "string" data type is selected.
- Modbus addressing: CO = Coil, DI = Discrete Input, IR = Input Register, HR = Holding Register.
- Modbus byte/word swapping is a bit tricky but I hope most of it functions correctly.
- Some error handling has been built into the app but it is also relying on the libplctag library itself for additional error handling.

There might be bugs in the app. Not everything could be tested by me, since I don't have access to all the different PLCs supported by the libplctag library.
See the libplctag website for all PLCs supported by the library.

Screenshots folder has pictures of this app running inside the Android x86 emulator tablet (Nexus 7 (2012) API 25) as well as BlueStacks emulator.
The app was also tested as working on an old RCA tablet with arm processor and Android 5.0.

# Build

All it takes is to:

- Download and install Android Studio.
- Download and extract the zip file of this project.
- Open this as an existing project in Android Studio and, on the menu, do a Build/Rebuild Project.
- Locate created APK file in this folder: app/build/outputs/apk/free/debug.
- Copy the APK over to your tablet and install it (you might need to enable the "Install from Unknown Sources" option).

# Licensing
This is all dual licensed under Mozilla Public License 2.0 and GNU Lesser/Library General Public License 2.1 to cover for the use of libplctag and jna libraries.

# Trademarks
Any and all trademarks, either directly or indirectly mentioned in this project, belong to their respective owners.

# Useful Resources
The AdvancedHMI website [forum](https://www.advancedhmi.com/forum/), which is another open source project providing communication with a large number of different PLCs.

