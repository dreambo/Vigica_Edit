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
package dtv;

import java.io.IOException;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.application.Preloader;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import org.apache.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Lazy;

/**
 * Main class
 * 
 * @author bnabi
 */
@Lazy
@SpringBootApplication
public class Vigica extends Application {

	private static final Logger LOG = Logger.getLogger(Vigica.class);

	private static String[] args;
	private static ConfigurableApplicationContext applicationContext;

    public static void main(String[] args) {

    	launch(Vigica.class, Vigica.args = args);
    }

	@Override
	public void init() throws Exception {
		applicationContext = SpringApplication.run(getClass(), args);
		applicationContext.getAutowireCapableBeanFactory().autowireBean(this);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		applicationContext.close();
	}

	@Override
    public void start(Stage primaryStage) throws Exception {

		notifyPreloader(new Preloader.StateChangeNotification(Preloader.StateChangeNotification.Type.BEFORE_START));

        primaryStage.setTitle("Vigica Edit");
        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/app_icon.png")));

        AnchorPane mainView = (AnchorPane) load("/dtv/view/FXMLMain.fxml");

        // Show the scene containing the root layout.
        Scene scene = new Scene(mainView);
        primaryStage.setScene(scene);
        primaryStage.setResizable(false);
        primaryStage.setOnCloseRequest(e -> Platform.exit());
        // set scss style
        /*
        String css = getClass().getResource("/myStyle.css").toExternalForm();
        scene.getStylesheets().clear();
        scene.getStylesheets().add(css);
        */
        primaryStage.show();
    }

    public static Object load(String url) {
        FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(clazz -> applicationContext.getBean(clazz));
        loader.setLocation(Vigica.class.getResource(url));

        try {
            return loader.load();
        } catch (IOException e) {
            LOG.error("Some bad things!", e);
        }

        return null;
    }
}
