# TabletTest
Android tablet app for communication with Allen Bradley and Modbus programmable logic controllers (PLC).

Intended to be used solely as a testing tool (not fit for any production environment).

Minimum requirement is Android 4.1 (API level 16) while targeting Android 11 (API level 30), designed for landscape orientation due to the number of components used.

It is using the following open source libraries, in the form of AAR modules added to the app:

- libplctag ( https://github.com/libplctag/libplctag )
- jna ( https://github.com/java-native-access/jna )

It is also using a modified version of the Tag.java wrapper, part of the libplctag project, so a tag_id for every tag created could be mapped and used in the software.
Other modifications would be related to added methods for signed/unsigned integers, previously mentioned in the related libplctag4android project (see below).
All the modifications can be seen just by openning the project, navigating to the "libplctag" project and openning the AAR file.

Related project: https://github.com/libplctag/libplctag4android

This app is as experimental as the above mentioned related project, but a bit more elaborate.
The above mentioned related project can be used to compile the latest prerelease version of the libplctag library.

See the following link for instructions on how to create AAR for libplctag: https://github.com/libplctag/libplctag4android/issues/1

# Functionality

- Only a single value will be displayed per tag entered, either of string/char/integer/float...etc.
- It provides automated READ while unused tag spots can be used to write in parallel.
- The left half of the screen is for a PLC utilizing AB protocol while the right half is for Modbus (simultaneous use).

Screenshots folder has pictures of this app running inside the Android x86 emulator tablet.
The app was also tested as working on an old RCA tablet with arm processor and Android 5.0.
