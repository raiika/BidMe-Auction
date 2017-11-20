package com.finalproject.bidmeauction;

import java.util.Date;

/**
 * Created by RAIIKA on 10/12/2017.
 */

public class Blog {

    private boolean available;
    private String title, desc, image, username, bidname, auction_id, biduid, uid;
    private long waktu, tutup;

    private int bid;

    public Blog(){

    }

    public Blog(String title, String desc, String image, String username, String bidname, String auction_id, String biduid, String uid, boolean available, long waktu, long tutup, int bid) {
        this.title = title;
        this.desc = desc;
        this.image = image;
        this.username = username;
        this.bidname = bidname;
        this.auction_id = auction_id;
        this.biduid = biduid;
        this.uid = uid;
        this.available = available;
        this.waktu = waktu;
        this.tutup = tutup;
        this.bid = bid;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getBidname() {
        return bidname;
    }

    public void setBidname(String bidname) {
        this.bidname = bidname;
    }

    public String getAuction_id() {
        return auction_id;
    }

    public void setAuction_id(String auction_id) {
        this.auction_id = auction_id;
    }

    public String getBiduid() {
        return biduid;
    }

    public void setBiduid(String biduid) {
        this.biduid = biduid;
    }

    public long getWaktu() {
        return waktu;
    }

    public void setWaktu(long waktu) {
        this.waktu = waktu;
    }

    public long getTutup() {
        return tutup;
    }

    public void setTutup(long tutup) {
        this.tutup = tutup;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(int bid) {
        this.bid = bid;
    }
}
