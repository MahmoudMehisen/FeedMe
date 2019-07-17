package foodOreder.feedme;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Model.Request;
import foodOreder.feedme.ViewHolder.OrderViewHolder;

public class OrderStatus extends AppCompatActivity {

    public RecyclerView recyclerView;
    public RecyclerView.LayoutManager layoutManager;

    FirebaseDatabase database;
    DatabaseReference requests;

    FirebaseRecyclerOptions<Request> options;
    FirebaseRecyclerAdapter<Request, OrderViewHolder> adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_status);

        database = FirebaseDatabase.getInstance();
        requests = database.getReference("Requests");

        recyclerView = (RecyclerView)findViewById(R.id.listOrders);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        loadOrders(Common.CommonUser.getPhone());




    }

    private void loadOrders(String phone) {
        options  = new FirebaseRecyclerOptions.Builder<Request>()
                .setQuery(requests.orderByChild("phone").equalTo(phone), Request.class)
                .build();
        adapter = new FirebaseRecyclerAdapter<Request, OrderViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull OrderViewHolder holder, int position, @NonNull Request model) {

                holder.orderId.setText(adapter.getRef(position).getKey());
                holder.orderStatus.setText(convertCodeToStatus(model.getStatus()));
                holder.orderPhone.setText(model.getPhone());
                holder.orderAdress.setText(model.getAddress());
            }

            @NonNull
            @Override
            public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.order_layout, viewGroup, false);
                return new  OrderViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();

    }
    private String convertCodeToStatus(String status)
    {
        if(status.equals("0"))
            return "Placed";
        else if(status.equals("1"))
            return "In my way";
        else
            return "Shipped";
    }
}
