package foodOreder.feedme;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.FirebaseApp;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Model.Rating;
import foodOreder.feedme.ViewHolder.showCommentViewHolder;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class ShowComment extends AppCompatActivity {


    RecyclerView recyclerView;
    RecyclerView.LayoutManager layoutManager;


    FirebaseDatabase database;
    DatabaseReference ratingTbl;

    SwipeRefreshLayout mSwipeRefreshLayout;


    FirebaseRecyclerAdapter<Rating,showCommentViewHolder> adapter;

    String foodId="";
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (adapter != null){
            adapter.stopListening();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder().setDefaultFontPath("fonts/of.otf")
        .setFontAttrId(R.attr.fontPath)
        .build());
        setContentView(R.layout.activity_show_comment);

        //fireBase

        database = FirebaseDatabase.getInstance();
        ratingTbl = database.getReference("Rating");

        recyclerView = (RecyclerView) findViewById(R.id.recyclerComment);

        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //swipeLayout
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeLayout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (getIntent() != null ){
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }

                if (!foodId.isEmpty() && foodId != null){
                    //create request query

                    Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>().setQuery(query, Rating.class).build();

                    adapter = new FirebaseRecyclerAdapter<Rating, showCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull showCommentViewHolder holder, int position, @NonNull Rating model) {

                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserPhone.setText(model.getUserPhone());

                        }

                        @NonNull
                        @Override
                        public showCommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_comment_layout,viewGroup,false);

                            return new showCommentViewHolder(view);
                        }
                    };

                    loadcomment(foodId);

                }
            }
        });

        // thread to load comment on first lunch

        mSwipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(true);

                if (getIntent() != null ){
                    foodId = getIntent().getStringExtra(Common.INTENT_FOOD_ID);
                }

                if (!foodId.isEmpty() && foodId != null){
                    //create request query

                    Query query = ratingTbl.orderByChild("foodId").equalTo(foodId);

                    FirebaseRecyclerOptions<Rating> options = new FirebaseRecyclerOptions.Builder<Rating>().setQuery(query, Rating.class).build();

                    adapter = new FirebaseRecyclerAdapter<Rating, showCommentViewHolder>(options) {
                        @Override
                        protected void onBindViewHolder(@NonNull showCommentViewHolder holder, int position, @NonNull Rating model) {

                            holder.ratingBar.setRating(Float.parseFloat(model.getRateValue()));
                            holder.txtComment.setText(model.getComment());
                            holder.txtUserPhone.setText(model.getUserPhone());

                        }

                        @NonNull
                        @Override
                        public showCommentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
                            View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.show_comment_layout,viewGroup,false);

                            return new showCommentViewHolder(view);
                        }
                    };

                    loadcomment(foodId);

                }

            }
        });

    }

    private void loadcomment(String foodId) {
        adapter.stopListening();

        recyclerView.setAdapter(adapter);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}
