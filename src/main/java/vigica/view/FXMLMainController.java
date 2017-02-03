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
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.DVBS2DBService;
import vigica.service.DVBT2DBService;
import vigica.service.IDVBDBService;
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
@SuppressWarnings("unchecked")
public class FXMLMainController<T extends DVBService> implements Initializable {

    private static final String DVB_S_MW_S1 = "dvb_s_mw_s1";
	private static final String DVB_T_MW_S1 = "dvb_t_mw_s1";
	private static final String DTV_PREFS   = "dtv_preferences.xml";
	private static final String CCCAM_CFG   = "Cccam.txt";

	private File dvbsFile;
	private File dvbtFile;
	private File prefsFile;
	private File cccamFile;

	// final FileChooser fileChooser = new FileChooser();
	final DirectoryChooser fileChooser = new DirectoryChooser();

    static private String[] perf = {"GENERAL", "INFO", "DOCUMENTARY", "MOVIES", "TV SHOW", "ZIC", "SPORT", "KIDS", "DIN", "MISC"}; 

    // @Autowired
    AbstractReader<T> reader;
    IDVBDBService<T> serviceDB;

    @Autowired
    DVBS2DBService serviceDBS2;
    @Autowired
    DVBT2DBService serviceDBT2;
    @Autowired
    DVBWriter<T> generate;
    @Autowired
    Compare_mw_s1<T> compare;
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
    private ObservableList<T> serviceData = FXCollections.observableArrayList();

    private File currentDir = new File("src/test/resources");

    @FXML
    private TableView<T> serviceDVBT2Table;
    @FXML
    private TableColumn<T, Integer> s_idxColumnT2;
    @FXML
    private TableColumn<T, String> s_nameColumnT2;
    @FXML
    private TableColumn<T, String> s_typeColumnT2;
    @FXML
    private TableColumn<T, Integer> s_nidColumnT2;
    @FXML
    private TableColumn<T, String> s_pprColumnT2;
    @FXML
    private TableColumn<T, String> s_newColumnT2;

    @FXML
    private TableView<T> serviceDVBS2Table;
    @FXML
    private TableColumn<T, Integer> s_idxColumnS2;
    @FXML
    private TableColumn<T, String> s_nameColumnS2;
    @FXML
    private TableColumn<T, String> s_typeColumnS2;
    @FXML
    private TableColumn<T, Integer> s_nidColumnS2;
    @FXML
    private TableColumn<T, String> s_pprColumnS2;
    @FXML
    private TableColumn<T, String> s_newColumnS2;

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

    public void initializeT2(URL url, ResourceBundle rb) {
        pi.setVisible(false);
        serviceDVBT2Table.setEditable(true);
        s_idxColumnT2.setCellValueFactory(cellData -> cellData.getValue().idxProperty().asObject());
        s_nameColumnT2.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        s_nameColumnT2.setEditable(true);
        s_typeColumnT2.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        s_nidColumnT2.setCellValueFactory(cellData -> cellData.getValue().nidProperty().asObject());
        s_pprColumnT2.setCellValueFactory(cellData -> cellData.getValue().pprProperty());
        s_newColumnT2.setCellValueFactory(cellData -> cellData.getValue().neewProperty());

        // Context menu
        serviceDVBT2Table.setRowFactory(new Callback<TableView<T>, TableRow<T>>() {
            @Override
            public TableRow<T> call(TableView<T> tableView) {
                final TableRow<T> row = new TableRow<>();
                final ContextMenu rowMenu = new ContextMenu();

                final MenuItem removeItem = new MenuItem("Delete");
                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        final T service = row.getItem();
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
    
        s_pprColumnT2.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
            @Override
            public TableCell<T, String> call(TableColumn<T, String> col) {
                final TableCell<T, String> cell = new TableCell<>();
                
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
                                        final T service = (T) cell.getTableRow().getItem();

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
        s_nameColumnT2.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
            @Override
            public TableCell<T, String> call(TableColumn<T, String> p) {
                return new EditingCell();
            }
        });
        s_nameColumnT2.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<T, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<T, String> t) {
                final T service = t.getTableView().getItems().get(t.getTablePosition().getRow());
                
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

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        pi.setVisible(false);
        serviceDVBS2Table.setEditable(true);
        s_idxColumnS2.setCellValueFactory(cellData -> cellData.getValue().idxProperty().asObject());
        s_nameColumnS2.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        s_nameColumnS2.setEditable(true);
        s_typeColumnS2.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        s_nidColumnS2.setCellValueFactory(cellData -> cellData.getValue().nidProperty().asObject());
        s_pprColumnS2.setCellValueFactory(cellData -> cellData.getValue().pprProperty());
        s_newColumnS2.setCellValueFactory(cellData -> cellData.getValue().neewProperty());
        
        // Context menu
        serviceDVBS2Table.setRowFactory(new Callback<TableView<T>, TableRow<T>>() {
            @Override
            public TableRow<T> call(TableView<T> tableView) {
                final TableRow<T> row = new TableRow<>();
                final ContextMenu rowMenu = new ContextMenu();

                final MenuItem removeItem = new MenuItem("Delete");
                removeItem.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent event) {
                        final T service = row.getItem();
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
    
        s_pprColumnS2.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
            @Override
            public TableCell<T, String> call(TableColumn<T, String> col) {
                final TableCell<T, String> cell = new TableCell<>();
                
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
                                        final T service = (T) cell.getTableRow().getItem();

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
        s_nameColumnS2.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
            @Override
            public TableCell<T, String> call(TableColumn<T, String> p) {
                return new EditingCell();
            }
        });
        s_nameColumnS2.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<T, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<T, String> t) {
                final T service = t.getTableView().getItems().get(t.getTablePosition().getRow());
                
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
        initializeT2(url, rb);
    }

    @FXML
    private void openAction(ActionEvent event) throws Exception {

        fileChooser.setTitle("Open Services File");
        fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
        File file = fileChooser.showDialog(serviceDVBS2Table.getScene().getWindow()); // OpenDialog(serviceTable.getScene().getWindow());

        if (file != null) {

            currentDir = file.getParentFile();
            initFiles(file);
            reader = getReader(dvbsFile.getName());
            reader.setServiceDB(serviceDB);

            if (reader == null) {
            	Message.errorMessage("Wrong file, only files starting with " + DVB_S_MW_S1 + " or " + DVB_T_MW_S1 + " are accepted!");
            	return;
            }

            reader.setDvbFile(dvbsFile);
            handleTask(event, (Service<List<T>>) reader, file.getName(), "Opening file");

            reader = getReader(dvbtFile.getName());
            reader.setServiceDB(serviceDB);

            if (reader == null) {
            	Message.errorMessage("Wrong file, only files starting with " + DVB_S_MW_S1 + " or " + DVB_T_MW_S1 + " are accepted!");
            	return;
            }

            reader.setDvbFile(dvbtFile);
            handleTask(event, (Service<List<T>>) reader, file.getName(), "TOTO");

        }
    }

    private void initFiles(File file) {

    	File[] files = file.listFiles();

    	for (File dtvFile: files) {
			if (dtvFile.getName().equalsIgnoreCase(DVB_S_MW_S1)) {
				dvbsFile = dtvFile;
			} else if (dtvFile.getName().equalsIgnoreCase(DVB_T_MW_S1)) {
				dvbtFile = dtvFile;
			} else if (dtvFile.getName().equalsIgnoreCase(DTV_PREFS)) {
				prefsFile = dtvFile;
			} else if (dtvFile.getName().equalsIgnoreCase(CCCAM_CFG)) {
				cccamFile = dtvFile;
			}
		}
	}

	private AbstractReader<T> getReader(String fileName) {

    	if (fileName.toLowerCase().startsWith(DVB_S_MW_S1)) {
    		serviceDB = (IDVBDBService<T>) serviceDBS2;
    		return (AbstractReader<T>) dvbs2reader;
    	}

    	if (fileName.toLowerCase().startsWith(DVB_T_MW_S1)) {
    		serviceDB = (IDVBDBService<T>) serviceDBT2;
    		return (AbstractReader<T>) dvbt2reader;
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
	    File file = fileChooser.showDialog(serviceDVBS2Table.getScene().getWindow()); // SaveDialog(serviceTable.getScene().getWindow());

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
        File file = fileChooser.showDialog(serviceDVBS2Table.getScene().getWindow()); // OpenDialog(serviceTable.getScene().getWindow());

        if (file != null) {
            currentDir = file.getParentFile();
            compare.setDvbFile(file);
            handleTask(event, compare, null, "Compare files");
        }
    }

    @FXML
	private void duplicateAction(ActionEvent event) throws Exception {
/*
    	duplicate.setServices(serviceData);
		handleTask(event, duplicate, null, "Remove duplicate");
*/
	}

	@FXML
    private void filterAction(KeyEvent event) {

    	List<T> services = serviceDB.read_bdd(s_name.getText());
        serviceData.setAll(services);
    }

    private void handleTask(ActionEvent event, Service<List<T>> task, String zeTitle, String action) throws Exception {

    	task.reset();
        pi.visibleProperty().bind(task.runningProperty());
        pi.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {

			@Override
            public void handle(WorkerStateEvent t) {
                // print services into tableview
        		List<T> dvbServices = task.getValue();
        		if (dvbServices != null) {
            		serviceData.setAll(dvbServices);
            		if ("TOTO".equals(action)) {
            			serviceDVBT2Table.setItems(serviceData);
            		} else {
                		serviceDVBS2Table.setItems(serviceData);
            		}
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

    class EditingCell extends TableCell<T, String> {

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
