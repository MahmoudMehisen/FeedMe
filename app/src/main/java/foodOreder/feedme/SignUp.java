package foodOreder.feedme;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.dd.processbutton.iml.ActionProcessButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Model.User;
import foodOreder.feedme.Util.ProgressGenerator;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class SignUp extends AppCompatActivity implements ProgressGenerator.OnCompleteListener {

    ActionProcessButton btnSignUp;
    ProgressGenerator progressGenerator;
    MaterialEditText editPhone, editName, editPassword, editSecureCode;
    FirebaseDatabase database;
    DatabaseReference table_user;
    boolean register = false;
    Intent HomeIntent;
    TextView slogan;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/main.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        setContentView(R.layout.activity_sign_up);


        progressGenerator = new ProgressGenerator(this);
        btnSignUp = (ActionProcessButton) findViewById(R.id.btnSignUp);
        editPassword = (MaterialEditText) findViewById(R.id.editPassword);
        editPhone = (MaterialEditText) findViewById(R.id.editPhone);
        editName = (MaterialEditText) findViewById(R.id.editName);
        editSecureCode = (MaterialEditText) findViewById(R.id.editSecureCode);
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("Users");
        slogan = (TextView) findViewById(R.id.slogan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/comics.ttf");
        slogan.setTypeface(face);



        btnSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(Common.isConnectedToInternet(getBaseContext())) {
                    /*
                    final ProgressDialog mDialog = new ProgressDialog(SignUp.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();
                    */
                    table_user.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                            if (dataSnapshot.child(editPhone.getText().toString()).exists() && !register) {
                                Toast.makeText(getApplicationContext(), "Phone Number already register", Toast.LENGTH_SHORT).show();
                            } else if (!register) {
                                User user = new User(editPassword.getText().toString(), editName.getText().toString(), editSecureCode.getText().toString());
                                table_user.child(editPhone.getText().toString()).setValue(user);
                                progressGenerator.start(btnSignUp);
                                btnSignUp.setEnabled(false);
                                editPassword.setEnabled(false);
                                editPhone.setEnabled(false);
                                editName.setEnabled(false);
                                register = true;
                                HomeIntent = new Intent(SignUp.this, Home.class);
                                Common.currentUser = user;

                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });
                }
                else{
                    Toast.makeText(SignUp.this, "Please Check Your Connection !!", Toast.LENGTH_SHORT).show();
                    return;
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
