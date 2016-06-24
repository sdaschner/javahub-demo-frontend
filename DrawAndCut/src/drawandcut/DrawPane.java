package drawandcut;

import static drawandcut.Configuration.LINE_WIDTH;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
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
    private Drawing drawing;

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
        drawing = new Drawing(x, y);
    }
    
    private void continueDrawing(InputEvent e) {
        if (drawing == null) {
            return;
        }
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        drawing.continueTo(x, y);
        canvas.getGraphicsContext2D().lineTo(x, y);
    }
    
    private void stopDrawing(InputEvent e) {
        if (drawing == null) {
            return;
        }
        double x = getX(e), y = getY(e);
        if (Double.isNaN(x) || Double.isNaN(y)) {
            return;
        }
        drawing.stop(x, y);
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
    
}
