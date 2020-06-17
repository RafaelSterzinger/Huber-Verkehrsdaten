
package com.example.huber.live.entity.disturbance;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class RelatedLineTypes {

    @SerializedName("13A")
    @Expose
    private String _13A;
    @SerializedName("VRT")
    @Expose
    private String vRT;
    @SerializedName("O")
    @Expose
    private String o;
    @SerializedName("25")
    @Expose
    private String _25;
    @SerializedName(" 26")
    @Expose
    private String _26;

    public String get13A() {
        return _13A;
    }

    public void set13A(String _13A) {
        this._13A = _13A;
    }

    public String getVRT() {
        return vRT;
    }

    public void setVRT(String vRT) {
        this.vRT = vRT;
    }

    public String getO() {
        return o;
    }

    public void setO(String o) {
        this.o = o;
    }

    public String get25() {
        return _25;
    }

    public void set25(String _25) {
        this._25 = _25;
    }

    public String get26() {
        return _26;
    }

    public void set26(String _26) {
        this._26 = _26;
    }

}
