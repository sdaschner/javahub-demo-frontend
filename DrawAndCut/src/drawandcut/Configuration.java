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
package drawandcut;

/**
 *
 * @author akouznet
 */
public class Configuration {
    public static final double IN = 25.4; // mm
    
    public static final double MATERIAL_SIZE_X = 198; // mm
    public static final double MATERIAL_SIZE_Y = 178; // mm
    public static final double MATERIAL_SIZE_Z = 1/8. * IN; // mm
    public static final double MATERIAL_SIZE_RATIO = MATERIAL_SIZE_X / MATERIAL_SIZE_Y;
    
    public static final double MATERIAL_BASE_X = 0; // mm
    public static final double MATERIAL_BASE_Y = 0; // mm
    public static final double MATERIAL_BASE_Z = 0; // mm
            
    public static final double TOOL_DIAMETER = 1/8. * IN; // mm
    
    public static final double LINE_WIDTH_MM = 4 * TOOL_DIAMETER; // mm
    
    public static final int TARGET_RPM = 9000; // rpm
    public static final double TARGET_FEED = 44 * IN; // mmpm
    public static final double PLUNGE_FEED = 14 * IN; // mmpm
    public static final double DOC = 0.019 * IN; // mm
    
    public static final double PROBING_OFFSET = 0.81; // mm (delta between probing Z and coordinate 0)
}
