package foodOreder.feedme;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Database.Database;
import foodOreder.feedme.Model.Food;
import foodOreder.feedme.Model.Order;

public class FoodDetail extends AppCompatActivity {

    TextView foodName, foodPrice, foodDescription;
    ImageView foodImage;
    CollapsingToolbarLayout collapsingToolbarLayout;
    FloatingActionButton btnCart;
    ElegantNumberButton numberButton;

    String foodId;
    FirebaseDatabase database;
    DatabaseReference foods;

    Food currentFood;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food_detail);

        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");

        numberButton = (ElegantNumberButton) findViewById(R.id.numberButton);
        btnCart = (FloatingActionButton) findViewById(R.id.btnCart);
        foodDescription = (TextView) findViewById(R.id.foodDescription);
        foodImage = (ImageView) findViewById(R.id.imageFood);
        foodPrice = (TextView) findViewById(R.id.foodPrice);
        foodName = (TextView) findViewById(R.id.foodName);

        collapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandedAppbar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppbar);

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Database(getApplicationContext()).addToCart(new Order(
                        foodId,
                        currentFood.getName(),
                        numberButton.getNumber(),
                        currentFood.getPrice(),
                        currentFood.getDiscount()
                ));
                Toast.makeText(getApplicationContext(),"Added to Cart",Toast.LENGTH_SHORT).show();
            }
        });


        if (getIntent() != null) {
            foodId = getIntent().getStringExtra("FoodId");
        }
        if (!foodId.isEmpty()) {
            if(Common.isConnectedToInternet(getBaseContext())){
                getDetailFood(foodId);
            }
            else{
                Toast.makeText(FoodDetail.this, "Please Check Your Internet Connection !!", Toast.LENGTH_SHORT).show();
                return;
            }
        }


    }

    private void getDetailFood(final String foodId) {

        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);

                Picasso.with(getApplicationContext()).load(currentFood.getImage()).into(foodImage);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                foodPrice.setText(currentFood.getPrice());
                foodDescription.setText(currentFood.getDescription());


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
}
