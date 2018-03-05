package edu.wm.cs420.juicebox;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.NumberFormat;

import edu.wm.cs420.juicebox.user.UserUtils;


public class NewPartyActivity extends AppCompatActivity {
    private static String TAG = "Juicebox-NewPartyActivity";

    private Button btnCreateParty;
    // Party info
    private TextInputLayout tilPartyName;
    private TextInputLayout tilPartyDesc;
    private String partyName;
    private String partyDesc;
    // Radius junk
    private SeekBar sbRadius;
    private TextView tvRadiusDisplay;
    private int radius = 0;
    // Radio Group
    private RadioGroup rgPrivacySet;
    private int privacy;

    @SuppressLint("SetText")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_party);
        // Watch party name
        tilPartyName = findViewById(R.id.party_name_input);
        tilPartyName.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                partyName = editable.toString();
                checkFormCompletion();
            }
        });
        // Watch party description
        tilPartyDesc = findViewById(R.id.party_description_input);
        tilPartyDesc.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {}
            @Override
            public void afterTextChanged(Editable editable) {
                partyDesc = editable.toString();
                checkFormCompletion();
            }
        });
        // Manage the radio group
        rgPrivacySet = findViewById(R.id.privacy_toggle_rGroup);
        rgPrivacySet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int i) {
                for (int j = 0; j < radioGroup.getChildCount(); j++) {
                    ToggleButton view = (ToggleButton) radioGroup.getChildAt(j);
                    view.setChecked(view.getId() == i);
                }
            }
        });
        // Seekbar radius listener
        tvRadiusDisplay = findViewById(R.id.radius_display_text);
        sbRadius = findViewById(R.id.radius_seekbar);
        sbRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                radius = i;
                tvRadiusDisplay.setText(NumberFormat.getInstance().format(i));
                checkFormCompletion();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar){}
        });

        // Start party button listener
        btnCreateParty = findViewById(R.id.create_party_button);
        btnCreateParty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                hostNewParty();
            }
        });
    }
    private void checkFormCompletion(){
        if(checkInfoSection() && checkPrivacySection() && checkRadiusSection()){
            // Enable the create party button
            Context context = getApplicationContext();
            CharSequence text = "All Fields Ready!!";
            int duration = Toast.LENGTH_SHORT;

            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
        }else{
            // Disable the create party button
        }
    }

    private boolean checkPrivacySection() {
        return privacy >= 0;
    }

    private boolean checkRadiusSection() {
        return radius > 0;
    }

    private boolean checkInfoSection(){
        return !TextUtils.isEmpty(partyName) && !TextUtils.isEmpty(partyDesc);
    }

    public void onToggle(View view) {
        ((RadioGroup)view.getParent()).check(view.getId());
        // app specific stuff ..
        switch(view.getId()) {
            case R.id.friendly_toggle_button:
                privacy = 0;
                break;
            case R.id.invite_toggle_button:
                privacy = 1;
                break;
            case R.id.public_toggle_button:
                privacy = 2;
                break;
            default:
                privacy = -1;
                break;
        };
        checkFormCompletion();
    }

    private void hostNewParty(){
        // TODO: Get the User's Last known location (doesn't have to be a string)
        String latLong = "37.269228,-76.712753"; // Roughly my room in williamsburg
        // create the party
        UserUtils.hostParty(partyName, partyDesc, latLong, radius, privacy);
        finish();
    }
}
