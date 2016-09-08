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
import javafx.scene.layout.GridPane;

/**
 *
 * @author akouznet
 */
public class ControlPane extends GridPane {
    
    private final double BUTTON_PREF_SIZE = 100;
    private final double PADDING = 8;

    private final Button scan = new Button("Scan");
    private final Button print = new Button("Print");
    private final Button load = new Button("Load");

    public ControlPane() {
        load.setOnAction(e -> load());
        load.setPrefSize(BUTTON_PREF_SIZE, BUTTON_PREF_SIZE);
        
        print.setOnAction(e -> print());
        print.setPrefSize(BUTTON_PREF_SIZE, BUTTON_PREF_SIZE);
        
        scan.setOnAction(e -> scan());
        scan.setPrefSize(BUTTON_PREF_SIZE, BUTTON_PREF_SIZE);
        
        setPadding(new Insets(PADDING));
        setAlignment(Pos.CENTER);
        addRow(0, scan);
        addRow(1, load);
        addRow(2, print);
    }
    
    public void scan() {
        System.out.println("Scan");
    }

    public void print() {
        System.out.println("Print");
    }

    public void load() {
        System.out.println("Load");
    }

    public Button printButton() {
        return print;
    }

    public Button loadButton() {
        return load;
    }
    
    public Button scanButton() {
        return scan;
    }
    
}
