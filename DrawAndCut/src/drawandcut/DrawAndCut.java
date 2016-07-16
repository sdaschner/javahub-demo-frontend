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

import static drawandcut.Configuration.DOC;
import static drawandcut.Configuration.PLUNGE_FEED;
import drawandcut.cutter.CutterConnection;
import static drawandcut.Configuration.TARGET_FEED;
import static drawandcut.Configuration.TARGET_RPM;
import drawandcut.gcode.PathConverter;
import drawandcut.path.Outliner;
import drawandcut.ui.ControlPane;
import drawandcut.ui.DrawPane;
import java.util.List;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.shape.ClosePath;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.stage.Stage;

/**
 *
 * @author akouznet
 */
public class DrawAndCut extends Application {
    private CutterConnection cutterConnection;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        DrawPane drawPane = new DrawPane();
        ControlPane controlPane = new ControlPane();
        controlPane.printButton().disableProperty().bind(drawPane.drawingProperty().isNull());
        controlPane.printButton().setOnAction(t -> {
            DrawPane.Drawing drawing = drawPane.drawingProperty().get();
            if (drawing == null) {
                return;
            }
            List<String> output = new PathConverter(drawing.getPath(), TARGET_RPM, TARGET_FEED, DOC, PLUNGE_FEED).getOutput();
            System.out.println("Program:");
            for(String line : output) {
                System.out.println(line);
            }
            cutterConnection.getCutter().sendSequence(output.toArray(new String[output.size()]));
        });
        
        BorderPane borderPane = new BorderPane(drawPane);
        borderPane.setLeft(controlPane);
        
        primaryStage.setTitle("JavaOne2016 - Draw and Cut demo");
        primaryStage.setScene(new Scene(borderPane));
        primaryStage.show();
        primaryStage.setMaximized(true);
        
        cutterConnection = new CutterConnection();        
//        cutterConnection.connectToCutter();

//        Path path = new Path(new MoveTo(0, 0), new LineTo(100, 0), new LineTo(0, 50), new ClosePath());
//        Outliner outliner = new Outliner(path);
//        Path outline = outliner.generateOutline();
    }
    
}
