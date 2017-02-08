package dtv.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;

public class ByteUtils {

    public static String bytesToHexString(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static String base64Encoder(byte[] in) {
        return Base64.getEncoder().encodeToString(in);
    }

    public static List<Byte> base64Decoder(String s) {
    	List<Byte> bytesList = new ArrayList<>();
        byte[] bytes = Base64.getDecoder().decode(s);
        for (byte myByte: bytes) {
        	bytesList.add(myByte);
        }

        return bytesList;
    }

    public static int find_end(byte[] bindata, int strt, byte[] endMagic) {

        for (int sidx = strt; sidx < (strt + 1000); sidx++) {   
            if (Arrays.equals(Arrays.copyOfRange(bindata, sidx, sidx + endMagic.length), endMagic)) {
            	return (sidx + endMagic.length);
            }
        }

        return -1;
    }

    public static int getInt(byte[] in) {
        int val = ((in[0] & 0xff) << 8) | (in[1] & 0xff);
        return val;
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

    public static List<Byte> hexStringToBytes(String s) {
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

    public static Byte[] int2ba(int integer) {
        Byte[] result = new Byte[4];

        result[0] = (byte) ((integer & 0xFF000000) >> 24);
        result[1] = (byte) ((integer & 0x00FF0000) >> 16);
        result[2] = (byte) ((integer & 0x0000FF00) >> 8);
        result[3] = (byte) (integer & 0x000000FF);

        return result;
    }

    public static void writeBytesToFile(List<Byte> bytes, File file) throws Exception {

    	OutputStream servicesf = new BufferedOutputStream(new FileOutputStream(file));
        for (byte car: bytes) {
            servicesf.write(car);
        }

        servicesf.close();
    }
}
