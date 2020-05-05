package sinabro.today.model;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/*
회원가입한 정보들을 인증에 저장하기위한 데이터 저장소
회원가입 이름, 사진, 직업 등 받을려면 변수를 생성하고 SignupActivity에 변수를 초기화 해주면 된다.
 */
public class UserModel {

    public String userName;
    public String pushToken;
    public String sex;
    public String age;
    public String personality;
    public String tall;
    public String body;
    public String heart;
    public String email;
    public String comment;
    private String uid;
    public String platform;
    private String ProfileOneImageUrl;
    private Double latitude;
    private Double longitude;
    private Map<String,Double> location = new HashMap<>();
    public Map<String, Photo> photo = new HashMap<>();


    public Photo photoInfo = new Photo();
    public static class Photo implements Serializable {
        public String image;
        public String imageHashCode;
        public String temp;
        public String tempHashCode;
    }

    public String getProfileOneImageUrl() {
        return ProfileOneImageUrl;
    }

    public void setProfileOneImageUrl(String ProfileOneImageUrl) {
        this.ProfileOneImageUrl = ProfileOneImageUrl;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPushToken() {
        return pushToken;
    }

    public void setPushToken(String pushToken) {
        this.pushToken = pushToken;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    public String getPersonality() {
        return personality;
    }

    public void setPersonality(String personality) {
        this.personality = personality;
    }

    public String getTall() {
        return tall;
    }

    public void setTall(String tall) {
        this.tall = tall;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getHeart() {
        return heart;
    }

    public void setHeart(String heart) {
        this.heart = heart;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Map<String, Double> getLocation() {
        return location;
    }

    public void setLocation(Map<String, Double> location) {
        this.location = location;
    }

}