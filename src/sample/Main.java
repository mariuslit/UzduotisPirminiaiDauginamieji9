package sample;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import static sample.Controller.irasaiTreeSet;

public class Main extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Skaičių skaidymas pirminiais dauginamaisiais");
        primaryStage.setScene(new Scene(root, 600, 400));
        primaryStage.show();
    }

    //if needed close method in main class
    @Override
    public void stop() throws Exception {

        // duomenų išsaugojimas išeinant iš programos
        WriteData.writeData(irasaiTreeSet);

        Platform.exit();
        System.exit(0);

    }

    public static void main(String[] args) {
        launch(args);
    }
}
