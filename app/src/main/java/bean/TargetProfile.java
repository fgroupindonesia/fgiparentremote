package bean;

public class TargetProfile {
    private String name;
    private String ip_address;

    public TargetProfile(){

    }

    public TargetProfile(String a, String b){
        name = a;
        ip_address = b;
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
}
