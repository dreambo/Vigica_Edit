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
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.IDBService;

/**
 *
 * @author bnabi
 */
@Component
public class Generate_mw_s1 extends DVBWriter {

	@Autowired
    private IDBService bdd;
	private File dvbFile;
	private List<Byte> fileVersion = Arrays.asList((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C); // file version 4 bytes

	public Generate_mw_s1() {}

	@Override
    public void setDvbFile(File dvbFile) {
    	this.dvbFile = dvbFile;
    }

	@Override
    public void setFileVersion(List<Byte> fileVersion) {
    	this.fileVersion = fileVersion;
    }

	/**
     * compress this list of dvb services into a the file dvbFile
     * @param chemin
     */
	@Override
    public void compress(List<DVBService> services) throws Exception {

    	List<Byte> satservices = new ArrayList<>();
    	List<Byte> sdata;

        for (DVBService service : services) {
            if (!service.getFlag()) {
                sdata = ByteUtils.hexStringToBytes(service.getLine());
                satservices.addAll(sdata);

            } else {

            	sdata = ByteUtils.hexStringToBytes(service.getLine());
                int entryLength = sdata.size();
                List<Byte> prefs = getPpr(service.getPpr());
                sdata.set(entryLength - 10, prefs.get(0));
                sdata.set(entryLength -  9, prefs.get(1));

                //sdata.set(rcdlen - 8, (byte) 0x01);
                List<Byte> entryName = new ArrayList<>();

                for (byte c : service.getName().getBytes("UTF-8")) {
                    entryName.add(c);
                }

                byte nameBegin  = (byte) 0x01;
                byte nameLength = (byte) entryName.size();
                List<Byte> filler = sdata.subList(2, 2 + 3); // 3 bytes
                byte rcdnamel   = sdata.get(1);
                List<Byte> payload = sdata.subList(rcdnamel + 5, sdata.size());

                List<Byte> entry = new ArrayList<>();
                entry.add(nameBegin);
                entry.add(nameLength);
                entry.addAll(filler);
                entry.addAll(entryName);
                entry.addAll(payload);

                satservices.addAll(entry);
            }
        }

        List<Byte> servicesCount = Arrays.asList(ByteUtils.int2ba(services.size())); // 4 bytes
        List<Byte> servicesData  = new ArrayList<>();

        servicesData.addAll(fileVersion);
        servicesData.addAll(servicesCount);
        servicesData.addAll(satservices);

        //CRC32 crc = new CRC32();
        CRC32_mpeg crc = new CRC32_mpeg();
        servicesData.forEach(crc::update);

        String crcresult = crc.getValue();
        crcresult = ("00000000" + crcresult).substring(crcresult.length());
        List<Byte> crcbyte = ByteUtils.hexStringToBytes(crcresult);
        
        List<Byte> mw_s1 = new ArrayList<>();
        List<Byte> flbytes = Arrays.asList(ByteUtils.int2ba(12 + satservices.size())); // 4 crc + 4 type + 4 service count + all service records

        mw_s1.addAll(flbytes);
        mw_s1.addAll(crcbyte);
        mw_s1.addAll(servicesData);

        ByteUtils.writeBytesToFile(mw_s1, dvbFile);
    }

    private List<Byte> getPpr(String preference) {

    	List<Byte> ppr = new ArrayList<>(2);
        ppr.add((byte) 0x00);
        ppr.add((byte) 0x00);

        if (preference.length() != 0) {
            for (String perf : preference.split("-")) {

                if (Integer.valueOf(perf) <= 8) {
                    Double pos = Math.pow(2, Integer.valueOf(perf) - 1);
                    byte temp = (byte) (ppr.get(1) | pos.byteValue());
                    ppr.set(1, temp);
                } else {
                    Double pos = Math.pow(2, Integer.valueOf(perf) - 8 - 1);
                    byte temp = (byte) (ppr.get(0) | pos.byteValue());
                    ppr.set(0, temp);
                }
            }
        }

        return ppr;
    }

	@Override
	protected Task<List<DVBService>> createTask() {
		return new Task<List<DVBService>>() {
			@Override
			protected List<DVBService> call() throws Exception {
	            updateProgress(-1, 0);
	            compress(bdd.read_bdd());
	            updateProgress(1, 1);

	            return null;
			}
		};
	}
}
