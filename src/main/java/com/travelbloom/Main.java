package com.travelbloom;

import com.travelbloom.database.Database;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(
                Main.class.getResource("/view/login.fxml")
        );

        Scene scene = new Scene(loader.load(), 1200, 750);

        stage.setTitle("TravelBloom - Login");
        stage.setScene(scene);

        stage.setMinWidth(1000);
        stage.setMinHeight(650);
        stage.setResizable(true);

        // Start maximized so the app fills the screen immediately. From
        // here on, every page change goes through SceneManager, which
        // preserves whatever size/maximized/fullscreen state the user is
        // currently in instead of resetting it back to 1200x750.
        stage.setMaximized(true);

        stage.show();
    }

    public static void main(String[] args) {

        Database.initializeDatabase();

        launch(args);
    }
}
