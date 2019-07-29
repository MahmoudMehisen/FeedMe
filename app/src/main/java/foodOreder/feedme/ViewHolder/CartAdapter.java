package foodOreder.feedme.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import foodOreder.feedme.Cart;
import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Database.Database;
import foodOreder.feedme.Model.Order;
import foodOreder.feedme.R;



public class CartAdapter extends RecyclerView.Adapter<CartViewHolder> {

    private List<Order> listData = new ArrayList<>();
    private Cart cart;

    public CartAdapter(List<Order> listData, Cart cart) {
        this.listData = listData;
        this.cart = cart;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        LayoutInflater inflater = LayoutInflater.from(cart);
        View itemView =  inflater.inflate(R.layout.cart_item,viewGroup,false);
        return new CartViewHolder(itemView);

    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder cartViewHolder, final int i) {

        Glide.with(cart.getBaseContext())
                .load(listData.get(i).getImage())
                .centerCrop()
                .placeholder(R.drawable.placeholder)
                .into(cartViewHolder.cart_image);

        cartViewHolder.btn_quantity.setNumber(listData.get(i).getQuantity());
        cartViewHolder.btn_quantity.setOnValueChangeListener(new ElegantNumberButton.OnValueChangeListener() {
            @Override
            public void onValueChange(ElegantNumberButton view, int oldValue, int newValue) {
                Order order =  listData.get(i);
                order.setQuantity(String.valueOf(newValue));
                new Database(cart).updateCart(order);

                //update txttotal
                //Calculate Total Price
                int total = 0;

                List<Order> orders = new Database(cart).getCart(Common.currentUser.getPhone());
                for (Order item : orders)
                    total += (Integer.parseInt(order.getPrice())) * (Integer.parseInt(item.getQuantity()));


                Locale locale = new Locale("en", "US");
                NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
                cart.totalPrice.setText(fmt.format(total));
            }
        });


        Locale locale = new Locale("en","US");
        NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
        int price = (Integer.parseInt(listData.get(i).getPrice()))*(Integer.parseInt(listData.get(i).getQuantity()));
        cartViewHolder.cartPrice.setText( fmt.format(price));
        cartViewHolder.cartName.setText(listData.get(i).getProductName());



    }

    @Override
    public int getItemCount() {
        return listData.size();
    }



    public Order getItem (int position)
    {
        return  listData.get(position);
    }

    public void removeItem (int position)
    {
        listData.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem (Order item, int position)
    {
        listData.add(position, item);
        notifyItemInserted(position);
    }


}
