/*
 * The MIT License
 *
 * Copyright 2016 Oracle.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package drawandcut;

/**
 * @author akouznet
 */
public class Configuration {

    public static final boolean DISABLE_CUTTER = Boolean.getBoolean("disableCutter");
    public static final boolean DISABLE_CAMERA = Boolean.getBoolean("disableCamera");

    public static final double IN = 25.4; // mm
    public static final double CM = 10; // mm

    public static final double MATERIAL_SIZE_X = Double.parseDouble(System.getProperty("materialSizeX", "200")); // mm
    public static final double MATERIAL_SIZE_Y = Double.parseDouble(System.getProperty("materialSizeY", "200")); // mm
    public static final double MATERIAL_SIZE_Z = Double.parseDouble(System.getProperty("materialSizeZ", "3.175")); // mm (1/8")
    public static final double MATERIAL_SIZE_RATIO = MATERIAL_SIZE_X / MATERIAL_SIZE_Y;

    public static final double MATERIAL_BASE_X = 0; // mm
    public static final double MATERIAL_BASE_Y = 0; // mm
    public static final double MATERIAL_BASE_Z = Double.parseDouble(System.getProperty("materialBaseZ", "0")); // mm

    public static final double TOOL_DIAMETER = 1 / 8. * IN; // mm

    public static final double MOTIF_WIDTH_MM = 3 * TOOL_DIAMETER; // mm

    public static final int RPM = Integer.parseInt(System.getProperty("rpm", "9000")); // rpm
    public static final double FEED = Double.parseDouble(System.getProperty("feed", Double.toString(44 * IN))); // mmpm
    public static final double PLUNGE_FEED = 14 * IN; // mmpm
    public static final double RECOMMENDED_DOC = 0.019 * IN; // 0.4826 mm <-- recommended DOC
    public static final double INITIALS_DOC = RECOMMENDED_DOC / 2; // mm
    public static final int NUMBER_OF_PASSES = Integer.parseInt(System.getProperty("numberOfPasses", "7"));
    public static final double DOC = MATERIAL_SIZE_Z / NUMBER_OF_PASSES; // mm
    public static final double Z_ACCURACY = 0.01; // mm

    public static final double PROBING_OFFSET = Double.parseDouble(System.getProperty("probingOffset", "0.81")); // mm (delta between probing Z and coordinate 0)
    public static final String PORT_NAME = System.getProperty("portName", "COM4");
    public static final double FLATNESS = 0.01; // mm

    public static final int SCREEN_WIDTH = Integer.parseInt(System.getProperty("screenWidth", "800"));
    public static final int SCREEN_HEIGHT = Integer.parseInt(System.getProperty("screenHeight", "480"));

    public static final boolean NO_HOLE = Boolean.getBoolean("noHole");

    public static final double BUTTON_PREF_WIDTH = 100;
    public static final double BUTTON_PREF_HEIGHT = 70;
    public static final double PADDING = 8;

    public static final int SCREEN_PADDING_TOP = Integer.parseInt(System.getProperty("screenPaddingTop", "0"));
    public static final int SCREEN_PADDING_BOTTOM = Integer.parseInt(System.getProperty("screenPaddingBottom", "0"));
    public static final int SCREEN_PADDING_LEFT = Integer.parseInt(System.getProperty("screenPaddingLeft", "0"));
    public static final int SCREEN_PADDING_RIGHT = Integer.parseInt(System.getProperty("screenPaddingRight", "0"));

    public static final double HOLE_DIAMETER = 6 / 32. * IN; // mm
    public static final double HOLE_DISTANCE_FROM_EDGE = 2.5 * CM; // mm

    public static final String FONT_FACE = System.getProperty("fontFace", "Verdana");
    public static final int FONT_SIZE = Integer.getInteger("fontSize", 100);

    static {
        System.out.println("Configuration summary:");
        System.out.println("DISABLE_CUTTER = " + DISABLE_CUTTER);
        System.out.println("DISABLE_CAMERA = " + DISABLE_CAMERA);
        System.out.println("System.getProperty(\"disableCamera\") = " + System.getProperty("disableCamera"));
        System.out.println();
        System.out.printf("Material size (X x Y x Z): %.1f x %.1f x %.3f mm. Bottom Z = %.3f mm.\n", MATERIAL_SIZE_X, MATERIAL_SIZE_Y, MATERIAL_SIZE_Z, MATERIAL_BASE_Z);
        System.out.printf("Number of passes: %d. Depth of one cut: %.3f mm (Recommended: %.3f mm).\n", NUMBER_OF_PASSES, DOC, RECOMMENDED_DOC);
        System.out.printf("RPM: %d. Feed: %.0f mmpm, plunge feed: %.0f mmpm\n", RPM, FEED, PLUNGE_FEED);
        System.out.println();
        System.out.printf("Material size (X x Y x Z): %.2f x %.2f x %.3f in. Bottom Z = %.3f in.\n", MATERIAL_SIZE_X / IN, MATERIAL_SIZE_Y / IN, MATERIAL_SIZE_Z / IN, MATERIAL_BASE_Z / IN);
        System.out.printf("Number of passes: %d. Depth of one cut: %.4f in (Recommended: %.4f in).\n", NUMBER_OF_PASSES, DOC / IN, RECOMMENDED_DOC / IN);
        System.out.printf("RPM: %d. Feed: %.1f ipm, plunge feed: %.1f ipm\n", RPM, FEED / IN, PLUNGE_FEED / IN);

        System.out.println("");
    }
}
