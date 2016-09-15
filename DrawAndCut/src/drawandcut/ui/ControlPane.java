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

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import static drawandcut.Configuration.*;

/**
 * @author akouznet
 */
public class ControlPane extends GridPane {
    private final ToggleButton scan = new ToggleButton("Scan");
    private final ToggleButton draw = new ToggleButton("Draw");
    private final TextField initials = new TextField();

    private final Button cut = new Button("Cut");
    private final ToggleButton load = new ToggleButton("Load");
    private final ToggleButton exit = new ToggleButton("Exit");
    private final Button even = new Button("Make surface even");

    public ControlPane() {
        scan.setId("scan");
        draw.setId("draw");
        load.setId("load");
        cut.setId("cut");
        exit.setId("exit");
        even.setId("even");

        setId("controlPane");
        setPadding(new Insets(PADDING));
        setVgap(PADDING);
        setAlignment(Pos.CENTER);
        
        VBox initialPane = new VBox();
        initialPane.setAlignment(Pos.CENTER);
        initialPane.getChildren().addAll(initials, new Label("Text"));
        
        int rowIndex = 0;
        addRow(rowIndex++, scan);
        addRow(rowIndex++, draw);
        if (ENABLE_TEXT) {
            addRow(rowIndex++, initialPane);
        }
        addRow(rowIndex++, load);
        addRow(rowIndex++, cut);

        if (ENABLE_EVENER) {
            addRow(rowIndex++, even);
        }
        addRow(rowIndex++, exit);
    }

    public Button cutButton() {
        return cut;
    }

    public ToggleButton loadButton() {
        return load;
    }

    public ToggleButton scanButton() {
        return scan;
    }

    public ToggleButton drawButton() {
        return draw;
    }
    
    public ToggleButton exitButton() {
        return exit;
    }

    public Button evenButton() {
        return even;
    }

    public TextField initialsField() {
        return initials;
    }
}
