/*
 * Copyright (C) 2016 bnabi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package vigica;

import java.io.IOException;
import java.net.URL;

import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;

/**
 * Main class
 * 
 * @author bnabi
 */
@Lazy
@SpringBootApplication
public class Vigica extends AbstractJavaFxApplicationSupport {

    private Stage primaryStage;
    private AnchorPane rootLayout;

    public Vigica() {}

    @Override
    public void start(Stage primaryStage) throws Exception {

		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

    	this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Vigica Edit");
        this.primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/app_icon.png")));

        initRootLayout();

        showServiceOverview();
        
        this.primaryStage.setOnCloseRequest(e -> Platform.exit());
    }

    /**
     * Initializes the root layout.
     */
    public void initRootLayout() {
        try {
            // Load root layout from fxml file.
            FXMLLoader loader = new FXMLLoader();
            URL fxml = getClass().getResource("/vigica/view/RootLayout.fxml");
            loader.setLocation(fxml);
            rootLayout = (AnchorPane) loader.load();

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            primaryStage.setScene(scene);
            primaryStage.setResizable(false);
            primaryStage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Shows the service overview inside the root layout.
     */
    public void showServiceOverview() {
        try {
            // Load person overview.
            FXMLLoader loader = new FXMLLoader();
            URL fxml = getClass().getResource("/vigica/view/FXMLMain.fxml");
            loader.setLocation(fxml);
            AnchorPane serviceOverview = (AnchorPane) loader.load();

            // Set service overview into the center of root layout.
            rootLayout.getChildren().add(serviceOverview);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {

    	launchApp(Vigica.class, args);
    }
}
