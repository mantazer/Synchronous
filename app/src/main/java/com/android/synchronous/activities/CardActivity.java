package com.android.synchronous.activities;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.Data;

import com.android.synchronous.R;
import com.android.synchronous.task.JSONArrayRemoveTask;
import com.parse.GetCallback;
import com.parse.GetDataCallback;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import org.json.JSONArray;

import de.hdodenhof.circleimageview.CircleImageView;

public class CardActivity extends Activity {

    public final static String CARD_USERNAME =
            "com.android.synchronous.cardactivity.card_username";
    public final static String CARD_POSITION =
            "com.android.synchronous.cardactivity.card_position";

    private ParseUser mParseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_card);

        ParseQuery<ParseUser> query = ParseUser.getQuery();
        query.whereEqualTo("username", getIntent().getStringExtra(CARD_USERNAME));
        query.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser parseUser, ParseException e) {
                mParseUser = parseUser;
            }
        });

        ParseFile imageFile = (ParseFile) mParseUser.get("photo");
        imageFile.getDataInBackground(new GetDataCallback() {
            @Override
            public void done(byte[] bytes, ParseException e) {
                CircleImageView mImage = (CircleImageView) findViewById(R.id.cardImage);
                Bitmap imageBitmap = BitmapFactory.decodeByteArray(bytes, 0,
                        bytes.length);
                mImage.setImageBitmap(imageBitmap);
            }
        });

        TextView mName = (TextView) findViewById(R.id.cardName);
        mName.setText(mParseUser.get("name").toString());

        TextView mEmail = (TextView) findViewById(R.id.cardEmail);
        mEmail.setText(mParseUser.getEmail());

        TextView mNumber = (TextView) findViewById(R.id.cardNumber);
        mNumber.setText(mParseUser.get("phone").toString());

        TextView mCompany = (TextView) findViewById(R.id.cardCompany);
        mCompany.setText(mParseUser.get("company").toString());

        TextView mTitle = (TextView) findViewById(R.id.cardTitle);
        mTitle.setText(mParseUser.get("title").toString());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_delete:
                JSONArray savedList = JSONArrayRemoveTask.remove(getIntent().
                        getIntExtra(CARD_POSITION, 0), ParseUser.getCurrentUser().
                        getJSONArray("saved"));

                ParseUser.getCurrentUser().put("saved", savedList);
                ParseUser.getCurrentUser().saveInBackground();

                Intent intent = new Intent(this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                startActivity(intent);
                return true;

            case R.id.action_save:
                ContentValues values = new ContentValues();
                values.put(Data.RAW_CONTACT_ID, 001);
                values.put(Data.MIMETYPE, Phone.CONTENT_ITEM_TYPE);
                values.put(Phone.NUMBER, mParseUser.get("phone").toString());
                values.put(Phone.TYPE, Phone.TYPE_MAIN);
                values.put(Phone.LABEL, mParseUser.get("name").toString());
                getContentResolver().insert(android.provider.ContactsContract.Data.CONTENT_URI, values);
                return true;

            case R.id.action_call:
                String number = "tel:" + mParseUser.get("phone").toString().trim();
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                callIntent.setData(Uri.parse(number));
                startActivity(callIntent);
                return true;

        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.viewcard_menu, menu);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        return true;
    }
}
