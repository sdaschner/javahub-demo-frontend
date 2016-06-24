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
package drawandcut;

import static drawandcut.Configuration.LINE_WIDTH;
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

/**
 *
 * @author akouznet
 */
public class DrawPane extends StackPane {
    
    private final Pane canvasBackground = new Pane();
    private final Canvas canvas;
    private ObjectProperty<Drawing> drawing = new SimpleObjectProperty<>();

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
            canvas.getGraphicsContext2D().setLineWidth(LINE_WIDTH * canvas.getWidth() / Configuration.MATERIAL_SIZE_X);
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
        canvas.getGraphicsContext2D().lineTo(x, y);
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
    
    private class Drawing {
        private Path p = new Path();

        public Drawing(double x, double y) {
            p.getElements().add(new MoveTo(x, y));
            canvas.getGraphicsContext2D().clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
            canvas.getGraphicsContext2D().beginPath();
            canvas.getGraphicsContext2D().moveTo(x, y);
        }
        
        public void continueTo(double x, double y) {
            p.getElements().add(new LineTo(x, y));
            canvas.getGraphicsContext2D().lineTo(x, y);
            canvas.getGraphicsContext2D().stroke();
        }
        
        public void stop(double x, double y) {
            p.getElements().add(new LineTo(x, y));
            p.getElements().add(new ClosePath());
            canvas.getGraphicsContext2D().lineTo(x, y);
            canvas.getGraphicsContext2D().closePath();
            canvas.getGraphicsContext2D().stroke();
        }
    }

    public ObjectProperty<Drawing> drawingProperty() {
        return drawing;
    }
    
}
