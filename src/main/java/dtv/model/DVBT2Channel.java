package dtv.model;

import javax.persistence.Entity;

@Entity
public class DVBT2Channel extends DVBChannel {

	public DVBT2Channel() {
		super();
	}

	public DVBT2Channel(String stype, Integer recd_idx, String rcdname_s, Integer nid_d, String ppr_s, String line_s, Boolean flag_b, String new_b) {
		super(stype, recd_idx, rcdname_s, nid_d, ppr_s, line_s, flag_b, new_b);
	}
}
