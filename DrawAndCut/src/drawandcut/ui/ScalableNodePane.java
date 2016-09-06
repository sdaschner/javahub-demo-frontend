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

import javafx.geometry.Bounds;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.transform.Scale;

/**
 *
 * @author akouznet
 */
public class ScalableNodePane extends Pane {
    private final Node node;    
    private final Scale scale = new Scale();

    public ScalableNodePane(Node node) {
        super(node);
        this.node = node;
        this.node.setManaged(false);
        this.node.getTransforms().setAll(scale);
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        double w = getWidth(), h = getHeight();
        Bounds nodeBounds = node.getBoundsInLocal();
        if (nodeBounds.getWidth() == 0 || nodeBounds.getHeight() == 0) {
            return;
        }
        double ratio = Math.min(w / nodeBounds.getWidth(), h / nodeBounds.getHeight());
        scale.setX(ratio);
        scale.setY(ratio);
        scale.setZ(ratio);
        double  x = (w - nodeBounds.getWidth() * ratio) / 2 - nodeBounds.getMinX() * ratio,
                y = (h - nodeBounds.getHeight() * ratio) / 2 - nodeBounds.getMinY() * ratio;
        this.node.setLayoutX(x);
        this.node.setLayoutY(y);
    }
}
