package foodOreder.feedme;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Common.Config;
import foodOreder.feedme.Database.Database;
import foodOreder.feedme.Helper.RecyclerItemTouchHelper;
import foodOreder.feedme.Interface.RecyclerItemTouchHelperListener;
import foodOreder.feedme.Model.DataMessage;
import foodOreder.feedme.Model.MyResponse;
import foodOreder.feedme.Model.Order;
import foodOreder.feedme.Model.Request;
import foodOreder.feedme.Model.Token;
import foodOreder.feedme.Remote.APIService;
import foodOreder.feedme.Remote.IGoogleService;
import foodOreder.feedme.ViewHolder.CartAdapter;
import foodOreder.feedme.ViewHolder.CartViewHolder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class Cart extends AppCompatActivity
        implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, RecyclerItemTouchHelperListener {


    public static final int PAYPAL_REQUEST_CODE = 9999;

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;


    public TextView totalPrice;
    Button btnPlace;

    IGoogleService mGoogleMapService;

    RelativeLayout rootLayout;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter;

    APIService mService;
    //Paypal payment
    static PayPalConfiguration config = new PayPalConfiguration()
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX) //use sandbox because we test
            .clientId(Config.PAYPAL_CLIENT_ID);
    String address, comment;

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
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/main.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        setContentView(R.layout.activity_cart);

        mGoogleMapService = Common.getGoogleMapAPI();

        mService = Common.getFCMService();


        rootLayout = (RelativeLayout) findViewById(R.id.rootLayout);

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


        //Init paypal
        Intent intent = new Intent(this, PayPalService.class);
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
        startService(intent);


        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        totalPrice = (TextView) findViewById(R.id.total);
        btnPlace = (Button) findViewById(R.id.btnPlaceOrder);


        //swipe to delete
        ItemTouchHelper.SimpleCallback itemTouchHelperCallback = new RecyclerItemTouchHelper(0, ItemTouchHelper.LEFT, this);
        new ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(recyclerView);


        loadListFood();

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (cart.size() > 0) {
                    showAlertDialog();
                } else {
                    Toast.makeText(Cart.this, "Your Cart is Empty !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });


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

    private void showAlertDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        LayoutInflater inflater = this.getLayoutInflater();
        View order_address_comment = inflater.inflate(R.layout.order_address_comment, null);


        final MaterialEditText edtComment = (MaterialEditText) order_address_comment.findViewById(R.id.edtComment);
        final RadioButton rdiShipToAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiShipToAddress);
        final RadioButton rdiHomeAddress = (RadioButton) order_address_comment.findViewById(R.id.rdiHomeAddress);
        final RadioButton rdiCOD = (RadioButton) order_address_comment.findViewById(R.id.rdiCOD);
        final RadioButton rdiPaypal = (RadioButton) order_address_comment.findViewById(R.id.rdiPaypal);
        final RadioButton rdiFeedMeBalance = (RadioButton) order_address_comment.findViewById(R.id.rdiFeedMeBalance);
        final CheckBox checkHome = (CheckBox) order_address_comment.findViewById(R.id.checkHome);

        rdiShipToAddress.setChecked(true);
        rdiCOD.setChecked(true);

        if (Common.currentUser.getHomeLat().equals("0")) {
            rdiHomeAddress.setVisibility(View.INVISIBLE);
            checkHome.setVisibility(View.VISIBLE);

        } else {
            rdiHomeAddress.setVisibility(View.VISIBLE);
            checkHome.setVisibility(View.INVISIBLE);
        }


        alertDialog.setView(order_address_comment);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {


                comment = edtComment.getText().toString();

                if (rdiShipToAddress.isChecked()) {
                    latLocation = String.valueOf(mLastLocation.getLatitude());
                    lngLocation = String.valueOf(mLastLocation.getLongitude());
                } else if (rdiHomeAddress.isChecked()) {
                    latLocation = Common.currentUser.getHomeLat();
                    lngLocation = Common.currentUser.getHomeLng();
                }

                if (rdiCOD.isChecked()) {

                    Request request = new Request(
                            Common.currentUser.getPhone(),
                            Common.currentUser.getName(),
                            latLocation,
                            lngLocation,
                            totalPrice.getText().toString(),
                            "COD",
                            "0",
                            comment,
                            "Unpaid",
                            cart
                    );
                    requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);

                } else if (rdiPaypal.isChecked()) {
                    String formatAmount = totalPrice.getText().toString().replace("$", "").replace(",", "");

                    PayPalPayment payPalPayment = new PayPalPayment(new BigDecimal(formatAmount), "USD", "Feed Me App Order", PayPalPayment.PAYMENT_INTENT_SALE);

                    Intent intent = new Intent(getApplicationContext(), PaymentActivity.class);
                    intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);
                    intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payPalPayment);
                    startActivityForResult(intent, PAYPAL_REQUEST_CODE);
                } else if (rdiFeedMeBalance.isChecked()) {
                    double amount = 0;

                    try {
                        amount = Common.formatCurrency(totalPrice.getText().toString(), Locale.US).doubleValue();
                        if (Double.parseDouble(Common.currentUser.getBalance().toString()) >= amount) {
                            Request request = new Request(
                                    Common.currentUser.getPhone(),
                                    Common.currentUser.getName(),
                                    latLocation,
                                    lngLocation,
                                    totalPrice.getText().toString(),
                                    "Feed Me Balance",
                                    "0",
                                    comment,
                                    "Paid",
                                    cart
                            );
                            requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);

                            double balance = Double.parseDouble(Common.currentUser.getBalance().toString()) - amount;
                            Common.currentUser.setBalance(balance);
                            Map<String, Object> update_balance = new HashMap<>();
                            update_balance.put("balance", balance);
                            FirebaseDatabase.getInstance()
                                    .getReference("Users")
                                    .child(Common.currentUser.getPhone())
                                    .updateChildren(update_balance);

                        } else {
                            Toast.makeText(Cart.this, "Your balance not enough, Please choose other payment", Toast.LENGTH_SHORT).show();
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }


                if (Common.currentUser.getHomeLat().equals("0") && checkHome.isChecked()) {

                    Common.currentUser.setHomeLat(String.valueOf(mLastLocation.getLatitude()));
                    Common.currentUser.setHomeLng(String.valueOf(mLastLocation.getLongitude()));
                    Map<String, Object> HomeLocation = new HashMap<>();
                    HomeLocation.put("homeLat", Common.currentUser.getHomeLat());
                    HomeLocation.put("homeLng", Common.currentUser.getHomeLng());


                    //make update
                    DatabaseReference user = FirebaseDatabase.getInstance().getReference("Users");
                    user.child(Common.currentUser.getPhone())
                            .updateChildren(HomeLocation);


                }


                String order_number = String.valueOf(System.currentTimeMillis());
                new Database(getApplicationContext()).cleanCart(Common.currentUser.getPhone());
                Toast.makeText(Cart.this, "Thank you , Order Placed", Toast.LENGTH_SHORT).show();
                sendNotificationOrder(order_number);


            }
        });
        alertDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.show();

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
                                        Toast.makeText(Cart.this, "Thank you, Order Place", Toast.LENGTH_SHORT).show();
                                        finish();
                                    } else {
                                        Toast.makeText(Cart.this, "Failed !!!", Toast.LENGTH_SHORT).show();

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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == PAYPAL_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                PaymentConfirmation confirmation = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);
                if (confirmation != null) {
                    try {
                        String paymentDetail = confirmation.toJSONObject().toString(4);
                        JSONObject jsonObject = new JSONObject(paymentDetail);


                        Request request = new Request(
                                Common.currentUser.getPhone(),
                                Common.currentUser.getName(),
                                latLocation,
                                lngLocation,
                                totalPrice.getText().toString(),
                                "Paypal",
                                "0",
                                comment,
                                jsonObject.getJSONObject("response").getString("state"),
                                cart
                        );
                        requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            } else if (requestCode == Activity.RESULT_CANCELED) {
                Toast.makeText(this, "Payment cancel", Toast.LENGTH_SHORT).show();
            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                Toast.makeText(this, "invalid payment", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void loadListFood() {

        cart = new Database(this).getCart(Common.currentUser.getPhone());
        adapter = new CartAdapter(cart, this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate Total Price
        int total = 0;
        for (Order order : cart) {
            total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(order.getQuantity()));

        }
        Locale locale = new Locale("en", "US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        totalPrice.setText(fmt.format(total));
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if (item.getTitle().equals(Common.DELETE)) {
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int position) {
        //remove item bu position
        cart.remove(position);
        //delete old data form SQLite
        new Database(this).cleanCart(Common.currentUser.getPhone());
        //udpate new data from list<order> to SQLite
        for (Order item : cart) {
            new Database(this).addToCart(item);
        }
        //Refresh
        loadListFood();

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

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction, int position) {
        if (viewHolder instanceof CartViewHolder) {
            String name = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition()).getProductName();

            final Order deleteItem = ((CartAdapter) recyclerView.getAdapter()).getItem(viewHolder.getAdapterPosition());
            final int deleteIndex = viewHolder.getAdapterPosition();

            adapter.removeItem(deleteIndex);
            new Database(getBaseContext()).removeFromCart(deleteItem.getProductId(), Common.currentUser.getPhone());


            //update txttotal
            //Calculate Total Price
            int total = 0;
            List<Order> orders = new Database(getBaseContext()).getCart(Common.currentUser.getPhone()); /// update salem
            for (Order item : orders)
                total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
            Locale locale = new Locale("en", "US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            totalPrice.setText(fmt.format(total));


            // Make Snackbar
            Snackbar snackbar = Snackbar.make(rootLayout, name + " removed from cart!", Snackbar.LENGTH_LONG);
            snackbar.setAction("UNDO", new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    adapter.restoreItem(deleteItem, deleteIndex);
                    new Database(getApplicationContext()).addToCart(deleteItem);

                    //update txttotal
                    //Calculate Total Price
                    int total = 0;
                    List<Order> orders = new Database(getBaseContext()).getCart(Common.currentUser.getPhone()); /// update salem
                    for (Order item : orders)
                        total += (Integer.parseInt(item.getPrice())) * (Integer.parseInt(item.getQuantity()));
                    Locale locale = new Locale("en", "US");
                    NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                    totalPrice.setText(fmt.format(total));

                }
            });
            snackbar.setActionTextColor(Color.YELLOW);
            snackbar.show();


        }

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

}
