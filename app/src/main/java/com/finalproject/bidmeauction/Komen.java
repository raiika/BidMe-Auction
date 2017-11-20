package com.finalproject.bidmeauction;

/**
 * Created by RAIIKA on 10/12/2017.
 */

public class Komen {

    private String desc, username, komen_id;
    private Long waktu;
    private boolean bestkomen;

    public Komen(){

    }

    public Komen(String desc, String username, Long waktu, String komen_id, boolean bestkomen) {
        this.desc = desc;
        this.username = username;
        this.waktu = waktu;
        this.komen_id = komen_id;
        this.bestkomen = bestkomen;
    }

    public boolean isBestkomen() {
        return bestkomen;
    }

    public void setBestkomen(boolean bestkomen) {
        this.bestkomen = bestkomen;
    }

    public String getKomen_id() {
        return komen_id;
    }

    public void setKomen_id(String komen_id) {
        this.komen_id = komen_id;
    }

    public Long getWaktu() {
        return waktu;
    }

    public void setWaktu(Long waktu) {
        this.waktu = waktu;
    }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
