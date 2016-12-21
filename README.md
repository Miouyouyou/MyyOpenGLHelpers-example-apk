About
-----

This is an example APK that is used in combination with 
[MyyOpenGLHelpers-example](https://github.com/Miouyouyou/MyyOpenGLHelpers-example).

Currently it looks for a CMakeLists.txt in the directory parent to this
one, as a result of the [app/build.gradle](./app/build.gradle) 
configuration.

You can change this behaviour by modifying the "path" argument of the
cmake block in that configuration file, like this :

```groovy
 externalNativeBuild {
    cmake {
      path "/path/to/another/CMakeLists.txt"
    }
  }
```

Requirements
------------

You'll need a recent NDK (v12+) and SDK (buildToolsVersion 25.0.1).
The required SDK version can be tweaked in 
[app/build.gradle](./app/build.gradle).

If you run the compilation through a terminal, without importing the
project in Android Studio first, you'll need the `ANDROID_NDK_HOME` and
`ANDROID_SDK_HOME` environment variables set !

However, **if you imported the project in Android Studio, do NOT set 
these variables** or you might encounter a NullPointerException during 
the initialisation of Gradle, due to 
[this bug](https://github.com/orrc/android-externalnativebuild-crash),
that triggers when `ANDROID_NDK_HOME` is set AND a local.properties file
exists.

This bug might also trigger during Red Moon nights or if you feed your 
Android after midnight.

Compilation and Installation
----------------------------

Plug an Android phone or an emulator and either :

* Run `./gradlew installDebug`
* Import the project through Android Studio and click the Debug icon


