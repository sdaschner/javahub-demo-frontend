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

import com.esri.core.geometry.OperatorGeneralize;
import com.esri.core.geometry.Polyline;
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
import drawandcut.path.OutlinerEsri;
import drawandcut.path.Smoother;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;

/**
 *
 * @author akouznet
 */
public class DrawPane extends BorderPane {
    
//    private static final Color CUT_COLOR = BACKGROUND_COLOR;
    private static final Color CUT_COLOR = Color.BLACK;
    private final Pane canvasBackground = new Pane();
    private final Canvas canvas;
    private ObjectProperty<Drawing> drawing = new SimpleObjectProperty<>();
    private final DoubleProperty pxPerMm = new SimpleDoubleProperty(1);
    private final StackPane stackPane;
    private Label title;
    private ObjectProperty<Path> outline = new SimpleObjectProperty<>();
    private ObjectProperty<Point2D> hole = new SimpleObjectProperty<>();
    private Group g;
    private final Circle holeCircle;
    private final Outliner outliner = new OutlinerEsri();
//    private final Outliner outliner = new OutlinerJava2D();
    
    private final Path smoothedPath = new Path();


    public DrawPane() {
        setId("drawPane");
        stackPane = new StackPane();
        canvasBackground.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY,
                Insets.EMPTY)));
        
        canvas = new Canvas();
        canvas.widthProperty().bind(Bindings.createDoubleBinding(() -> {
                    return Math.min(stackPane.widthProperty().get(), stackPane.heightProperty().get() * Configuration.MATERIAL_SIZE_RATIO);
                },
                stackPane.widthProperty(), stackPane.heightProperty()));
        canvas.heightProperty().bind(Bindings.createDoubleBinding(() -> {
                    return Math.min(stackPane.widthProperty().get() / Configuration.MATERIAL_SIZE_RATIO, stackPane.heightProperty().get());
                },
                stackPane.widthProperty(), stackPane.heightProperty()));
        canvasBackground.maxWidthProperty().bind(canvas.widthProperty());
        canvasBackground.maxHeightProperty().bind(canvas.heightProperty());
        stackPane.getChildren().addAll(canvasBackground, canvas);
        
        canvas.getGraphicsContext2D().setLineCap(StrokeLineCap.ROUND);
        canvas.getGraphicsContext2D().setLineJoin(StrokeLineJoin.ROUND);
        canvas.getGraphicsContext2D().setStroke(CUT_COLOR);
        
        canvas.widthProperty().addListener(e -> {
            pxPerMm.set(canvas.getWidth() / Configuration.MATERIAL_SIZE_X);
            canvas.getGraphicsContext2D().setLineWidth(MOTIF_WIDTH_MM * pxPerMm.get());
        });
        
        title = new Label("Draw");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(25));
        BorderPane.setAlignment(title, Pos.CENTER);
        
        setCenter(stackPane);
        setTop(title);
        
        drawShape();
        
        holeCircle = new Circle();
        holeCircle.radiusProperty().bind(pxPerMm.multiply(Configuration.HOLE_DIAMETER / 2));
        holeCircle.setFill(CUT_COLOR);
        holeCircle.setManaged(false);
        holeCircle.setMouseTransparent(true);
        holeCircle.layoutXProperty().bind(canvas.layoutXProperty());
        holeCircle.layoutYProperty().bind(canvas.layoutYProperty());
        
        smoothedPath.setManaged(false);
        smoothedPath.setStroke(Color.RED);
        smoothedPath.layoutXProperty().bind(canvas.layoutXProperty());
        smoothedPath.layoutYProperty().bind(canvas.layoutYProperty());
        stackPane.getChildren().add(smoothedPath);
    }
    
    public void drawShape() {
        canvas.setOnMousePressed(e -> startDrawing(e));
        canvas.setOnMouseDragged(e -> continueDrawing(e));
        canvas.setOnMouseReleased(e -> stopDrawing(e));
        canvas.setOnTouchPressed(e -> startDrawing(e));
        canvas.setOnTouchMoved(e -> continueDrawing(e));
        canvas.setOnTouchReleased(e -> stopDrawing(e));
        title.setText("Draw shape");
        reset();
    }
    
    public void positionHole() {
        canvas.setOnMousePressed(e -> positionHole(e));
        canvas.setOnMouseDragged(e -> positionHole(e));
        canvas.setOnMouseReleased(null);
        canvas.setOnTouchPressed(e -> positionHole(e));
        canvas.setOnTouchMoved(e -> positionHole(e));
        canvas.setOnTouchReleased(null);
        title.setText("Position badge holder hole");
    }
    
    public void readyToCut() {
        title.setText("Ready to cut");
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
        reset();
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
        positionHole();
    }
    
    private void positionHole(InputEvent e) {
        if (drawing.get() == null) {
            return;
        }
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        drawing.get().positionHole(x, y);
        readyToCut();
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
        
        drawing.set(new Drawing(0, 0)); // TODO: Fix this workaround to indicate there is a drawing
        reset();
        Path path = (Path) Shape.union(svgPath, new Rectangle(0, 0));
        printPathCount(path, "path");
        Path simplifiedPath = simplify(path);    
        printPathCount(simplifiedPath, "simplifiedPath");
        Path outlinedPath = outliner.generateFilledOutline(simplifiedPath);
        printPathCount(outlinedPath, "outlinedPath");
        Path outlinePath = simplify(outlinedPath);
        printPathCount(outlinePath, "outline");
        outlinePath.setStrokeWidth(Configuration.TOOL_DIAMETER);
        outlinePath.setStroke(CUT_COLOR);
        outlinePath.setStrokeLineJoin(StrokeLineJoin.ROUND);
        outlinePath.setStrokeLineCap(StrokeLineCap.ROUND);
        outlinePath.getTransforms().addAll(
                new Translate(0, -Configuration.MATERIAL_SIZE_Y), 
                new Scale(pxPerMm.get(), -pxPerMm.get(), 0, MATERIAL_SIZE_Y));
        outlinePath.setMouseTransparent(true);
        outlinePath.setManaged(false);
        outlinePath.layoutXProperty().bind(canvas.layoutXProperty());
        outlinePath.layoutYProperty().bind(canvas.layoutYProperty());
        System.out.println("before outline.getBoundsInLocal() = " + outlinePath.getBoundsInLocal());
        System.out.println("after outline.getBoundsInParent() = " + outlinePath.getBoundsInParent());
        stackPane.getChildren().add(outlinePath);
        outline.set(outlinePath);

        g = new Group(svgPath);
        svgPath.setFill(Color.RED);
        svgPath.setOpacity(0.5);
        g.setMouseTransparent(true);
        g.setManaged(false);
        g.layoutXProperty().bind(canvas.layoutXProperty());
        g.layoutYProperty().bind(canvas.layoutYProperty());
        g.getTransforms().addAll(
                new Translate(0, -Configuration.MATERIAL_SIZE_Y), 
                new Scale(pxPerMm.get(), -pxPerMm.get(), 0, MATERIAL_SIZE_Y));
        stackPane.getChildren().add(g);
        
        positionHole();
    }

    private static void printPathCount(Path path, String name) {
        System.out.println(name + " count = " + path.getElements().stream().filter(
                elem -> elem instanceof MoveTo).count());
    }
    
    private Path simplify(Path path) {
        // step 1: remove cubic curves
        Path path1 = PathConversions.convertToPath(PathConversions.convertToPath2D(
                path).getPathIterator(null, Configuration.FLATNESS));
        printPathCount(path1, "path1");
        // step 2: remove small polygons
        Path path2 = SmallPolygonsCleaner.clean(path1);
        return path2;
    }
    
    private void reset() {
        if (outline.get() != null) {
            stackPane.getChildren().remove(outline.get());
            outline.set(null);
        }
        if (g != null) {
            stackPane.getChildren().remove(g);
            g = null;
        }
        stackPane.getChildren().remove(holeCircle);
        hole.set(null);
        canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        smoothedPath.getElements().clear();
    }

    public class Drawing {
        private Path p = new Path();
        private List<Point2D> points = new ArrayList<>();

        private Drawing(double x, double y) {
            p.getElements().add(new MoveTo(convertX(x), convertY(y)));
            canvas.getGraphicsContext2D().beginPath();
            canvas.getGraphicsContext2D().moveTo(x, y);
            points.add(new Point2D(x, y));
            smooth();
        }
        
        private void continueTo(double x, double y) {
            p.getElements().add(new LineTo(convertX(x), convertY(y)));
            canvas.getGraphicsContext2D().lineTo(x, y);
            canvas.getGraphicsContext2D().stroke();
            points.add(new Point2D(x, y));
            smooth();
        }
        
        private void stop(double x, double y) {
//            p.getElements().add(new LineTo(convertX(x), convertY(y)));
            p.getElements().add(new ClosePath());
            canvas.getGraphicsContext2D().lineTo(x, y);
            canvas.getGraphicsContext2D().closePath();
            canvas.getGraphicsContext2D().stroke();
//            generateOutline();
            points.add(new Point2D(x, y));
            points.add(points.get(0));
            smooth();
        }
        
        private void smooth() {
            Polyline polyline = new Polyline();
            if (points.size() > 0) {
                polyline.startPath(points.get(0).getX(), points.get(0).getY());
                for (int i = 1; i < points.size(); i++) {
                    polyline.lineTo(points.get(i).getX(), points.get(i).getY());
                }
                Polyline generalizedPolyline = (Polyline) OperatorGeneralize.local().execute(polyline, 10,
                        true, null);
                List<Point2D> smoothedPoints
                        = Arrays.asList(generalizedPolyline.getCoordinates2D())
                                .stream()
                                .map(p -> new Point2D(p.x, p.y))
                                .collect(Collectors.toList());
                smoothedPath.getElements().setAll(Smoother.smooth(smoothedPoints.toArray(new Point2D[smoothedPoints.size()])));
            }
        }

        private void positionHole(double x, double y) {
            hole.set(new Point2D(convertX(x), convertY(y)));

            holeCircle.setCenterX(x);
            holeCircle.setCenterY(y);
            if (!stackPane.getChildren().contains(holeCircle)) {
                stackPane.getChildren().add(holeCircle);
            }
        }
        
        private double convertX(double x) {
            return x / pxPerMm.get();
        }
        
        private double convertY(double y) {
            return Configuration.MATERIAL_SIZE_Y - y / pxPerMm.get();
        }

        public Path getPath() {
            return new Path(p.getElements());
        }

        private void generateOutline() {
    //        Path path = new Path(new MoveTo(0, 0), new LineTo(100, 0), new LineTo(80, 25), new LineTo(100, 50), new LineTo(0, 50), new ClosePath());
    //        Outliner outliner = new Outliner(path);
            Path outlinePath = outliner.generateOutline(drawing.get().getPath());
            outlinePath.setStrokeWidth(Configuration.TOOL_DIAMETER);
            outlinePath.setStroke(CUT_COLOR);
            outlinePath.setStrokeLineJoin(StrokeLineJoin.ROUND);
            outlinePath.setStrokeLineCap(StrokeLineCap.ROUND);
            outlinePath.getTransforms().addAll(
                    new Translate(0, -Configuration.MATERIAL_SIZE_Y), 
                    new Scale(pxPerMm.get(), -pxPerMm.get(), 0, Configuration.MATERIAL_SIZE_Y));
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            stackPane.getChildren().add(outlinePath);
            outlinePath.setMouseTransparent(true);
            outlinePath.setManaged(false);
            outlinePath.layoutXProperty().bind(canvas.layoutXProperty());
            outlinePath.layoutYProperty().bind(canvas.layoutYProperty());
            outline.set(outlinePath);
        }
    }

    public ObjectProperty<Point2D> holeProperty() {
        return hole;
    }
    
    public ObjectProperty<Path> outlineProperty() {
        return outline;
    }
    
}
