package foodOreder.feedme;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ai.api.AIDataService;
import ai.api.AIListener;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Model.ChatMessage;
import foodOreder.feedme.Model.DataMessage;
import foodOreder.feedme.Model.Food;
import foodOreder.feedme.Model.MyResponse;
import foodOreder.feedme.Model.Order;
import foodOreder.feedme.Model.Request;
import foodOreder.feedme.Model.Token;
import foodOreder.feedme.Remote.APIService;
import foodOreder.feedme.Remote.IGoogleService;
import foodOreder.feedme.ViewHolder.ChatViewHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatBot extends AppCompatActivity implements AIListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    RecyclerView recyclerView;
    EditText editText;
    RelativeLayout addBtn;
    DatabaseReference ref;
    DatabaseReference foods;
    List<Food> foodList;
    List<Order> orders;
    FirebaseRecyclerOptions<ChatMessage> options;
    ArrayList<ChatMessage> chatMessages;
    FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder> adapter;
    Boolean flagFab = true;
    private AIService aiService;
    APIService mService;

    IGoogleService mGoogleMapService;


    private LocationRequest mLocationRequest;
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;

    private static final int UPDATE_INTERVAL = 5000;
    private static final int FATEST_INTERVAL = 3000;
    private static final int DISPLAYMENT = 10;

    private static final int LOCATION_REQUEST_CODE = 9999;
    private static final int PLAY_SERVICE_REQUEST = 9997;

    public String latLocation;
    public String lngLocation;
    private int totalPrice;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case LOCATION_REQUEST_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (checkPlayServices()) {
                        buildGoogleApiClient();
                        createLocationRequest();
                    }
                }
            }
            break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_bot);

        mGoogleMapService = Common.getGoogleMapAPI();

        mService = Common.getFCMService();

        turnGPSOn();

        //Runtime permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, LOCATION_REQUEST_CODE);
        } else {
            if (checkPlayServices()) {
                buildGoogleApiClient();
                createLocationRequest();
            }
        }

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 1);
        foodList = new ArrayList<>();

        foods = FirebaseDatabase.getInstance().getReference("Foods");
        foods.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                    //System.out.println(dataSnapshot1);

                    foodList.add(dataSnapshot1.getValue(Food.class));


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
        orders = new ArrayList<>();
        totalPrice = 0;


        recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
        editText = (EditText) findViewById(R.id.editText);
        addBtn = (RelativeLayout) findViewById(R.id.addBtn);
        chatMessages = new ArrayList<>();
        recyclerView.setHasFixedSize(true);
        final LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(linearLayoutManager);

        ref = FirebaseDatabase.getInstance().getReference("Chat");
        ref.keepSynced(true);

        final AIConfiguration config = new AIConfiguration("c0fe194c25f947dfbb2675f127c6ce32",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        aiService.setListener(this);


        options = new FirebaseRecyclerOptions.Builder<ChatMessage>()
                .setQuery(ref.child(Common.currentUser.getPhone()), ChatMessage.class)
                .build();

        final AIDataService aiDataService = new AIDataService(config);

        final AIRequest aiRequest = new AIRequest();


        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String message = editText.getText().toString().trim();

                if (!message.equals("")) {

                    ChatMessage chatMessage = new ChatMessage(message, "User");
                    ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);

                    aiRequest.setQuery(message);
                    new AsyncTask<AIRequest, Void, AIResponse>() {

                        @Override
                        protected AIResponse doInBackground(AIRequest... aiRequests) {
                            final AIRequest request = aiRequests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }

                        @Override
                        protected void onPostExecute(AIResponse response) {
                            if (response != null) {

                                Result result = response.getResult();
                                String reply = result.getFulfillment().getSpeech();
                                chatBotResponse(reply);

                            }
                        }
                    }.execute(aiRequest);
                } else {
                    aiService.startListening();
                }

                editText.setText("");

            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                ImageView fab_img = (ImageView) findViewById(R.id.fab_img);
                Bitmap img = BitmapFactory.decodeResource(getResources(), R.drawable.ic_send_white_24dp);
                Bitmap img1 = BitmapFactory.decodeResource(getResources(), R.drawable.ic_mic_white_24dp);


                if (s.toString().trim().length() != 0 && flagFab) {
                    ImageViewAnimatedChange(ChatBot.this, fab_img, img);
                    flagFab = false;

                } else if (s.toString().trim().length() == 0) {
                    ImageViewAnimatedChange(ChatBot.this, fab_img, img1);
                    flagFab = true;

                }


            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        adapter = new FirebaseRecyclerAdapter<ChatMessage, ChatViewHolder>(options) {


            @NonNull
            @Override
            public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.msglist, viewGroup, false);

                return new ChatViewHolder(view);
            }

            @Override
            protected void onBindViewHolder(@NonNull ChatViewHolder viewHolder, int position, @NonNull ChatMessage model) {
                if (model.getMsgUser().equals("User")) {


                    viewHolder.rightText.setText(model.getMsgText());

                    viewHolder.rightText.setVisibility(View.VISIBLE);
                    viewHolder.leftText.setVisibility(View.GONE);
                } else {
                    viewHolder.leftText.setText(model.getMsgText());

                    viewHolder.rightText.setVisibility(View.GONE);
                    viewHolder.leftText.setVisibility(View.VISIBLE);
                }
            }

        };

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);

                int msgCount = adapter.getItemCount();
                int lastVisiblePosition = linearLayoutManager.findLastCompletelyVisibleItemPosition();

                if (lastVisiblePosition == -1 ||
                        (positionStart >= (msgCount - 1) &&
                                lastVisiblePosition == (positionStart - 1))) {
                    recyclerView.scrollToPosition(positionStart);

                }

            }
        });

        recyclerView.setAdapter(adapter);
        adapter.startListening();





    }

    public void ImageViewAnimatedChange(Context c, final ImageView v, final Bitmap new_image) {
        final Animation anim_out = AnimationUtils.loadAnimation(c, R.anim.zoom_out);
        final Animation anim_in = AnimationUtils.loadAnimation(c, R.anim.zoom_in);
        anim_out.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                v.setImageBitmap(new_image);
                anim_in.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                    }
                });
                v.startAnimation(anim_in);
            }
        });
        v.startAnimation(anim_out);
    }

    public void chatBotResponse(String reply) {

        if (reply.contains("add order")) {
            String[] order = reply.substring(9).split("##");
            int amount;
            String nameOfOrder = order[1];
            if (order[0].charAt(0) >= 'a' && order[0].charAt(0) <= 'z') {
                amount = getInt(order[0]);
            } else {
                amount = Integer.parseInt(order[0]);
            }
            if (addOrder(amount, nameOfOrder)) {
                ChatMessage chatMessage = new ChatMessage("Done", "bot");
                ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
            } else {
                ChatMessage chatMessage = new ChatMessage("No food match", "bot");
                ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
            }

        }
        else if (reply.contains( "Confirmed")){


            if(orders.size()>0) {

                latLocation = String.valueOf(mLastLocation.getLatitude());
                lngLocation = String.valueOf(mLastLocation.getLongitude());
                Request request = new Request(
                        Common.currentUser.getPhone(),
                        Common.currentUser.getName(),
                        latLocation,
                        lngLocation,
                        String.valueOf(totalPrice),
                        "COD",
                        "0",
                        "",
                        "Unpaid",
                        orders
                );
                FirebaseDatabase.getInstance().getReference("Requests").child(String.valueOf(System.currentTimeMillis())).setValue(request);

                sendNotificationOrder(String.valueOf(System.currentTimeMillis()));
                ChatMessage chatMessage = new ChatMessage(reply, "bot");
                ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
                orders.clear();
                totalPrice= 0;
            }
            else{
                ChatMessage chatMessage = new ChatMessage("No Orders to confirm", "bot");
                ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
            }
        }
        else if(reply.contains("remove")){
            String[] orders = reply.split("##");
            if(deleteOrder(orders[1]))
            {
                ChatMessage chatMessage = new ChatMessage("Done", "bot");
                ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
            }
            else{
                ChatMessage chatMessage = new ChatMessage("No order match", "bot");
                ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
            }
        }
        else if(reply.contains("Check"))
        {
            String msg ="";
            int num=1;
            for(Order order :orders)
            {
                msg+="Order "+num+"#:\n";
                msg+="Name: "+order.getProductName()+'\n';
                msg+="Quantity: "+order.getQuantity()+'\n';
                msg+="Price: "+Integer.parseInt(order.getPrice())+'\n';
                msg+="-----\n";
                num++;

            }
            msg+="Total: "+totalPrice+"\n";
            ChatMessage chatMessage = new ChatMessage(msg, "bot");
            ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
        }
        else {
            ChatMessage chatMessage = new ChatMessage(reply, "bot");
            ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);
        }
    }

    private boolean deleteOrder(String order) {
        boolean ok = false;
        for(Order food:orders)
        {
            if(food.getProductName().toLowerCase().contains(order.toLowerCase()))
            {
                totalPrice -= Integer.parseInt(food.getQuantity()) * Integer.parseInt(food.getPrice());
                ok=true;
                orders.remove(food);
                break;

            }
        }
        return ok;
    }

    private boolean addOrder(int amount, String nameOfOrder) {

        boolean ok = false;
        for (Food food : foodList) {
            if (food.getName().toLowerCase().contains(nameOfOrder.toLowerCase())) {
                orders.add(new Order(
                        Common.currentUser.getPhone(),
                        foods.orderByChild("name").equalTo(nameOfOrder).getRef().getKey(),
                        food.getName(),
                        String.valueOf(amount),
                        food.getPrice(),
                        food.getDiscount(),
                        food.getImage()
                ));
                totalPrice += amount * Integer.parseInt(food.getPrice());
                ok = true;
                break;
            }
        }
        return ok;
    }

    private int getInt(String num) {
        if (num.equals("one"))
            return 1;
        else if (num.equals("two"))
            return 2;
        else if (num.equals("three"))
            return 3;
        else if (num.equals("four"))
            return 4;
        else if (num.equals("five"))
            return 5;
        else if (num.equals("six"))
            return 6;
        else if (num.equals("seven"))
            return 7;
        else if (num.equals("eight"))
            return 8;
        else if (num.equals("nine"))
            return 9;
        else if (num.equals("ten"))
            return 10;
        else if (num.equals("eleven"))
            return 11;
        else if (num.equals("twelve"))
            return 12;
        else
            return 13;
    }

    @Override
    public void onResult(ai.api.model.AIResponse response) {


        Result result = response.getResult();

        String message = result.getResolvedQuery();
        ChatMessage chatMessage0 = new ChatMessage(message, "User");
        ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage0);


        String reply = result.getFulfillment().getSpeech();
        ChatMessage chatMessage = new ChatMessage(reply, "bot");
        ref.child(Common.currentUser.getPhone()).push().setValue(chatMessage);


    }

    @Override
    public void onError(ai.api.model.AIError error) {

    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {

    }

    @Override
    public void onListeningCanceled() {

    }

    @Override
    public void onListeningFinished() {

    }

    private void turnGPSOn() {

        LocationManager manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean statusOfGPS = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if (!statusOfGPS) {
            Toast.makeText(getApplicationContext(), "Open Gps to get Your location", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
    }

    private void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FATEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(DISPLAYMENT);
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    private boolean checkPlayServices() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this, PLAY_SERVICE_REQUEST).show();
            } else {
                Toast.makeText(this, "This device is not supported", Toast.LENGTH_SHORT).show();
                finish();
            }
            return false;
        }
        return true;
    }


    @Override
    public void onConnected(@Nullable Bundle bundle) {
        displayLocation();
        startLocationUpdates();
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);


    }

    private void displayLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            Log.d("LOCATION", "Your Location : " + mLastLocation.getLatitude() + "," + mLastLocation.getLongitude());
        } else {
            Log.d("LOCATION", "Could not get your location");
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {
        mLastLocation = location;
        displayLocation();
    }

    private void sendNotificationOrder(final String order_number) {
        DatabaseReference tokens = FirebaseDatabase.getInstance().getReference("Tokens");
        Query data = tokens.orderByChild("serverToken").equalTo(true);
        data.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Token serverToken = null;
                for (DataSnapshot postSnapShot : dataSnapshot.getChildren()) {
                    serverToken = postSnapShot.getValue(Token.class);
                }

                Map<String, String> dataSend = new HashMap<>();
                dataSend.put("title", "Feed Me");
                dataSend.put("message", "You have new order " + order_number);
                DataMessage dataMessage = new DataMessage(serverToken.getToken(), dataSend);

                String test = new Gson().toJson(dataMessage);
                Log.d("Content", test);

                mService.sendNorification(dataMessage)
                        .enqueue(new Callback<MyResponse>() {
                            @Override
                            public void onResponse(Call<MyResponse> call, Response<MyResponse> response) {
                                if (response.code() == 200) {
                                    if (response.body().success == 1) {
                                        Toast.makeText(getApplicationContext(), "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(getApplicationContext(), "Failed !!!", Toast.LENGTH_SHORT).show();

                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<MyResponse> call, Throwable t) {

                            }
                        });


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

}
