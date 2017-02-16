package dtv.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import dtv.tools.Utils;

public class DVBFile<T extends DVBChannel> {

	private File dvbFile;
	private int version;
	private List<T> dvbServices;

	public File getDvbFile() {
		return dvbFile;
	}
	public void setDvbFile(File dvbFile) {
		this.dvbFile = dvbFile;
	}

	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	public List<T> getDvbServices() {
		return dvbServices;
	}
	public void setDvbServices(List<T> dvbServices) {
		this.dvbServices = dvbServices;
	}

	public List<T> createDvbServices(List<Byte> bytes) {
		return dvbServices;
	}

	// convert all properties to a list of bytes
	public List<Byte> getBytes() throws Exception {

		List<Byte> header = new ArrayList<>();
		List<Byte> servicesData = getServicesData();
		List<Byte> bytes = new ArrayList<>();

		int fileLength = 12 + servicesData.size(); // 4 crc + 4 type + 4 service count + all service records
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

        for (T service : dvbServices) {

        	// last two bytes must be the channel index, beginning from 0
            Byte[] indexBa = Utils.int2ba(service.getIdx() - 1);
            sdata = service.getLine();
            sdata.set(sdata.size() - 1, indexBa[indexBa.length - 1]);
            sdata.set(sdata.size() - 2, indexBa[indexBa.length - 2]);

            if (!service.isModified()) {
                satservices.addAll(sdata);

            } else {

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
}
