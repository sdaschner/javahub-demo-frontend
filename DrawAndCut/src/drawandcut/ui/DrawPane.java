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
import drawandcut.path.Outliner;
import drawandcut.path.OutlinerEsri;
import drawandcut.path.PathConversions;
import drawandcut.path.SmallPolygonsCleaner;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.InputEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TouchEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.text.Font;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;

import java.util.stream.Collectors;

import static drawandcut.Configuration.*;

/**
 * @author akouznet
 */
public class DrawPane extends BorderPane {
    private final TextPane textPane;

    public TextPane getTextPane() {
        return textPane;
    }

    private enum DrawStep {DrawShape, PositionHole, DrawInitials, ReadyToCut}

    //    private static final Color CUT_COLOR = BACKGROUND_COLOR;
    private static final Color CUT_COLOR = Color.BLACK;
    private final Pane canvas = new Pane();
    private final Path pathThin = new Path();
    private final Path pathThick = new Path();
    private final Path initials = new Path();
    private ObjectProperty<Drawing> drawing = new SimpleObjectProperty<>();
    private final DoubleProperty pxPerMm = new SimpleDoubleProperty(1);
    private final StackPane stackPane;
    private final Label title;
    private final Label errorMessage = new Label();
    private ObjectProperty<Path> outline = new SimpleObjectProperty<>();
    private ObjectProperty<Point2D> hole = new SimpleObjectProperty<>();
    private Group g;
    private final Circle holeCircle;
    private final Circle holeSafeZone;
    private final Outliner outliner = new OutlinerEsri();
    private double margin;
    private final Button nextButton = new Button();
    private final Button prevButton = new Button();
    private final ObjectProperty<DrawStep> drawStep = new SimpleObjectProperty<>(DrawStep.DrawShape);
//    private final Outliner outliner = new OutlinerJava2D();


    public DrawPane() {
        setId("drawPane");
        stackPane = new StackPane();
        canvas.setBackground(new Background(new BackgroundFill(Color.WHITE, CornerRadii.EMPTY,
                Insets.EMPTY)));

        canvas.setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        canvas.setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        canvas.prefWidthProperty().bind(Bindings.createDoubleBinding(() -> {
                    return Math.min(stackPane.widthProperty().get(), stackPane.heightProperty().get() * Configuration.MATERIAL_SIZE_RATIO);
                },
                stackPane.widthProperty(), stackPane.heightProperty()));
        canvas.prefHeightProperty().bind(Bindings.createDoubleBinding(() -> {
                    return Math.min(stackPane.widthProperty().get() / MATERIAL_SIZE_RATIO, stackPane.heightProperty().get());
                },
                stackPane.widthProperty(), stackPane.heightProperty()));


        stackPane.getChildren().add(canvas);

        textPane = new TextPane(5.0); // need to figure out proper width of line
        stackPane.getChildren().add(textPane);


        pathThick.setStrokeLineCap(StrokeLineCap.ROUND);
        pathThick.setStrokeLineJoin(StrokeLineJoin.ROUND);
        pathThick.setStroke(CUT_COLOR);
        pathThick.setManaged(false);
        pathThick.setMouseTransparent(true);
        pathThick.layoutXProperty().bind(canvas.layoutXProperty());
        pathThick.layoutYProperty().bind(canvas.layoutYProperty());
        stackPane.getChildren().add(pathThick);

        initials.setStrokeLineCap(StrokeLineCap.ROUND);
        initials.setStrokeLineJoin(StrokeLineJoin.ROUND);
        initials.setStroke(Color.web("7999AC"));
        initials.setManaged(false);
        initials.setMouseTransparent(true);
        initials.layoutXProperty().bind(canvas.layoutXProperty());
        initials.layoutYProperty().bind(canvas.layoutYProperty());
        stackPane.getChildren().add(initials);

        pathThin.setStrokeLineCap(StrokeLineCap.ROUND);
        pathThin.setStrokeLineJoin(StrokeLineJoin.ROUND);
        pathThin.setStroke(Color.web("FF8080"));
        pathThin.setManaged(false);
        pathThin.setMouseTransparent(true);
        pathThin.layoutXProperty().bind(canvas.layoutXProperty());
        pathThin.layoutYProperty().bind(canvas.layoutYProperty());
        stackPane.getChildren().add(pathThin);


        canvas.widthProperty().addListener(e -> {
            pxPerMm.set(canvas.getWidth() / MATERIAL_SIZE_X);
            pathThin.setStrokeWidth(MOTIF_WIDTH_MM * pxPerMm.get());
            pathThick.setStrokeWidth((MOTIF_WIDTH_MM + 2 * TOOL_DIAMETER) * pxPerMm.get());
            initials.setStrokeWidth(TOOL_DIAMETER * pxPerMm.get());
            margin = MOTIF_WIDTH_MM * pxPerMm.get() / 2;
        });

        title = new Label("Draw");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(25));
        BorderPane.setAlignment(title, Pos.CENTER);

        nextButton.setId("nextButton");
        nextButton.disableProperty()
                .bind(drawStep.isEqualTo(DrawStep.DrawShape)
                        .or(drawStep.isEqualTo(DrawStep.ReadyToCut))
                        .or(drawStep.isEqualTo(DrawStep.PositionHole).and(hole.isNull())));
        nextButton.setOnAction(t -> next());
        prevButton.setId("prevButton");
        prevButton.disableProperty()
                .bind(drawStep.isEqualTo(DrawStep.DrawShape));
        prevButton.setOnAction(t -> prev());
        HBox topHBox = new HBox(prevButton, title, nextButton);
        topHBox.setSpacing(20);
        topHBox.setAlignment(Pos.CENTER);

        setCenter(stackPane);
        setTop(topHBox);

        drawShape();

        holeCircle = new Circle();
        holeCircle.radiusProperty().bind(pxPerMm.multiply(HOLE_DIAMETER / 2));
        holeCircle.setFill(CUT_COLOR);
        holeCircle.setManaged(false);
        holeCircle.setMouseTransparent(true);
        holeCircle.layoutXProperty().bind(canvas.layoutXProperty());
        holeCircle.layoutYProperty().bind(canvas.layoutYProperty());

        holeSafeZone = new Circle();
        holeSafeZone.radiusProperty().bind(pxPerMm.multiply((HOLE_DISTANCE_FROM_EDGE + HOLE_DIAMETER) / 2));
        holeSafeZone.setStroke(Color.web("#7999AC"));
        holeSafeZone.setFill(Color.TRANSPARENT);
        holeSafeZone.visibleProperty().bind(drawStep.isEqualTo(DrawStep.PositionHole));
        holeSafeZone.setManaged(false);
        holeSafeZone.setMouseTransparent(true);
        holeSafeZone.layoutXProperty().bind(canvas.layoutXProperty());
        holeSafeZone.layoutYProperty().bind(canvas.layoutYProperty());
        holeSafeZone.centerXProperty().bind(holeCircle.centerXProperty());
        holeSafeZone.centerYProperty().bind(holeCircle.centerYProperty());

        errorMessage.setTextFill(Color.RED);
        errorMessage.setFont(Font.font(20));
        errorMessage.setManaged(false);
        errorMessage.setMouseTransparent(true);
        errorMessage.layoutXProperty().bind(canvas.layoutXProperty());
        errorMessage.layoutYProperty().bind(canvas.layoutYProperty());
        errorMessage.setAlignment(Pos.BOTTOM_CENTER);
        errorMessage.setPadding(new Insets(PADDING));

    }

    public final void prev() {
        switch (drawStep.get()) {
            case PositionHole:
                drawShape();
                hole.set(null);
                break;
            case DrawInitials:
                if (!undoInitials()) {
                    if (NO_HOLE) {
                        drawShape();
                    } else {
                        positionHole();
                    }
                    initials.getElements().clear();
                }
                break;
            case ReadyToCut:
                if (NO_HOLE) {
                    drawShape();
                } else {
                    drawInitials();
                }
                break;
            default:
                throw new IllegalStateException("Cannot proceed to the next drawing stage from this: " + drawStep.get());
        }
    }

    public final boolean undoInitials() {
        for (int i = initials.getElements().size() - 1; i >= 0; i--) {
            if (initials.getElements().get(i) instanceof MoveTo) {
                initials.getElements().subList(i, initials.getElements().size()).clear();
                return true;
            }
        }
        return false;
    }

    public final void next() {
        switch (drawStep.get()) {
            case PositionHole:
                drawInitials();
                break;
            case DrawInitials:
                readyToCut();
                break;
            default:
                throw new IllegalStateException("Cannot proceed to the next drawing stage from this: " + drawStep.get());
        }
    }

    public final void drawShape() {
        drawStep.set(DrawStep.DrawShape);
        canvas.setOnMousePressed(e -> startDrawing(e));
        canvas.setOnMouseDragged(e -> continueDrawing(e));
        canvas.setOnMouseReleased(e -> stopDrawing(e));
        canvas.setOnTouchPressed(e -> startDrawing(e));
        canvas.setOnTouchMoved(e -> continueDrawing(e));
        canvas.setOnTouchReleased(e -> stopDrawing(e));
        reset();
    }

    public void positionHole() {
        if (NO_HOLE) {
            readyToCut();
            return;
        }
        drawStep.set(DrawStep.PositionHole);
        canvas.setOnMousePressed(e -> positionHole(e));
        canvas.setOnMouseDragged(e -> positionHole(e));
        canvas.setOnMouseReleased(null);
        canvas.setOnTouchPressed(e -> positionHole(e));
        canvas.setOnTouchMoved(e -> positionHole(e));
        canvas.setOnTouchReleased(null);
        title.setText("Position badge holder hole");
    }

    private void drawInitials() {
        if (NO_HOLE) {
            readyToCut();
            return;
        }
        drawStep.set(DrawStep.DrawInitials);
        stackPane.getChildren().remove(initials);
        stackPane.getChildren().add(stackPane.getChildren().indexOf(outline.get()), initials);
        canvas.setOnMousePressed(e -> startDrawingInitials(e));
        canvas.setOnMouseDragged(e -> continueDrawingInitials(e));
        canvas.setOnMouseReleased(e -> continueDrawingInitials(e));
        canvas.setOnTouchPressed(e -> startDrawingInitials(e));
        canvas.setOnTouchMoved(e -> continueDrawingInitials(e));
        canvas.setOnTouchReleased(e -> continueDrawingInitials(e));
        title.setText("Draw your initials");
    }

    public void readyToCut() {
        drawStep.set(DrawStep.ReadyToCut);
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
        try {
            drawing.get().stop(x, y);
            if (NO_HOLE) {
                readyToCut();
            } else {
                positionHole();
            }
        } catch (IllegalArgumentException iae) {
            showErrorMessage(iae);
        }
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
    }

    private void startDrawingInitials(InputEvent e) {
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        initials.getElements().add(new MoveTo(clampX(x), clampY(y)));
    }

    private void continueDrawingInitials(InputEvent e) {
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        initials.getElements().add(new LineTo(clampX(x), clampY(y)));
    }

    public static enum ImportSource {MODEL, WEBAPP}

    ;

    public void importSVG(String svg, double size, ImportSource importSource) {
        SVGPath svgPath = new SVGPath();
        svgPath.setContent(svg);
        svgPath.setFillRule(FillRule.EVEN_ODD);

        Bounds b = svgPath.getBoundsInLocal();
        if (b.getWidth() != 0 && b.getHeight() != 0) {
            double scale = Math.min(
                    Math.min(size, MATERIAL_SIZE_X) / b.getWidth(),
                    Math.min(size, MATERIAL_SIZE_Y) / b.getHeight());
            svgPath.getTransforms().addAll(
                    new Translate(
                            -b.getMinX() * scale + (MATERIAL_SIZE_X - b.getWidth() * scale) / 2,
                            -b.getMinY() * -scale + (MATERIAL_SIZE_Y - b.getHeight() * -scale) / 2),
                    new Scale(scale, -scale));
        }
//        System.out.println("before svgPath.getBoundsInLocal() = " + svgPath.getBoundsInLocal());
//        System.out.println("after svgPath.getBoundsInParent() = " + svgPath.getBoundsInParent());

        drawing.set(new Drawing(0, 0)); // TODO: Fix this workaround to indicate there is a drawing
        reset();
        Path path = (Path) Shape.union(svgPath, new Rectangle(0, 0));
        printPathCount(path, "path");
        Path simplifiedPath = simplify(path);
        printPathCount(simplifiedPath, "simplifiedPath");
        Path outlinePath;
        switch (importSource) {
            case MODEL:
                Path outlinedPath = outliner.generateFilledOutline(simplifiedPath);
                printPathCount(outlinedPath, "outlinedPath");
                outlinePath = simplify(outlinedPath);
                printPathCount(outlinePath, "outline");
                break;
            case WEBAPP:
                outlinePath = simplifiedPath;
                break;
            default:
                throw new IllegalStateException("Unexpected import source: " + importSource);
        }
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
        stackPane.getChildren().addAll(g, outlinePath);

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
        stackPane.getChildren().removeAll(holeCircle, holeSafeZone, errorMessage);
        hole.set(null);
        pathThin.getElements().clear();
        pathThick.getElements().clear();
        initials.getElements().clear();
        title.setTextFill(Color.WHITE);
        title.setText("Draw shape");
    }

    public class Drawing {
        private Path p = new Path();

        private Drawing(double x, double y) {
            x = clampX(x);
            y = clampY(y);
            p.getElements().add(new MoveTo(convertX(x), convertY(y)));
            pathThin.getElements().add(new MoveTo(x, y));
            pathThick.getElements().add(new MoveTo(x, y));
        }

        private void continueTo(double x, double y) {
            x = clampX(x);
            y = clampY(y);
            p.getElements().add(new LineTo(convertX(x), convertY(y)));
            pathThin.getElements().add(new LineTo(x, y));
            pathThick.getElements().add(new LineTo(x, y));
        }

        private void stop(double x, double y) {
            x = clampX(x);
            y = clampY(y);
            p.getElements().add(new LineTo(convertX(x), convertY(y)));
            p.getElements().add(new ClosePath());
            pathThin.getElements().add(new LineTo(x, y));
            pathThin.getElements().add(new ClosePath());
            pathThick.getElements().add(new LineTo(x, y));
            pathThick.getElements().add(new ClosePath());
            generateOutline();
        }

        private void positionHole(double x, double y) {
            x = clampX(x);
            y = clampY(y);
            hole.set(new Point2D(convertX(x), convertY(y)));

            holeCircle.setCenterX(x);
            holeCircle.setCenterY(y);
            if (!stackPane.getChildren().contains(holeCircle)) {
                stackPane.getChildren().add(holeCircle);
                stackPane.getChildren().add(holeSafeZone);
            }
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
            pathThick.getElements().clear();
            stackPane.getChildren().add(outlinePath);
            outlinePath.setMouseTransparent(true);
            outlinePath.setManaged(false);
            outlinePath.layoutXProperty().bind(canvas.layoutXProperty());
            outlinePath.layoutYProperty().bind(canvas.layoutYProperty());
            outline.set(outlinePath);
        }
    }

    private double clampX(double x) {
        return Math.max(margin, Math.min(x, canvas.getWidth() - margin));
    }

    private double clampY(double y) {
        return Math.max(margin, Math.min(y, canvas.getHeight() - margin));
    }

    private double convertX(double x) {
        return x / pxPerMm.get();
    }

    private double convertY(double y) {
        return Configuration.MATERIAL_SIZE_Y - y / pxPerMm.get();
    }

    private void showErrorMessage(Exception ex) {
        errorMessage.setText(ex.getMessage());
        errorMessage.resize(canvas.getWidth(), canvas.getHeight());
        stackPane.getChildren().add(errorMessage);
    }

    public ObjectProperty<Point2D> holeProperty() {
        return hole;
    }

    public ObjectProperty<Path> outlineProperty() {
        return outline;
    }

    public Path getInitials() {
        return new Path(initials.getElements().stream().map(pathElement -> {
            if (pathElement instanceof MoveTo) {
                MoveTo mt = (MoveTo) pathElement;
                return new MoveTo(convertX(mt.getX()), convertY(mt.getY()));
            } else if (pathElement instanceof LineTo) {
                LineTo mt = (LineTo) pathElement;
                return new LineTo(convertX(mt.getX()), convertY(mt.getY()));
            } else {
                throw new IllegalStateException("Unexpected path element: " + pathElement);
            }
        }).collect(Collectors.toList()));
    }
}
