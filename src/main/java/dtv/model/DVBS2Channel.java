package dtv.model;

import javax.persistence.Entity;

@Entity
public class DVBS2Channel extends DVBChannel {

	public DVBS2Channel() {
		super();
	}

	public DVBS2Channel(String stype, Integer recd_idx, String rcdname_s, Integer nid_d, String ppr_s, String line_s, Boolean flag_b, String new_b) {
		super(stype, recd_idx, rcdname_s, nid_d, ppr_s, line_s, flag_b, new_b);
	}
}
