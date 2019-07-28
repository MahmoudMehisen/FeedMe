package foodOreder.feedme.ViewHolder;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.List;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Database.Database;
import foodOreder.feedme.FoodDetail;
import foodOreder.feedme.Interface.ItemClickListener;
import foodOreder.feedme.Model.Favorites;
import foodOreder.feedme.Model.Order;
import foodOreder.feedme.R;

public class FavoritesAdapter extends RecyclerView.Adapter<FavoriteViewHolder> {

    private Context context;
    private List<Favorites> favorites;

    public FavoritesAdapter(Context context, List<Favorites> favorites) {
        this.context = context;
        this.favorites = favorites;
    }

    @NonNull
    @Override
    public FavoriteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(context)
                .inflate(R.layout.favorites_item,viewGroup,false);
     return new FavoriteViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FavoriteViewHolder favoriteViewHolder, final int pos) {



        favoriteViewHolder.foodName.setText(favorites.get(pos).getFoodName());
        favoriteViewHolder.foodPrice.setText(String.format("$ %s",favorites.get(pos).getFoodPrice().toString()));
                System.out.println(favorites.get(pos).getFoodImage());
                Glide.with(context).load(favorites.get(pos).getFoodImage()).centerCrop().placeholder(R.drawable.logo).into(favoriteViewHolder.foodImage);

                //Quick Cart
        favoriteViewHolder.quick_cart.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean isExist = new Database(context).checkFoodExists(favorites.get(pos).getFoodId(), Common.currentUser.getPhone());
                        if (!isExist) {
                            new Database(context).addToCart(new Order(
                                    Common.currentUser.getPhone(),
                                    favorites.get(pos).getFoodId(),
                                    favorites.get(pos).getFoodName(),
                                    "1",
                                    favorites.get(pos).getFoodPrice(),
                                    favorites.get(pos).getFoodDiscount(),
                                    favorites.get(pos).getFoodImage()
                            ));
                        } else {
                            new Database(context).IncreaseCart(Common.currentUser.getPhone(), favorites.get(pos).getFoodId());
                        }
                        Toast.makeText(context, "Added to Cart", Toast.LENGTH_SHORT).show();

                    }
                });



                final Favorites food = favorites.get(pos);
                //System.out.println(food.getName());
        favoriteViewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View view, int position, boolean isLongClick) {
                        Intent foodDetail = new Intent(context, FoodDetail.class);
                        foodDetail.putExtra("FoodId", favorites.get(pos).getFoodId());
                       context.startActivity(foodDetail);

                    }
                });
    }

    @Override
    public int getItemCount() {
        return favorites.size();
    }
    public void removeItem (int position)
    {
        favorites.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem (Favorites item, int position)
    {
        favorites.add(position, item);
        notifyItemInserted(position);
    }
    public Favorites getItem(int pos)
    {
        return favorites.get(pos);
    }

}
