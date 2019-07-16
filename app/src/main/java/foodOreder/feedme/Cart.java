package foodOreder.feedme;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.Button;
import android.widget.TextView;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foodOreder.feedme.Model.Order;
import foodOreder.feedme.ViewHolder.CartAdapter;
import foodOreder.feedme.Database.*;
public class Cart extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference request;

    TextView totalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        database = FirebaseDatabase.getInstance();
        request = database.getReference("Requests");

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        totalPrice = (TextView) findViewById(R.id.total);
        btnPlace = (Button) findViewById(R.id.btnPlaceOrder);

        loadListFood();





    }

    private void loadListFood() {

        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));
        cart.add(new Order("a","a","1","1","a"));


        adapter = new CartAdapter(cart,this);
        recyclerView.setAdapter(adapter);

        int total = 0;
        for(Order order:cart )
        {
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
            Locale locale = new Locale("en","US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            totalPrice.setText(fmt.format(total));
        }
        new Database(getApplicationContext()).addToCart(new Order("a","a","1","1","a"));

    }
}
