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
import foodOreder.feedme.Model.User;
import foodOreder.feedme.Util.ProgressGenerator;

public class SignUp extends AppCompatActivity implements ProgressGenerator.OnCompleteListener {

    ActionProcessButton btnSignUp;
    ProgressGenerator progressGenerator;
    EditText editPhone, editName, editPassword;
    FirebaseDatabase database;
    DatabaseReference table_user;
    boolean register = false;
    Intent HomeIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);


        progressGenerator = new ProgressGenerator(this);
        btnSignUp = (ActionProcessButton) findViewById(R.id.btnSignUp);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editPhone = (EditText) findViewById(R.id.editPhone);
        editName = (EditText) findViewById(R.id.editName);
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("Users");


        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                        if (dataSnapshot.child(editPhone.getText().toString()).exists() && !register) {
                            Toast.makeText(getApplicationContext(), "Phone Number already register", Toast.LENGTH_SHORT).show();
                        } else if (!register) {
                            User user = new User(editPassword.getText().toString(), editName.getText().toString());
                            table_user.child(editPhone.getText().toString()).setValue(user);
                            progressGenerator.start(btnSignUp);
                            btnSignUp.setEnabled(false);
                            editPassword.setEnabled(false);
                            editPhone.setEnabled(false);
                            editName.setEnabled(false);
                            register = true;
                            HomeIntent = new Intent(SignUp.this, Home.class);
                            Common.CommonUser = user;

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
        startActivity(HomeIntent);
        finish();
    }
}
