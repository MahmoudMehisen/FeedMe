package foodOreder.feedme.Remote;

import foodOreder.feedme.Model.MyResponse;
import foodOreder.feedme.Model.Sender;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

public interface APIService {
    @Headers(
            {
                    "Content-Type:application/json",
                    "Authorization:key=AAAAYAOq9O4:APA91bFGqFJvAlS5nNRCCkCHgur2qLTsvOZp12EWbjW56VYwSahcyVVu5MvYMbHwkNKtdufE6UUxPPza-WwDilgV-4e4-xgmJoKdQ5DBNTDjVKxJTJC4ezBF9-WcmLRE--mE0P0Gmidu"

            }

    )


    @POST("fcm/send")
    Call<MyResponse> sendNorification(@Body Sender body);

}
