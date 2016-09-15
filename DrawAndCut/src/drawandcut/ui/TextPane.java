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
import drawandcut.util.LineSegment;
import drawandcut.util.Text2DHelper;
import javafx.geometry.BoundingBox;
import javafx.geometry.Bounds;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Path;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;

/**
 * @author Heinz Kabutz
 */
public class TextPane extends BorderPane {
    private final Path all = new Path();
    private final String fontFace;
    private final int fontSize;
    private String text;

    public TextPane() {
        this.fontFace = Configuration.FONT_FACE;
        this.fontSize = Configuration.FONT_SIZE;

        all.setStroke(Color.web("7999AC"));
        all.setStrokeLineCap(StrokeLineCap.ROUND);
        all.setStrokeLineJoin(StrokeLineJoin.ROUND);

        setCenter(all);
    }

    public void setStrokeWidth(double lineWidth) {
        if (originalFontLineWidth == 0.0) {
            // first time we are setting the font line width.  This will be our
            // set ratio for the program to be used in determining the future
            // font sizes
            fontLineScaleRatio = 1.0;
            originalFontLineWidth = lineWidth;
        } else {
            fontLineScaleRatio = lineWidth / originalFontLineWidth;
        }
        all.setStrokeWidth(lineWidth);
        refresh();
    }

    private void refresh() {
        setText(this.text);
    }
    
    private Bounds materialBounds = new BoundingBox(0, 0, 200, 200);

    public void setMaterialBounds(Bounds materialBounds) {
        this.materialBounds = materialBounds;
    }
    
    public void setText(String text) {
        this.text = text;
        all.getElements().clear();
        for (LineSegment lineSegment : new Text2DHelper(text, fontFace, getScaledFontSize(), materialBounds.getWidth(), materialBounds.getHeight()).getLineSegment()) {
            Path path = lineSegment.getPath();
            all.getElements().addAll(path.getElements());
            for (LineSegment segment : lineSegment.getHoles()) {
                Path holePath = segment.getPath();
                all.getElements().addAll(holePath.getElements());
            }
        }
    }

    private double getScaledFontSize() {
        return fontSize * fontLineScaleRatio;
    }

    private double fontLineScaleRatio = 1.0;
    private double originalFontLineWidth = 0.0;

    public Path getAll() {
        return all;
    }
}
