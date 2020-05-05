package sinabro.today.model;

public class NotificationModel {

    public String to;

    public Notification notification = new Notification();
    public Data data = new Data();

     public static class Notification {
          public String title;
          public String text;
          public String click_action;
    } // 이걸 만들어서 보내면 백그라운드로 보내진다. 이거하면 백그라운드 헤드업 안옴

    public static class Data{
        public String title;
        public String text;
        public String click_action;

    } // 이걸 만들어서 보내면 포그라운드로 보내진다.
}