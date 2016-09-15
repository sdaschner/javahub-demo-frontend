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
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
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
import javafx.scene.shape.FillRule;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Shape;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.scene.transform.Scale;
import javafx.scene.transform.Translate;
import java.util.stream.Collectors;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.PathElement;
import javafx.scene.text.Font;
import static drawandcut.Configuration.*;
import java.util.Comparator;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.geometry.BoundingBox;
import javafx.scene.Node;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;

/**
 * @author akouznet
 */
public class DrawPane extends BorderPane {
    private final TextPane textPane;

    public TextPane getTextPane() {
        return textPane;
    }

    private enum DrawStep {DrawShape, PositionHole, TypeText, DrawInitials, ReadyToCut}
    private enum SortOrder { 
        TEXT_FIELD, CANVAS, INTERIOR, THIN, 
        TEXT, INITIALS, 
        OUTLINE, HOLE, THICK, THIN_DRAW_SHAPE,
        HOLE_POSITION_HOLE, HOLE_SAFE_ZONE,
        DRAW_TOOL_BOX,
        ERROR_MESSAGE
    }

    //    private static final Color CUT_COLOR = BACKGROUND_COLOR;
    private static final Color CUT_COLOR = Color.BLACK;
    private static final Color SHAPE_INTERIOR_COLOR = Color.web("FF8080");
    private static final Color CARVE_COLOR = Color.web("7999AC");
    private final Pane canvas = new Pane();
    private final Path pathThin = new Path();
    private final Path pathThick = new Path();
    private final Path initials = new Path();
    private final DrawToolBox drawToolBox = new DrawToolBox();
    private ObjectProperty<Drawing> drawing = new SimpleObjectProperty<>();
    private final DoubleProperty pxPerMm = new SimpleDoubleProperty(1);
    private final StackPane stackPane;
    private final Label title;
    private final Label errorMessage = new Label();
    private ObjectProperty<Path> outline = new SimpleObjectProperty<>();
    private ObjectProperty<Point2D> hole = new SimpleObjectProperty<>();
    private Group importedShapeInterior;
    private final Circle holeCircle;
    private final Circle holeSafeZone;
    private final Outliner outliner = new OutlinerEsri();
    private final StringProperty text = new SimpleStringProperty("");
    private double margin;
    private final ObjectProperty<DrawStep> drawStep = new SimpleObjectProperty<>(DrawStep.DrawShape);
    private final TextField textField = new TextField();
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
        canvas.setUserData(SortOrder.CANVAS.ordinal());

        textPane = new TextPane();
        textPane.setMouseTransparent(true);
        textPane.maxWidthProperty().bind(canvas.widthProperty());
        textPane.minWidthProperty().bind(canvas.widthProperty());
        textPane.setUserData(SortOrder.TEXT.ordinal());
        
        final int MAX_TEXT_LENGTH = 10;
        text.addListener(t -> {
            if (text.get().length() > MAX_TEXT_LENGTH) {
                textField.setText(text.get().substring(0, MAX_TEXT_LENGTH));
            }
            textPane.setText(text.get());
        });

        textField.setTextFormatter(new TextFormatter(new UnaryOperator<TextFormatter.Change>() {
            @Override
            public TextFormatter.Change apply(TextFormatter.Change t) {
                if (t.isContentChange()) {
                    String newText = t.getControlNewText();
                    int newLength = newText.length();
                    if (newLength > MAX_TEXT_LENGTH) {
                        int extraLength = newLength - MAX_TEXT_LENGTH;
                        int caretMove = t.getCaretPosition() - t.getControlCaretPosition();
                        int anchorMove = t.getAnchor() - t.getControlAnchor();
                        t.setText(t.getText().substring(0, t.getText().length() - extraLength));                    
                        t.selectRange(
                                t.getControlAnchor() + anchorMove - extraLength, 
                                t.getControlCaretPosition() + caretMove - extraLength);
                    }
                    return t;
                } else {
                    return null;
                }
            }
        }));
        textField.textProperty().bindBidirectional(text);
        textField.setMaxWidth(100);
        textField.setUserData(SortOrder.TEXT_FIELD.ordinal());

        stackPane.getChildren().addAll(textField, canvas, textPane);
        
        pathThick.setStrokeLineCap(StrokeLineCap.ROUND);
        pathThick.setStrokeLineJoin(StrokeLineJoin.ROUND);
        pathThick.setStroke(CUT_COLOR);
        pathThick.setManaged(false);
        pathThick.setMouseTransparent(true);
        pathThick.layoutXProperty().bind(canvas.layoutXProperty());
        pathThick.layoutYProperty().bind(canvas.layoutYProperty());
        pathThick.setUserData(SortOrder.THICK.ordinal());
        stackPane.getChildren().add(pathThick);

        initials.setStrokeLineCap(StrokeLineCap.ROUND);
        initials.setStrokeLineJoin(StrokeLineJoin.ROUND);
        initials.setStroke(CARVE_COLOR);
        initials.setManaged(false);
        initials.setMouseTransparent(true);
        initials.layoutXProperty().bind(canvas.layoutXProperty());
        initials.layoutYProperty().bind(canvas.layoutYProperty());
        initials.setUserData(SortOrder.INITIALS.ordinal());
        stackPane.getChildren().add(initials);

        pathThin.setStrokeLineCap(StrokeLineCap.ROUND);
        pathThin.setStrokeLineJoin(StrokeLineJoin.ROUND);
        pathThin.setStroke(SHAPE_INTERIOR_COLOR);
        pathThin.setManaged(false);
        pathThin.setMouseTransparent(true);
        pathThin.layoutXProperty().bind(canvas.layoutXProperty());
        pathThin.layoutYProperty().bind(canvas.layoutYProperty());
        pathThin.setUserData(SortOrder.THIN_DRAW_SHAPE.ordinal());
        stackPane.getChildren().add(pathThin);

        canvas.widthProperty().addListener(e -> {
            pxPerMm.set(canvas.getWidth() / MATERIAL_SIZE_X);
            pathThin.setStrokeWidth(MOTIF_WIDTH_MM * pxPerMm.get());
            pathThick.setStrokeWidth((MOTIF_WIDTH_MM + 2 * TOOL_DIAMETER) * pxPerMm.get());
            initials.setStrokeWidth(TOOL_DIAMETER * pxPerMm.get());
            textPane.setStrokeWidth(TOOL_DIAMETER * pxPerMm.get());
            textPane.setMaterialBounds(new BoundingBox(
                    MATERIAL_BASE_X * pxPerMm.get(),
                    MATERIAL_BASE_Y * pxPerMm.get(),
                    MATERIAL_SIZE_X * pxPerMm.get(),
                    MATERIAL_SIZE_Y * pxPerMm.get()));
            margin = MOTIF_WIDTH_MM * pxPerMm.get() / 2;
        });

        title = new Label("Draw");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(25));
        BorderPane.setAlignment(title, Pos.CENTER);

        setCenter(stackPane);
        setTop(title);

        holeCircle = new Circle();
        holeCircle.radiusProperty().bind(pxPerMm.multiply(HOLE_DIAMETER / 2));
        holeCircle.setFill(CUT_COLOR);
        holeCircle.setManaged(false);
        holeCircle.setMouseTransparent(true);
        holeCircle.layoutXProperty().bind(canvas.layoutXProperty());
        holeCircle.layoutYProperty().bind(canvas.layoutYProperty());
        holeCircle.setUserData(SortOrder.HOLE.ordinal());

        holeSafeZone = new Circle();
        holeSafeZone.radiusProperty().bind(pxPerMm.multiply((HOLE_DISTANCE_FROM_EDGE + HOLE_DIAMETER) / 2));
        holeSafeZone.setStroke(CARVE_COLOR);
        holeSafeZone.setFill(Color.TRANSPARENT);
        holeSafeZone.visibleProperty().bind(drawStep.isEqualTo(DrawStep.PositionHole));
        holeSafeZone.setManaged(false);
        holeSafeZone.setMouseTransparent(true);
        holeSafeZone.layoutXProperty().bind(canvas.layoutXProperty());
        holeSafeZone.layoutYProperty().bind(canvas.layoutYProperty());
        holeSafeZone.centerXProperty().bind(holeCircle.centerXProperty());
        holeSafeZone.centerYProperty().bind(holeCircle.centerYProperty());
        holeSafeZone.setUserData(SortOrder.HOLE_SAFE_ZONE.ordinal());

        errorMessage.setTextFill(Color.RED);
        errorMessage.setFont(Font.font(20));
        errorMessage.setManaged(false);
        errorMessage.setMouseTransparent(true);
        errorMessage.layoutXProperty().bind(canvas.layoutXProperty());
        errorMessage.layoutYProperty().bind(canvas.layoutYProperty());
        errorMessage.setAlignment(Pos.BOTTOM_CENTER);
        errorMessage.setPadding(new Insets(PADDING));
        errorMessage.setUserData(SortOrder.ERROR_MESSAGE.ordinal());
        
        drawToolBox.setManaged(false);
        drawToolBox.layoutXProperty().bind(canvas.layoutXProperty().subtract(
                drawToolBox.widthProperty()));
        drawToolBox.setUserData(SortOrder.DRAW_TOOL_BOX.ordinal());
        drawToolBox.shape.setOnAction(t -> drawShape());
        drawToolBox.initials.setOnAction(t -> drawInitials());
        drawToolBox.text.setOnAction(t -> typeText());
        drawToolBox.hole.setOnAction(t -> positionHole());
        drawToolBox.remove.disableProperty().bind(Bindings.createBooleanBinding(
                () -> {
                    switch (drawStep.get()) {
                        case DrawShape:
                            return outline.get() == null && errorMessage.getParent() == null;
                        case PositionHole:
                            return hole.get() == null;
                        case DrawInitials:
                            return initials.getElements().isEmpty();
                        case TypeText:
                            return Optional.ofNullable(text.get()).orElse("").isEmpty();
                    }
                    return false;
                }, drawStep, hole, outline, initials.getElements(), text, errorMessage.parentProperty()));
        stackPane.getChildren().add(drawToolBox);
        
        drawShape();
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        drawToolBox.resize(drawToolBox.prefWidth(stackPane.getHeight()), stackPane.getHeight());        
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


    public final void drawShape() {
        drawStep.set(DrawStep.DrawShape);
        drawToolBox.mode.selectToggle(drawToolBox.shape);
        reorderNodes();
        canvas.setOnMousePressed(e -> startDrawing(e));
        canvas.setOnMouseDragged(e -> continueDrawing(e));
        canvas.setOnMouseReleased(e -> stopDrawing(e));
        canvas.setOnTouchPressed(e -> startDrawing(e));
        canvas.setOnTouchMoved(e -> continueDrawing(e));
        canvas.setOnTouchReleased(e -> stopDrawing(e));
        canvas.requestFocus();
        title.setText("Draw shape");
        drawToolBox.remove.setOnAction(t -> resetShape());
    }

    public void positionHole() {
        if (NO_HOLE) {
            readyToCut();
            return;
        }
        drawStep.set(DrawStep.PositionHole);
        drawToolBox.mode.selectToggle(drawToolBox.hole);
        reorderNodes();
        canvas.setOnMousePressed(e -> positionHole(e));
        canvas.setOnMouseDragged(e -> positionHole(e));
        canvas.setOnMouseReleased(null);
        canvas.setOnTouchPressed(e -> positionHole(e));
        canvas.setOnTouchMoved(e -> positionHole(e));
        canvas.setOnTouchReleased(null);
        canvas.requestFocus();
        title.setText("Position badge holder hole");        
        drawToolBox.remove.setOnAction(t -> resetHole());
    }

    public void typeText() {
        if (NO_HOLE) {
            readyToCut();
            return;
        }
        canvas.setOnMousePressed(null);
        canvas.setOnMouseDragged(null);
        canvas.setOnMouseReleased(null);
        canvas.setOnTouchPressed(null);
        canvas.setOnTouchMoved(null);
        canvas.setOnTouchReleased(null);
        drawStep.set(DrawStep.TypeText);
        drawToolBox.mode.selectToggle(drawToolBox.text);
        reorderNodes();
        title.setText("Type text");
        drawToolBox.remove.setOnAction(t -> resetText());
        textField.requestFocus();
    }

    private void drawInitials() {
        if (NO_HOLE) {
            readyToCut();
            return;
        }
        drawStep.set(DrawStep.DrawInitials);
        drawToolBox.mode.selectToggle(drawToolBox.initials);
        reorderNodes();
        canvas.setOnMousePressed(e -> startDrawingInitials(e));
        canvas.setOnMouseDragged(e -> continueDrawingInitials(e));
        canvas.setOnMouseReleased(e -> continueDrawingInitials(e));
        canvas.setOnTouchPressed(e -> startDrawingInitials(e));
        canvas.setOnTouchMoved(e -> continueDrawingInitials(e));
        canvas.setOnTouchReleased(e -> continueDrawingInitials(e));
        title.setText("Draw your initials");
        drawToolBox.remove.setOnAction(t -> undoInitials());
        canvas.requestFocus();
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
        resetShape();
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
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        positionHole(x, y);
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
            reorderNodes();
        }
    }

    private void startDrawingInitials(InputEvent e) {
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        ObservableList<PathElement> ie = initials.getElements();
        if (!ie.isEmpty() && ie.get(ie.size() - 1) instanceof MoveTo) {
            ie.remove(ie.size() - 1);
            System.err.println("Removing MoveTo without following LineTo");
        }
        ie.add(new MoveTo(clampX(x), clampY(y)));
    }

    private void continueDrawingInitials(InputEvent e) {
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        ObservableList<PathElement> ie = initials.getElements();
        if (ie.isEmpty()) {
            System.err.println("Skipping adding LineTo to empty initials");
            return;
        }
        LineTo lineTo = new LineTo(clampX(x), clampY(y));
        PathElement prevElement = ie.get(ie.size() - 1);
        if (!lineToEquals(lineTo, prevElement)) {
            ie.add(lineTo);
        }
    }
    
    private final static boolean lineToEquals(LineTo lineTo, Object obj) {
        if (!(obj instanceof LineTo)) {
            return false;
        }
        LineTo aLineTo = (LineTo) obj;
        return lineTo.getX() == aLineTo.getX() && lineTo.getY() == aLineTo.getY();
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
        outlinePath.setUserData(SortOrder.OUTLINE.ordinal());
        outline.set(outlinePath);

        importedShapeInterior = new Group(svgPath);
        svgPath.setFill(SHAPE_INTERIOR_COLOR);
        importedShapeInterior.setMouseTransparent(true);
        importedShapeInterior.setManaged(false);
        importedShapeInterior.layoutXProperty().bind(canvas.layoutXProperty());
        importedShapeInterior.layoutYProperty().bind(canvas.layoutYProperty());
        importedShapeInterior.getTransforms().addAll(
                new Translate(0, -Configuration.MATERIAL_SIZE_Y),
                new Scale(pxPerMm.get(), -pxPerMm.get(), 0, MATERIAL_SIZE_Y));
        importedShapeInterior.setUserData(SortOrder.INTERIOR.ordinal());
        stackPane.getChildren().addAll(importedShapeInterior, outlinePath);

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
    
    private void resetText() {
        text.set("");
        textField.requestFocus();
    }

    private void resetShape() {
        if (outline.get() != null) {
            stackPane.getChildren().remove(outline.get());
            outline.set(null);
        }
        if (importedShapeInterior != null) {
            stackPane.getChildren().remove(importedShapeInterior);
            importedShapeInterior = null;
        }
        pathThin.getElements().clear();
        pathThick.getElements().clear();
        stackPane.getChildren().removeAll(errorMessage);
    }
    
    private void resetHole() {
        stackPane.getChildren().removeAll(holeCircle, holeSafeZone);
        hole.set(null);
    }
    
    public void reset() {
        resetShape();
        resetHole();
        resetText();
        resetInitials();
    }

    private void resetInitials() {
        initials.getElements().clear();
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
            outlinePath.setMouseTransparent(true);
            outlinePath.setManaged(false);
            outlinePath.layoutXProperty().bind(canvas.layoutXProperty());
            outlinePath.layoutYProperty().bind(canvas.layoutYProperty());
            outlinePath.setUserData(SortOrder.OUTLINE.ordinal());
            outline.set(outlinePath);
            stackPane.getChildren().add(outlinePath);
            reorderNodes();
        }
    }
    
    private void reorderNodes() {
        pathThin.setUserData(drawStep.get() == DrawStep.DrawShape 
                ? SortOrder.THIN_DRAW_SHAPE.ordinal()
                : SortOrder.THIN.ordinal());
        holeCircle.setUserData(drawStep.get() == DrawStep.PositionHole 
                ? SortOrder.HOLE_POSITION_HOLE.ordinal()
                : SortOrder.HOLE.ordinal());
        stackPane.getChildren().setAll(stackPane.getChildren().stream().sorted(new Comparator<Node>() {
            
            @Override
            public int compare(Node o1, Node o2) {
                return (int) o1.getUserData() - (int) o2.getUserData();
            }
        }).collect(Collectors.toList()));
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
        if (!stackPane.getChildren().contains(errorMessage)) {
            stackPane.getChildren().add(errorMessage);
            reorderNodes();
        }
    }

    public ObjectProperty<Point2D> holeProperty() {
        return hole;
    }

    public ObjectProperty<Path> outlineProperty() {
        return outline;
    }

    public Path getInitials() {
        Path textInitials = textPane.getAll();
        Bounds b = textInitials.getBoundsInLocal();
        double dx = b.getMinX() + b.getWidth() / 2 - canvas.getWidth() / 2;
        double dy = b.getMinY() + b.getHeight()/ 2 - canvas.getHeight() / 2;
        if (!ENABLE_TEXT) {
            textInitials = new Path();
        }
        return new Path(Stream.concat(initials.getElements().stream().map(pathElement -> {
            if (pathElement instanceof MoveTo) {
                MoveTo mt = (MoveTo) pathElement;
                return new MoveTo(convertX(mt.getX()), convertY(mt.getY()));
            } else if (pathElement instanceof LineTo) {
                LineTo mt = (LineTo) pathElement;
                return new LineTo(convertX(mt.getX()), convertY(mt.getY()));
            } else {
                throw new IllegalStateException("Unexpected path element: " + pathElement);
            }
        }), textInitials.getElements().stream().map(pathElement -> {
            if (pathElement instanceof MoveTo) {
                MoveTo mt = (MoveTo) pathElement;
                return new MoveTo(convertX(mt.getX() - dx), convertY(mt.getY() - dy));
            } else if (pathElement instanceof LineTo) {
                LineTo mt = (LineTo) pathElement;
                return new LineTo(convertX(mt.getX() - dx), convertY(mt.getY() - dy));
            } else if (pathElement instanceof ClosePath) {
                return pathElement;
            } else {
                throw new IllegalStateException("Unexpected path element: " + pathElement);
            }
        })).collect(Collectors.toList()));
    }
}
