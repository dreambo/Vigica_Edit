package dtv.tools.reader;

import java.io.File;
import java.util.List;

import dtv.model.DVBChannel;

public interface DVBReader<T extends DVBChannel> {

	public abstract void setDvbFile(File dvbFile);

	/**
	 * decompress the given dvb file
	 * @param file
	 * @throws java.lang.Exception
	 */
	public abstract List<T> decompress() throws Exception;

	public abstract List<Byte> getFileVersion();
}
