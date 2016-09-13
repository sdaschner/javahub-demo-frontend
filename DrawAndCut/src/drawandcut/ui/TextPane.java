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

    public TextPane(double lineWidth) {
        this.fontFace = Configuration.FONT_FACE;
        this.fontSize = Configuration.FONT_SIZE;

        setLineWidth(lineWidth);
        all.setStroke(Color.GRAY);
        all.setStrokeLineCap(StrokeLineCap.ROUND);
        all.setStrokeLineJoin(StrokeLineJoin.ROUND);

        setCenter(all);
    }
    
    public void setLineWidth(double lineWidth) {
        all.setStrokeWidth(lineWidth);
    }

    public void setText(String text) {
        all.getElements().clear();
        for (LineSegment lineSegment : new Text2DHelper(text, fontFace, fontSize).getLineSegment()) {
            Path path = lineSegment.getPath();
            all.getElements().addAll(path.getElements());
            for (LineSegment segment : lineSegment.getHoles()) {
                Path holePath = segment.getPath();
                all.getElements().addAll(holePath.getElements());
            }
        }
    }

    public void setFontLineWidth(double fontLineWidth) {
        all.setStrokeWidth(fontLineWidth);
    }

    public Path getAll() {
        return all;
    }
}
