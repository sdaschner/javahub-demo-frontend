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
public final class PathConversions {
    public static Path2D convertToPath2D(Path path) throws UnsupportedOperationException {
        Path2D path2D = new Path2D.Double();
        ObservableList<PathElement> elements = path.getElements();
        for (PathElement element : elements) {
            if (element instanceof MoveTo) {
                MoveTo mt = (MoveTo) element;
                path2D.moveTo(mt.getX(), mt.getY());
            } else if (element instanceof LineTo) {
                LineTo lt = (LineTo) element;
                path2D.lineTo(lt.getX(), lt.getY());
            } else if (element instanceof CubicCurveTo) {
                CubicCurveTo cct = (CubicCurveTo) element;
                path2D.curveTo(
                        cct.getControlX1(), cct.getControlY1(), 
                        cct.getControlX2(), cct.getControlY2(), 
                        cct.getX(), cct.getY());
            } else if (element instanceof ClosePath) {
                path2D.closePath();
            } else {
                throw new UnsupportedOperationException("This PathElement is not supported: " + element);
            }
        }
        return path2D;
    }
    
    public static Path convertToPath(PathIterator pathIterator) {
        Path path = new Path();
        double[] coords = new double[6];
        while (!pathIterator.isDone()) {            
            int segType = pathIterator.currentSegment(coords);
            switch (segType) {
                case PathIterator.SEG_CLOSE:
                    path.getElements().add(new ClosePath());
                    break;
                case PathIterator.SEG_LINETO:
                    path.getElements().add(new LineTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_MOVETO:
                    path.getElements().add(new MoveTo(coords[0], coords[1]));
                    break;
                case PathIterator.SEG_CUBICTO:
                    path.getElements().add(new CubicCurveTo(coords[0], coords[1],
                            coords[2], coords[3], coords[4], coords[5]));
                    break;
                default:
                    throw new UnsupportedOperationException("This segment type is not supported: " + segType);                    
            }
            pathIterator.next();
        }
        return path;
    }
}
