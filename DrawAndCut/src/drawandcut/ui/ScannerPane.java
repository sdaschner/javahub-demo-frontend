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

import drawandcut.scanner.QRCodeScanner;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.beans.binding.Bindings;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

/**
 *
 * @author akouznet
 */
public class ScannerPane extends BorderPane {
    
    private final QRCodeScanner codeScanner = new QRCodeScanner();
//    private final Scene scene;
    private final ImageView imageView;
//    private PauseTransition pauseTransition;

    public ScannerPane() {
        imageView = new ImageView();
        imageView.setFitWidth(500);
        imageView.setFitHeight(400);
//        imageView.fitWidthProperty().bind(widthProperty());
//        imageView.fitHeightProperty().bind(heightProperty());

        setBackground(new Background(new BackgroundFill(Color.BLACK, CornerRadii.EMPTY,
                Insets.EMPTY)));
        
        setCenter(imageView);        
        
        Label title = new Label("Scan QR code");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font(25));
        BorderPane.setMargin(title, new Insets(20, 0, 0, 0)); // TODO: Remove this (fixes bad screen feature)
        BorderPane.setAlignment(title, Pos.CENTER);
        
        setTop(title);
        
//        pauseTransition = new PauseTransition(Duration.seconds(0.5));
//        pauseTransition.setCycleCount(Timeline.INDEFINITE);
//        pauseTransition.setOnFinished(t -> takeImage());
//        pauseTransition.play();
        
//        scene = new Scene(this, 500, 400, Color.RED);
//        System.out.println("getScene() = " + getScene());
        //pauseTransition.rateProperty().bind(Bindings.when(scene.windowProperty().isNotNull()).then(1).otherwise(0));
        
//        setOnMouseClicked(e -> takeImage());
    }

    @Override
    protected void layoutChildren() {
        super.layoutChildren();
        Bounds boundsInParent = imageView.getBoundsInParent();
//        codeScanner.setPreviewPosition(
//                (int) Math.round(boundsInParent.getMinX()), 
//                (int) Math.round(boundsInParent.getMinY()), 
//                (int) Math.round(boundsInParent.getWidth()), 
//                (int) Math.round(boundsInParent.getHeight()));
        System.out.println("ScannerPane.layoutChildren() " + boundsInParent);
    }
    
    private int counter = 0;
    
    public void start() {
        System.out.println("ScannerPane.start()");
        codeScanner.startTakingStillImages(imageView.getBoundsInParent().getWidth(), imageView.getBoundsInParent().getHeight(), image -> {
            imageView.setImage(image);
            System.out.println((counter++) + ". image = " + image);
        }, System.err::println);
    }
    
    private void takeImage() {
        System.out.println("ScannerPane.takeImage()");
        codeScanner.takeImage().ifPresent(image -> imageView.setImage(image));
    }
}
