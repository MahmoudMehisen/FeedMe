package foodOreder.feedme.Common;

import foodOreder.feedme.Model.User;

public class Common {
    public static User CommonUser;

    public static String convertCodeToStatus(String status)
    {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "In my way";
        else
            return "Shipped";
    }
}
