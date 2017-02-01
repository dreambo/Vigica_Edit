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
public class Generate_mw_s1 extends DVBCompressor {

	@Autowired
    private IDBService bdd;
	private File dvbFile;

	public Generate_mw_s1() {}

    public void setDvbFile(File dvbFile) {
    	this.dvbFile = dvbFile;
    }

	/**
     * compress this list of dvb services into a the file dvbFile
     * @param chemin
     */
    public void compress(List<DVBService> services) throws Exception {

    	List<Byte> satservices = new ArrayList<>();

        for (DVBService service : services) {
            if (!service.getFlag()) {
                List<Byte> sdata = ByteUtils.hexStringToBytes(service.getLine());
                satservices.addAll(sdata);

            } else {
            	List<Byte> sdata = ByteUtils.hexStringToBytes(service.getLine());
                int rcdlen = sdata.size();
                List<Byte> prefba = getppr(service.getPpr());
                sdata.set(rcdlen-10, prefba.get(0));
                sdata.set(rcdlen-9, prefba.get(1));

                //sdata.set(rcdlen - 8, (byte) 0x01);
                List<Byte> newn = new ArrayList<>();

                for (byte c : service.getName().getBytes("UTF-8")) {
                    newn.add(c);
                }

                int newl = newn.size();
                int rcdnamel = Integer.valueOf(sdata.get(1));
                List<Byte> filler = sdata.subList(2, 2 + 3);
                List<Byte> payload = sdata.subList(rcdnamel + 5, sdata.size());

                List<Byte> newrec = new ArrayList<>();
                newrec.add((byte) 0x01);
                newrec.add((byte) newl);
                newrec.addAll(filler);
                newrec.addAll(newn);
                newrec.addAll(payload);

                satservices.addAll(newrec);
            }
        }

        List<Byte> ffbytes = Arrays.asList((byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C); // 4 bytes
        List<Byte> snbytes = Arrays.asList(ByteUtils.int2ba(services.size())); // 4 bytes

        List<Byte> bindata = new ArrayList<>();
        bindata.addAll(ffbytes);
        bindata.addAll(snbytes);
        bindata.addAll(satservices);

        //CRC32 crc = new CRC32();
        CRC32_mpeg crc = new CRC32_mpeg();
        bindata.forEach(crc::update);

        String crcresult = crc.getValue();
        crcresult = ("00000000" + crcresult).substring(crcresult.length());
        List<Byte> crcbyte = ByteUtils.hexStringToBytes(crcresult);
        
        List<Byte> mw_s1 = new ArrayList<>();
        int fl = satservices.size() + 12; // 4 crc + 4 type + 4 service count + all service records
        List<Byte> flbytes = Arrays.asList(ByteUtils.int2ba(fl));

        mw_s1.addAll(flbytes);
        mw_s1.addAll(crcbyte);
        mw_s1.addAll(bindata);

        ByteUtils.writeBytesToFile(mw_s1, dvbFile);
    }

    private List<Byte> getppr(String preference) {

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
	protected Task<Void> createTask() {
		return new Task<Void>() {
			@Override
			protected Void call() throws Exception {
	            updateProgress(-1, 0);
	            compress(bdd.read_bdd());
	            updateProgress(1, 1);

	            return null;
			}
		};
	}
}
