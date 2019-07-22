package foodOreder.feedme.Model;

public class Rating {
    private String UserPhone, FoodId, RateValue, comment;

    public Rating(){}


    public Rating(String userPhone, String foodId, String rateValue, String comment) {
        UserPhone = userPhone;
        FoodId = foodId;
        RateValue = rateValue;
        this.comment = comment;
    }

    public String getUserPhone() {
        return UserPhone;
    }

    public void setUserPhone(String userPhone) {
        UserPhone = userPhone;
    }

    public String getFoodId() {
        return FoodId;
    }

    public void setFoodId(String foodId) {
        FoodId = foodId;
    }

    public String getRateValue() {
        return RateValue;
    }

    public void setRateValue(String rateValue) {
        RateValue = rateValue;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
