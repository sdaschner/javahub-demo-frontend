package drawandcut;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

/**
 *
 * @author akouznet
 */
public class DrawAndCut extends Application {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("JavaOne2016 - Draw and Cut demo");
        primaryStage.setScene(new Scene(new BorderPane(new DrawPane())));
        primaryStage.show();
        primaryStage.setMaximized(true);
    }
    
}
