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
import static drawandcut.Configuration.log;
import drawandcut.scanner.QRCodeScanner;
import java.util.function.Consumer;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.geometry.Bounds;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 *
 * @author akouznet
 */
public class ScannerPane extends BorderPane {
    
    private final QRCodeScanner codeScanner = new QRCodeScanner();
    private Consumer<String> onRead;
    private int counter;
    private final Label title;
    private final Pane preview = new StackPane();
    private final ImageView previewImage;
    private final ProgressIndicator progress = new ProgressIndicator(ProgressIndicator.INDETERMINATE_PROGRESS);
    private boolean startAfterLayout = false;
    private Bounds previewBounds = null;
    private final BooleanProperty showProgress = new SimpleBooleanProperty(false);
    private boolean running = false;
    
    public ScannerPane() {
        
        title = new Label("Scan QR code");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(25));
        BorderPane.setAlignment(title, Pos.CENTER);
        
        showProgress.addListener(t -> {
            if (showProgress.get()) {
                title.setText("Loading...");
            }
        });
        
        previewImage = new ImageView();
        previewImage.setManaged(false);
        previewImage.fitWidthProperty().bind(preview.widthProperty());
        previewImage.fitHeightProperty().bind(preview.heightProperty());
        preview.getChildren().add(previewImage);
        
        if (Configuration.DISABLE_CAMERA) {
            Button scan = new Button("Camera preview will appear here");
            scan.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            scan.setOnAction(t -> {
                if (onRead != null) {
                    onRead.accept("01a79a5a-df29-4c21-9fb8-770406a30509");
                }
            });
            preview.getChildren().add(scan);
        }
        
        progress.setMaxSize(100, 100);
        progress.visibleProperty().bind(showProgress);
        preview.getChildren().add(progress);
        
        setCenter(preview);
        
        setId("scannerPane");
        setTop(title);
    }

    public void start() {        
        title.setText("Scan QR code");
        counter = 0;
        log("ScannerPane.start()");
        if (previewBounds == null) {
            startAfterLayout = true;
        } else {
            doStart();
        }
    }

    public ScannerPane(Consumer<String> onRead, int counter) {
        this();
        this.onRead = onRead;
        this.counter = counter;
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren(); //To change body of generated methods, choose Tools | Templates.
        if (startAfterLayout) {
            doStart();
        }
    }
    
    private void doStart() {
        startAfterLayout = false;
        if (running) {
            return;
        }
        running = true;
        log("ScannerPane.doStart()");
        setPreviewBounds();
        codeScanner.startTakingStillImages(previewBounds.getWidth(), previewBounds.getHeight(), (image, code) -> {
            running = false;
            log((counter++) + ". image = " + image);
            previewImage.setImage(image);
            if (onRead != null) {
                onRead.accept(code);
            }
        }, System.err::println);
    }
    
    private void setPreviewBounds() {
        if (previewBounds == null) {
            previewBounds = preview.localToScreen(preview.getBoundsInLocal());
            codeScanner.setPreviewPosition(
                    (int) Math.round(previewBounds.getMinX()), 
                    (int) Math.round(previewBounds.getMinY()), 
                    (int) Math.round(previewBounds.getWidth()), 
                    (int) Math.round(previewBounds.getHeight()));
        }
    }
    
    public void stop() {
        log("ScannerPane.stop()");
        codeScanner.stopTakingStillImages();
        running = false;
    }
    
    public void setOnRead(Consumer<String> onRead) {
        this.onRead = onRead;
    }

    public BooleanProperty showProgress() {
        return showProgress;
    }
    
    public void setTitle(String text) {
        title.setText(text);
    }    
}
