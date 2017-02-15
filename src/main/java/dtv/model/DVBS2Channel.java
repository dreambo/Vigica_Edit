package dtv.model;

import java.util.List;

public class DVBS2Channel extends DVBChannel {

	public DVBS2Channel() {
		super();
	}

	public DVBS2Channel(String stype, Integer recd_idx, String rcdname_s, Integer nid_d, String ppr_s, List<Byte> line_s) {
		super(stype, recd_idx, rcdname_s, nid_d, ppr_s, line_s);
	}
}
