package foodOreder.feedme;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Database.Database;
import foodOreder.feedme.Model.Order;
import foodOreder.feedme.Model.Request;
import foodOreder.feedme.ViewHolder.CartAdapter;
public class Cart extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    TextView totalPrice;
    Button btnPlace;

    List<Order> cart = new ArrayList<>();

    CartAdapter adapter ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);


        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView) findViewById(R.id.listCart);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        totalPrice = (TextView) findViewById(R.id.total);
        btnPlace = (Button) findViewById(R.id.btnPlaceOrder);

        loadListFood();

        btnPlace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cart.size()>0){
                    showAlertDialog();
                }
                else {
                    Toast.makeText(Cart.this, "Your Cart is Empty !!!", Toast.LENGTH_SHORT).show();
                }
            }
        });





    }
    private void showAlertDialog()
    {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Cart.this);
        alertDialog.setTitle("One more step!");
        alertDialog.setMessage("Enter your address: ");

        final EditText editAdress = new EditText(Cart.this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
          LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        );
        editAdress.setLayoutParams(lp);
        alertDialog.setView(editAdress);
        alertDialog.setIcon(R.drawable.ic_shopping_cart_black_24dp);

        alertDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Request request = new Request(
                        Common.CommonUser.getPhone(),
                        Common.CommonUser.getName(),
                        editAdress.getText().toString(),
                        totalPrice.getText().toString(),
                        cart

                );
                requests.child(String.valueOf(System.currentTimeMillis())).setValue(request);

                new Database(getApplicationContext()).cleanCart();
                Toast.makeText(Cart.this,"Thank you , Order Place",Toast.LENGTH_SHORT).show();
                finish();

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

    private void loadListFood() {

        cart  =  new Database(this).getCart();
        adapter = new CartAdapter(cart,this);
        adapter.notifyDataSetChanged();
        recyclerView.setAdapter(adapter);

        //Calculate Total Price
        int total = 0;
        for(Order order:cart )
        {
            total+=(Integer.parseInt(order.getPrice()))*(Integer.parseInt(order.getQuantity()));
            Locale locale = new Locale("en","US");
            NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
            totalPrice.setText(fmt.format(total));
        }
    }


    @Override
    public boolean onContextItemSelected(MenuItem item) {
        if(item.getTitle().equals(Common.DELETE)){
            deleteCart(item.getOrder());
        }
        return true;
    }

    private void deleteCart(int position) {
        //remove item bu position
        cart.remove(position);
        //delete old data form SQLite
        new Database(this).cleanCart();
        //udpate new data from list<order> to SQLite
        for(Order item:cart){
            new Database(this).addToCart(item);
        }
        //Refresh
        loadListFood();

    }
}
