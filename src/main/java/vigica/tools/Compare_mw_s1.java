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
package vigica.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.IDBService;
import vigica.view.FXMLCompareController;

/**
 *
 * @author bnabi
 */
@Component
public class Compare_mw_s1 {

	public List<DVBService> servicesLost = new ArrayList<>();
    public CompareTask compareTask;

    @Autowired
    private Decompress_mw_s1 decompress;
    @Autowired
    private IDBService bdd;

    private List<DVBService> getLostServices() {
        return servicesLost;
    }
    
    public Compare_mw_s1() {
        compareTask = new CompareTask();
    }
    
    private void detectNew(List<DVBService> services, List<DVBService> servicesOld) throws Exception {

    	Boolean isNew;
        String name;
        String oldName;

        for (DVBService service : services) {
            isNew = true;

            for (DVBService serviceOld : servicesOld) {
            	name = service.getName();
            	oldName = serviceOld.getName();

                if (name.equals(oldName)) {
                    isNew = false;
                    break;
                }
            }

            if (isNew) {
                service.setNeew("N");
            }
        }
    }

    private void integratePPR(List<DVBService> services, List<DVBService> servicesOld) throws Exception {
        Boolean isFind;
        servicesLost.clear();
        
        try {
            for (DVBService serviceOld : servicesOld) {
                isFind = false;
                String line;
                String lineOld;

                if (serviceOld.getPpr().length() != 0) {
                    for (DVBService service : services) {
                        line = service.getName();
                        lineOld = serviceOld.getName();

                        if (line.equalsIgnoreCase(lineOld)) {
                            service.setPpr(serviceOld.getPpr());
                            service.setFlag(true);
                            isFind = true;
                            break;
                        }
                    }
                }
                else
                    isFind = true;

                if (!isFind) {
                    servicesLost.add(new DVBService(serviceOld.getType(), serviceOld.getIdx(), serviceOld.getName(), serviceOld.getNid(), serviceOld.getPpr(), serviceOld.getLine(), serviceOld.getFlag(), serviceOld.getNeew()));
                }
            }
        }catch (Exception e) {
            throw new Exception(e.getCause().getMessage());
        }
    }
    
    private void showOldPPR(Stage primaryStage, List<DVBService> services) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/vigica/view/FXMLCompare.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();

            FXMLCompareController t1 = (FXMLCompareController)loader.getController();
            t1.setServices(services);

            Stage modal_dialog = new Stage(StageStyle.DECORATED);
            modal_dialog.initModality(Modality.NONE);
            modal_dialog.initOwner(primaryStage);
            modal_dialog.setTitle("Lost Preferences");
            modal_dialog.getIcons().add(new Image(getClass().getResourceAsStream("/app_icon.png")));

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            modal_dialog.setScene(scene);
            modal_dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public class CompareTask extends Task<List<DVBService>> {

        private File chemin;
        Stage stage;
        
        public File getChemin() {
            return this.chemin;
        }

        public void setChemin(File chemin) {
            this.chemin = chemin;
        }
        
        public String getStage() {
            return this.chemin.getAbsolutePath();
        }

        public void setStage(Stage stage) {
            this.stage = stage;
        }

        @Override
        protected List<DVBService> call() throws Exception {
            List<DVBService> services;
            List<DVBService> servicesOld;
            List<DVBService> servicesNew;
            int count = 0;

            updateProgress(-1, 0);
            decompress.decompress(chemin);
            servicesOld = decompress.getServices();

            services = bdd.read_bdd();

            detectNew(services, servicesOld);
            integratePPR(services, servicesOld);
            servicesNew = getLostServices();

            for(DVBService service : services){
                count++;
                updateProgress(count, services.size());
                bdd.save_bdd(service);
            }

            Platform.runLater(() -> showOldPPR(stage, servicesNew));

            return services;
        };
    }
}
