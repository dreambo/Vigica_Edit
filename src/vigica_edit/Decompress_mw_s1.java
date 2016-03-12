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
package vigica_edit;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import vigica_edit.view.Error_Msg;
import vigica_edit.model.Service;

/**
 * Util class for file decomposition
 * 
 * @author nabillo
 */
public class Decompress_mw_s1 {
    
    private final byte[] endpatt = {(byte) 0x00, (byte)0x00, (byte)0x3f, (byte)0xff};
    private byte[] bindata;
    static private Error_Msg error_msg = new Error_Msg();
    private Service service;
    

    public ArrayList services = new ArrayList();

    public ArrayList getServices() {
        return services;
    }
    
    /**
     *
     * @param chemin
     */
    public void decompress(String chemin) throws Exception {
        Path binfile = Paths.get(chemin);
        bindata = Files.readAllBytes(binfile);
        Integer binl = bindata.length;
        byte[] givl_s = Arrays.copyOfRange(bindata, 0, 4);
        Integer givl_d = ByteBuffer.wrap(givl_s).getInt() + 4;
        if (binl - givl_d != 0)
            error_msg.Error_diag("length of input binary file \\n differs from length given in that file!");

        byte[] recd_nmbr_s = Arrays.copyOfRange(bindata, 12, 16);
        Integer recd_nmbr_d = ByteBuffer.wrap(recd_nmbr_s).getInt();
        Integer recd_idx = 1;
        Integer bind_idx = 16;
        while (recd_idx <= recd_nmbr_d) {
            Integer nxt_idx = find_end(bind_idx);
            byte[] binrcd = Arrays.copyOfRange(bindata, bind_idx, nxt_idx);
            Integer rcdlen = nxt_idx - bind_idx;
            // create record file name
            byte rcdnamel = binrcd[1];
            byte[] rcdname = Arrays.copyOfRange(binrcd, 5, Byte.toUnsignedInt(rcdnamel) + 5);
            for (int idx=0; idx < Byte.toUnsignedInt(rcdnamel); idx++) {
                if (rcdname[idx] < 0x21)  // non printable codes or spaces
                    rcdname[idx] = 0x5f;    // an underscore instead
                if (rcdname[idx] == 0x7f || rcdname[idx] == 0x3c || rcdname[idx] == 0x3a || rcdname[idx] == 0x22 || 
                    rcdname[idx] == 0x2f || rcdname[idx] == 0x5c || rcdname[idx] == 0x7c || rcdname[idx] == 0x3f ||
                    rcdname[idx] == 0x2a
                    )  // control chars < > : " / \ | ? * space are not allowed in file names; replace by %
                    rcdname[idx] = 0x25;
            }

            // start the filename with R | TV | ? to indicate the radio/TV/unknown service type
            String stype = "U"; // unknown
            if (binrcd[rcdlen - 17] == 0x00) // this byte in fixed distance 17 back from next record has TV/Radio
                stype = "TV";
            else if (binrcd[rcdlen - 17] == 0x01)
                stype = "R";

            byte[] nid_s = Arrays.copyOfRange(binrcd, rcdlen - 26, rcdlen - 24); // also fixed distance back from end
            Integer nid_d = getInt(nid_s); // make the two bytes into an integer
            byte[] ppr = Arrays.copyOfRange(binrcd, rcdlen - 10, rcdlen - 8); // preference setting
            String ppr_s = getPreference(ppr);

            // add the network number and preference setting to the end of the file name
            String rcdname_s = new String(rcdname, "cp860");
            String asciiname = stype + "~" + recd_idx + "~" + rcdname_s + "~E0~" + "N" + nid_d + "~" + "P" + ppr_s;
            services.add(new Service(stype, recd_idx, rcdname_s, nid_d, ppr_s));
            recd_idx++;
            bind_idx = nxt_idx;
        }
    }
    
    private Integer find_end(Integer strt) {
        Integer sidx;
        for (sidx = strt; sidx <strt+300; sidx++)
        {   
            if (Arrays.equals(Arrays.copyOfRange(bindata, sidx, sidx+4), endpatt))
                break;
        }
        return sidx+4;
    }
    
    private Integer getInt(byte[] in) {
        int val = ((in[0] & 0xff) << 8) | (in[1] & 0xff);;
        return val;
    }

    private String getPreference(byte[] ppr) {
        String ppr_s ="";
        for(int i=0; i<8; i++) {
            if (((ppr[1] >> i) & 1) == 1) {
                ppr_s += (i+1) + "-";
            }
        }
        for(int i=9; i<=10; i++) {
            if (((ppr[0] >> (i-9)) & 1) == 1) {
                if (i<10)
                    ppr_s += (i) + "-";
                else
                    ppr_s += (i);
            }
        }
        return ppr_s;
    }
    
    public Boolean isPreferenceOn(String ppr_s, int index) {
        Boolean isOk = false;
        for (String ppr: ppr_s.split("-")){
            if (ppr.equals(String.valueOf(index))) {
                isOk = true;
            }
        }
        return isOk;
    }
    
    public String add_ppr(String old_ppr, int new_Value) {
        String new_ppr = "";
        Boolean isFirst = true;
        Boolean isAdded = false;
        
        // not preference yet
        if (old_ppr.length() == 0)
            return old_ppr += new_Value;
        for (String ppr: old_ppr.split("-")){
            // still not in position
            if ((Integer.valueOf(ppr) < new_Value)) {
                if (isFirst) {
                    new_ppr += ppr;
                    isFirst = false;
                }
                else
                    new_ppr += "-" + ppr;
            }
            // here we are
            else if ((Integer.valueOf(ppr) > new_Value) && !isAdded) {
                // expect for 1 to not have a -
                if (new_Value == 1)
                    new_ppr += new_Value + "-" + ppr ;
                else
                    new_ppr += "-" + new_Value + "-" + ppr ;
                isAdded = true;
            }
            // the rest of the line
            else {
                new_ppr += "-" + ppr ;
            }
        }
        // if new value is the last one we added it manualy
        if (!isAdded)
            new_ppr += "-" + new_Value;
        return new_ppr;
    }
    
    public String remove_ppr(String old_ppr, int new_Value) {
        String new_ppr = "";
        Boolean isFirst = true;
        
        for (String ppr: old_ppr.split("-")){
            if (Integer.valueOf(ppr) == new_Value) {
                continue;
            }
            else {
                if (isFirst) {
                    new_ppr += ppr;
                    isFirst = false;
                }
                else
                    new_ppr += "-" + ppr;
            }
        }
        return new_ppr;
    }
    
    public void update_bdd (Service service)  throws Exception {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();    
        Query q = session.createSQLQuery("UPDATE SERVICE SET  " + "PPR = :ppr " + "WHERE IDX = :idx");
        q.setString("ppr",service.getS_ppr());
        q.setInteger("idx",service.getS_idx());
        int rowCount = q.executeUpdate();
        tx.commit();
    }
}