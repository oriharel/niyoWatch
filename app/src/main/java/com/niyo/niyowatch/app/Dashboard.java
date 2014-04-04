package com.niyo.niyowatch.app;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.ActionBar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.DrawerLayout;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.niyo.niyowatch.app.data.AccountsTableColumns;
import com.niyo.niyowatch.app.data.DataConstans;
import com.niyo.niyowatch.app.data.JsonFetchIntentService;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class Dashboard extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */

    public static final String LOG_TAG = Dashboard.class.getSimpleName();
    LoaderManager.LoaderCallbacks<Cursor> mLoader;
    int mLoaderId;
    private ContentObserver mObserver;
    protected final Handler pHandler = new Handler();
    protected Handler getHandler() {
        return pHandler;
    }

    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate started");
        setContentView(R.layout.activity_dashboard);

        mTitle = getTitle();

        resetActivity();

        initLoader();

        getLoaderManager().initLoader(mLoaderId, null, mLoader);

        fetchData();

        ServiceCaller callback = new ServiceCaller() {
            @Override
            public void success(Object data) {
                findViewById(R.id.fetchingLayoutUpper).setVisibility(View.VISIBLE);
            }

            @Override
            public void failure(Object data, String description) {
                findViewById(R.id.fetchingLayoutUpper).setVisibility(View.GONE);
            }
        };

        ((WatchApplication)getApplication()).registerProgressCallback(callback);
    }

    private void fetchData() {

        findViewById(R.id.bezeqBox).setVisibility(View.GONE);
        findViewById(R.id.elecBox).setVisibility(View.GONE);
        findViewById(R.id.noAccounts).setVisibility(View.GONE);
        Intent serviceIntent = new Intent(this, JsonFetchIntentService.class);
        ArrayList<String> urls = new ArrayList<String>();
        urls.add("http://calm-fortress-7680.herokuapp.com/getAccounts");
//        urls.add("http://calm-fortress-7680.herokuapp.com/bezeq");
        serviceIntent.putStringArrayListExtra(JsonFetchIntentService.URLS_EXTRA, urls);
        startService(serviceIntent);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void resetActivity() {
        findViewById(R.id.noAccounts).setVisibility(View.GONE);
        findViewById(R.id.bezeqBox).setVisibility(View.GONE);
        findViewById(R.id.elecBox).setVisibility(View.GONE);
    }

    private void initLoader() {

        final Dashboard context = this;

        mLoader = new LoaderManager.LoaderCallbacks<Cursor>() {

            @Override
            public Loader<Cursor> onCreateLoader(int id, Bundle args) {

                Uri baseUri = DataConstans.ACCOUNTS_URI;

                // Now create and return a CursorLoader that will take care of
                // creating a Cursor for the data being displayed.
                String select = "((" + AccountsTableColumns.NAME + " NOTNULL) AND ("
                        + AccountsTableColumns.NAME + " != '' ))";
                Log.d(LOG_TAG, "creating cursor loader");
                Loader<Cursor> result = new CursorLoader(context, baseUri,
                        DataConstans.ACCOUNTS_SUMMARY_PROJECTION, select, null,
                        AccountsTableColumns.NAME + " COLLATE LOCALIZED ASC");

                mLoaderId = result.getId();
                return result;
            }

            @Override
            public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {

                Log.d(LOG_TAG, "data getting finished from local db");
                if (cursor.getCount() <= 0) {
                    showNoAccounts();
                }

                else {
                    cursor.moveToFirst();

                    while(!cursor.isAfterLast()) {

                        String name = cursor.getString(cursor.getColumnIndex(AccountsTableColumns.NAME));
                        String amount = cursor.getString(cursor.getColumnIndex(AccountsTableColumns.AMOUNT));
                        Long updateTimeInMillis = cursor.getLong(cursor.getColumnIndex(AccountsTableColumns.UPDATE_TIME));
                        Calendar cal = Calendar.getInstance();
                        cal.setTimeInMillis(updateTimeInMillis);
                        if (name.toLowerCase().contains("bezeq")) {
                            renderBox(R.id.bezeqBox, R.id.bezeqAmount, R.id.bezeqUpdate, amount.toString(), cal);
                        }
                        else if (name.toLowerCase().indexOf("elec") > -1){
                            renderBox(R.id.elecBox, R.id.elecAmount, R.id.elecUpdate, amount.toString(), cal);
                        }
                        cursor.moveToNext();
                    }
                }
            }

            @Override
            public void onLoaderReset(Loader<Cursor> arg0) {
                // TODO Auto-generated method stub

            }
        };

    }

    private void showNoAccounts() {

        findViewById(R.id.noAccounts).setVisibility(View.VISIBLE);
        findViewById(R.id.elecBox).setVisibility(View.GONE);;
        findViewById(R.id.bezeqBox).setVisibility(View.GONE);

    }

    private void renderBox(int boxId, int amountViewId, int updateViewId, String currentAmount, Calendar updateTimeCal) {
        findViewById(R.id.noAccounts).setVisibility(View.GONE);
        findViewById(boxId).setVisibility(View.VISIBLE);
        TextView amountText = (TextView)findViewById(amountViewId);
        amountText.setText("₪"+currentAmount);
        TextView updateTime = (TextView)findViewById(updateViewId);
        DateFormat df = new SimpleDateFormat("dd/MM/yy");
        String formattedDate = df.format(updateTimeCal.getTime());
        updateTime.setText(formattedDate);
        findViewById(R.id.elecSivu).setVisibility(View.GONE);
        findViewById(R.id.elecAmount).setVisibility(View.VISIBLE);
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        // update the main content by replacing fragments
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.container, PlaceholderFragment.newInstance(position + 1))
                .commit();
    }

    public void onSectionAttached(int number) {
        switch (number) {
            case 1:
                mTitle = getString(R.string.title_section1);
                break;
            case 2:
                mTitle = getString(R.string.title_section2);
                break;
            case 3:
                mTitle = getString(R.string.title_section3);
                break;
        }
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
//        if (!mNavigationDrawerFragment.isDrawerOpen()) {
//            // Only show items in the action bar relevant to this screen
//            // if the drawer is not showing. Otherwise, let the drawer
//            // decide what to show in the action bar.
//            getMenuInflater().inflate(R.menu.dashboard, menu);
//            restoreActionBar();
//            return true;
//        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(Integer.toString(getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            ((Dashboard) activity).onSectionAttached(
                    getArguments().getInt(ARG_SECTION_NUMBER));
        }
    }

}
