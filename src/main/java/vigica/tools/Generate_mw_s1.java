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
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Task;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import vigica.model.DVBService;
import vigica.service.BeanFactory;
import vigica.service.IService;

/**
 *
 * @author bnabi
 */
@Component
public class Generate_mw_s1 {

	@Autowired
    private IService bdd = BeanFactory.getService();

	public GenerateTask generateTask;

    public Generate_mw_s1 () {
        generateTask = new GenerateTask();
    }
    
    /**
     *
     * @param chemin
     */
    public void compress(List<DVBService> services, File chemin) throws Exception {
        List<Byte> satservices = new ArrayList<>();

        for (DVBService service : services) {
            if (service.getFlag() == false) {
                List<Byte> sdata = hexStringToBytes(service.getLine());
                satservices.addAll(sdata);
            } else {
            	List<Byte> sdata = hexStringToBytes(service.getLine());
                int rcdlen = sdata.size();
                List<Byte> prefba = getppr(service.getPpr());
                sdata.set(rcdlen-10, prefba.get(0));
                sdata.set(rcdlen-9, prefba.get(1));

                //sdata.set(rcdlen - 8, (byte) 0x01);
                List<Byte> newn = new ArrayList<>();
                
                for (byte c : service.getName().getBytes("UTF-8"))
                    newn.add(c);
                
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

        Byte[] ffbytes = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x0C};
        List<Byte> ffbyte = new ArrayList<>(Arrays.asList(ffbytes));

        Byte[] snbytes = int2ba(services.size());
        List<Byte> snbyte = new ArrayList<>(Arrays.asList(snbytes));

        int fl = satservices.size() + 12; // 4 crc + 4 type + 4 service count + all service records
        Byte[] flbytes = int2ba(fl);
        List<Byte> flbyte = new ArrayList<>(Arrays.asList(flbytes));

        List<Byte> bindata = new ArrayList<>();
        bindata.addAll(ffbyte);
        bindata.addAll(snbyte);
        bindata.addAll(satservices);
        
        //CRC32 crc = new CRC32();
        CRC32_mpeg crc = new CRC32_mpeg();
        bindata.stream().forEach((bin) -> {
            crc.update(bin);
        });
        
        String crcresult = crc.getValue();
        crcresult = ("00000000" + crcresult).substring(crcresult.length());
        List<Byte> crcbyte = hexStringToBytes(crcresult);
        
        List<Byte> mw_s1 = new ArrayList<>();
        mw_s1.addAll(flbyte);
        mw_s1.addAll(crcbyte);
        mw_s1.addAll(bindata);
        FileOutputStream servicesf = new FileOutputStream(chemin);
        for (byte car: mw_s1)
            servicesf.write(car);
        servicesf.close();
        
    }

    private static List<Byte> hexStringToBytes(String s) {
        int len = s.length();
        byte[] temp = new byte[len / 2];
        List<Byte> data = new ArrayList<>(len / 2);
        for (int i = 0; i < len; i += 2) {
            temp[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
            data.add(temp[i / 2]);
        }
        
        return data;
    }

    private Byte[] int2ba(int integer) {
        Byte[] result = new Byte[4];

        result[0] = (byte) ((integer & 0xFF000000) >> 24);
        result[1] = (byte) ((integer & 0x00FF0000) >> 16);
        result[2] = (byte) ((integer & 0x0000FF00) >> 8);
        result[3] = (byte) (integer & 0x000000FF);

        return result;
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
    
    public class GenerateTask extends Task<Void> {

        private File chemin;
        
        public File getChemin() {
            return this.chemin;
        }

        public void setChemin(File chemin) {
            this.chemin = chemin;
        }

        @Override
        protected Void call() throws Exception {
            List<DVBService> services;
            int count = 0;

            updateProgress(-1, 0);
            services = bdd.read_bdd();

            compress(services, chemin);

            return null;
        };
    }
}
