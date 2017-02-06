package vigica.tools.reader;

import java.io.File;
import java.util.List;

import vigica.model.DVBService;

public interface DVBReader<T extends DVBService> {

	public abstract void setDvbFile(File dvbFile);

	/**
	 * decompress the given dvb file
	 * @param file
	 * @throws java.lang.Exception
	 */
	public abstract List<T> decompress() throws Exception;

	public abstract List<Byte> getFileVersion();
}
