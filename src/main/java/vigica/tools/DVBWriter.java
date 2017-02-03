package vigica.tools;

import java.io.File;
import java.util.List;

import javafx.concurrent.Service;
import vigica.model.DVBService;

public abstract class DVBWriter extends Service<List<DVBService>> {

	public abstract void setDvbFile(File dvbFile);

	/**
	 * compress the given dvb services to this file
	 * @param file
	 * @throws java.lang.Exception
	 */
	public abstract void compress(List<DVBService> services) throws Exception;

	public abstract void setFileVersion(List<Byte> fileVersion);
}
