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
package drawandcut.cutter;

import drawandcut.Configuration;
import com.willwinder.universalgcodesender.GrblController;

/**
 *
 * @author akouznet
 */
public class CutterConnection {
    
    private Cutter cutter;
    
    public void connectToCutter() {
        cutter = new Cutter(this::toConnect);
        cutter.connect();
    }
    
    private void toConnect() {
        try {
            GrblController grblController = new GrblController();
            cutter.bindToController(grblController);
            Boolean openCommPort = grblController.openCommPort(Configuration.PORT_NAME, 115200);
            if (openCommPort != true) {
                throw new IllegalStateException("Connection to cutter failed to open. "
                        + "Use -DdisableCutter=true to run without cutter. "
                        + "Use -DportName=/dev/ttyACM0 or -DportName=COM3 to specify port name.");
            }
        } catch (Exception ex) {
            throw new IllegalStateException("Cannot connect to the cutter. "
                    + "Use -DdisableCutter=true to run without cutter. "
                    + "Use -DportName=/dev/ttyACM0 or -DportName=COM3 to specify port name.", ex);
        }
    }

    public Cutter getCutter() {
        return cutter;
    }
}
