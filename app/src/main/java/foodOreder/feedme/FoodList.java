package foodOreder.feedme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mancj.materialsearchbar.MaterialSearchBar;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.ArrayList;
import java.util.List;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Database.Database;
import foodOreder.feedme.Interface.ItemClickListener;
import foodOreder.feedme.Model.Food;
import foodOreder.feedme.Model.Order;
import foodOreder.feedme.ViewHolder.FoodViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class FoodList extends AppCompatActivity {

    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference foodList;
    String categoryId;
    FirebaseRecyclerOptions<Food> options;
    FirebaseRecyclerAdapter<Food, FoodViewHolder> adapter;

    //Search Functionality
    FirebaseRecyclerAdapter<Food, FoodViewHolder> searchAdapter;
    FirebaseRecyclerOptions<Food> searchOptions;
    List<String> suggestList = new ArrayList<>();
    MaterialSearchBar materialSearchBar;


    //Favorites
    Database localDB;

    //Facebook Share
    CallbackManager callbackManager;
    ShareDialog shareDialog;

    SwipeRefreshLayout swipeRefreshLayout;

    //Create Target from Picasso
    Target target = new Target() {
        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            SharePhoto photo = new SharePhoto.Builder()
                    .setBitmap(bitmap)
                    .build();

            if (ShareDialog.canShow(SharePhotoContent.class)) {
                SharePhotoContent content = new SharePhotoContent.Builder()
                        .addPhoto(photo)
                        .build();
                shareDialog.show(content);
            }
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {

        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    };

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/main.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        setContentView(R.layout.activity_food_list);

        //Init Facebook
        callbackManager = CallbackManager.Factory.create();
        shareDialog = new ShareDialog(this);


        // swip
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary, R.color.holo_green_light, R.color.holo_orange_light, R.color.blue_normal);


        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() != null) {
                    categoryId = getIntent().getStringExtra("CategoryId");
                }
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        loadListFood(categoryId);
                    } else {
                        Toast.makeText(FoodList.this, "Please Check Your Internet Connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (getIntent() != null) {
                    categoryId = getIntent().getStringExtra("CategoryId");
                }
                if (!categoryId.isEmpty() && categoryId != null) {
                    if (Common.isConnectedToInternet(getBaseContext())) {
                        loadListFood(categoryId);
                    } else {
                        Toast.makeText(FoodList.this, "Please Check Your Internet Connection !!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
        });

        database = FirebaseDatabase.getInstance();
        foodList = database.getReference("Foods");


        //Local DB
        localDB = new Database(this);

        recyclerView = (RecyclerView) findViewById(R.id.recyclerFood);
        recyclerView.setHasFixedSize(true);

        layoutManager = new LinearLayoutManager(this);

        recyclerView.setLayoutManager(layoutManager);


        materialSearchBar = (MaterialSearchBar) findViewById(R.id.searchBar);
        materialSearchBar.setHint("Enter your food");

        loadSuggest();
        materialSearchBar.setLastSuggestions(suggestList);
        materialSearchBar.setCardViewElevation(10);
        materialSearchBar.addTextChangeListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                List<String> suggest = new ArrayList<>();
                for (String search : suggestList) {
                    if (search.toLowerCase().contains(materialSearchBar.getText().toLowerCase())) {
                        suggest.add(search);
                    }
                }
                materialSearchBar.setLastSuggestions(suggest);


            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        materialSearchBar.setOnSearchActionListener(new MaterialSearchBar.OnSearchActionListener() {
            @Override
            public void onSearchStateChanged(boolean enabled) {
                if (!enabled) {
                    recyclerView.setAdapter(adapter);
                    adapter.startListening();
                }
            }

            @Override
            public void onSearchConfirmed(CharSequence text) {
                startSearch(text);

            }

            @Override
            public void onButtonClicked(int buttonCode) {

            }
        });

    }

    private void startSearch(CharSequence text) {
        searchOptions = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodList.orderByChild("name").equalTo(text.toString()), Food.class)
                .build();
        searchAdapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(searchOptions) {
            @Override
            protected void onBindViewHolder(@NonNull FoodViewHolder holder, final int position, @NonNull final Food model) {

                holder.foodName.setText(model.getName());
                holder.foodPrice.setText(String.format("$ %s",model.getPrice().toString()));
                Picasso.with(getApplicationContext()).load(model.getImage()).into(holder.foodImage);


                //Quick Cart
                holder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Database(getApplicationContext()).addToCart(new Order(
                                adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount()
                        ));
                        Toast.makeText(FoodList.this,"Added to Cart",Toast.LENGTH_SHORT).show();

                    }
                });


                final Food food = model;
                //System.out.println(food.getName());
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", searchAdapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });
            }


            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item, viewGroup, false);

                return new FoodViewHolder(view);
            }
        };
        recyclerView.setAdapter(searchAdapter);
        searchAdapter.startListening();
        swipeRefreshLayout.setRefreshing(false);

    }

    private void loadSuggest() {
        foodList.orderByChild("menuId").equalTo(categoryId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot postSnapshot : dataSnapshot.getChildren()) {
                    Food item = postSnapshot.getValue(Food.class);
                    suggestList.add(item.getName());
                    System.out.println(item.getName());

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }

    private void loadListFood(String categoryId) {
        options = new FirebaseRecyclerOptions.Builder<Food>()
                .setQuery(foodList.orderByChild("menuId").equalTo(categoryId), Food.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Food, FoodViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FoodViewHolder holder, final int position, @NonNull final Food model) {

                holder.foodName.setText(model.getName());
                holder.foodPrice.setText(String.format("$ %s",model.getPrice().toString()));
                Picasso.with(getApplicationContext()).load(model.getImage()).into(holder.foodImage);

                //Quick Cart
                holder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new Database(getApplicationContext()).addToCart(new Order(
                                adapter.getRef(position).getKey(),
                                model.getName(),
                                "1",
                                model.getPrice(),
                                model.getDiscount()
                        ));
                        Toast.makeText(FoodList.this,"Added to Cart",Toast.LENGTH_SHORT).show();

                    }
                });


                //Add Favorites
                if (localDB.isFavorites(adapter.getRef(position).getKey())) {
                    holder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                }

                // click ot share
                holder.shareImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Picasso.with(getApplicationContext())
                                .load(model.getImage())
                                .into(target);
                    }
                });

                //click to change status of favorits
                holder.fav_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (!localDB.isFavorites(adapter.getRef(position).getKey())) {
                            localDB.AddToFavorites(adapter.getRef(position).getKey());
                            holder.fav_image.setImageResource(R.drawable.ic_favorite_black_24dp);
                            Toast.makeText(FoodList.this, "" + model.getName() + " was added to Favorites", Toast.LENGTH_SHORT).show();
                        } else {
                            localDB.RemoveFromFavorites(adapter.getRef(position).getKey());
                            holder.fav_image.setImageResource(R.drawable.ic_favorite_border_black_24dp);
                            Toast.makeText(FoodList.this, "" + model.getName() + " was removed from Favorites", Toast.LENGTH_SHORT).show();
                        }
                    }
                });


                final Food food = model;
                //System.out.println(food.getName());
                holder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(FoodList.this, FoodDetail.class);
                        foodDetail.putExtra("FoodId", adapter.getRef(position).getKey());
                        startActivity(foodDetail);

                    }
                });

            }

            @NonNull
            @Override
            public FoodViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.food_item, viewGroup, false);

                return new FoodViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
        swipeRefreshLayout.setRefreshing(false);

    }
}
