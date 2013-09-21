BrianDROID
==========

BrianDROID offers a template application which is used by Brian2's code generation to create a working Android app for running neural network simulations.
Brian2's android\_standalone submodule is required to create runnable simulations for BrianDROID, which is currently available in the devices\_android branch of Brian2, https://github.com/achilleas-k/Brian2/tree/devices_android .
The branch also contains example scripts for generating Android code (briandroid\_LIF\_example.py, briandroid\_HH\_example.py, briandroid\_IF\_curve.py).

Generating code
--------------
Any Brian2 script which defines ``set_device('android')`` at the top will generate code that will run in BrianDROID (some limitations exist; see the [wiki page](https://github.com/achilleas-k/BrianDROID/wiki/Limitations)).
When the script is run, it creates an ``output`` directory in the current working directory and saves 2 files:

- **Simulation.java** : The main simulation class file which is responsible for setting up the simulation, allocating memory (defining arrays, monitors, etc.), initialising the renderscript engine and calling the renderscript kernels every timestep.
- **renderscript.rs** : A renderscript file which contains functions for updating the state variables every time step. This is where the simulation state update equations, thresholding and resetting functions are defined.

The two files should be moved/copied to BrianDROID and the existing files with the same names should be overwritten.
The existing files are copies of the templates used to generate the code, which can also be found in the devices\_android branch of Brian2.

File locations in BrianDROID project:

- **Simulation.java**: `<BrianDROID root>/src/main/java/org/briansimulator/briandroid/Simulation.java`
- **renderscript.rs**: `<BrianDROID root>/src/main/rs/renderscript.rs`

Building BrianDROID
-------------------
Once the generated files are in their appropriate locations, the project can be compiled to generate an Android APK that will run the simulation on the device.
The project can be imported into Eclipse or Android Studio.
BrianDROID also contains a ``build.gradle`` file so one can build an APK file using gradle directly by running ``gradle build`` in the BrianDROID project root.
Note the required versions for Android SDK and build tools defined in the file when building.

Importing project to Android Studio
-----------------------------------
1. Clone the repository to a local directory.
2. Open Android Studio and open the Import Project dialog (File -> Import Project) or select the Import Project option from the Welcome screen.
3. Select the root of the cloned repository.
4. Default settings on the subsequent dialogs should require no change.

Installing and Running on Android
---------------------------------
When building using Android Studio, the build process automatically attempts to push the compiled APK to a connected device (USB debugging must be enabled on the device).
Alternatively, a virtual (emulated) device can be created and used, which may be useful for testing.

If the APK was built using gradle or any other method that doesn't automate the installation, the compiled APK can be found in `<BrianDROID root>/build/apk/`.
This can be installed using one of several methods:

- Transfer the APK to the device (through USB, putting it directly on the SD card, or copy over the network) and install through a file browser.
- Install using ``adb install <filename>`` while the device is connected and USB debugging is enabled.

Note that the apk directory should contain two files, ``BrianDROID-debug-unaligned.apk`` and ``BrianDROID-release-unsigned.apk``.
The former is automatically signed during the build process using the debug key.
Since Android apps must be signed to be installed, the unsigned APK will fail to install and debug-unaligned file allows the app to be installed without issue.
Debug signed files should never be used for distribution.
Since the app isn't meant for official distribution, there is no need to go through the signing process.

One may wish to zipalign the APK before installing by running ``zipalign -f -v 4 build/apk/BrianDROID-debug-unaligned.apk ./BrianDROID.apk`` (see http://developer.android.com/tools/help/zipalign.html ) and installing the resulting aligned file instead.


--------
--------
BrianDROID is a Google Summer of Code, 2013 project under the support of the [INCF](http://www.incf.org/) (as a mentoring organisation) and the two project mentors, Marcel Stimberg and Dan Goodman.

See the [INCF project page](http://www.incf.org/gsoc/2013/briandroid-neural-simulation-on-mobile-devices) and the [GSoC project page](http://www.google-melange.com/gsoc/project/google/gsoc2013/achilleask/24002) for the original project description.

--------
All code is licensed under the CeCILL free software license.
A copy of the license can be found in the root of the repository in the [LICENSE](https://raw.github.com/achilleas-k/BrianDROID/master/LICENSE) file.

