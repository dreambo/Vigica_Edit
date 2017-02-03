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
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.IDBService;
import vigica.view.Message;

/**
 * Util class for file decomposition
 * 
 * @author nabillo
 */
@Component
public class Decompress_mw_s1 extends Service<List<DVBService>> implements DVBReader {

	private static final Logger LOG = Logger.getLogger(Decompress_mw_s1.class);
	// private static final byte[] END_MAGIC = {(byte) 0x00, (byte) 0x00, (byte) 0x3F, (byte) 0xFF};
	private static final byte[] END_MAGIC = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};

    private File dvbFile;
    private List<Byte> fileVersion;

    @Autowired
    private IDBService bdd;

    public Decompress_mw_s1() {}

    @Override
	public void setDvbFile(File dvbFile) {
        this.dvbFile = dvbFile;
    }

    @Override
    public List<Byte> getFileVersion() {
    	return fileVersion;
    }

    @Override
	public List<DVBService> decompress() throws Exception {

    	byte[] bindata;
    	List<DVBService> services = new ArrayList<>();

        bindata = Files.readAllBytes(Paths.get(dvbFile.getAbsolutePath()));
        int binl = bindata.length;
        fileVersion = Arrays.asList((byte) 0x00, (byte) 0x00, (byte) 0x00, bindata[11]);
        byte[] givl_s = Arrays.copyOfRange(bindata, 0, 4);
        int givl_d = ByteBuffer.wrap(givl_s).getInt() + 4;

        if (binl != givl_d) {
        	Message.errorMessage("length of input binary file \\n differs from length given in that file!");
            return services;
        }

        byte[] recd_nmbr_s = Arrays.copyOfRange(bindata, 12, 16);
        int recd_nmbr_d = ByteBuffer.wrap(recd_nmbr_s).getInt();
        int recd_idx = 0;
        int bind_idx = 16;
        // int offset = (fileVersion.get(3) == 0x0E ? (4*16 + 2) : 0);
        int offset = (fileVersion.get(3) == 0x0E ? (4*16 + 3) : 0);

        while (recd_idx < recd_nmbr_d) try {
            int nxt_idx = ByteUtils.find_end(bindata, bind_idx, END_MAGIC);
            byte[] entry = Arrays.copyOfRange(bindata, bind_idx, nxt_idx + offset);
            int entryLength = nxt_idx - bind_idx;
            // create record file name
            byte[] entryName = Arrays.copyOfRange(entry, 5, Byte.toUnsignedInt(entry[1]) + 5);

            // start the filename with R | TV | ? to indicate the radio/TV/unknown service type
            String stype = "U"; // unknown
            if (entry[entryLength - 17] == 0x00) { // this byte in fixed distance 17 back from next record has TV/Radio
                stype = "TV";
            } else if (entry[entryLength - 17] == 0x01) {
                stype = "R";
            }

            byte[] nid_s = Arrays.copyOfRange(entry, entryLength - 26, entryLength - 24); // also fixed distance back from end
            int nid_d = ByteUtils.getInt(nid_s); // make the two bytes into an integer
            byte[] ppr = Arrays.copyOfRange(entry, entryLength - 10, entryLength - 8); // preference setting
            String ppr_s = getPreference(ppr);

            // add the network number and preference setting to the end of the file name
            String rcdname_s = new String(entryName, "UTF-8");
            String binrcd_s = ByteUtils.bytesToHexString(entry);
            // String asciiname = stype + "~" + recd_idx + "~" + rcdname_s + "~E0~" + "N" + nid_d + "~" + "P" + ppr_s;
            services.add(new DVBService(stype, ++recd_idx, rcdname_s, nid_d, ppr_s, binrcd_s, false, ""));
            bind_idx = nxt_idx + offset;

        } catch (Exception e) {
            throw new Exception(e.getCause().getMessage());
        }

        return services;
    }

    private String getPreference(byte[] ppr) {
        String ppr_s ="";
        Boolean isFirst = true;

        for (int i = 0; i < 8; i++) {
            if (((ppr[1] >> i) & 1) == 1) {
                if (isFirst) {
                    ppr_s += (i+1);
                    isFirst = false;

                } else {
                    ppr_s += "-" + (i+1);
                }
            }
        }

        for (int i = 9; i < 11; i++) {
            if (((ppr[0] >> (i-9)) & 1) == 1) {

            	if (i < 10) {
                    if (isFirst) {
                        ppr_s += (i);
                        isFirst = false;
                    } else {
                        ppr_s += "-" + (i);
                    }
                } else {
                    ppr_s += (i);
                }
            }
        }

        return ppr_s;
    }

	@Override
	protected Task<List<DVBService>> createTask() {
		
		return new Task<List<DVBService>>() {

			@Override
			protected List<DVBService> call() throws Exception {

				int countOK = 0;
	            int countKO = 0;

	            updateProgress(-1, 0);
	            List<DVBService> services = decompress();

	            // Add to database
	            for (DVBService service : services) try {
	                updateProgress(countOK + countKO, services.size());
	                bdd.save_bdd(service);
	                countOK++;
	            } catch(Exception e) {
	            	LOG.error("Error while saving services", e);
	                countKO++;
	            }

	            LOG.info("Services OK: " + countOK + " -- Services KO: " + countKO);
	            return services;
			}
		};
	}
}
