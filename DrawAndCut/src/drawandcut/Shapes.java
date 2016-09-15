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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author akouznet
 */
public class Shapes {
    
    private final Map<String, Shape> shapes = new HashMap<>();
    private final Map<String, Shape> unmodifiableMap = Collections.unmodifiableMap(shapes);

    public Shapes() {
        load();
    }
    
    private void load() {
        try (InputStream resourceAsStream
                    = Shapes.class.getResourceAsStream("shapes.properties")) {
            Properties props = new Properties();
            props.load(resourceAsStream);
            props.keySet().forEach(k -> {
                String key = (String) k;
                double size = 200;
                switch (key) {
                    case "child":
                        size = 138;
                        break;
                    case "bird":
                        shapes.put("big bird", new Shape((String) props.get(key), size));
                        shapes.put("small bird", new Shape((String) props.get(key), 140));
                        return;
                    case "key":
                        size = 157;
                        break;
                }
                shapes.put(key, new Shape((String) props.get(key), size));
            });
        } catch (IOException ex) {
            Logger.getLogger(DrawAndCut.class.getName())
                    .log(Level.SEVERE, null, ex);            
        }
    }

    public Map<String, Shape> get() {
        return unmodifiableMap;
    }
    
    public static class Shape {
        private final String svg;
        private final double size;

        public Shape(String svg, double size) {
            this.svg = svg;
            this.size = size;
        }

        public double getSize() {
            return size;
    }

        public String getSvg() {
            return svg;
        }
    }
}
