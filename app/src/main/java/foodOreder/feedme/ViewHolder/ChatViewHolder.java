package foodOreder.feedme.ViewHolder;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import foodOreder.feedme.R;

public class ChatViewHolder extends RecyclerView.ViewHolder {


    public TextView leftText, rightText;

    public ChatViewHolder(@NonNull View itemView) {
        super(itemView);
        leftText = (TextView) itemView.findViewById(R.id.leftText);
        rightText = (TextView) itemView.findViewById(R.id.rightText);

    }

}
