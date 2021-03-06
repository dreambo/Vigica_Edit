/*
 * Copyright (C) 2016 bnabi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package dtv.model;

import java.util.List;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 * Model class for
 * 
 * @author nabillo
 */
public class DVBChannel implements Comparable<DVBChannel> {

    private final IntegerProperty idx;
    private final StringProperty type;
    private final StringProperty name;
    private final IntegerProperty nid;
    private final StringProperty ppr;
    private List<Byte> line;
    private boolean modified = false;
    
    public DVBChannel() {
        this.type = new SimpleStringProperty("");
        this.idx = new SimpleIntegerProperty(0);
        this.name = new SimpleStringProperty("");
        this.nid = new SimpleIntegerProperty(0);
        this.ppr = new SimpleStringProperty("");
    }
    
    public DVBChannel(String stype, Integer recd_idx, String rcdname_s, Integer nid_d, String ppr_s, List<Byte> line_s) {
        this.type = new SimpleStringProperty(stype);
        this.idx = new SimpleIntegerProperty(recd_idx);
        this.name = new SimpleStringProperty(rcdname_s);
        this.nid = new SimpleIntegerProperty(nid_d);
        this.ppr = new SimpleStringProperty(ppr_s);
        this.line = line_s;
    }

    public int getIdx() {
        return idx.get();
    }
    public void setIdx(int idx) {
        this.idx.set(idx);
    }

    public String getType() {
        return type.get();
    }
    public void setType(String type) {
        this.type.set(type);
    }

    public StringProperty typeProperty() {
        return type;
    }
    
    public IntegerProperty idxProperty() {
        return idx;
    }
    
    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }
    
    public int getNid() {
        return nid.get();
    }
    public void setNid(int nid) {
        this.nid.set(nid);
    }

    public IntegerProperty nidProperty() {
        return nid;
    }
    
    public String getPpr() {
        return ppr.get();
    }
    public void setPpr(String ppr) {
        this.ppr.set(ppr);
    }

    public StringProperty pprProperty() {
        return ppr;
    }
    
    public List<Byte> getLine() {
        return line;
    }
    public void setLine(List<Byte> line) {
        this.line = line;
    }
    
    public boolean isModified() {
        return modified;
    }
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public boolean equals(Object obj) {

    	if (obj == null || !(obj instanceof DVBChannel)) {
    		return false;
    	}

    	DVBChannel channel = (DVBChannel) obj;

    	return (getName() != null && getName().equals(channel.getName()) && getType() != null && getType().equals(channel.getType()));
    }

    @Override
    public int hashCode() {
    	return (getName() + getType()).hashCode();
    }

    @Override
    public String toString() {
    	return getName() + "[" + getType() + "]";
    }

	@Override
	public int compareTo(DVBChannel channel) {

		if (getType().equals(channel.getType())) {
			return getName().toLowerCase().compareTo(channel.getName().toLowerCase());
		}

		return 0;
	}
}
