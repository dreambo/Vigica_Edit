package vigica.tools;

import java.io.File;
import java.util.List;

import javafx.concurrent.Service;
import vigica.model.DVBService;

public abstract class DVBCompressor extends Service<Void> {

	public abstract void setDvbFile(File dvbFile);

	/**
	 * compress the given dvb services to this file
	 * @param file
	 * @throws java.lang.Exception
	 */
	public abstract void compress(List<DVBService> services) throws Exception;
}
