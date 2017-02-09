package dtv.tools.writer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import dtv.database.DVBDBService;
import dtv.model.DVBChannel;
import dtv.tools.ByteUtils;
import javafx.concurrent.Service;
import javafx.concurrent.Task;

public abstract class DVBWriter<T extends DVBChannel> extends Service<List<T>> {

	public abstract File getDvbFile();
    public abstract void setDvbFile(File dvbFile);
	public abstract List<Byte> getFileVersion();
	public abstract void setFileVersion(List<Byte> fileversion);
	public abstract DVBDBService<T> getServiceDB();

	/**
     * compress this list of dvb services into a the file dvbFile
     * @param chemin
     */
    public void compress(List<T> services) throws Exception {

    	List<Byte> satservices = new ArrayList<>();
    	List<Byte> sdata;
    	int index = 0;

    	// TODO: only for tests
    	Collections.sort(services, (s1, s2) -> s1.getName().compareTo(s2.getName()));

        for (T service : services) {

        	// last two bytes must be the channel index, beginning from 0
            Byte[] indexBa = ByteUtils.int2ba(index++);
            sdata = ByteUtils.base64Decoder(service.getLine()); // ByteUtils.hexStringToBytes(service.getLine());
            sdata.set(sdata.size() - 1, indexBa[indexBa.length - 1]);
            sdata.set(sdata.size() - 2, indexBa[indexBa.length - 2]);

            if (!service.getFlag()) {
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

        List<Byte> servicesCount = Arrays.asList(ByteUtils.int2ba(services.size())); // 4 bytes
        List<Byte> servicesData  = new ArrayList<>();

        servicesData.addAll(getFileVersion());
        servicesData.addAll(servicesCount);
        servicesData.addAll(satservices);

        //CRC32
        String crcresult = ByteUtils.crc32Mpeg(servicesData);
        crcresult = ("00000000" + crcresult).substring(crcresult.length());
        List<Byte> crcbyte = ByteUtils.hexStringToBytes(crcresult);
        
        List<Byte> mw_s1 = new ArrayList<>();
        List<Byte> flbytes = Arrays.asList(ByteUtils.int2ba(12 + satservices.size())); // 4 crc + 4 type + 4 service count + all service records

        mw_s1.addAll(flbytes);
        mw_s1.addAll(crcbyte);
        mw_s1.addAll(servicesData);

        ByteUtils.writeBytesToFile(mw_s1, getDvbFile());
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
	protected Task<List<T>> createTask() {
		return new Task<List<T>>() {
			@Override
			protected List<T> call() throws Exception {
	            updateProgress(-1, 0);
	            compress(getServiceDB().read_bdd());
	            updateProgress(1, 1);

	            return null;
			}
		};
	}
}
