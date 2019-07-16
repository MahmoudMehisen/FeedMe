package foodOreder.feedme.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import foodOreder.feedme.Interface.ItemClickListener;
import foodOreder.feedme.R;

public class FoodViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {



    public TextView foodName;
    public ImageView foodImage;


    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public FoodViewHolder(@NonNull View itemView) {
        super(itemView);
        foodName = (TextView)itemView.findViewById(R.id.food_name);
        foodImage =(ImageView)itemView.findViewById(R.id.food_image);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
