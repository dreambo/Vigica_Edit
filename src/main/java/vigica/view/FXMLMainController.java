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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.FileChooser;
import javafx.util.Callback;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.IDBService;
import vigica.tools.AbstractReader;
import vigica.tools.ByteUtils;
import vigica.tools.Compare_mw_s1;
import vigica.tools.DVBS2Reader;
import vigica.tools.DVBT2Reader;
import vigica.tools.DVBWriter;
import vigica.tools.DuplicateFinder;

/**
 *
 * @author bnabi
 */
@Component
public class FXMLMainController implements Initializable {

    private static final String DVB_S_MW_S1 = "dvb_s_mw_s1";
	private static final String DVB_T_MW_S1 = "dvb_t_mw_s1";

	final FileChooser fileChooser = new FileChooser();
    static private String[] perf = {"GENERAL", "INFO", "DOCUMENTARY", "MOVIES", "TV SHOW", "ZIC", "SPORT", "KIDS", "DIN", "MISC"}; 

    // @Autowired
    AbstractReader reader;
    @Autowired
    IDBService serviceDB;
    @Autowired
    DVBWriter generate;
    @Autowired
    Compare_mw_s1 compare;
    @Autowired
    DuplicateFinder duplicate;

    // test
    @Autowired
    DVBT2Reader dvbt2reader;
    @Autowired
    DVBS2Reader dvbs2reader;
    // /test

    /**
    * The data as an observable list of Service.
    */
    private ObservableList<DVBService> serviceData = FXCollections.observableArrayList();

    private File currentDir = new File("src/test/resources");

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
    @FXML
    private TableColumn<DVBService, String> s_newColumn;
    @FXML
    private TextField s_name;
    @FXML
    private Button saveButton;
    @FXML
    private Button compareButton;
    @FXML
    private Button duplicateButton;

    @FXML
    private ProgressIndicator pi;
    @FXML
    private Label title;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public FXMLMainController() {}

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pi.setVisible(false);
        serviceTable.setEditable(true);
        s_idxColumn.setCellValueFactory(cellData -> cellData.getValue().idxProperty().asObject());
        s_nameColumn.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        s_nameColumn.setEditable(true);
        s_typeColumn.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        s_nidColumn.setCellValueFactory(cellData -> cellData.getValue().nidProperty().asObject());
        s_pprColumn.setCellValueFactory(cellData -> cellData.getValue().pprProperty());
        s_newColumn.setCellValueFactory(cellData -> cellData.getValue().neewProperty());
        
        // Context menu
        serviceTable.setRowFactory(new Callback<TableView<DVBService>, TableRow<DVBService>>() {
            @Override
            public TableRow<DVBService> call(TableView<DVBService> tableView) {
                final TableRow<DVBService> row = new TableRow<>();
                final ContextMenu rowMenu = new ContextMenu();

                final MenuItem removeItem = new MenuItem("Delete");
                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        final DVBService service = row.getItem();
                        serviceData.removeAll(service);
                        
                        try {
                        	serviceDB.delete_bdd(service);
                        }
                        catch (HibernateException e) {
                            Message.errorMessage("Error delete service BDD\n"+e.getMessage());
                        }
                    }
                });
                
                rowMenu.getItems().addAll(removeItem);
                row.contextMenuProperty().bind(
                        Bindings.when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));
                return row;
            }
        });
    
        s_pprColumn.setCellFactory(new Callback<TableColumn<DVBService, String>, TableCell<DVBService, String>>() {
            @Override
            public TableCell<DVBService, String> call(TableColumn<DVBService, String> col) {
                final TableCell<DVBService, String> cell = new TableCell<>();
                
                cell.textProperty().bind(cell.itemProperty());
                cell.itemProperty().addListener(new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> obs, String oldValue, String newValue) {
                        if (newValue != null) {
                            final ContextMenu cellMenu = new ContextMenu();
                            for (int i=1; i<=10; i++) {
                                final CheckMenuItem prefMenuItem = new CheckMenuItem(perf[i-1]);
                                final int line = i;

                                prefMenuItem.setId(String.valueOf(i));
                                if (ByteUtils.isPreferenceOn(cell.getText(), i)) {
                                    prefMenuItem.setSelected(true);
                                }

                                prefMenuItem.selectedProperty().addListener(new ChangeListener<Boolean>() {
                                    @Override
                                    public void changed(ObservableValue<? extends Boolean> obs1, Boolean old_val, Boolean new_val) {
                                        final String new_ppr;
                                        final DVBService service = (DVBService) cell.getTableRow().getItem();

                                        if (new_val) {
                                            new_ppr = ByteUtils.add_ppr(cell.getText(), line);
                                        } else {
                                            new_ppr = ByteUtils.remove_ppr(cell.getText(), line);
                                        }

                                        try {
                                            service.setPpr(new_ppr);
                                            service.setFlag(true);
                                            serviceDB.update_bdd(service);
                                        }
                                        catch (HibernateException e) {
                                            Message.errorMessage("Error update BDD\n"+e.getMessage());
                                        }
                                    }
                                });

                                cellMenu.getItems().add(prefMenuItem);
                                cell.setContextMenu(cellMenu);
                            }
                        } else {
                            cell.setContextMenu(null);
                        }
                    }
                });
                return cell;
            }
        });
        
        // Editable service name
        s_nameColumn.setCellFactory(new Callback<TableColumn<DVBService, String>, TableCell<DVBService, String>>() {
            @Override
            public TableCell<DVBService, String> call(TableColumn<DVBService, String> p) {
                return new EditingCell();
            }
        });
        s_nameColumn.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<DVBService, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<DVBService, String> t) {
                final DVBService service = t.getTableView().getItems().get(t.getTablePosition().getRow());
                
                try {
                    service.setName(t.getNewValue());
                    service.setFlag(true);
                    serviceDB.update_bdd(service);
                }
                catch (HibernateException e) {
                    Message.errorMessage("Error update BDD\n"+e.getMessage());
                }
            }
        });

        enableComponents(false);
    }

    @FXML
    private void openAction(ActionEvent event) throws Exception {

        fileChooser.setTitle("Open Services File");
        fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
        File file = fileChooser.showOpenDialog(serviceTable.getScene().getWindow());

        if (file != null) {

            currentDir = file.getParentFile();
            String fileName = file.getName();
            reader = getReader(fileName);
            if (reader == null) {
            	Message.errorMessage("Wrong file, only files starting with " + DVB_S_MW_S1 + " or " + DVB_T_MW_S1 + " are accepted!");
            	return;
            }

            reader.setDvbFile(file);
            handleTask(event, (Service<List<DVBService>>) reader, fileName, "Opening file");
        }
    }

    private AbstractReader getReader(String fileName) {

    	if (fileName.toLowerCase().startsWith(DVB_S_MW_S1)) {
    		return dvbs2reader;
    	}

    	if (fileName.toLowerCase().startsWith(DVB_T_MW_S1)) {
    		return dvbt2reader;
    	}

		return null;
	}

	@FXML
	private void saveAction(ActionEvent event) throws Exception {
	
    	if (serviceData.size() == 0) {
	        Message.errorMessage("No service file loaded\n");
	        return;
	    }
	    fileChooser.setTitle("Export Services");
	    fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
	    File file = fileChooser.showSaveDialog(serviceTable.getScene().getWindow());
	
	    if (file != null) {
	        currentDir = file.getParentFile();
	        generate.setDvbFile(file);
	        generate.setFileVersion(reader.getFileVersion());
	        handleTask(event, generate, file.getName(), "Save services");
	    }
	}

	@FXML
    private void compareAction(ActionEvent event) throws Exception {

        if (serviceData.size() == 0) {
            Message.errorMessage("No service file loaded\n");
            return;
        }
        fileChooser.setTitle("Open Old Services");
        fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
        File file = fileChooser.showOpenDialog(serviceTable.getScene().getWindow());

        if (file != null) {
            currentDir = file.getParentFile();
            compare.setDvbFile(file);
            handleTask(event, compare, null, "Compare files");
        }
    }

    @FXML
	private void duplicateAction(ActionEvent event) throws Exception {

    	duplicate.setServices(serviceData);
		handleTask(event, duplicate, null, "Remove duplicate");
	}

	@FXML
    private void filterAction(KeyEvent event) {

    	List<DVBService> services = serviceDB.read_bdd(s_name.getText());
        serviceData.setAll(services);
    }

    private void handleTask(ActionEvent event, Service<List<DVBService>> task, String zeTitle, String action) throws Exception {

    	task.reset();
        pi.visibleProperty().bind(task.runningProperty());
        pi.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
            public void handle(WorkerStateEvent t) {
                // print services into tableview
        		List<DVBService> dvbServices = task.getValue();
        		if (dvbServices != null) {
            		serviceData.setAll(dvbServices);
            		serviceTable.setItems(serviceData);
            	}

                if (zeTitle != null) title.setText(zeTitle);
                enableComponents(true);
            }
        });

        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                Message.errorMessage("Error " + action + "\n" + task.getException().getMessage());
            }
        });

        task.start();
    }

    class EditingCell extends TableCell<DVBService, String> {

        private TextField textField;

        @Override
        public void startEdit() {
            super.startEdit();

            if (textField == null) {
                createTextField();
            }

            setGraphic(textField);
            setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            textField.selectAll();
        }

        @Override
        public void cancelEdit() {
            super.cancelEdit();

            setText(String.valueOf(getItem()));
            setContentDisplay(ContentDisplay.TEXT_ONLY);
        }

        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);

            if (empty) {
                setText(null);
                setGraphic(null);
            } else if (isEditing()) {
                if (textField != null) {
                    textField.setText(getString());
                }
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            } else {
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }
        }

        private void createTextField() {
            textField = new TextField(getString());
            textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
            textField.setOnKeyPressed(new EventHandler<KeyEvent>() {

                @Override
                public void handle(KeyEvent t) {
                    if (t.getCode() == KeyCode.ENTER) {
                        commitEdit(textField.getText());
                    } else if (t.getCode() == KeyCode.ESCAPE) {
                        cancelEdit();
                    }
                }
            });
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }

    private void enableComponents(boolean enable) {
        s_name.setDisable(!enable);
        s_name.clear();
        saveButton.setDisable(!enable);
        compareButton.setDisable(!enable);
        duplicateButton.setDisable(!enable);
	}
}
