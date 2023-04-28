package bean;

import java.lang.annotation.Target;

public class TargetProfile {
    private String name;
    private String ip_address;
    private String uuid;
    // either pc/laptop/android
    private String clientType;

    public TargetProfile(){

    }

    public TargetProfile(String a, String b){
        name = a;
        ip_address = b;
    }

    public TargetProfile(String namana, String ipna, String uuidna, String tipena){
        name = namana;
        ip_address = ipna;
        uuid = uuidna;
        clientType = tipena;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp_address() {
        return ip_address;
    }

    public void setIp_address(String ip_address) {
        this.ip_address = ip_address;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getClientType() {
        return clientType;
    }

    public void setClientType(String clientType) {
        this.clientType = clientType;
    }
}
