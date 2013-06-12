BrianDROID
==========

Port of the Brian neural network simulator for Android

Instructions
------------

Compiling in Android Studio

1. Clone the repository to a local directory.
2. Open Android Studio and open the Import Project dialog (File -> Import Project).
3. Select the root of the cloned repository.
4. Add the support-v4 library to the External Libraries:
    - Go to File -> Project Structure - SDKs
    - Select Android 4.2.2 from the middle pane
    - In the right-hand pane, under the Classpath tab, click the + (plus) symbol and browse for the library called android-support-v4.jar.
    - The default location of this jar, if you used the Android SDK manager, should be: <android-sdk root>/extras/android/support/v4/android-support-v4.jar
5. Compile the project.


