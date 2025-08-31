package org.example.dto;

public class DomainIpRecord {
    private String domain;
    private String ip;

    public DomainIpRecord() {
    }

    public DomainIpRecord(String domain, String ip) {
        this.domain = domain;
        this.ip = ip;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Override
    public String toString() {
        return domain + " - " + ip;
    }
}
