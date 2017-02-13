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
package dtv.controller;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;


/**
 *
 * @author bnabi
 */
public class Message {

	private Message() {}

	public static void errorMessage(String msg) {
    	showMessage(msg, false);
    }
    
    public static void infoMessage(String msg) {
    	showMessage(msg, true);
    }

    private static void showMessage(String msg, boolean info) {

    	/*
    	Stage dialog = new Stage();
    	dialog.initStyle(StageStyle.UTILITY);
    	Scene scene = new Scene(new Group(new Text(25, 25, msg)));
    	dialog.setResizable(false);
    	dialog.setWidth(200);
    	dialog.setHeight(100);
    	dialog.setScene(scene);
    	dialog.show();
    	*/
        Alert alert = new Alert(info ? AlertType.INFORMATION : AlertType.ERROR);
        alert.setTitle("Info");
        alert.setHeaderText(null);
        alert.setContentText(msg);

        alert.showAndWait();
    }
}
