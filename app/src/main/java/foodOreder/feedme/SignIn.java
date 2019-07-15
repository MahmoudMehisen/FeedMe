package foodOreder.feedme;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


import foodOreder.feedme.Common.Common;
import foodOreder.feedme.utils.ProgressGenerator;


public class SignIn extends AppCompatActivity implements ProgressGenerator.OnCompleteListener {

    ActionProcessButton btnSignIn;
    ProgressGenerator progressGenerator;
    EditText editPhone,editPassword;
    FirebaseDatabase database;
    DatabaseReference table_user;



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



        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if(dataSnapshot.child(editPhone.getText().toString()).exists()) {
                            User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
                            if (user.getPassword().equals(editPassword.getText().toString())) {
                                progressGenerator.start(btnSignIn);
                                btnSignIn.setEnabled(false);
                                editPassword.setEnabled(false);
                                editPhone.setEnabled(false);

                                Intent HomeIntent = new Intent(SignIn.this, Home.class);
                                Common.CommonUser = user;
                                startActivity(HomeIntent);
                                finish();


                            } else {
                                Toast.makeText(getApplicationContext(), "Wrong Password", Toast.LENGTH_SHORT).show();
                            }
                        }
                        else
                        {
                            Toast.makeText(getApplicationContext(), "User Not Exist", Toast.LENGTH_SHORT).show();
                        }

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });


    }

    @Override
    public void onComplete() {
        Toast.makeText(this, "Done", Toast.LENGTH_LONG).show();
    }
}
