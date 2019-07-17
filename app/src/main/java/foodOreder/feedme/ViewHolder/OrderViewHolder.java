package foodOreder.feedme.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import foodOreder.feedme.Interface.ItemClickListener;
import foodOreder.feedme.R;

public class OrderViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

    public TextView orderId,orderStatus,orderPhone,orderAdress;
    private ItemClickListener itemClickListener;
    public void setItemClickListener(ItemClickListener itemClickListener) {
        this.itemClickListener = itemClickListener;
    }

    public OrderViewHolder(@NonNull View itemView) {
        super(itemView);

        orderId = (TextView) itemView.findViewById(R.id.orderId);
        orderAdress = (TextView) itemView.findViewById(R.id.orderAdress);
        orderPhone = (TextView) itemView.findViewById(R.id.orderPhone);
        orderStatus = (TextView) itemView.findViewById(R.id.orderStatus);

        itemView.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        itemClickListener.onClick(view, getAdapterPosition(), false);

    }
}
