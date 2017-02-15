package dtv.tools.writer;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javafx.concurrent.Service;
import javafx.concurrent.Task;
import dtv.model.DVBChannel;
import dtv.tools.Utils;

public abstract class DVBWriter<T extends DVBChannel> extends Service<List<T>> {

	public abstract File getDvbFile();
    public abstract void setDvbFile(File dvbFile);
	public abstract List<Byte> getFileVersion();
	public abstract void setFileVersion(List<Byte> fileversion);
	public abstract List<T> getServices();
	public abstract void setServices(List<T> services);

	/**
     * compress this list of dvb services into a the file dvbFile
     * @param chemin
     */
    public void compress() throws Exception {

    	List<Byte> satservices = new ArrayList<>();
    	List<Byte> sdata;

        for (T service : getServices()) {

        	// last two bytes must be the channel index, beginning from 0
            Byte[] indexBa = Utils.int2ba(service.getIdx());
            sdata = service.getLine(); // ByteUtils.hexStringToBytes(service.getLine());
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

        List<Byte> servicesCount = Arrays.asList(Utils.int2ba(getServices().size())); // 4 bytes
        List<Byte> servicesData  = new ArrayList<>();

        servicesData.addAll(getFileVersion());
        servicesData.addAll(servicesCount);
        servicesData.addAll(satservices);

        //CRC32
        String crcresult = Utils.crc32Mpeg(servicesData);
        crcresult = ("00000000" + crcresult).substring(crcresult.length());
        List<Byte> crcbyte = Utils.hexStringToBytes(crcresult);
        
        List<Byte> mw_s1 = new ArrayList<>();
        List<Byte> flbytes = Arrays.asList(Utils.int2ba(12 + satservices.size())); // 4 crc + 4 type + 4 service count + all service records

        mw_s1.addAll(flbytes);
        mw_s1.addAll(crcbyte);
        mw_s1.addAll(servicesData);

        Utils.writeBytesToFile(mw_s1, getDvbFile());
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

	@Override
	protected Task<List<T>> createTask() {
		return new Task<List<T>>() {
			@Override
			protected List<T> call() throws Exception {
	            updateProgress(-1, 0);
	            compress();
	            updateProgress(1, 1);

	            return null;
			}
		};
	}
}
