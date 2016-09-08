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

import static drawandcut.Configuration.DISABLE_CUTTER;
import static drawandcut.Configuration.DOC;
import static drawandcut.Configuration.PLUNGE_FEED;
import drawandcut.cutter.CutterConnection;
import drawandcut.gcode.PathConverter;
import drawandcut.ui.ControlPane;
import drawandcut.ui.DrawPane;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import static drawandcut.Configuration.FEED;
import static drawandcut.Configuration.RPM;
import drawandcut.ui.ScannerPane;
import drawandcut.ui.ShapesPopup;
import javafx.geometry.Bounds;
import javafx.scene.paint.Color;

/**
 *
 * @author akouznet
 */
public class DrawAndCut extends Application {
    private CutterConnection cutterConnection;
    private final Shapes shapes = new Shapes();
    private Scene drawScene;

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
        ShapesPopup shapesPopup = new ShapesPopup(shapes);
        shapesPopup.setOnAction(key -> drawPane.importSVG(shapes.get().get(key)));
        controlPane.loadButton().setOnAction(t -> {
            if (shapesPopup.isShowing()) {
                shapesPopup.hide();
            } else {
            Bounds b = controlPane.loadButton().getBoundsInParent();
                shapesPopup.setAutoHide(true);
            shapesPopup.show(primaryStage, b.getMaxX(), b.getMinY());
            }
        });
        controlPane.printButton().disableProperty().bind(drawPane.drawingProperty().isNull());
        controlPane.printButton().setOnAction(t -> {
            DrawPane.Drawing drawing = drawPane.drawingProperty().get();
            if (drawing == null) {
                return;
            }
            List<String> output = new PathConverter(drawing.getOutline(), RPM, FEED, DOC, PLUNGE_FEED).getOutput();
            System.out.println("Program:");
            for(String line : output) {
                System.out.println(line);
            }
            try {
                Files.write(new File("output.nc").toPath(), output);
            } catch (IOException ex) {
                Logger.getLogger(DrawAndCut.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            cutterConnection.getCutter().sendSequence(output.toArray(new String[output.size()]));
        });
        
        BorderPane borderPane = new BorderPane(drawPane);
        borderPane.setLeft(controlPane);
        
        primaryStage.setTitle("JavaOne2016 - Draw and Cut demo");
        drawScene = new Scene(borderPane);
        ScannerPane scannerPane = new ScannerPane();
        Scene scannerScene = new Scene(scannerPane, Color.RED);
        
        controlPane.scanButton().setDisable(Configuration.DISABLE_CAMERA);
        controlPane.scanButton().setOnAction(t -> {
            primaryStage.setScene(scannerScene);
            scannerPane.start();
        });
        scannerPane.setOnRead(code -> {
            String svg = shapes.get().get(code);
            if (svg != null) {
                drawPane.importSVG(svg);
            }
            primaryStage.setScene(drawScene);
        });

        primaryStage.setScene(drawScene);
        primaryStage.show();
        primaryStage.setMaximized(true);
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        
        cutterConnection = new CutterConnection();
        if (!DISABLE_CUTTER) {
            cutterConnection.connectToCutter();
        }
        
//        Path path = new Path(new MoveTo(0, 0), new LineTo(100, 0), new LineTo(0, 50), new ClosePath());
//        Outliner outliner = new Outliner(path);
//        Path outline = outliner.generateOutline();
    }
    
}
