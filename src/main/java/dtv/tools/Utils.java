package dtv.tools;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dtv.model.DVBChannel;

public class Utils {

	public static final String FAV_SEP = "#";
    public static String[] PPREFS  = {"FAV0", "FAV1", "FAV2", "FAV3", "FAV4", "FAV5", "FAV6", "FAV7", "FAV8", "FAV9"};
    public static String[] prefTab = PPREFS;

    public static String bytesToHexString(byte[] in) {
        final StringBuilder builder = new StringBuilder();
        for (byte b : in) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
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

    public static Boolean isPreferenceOn(String ppr_s, String pref) {

        for (String ppr: ppr_s.split("-")) {
            if (ppr.equals(pref)) {
                return true;
            }
        }

        return false;
    }

    public static String setPref(String old_ppr, String new_Value, boolean add) {

        // not preference yet
        if (old_ppr.length() == 0) {
            return old_ppr += new_Value;
        }

        return (old_ppr + "-" + new_Value);
    }

    public static String add_ppr(String old_ppr, String new_Value) {

        return (old_ppr + (old_ppr.isEmpty() ? "" : "-" ) + new_Value);
    }

    public static String remove_ppr(String old_ppr, String new_Value) {
        String new_ppr = "";
        Boolean isFirst = true;
        
        for (String ppr: old_ppr.split("-")) {
            if (new_Value.equals(ppr)) {
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

    public static String crc32Mpeg(List<Byte> bytes) {

    	int register = 0xffffffff;

    	for (Byte value: bytes) {
            // Create a mask to isolate the highest bit.
            int bitMask = (int) (1 << 31);

            byte element = (byte) value;

            register ^= ((int) element << 24);
            for (int i = 0; i < 8; i++) {
                if ((register & bitMask) != 0) {
                    register = (int) ((register << 1) ^ 0x04c11db7);
                } else {
                    register <<= 1;
                }
            }
		}

    	// XOR the final register value.
        register ^= 0x00000000;
        // Create a mask to isolate only the correct width of bits.
        long fullMask = (((1L << 31) - 1L) << 1) | 1L;

        String crcresult = Long.toHexString(register & fullMask);

        return ("00000000" + crcresult).substring(crcresult.length());
    }

    public static <T extends DVBChannel> void initIds(List<T> services) {
        int i = 0;
        for (T service : services) {
			service.setIdx(++i);
			service.setModified(true);
		}
    }

    public static int getPrefIndex(String pref) {

    	for (int i = 0; i < prefTab.length; i++) {
    		if (prefTab[i].equals(pref)) {
    			return i;
    		}
    	}

    	return -1;
    }

    public static String getPreferences() {

    	String preferences = "";
    	for (String pref: prefTab) {
    		preferences += pref + FAV_SEP;
    	}

    	return preferences;
    }

	public static List<Byte> asList(byte[] entry) {

		List<Byte> bytes = new ArrayList<>();

		for (byte myByte: entry) {
			bytes.add(Byte.valueOf(myByte));
		}

		return bytes;
	}

	/**
	 * copy the file src to this folder
	 * @param src: source file
	 * @param folder: destination folder
	 */
	public static void copy(File src, File folder) {
		try {
			File dst = new File(folder, src.getName());
			Files.copy(src.toPath(), dst.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
