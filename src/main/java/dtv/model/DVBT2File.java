package dtv.model;

import org.springframework.stereotype.Component;

@Component
public class DVBT2File extends DVBFile {

	private static final byte[] END_MAGIC = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};

	public DVBT2File() {}

	@Override
	protected int getOffset(byte version) {
		return (version == 0x0E ? (4*16 + 3) : 0);
	}

	@Override
	protected byte[] getEndMagic() {
		return END_MAGIC;
	}
}
