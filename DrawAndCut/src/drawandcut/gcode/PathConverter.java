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
import static drawandcut.Configuration.*;
import drawandcut.path.PathConversions;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.util.List;
import javafx.geometry.Point2D;
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
    private final Point2D hole;
    private final double feed;
    private final double plungeFeed;
    private final double doc;
    private double startX;
    private double startY;

    public PathConverter(Path path, Point2D hole, int rpm, double feed, double doc, double plungeFeed) {
        this.feed = feed;
        this.path = path;
        this.hole = hole;
        this.plungeFeed = plungeFeed;
        this.doc = doc;
        gcg.init(rpm);
        processHole();
        processPath();
        gcg.spindleStop();
        gcg.goHome();
        gcg.programEnd();
    }

    private void processHole() {
        if (hole != null) {
            double x = hole.getX();
            double y = hole.getY();
            if (HOLE_DIAMETER <= TOOL_DIAMETER) {
                gcg.rapid(x, y, gcg.getSafeZ());
                gcg.linearZF(gcg.getBottomZ(), plungeFeed);
                gcg.rapid(x, y, gcg.getSafeZ());
            } else {
                double dr = (HOLE_DIAMETER - TOOL_DIAMETER) / 2.;
                Path2D holePath1 = new Path2D.Double(new Ellipse2D.Double(x - dr, y - dr, dr + dr, dr + dr));
//                System.out.println("holePath1 = " + holePath1);                
                Path holePath = PathConversions.convertToPath(
                        holePath1.getPathIterator(null, Configuration.FLATNESS));
//                System.out.println("holePath = " + holePath);
                path.getElements().addAll(0, holePath.getElements());
            }
        }
    }
    
    private void processPath() {
        int zSteps = (int) Math.ceil(MATERIAL_SIZE_Z / doc);
        System.out.println("zSteps = " + zSteps);
        double oldZ = gcg.getTopZ();
        System.out.println("oldZ = " + oldZ);
        for (int i = 1; i <= zSteps; i++) {
            double newZ = (gcg.getBottomZ() * i + gcg.getTopZ() * (zSteps - i)) / zSteps;
            System.out.println("newZ = " + newZ);
            System.out.println("Math.abs(newZ - oldZ) = " + Math.abs(newZ - oldZ));
            System.out.println("doc = " + doc);
            assert Math.abs(newZ - oldZ) < doc + Z_ACCURACY;
            assert newZ <= gcg.getTopZ() + Z_ACCURACY;
            assert newZ >= gcg.getBottomZ() - Z_ACCURACY;
            processPathInXY(newZ);
            oldZ = newZ;
        }
        gcg.rapidZ(gcg.getSafeZ());        
    }
    
    private void processPathInXY(double targetZ) {
//        Bounds boundsInLocal = path.getBoundsInLocal();
//        System.out.println("path.getBoundsInLocal() = " + boundsInLocal);
        startX = Double.NaN;
        startY = Double.NaN;
        for (PathElement pe : path.getElements()) {
            if (pe instanceof MoveTo) {
                MoveTo mt = MoveTo.class.cast(pe);
                startX = mt.getX();
                startY = mt.getY();
                double x = convertX(startX);
                double y = convertY(startY);
                if (gcg.getZ() < gcg.getSafeZ() || x != gcg.getX() || y != gcg.getY()) {
                    if (!Double.isNaN(gcg.getZ()) || gcg.getZ() < gcg.getSafeZ()) {
                        gcg.rapidZ(gcg.getSafeZ());
                    }
                    gcg.rapid(x, y, gcg.getSafeZ());
                    gcg.rapidZ(gcg.getTopZ() + 1);
                }
                gcg.linearZF(targetZ, plungeFeed);
                gcg.setFeed(feed);
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
