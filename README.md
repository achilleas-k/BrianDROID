BrianDROID
==========

Port of the Brian neural network simulator for Android

Importing project to Android Studio
-----------------------------------

1. Clone the repository to a local directory.
2. Create a libs directory under the repository directory and add the support-v4 library.
    - Find and copy the android-support-v4.jar to the libs/ directory.
    - The default location of this jar, if you used the Android SDK manager, should be: \<android-sdk root\>/extras/android/support/v4/android-support-v4.jar
3. Open Android Studio and open the Import Project dialog (File -> Import Project) or select the Import Project option from the Welcome screen.
4. Select the root of the cloned repository.
5. Default settings on the subsequent dialogs should require no change.


Usage
-----

BrianDROID works as a classloader for pre-compiled simulation classes whose main code was automatically generated using brian2's codegen module.
The current state of the code generator requires some code to be pasted and edited manually to work.

The following procedure should work for most simple simulations

1. Write a brian2 python simulation and set it up to print all generated code.
    - Set the run duration to 0 seconds.
    - Use ``codeobj_class=JavaCodeObject`` when defining the ``NeuronGroup``
    - Collect generated code from the CodeObject and print it to screen.
    - See https://raw.github.com/achilleas-k/brian2/codegen_java/examples/HH_codegen_android.py for a working example.

2. Copy all generated code to the SimTemplate found at https://raw.github.com/achilleas-k/BDsimulations/master/SimTemplate.java
    - Overwrite any existing ``setup`` or ``run`` methods.
    - Rename the class to match the filename (without the extension).

3. Use the ``dex`` script found at https://raw.github.com/achilleas-k/BDsimulations/master/dex to compile the java code to dalvik dex.
    - Note the paths for javac and dx and adjust them accordingly
    - The ``-source`` and ``-target`` arguments are required when using javac version 1.7.

4. Copy the resulting .dex file to the external storage of your android device.
    - Currently, BrianDROID searches for .dex files in ``/sdcard/BrianDROIDsims/``.

5. When BrianDROID is started, it will list the names of all .dex classes found in the directory.
Clicking any name will bring up a view which allows for running the simulation.


