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
package drawandcut.gcode;

import drawandcut.Configuration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author akouznet
 */
public class GCodeGenerator {
    
    public static final int MIN_RPM = 2000;
    public static final int MAX_RPM = 10000;
    public static final double EMPTY = Double.NaN;
    
    private static final double TOP_Z = Configuration.MATERIAL_BASE_Z + Configuration.MATERIAL_SIZE_Z;
    private static final double BOTTOM_Z = Configuration.MATERIAL_BASE_Z;
    private static final double SAFE_Z = TOP_Z + 5;
    
    private final List<String> output = new ArrayList<>();
    
    private double x = EMPTY, y = EMPTY, z = EMPTY, f = EMPTY, rpm = EMPTY;
    private Units units = null;
    private Coordinates coordinates = null;
    private MovementMode movementMode = null;

    public enum Units { MM, INCHES };
    public enum Coordinates { ABSOLUTE, RELATIVE };
    public enum MovementMode { RAPID, LINEAR };

    public GCodeGenerator() {
    }
    
    public void init(int targetRPM) {
        unitsMillimeters();
        coordinatesAbsolute();
        // skipping the tool change
        spindleClockwise(targetRPM);
    }
    
    public void rapidZ(double z) {
        rapid(EMPTY, EMPTY, z);
    }
    
    public void rapid(double x, double y) {
        rapid(x, y, EMPTY);
    }
    
    public void rapid(double x, double y, double z) {
        output.add("G0" + buildXYZF(x, y, z));
        this.x = x; this.y = y; this.z = z; this.movementMode = MovementMode.RAPID;
    }

    public void linearZ(double z) {
        linear(EMPTY, EMPTY, z, EMPTY);
    }
    
    public void linearZF(double z, double f) {
        linear(EMPTY, EMPTY, z, f);
    }
    
    public void linear(double x, double y) {
        linear(x, y, EMPTY, EMPTY);
    }
    
    public void linear(double x, double y, double z) {
        linear(x, y, z, EMPTY);
    }

    public void linear(double x, double y, double z, double f) {
        output.add("G1" + buildXYZF(x, y, z, f));
        this.x = x; this.y = y; this.z = z; this.f = f; 
        this.movementMode = MovementMode.LINEAR;
    }

    public void unitsInches() {
        output.add("G20");
        this.units = Units.INCHES;
    }
    
    public void unitsMillimeters() {
        output.add("G21");
        this.units = Units.MM;
    }
    
    public void coordinatesAbsolute() {
        output.add("G90");
        this.coordinates = Coordinates.ABSOLUTE;
    }
    
    public void coordinatesRelative() {
        output.add("G91");
        this.coordinates = Coordinates.RELATIVE;
    }
    
    public void spindleClockwise(int rpm) {
        if (rpm > MAX_RPM || rpm < MIN_RPM) {
            throw new IllegalArgumentException("Spindle rpm is out of range " + MIN_RPM + " to " + MAX_RPM + ": " + rpm);
        }
        output.add("M3 S" + rpm);
        this.rpm = rpm;
    }
    
    public void spindleStop() {
        output.add("M5");
        this.rpm = 0;
    }
    
    public void programEnd() {
        output.add("M30");
        resetState();
    }
    
    public void goHome() {
        output.add("$H");
        resetState();
    }
    
    private void resetState() {
        this.x = this.y = this.z = this.f = this.rpm = EMPTY;
        this.units = null;
        this.coordinates = null;
        this.movementMode = null;
    }

    void setFeed(double feed) {
        output.add(buildXYZF(EMPTY, EMPTY, EMPTY, feed));
        this.f = feed;
    }
    
    private String buildXYZF(double... coords) {
        StringBuilder sb = new StringBuilder(20);
        String[] names = { "X", "Y", "Z", "F" };
        for (int i = 0; i < coords.length && i < names.length; i++) {
            if (!Double.isNaN(coords[i])) {
                sb.append(names[i]).append(String.format("%.3f", coords[i]));
            }
        }
        return sb.toString();
    }

    public double getSafeZ() {
        return SAFE_Z;
    }

    public double getBottomZ() {
        return BOTTOM_Z;
    }

    public double getTopZ() {
        return TOP_Z;
    }

    public List<String> getOutput() {
        return Collections.unmodifiableList(output);
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public double getF() {
        return f;
    }

    public double getRpm() {
        return rpm;
    }
    
    public Units getUnits() {
        return units;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public MovementMode getMovementMode() {
        return movementMode;
    }
}
