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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.function.Predicate;

import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Service;
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
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.stage.DirectoryChooser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dtv.model.DVBChannel;
import dtv.tools.DuplicateRemover;
import dtv.tools.Utils;
import dtv.tools.reader.AbstractReader;
import dtv.tools.reader.DVBS2Reader;
import dtv.tools.reader.DVBT2Reader;
import dtv.tools.writer.DVBS2Writer;
import dtv.tools.writer.DVBT2Writer;
import dtv.tools.writer.DVBWriter;

/**
 *
 * @author bnabi
 */
@Component
@SuppressWarnings("unchecked")
public class FXMLMainController<T extends DVBChannel> implements Initializable {

	private static final Logger LOG = LoggerFactory.getLogger(FXMLMainController.class);

    private static final String DVB_S_MW_S1 = "dvb_s_mw_s1";
	private static final String DVB_T_MW_S1 = "dvb_t_mw_s1";
	private static final String DTV_PREFS   = "dtv_preferences.xml";
	private static final String CCCAM_CFG   = "Cccam.txt";

	private static final String DVB_S2 = "DVB-S2";
	private static final String DVB_T2 = "DVB-T2";

	private static final String FAV_TAG = "fav_list_name";
	private static final String FAV_SEP = "#";

	private File dvbs2File;
	private File dvbt2File;
	private File prefsFile;
	private File cccamFile;

	private int order = 1;

	final DirectoryChooser fileChooser = new DirectoryChooser();

    DVBWriter<T> writer;
    @Autowired
    DVBS2Writer dvbs2Writer;
    @Autowired
    DVBT2Writer dvbt2Writer;
    @Autowired
    DuplicateRemover<T> duplicate;

    @Autowired
    DVBT2Reader dvbt2reader;
    @Autowired
    DVBS2Reader dvbs2reader;

    SortedList<T> sortedDataS2;
    SortedList<T> sortedDataT2;

    /**
    * The data as an observable list of Service.
    */
    private ObservableList<T> serviceDataS2 = FXCollections.observableArrayList();
    private ObservableList<T> serviceDataT2 = FXCollections.observableArrayList();

    private File currentDir = new File("src/test/resources");

    private TableView<T> currentTable;

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

    @FXML
    private TextArea prefs;
    @FXML
    private TextArea cccam;
    @FXML
    private TextArea logs;

    /**
     * The constructor.
     * The constructor is called before the initialize() method.
     */
    public FXMLMainController() {}

    public void init(ObservableList<T> serviceData, TableView<T> table, TableColumn<T, Integer> idx, TableColumn<T, String> name, TableColumn<T, String> type, TableColumn<T, String> ppr) {

        table.setEditable(true);
        idx.setCellValueFactory(cellData -> cellData.getValue().idxProperty().asObject());
        name.setCellValueFactory(cellData -> cellData.getValue().nameProperty());
        name.setEditable(true);
        type.setCellValueFactory(cellData -> cellData.getValue().typeProperty());
        // nid.setCellValueFactory(cellData -> cellData.getValue().nidProperty().asObject());
        ppr.setCellValueFactory(cellData -> cellData.getValue().pprProperty());
        // newCol.setCellValueFactory(cellData -> cellData.getValue().neewProperty());

        // Context menu
        table.setRowFactory(tableView -> {
            final TableRow<T> row = new TableRow<>();
            final ContextMenu rowMenu = new ContextMenu();

            final MenuItem removeItem = new MenuItem("Delete");
            removeItem.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    final T service = row.getItem();
                    serviceData.removeAll(service);
                }
            });
            
            rowMenu.getItems().addAll(removeItem);
            row.contextMenuProperty().bind(Bindings.when(Bindings.isNotNull(row.itemProperty()))
                    .then(rowMenu)
                    .otherwise((ContextMenu) null));

            return row;
        });
    
        ppr.setCellFactory(col -> {
            final TableCell<T, String> cell = new TableCell<>();
            
            cell.textProperty().bind(cell.itemProperty());
            cell.itemProperty().addListener((obs, oldValue, newValue) -> {
                if (newValue != null) {
                    final ContextMenu cellMenu = new ContextMenu();
                    for (String pref: Utils.prefTab) {
                        final CheckMenuItem prefMenuItem = new CheckMenuItem(pref);

                        if (Utils.isPreferenceOn(cell.getText(), pref)) {
                            prefMenuItem.setSelected(true);
                        }

                        prefMenuItem.selectedProperty().addListener((obs1, old_val, new_val) -> {
                            final String new_ppr;
                            final T service = (T) cell.getTableRow().getItem();

                            if (new_val) {
                            	new_ppr = Utils.add_ppr(cell.getText(), pref);
                            } else {
                                new_ppr = Utils.remove_ppr(cell.getText(), pref);
                            }

                            service.setPpr(new_ppr);
                            service.setFlag(true);
                        });

                        cellMenu.getItems().add(prefMenuItem);
                        cell.setContextMenu(cellMenu);
                    }
                } else {
                    cell.setContextMenu(null);
                }
            });
            return cell;
        });

        // Editable service name
        name.setCellFactory(p -> new EditingCell());

        name.setOnEditCommit(t -> {

            final T service = t.getTableView().getItems().get(t.getTablePosition().getRow());
            service.setName(t.getNewValue());
            service.setFlag(true);
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        pi.setVisible(false);

        // test
        FilteredList<T> filteredDataS2 = new FilteredList<>(serviceDataS2);
        FilteredList<T> filteredDataT2 = new FilteredList<>(serviceDataT2);

        s_name.textProperty().addListener(obs -> filteredDataS2.setPredicate(getPredicate(obs)));
        s_name.textProperty().addListener(obs -> filteredDataT2.setPredicate(getPredicate(obs)));

        sortedDataS2 = new SortedList<>(filteredDataS2);
        sortedDataT2 = new SortedList<>(filteredDataT2);
        sortedDataS2.comparatorProperty().bind(serviceDVBS2Table.comparatorProperty());
        sortedDataT2.comparatorProperty().bind(serviceDVBT2Table.comparatorProperty());

        init(serviceDataS2, serviceDVBS2Table, s_idxColumnS2, s_nameColumnS2, s_typeColumnS2, s_pprColumnS2);
        init(serviceDataT2, serviceDVBT2Table, s_idxColumnT2, s_nameColumnT2, s_typeColumnT2, s_pprColumnT2);

        logs.setEditable(false);
        disableComponents(true);
    }

    private Predicate<T> getPredicate(Observable filter) {

    	return (s -> {
        	if (filter == null || !(filter instanceof StringProperty)) {
        		return true;
        	}

        	String lower = ((StringProperty) filter).getValue().toLowerCase();
        	return (lower.isEmpty() || s.getName().toLowerCase().contains(lower) || (s.getIdx() + "").toLowerCase().contains(lower));
        });
    }

    @FXML
    private void openAction(ActionEvent event) throws Exception {

        fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
	    fileChooser.setTitle("Choose the DTV backup folder");
        File rootFolder = fileChooser.showDialog(tabPane.getScene().getWindow());

        if (rootFolder != null) {

            currentDir = rootFolder;
            initFiles(rootFolder);
            AbstractReader<T> reader;
			serviceDataS2.clear();
			serviceDataT2.clear();

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
    	String msg;

    	for (File dtvFile: files) {
			if (dtvFile.getName().equalsIgnoreCase(DVB_S_MW_S1)) {
				dvbs2File = dtvFile;
				msg = "Using " + dvbs2File;
				LOG.info(msg);
				logs.appendText("\n" + msg);
			} else if (dtvFile.getName().equalsIgnoreCase(DVB_T_MW_S1)) {
				dvbt2File = dtvFile;
				msg = "Using " + dvbt2File;
				LOG.info(msg);
				logs.appendText("\n" + msg);
			} else if (dtvFile.getName().equalsIgnoreCase(DTV_PREFS)) {
				prefsFile = dtvFile;
				msg = "Using " + prefsFile;
				LOG.info(msg);
				logs.appendText("\n" + msg);
				try {
					prefs.setText(getPreferences());
				} catch (Exception e) {}

			} else if (dtvFile.getName().equalsIgnoreCase(CCCAM_CFG)) {
				cccamFile = dtvFile;
				msg = "Using " + cccamFile;
				LOG.info(msg);
				logs.appendText("\n" + msg);
			}
		}
	}

	private String getPreferences() throws Exception {
		Scanner scanner = new Scanner(prefsFile);
		String line;

		while (scanner.hasNext()) {
			line = scanner.nextLine();
			if (line.contains(FAV_TAG)) {
				scanner.close();
				String preferences = line.substring(1 + line.indexOf(">"), line.lastIndexOf("<"));
				prefs.setText(preferences);
				Utils.prefTab = preferences.split(FAV_SEP);
			}
		}

		scanner.close();

		return null;
	}

	private AbstractReader<T> getReader(File dvbFile) {

		String fileName = dvbFile.getName();
		AbstractReader<T> reader = null;

    	if (fileName.toLowerCase().startsWith(DVB_S_MW_S1)) {
    		reader = (AbstractReader<T>) dvbs2reader;
    		reader.setDvbFile(dvbFile);

    	} else if (fileName.toLowerCase().startsWith(DVB_T_MW_S1)) {
    		reader = (AbstractReader<T>) dvbt2reader;
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
        writer.setServices(serviceData);
        writer.setFileVersion(getReader(dvbFile).getFileVersion());
        handleTask(writer, dvbFile.getParentFile().getName(), "Save services " + dvbFile.getName());
	}

	@FXML
	private void saveAction(ActionEvent event) throws Exception {

	    fileChooser.setInitialDirectory(currentDir.exists() ? currentDir : null);
	    fileChooser.setTitle("Choose the folder to where store the files");
	    File rootFolder = fileChooser.showDialog(tabPane.getScene().getWindow()); // SaveDialog(serviceTable.getScene().getWindow());

	    if (rootFolder != null) {
	        currentDir = rootFolder;
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

    @FXML
	private void duplicateAction(ActionEvent event) throws Exception {

    	String action = "Remove duplicated services";
    	ObservableList<T> tableData = getData();
		if (tableData != null) {
			duplicate.setServices(tableData);
			handleTask(duplicate, null, action);
		}
	}

	@FXML
    private void sortAction(ActionEvent event) {

		ObservableList<T> tableData = getData();
		if (tableData != null) {
			currentTable.getSortOrder().clear();
			tableData.setAll(currentTable.getItems());
			Comparator<T> comparator = (channel1, channel2) -> (order * channel1.compareTo(channel2));
			Collections.sort(tableData, comparator);
			Utils.initIds(tableData);
			order = -order;
		}
    }

    private ObservableList<T> getData() {

		if (tabPane.getSelectionModel().isSelected(0)) {
			currentTable = serviceDVBS2Table;
	    	return serviceDataS2; 
		}

		if (tabPane.getSelectionModel().isSelected(1)) {
			currentTable = serviceDVBT2Table;
	    	return serviceDataT2; 
		}

		return null;
    }

    private void handleTask(Service<List<T>> task, String zeTitle, String action) throws Exception {

        pi.visibleProperty().bind(task.runningProperty());
        pi.progressProperty().bind(task.progressProperty());

        task.setOnSucceeded(t -> {
            // print services into tableview
    		List<T> dvbServices = task.getValue();
    		if (dvbServices != null) {
        		if (action.contains(DVB_S2)) {
            		serviceDataS2.setAll(dvbServices);
        			serviceDVBS2Table.setItems(sortedDataS2);
        		} else if (action.contains(DVB_T2)) {
            		serviceDataT2.setAll(dvbServices);
            		serviceDVBT2Table.setItems(sortedDataT2);
        		}
        	}

            if (zeTitle != null) title.setText(zeTitle);
            disableComponents(false);
        });

        task.setOnFailed(t -> {
        	Message.errorMessage("Error " + action + "\n" + task.getException().getMessage());
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
