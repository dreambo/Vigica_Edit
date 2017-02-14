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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.springframework.stereotype.Component;

import dtv.model.DVBChannel;

/**
 * Util class for file decomposition
 * 
 * @author nabillo
 */
@Component
public class DuplicateRemover<T extends DVBChannel> extends Service<List<T>> {

	private ObservableList<T> services;

	public ObservableList<T> getServices() {
        return services;
    }

	public void setServices(ObservableList<T> services) {
        this.services = services;
    }

	@Override
	protected Task<List<T>> createTask() {

		return new Task<List<T>>() {
			@Override
			protected List<T> call() throws Exception {
	            Set<T> serviceSet = new LinkedHashSet<T>(services);
	            updateProgress(-1, 0);

	            services.setAll(serviceSet);
	            Utils.initIds(services);

	            return services;
			}
		};
	}
}
