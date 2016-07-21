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
package drawandcut.path;

import com.esri.core.geometry.Geometry;
import com.esri.core.geometry.OperatorBoundary;
import com.esri.core.geometry.OperatorBuffer;
import com.esri.core.geometry.Point;
import com.esri.core.geometry.Polyline;
import drawandcut.Configuration;
import java.awt.BasicStroke;
import java.awt.Shape;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import javafx.collections.ObservableList;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

/**
 *
 * @author akouznet
 */
public class Outliner {
    private final Path path;

    public Outliner(Path path) {
        this.path = path;
    }
    
    public Path generateOutline() {
        Polyline path2D = convertToPolyline();
        Geometry buffer = OperatorBuffer.local().execute(path2D, null, Configuration.LINE_WIDTH_MM / 2, null);
        Geometry outlineGeom = OperatorBoundary.local().execute(buffer, null);

        Path outline = new Path();
        Polyline outlinePolyline = (Polyline) outlineGeom;
        int pathCount = outlinePolyline.getPathCount();
        System.out.println("pathCount = " + pathCount);
        if (pathCount != 2) {
            throw new IllegalArgumentException("The path cannot have intersections or have no interior outline");
        }   
        
        Point p = new Point();
        for (int i = 0; i < pathCount; i++) {
            int ps = outlinePolyline.getPathStart(i);
            int pe = outlinePolyline.getPathEnd(i);
            outlinePolyline.getPoint(ps, p);
            outline.getElements().add(new MoveTo(p.getX(), p.getY()));
            for (int j = ps + 1; j < pe; j++) {
                outlinePolyline.getPoint(j, p);
                outline.getElements().add(new LineTo(p.getX(), p.getY()));
            }
            outline.getElements().add(new ClosePath());
        }
        return outline;
    }

    private Polyline convertToPolyline() throws UnsupportedOperationException {
        Polyline polyline = new Polyline();
        ObservableList<PathElement> elements = path.getElements();
        for (PathElement element : elements) {
            if (element instanceof MoveTo) {
                MoveTo mt = (MoveTo) element;
                polyline.startPath(mt.getX(), mt.getY());
            } else if (element instanceof LineTo) {
                LineTo lt = (LineTo) element;
                polyline.lineTo(lt.getX(), lt.getY());
            } else if (element instanceof ClosePath) {
                polyline.closePathWithLine();
            } else {
                throw new UnsupportedOperationException("This PathElement is not supported: " + element);
            }
        }
        return polyline;
    }
}
