
Please read before you run it:

It uses openjdk credentials but may also work without them. Please try and let me know if you can clone it.

When the app starts, it tries to connect to Cutter on COM4 by default (you can override this using -DportName=/dev/ttyACM0 property). If connection is successful it does homing and the probing.

Unfortunately, probing isn't stable. It should slow down half way to the probe (probe is on the right side in my cutter). If it doesn't - then something isn't right and you better turn off the cutter. It could also stop before touching the probe. Restart the app in that case too.

If everything's fine, it will return to initial (-5, -5, -5) position and reset work coordinate system so that 0 is on the top of the wasteboard (or the bottom of the material). I've got an empirical constant in Configuration.PROBING_OFFSET (-DprobingOffset=0.81 in mm). It will likely be different for you machine, so you need to measure it. This is an important thing as it affects the DOC precision.

    Measurement and calculation.

        Make sure you are using metric system.
        Do probing either using Nomad software or using DrawAndCut app. When the probing is done, there is something like [PRB:-2.500,-5.000,-84.405:1] in the output (there will be two of those, you the last one, it's more precise). Take Z from this message:
        PRB_Z = -84.405
        Manually move cutter to touch the wasteboard. A single sheet of paper may be inserted between the tool tip and the wasteboard, there should be noticeable friction when you move it. Record Z from machine coordinates:
        Z_W = -90.215
        Calculate PROBING_OFFSET:
        PROBING_OFFSET = PRB_Z - Z_W - 5 = -84.405 - (-90.215) - 5 = 0.81.

    Explanation.

    Formula is Z = PROBING_OFFSET - PRB_Z = 0.81 - (-84.405) = 85.215. This is the offset of the work coordinate (WC) system for the -5 MC, i. e. -5 MC matches 85.215 WC. Derived from here 0 WC matches 85.215 + 5 = -90.215 MC (machine coordinates) and So if you have measured top of the wasteboard to be at Z_W = -90.215 MC you can calculate PROBING_OFFSET as PRB_Z - Z_W - 5 = -84.405 - (-90.215) - 5 = 0.81.

You may also want to specify the size of the plastic you have. I've -DmaterialSizeX and -DmaterialSizeY double properties you can use to specify the size of the piece in mm. (X is left to right and Y is from you towards inside the cutter). 0 is roughly set to the left nearest corner of the wasteboard slightly shifted to the inside. So when you insert material, it should be aligned in that corner.

If the startup was successful, you can draw something on the screen. You can do several attempts, it resets automatically. Once you finished drawing, it will convert it into two tool paths and show them. Feel free to draw again :-) If there are no paths shown - then the shape is not correct. Draw again!

Press Print to start printing! Good luck on this step :-)

It will print the thing using 7 passes approach. You can play with parameters on the command line: -DnumberOfPasses=7 -Drpm=9000 -Dfeed=1118 (feed in mm) or in Configuration.java file.