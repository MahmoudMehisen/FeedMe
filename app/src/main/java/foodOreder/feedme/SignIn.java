package foodOreder.feedme;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Model.User;
import foodOreder.feedme.Util.ProgressGenerator;


public class SignIn extends AppCompatActivity implements ProgressGenerator.OnCompleteListener {

    ActionProcessButton btnSignIn;
    ProgressGenerator progressGenerator;
    EditText editPhone, editPassword;
    FirebaseDatabase database;
    DatabaseReference table_user;
    Intent HomeIntent;
    TextView slogan;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signin);


        progressGenerator = new ProgressGenerator(this);
        btnSignIn = (ActionProcessButton) findViewById(R.id.btnSignIn);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editPhone = (EditText) findViewById(R.id.editPhone);
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("Users");
        slogan = (TextView) findViewById(R.id.slogan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/comics.ttf");
        slogan.setTypeface(face);

        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
                                User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
                                user.setPhone(editPhone.getText().toString());
                                if (user.getPassword().equals(editPassword.getText().toString())) {
                                    progressGenerator.start(btnSignIn);
                                    btnSignIn.setEnabled(false);
                                    editPassword.setEnabled(false);
                                    editPhone.setEnabled(false);
                                    HomeIntent = new Intent(SignIn.this, Home.class);
                                    Common.CommonUser = user;


                                } else {
                                    Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), "User Not Exist", Toast.LENGTH_SHORT).show();
                                return;
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

                }
                else{
                    Toast.makeText(SignIn.this, "Please Check Your Internet Connection !!", Toast.LENGTH_SHORT).show();
                }
            }
        });


    }

    @Override
    public void onComplete() {

        startActivity(HomeIntent);
        finish();
    }
}
