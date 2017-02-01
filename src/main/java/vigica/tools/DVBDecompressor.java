package vigica.tools;

import java.io.File;
import java.util.List;

import javafx.concurrent.Service;
import vigica.model.DVBService;

public abstract class DVBDecompressor extends Service<List<DVBService>> {

	public abstract void setDvbFile(File dvbFile);

	/**
	 * decompress the given dvb file
	 * @param file
	 * @throws java.lang.Exception
	 */
	public abstract List<DVBService> decompress() throws Exception;

}
