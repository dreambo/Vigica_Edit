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
package dtv.tools.writer;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import dtv.database.DVBDBService;
import dtv.database.DVBT2DBService;
import dtv.model.DVBT2Channel;

/**
 *
 * @author bnabi
 */
@Component
public class DVBT2Writer extends DVBWriter<DVBT2Channel> {

	@Autowired
	private DVBT2DBService bdd;
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
    public DVBDBService<DVBT2Channel> getServiceDB() {
    	return (DVBDBService<DVBT2Channel>) bdd;
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
