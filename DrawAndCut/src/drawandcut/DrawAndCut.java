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

import static drawandcut.Configuration.*;
import drawandcut.cutter.Cutter;
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
import drawandcut.ui.ScannerPane;
import drawandcut.ui.ShapesPane;
import javafx.beans.binding.Bindings;
import javafx.geometry.Insets;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;

/**
 *
 * @author akouznet
 */
public class DrawAndCut extends Application {
    private CutterConnection cutterConnection;
    private final Shapes shapes = new Shapes();
    private Scene drawScene;
    private ControlPane controlPane;
    private ScannerPane scannerPane;
    private ShapesPane shapesPane;
    private DrawPane drawPane;
    private BorderPane borderPane;

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {

        cutterConnection = new CutterConnection();
        if (!DISABLE_CUTTER) {
            try {
                cutterConnection.connectToCutter();
            } catch (Throwable t) {
                t.printStackTrace();
                System.exit(-1);
            }                
        }
        
        borderPane = new BorderPane();
        borderPane.setBackground(Background.EMPTY);
        borderPane.setPadding(new Insets(
                SCREEN_PADDING_TOP, 
                SCREEN_PADDING_RIGHT, 
                SCREEN_PADDING_BOTTOM, 
                SCREEN_PADDING_LEFT));
        
        scannerPane = new ScannerPane();
        scannerPane.setPadding(new Insets(PADDING));
        
        drawPane = new DrawPane();
        drawPane.setFocusTraversable(true);
        drawPane.holeProperty().addListener(o -> {
            if (drawPane.holeProperty().get() != null) {
                controlPane.drawButton().setSelected(false);
            }
        });
        drawPane.setPadding(new Insets(PADDING));
        borderPane.setCenter(drawPane);
        
        shapesPane = new ShapesPane(shapes);
        shapesPane.setOnAction(key -> {
            drawPane.importSVG(shapes.get().get(key));
            showDrawPane();
        });
        
        controlPane = new ControlPane();
        borderPane.setLeft(controlPane);
        
        ToggleGroup tg = new ToggleGroup();
        tg.getToggles().addAll(
                controlPane.drawButton(), 
                controlPane.loadButton(), 
                controlPane.scanButton());
        
        controlPane.loadButton().setOnAction(t -> showLoadPane());
        controlPane.cutButton().disableProperty().bind(
                drawPane.outlineProperty().isNull()
                        .or(drawPane.holeProperty().isNull())
                        .or(borderPane.centerProperty().isEqualTo(scannerPane))
                        .or(DISABLE_CUTTER 
                                ? Bindings.createBooleanBinding(() -> false) 
                                : cutterConnection.getCutter().ready().not()));
        controlPane.cutButton().setOnAction(t -> {
            List<String> output = new PathConverter(
                    drawPane.outlineProperty().get(), 
                    drawPane.holeProperty().get(), 
                    RPM, FEED, DOC, PLUNGE_FEED).getOutput();
//            System.out.println("Program:");
//            for(String line : output) {
//                System.out.println(line);
//            }
            try {
                Files.write(new File("output.nc").toPath(), output);
            } catch (IOException ex) {
                Logger.getLogger(DrawAndCut.class.getName())
                        .log(Level.SEVERE, null, ex);
            }
            Cutter cutter = cutterConnection.getCutter();
            if (cutter != null) {
                cutter.sendSequence(output.toArray(new String[output.size()]));
            }
        });
        controlPane.exitButton().setOnAction(t -> System.exit(0));
        
        primaryStage.setTitle("JavaOne2016 - Draw and Cut demo");
        drawScene = new Scene(borderPane, SCREEN_WIDTH, SCREEN_HEIGHT, Color.BLACK);
        
//        controlPane.scanButton().setDisable(Configuration.DISABLE_CAMERA);
        controlPane.scanButton().setOnAction(t -> {
            if (borderPane.getCenter() == scannerPane) {
                showDrawPane();
            } else {
                showScannerPane();
            }
        });
        controlPane.drawButton().setOnAction(t -> {
            showDrawPane();
            drawPane.drawShape();
            controlPane.drawButton().setSelected(true);
        });
        controlPane.drawButton().setSelected(true);
        
        scannerPane.setOnRead(code -> {
            String svg = shapes.get().get(code);
            if (svg != null) {
                drawPane.importSVG(svg);
            }
            showDrawPane();
        });

        primaryStage.setScene(drawScene);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> System.exit(0));
        drawScene.getStylesheets().add(
                DrawAndCut.class.getResource("styles.css").toExternalForm());
        
//        Path path = new Path(new MoveTo(0, 0), new LineTo(100, 0), new LineTo(0, 50), new ClosePath());
//        Outliner outliner = new Outliner(path);
//        Path outline = outliner.generateOutline();
    }
    
    private void showDrawPane() {
        scannerPane.stop();
        borderPane.setCenter(drawPane);
        drawPane.requestFocus();
        controlPane.drawButton().setSelected(true);
    }
    
    private void showScannerPane() {
        borderPane.setCenter(scannerPane);
        scannerPane.start();
        scannerPane.requestFocus();
    }

    private void showLoadPane() {
        scannerPane.stop();
        borderPane.setCenter(shapesPane);
        shapesPane.requestFocus();
    }
    
}
