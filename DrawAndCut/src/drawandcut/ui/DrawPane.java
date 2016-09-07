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
package drawandcut.ui;

import drawandcut.Configuration;
import static drawandcut.Configuration.MATERIAL_SIZE_X;
import static drawandcut.Configuration.MATERIAL_SIZE_Y;
import javafx.beans.binding.Bindings;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.scene.canvas.Canvas;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import drawandcut.path.Outliner;
import drawandcut.path.PathConversions;
import drawandcut.path.SmallPolygonsCleaner;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import static drawandcut.Configuration.MOTIF_WIDTH_MM;
import javafx.geometry.Bounds;
import javafx.scene.Group;

/**
 *
 * @author akouznet
 */
public class DrawPane extends StackPane {
    
    private final Pane canvasBackground = new Pane();
    private final Canvas canvas;
    private ObjectProperty<Drawing> drawing = new SimpleObjectProperty<>();
    private double pxPerMm = 1;

    public DrawPane() {
        setBackground(new Background(new BackgroundFill(Color.DARKGRAY, CornerRadii.EMPTY,
                Insets.EMPTY)));
        canvasBackground.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY,
                Insets.EMPTY)));
        
        canvas = new Canvas();
        canvas.widthProperty().bind(Bindings.createDoubleBinding(() -> {
                    return Math.min(widthProperty().get(), heightProperty().get() * Configuration.MATERIAL_SIZE_RATIO);
                },
                widthProperty(), heightProperty()));
        canvas.heightProperty().bind(Bindings.createDoubleBinding(() -> {
                    return Math.min(widthProperty().get() / Configuration.MATERIAL_SIZE_RATIO, heightProperty().get());
                },
                widthProperty(), heightProperty()));
        canvasBackground.maxWidthProperty().bind(canvas.widthProperty());
        canvasBackground.maxHeightProperty().bind(canvas.heightProperty());
        getChildren().addAll(canvasBackground, canvas);
        
        canvas.setOnMousePressed(e -> startDrawing(e));
        canvas.setOnMouseDragged(e -> continueDrawing(e));
        canvas.setOnMouseReleased(e -> stopDrawing(e));
        canvas.setOnTouchPressed(e -> startDrawing(e));
        canvas.setOnTouchMoved(e -> continueDrawing(e));
        canvas.setOnTouchReleased(e -> stopDrawing(e));
        
        canvas.getGraphicsContext2D().setLineCap(StrokeLineCap.ROUND);
        canvas.getGraphicsContext2D().setLineJoin(StrokeLineJoin.ROUND);
        canvas.getGraphicsContext2D().setStroke(Color.BLACK);
        
        canvas.widthProperty().addListener(e -> {
            pxPerMm = canvas.getWidth() / Configuration.MATERIAL_SIZE_X;
            canvas.getGraphicsContext2D().setLineWidth(MOTIF_WIDTH_MM * pxPerMm);
        });
    }
    
    private double getX(InputEvent e) {
        if (e instanceof MouseEvent) {
            MouseEvent me = MouseEvent.class.cast(e);
            return me.getX();
        } else if (e instanceof TouchEvent) {
            TouchEvent te = TouchEvent.class.cast(e);
            return te.getTouchPoint().getX();
        } else {
            return Double.NaN;
        }
    }
    
    private double getY(InputEvent e) {
        if (e instanceof MouseEvent) {
            MouseEvent me = MouseEvent.class.cast(e);
            return me.getY();
        } else if (e instanceof TouchEvent) {
            TouchEvent te = TouchEvent.class.cast(e);
            return te.getTouchPoint().getY();
        } else {
            return Double.NaN;
        }
    }
    
    private void startDrawing(InputEvent e) {
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        drawing.set(new Drawing(x, y));
    }
    
    private void continueDrawing(InputEvent e) {
        if (drawing.get() == null) {
            return;
        }
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        drawing.get().continueTo(x, y);
    }
    
    private void stopDrawing(InputEvent e) {
        if (drawing.get() == null) {
            return;
        }
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        drawing.get().stop(x, y);
    }
    
    public void importSVG(String svg) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svg);
        svgPath.setFillRule(FillRule.EVEN_ODD);
        
        Bounds b = svgPath.getBoundsInLocal();
        if (b.getWidth() != 0 && b.getHeight() != 0) {
            double scale = Math.min(
                    MATERIAL_SIZE_X / b.getWidth(), 
                    MATERIAL_SIZE_Y / b.getHeight());            
            svgPath.getTransforms().addAll(
                    new Translate(
                            -b.getMinX() * scale + (MATERIAL_SIZE_X - b.getWidth() * scale) / 2, 
                            -b.getMinY() * -scale + (MATERIAL_SIZE_Y - b.getHeight() * -scale) / 2), 
                    new Scale(scale, -scale));
        }
        System.out.println("before svgPath.getBoundsInLocal() = " + svgPath.getBoundsInLocal());
        System.out.println("after svgPath.getBoundsInParent() = " + svgPath.getBoundsInParent());
        
        drawing.set(new Drawing(0, 0));
        Path path = (Path) Shape.union(svgPath, new Rectangle(0, 0));
        printPathCount(path, "path");
        Path simplifiedPath = simplify(path);    
        printPathCount(simplifiedPath, "simplifiedPath");
        Path outlinedPath = new Outliner(simplifiedPath).generateFilledOutline();
        printPathCount(outlinedPath, "outlinedPath");
        outline = simplify(outlinedPath);
        printPathCount(outline, "outline");
        outline.setStrokeWidth(Configuration.TOOL_DIAMETER);
        outline.setStroke(Color.BLACK);
        outline.setOpacity(0.5);
        outline.setStrokeLineJoin(StrokeLineJoin.ROUND);
        outline.setStrokeLineCap(StrokeLineCap.ROUND);
        outline.getTransforms().addAll(
                new Translate(0, -Configuration.MATERIAL_SIZE_Y), 
                new Scale(pxPerMm, -pxPerMm, 0, MATERIAL_SIZE_Y));
        outline.setMouseTransparent(true);
        outline.setManaged(false);
        outline.layoutXProperty().bind(canvas.layoutXProperty());
        outline.layoutYProperty().bind(canvas.layoutYProperty());
        System.out.println("before outline.getBoundsInLocal() = " + outline.getBoundsInLocal());
        System.out.println("after outline.getBoundsInParent() = " + outline.getBoundsInParent());
        getChildren().add(outline);

        g = new Group(svgPath);
        svgPath.setFill(Color.RED);
        svgPath.setOpacity(0.5);
        g.setMouseTransparent(true);
        g.setManaged(false);
        g.layoutXProperty().bind(canvas.layoutXProperty());
        g.layoutYProperty().bind(canvas.layoutYProperty());
        g.getTransforms().addAll(
                new Translate(0, -Configuration.MATERIAL_SIZE_Y), 
                new Scale(pxPerMm, -pxPerMm, 0, MATERIAL_SIZE_Y));
        getChildren().add(g);
        
    }

    private static void printPathCount(Path path, String name) {
        System.out.println(name + " count = " + path.getElements().stream().filter(
                elem -> elem instanceof MoveTo).count());
    }
    
    private Path outline;
    private Group g;

    private Path simplify(Path path) {
        // step 1: remove cubic curves
        Path path1 = PathConversions.convertToPath(PathConversions.convertToPath2D(
                path).getPathIterator(null, Configuration.FLATNESS));
        printPathCount(path1, "path1");
        // step 2: remove small polygons
        Path path2 = SmallPolygonsCleaner.clean(path1);
        return path2;
    }

    public class Drawing {
        private Path p = new Path();

        private Drawing(double x, double y) {
            p.getElements().add(new MoveTo(convertX(x), convertY(y)));
            if (outline != null) {
                getChildren().remove(outline);
                outline = null;
            }
            if (g != null) {
                getChildren().remove(g);
                g = null;
            }
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.getGraphicsContext2D().beginPath();
            canvas.getGraphicsContext2D().moveTo(x, y);
        }
        
        private void continueTo(double x, double y) {
            p.getElements().add(new LineTo(convertX(x), convertY(y)));
            canvas.getGraphicsContext2D().lineTo(x, y);
            canvas.getGraphicsContext2D().stroke();
        }
        
        private void stop(double x, double y) {
            p.getElements().add(new LineTo(convertX(x), convertY(y)));
            p.getElements().add(new ClosePath());
            canvas.getGraphicsContext2D().lineTo(x, y);
            canvas.getGraphicsContext2D().closePath();
            canvas.getGraphicsContext2D().stroke();
            generateOutline();
        }
        
        private double convertX(double x) {
            return x / pxPerMm;
        }
        
        private double convertY(double y) {
            return Configuration.MATERIAL_SIZE_Y - y / pxPerMm;
        }

        public Path getPath() {
            return new Path(p.getElements());
        }

        public Path getOutline() {
            return outline;
        }
        
        private void generateOutline() {
    //        Path path = new Path(new MoveTo(0, 0), new LineTo(100, 0), new LineTo(80, 25), new LineTo(100, 50), new LineTo(0, 50), new ClosePath());
    //        Outliner outliner = new Outliner(path);
            Outliner outliner = new Outliner(drawing.get().getPath());
            outline = outliner.generateOutline();
            outline.setStrokeWidth(Configuration.TOOL_DIAMETER);
            outline.setStroke(Color.BLACK);
            outline.setStrokeLineJoin(StrokeLineJoin.ROUND);
            outline.setStrokeLineCap(StrokeLineCap.ROUND);
            outline.getTransforms().addAll(
                    new Translate(0, -Configuration.MATERIAL_SIZE_Y), 
                    new Scale(pxPerMm, -pxPerMm, 0, Configuration.MATERIAL_SIZE_Y));
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            getChildren().add(outline);
            outline.setMouseTransparent(true);
            outline.setManaged(false);
            outline.layoutXProperty().bind(canvas.layoutXProperty());
            outline.layoutYProperty().bind(canvas.layoutYProperty());
        }
    }

    public ObjectProperty<Drawing> drawingProperty() {
        return drawing;
    }
    
}
