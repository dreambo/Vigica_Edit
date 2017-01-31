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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.IDBService;
import vigica.view.Error_Msg;

/**
 * Util class for file decomposition
 * 
 * @author nabillo
 */
@Component
public class Decompress_mw_s1 extends Service<List<DVBService>> {

	private static final Logger LOG = Logger.getLogger(Decompress_mw_s1.class);

	private final byte[] endpatt = {(byte) 0x00, (byte) 0x00, (byte) 0x3f, (byte) 0xff};
    static private Error_Msg error_msg = new Error_Msg();
    private File dvbFile;

    @Autowired
    private IDBService bdd;
    public DuplicateTask duplicateTask = new DuplicateTask();

    private List<DVBService> services = new ArrayList<>();

    public List<DVBService> getServices() {
        return services;
    }

    public Decompress_mw_s1() {}

    public void setDvbFile(File dvbFile) {
        this.dvbFile = dvbFile;
    }

	/**
     *
     * @param file
     * @throws java.lang.Exception
     */
    public void decompress(File file) throws Exception {

    	byte[] bindata;
        services.clear();

        try {
            Path binfile = Paths.get(file.getAbsolutePath());
            bindata = Files.readAllBytes(binfile);
            int binl = bindata.length;
            byte[] givl_s = Arrays.copyOfRange(bindata, 0, 4);
            int givl_d = ByteBuffer.wrap(givl_s).getInt() + 4;
            if (binl - givl_d != 0)
                error_msg.Error_diag("length of input binary file \\n differs from length given in that file!");

            byte[] recd_nmbr_s = Arrays.copyOfRange(bindata, 12, 16);
            int recd_nmbr_d = ByteBuffer.wrap(recd_nmbr_s).getInt();
            int recd_idx = 1;
            int bind_idx = 16;
            while (recd_idx <= recd_nmbr_d) {
                int nxt_idx = find_end(bindata, bind_idx);
                byte[] binrcd = Arrays.copyOfRange(bindata, bind_idx, nxt_idx);
                int rcdlen = nxt_idx - bind_idx;
                // create record file name
                byte rcdnamel = binrcd[1];
                byte[] rcdname = Arrays.copyOfRange(binrcd, 5, Byte.toUnsignedInt(rcdnamel) + 5);

                // start the filename with R | TV | ? to indicate the radio/TV/unknown service type
                String stype = "U"; // unknown
                if (binrcd[rcdlen - 17] == 0x00) // this byte in fixed distance 17 back from next record has TV/Radio
                    stype = "TV";
                else if (binrcd[rcdlen - 17] == 0x01)
                    stype = "R";

                byte[] nid_s = Arrays.copyOfRange(binrcd, rcdlen - 26, rcdlen - 24); // also fixed distance back from end
                int nid_d = getInt(nid_s); // make the two bytes into an integer
                byte[] ppr = Arrays.copyOfRange(binrcd, rcdlen - 10, rcdlen - 8); // preference setting
                String ppr_s = getPreference(ppr);

                // add the network number and preference setting to the end of the file name
                String rcdname_s = new String(rcdname, "UTF-8");
                String binrcd_s = bytesToHexString(binrcd);
//            String asciiname = stype + "~" + recd_idx + "~" + rcdname_s + "~E0~" + "N" + nid_d + "~" + "P" + ppr_s;
                services.add(new DVBService(stype, recd_idx, rcdname_s, nid_d, ppr_s,binrcd_s, false, ""));
                recd_idx++;
                bind_idx = nxt_idx;
            }
        } catch (Exception e) {
            throw new Exception(e.getCause().getMessage());
        }
    }

    private static String bytesToHexString(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    private int find_end(byte[] bindata, int strt) {
        int sidx;
        for (sidx = strt; sidx <strt+1000; sidx++)
        {   
            if (Arrays.equals(Arrays.copyOfRange(bindata, sidx, sidx+4), endpatt))
                break;
        }
        return sidx+4;
    }
    
    private int getInt(byte[] in) {
        int val = ((in[0] & 0xff) << 8) | (in[1] & 0xff);;
        return val;
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

    public static Boolean isPreferenceOn(String ppr_s, int index) {
        Boolean isOk = false;
        for (String ppr: ppr_s.split("-")) {
            if (ppr.equals(String.valueOf(index))) {
                isOk = true;
            }
        }

        return isOk;
    }

    public static String add_ppr(String old_ppr, int new_Value) {
        String new_ppr = "";
        Boolean isFirst = true;
        Boolean isAdded = false;

        // not preference yet
        if (old_ppr.length() == 0) {
            return old_ppr += new_Value;
        }

        for (String ppr: old_ppr.split("-")) {
            // still not in position
            if ((Integer.valueOf(ppr) < new_Value)) {
                if (isFirst) {
                    new_ppr += ppr;
                    isFirst = false;
                } else {
                    new_ppr += "-" + ppr;
                }
            // here we are
            } else if ((Integer.valueOf(ppr) > new_Value) && !isAdded) {
                // expect for 1 to not have a -
                if (new_Value == 1) {
                    new_ppr += new_Value + "-" + ppr;
                } else {
                    new_ppr += "-" + new_Value + "-" + ppr;
                }

                isAdded = true;
            // the rest of the line
            } else {
                new_ppr += "-" + ppr;
            }
        }

        // if new value is the last one we added it manualy
        if (!isAdded) {
            new_ppr += "-" + new_Value;
        }

        return new_ppr;
    }
    
    public static String remove_ppr(String old_ppr, int new_Value) {
        String new_ppr = "";
        Boolean isFirst = true;
        
        for (String ppr: old_ppr.split("-")) {
            if (Integer.valueOf(ppr) == new_Value) {
                continue;
            }

            if (isFirst) {
                new_ppr += ppr;
                isFirst = false;
            } else {
                new_ppr += "-" + ppr;
            }
        }

        return new_ppr;
    }

    public class DuplicateTask2 extends Task<Set<DVBService>> {

        ObservableList<DVBService> services = FXCollections.observableArrayList();

        public void setServices(ObservableList<DVBService> services) {
            this.services = services;
        }

        @Override
        protected Set<DVBService> call() throws Exception {
        	Set<DVBService> servicesUnique = new HashSet<>();
        	servicesUnique.addAll(services);

            updateProgress(-1, 0);

            // Add to database
            bdd.save_bdd(servicesUnique);

            return servicesUnique;
        }
    }

    public class DuplicateTask extends Task<List<DVBService>> {

        ObservableList<DVBService> services = FXCollections.observableArrayList();

        public void setServices(ObservableList<DVBService> services) {
            this.services = services;
        }

        @Override
        protected List<DVBService> call() throws Exception {
            List<DVBService> servicesUnique = new ArrayList<>();

            updateProgress(-1, 0);
            List<String> uniqueIds = new ArrayList<>();

            int i = 0;

            for (DVBService service: services) {
                if (!uniqueIds.contains(service.getName()) || !service.getPpr().isEmpty()) {
                    service.setIdx(++i);
                    servicesUnique.add(service);
                    uniqueIds.add(service.getName());
                }
            }

            // Add to database
            bdd.save_bdd(servicesUnique);

            return servicesUnique;
        }
    }

	@Override
	protected Task<List<DVBService>> createTask() {
		return new Task<List<DVBService>>() {
			@Override
			protected List<DVBService> call() throws Exception {
	            int countOK = 0;
	            int countKO = 0;

	            updateProgress(-1, 0);
	            decompress(dvbFile);

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
