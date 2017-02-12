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
package dtv.tools;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import dtv.model.DVBChannel;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

/**
 * Util class for file decomposition
 * 
 * @author nabillo
 */
@Component
public class DuplicateFinder<T extends DVBChannel> extends Service<List<T>> {

	private static final Logger LOG = LoggerFactory.getLogger(DuplicateFinder.class);

	private ObservableList<T> services;

    public DuplicateFinder() {}

	public void setServices(ObservableList<T> services) {
        this.services = services;
    }

	@Override
	protected Task<List<T>> createTask() {

		return new Task<List<T>>() {
			@Override
			protected List<T> call() throws Exception {
	            List<T> servicesUnique = new ArrayList<>();

	            updateProgress(-1, 0);
	            List<String> uniqueIds = new ArrayList<>();
	            int i = 0;

	            for (T service: services) {
	                if (uniqueIds.contains(service.getName()) && service.getPpr().isEmpty()) {
	                	LOG.info("Service " + service + " duplicated!");
	                } else {
	                    service.setIdx(++i);
	                    servicesUnique.add(service);
	                    uniqueIds.add(service.getName());
	                }
	            }

	            return servicesUnique;
			}
		};
	}
}
