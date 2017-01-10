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
package vigica.model;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Model class for
 * 
 * @author nabillo
 */
@Entity
@Table(name="SERVICE")
public class DVBService {

	@Id
    private final StringProperty type;
    private final IntegerProperty idx;
    private final StringProperty name;
    private final IntegerProperty nid;
    private final StringProperty ppr;
    private final StringProperty line;
    private final BooleanProperty flag;
    @Column(name="NEW")
    private final StringProperty neew;
    
    /**
    * Default constructor.
    */
    public DVBService() {
        this.type = new SimpleStringProperty("");
        this.idx = new SimpleIntegerProperty(0);
        this.name = new SimpleStringProperty("");
        this.nid = new SimpleIntegerProperty(0);
        this.ppr = new SimpleStringProperty("");
        this.line = new SimpleStringProperty("");
        this.flag = new SimpleBooleanProperty(false);
        this.neew = new SimpleStringProperty("");
    }
    
    public DVBService(String stype, Integer recd_idx, String rcdname_s, Integer nid_d, String ppr_s, String line_s, Boolean flag_b, String new_b) {
        this.type = new SimpleStringProperty(stype);
        this.idx = new SimpleIntegerProperty(recd_idx);
        this.name = new SimpleStringProperty(rcdname_s);
        this.nid = new SimpleIntegerProperty(nid_d);
        this.ppr = new SimpleStringProperty(ppr_s);
        this.line = new SimpleStringProperty(line_s);
        this.flag = new SimpleBooleanProperty(flag_b);
        this.neew = new SimpleStringProperty(new_b);
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
    
    public int getIdx() {
        return idx.get();
    }

    public void setIdx(int idx) {
        this.idx.set(idx);
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
    
    public String getLine() {
        return line.get();
    }

    public void setLine(String line) {
        this.line.set(line);
    }

    public StringProperty lineProperty() {
        return line;
    }
    
    public Boolean getFlag() {
        return flag.get();
    }

    public void setFlag(Boolean flag) {
        this.flag.set(flag);
    }

    public BooleanProperty flagProperty() {
        return flag;
    }
    
    public String getNeew() {
        return neew.get();
    }

    public void setNeew(String neew) {
        this.neew.set(neew);
    }

    public StringProperty neewProperty() {
        return neew;
    }
}
