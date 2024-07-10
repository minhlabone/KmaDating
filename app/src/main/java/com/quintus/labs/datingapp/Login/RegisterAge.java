package com.quintus.labs.datingapp.Login;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.Toast;

import com.quintus.labs.datingapp.R;
import com.quintus.labs.datingapp.Utils.User;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class RegisterAge extends AppCompatActivity {

    String password;
    User user;
    SimpleDateFormat dateFormatter = new SimpleDateFormat("MM-dd-yyyy");
    private DatePicker ageSelectionPicker;
    private AppCompatButton ageContinueButton;
    // age limit attribute
    private int ageLimit = 13;
    private int age;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_age);

        Intent intent = getIntent();
        user = (User) intent.getSerializableExtra("classUser");
        password = intent.getStringExtra("password");

        ageSelectionPicker = findViewById(R.id.ageSelectionPicker);


        ageContinueButton = findViewById(R.id.ageContinueButton);

        ageContinueButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openHobbiesEntryPage();
            }
        });


    }

    public void openHobbiesEntryPage() {
        //Sử dụng phương thức getAge để tính tuổi dựa trên năm, tháng và ngày từ DatePicker
        age = getAge(ageSelectionPicker.getYear(), ageSelectionPicker.getMonth(), ageSelectionPicker.getDayOfMonth());

        // if user is above 13 years old then only he/she will be allowed to register to the system.
        if (age > ageLimit) {
            user.setAge(age);
            // code for converting date to string ( chuyển đổi ngày sinh DatePicker sang Date
            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.YEAR, ageSelectionPicker.getYear());
            cal.set(Calendar.MONTH, ageSelectionPicker.getMonth());
            cal.set(Calendar.DAY_OF_MONTH, ageSelectionPicker.getDayOfMonth());
            Date dateOfBirth = cal.getTime();
            //set ngày sinh thành String
            String strDateOfBirth = dateFormatter.format(dateOfBirth);

            // đặt thuộc tính ngày sinh cho đối tượng age
            user.setDateOfBirth(strDateOfBirth);
           //Chuyển sang Sở thích
            Intent intent = new Intent(this, RegisterHobby.class);
            intent.putExtra("password", password);
            intent.putExtra("classUser", user);
//            intent.putExtra("age", age);
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "Độ tuổi của người dùng phải lớn hơn " + ageLimit + " !!!", Toast.LENGTH_SHORT).show();
        }

    }

    // Tính tuổi hiện tại của người dùng
    private int getAge(int year, int month, int day) {
        Calendar dateOfBirth = Calendar.getInstance();
        Calendar today = Calendar.getInstance();

        dateOfBirth.set(year, month, day);
      // tính tuổi bằng năm hiện tại - năm sinh
        int age = today.get(Calendar.YEAR) - dateOfBirth.get(Calendar.YEAR);
      //ktra ngày hiện tại nhỏ hơn ngày năm sinh k. nếu có thì giảm đi 1 tuổi vì chưa tới sinh nhật
        if (today.get(Calendar.DAY_OF_YEAR) < dateOfBirth.get(Calendar.DAY_OF_YEAR)) {
            age--;
        }

        return age;
    }
}
