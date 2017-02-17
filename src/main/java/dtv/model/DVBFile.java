package dtv.model;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import dtv.controller.Message;
import dtv.tools.Utils;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public abstract class DVBFile extends Service<List<DVBChannel>> {

	private static final Logger LOG = LoggerFactory.getLogger(DVBFile.class);

	private boolean read = true;
	private int offset;
	private File dvbFile;
	private byte version;
	private List<DVBChannel> dvbServices;

	public DVBFile() {}

	protected abstract int getOffset(byte version);
	protected abstract byte[] getEndMagic();

	public void setRead(boolean read) {
		this.read = read;
	}

	public void setDvbFile(File dvbFile) {
		this.dvbFile = dvbFile;
	}

	public void setDvbServices(List<DVBChannel> dvbServices) {
		this.dvbServices = dvbServices;
	}

	// convert all properties to a list of bytes
	public List<Byte> getBytes() throws Exception {

		List<Byte> header = new ArrayList<>();
		List<Byte> servicesData = getServicesData();
		List<Byte> bytes = new ArrayList<>();

		int fileLength = 12 + servicesData.size(); // 4 crc + 4 version + 4 service count + all service records
		String crc = Utils.crc32Mpeg(servicesData);

		// header
		header.addAll(Arrays.asList(Utils.int2ba(fileLength)));			// file length 4 bytes
		header.addAll(Utils.hexStringToBytes(crc));						// crc code 4 bytes
		header.addAll(Arrays.asList(Utils.int2ba(version)));			// file version 4 bytes
		header.addAll(Arrays.asList(Utils.int2ba(dvbServices.size())));	// number of channels 4 bytes

		// all bytes
		bytes.addAll(header);
		bytes.addAll(servicesData);

		return bytes;
	}

    public List<Byte> getServicesData() throws Exception {

    	List<Byte> satservices = new ArrayList<>();
    	List<Byte> sdata;

        for (DVBChannel service : dvbServices) {

            sdata = service.getLine();
            if (version > 0x0D) {
            	// last two bytes must be the channel index, beginning from 0
                Byte[] indexBa = Utils.int2ba(service.getIdx() - 1);
            	sdata.set(sdata.size() - 1, indexBa[indexBa.length - 1]);
            	sdata.set(sdata.size() - 2, indexBa[indexBa.length - 2]);
            }

            if (!service.isModified()) {
                satservices.addAll(sdata);

            } else {

                int entryLength = sdata.size() - offset;
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
                byte rcdnamel = sdata.get(1);

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

        return satservices;
    }

    private List<Byte> getPpr(String preference) {

    	List<Byte> ppr = new ArrayList<>(2);
        ppr.add((byte) 0x00);
        ppr.add((byte) 0x00);

        if (!preference.trim().isEmpty()) {
            for (String perf : preference.split("-")) {

            	int index = Utils.getPrefIndex(perf);

                if (index < 8) {
                    Double pos = Math.pow(2, index);
                    byte temp = (byte) (ppr.get(1) | pos.byteValue());
                    ppr.set(1, temp);
                } else {
                    Double pos = Math.pow(2, index - 8);
                    byte temp = (byte) (ppr.get(0) | pos.byteValue());
                    ppr.set(0, temp);
                }
            }
        }

        return ppr;
    }

	public List<DVBChannel> load() throws Exception {

    	byte[] bindata;
    	List<DVBChannel> services = new ArrayList<>();

        bindata = Files.readAllBytes(Paths.get(dvbFile.getAbsolutePath()));
        int binl = bindata.length;
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
        offset = getOffset(version = bindata[11]);

        while (recd_idx < recd_nmbr_d) {
            int nxt_idx = Utils.find_end(bindata, bind_idx, getEndMagic());
            if (nxt_idx < 0) {
            	LOG.error("Can not find magic end " + getEndMagic());
            	return services;
            }

            byte[] entry = Arrays.copyOfRange(bindata, bind_idx, nxt_idx + offset);
            // int entryLength = nxt_idx - bind_idx;
            int entryLength = entry.length - offset;
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
            int nid_d = Utils.getInt(nid_s); // make the two bytes into an integer
            byte[] ppr = Arrays.copyOfRange(entry, entryLength - 10, entryLength - 8); // preference setting
            String ppr_s = getPreference(ppr);

            // add the network number and preference setting to the end of the file name
            String rcdname_s = new String(entryName, "UTF-8");
            // String asciiname = stype + "~" + recd_idx + "~" + rcdname_s + "~E0~" + "N" + nid_d + "~" + "P" + ppr_s;
            DVBChannel service = new DVBChannel(stype, ++recd_idx, rcdname_s, nid_d, ppr_s, Utils.asList(entry));

            services.add(service);
            bind_idx = nxt_idx + offset;
        }

        return services;
    }

	private String getPreference(byte[] ppr) {

		String ppr_s = "";

        for (int i = 0; i < Utils.prefTab.length; i++) {

        	int number = (i < 8 ? (ppr[1] >> i) : (ppr[0] >> (i-8)));
            if ((number & 1) == 1) {
            	ppr_s += "-" + Utils.prefTab[i];
            }
        }

        return ppr_s.replaceAll("^\\-", "");
    }

	private List<DVBChannel> write() throws Exception {
		Utils.writeBytesToFile(getBytes(), dvbFile);
		return null;
	}

	@Override
	protected Task<List<DVBChannel>> createTask() {
		
		return new Task<List<DVBChannel>>() {

			@Override
			protected List<DVBChannel> call() throws Exception {

	            updateProgress(-1, 0);
	            return (read ? load() : write());
			}
		};
	}
}
