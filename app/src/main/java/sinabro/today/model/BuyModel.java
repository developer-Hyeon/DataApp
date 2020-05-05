package sinabro.today.model;

public class BuyModel {

    public String buys_date; // 날짜(구매한 날짜, 사용 날짜)
    public String buys_comment; // 내역(ex)무료충전200개, 추천인150개, 구매내역 몇개
    public String buys_change; // 내역변화
    public String current_heart; // 현재 하트 몇개? 변화랑 누적은 comment로 계산해서 저장
    public String buys_id; // 결제시 주문ID

    public BuyModel(String buys_date, String buys_comment, String buys_change, String current_heart, String buys_id) {
        this.buys_date = buys_date;
        this.buys_comment = buys_comment;
        this.buys_change = buys_change;
        this.current_heart = current_heart;
        this.buys_id = buys_id;
    }

    public BuyModel(){

    }

    public String getBuys_change() {
        return buys_change;
    }

    public void setBuys_change(String buys_change) {
        this.buys_change = buys_change;
    }

    public String getBuys_date() {
        return buys_date;
    }

    public void setBuys_date(String buys_date) {
        this.buys_date = buys_date;
    }

    public String getBuys_comment() {
        return buys_comment;
    }

    public void setBuys_comment(String buys_comment) {
        this.buys_comment = buys_comment;
    }

    public String getCurrent_heart() {
        return current_heart;
    }

    public void setCurrent_heart(String current_heart) {
        this.current_heart = current_heart;
    }

    public String getBuys_id() {
        return buys_id;
    }

    public void setBuys_id(String buys_id) {
        this.buys_id = buys_id;
    }
}