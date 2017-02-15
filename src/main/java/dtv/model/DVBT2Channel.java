package dtv.model;

import java.util.List;

public class DVBT2Channel extends DVBChannel {

	public DVBT2Channel() {
		super();
	}

	public DVBT2Channel(String stype, Integer recd_idx, String rcdname_s, Integer nid_d, String ppr_s, List<Byte> line_s) {
		super(stype, recd_idx, rcdname_s, nid_d, ppr_s, line_s);
	}
}
