package foodOreder.feedme;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
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
import com.rengwuxian.materialedittext.MaterialEditText;

import foodOreder.feedme.Common.Common;
import foodOreder.feedme.Model.User;
import foodOreder.feedme.Util.ProgressGenerator;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class SignIn extends AppCompatActivity implements ProgressGenerator.OnCompleteListener {

    ActionProcessButton btnSignIn;
    ProgressGenerator progressGenerator;
    EditText editPhone, editPassword;
    FirebaseDatabase database;
    DatabaseReference table_user;
    Intent HomeIntent;
    TextView slogan, txtForgotPwd;
    com.rey.material.widget.CheckBox ckbRemember;
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/main.otf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );


        setContentView(R.layout.activity_signin);


        progressGenerator = new ProgressGenerator(this);
        btnSignIn = (ActionProcessButton) findViewById(R.id.btnSignIn);
        editPassword = (EditText) findViewById(R.id.editPassword);
        editPhone = (EditText) findViewById(R.id.editPhone);
        txtForgotPwd = (TextView) findViewById(R.id.txtForgotPWD);

        //Init Firebase
        database = FirebaseDatabase.getInstance();
        table_user = database.getReference("Users");

        slogan = (TextView) findViewById(R.id.slogan);
        Typeface face = Typeface.createFromAsset(getAssets(), "fonts/comics.ttf");
        slogan.setTypeface(face);
        ckbRemember = (com.rey.material.widget.CheckBox) findViewById(R.id.ckbRemember);

        //Init paper
        Paper.init(this);


        txtForgotPwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgotPwdDialog();
            }
        });



        btnSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (Common.isConnectedToInternet(getBaseContext())) {

/*
                    final ProgressDialog mDialog = new ProgressDialog(SignIn.this);
                    mDialog.setMessage("Please Wait...");
                    mDialog.show();
*/
                    table_user.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child(editPhone.getText().toString()).exists()) {
                                User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);
                                user.setPhone(editPhone.getText().toString());
                                if (user.getPassword().equals(editPassword.getText().toString())) {
                                    //save user and password
                                    if (ckbRemember.isChecked()) {
                                        Paper.book().write(Common.USER_KEY, editPhone.getText().toString());
                                        Paper.book().write(Common.PWD_KEY, editPassword.getText().toString());
                                    }
                                    progressGenerator.start(btnSignIn);
                                    btnSignIn.setEnabled(false);
                                    editPassword.setEnabled(false);
                                    editPhone.setEnabled(false);
                                    HomeIntent = new Intent(SignIn.this, Home.class);
                                    Common.currentUser = user;


                                    table_user.removeEventListener(this);



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

                } else {
                    Toast.makeText(SignIn.this, "Please Check Your Internet Connection !!", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void showForgotPwdDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Forgot Password");
        builder.setMessage("Enter Your Secure Code");

        LayoutInflater infalter = this.getLayoutInflater();
        View forgot_view = infalter.inflate(R.layout.forgot_password_layout, null);

        builder.setView(forgot_view);
        builder.setIcon(R.drawable.ic_security_black_24dp);

        MaterialEditText edtPhone = (MaterialEditText)forgot_view.findViewById(R.id.editPhone);
        final MaterialEditText edtSecureCode = (MaterialEditText)forgot_view.findViewById(R.id.editSecureCode);

        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                //check if user available
                table_user.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        User user = dataSnapshot.child(editPhone.getText().toString()).getValue(User.class);

                        if(user.getSecureCode().equals(edtSecureCode.getText().toString())){
                            Toast.makeText(SignIn.this, "Your Password is "+user.getPassword(), Toast.LENGTH_LONG).show();
                        }
                        else{
                            Toast.makeText(SignIn.this, "Wrong Secure Code !!!", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }
        });

        builder.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });

        builder.show();

    }

    @Override
    public void onComplete() {

        startActivity(HomeIntent);
        finish();
    }
}
