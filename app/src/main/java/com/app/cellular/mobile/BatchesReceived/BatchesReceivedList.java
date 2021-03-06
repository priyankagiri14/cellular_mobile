package com.app.cellular.mobile.BatchesReceived;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.app.cellular.mobile.Agent.NetworkError;
import com.app.cellular.mobile.AgentAllocationTab;
import com.app.cellular.mobile.AgentsList.AgentsList;
import com.app.cellular.mobile.NetworkStateReceiver;
import com.app.cellular.mobile.R;
import com.app.cellular.mobile.Web_Services.RetrofitToken;
import com.app.cellular.mobile.Web_Services.Web_Interface;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BatchesReceivedList extends AppCompatActivity implements View.OnClickListener, SearchView.OnQueryTextListener , NetworkStateReceiver.NetworkStateReceiverListener {

    private BatchesReceivedListAdapter adapter;
    List<BatchesReceivedResponse> list1 = new ArrayList<>();
    ArrayList<Body> bodyArrayList = new ArrayList<>();
    List<Body> bodyArrayList1 = new ArrayList<>();
    ArrayList<String> bodyArrayListbatches = new ArrayList<String>();
    String bodybatchesstring[];
    public ListView listView;
    TextView batchesreceivedtext, nobatchesreceived;
    Button btnstts;
    ProgressDialog progressBar;
    int count = 0;
    SearchView searchView;
    private NetworkStateReceiver networkStateReceiver;

    /**
     * This function populates the list got from the ERP (Array List) with confirmation of acceptance.
     * @param batchesReceivedResponseList This  is the list of the Body class of the Agent Batches Get Class.
     */

    private void populateListView(List<Body> batchesReceivedResponseList) {
        Log.d("PNK", "POPULATELIST");
        Log.d("PNK", list1.toString());
        bodyArrayList1 = batchesReceivedResponseList;
        adapter = new BatchesReceivedListAdapter(this, bodyArrayList1);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);
        searchView.setVisibility(View.VISIBLE);
        progressBar.cancel();
    }

    /**
     * Thus method is initializing all the design components which will be used further for some functionalty.
     * @param savedInstanceState
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batches_received_list);
        networkStateReceiver = new NetworkStateReceiver();
        networkStateReceiver.addListener(this);
        this.registerReceiver(networkStateReceiver, new IntentFilter(android.net.ConnectivityManager.CONNECTIVITY_ACTION));
        searchView = (SearchView) findViewById(R.id.driverbatchsearch);
        searchView.onActionViewExpanded();
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Batches");
        searchView.requestFocusFromTouch();
        searchView.setVisibility(View.INVISIBLE);


        progressBar = new ProgressDialog(this);
        progressBar.setCancelable(false);
        progressBar.setMessage("Please Wait...");
        progressBar.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progressBar.show();

        listView = (ListView) findViewById(R.id.batches_received_listview);
        listView.setTextFilterEnabled(true);
        batchesreceivedtext = (TextView) findViewById(R.id.driver_batches_received);
        nobatchesreceived = (TextView) findViewById(R.id.nobatchesrc);
        listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
        btnstts = (Button) findViewById(R.id.btnreceive);
        btnstts.setVisibility(View.INVISIBLE);
        batchesreceivedtext.setVisibility(View.INVISIBLE);
        batchesGet();
        btnstts.setOnClickListener(this);
    }

    /**
     *This function return the API Results according to its response code, it will be failure or success. & populates the list.
     */

    private void batchesGet() {

        Log.d("PNK", "BATCHES Received");

        Web_Interface web_interface1 = RetrofitToken.getClient().create(Web_Interface.class);
        Call<BatchesReceivedResponse> batchesGetResponseCall = web_interface1.requestBatchesReceived(0, 0);
        batchesGetResponseCall.enqueue(new Callback<BatchesReceivedResponse>() {
            @Override
            public void onResponse(Call<BatchesReceivedResponse> call, Response<BatchesReceivedResponse> response) {
                List<Body> list = new ArrayList<>();
                if (response.body() != null && response.body().getBody() != null) {
                    list = response.body().getBody();

                    for (int i = 0; i < list.size(); i++) {
                        String status = response.body().getBody().get(i).getStatus();
                        String valueSim = String.valueOf(response.body().getBody().get(i).isValueSim());
                        if (status.equals("RECEIVED") && valueSim.equals("true")) {
                            bodyArrayList1.add(list.get(i));
                            populateListView(bodyArrayList1);
                            btnstts.setVisibility(View.VISIBLE);
                            batchesreceivedtext.setVisibility(View.VISIBLE);
                        }
                    }
                }

                String[] batches = new String[bodyArrayList1.size()];
                if (bodyArrayList1.size() > 0) {
                    for (int j = 0; j < bodyArrayList1.size(); j++) {
                        batches[j] = bodyArrayList1.get(j).getBatchNo();
                    }
                }

                if (listView.getCount() == 0) {
                    nobatchesreceived.setVisibility(View.VISIBLE);
                    searchView.setVisibility(View.INVISIBLE);
                    progressBar.cancel();
                    // Toast.makeText(BatchesReceivedList.this, "No Data is Received by You..!", Toast.LENGTH_SHORT).show();
                }
//                for (int i =0; i <list.size(); i++) {
//                    list1.add(response.body());
//                }

                Log.d("PNK", "LIST1");
                Log.d("PNK", list1.toString());

//                populateListView(list1.get(0).getBody());

                Log.d("Batches", "onResponse: " + list1);
            }

            @Override
            public void onFailure(Call<BatchesReceivedResponse> call, Throwable t) {
                Toast.makeText(BatchesReceivedList.this, t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });


    }

    /**
     * This functions click got the values of serial numbers after receiving it from user side with an alert dialog for confirmation & used for
     * work on it i.e. stock Allocation to Agent.
     * @param v
     */

    @Override
    public void onClick(View v) {
        Log.d("PNK", "ONCLICK");
        Log.d("PNK", "" + v.getId());

        AlertDialog alertDialog = new AlertDialog.Builder(BatchesReceivedList.this).create();
        alertDialog.setMessage("Do you want to allocate the stock to Agent");

        alertDialog.setButton(Dialog.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                progressBar.show();

                int size = listView.getCount();
                bodybatchesstring = new String[size];

                for (int j = 0; j < bodyArrayList1.size(); j++) {

                    if (bodyArrayList1.get(j).isIschecked()) {
                        bodyArrayListbatches.add(bodyArrayList1.get(j).getBatchNo());
                        count++;
                        //batches[j] = bodyArrayList1.get(j).getBatchNo();
                    }

                }
                if (count == 0) {
                    progressBar.cancel();
                    Toast.makeText(BatchesReceivedList.this, "Select any Batch for Allocation", Toast.LENGTH_SHORT).show();
                } else {
                    String[] batches = new String[bodyArrayListbatches.size()];

                    for (int j = 0; j < bodyArrayListbatches.size(); j++) {
                        batches[j] = bodyArrayListbatches.get(j);
                    }

                    progressBar.cancel();
                    Intent intent = new Intent(BatchesReceivedList.this, AgentsList.class);
                    intent.putExtra("allocation_stock", batches);
                    startActivity(intent);
                }
            }
        });
        alertDialog.setButton(Dialog.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                alertDialog.cancel();
                progressBar.cancel();
            }
        });

        alertDialog.show();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    /**
     *
     * @param newText It gets the text parameter to search it in the array list.
     * @return Searched value returned
     */

    @Override
    public boolean onQueryTextChange(String newText) {

        if (TextUtils.isEmpty(newText)) {
            listView.clearTextFilter();
        } else {
            listView.setFilterText(newText);
        }
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        networkStateReceiver.removeListener(this);
        this.unregisterReceiver(networkStateReceiver);
    }

    @Override
    public void networkAvailable() {

    }

    @Override
    public void networkUnavailable() {
        Intent i=new Intent(this, NetworkError.class);
        startActivity(i);
    }
}
