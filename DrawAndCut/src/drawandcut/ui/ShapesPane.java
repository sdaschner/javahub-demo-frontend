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

import static drawandcut.Configuration.PADDING;
import drawandcut.Shapes;
import java.util.function.Consumer;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.VPos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.Font;

/**
 *
 * @author akouznet
 */
public class ShapesPane extends BorderPane {
    
    private Consumer<String> onAction;

    public ShapesPane(Shapes shapes) {        
        FlowPane flowPane = new FlowPane();
        flowPane.setId("flowPane");
        flowPane.setVgap(PADDING);
        flowPane.setHgap(PADDING);
        flowPane.setAlignment(Pos.CENTER);
        flowPane.setColumnHalignment(HPos.LEFT);
        flowPane.setRowValignment(VPos.CENTER);
        flowPane.setMaxWidth(USE_PREF_SIZE);
        flowPane.setPadding(new Insets(0, 50, 0, 50));
        shapes.get().forEach((key, shape) -> {
            SVGPath svgPath = new SVGPath();
            svgPath.getStyleClass().add("svg-path");
            svgPath.setContent(shape);
            svgPath.setFillRule(FillRule.EVEN_ODD);
            ScalableNodePane scalableNodePane = new ScalableNodePane(svgPath);
            scalableNodePane.setMinSize(100, 100);
            scalableNodePane.setPrefSize(100, 100);
            Button button = new Button(key, scalableNodePane);
            button.setOnAction(t -> {
                if (onAction != null) {
                    onAction.accept(key);
                }
            });
            flowPane.getChildren().add(button);
        });
        
        Label title = new Label("Load model");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(25));
        BorderPane.setAlignment(title, Pos.CENTER);
        
        setId("shapesPane");
        setPadding(new Insets(PADDING));        
        setTop(title);
        setCenter(flowPane);
    }

    public void setOnAction(Consumer<String> onAction) {
        this.onAction = onAction;
    }
    
}
