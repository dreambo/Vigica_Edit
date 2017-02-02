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
package vigica.view;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.stereotype.Component;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import vigica.model.DVBService;

/**
 *
 * @author bnabi
 */
@Component
public class FXMLCompareController implements Initializable {

    /**
    * The data as an observable list of Service.
    */
    private ObservableList<DVBService> serviceData = FXCollections.observableArrayList();

    // @FXML
    // private Stage stage;
    @FXML
    private TableView<DVBService> serviceTable;
    @FXML
    private TableColumn<DVBService, Integer> s_idxColumn;
    @FXML
    private TableColumn<DVBService, String> s_nameColumn;
    @FXML
    private TableColumn<DVBService, String> s_typeColumn;
    @FXML
    private TableColumn<DVBService, Integer> s_nidColumn;
    @FXML
    private TableColumn<DVBService, String> s_pprColumn;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public FXMLCompareController() {}

    public void setServices(List<DVBService> services) {
        serviceData.setAll(services);
        serviceTable.setItems(serviceData);
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        s_idxColumn.setCellValueFactory(cellData -> cellData.getValue().idxProperty().asObject());
        s_nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        s_typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        s_nidColumn.setCellValueFactory(cellData -> cellData.getValue().nidProperty().asObject());
        s_pprColumn.setCellValueFactory(cellData -> cellData.getValue().pprProperty());
    }
}
