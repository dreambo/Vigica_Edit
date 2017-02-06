package vigica.tools.reader;

import org.springframework.stereotype.Component;

import vigica.model.DVBT2Service;

@Component
public class DVBT2Reader extends AbstractReader<DVBT2Service> {

	private static final byte[] END_MAGIC = {(byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0x00, (byte) 0x00, (byte) 0x00};

	@Override
	protected int getOffset(byte version) {
		return (version == 0x0E ? (4*16 + 3) : 0);
	}

	@Override
	protected byte[] getEndMagic() {
		return END_MAGIC;
	}

	@Override
	protected DVBT2Service getDVBService(String stype, int i, String rcdname_s, int nid_d, String ppr_s, String binrcd_s, boolean b, String string) {
		return new DVBT2Service(stype, i, rcdname_s, nid_d, ppr_s, binrcd_s, b, string);
	}
}
