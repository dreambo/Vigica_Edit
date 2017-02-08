package dtv.tools.reader;

import org.springframework.stereotype.Component;

import dtv.model.DVBS2Channel;

@Component
public class DVBS2Reader extends AbstractReader<DVBS2Channel> {

	private static final byte[] END_MAGIC = {(byte) 0x00, (byte) 0x00, (byte) 0x3F, (byte) 0xFF};

	@Override
	protected int getOffset(byte version) {
		return (version == 0x0E ? (4*16 + 2) : 0);
	}

	@Override
	protected byte[] getEndMagic() {
		return END_MAGIC;
	}

	@Override
	protected DVBS2Channel getDVBService(String stype, int i, String rcdname_s, int nid_d, String ppr_s, String binrcd_s, boolean b, String string) {
		return new DVBS2Channel(stype, i, rcdname_s, nid_d, ppr_s, binrcd_s, b, string);
	}
}
