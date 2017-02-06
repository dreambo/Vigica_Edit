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
package vigica.tools.writer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBS2Service;
import vigica.service.DVBS2DBService;
import vigica.service.DVBDBService;

/**
 *
 * @author bnabi
 */
@Component
public class DVBS2Writer extends DVBWriter<DVBS2Service> {

	@Autowired
	private DVBS2DBService bdd;
	private File dvbFile;

	private List<Byte> fileVersion = Arrays.asList((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C); // file version 4 bytes

	@Override
    public void setDvbFile(File dvbFile) {
    	this.dvbFile = dvbFile;
    }

	@Override
	public File getDvbFile() {
		return dvbFile;
	}

	@Override
    public DVBDBService<DVBS2Service> getServiceDB() {
    	return (DVBDBService<DVBS2Service>) bdd;
    }

	@Override
    public void setFileVersion(List<Byte> fileVersion) {
    	this.fileVersion = fileVersion;
    }

	@Override
    public List<Byte> getFileVersion() {
    	return fileVersion;
    }
}
