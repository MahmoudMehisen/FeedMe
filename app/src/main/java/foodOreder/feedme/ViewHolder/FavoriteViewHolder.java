package foodOreder.feedme.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import foodOreder.feedme.Interface.ItemClickListener;
import foodOreder.feedme.R;

public class FavoriteViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {



    public TextView foodName,foodPrice;
    public ImageView foodImage, quick_cart;
    public RelativeLayout view_background;
    public LinearLayout view_foreground;

    private ItemClickListener itemClickListener;

    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }
    public FavoriteViewHolder(@NonNull View itemView) {
        super(itemView);
        foodName = (TextView)itemView.findViewById(R.id.food_name);
        foodImage =(ImageView)itemView.findViewById(R.id.food_image);
        foodPrice = (TextView)itemView.findViewById(R.id.food_price);
        quick_cart= (ImageView) itemView.findViewById(R.id.btn_quick_cart);


        view_background = (RelativeLayout) itemView.findViewById(R.id.view_background);
        view_foreground = (LinearLayout) itemView.findViewById(R.id.view_foreground);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);
    }
}
