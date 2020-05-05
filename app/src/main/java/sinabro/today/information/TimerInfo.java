package sinabro.today.information;

public class TimerInfo {

    private String now;
    private String deviceID;

    public TimerInfo(){

    }

    public TimerInfo(String now, String deviceID) {
        this.now = now;
        this.deviceID = deviceID;
    }

    public String getNow() {
        return now;
    }

    public void setNow(String now) {
        this.now = now;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }
}
