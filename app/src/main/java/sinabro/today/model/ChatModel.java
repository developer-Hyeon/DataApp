package sinabro.today.model;

import java.util.HashMap;
import java.util.Map;

public class ChatModel {

    public Map<String,Boolean> users = new HashMap<>(); //채팅방의 유저들
    public Map<String,Comment> comments = new HashMap<>();//채팅방의 대화내용

    public static class Comment {

        public String uid;
        public String message;
        public Object timestamp;
        public String readUsers;// 어차피 이건 한명만 들어온다 그냥 일반 변수로 선언하게끔 만들자

    }



}