/*
 * MIT License
 *
 * Copyright (c) 2017 CMPUT301W17T12
 * Authors rsauveho vuk bfleyshe henrywei cs3
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.projectattitude.projectattitude.Activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.projectattitude.projectattitude.Abstracts.MoodActivity;
import com.projectattitude.projectattitude.Objects.DatePickerEditText;
import com.projectattitude.projectattitude.Objects.Mood;
import com.projectattitude.projectattitude.R;

import java.io.ByteArrayOutputStream;
import java.util.Date;

/**
 * This activity allows for the manipulation of mood objects. The view is similar to the
 * viewmood. Initially, any information from the object is loaded into the appropriate fields
 * to help the user remember what was already there. By updating the mood object, the object
 * in the list is then also updated.
 */

public class EditMoodActivity extends MoodActivity {
    Button completeButton;

    DatePickerEditText date;
    Spinner emotionSpinner;
    EditText etTrigger;
    Spinner socialSituationSpinner;
    CheckBox saveLocation;
    TextView currentLocation;
    private Mood newMood;

    private Double latitude;
    private Double longitude;

    private ImageView imageView;
    private byte[] byteArray;
    private String s;


    //https://www.mkyong.com/android/android-spinner-drop-down-list-example/
    //Resourse for the spinner I made
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_mood);
        completeButton = (Button) findViewById(R.id.saveButton);
        date = new DatePickerEditText(this, R.id.dateField);
        emotionSpinner = (Spinner) findViewById(R.id.emotionSpinner);
        etTrigger = (EditText) findViewById(R.id.triggerField);
        socialSituationSpinner = (Spinner) findViewById(R.id.spinner);
        saveLocation = (CheckBox) findViewById(R.id.saveLocation);

        currentLocation = (TextView) findViewById(R.id.currentLocation);

        Button addPhoto = (Button) findViewById(R.id.addPhoto);
        imageView = (ImageView) findViewById(R.id.imageView);
        imageView.setVisibility(View.GONE);
        s = "";

//        if(saveLocation.isChecked()){ //TODO check location
//            createLocation();
//        }
//        else{
//            //GeoPoint myLocation = null;   //TODO set location to null
//        }


        Mood mood = (Mood) getIntent().getSerializableExtra("mood");
        //Changes the fields to the selected mood
        etTrigger.setText(mood.getTrigger());
        Date tempDate = (Date) mood.getMoodDate();
        latitude = mood.getLatitude();
        longitude = mood.getLongitude();

        //not sure how to properly display location stuff using string resource
//        currentLocation.setText(getString(R.string.display_location, mood.getLatitude(), mood.getLongitude()));
        currentLocation.setText("Lat: " + mood.getLatitude() + " Long: " + mood.getLongitude());

        tempDate.setYear(tempDate.getYear() + 1900);
        date.setDate(tempDate);
        //disgusting single line way to set the spinners
        //Taken from http://stackoverflow.com/questions/2390102/how-to-set-selected-item-of-spinner-by-value-not-by-position
        emotionSpinner.setSelection(((ArrayAdapter<String>) emotionSpinner.getAdapter())
                .getPosition(mood.getEmotionState()));
        socialSituationSpinner.setSelection(((ArrayAdapter<String>) socialSituationSpinner
                .getAdapter()).getPosition(mood.getSocialSituation()));

        final byte[] imageBytes = Base64.decode(mood.getPhoto(), Base64.DEFAULT);
        final Bitmap decodedImage = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.length);
        imageView.setImageBitmap(decodedImage);

        completeButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //Spinner class will return a textview when you use getSelectedView(), allows for easy setError
                TextView errorText = (TextView) emotionSpinner.getSelectedView();

                if (errorCheck(errorText, etTrigger)) {
                    newMood = new Mood();
                    newMood.setEmotionState(emotionSpinner.getSelectedItem().toString());
                    newMood.setMoodDate(date.getDate());
                    newMood.setTrigger(etTrigger.getText().toString().trim());
                    newMood.setLatitude(latitude);
                    newMood.setLongitude(longitude);

                    if(socialSituationSpinner.getSelectedItem().toString().equals("Select a social situation")){
                        newMood.setSocialSituation("");
                    }
                    else{
                        newMood.setSocialSituation(socialSituationSpinner.getSelectedItem().toString());
                    }

                    if(decodedImage!=null && s == ""){
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        decodedImage.compress(Bitmap.CompressFormat.PNG, 100, stream);
                        byteArray = stream.toByteArray();
                        s = Base64.encodeToString(byteArray, Base64.DEFAULT);
                    }
                    newMood.setPhoto(s);
                    Intent returnCreateMoodIntent = new Intent();
                    returnCreateMoodIntent.putExtra("mood", newMood);
                    setResult(RESULT_OK, returnCreateMoodIntent);
                    finish();
                }
            }
        });

        saveLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                GPSTracker gps = new GPSTracker(EditMoodActivity.this);

                // check if GPS location can get Location
                if(saveLocation.isChecked()) {
                    //GPSTracker gps = new GPSTracker(CreateMoodActivity.this);
                    if (gps.canGetLocation()) {

                        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

                        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                            Log.d("UserLocation", "latitude:" + gps.getLatitude()
                                    + ", longitude: " + gps.getLongitude());

                            //sometimes only round to 3 decimals, I think it has to do with the
                            //how the round function calculates
                            latitude = Math.round(gps.getLatitude() * 10000d)/10000d;
                            longitude = Math.round(gps.getLongitude() * 10000d)/10000d;
                        }
                    }
                }
                else{
                    //NaN breaks the app when you undo location selection and complete mood creation
//                    latitude = NaN;
//                    longitude = NaN;
                    latitude = 0.0;
                    longitude = 0.0;

                }
            }
        });

        addPhoto.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, 3);
                imageView.setVisibility(View.VISIBLE);
            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == 3 && resultCode == RESULT_OK && null != data ){
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            Log.d("PhotoBytes1", photo.getByteCount()+"");
            Log.d("PhotoHeight1", photo.getHeight()+"");
            Log.d("PhotoHeight1", photo.getWidth()+"");

            if(photo.getByteCount() > 65536) {
                Bitmap photo1 = Bitmap.createScaledBitmap(photo, (photo.getWidth() / 3), (photo.getHeight() / 3), false);
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo1.compress(Bitmap.CompressFormat.PNG, 100, stream);
                Log.d("Compressed", photo1.getByteCount() + "");
                byteArray = stream.toByteArray();
                s = Base64.encodeToString(byteArray, Base64.DEFAULT);
                imageView.setImageBitmap(photo1);
            }

            else{
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                photo.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byteArray = stream.toByteArray();
                s = Base64.encodeToString(byteArray, Base64.DEFAULT);
                imageView.setImageBitmap(photo);
            }
        }

        else{
            s = "";
            Log.d("PhotoEmpty", s);
        }

    }

    /*error checks Emotional State spinner to make sure an emotional state was chosen
also error checks trigger input field for character length*/
    public boolean errorCheck(TextView emotionStateText, EditText etTriggerText) {

        String etTriggerString = etTriggerText.getText().toString().trim();

        //count whitespace of trigger string
        int spaces = etTriggerString.length() - etTriggerString.replace(" ", "").length();

        if(emotionStateText.getText().toString().equals("Select an emotional state")) {
            emotionStateText.setTextColor(Color.RED);
            emotionStateText.setText(R.string.emotion_state_error);

            return false;
        }

        //trigger is longer then 20 characters
        else if(etTriggerString.length() > 20){
            etTriggerText.setError("Reason must be no more than 20 characters");
            return false;
        }

        //trigger is more than 3 words
        else if(spaces > 2){
            etTriggerText.setError("Reason must be no more than 3 words");
            return false;
        }
        return true;
    }


    /**
     * @see CreateMoodActivity
     * @return A GeoPoint
     * This function creates a GeoPoint of the current user's last known location
     */
    private void createLocation(){
        //GeoPoint myLocation = LocationServices.FusedLocationApi.getLastLocation()
        //newMood.setGeoLocation(myLocation);
        return;
    }

    /**
     * @see CreateMoodActivity
     * @return an Image most likely
     */
    private void createPicture(){
        return;
    }
}
