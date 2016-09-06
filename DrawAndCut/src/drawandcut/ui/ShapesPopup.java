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

import drawandcut.Shapes;
import java.util.function.Consumer;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.scene.shape.FillRule;
import javafx.scene.shape.SVGPath;
import javafx.scene.text.TextAlignment;
import javafx.stage.Popup;

/**
 *
 * @author akouznet
 */
public class ShapesPopup extends Popup {
    
    private Consumer<String> onAction;

    public ShapesPopup(Shapes shapes) {        
        FlowPane flowPane = new FlowPane();
        getContent().add(flowPane);
        shapes.get().forEach((key, shape) -> {
            SVGPath svgPath = new SVGPath();
            svgPath.setContent(shape);
            svgPath.setFillRule(FillRule.EVEN_ODD);
            ScalableNodePane scalableNodePane = new ScalableNodePane(svgPath);
            scalableNodePane.setMinSize(100, 100);
            scalableNodePane.setPrefSize(100, 100);
            Button button = new Button(key, scalableNodePane);
            button.setGraphicTextGap(5);
            button.setContentDisplay(ContentDisplay.TOP);
            button.setOnAction(t -> {
                if (onAction != null) {
                    onAction.accept(key);
                }
                hide();
            });
//            button.setMaxSize(100, 100);
            flowPane.getChildren().add(button);
        });
    }

    public void setOnAction(Consumer<String> onAction) {
        this.onAction = onAction;
    }
    
}
