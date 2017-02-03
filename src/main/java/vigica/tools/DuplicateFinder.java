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

import java.util.ArrayList;
import java.util.List;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.IDVBDBService;

/**
 * Util class for file decomposition
 * 
 * @author nabillo
 */
@Component
public class DuplicateFinder extends Service<List<DVBService>> {

	private static final Logger LOG = Logger.getLogger(DuplicateFinder.class);

    // @Autowired
    private IDVBDBService<DVBService> bdd;
    private ObservableList<DVBService> services;

    public DuplicateFinder() {}

	public void setServices(ObservableList<DVBService> services) {
        this.services = services;
    }

	@Override
	protected Task<List<DVBService>> createTask() {
		
		return new Task<List<DVBService>>() {
			@Override
			protected List<DVBService> call() throws Exception {
	            List<DVBService> servicesUnique = new ArrayList<>();

	            updateProgress(-1, 0);
	            List<String> uniqueIds = new ArrayList<>();
	            int i = 0;

	            for (DVBService service: services) {
	                if (uniqueIds.contains(service.getName()) && service.getPpr().isEmpty()) {
	                	LOG.info("Service " + service + " duplicated!");
	                } else {
	                    service.setIdx(++i);
	                    servicesUnique.add(service);
	                    uniqueIds.add(service.getName());
	                }
	            }

	            // Add to database
	            bdd.save_bdd(servicesUnique);

	            return servicesUnique;
			}
		};
	}
}
