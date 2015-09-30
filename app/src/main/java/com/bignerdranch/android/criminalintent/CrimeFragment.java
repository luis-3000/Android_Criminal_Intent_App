package com.bignerdranch.android.criminalintent;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.NavUtils;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.text.format.DateFormat;

import java.util.Date;
import java.util.UUID;

/**
 * Created by joseluiscastillo on 9/16/15.
 */
public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final int REQUEST_DATE = 0; //Constant for the request code
    private static final int REQUEST_CONTACT = 1;  //Request code from contact's list to list as a 'suspect'

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton; //Button to request a 'suspect' from the contact's list

    /* Constructor that accepts a UUID, creates an arguments bundle, creates a fragment instance
    *  and then attaches the arguments to the fragment. */
    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    /* Must be public because it will be called by any activity(ies)
    *  hosting the fragment. */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCrime = new Crime();

        //Retrieve the UUID from the fragment arguments
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);

        //After getting the ID, it is used to fetch the Crime from CrimeLab.
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }


    @Override
    /* Updates the CrimeLab's copy of 'Crime' instances. This effectively updates the crimes list. */
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    /* Overrinding this method to make it retrieve the 'extra', set the date on the Crime, and
    * refresh the text of the date button. */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if(requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            //Specify which fields you want your query to return values for
            String[] queryFields = new String[] {
                    ContactsContract.Contacts.DISPLAY_NAME
            };


            //Next, we create a query that asks for all the display names of the contacts in the returned data,
            ////then query the contact's database and get a Cursor object to work with.

            //Perform your query - the contactUri is like a "where clause here
            Cursor c = getActivity().getContentResolver().query(contactUri, queryFields, null, null, null);


            try {
                //Double check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }

                //Pull out the first column of the first row of data -  that is the suspect's name.
                c.moveToFirst(); //Because the curors only contains one item, we move it to the first item
                String suspect = c.getString(0);  //... and get it as a string, this string will be the name of the suspect.
                mCrime.setSuspect(suspect);       //set the suspect
                mSuspectButton.setText(suspect);  //set the corresponding button
            } finally {
                c.close();
            }
        }
    }

    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    /* Creates four strings and then pieces them together and return a complete report. */
    private String getCrimeReport() {
        String solvedString = null;

        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();

        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(), dateString, solvedString, suspect);

        return report;
    }

    @Override
    /* Inflates the view of fragment_crime.xml  */
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View v = inflater.inflate(R.layout.fragment_crime, container, false);
                                                          //View's parent /->false, whether to add the inflated view to the view's parent
                                                                        // The view will be added in the activity's code
        mTitleField = (EditText) v.findViewById(R.id.crime_title);

        //Now that CrimeFragment fetches a Crime, its view can display that Crime's data.
        //Updating this line to display the Crime's title
        mTitleField.setText(mCrime.getTitle());

        /* Anonymous class that implements the TextWatcher listener interface. */
        mTitleField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //space intentionally left blank
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString()); //Set crime's title to user's input
            }

            @Override
            public void afterTextChanged(Editable s) {
                //space intentionally left blank
            }
        });

        //Get a refence to the date button
        mDateButton = (Button) v.findViewById(R.id.crime_date);

        //Set it's text as the date of the crime
        updateDate();

        //Activate the DateButton
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();

                //DatePickerFragment dialog = new DatePickerFragment(); //replaced by below line

                //DatePickerFragment needs to initialize the DatePicker using the information held in the 'Date'
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());

                //Set CrimeFragment as the target fragment for DatePickerFragment
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);

                dialog.show(manager, DIALOG_DATE);
            }
        });

        //Get a reference for the CheckBox
        mSolvedCheckBox = (CheckBox)v.findViewById(R.id.crime_solved);

        //Display the solved status
        mSolvedCheckBox.setChecked(mCrime.isSolved());


        //Set a listener that will update the mSolved field of the crime
        mSolvedCheckBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                //Set the crime's solved property
                mCrime.setSolved(isChecked);
            }
        });

        //Send a report
        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {
               Intent i = new Intent(Intent.ACTION_SEND); //Intent constructor that accepts a string which is a constant defining the action
               i.setType("text/plain");
               i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
               i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
               //Make sure to always display a chooser to send the report (email/twitter,etc)
               i = Intent.createChooser(i, getString(R.string.send_report));
               startActivity(i);
           }
        });

        //Get a reference to the 'request suspect name button'
        final Intent pickContact = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button)v.findViewById(R.id.crime_suspect);

        //... and set a listener to it
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        //Guard against no contact app to pick 'suspects'
        //PackageManager know about all the componetns installed on the Android device, including all of its activities.
        PackageManager packageManager = getActivity().getPackageManager();

        //By calling resolveActivity(Intent, int) we are asking it to find an activity that matches the Intent we gave it.
        //The MATCH_DEFAULT_ONLY flag restrict this search to activitties with the DATEGORY_DEFAULT flag, just like startActivity(Intent) does.
        //If the search is successful, it will return an instance of ResolveInfo telling us all about which activity it found. In contracts, if
        //it returns null, the game is up, no contacts app - so we disable the usedless suspect button.
        if (packageManager.resolveActivity(pickContact, PackageManager.MATCH_DEFAULT_ONLY) == null)
        {
            mSuspectButton.setEnabled(false);
        }

        return v;
    }



}
