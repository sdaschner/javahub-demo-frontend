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

import java.util.ArrayList;
import java.util.List;
import javafx.geometry.Point2D;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.CubicCurveTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.PathElement;

/**
 *
 * @author akouznet, JosePereda
 */
public class SmallPolygonsCleaner {

    private final static double MINIMUM_AREA = 3000;
    private final static int POINTS_CURVE = 20;

    private Point2D p0;
    private List<PathElement> elements;
    private List<Path> validPaths;
    private List<PathElement> linearPath;
    private List<Point2D> list;
//    private Affine affine;
    
    public static Path clean(Path path) {
        SmallPolygonsCleaner spc = new SmallPolygonsCleaner();
        spc.clearSmallPolygons(path);
        return new Path(spc.linearPath);
    }
    
    private void clearSmallPolygons(Path path) {
//        affine = new Affine(new Translate(-pane.localToScene(Point2D.ZERO).getX(), 
//                -pane.localToScene(Point2D.ZERO).getY()));
        validPaths = new ArrayList<>();
        linearPath = new ArrayList<>();
        for (PathElement elem : path.getElements()) {
            clearSmallPolygons(elem);
        }
        clearSmallPolygons((PathElement) null);
    }
    
    private void clearSmallPolygons(PathElement elem){
        if (elem instanceof MoveTo) {
            close();
            list = new ArrayList<>();
            elements = new ArrayList<>();
            elements.add(elem);
            p0 = new Point2D(((MoveTo)elem).getX(), ((MoveTo)elem).getY());
            list.add(p0);
        } else if (elem instanceof LineTo) {
            elements.add(elem);
            list.add(new Point2D(((LineTo)elem).getX(), ((LineTo)elem).getY()));
        } else if (elem instanceof CubicCurveTo) {
            elements.add(elem);
            Point2D ini = list.size() > 0 ? list.get(list.size() - 1) : p0;
            for (int i = 0; i < POINTS_CURVE; i++) {
                list.add(evalCubicBezier((CubicCurveTo)elem, ini, ((double)i)/POINTS_CURVE));
            }
        } else if (elem instanceof ClosePath) {
            elements.add(elem);
            list.add(p0);
            close();
        } else if (elem == null) {
            close();
        } else {
            throw new IllegalStateException("Unexpected PathElement: " + elem);
        }
    }
    
    private void close() {
        if (list != null && Math.abs(calculateArea()) > MINIMUM_AREA) {
            validPaths.add(new Path(elements));
            linearPath.addAll(generateLinearPath().getElements());
        }
        list = null;
    }
    
    private Point2D evalCubicBezier(CubicCurveTo c, Point2D ini, double t){
        Point2D p = new Point2D(Math.pow(1 - t, 3) * ini.getX() +
                3 * t * Math.pow(1 - t, 2) * c.getControlX1() +
                3 * (1 - t) * t * t * c.getControlX2() +
                Math.pow(t, 3) * c.getX(),
                Math.pow(1 - t, 3)*ini.getY() +
                3 * t * Math.pow(1 - t, 2) * c.getControlY1() +
                3 * (1 - t) * t * t * c.getControlY2() +
                Math.pow(t, 3) * c.getY());
        return p;
    }

    private Point2D getPoint(int i) {
        return list.get(i >= list.size() ? i - list.size() : i);
    }   
    
    private double calculateArea(){
        double a = 0d;
        for (int i = 0; i < list.size(); i++) {
            a += getPoint(i).crossProduct(getPoint(i + 1)).getZ();
        } 
        return Math.abs(a/2d);
    }   
    
    private Path generateLinearPath() {
//        Point2D m = affine.transform(list.get(0));
        Point2D m = list.get(0);
        Path path = new Path(new MoveTo(m.getX(), m.getY()));
        for (Point2D p : list) {
//            m = affine.transform(p);
            m = p;
            path.getElements().add(new LineTo(m.getX(), m.getY()));
        }
        path.getElements().add(new ClosePath());
//        path.setStroke(Color.GREEN);
//        path.setFill(Color.RED);
        return path;
    }    
}
