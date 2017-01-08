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
package vigica_edit;

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
import vigica_edit.model.Service;
import vigica_edit.view.FXMLCompareController;

/**
 *
 * @author bnabi
 */
public class Compare_mw_s1 {
    public List<Service> servicesLost = new ArrayList<>();
    private Decompress_mw_s1 decompress = new Decompress_mw_s1();
    private Service_BDD bdd = new Service_BDD();
    public CompareTask compareTask;
    
    private List<Service> getLostServices() {
        return servicesLost;
    }
    
    public Compare_mw_s1 () {
        compareTask = new CompareTask();
    }
    
    private void detectNew(List<Service> services, List<Service> servicesOld) throws Exception {
        Boolean isNew;
        
        try {
            for (Service service : services) {
                isNew = true;
                String line;
                String lineOld;

                for (Service serviceOld : servicesOld) {
                    line = service.getS_name();
                    lineOld = serviceOld.getS_name();

                    if (line.equalsIgnoreCase(lineOld)) {
                        isNew = false;
                        break;
                    }
                }
                if (isNew)
                    service.setS_new("N");
            }
        }catch (Exception e) {
            throw new Exception(e.getCause().getMessage());
        }
    }
    
    private void integratePPR(List<Service> services, List<Service> servicesOld) throws Exception {
        Boolean isFind;
        servicesLost.clear();
        
        try {
            for (Service serviceOld : servicesOld) {
                isFind = false;
                String line;
                String lineOld;

                if (serviceOld.getS_ppr().length() != 0) {
                    for (Service service : services) {
                        line = service.getS_name();
                        lineOld = serviceOld.getS_name();

                        if (line.equalsIgnoreCase(lineOld)) {
                            service.setS_ppr(serviceOld.getS_ppr());
                            service.setS_flag(true);
                            isFind = true;
                            break;
                        }
                    }
                }
                else
                    isFind = true;

                if (!isFind) {
                    servicesLost.add(new Service(serviceOld.getS_type(), serviceOld.getS_idx(), serviceOld.getS_name(), serviceOld.getS_nid(), serviceOld.getS_ppr(), serviceOld.getS_line(), serviceOld.getS_flag(), serviceOld.getS_new()));
                }
            }
        }catch (Exception e) {
            throw new Exception(e.getCause().getMessage());
        }
    }
    
    private void showOldPPR(Stage primaryStage, List<Service> services) {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Vigica_Edit.class.getResource("view/FXMLCompare.fxml"));
            AnchorPane rootLayout = (AnchorPane) loader.load();

            FXMLCompareController t1 = (FXMLCompareController)loader.getController();
            t1.setServices(services);

            Stage modal_dialog = new Stage(StageStyle.DECORATED);
            modal_dialog.initModality(Modality.NONE);
            modal_dialog.initOwner(primaryStage);
            modal_dialog.setTitle("Lost Preferences");
            modal_dialog.getIcons().add(new Image(getClass().getResourceAsStream("app_icon.png")));

            // Show the scene containing the root layout.
            Scene scene = new Scene(rootLayout);
            modal_dialog.setScene(scene);
            modal_dialog.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public class CompareTask extends Task<List<Service>> {

        private String chemin;
        Stage stage;
        
        public String getChemin() {
            return this.chemin;
        }

        public void setChemin(String chemin) {
            this.chemin = chemin;
        }
        
        public String getStage() {
            return this.chemin;
        }

        public void setStage(Stage stage) {
            this.stage = stage;
        }

        @Override
        protected List<Service> call() throws Exception {
            List<Service> services;
            List<Service> servicesOld;
            List<Service> servicesNew;
            int count = 0;

            updateProgress(-1, 0);
            decompress.decompress(chemin);
            servicesOld = decompress.getServices();

            String sql = "FROM Service ";
            services = bdd.read_bdd(sql);

            detectNew(services, servicesOld);
            integratePPR(services, servicesOld);
            servicesNew = getLostServices();
            bdd.truncate_bdd();
            for(Service service : services){
                count++;
                updateProgress(count, services.size());
                bdd.save_bdd(service);
            }

            Platform.runLater(() -> showOldPPR(stage, servicesNew));

            return services;
        };
    }
}
