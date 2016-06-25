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

import java.util.List;
import javafx.geometry.Bounds;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

/**
 *
 * @author akouznet
 */
public class PathConverter {

    private final GCodeGenerator gcg = new GCodeGenerator();
    private final Path path;
    private final double targetFeed;
    private double startX;
    private double startY;

    public PathConverter(Path input, int targetRPM, double targetFeed) {
        this.targetFeed = targetFeed;
        this.path = input;
        gcg.init(targetRPM);
        processPath();
        gcg.rapidZ(gcg.getSafeZ());
        gcg.spindleStop();
        gcg.programEnd();
    }

    private void processPath() {
        Bounds boundsInLocal = path.getBoundsInLocal();
        System.out.println("path.getBoundsInLocal() = " + boundsInLocal);
        startX = Double.NaN;
        startY = Double.NaN;
        for (PathElement pe : path.getElements()) {
            if (pe instanceof MoveTo) {
                MoveTo mt = MoveTo.class.cast(pe);
                gcg.rapidZ(gcg.getSafeZ());
                startX = mt.getX();
                startY = mt.getY();
                gcg.rapid(convertX(startX), convertY(startY), gcg.getSafeZ());
                gcg.linearZF(gcg.getBottomZ(), targetFeed);
            } else if (pe instanceof LineTo) {
                assertStarted();
                LineTo lt = LineTo.class.cast(pe);
                gcg.linear(convertX(lt.getX()), convertY(lt.getY()));
            } else if (pe instanceof ClosePath) {
                assertStarted();
                gcg.linear(convertX(startX), convertY(startY));
            } else {
                throw new IllegalArgumentException("Unsupported path element: "
                        + pe);
            }
        }
    }
    
    private void assertStarted() {
        if (Double.isNaN(startX) || Double.isNaN(startY)) {
            throw new IllegalStateException("Path has not been started");
        }
    }

    private double convertX(double x) {
        return x;
    }

    private double convertY(double y) {
        return y;
    }

    public List<String> getOutput() {
        return gcg.getOutput();
    }
}
