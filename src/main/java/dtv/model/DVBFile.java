package dtv.model;

import java.util.List;

public class DVBFile<T extends DVBChannel> {

	private int fileLength;
	private int crc;
	private int version;
	private int servicesCount;
	private List<T> dvbServices;
	public int getFileLength() {
		return fileLength;
	}

	public void setFileLength(int fileLength) {
		this.fileLength = fileLength;
	}
	public int getCrc() {
		return crc;
	}
	public void setCrc(int crc) {
		this.crc = crc;
	}
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	public int getServicesCount() {
		return servicesCount;
	}
	public void setServicesCount(int servicesCount) {
		this.servicesCount = servicesCount;
	}
	public List<T> getDvbServices() {
		return dvbServices;
	}
	public void setDvbServices(List<T> dvbServices) {
		this.dvbServices = dvbServices;
	}

	// convert all properties to a list of bytes
	public List<Byte> getBytes() {
		return null;
	}
}
