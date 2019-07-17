package org.tensorflow.demo;

public class DiaryVO {

    private String ddate;
    private String dtitle;
    private String dimgpath;
    private String dcontent;
    private String id;
    private String no;
    private byte[] byteArray2;

    public DiaryVO() {
        this.ddate = ddate;
        this.dtitle = dtitle;
        this.dimgpath = dimgpath;
        this.dcontent = dcontent;
        this.id = id;
        this.no = no;
        this.byteArray2 = byteArray2;
    }

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getDdate() {
        return ddate;
    }

    public void setDdate(String ddate) {
        this.ddate = ddate;
    }

    public String getDtitle() {
        return dtitle;
    }

    public void setDtitle(String dtitle) {
        this.dtitle = dtitle;
    }

    public String getDimgpath() {
        return dimgpath;
    }

    public void setDimgpath(String dimgpath) {
        this.dimgpath = dimgpath;
    }

    public String getDcontent() {
        return dcontent;
    }

    public void setDcontent(String dcontent) {
        this.dcontent = dcontent;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public byte[] getByteArray2() {
        return byteArray2;
    }

    public void setByteArray2(byte[] byteArray2) {
        this.byteArray2 = byteArray2;
    }
}
