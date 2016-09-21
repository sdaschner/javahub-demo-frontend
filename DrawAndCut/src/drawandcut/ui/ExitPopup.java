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

import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.stage.Popup;
import javafx.stage.PopupWindow;

/**
 *
 * @author akouznet
 */
public class ExitPopup extends Popup {
    
    public final Button reboot = new Button("Reboot");
    public final Button poweroff = new Button("Poweroff");
    public final Button restart = new Button("Restart");
    public final Button exit = new Button("Exit");

    public ExitPopup() {
        reboot.setId("reboot");
        poweroff.setId("poweroff");
        restart.setId("restart");
        exit.setId("exit");
        HBox hbox = new HBox(exit, restart, reboot, poweroff);
        hbox.setId("exitPopup");
        getContent().add(hbox);
        setAutoFix(true);
        setAutoHide(true);
//        setAnchorLocation(PopupWindow.AnchorLocation.WINDOW_BOTTOM_LEFT);
    }
    
}
