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
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import org.springframework.stereotype.Component;

import vigica.Vigica;
import vigica.model.DVBService;
import vigica.service.DVBDBService;
import vigica.tools.reader.DVBReader;
import vigica.view.FXMLCompareController;

/**
 *
 * @author bnabi
 */
@Component
public class Compare_mw_s1<T extends DVBService> extends Service<List<T>> {

	public List<T> servicesLost = new ArrayList<>();
    private File dvbFile;

    // @Autowired
    private DVBReader<T> reader;
    // @Autowired
    private DVBDBService<T> bdd;
    // @Autowired
    private FXMLCompareController<T> compareController;

    private List<T> getLostServices() {
        return servicesLost;
    }

    public void setDvbFile(File dvbFile) {
    	this.dvbFile = dvbFile;
    }

    private void detectNew(List<T> services, List<T> servicesOld) throws Exception {

    	Boolean isNew;
        String name;
        String oldName;

        for (T service : services) {
            isNew = true;

            for (T serviceOld : servicesOld) {
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

    private void integratePPR(List<T> services, List<T> servicesOld) throws Exception {

    	Boolean isFind;
        servicesLost.clear();

        for (T serviceOld : servicesOld) {
            isFind = false;
            String line;
            String lineOld;

            if (serviceOld.getPpr().length() != 0) {
                for (T service : services) {
                    line = service.getName();
                    lineOld = serviceOld.getName();

                    if (line.equalsIgnoreCase(lineOld)) {
                        service.setPpr(serviceOld.getPpr());
                        service.setFlag(true);
                        isFind = true;
                        break;
                    }
                }
            } else {
                isFind = true;
            }

            if (!isFind) {
            	T service = (T) new DVBService(serviceOld.getType(), serviceOld.getIdx(), serviceOld.getName(), serviceOld.getNid(), serviceOld.getPpr(), serviceOld.getLine(), serviceOld.getFlag(), serviceOld.getNeew());
                servicesLost.add(service);
            }
        }
    }

    private void showOldPPR(List<T> services) {

        Stage modal_dialog = new Stage(StageStyle.DECORATED);
        modal_dialog.initModality(Modality.NONE);
        modal_dialog.setTitle("Lost Preferences");
        modal_dialog.getIcons().add(new Image(getClass().getResourceAsStream("/app_icon.png")));

        // Show the scene containing the root layout.
        AnchorPane rootLayout = (AnchorPane) Vigica.load("/vigica/view/FXMLCompare.fxml");
        compareController.setServices(services);
        Scene scene = new Scene(rootLayout);
        modal_dialog.setScene(scene);
        modal_dialog.show();
    }

	@Override
	protected Task<List<T>> createTask() {

		return new Task<List<T>>() {

			@Override
			protected List<T> call() throws Exception {
	            List<T> services;
	            List<T> servicesOld;
	            List<T> servicesNew;
	            int count = 0;

	            updateProgress(-1, 0);
	            reader.setDvbFile(dvbFile);
	            servicesOld = reader.decompress();

	            services = bdd.read_bdd();

	            detectNew(services, servicesOld);
	            integratePPR(services, servicesOld);
	            servicesNew = getLostServices();

	            for(T service : services){
	                count++;
	                updateProgress(count, services.size());
	                bdd.save_bdd(service);
	            }

	            Platform.runLater(() -> showOldPPR(servicesNew));

	            return services;
			}
		};
	}
}
