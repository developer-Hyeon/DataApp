package sinabro.today.information;

public class DeviceInfo {

    String deviceID;
    String email;
    String password;

    public DeviceInfo(){

    }

    public DeviceInfo(String deviceID, String email, String password) {
        this.deviceID = deviceID;
        this.email = email;
        this.password = password;

    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
