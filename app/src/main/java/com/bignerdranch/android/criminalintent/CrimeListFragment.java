package com.bignerdranch.android.criminalintent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

/**
 * Created by joseluiscastillo on 9/19/15.
 */
public class CrimeListFragment extends Fragment {

    private RecyclerView mCrimeRecyclerView;

    private CrimeAdapter mAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Hook up the view to the fragment
        View view = inflater.inflate(R.layout.fragment_crime_list, container, false);

        mCrimeRecyclerView = (RecyclerView) view.findViewById(R.id.crime_recycler_view);

        mCrimeRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        updateUI();

        return view;
    }

    /* Overriding method.
    * Triggers a call to the updateUI() to reload the list.
    * This method is being overriden to update the RecyclerView becuase we cannot assume that the
    * activity will be stopped when another activity is in front of it. If the other activity is
    * transparent, the current activity may just be paused. This is the safest place to take action
    * to update a fragment's view. */
    @Override
    public void onResume() {
        super.onResume();
        updateUI();
    }

    /* Havind the CrimeAdapter, now it's time to connect it to the RecyclerView.
    * This method sets up CrimeListFragment's user interface. Initial functionality will be to
    * create a CrimeAdapter and set it on the RecyclerView. Other functionality to be added later. */
    private void updateUI() {
        CrimeLab crimeLab = CrimeLab.get(getActivity());
        List<Crime> crimes = crimeLab.getCrimes();

        if (mAdapter == null) { //If CrimeAdapter not set up yet, set it up
            mAdapter = new CrimeAdapter(crimes);
            mCrimeRecyclerView.setAdapter(mAdapter);
        } else { //Otherwise, send notification that it's already been set up
            mAdapter.notifyDataSetChanged();
        }


        mCrimeRecyclerView.setAdapter(mAdapter);
    }

    /* Defining the ViewHolder asn an inner class. */   //Modified to handle presses for an entire row
    private class CrimeHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        private Crime mCrime;
        private TextView mTitleTextView;
        private TextView mDateTextView;
        private CheckBox mSolvedCheckBox;

        //Constructor
        public CrimeHolder( View itemView) {
            super(itemView);
            itemView.setOnClickListener(this); //Respond to a click on a list item

            //Give more responsibilities to this private class
            mTitleTextView = (TextView) itemView.findViewById(R.id.list_item_crime_title_text_view);
            mDateTextView = (TextView) itemView.findViewById(R.id.list_item_crime_date_text_view);
            mSolvedCheckBox = (CheckBox) itemView.findViewById(R.id.list_item_crime_solved_check_box);
        }

        /* When given a Crime, CrimeHolder will now update the title TextView, date TextView, and solved
        * CheckBox to refelct the state of the Crime. */
        public void bindCrime(Crime crime) {
            mCrime = crime;
            mTitleTextView.setText(mCrime.getTitle());
            mDateTextView.setText(mCrime.getDate().toString());
            mSolvedCheckBox.setChecked(mCrime.isSolved());
        }

        @Override
        public void onClick(View v) {
            //Start a CrimeActivity from a Fragment
            //Create a specific intent that names the CrimeActivity class.
            //getActivity() is used to pass its hosting activity as the Context object that the Intent constructor requires.
            Intent intent = CrimeActivity.newIntent(getActivity(), mCrime.getId()); //pass in the crime ID
            startActivity(intent);
        }
    }

    /* Another private inner class to define the CrimeAdapter.
    * This adapter will know all of Crime's details. */
    private class CrimeAdapter extends RecyclerView.Adapter<CrimeHolder> {

        private List<Crime> mCrimes;

        public CrimeAdapter(List<Crime> crimes) {
            mCrimes = crimes;
        }

        @Override
        /* Called by the RecyclerView when it needs a new Viwe to display an item.
        * In it, a View* is created and then it is wrapped in a ViewHolder. */
        public CrimeHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            /* For a 'View' a layout is inflated from the Android standard library called
            * simple_list_item_1 which contains a single TextView, styled to look nice in a list.
            * After creating iist_item_crime.xml, such name was substituted to provide TextView for
            * each "crime". */
            View view = layoutInflater.inflate(R.layout.list_item_crime, parent, false);
            return new CrimeHolder(view);
        }

        @Override
        /* Binds a ViewHolder's View to the model object. It reeived the ViewHolder and a position
        * in the data set. To bind the 'View' the position is used to find the right model data.
        * Then, the View is updated to reflect that model data. For this implementation, that position
        * is the index of the Crime in the array of crimes. Once it is pulled out, that indicated
        * 'Crime' object is bound to the 'View' by sending its title to the ViewHolder's TextView.*/
        public void onBindViewHolder(CrimeHolder holder, int position) {

            Crime crime = mCrimes.get(position);

            //Now connecting the CrimeAdapter to the bindCrime()
            holder.bindCrime(crime);
        }

        @Override
        public int getItemCount() {
            return mCrimes.size();
        }
    }

}
