package foodOreder.feedme.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import foodOreder.feedme.Interface.ItemClickListener;
import foodOreder.feedme.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {



    public TextView foodName,foodPrice;
    public ImageView foodImage, fav_image,shareImage;


    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);
        foodName = (TextView)itemView.findViewById(R.id.food_name);
        foodImage =(ImageView)itemView.findViewById(R.id.food_image);
        shareImage = (ImageView)itemView.findViewById(R.id.btnShare);
        foodPrice = (TextView)itemView.findViewById(R.id.food_price);

        fav_image =(ImageView)itemView.findViewById(R.id.fav);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
