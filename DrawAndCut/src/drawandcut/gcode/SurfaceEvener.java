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
package drawandcut.gcode;

import drawandcut.Configuration;
import static drawandcut.Configuration.*;
import java.util.List;

/**
 *
 * @author akouznet
 */
public class SurfaceEvener {
    
    private final GCodeGenerator gcg = new GCodeGenerator();
    private final double plungeFeed;
    private final double feed;
    
    public SurfaceEvener(int rpm, double feed, double plungeFeed) {
        this.feed = feed;
        this.plungeFeed = plungeFeed;
        gcg.init(rpm);
        evenSurface();
        gcg.spindleStop();
        gcg.goHome();
        gcg.programEnd();
    }

    private void evenSurface() {
        gcg.rapid(0, 0, gcg.getSafeZ());
        gcg.linearZF(gcg.getBottomZ(), plungeFeed);
        gcg.setFeed(feed);
        double yMin = 0;
        double yMax = MATERIAL_SIZE_Y + TOOL_DIAMETER * 2;
        double yStep = TOOL_DIAMETER * 0.9;
        double xLeft = 0;
        double xRight = MATERIAL_SIZE_X + TOOL_DIAMETER;
        int direction = 1;
        for (double y = yMin; y < yMax; y += yStep) {
            gcg.linear(direction > 0 ? xRight : xLeft, y);
            gcg.linear(direction > 0 ? xRight : xLeft, y + yStep);
            direction = -direction;
        }
        gcg.rapidZ(gcg.getSafeZ());
    }
    
    public List<String> getOutput() {
        return gcg.getOutput();
    }
    
}
