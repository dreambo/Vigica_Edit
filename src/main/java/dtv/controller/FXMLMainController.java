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

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dtv.database.DVBDBService;
import dtv.database.DVBS2DBService;
import dtv.database.DVBT2DBService;
import dtv.model.DVBChannel;
import dtv.tools.ByteUtils;
import dtv.tools.DuplicateFinder;
import dtv.tools.reader.AbstractReader;
import dtv.tools.reader.DVBS2Reader;
import dtv.tools.reader.DVBT2Reader;
import dtv.tools.writer.DVBS2Writer;
import dtv.tools.writer.DVBT2Writer;
import dtv.tools.writer.DVBWriter;
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
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.SortType;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;
import javafx.util.Callback;

/**
 *
 * @author bnabi
 */
@Component
@SuppressWarnings("unchecked")
public class FXMLMainController<T extends DVBChannel> implements Initializable {

    private static final String DVB_S_MW_S1 = "dvb_s_mw_s1";
	private static final String DVB_T_MW_S1 = "dvb_t_mw_s1";
	private static final String DTV_PREFS   = "dtv_preferences.xml";
	private static final String CCCAM_CFG   = "Cccam.txt";

	private static final String DVB_S2 = "DVB-S2";
	private static final String DVB_T2 = "DVB-T2";

	private File dvbs2File;
	private File dvbt2File;
	private File prefsFile;
	private File cccamFile;

	// final FileChooser fileChooser = new FileChooser();
	final DirectoryChooser fileChooser = new DirectoryChooser();

    static private String[] perf = {"GENERAL", "INFO", "DOCUMENTARY", "MOVIES", "TV SHOW", "ZIC", "SPORT", "KIDS", "DIN", "MISC"}; 

    @Autowired
    DVBS2DBService serviceDBS2;
    @Autowired
    DVBT2DBService serviceDBT2;
    DVBWriter<T> writer;
    @Autowired
    DVBS2Writer dvbs2Writer;
    @Autowired
    DVBT2Writer dvbt2Writer;
    @Autowired
    DuplicateFinder<T> duplicate;

    // test
    @Autowired
    DVBT2Reader dvbt2reader;
    @Autowired
    DVBS2Reader dvbs2reader;
    // /test

    /**
    * The data as an observable list of Service.
    */
    private ObservableList<T> serviceDataS2 = FXCollections.observableArrayList();
    private ObservableList<T> serviceDataT2 = FXCollections.observableArrayList();

    private File currentDir = new File("src/test/resources");

    @FXML
    TabPane tabPane;
    @FXML
    private TableView<T> serviceDVBT2Table;
    @FXML
    private TableColumn<T, Integer> s_idxColumnT2;
    @FXML
    private TableColumn<T, String> s_nameColumnT2;
    @FXML
    private TableColumn<T, String> s_typeColumnT2;
    // @FXML
    // private TableColumn<T, Integer> s_nidColumnT2;
    @FXML
    private TableColumn<T, String> s_pprColumnT2;
    // @FXML
    // private TableColumn<T, String> s_newColumnT2;

    @FXML
    private TableView<T> serviceDVBS2Table;
    @FXML
    private TableColumn<T, Integer> s_idxColumnS2;
    @FXML
    private TableColumn<T, String> s_nameColumnS2;
    @FXML
    private TableColumn<T, String> s_typeColumnS2;
    // @FXML
    // private TableColumn<T, Integer> s_nidColumnS2;
    @FXML
    private TableColumn<T, String> s_pprColumnS2;
    // @FXML
    // private TableColumn<T, String> s_newColumnS2;

    @FXML
    private TextField s_name;
    @FXML
    private Button openButton;
    @FXML
    private Button saveButton;
    @FXML
    private Button sortButton;
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

    public void init(ObservableList<T> serviceData, DVBDBService<T> serviceDB, TableView<T> table, TableColumn<T, Integer> idx, TableColumn<T, String> name, TableColumn<T, String> type, TableColumn<T, String> ppr) {

        table.setEditable(true);
        idx.setCellValueFactory(cellData -> cellData.getValue().idxProperty().asObject());
        name.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        name.setEditable(true);
        type.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        // nid.setCellValueFactory(cellData -> cellData.getValue().nidProperty().asObject());
        ppr.setCellValueFactory(cellData -> cellData.getValue().pprProperty());
        // newCol.setCellValueFactory(cellData -> cellData.getValue().neewProperty());

        // Context menu
        table.setRowFactory(new Callback<TableView<T>, TableRow<T>>() {
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
                       	serviceDB.delete_bdd(service);
                    }
                });
                
                rowMenu.getItems().addAll(removeItem);
                row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty()))
                        .then(rowMenu)
                        .otherwise((ContextMenu) null));

                return row;
            }
        });
    
        ppr.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
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

                                        service.setPpr(new_ppr);
                                        service.setFlag(true);
                                        serviceDB.update_bdd(service);
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
        name.setCellFactory(new Callback<TableColumn<T, String>, TableCell<T, String>>() {
            @Override
            public TableCell<T, String> call(TableColumn<T, String> p) {
                return new EditingCell();
            }
        });
        name.setOnEditCommit(new EventHandler<TableColumn.CellEditEvent<T, String>>() {
            @Override
            public void handle(TableColumn.CellEditEvent<T, String> t) {
                final T service = t.getTableView().getItems().get(t.getTablePosition().getRow());
                
                service.setName(t.getNewValue());
                service.setFlag(true);
                serviceDB.update_bdd(service);
            }
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        pi.setVisible(false);
        init(serviceDataS2, (DVBDBService<T>) serviceDBS2, serviceDVBS2Table, s_idxColumnS2, s_nameColumnS2, s_typeColumnS2, s_pprColumnS2);
        init(serviceDataT2, (DVBDBService<T>) serviceDBT2, serviceDVBT2Table, s_idxColumnT2, s_nameColumnT2, s_typeColumnT2, s_pprColumnT2);
        disableComponents(true);
    }

    @FXML
    private void openAction(ActionEvent event) throws Exception {

        fileChooser.setTitle("Open Services File");
        fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
        File rootFolder = fileChooser.showDialog(tabPane.getScene().getWindow()); // OpenDialog(serviceTable.getScene().getWindow());

        if (rootFolder != null) {

            currentDir = rootFolder; // .getParentFile();
            initFiles(rootFolder);
            AbstractReader<T> reader;
			serviceDataS2.clear();
			serviceDataT2.clear();
			serviceDBS2.deleteAll();
			serviceDBT2.deleteAll();

			if (dvbs2File != null) {
            	reader = getReader(dvbs2File);
	            handleTask((Service<List<T>>) reader, rootFolder.getName(), "Opening " + DVB_S2 + " file " + dvbs2File);
	            tabPane.getSelectionModel().select(0);
            }

            if (dvbt2File != null) {
	            reader = getReader(dvbt2File);
	            handleTask((Service<List<T>>) reader, rootFolder.getName(), "Opening " + DVB_T2 + " file " + dvbs2File);
	            tabPane.getSelectionModel().select(1);
            }
        }
    }

    private void initFiles(File rootFolder) {

    	File[] files = rootFolder.listFiles();
    	dvbs2File = dvbt2File = prefsFile = cccamFile = null;

    	for (File dtvFile: files) {
			if (dtvFile.getName().equalsIgnoreCase(DVB_S_MW_S1)) {
				dvbs2File = dtvFile;
			} else if (dtvFile.getName().equalsIgnoreCase(DVB_T_MW_S1)) {
				dvbt2File = dtvFile;
			} else if (dtvFile.getName().equalsIgnoreCase(DTV_PREFS)) {
				prefsFile = dtvFile;
			} else if (dtvFile.getName().equalsIgnoreCase(CCCAM_CFG)) {
				cccamFile = dtvFile;
			}
		}
	}

	private AbstractReader<T> getReader(File dvbFile) {

		String fileName = dvbFile.getName();
		AbstractReader<T> reader = null;

    	if (fileName.toLowerCase().startsWith(DVB_S_MW_S1)) {
    		reader = (AbstractReader<T>) dvbs2reader;
    		reader.setServiceDB((DVBDBService<T>) serviceDBS2);
    		reader.setDvbFile(dvbFile);

    	} else if (fileName.toLowerCase().startsWith(DVB_T_MW_S1)) {
    		reader = (AbstractReader<T>) dvbt2reader;
    		reader.setServiceDB((DVBDBService<T>) serviceDBT2);
    		reader.setDvbFile(dvbFile);
    	}

    	return reader;
	}

	private void save(File dvbFile, ObservableList<T> serviceData) throws Exception {

		if (serviceData.size() == 0 || dvbFile == null) {
	        Message.errorMessage("No service file loaded\n");
	        return;
	    }

        writer.setDvbFile(dvbFile);
        writer.setFileVersion(getReader(dvbFile).getFileVersion());
        handleTask(writer, dvbFile.getName(), "Save services " + dvbFile.getName());
	}

	@FXML
	private void saveAction(ActionEvent event) throws Exception {

    	fileChooser.setTitle("Export Services");
	    fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
	    File rootFolder = fileChooser.showDialog(tabPane.getScene().getWindow()); // SaveDialog(serviceTable.getScene().getWindow());

	    if (rootFolder != null) {
	        currentDir = rootFolder; //.getParentFile();
	        // save DVB-S2
			if (dvbs2File != null) {
				writer = (DVBWriter<T>) dvbs2Writer;
				save(new File(rootFolder, DVB_S_MW_S1), serviceDataS2);
			}
	        // save DVB-T2
			if (dvbt2File != null) {
				writer = (DVBWriter<T>) dvbt2Writer;
				save(new File(rootFolder, DVB_T_MW_S1), serviceDataT2);
			}
	    }
	}
/*
	@FXML
    private void compareAction(ActionEvent event) throws Exception {

        if (serviceDataS2.size() == 0) {
            Message.errorMessage("No service file loaded\n");
            return;
        }
        fileChooser.setTitle("Open Old Services");
        fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
        File file = fileChooser.showDialog(tabPane.getScene().getWindow()); // OpenDialog(serviceTable.getScene().getWindow());

        if (file != null) {
            currentDir = file.getParentFile();
            compare.setDvbFile(file);
            handleTask(compare, null, "Compare files");
        }
    }
*/
    @FXML
	private void duplicateAction(ActionEvent event) throws Exception {
/*
    	duplicate.setServices(serviceData);
		handleTask(event, duplicate, null, "Remove duplicate");
*/
    	String action = "Remove duplicate services ";
		if (tabPane.getSelectionModel().isSelected(0)) {
			// DVB-S2
			duplicate.setServices((ObservableList<T>) serviceDataS2);
	        duplicate.setBdd((DVBDBService<T>) serviceDBS2);
	        action += DVB_S2;

		} else if (tabPane.getSelectionModel().isSelected(1)) {
			// DVB-T2
			duplicate.setServices((ObservableList<T>) serviceDataT2);
	        duplicate.setBdd((DVBDBService<T>) serviceDBT2);
	        action += DVB_T2;
		}

		handleTask(duplicate, null, action);
	}

	@FXML
    private void sortAction(ActionEvent event) {

		if (tabPane.getSelectionModel().isSelected(0)) {
			// DVB-S2
			SortType sortType = s_nameColumnT2.getSortType();
			System.out.println(sortType);
	    	List<T> services = (List<T>) serviceDBS2.read_bdd(s_name.getText());
	        serviceDataS2.setAll(services);

		} else if (tabPane.getSelectionModel().isSelected(1)) {
			// DVB-T2
	    	List<T> services = (List<T>) serviceDBT2.read_bdd(s_name.getText());
	        serviceDataT2.setAll(services);
		}
    }

	@FXML
    private void filterAction(KeyEvent event) {

		if (tabPane.getSelectionModel().isSelected(0)) {
			// DVB-S2
	    	List<T> services = (List<T>) serviceDBS2.read_bdd(s_name.getText());
	        serviceDataS2.setAll(services);

		} else if (tabPane.getSelectionModel().isSelected(1)) {
			// DVB-T2
	    	List<T> services = (List<T>) serviceDBT2.read_bdd(s_name.getText());
	        serviceDataT2.setAll(services);
		}
    }

    private void handleTask(Service<List<T>> task, String zeTitle, String action) throws Exception {

        pi.visibleProperty().bind(task.runningProperty());
        pi.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(new EventHandler<WorkerStateEvent>() {
			@Override
            public void handle(WorkerStateEvent t) {
                // print services into tableview
        		List<T> dvbServices = task.getValue();
        		if (dvbServices != null) {
            		if (action.contains(DVB_S2)) {
                		serviceDataS2.setAll(dvbServices);
            			serviceDVBS2Table.setItems(serviceDataS2);
            		} else if (action.contains(DVB_T2)) {
                		serviceDataT2.setAll(dvbServices);
                		serviceDVBT2Table.setItems(serviceDataT2);
            		}
            	}

                if (zeTitle != null) title.setText(zeTitle);
                disableComponents(false);
            }
        });

        task.setOnFailed(new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent t) {
                Message.errorMessage("Error " + action + "\n" + task.getException().getMessage());
            }
        });

    	if (!task.isRunning()) {
    		task.reset();
    		task.start();
    	}
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

    private void disableComponents(boolean disable) {
        s_name.setDisable(disable);
        s_name.clear();
        saveButton.setDisable(disable);
        sortButton.setDisable(disable);
        duplicateButton.setDisable(disable);
        tabPane.setDisable(disable);
	}
}