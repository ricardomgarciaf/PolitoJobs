package com.example.ricardogarcia.politojobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ricardogarcia on 26/04/15.
 */
public class JobAdapter extends BaseAdapter implements View.OnClickListener{

    public static final String JOB = "com.example.ricardogarcia.politojobs.JOB";

    private LayoutInflater inflater;
    private Activity activity;
    private List<Job> listjobs;


    public JobAdapter(Activity activity,ArrayList list){
        inflater= (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.activity=activity;
        this.listjobs =list;

    }


    @Override
    public int getCount() {
        return listjobs.size();
    }

    @Override
    public Object getItem(int position) {
        return listjobs.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder vholder;
        View v=convertView;

        if(listjobs.size()>0){
            if(v==null){
                v=inflater.inflate(R.layout.job_row, parent, false);
                vholder= new ViewHolder();
                vholder.textJob= (TextView) v.findViewById(R.id.textJobPosition);
                vholder.textCompany= (TextView) v.findViewById(R.id.textCompany);
                vholder.textLocation= (TextView) v.findViewById(R.id.textLocation);
                vholder.textDate= (TextView) v.findViewById(R.id.textDatePosted);
                vholder.buttonView=(Button) v.findViewById(R.id.buttonView);
                vholder.buttonSave=(Button) v.findViewById(R.id.buttonSave);
                v.setTag(vholder);
            }
            else{
                vholder= (ViewHolder) v.getTag();
            }

            vholder.textJob.setText(listjobs.get(position).getPosition().substring(0,1).toUpperCase()+listjobs.get(position).getPosition().substring(1));
            vholder.textCompany.setText(listjobs.get(position).getCompany().getName().substring(0,1).toUpperCase()+listjobs.get(position).getCompany().getName().substring(1));
            vholder.textLocation.setText(listjobs.get(position).getLocation().substring(0,1).toUpperCase()+listjobs.get(position).getLocation().substring(1));
            vholder.textDate.setText(listjobs.get(position).getDate().substring(4,19));
            vholder.buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent= new Intent(activity,ViewJob.class);
                    intent.putExtra(JOB,listjobs.get(position));
                    activity.startActivity(intent);
                }
            });

            vholder.buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    try {
                        /*
                        ParseQuery<ParseUser> query = ParseUser.getQuery();
                        query.whereEqualTo("objectId", "2AM7fmxH5S");
                        ParseUser user = query.getFirst();*/

                        ParseQuery<ParseObject> queryStudent = ParseQuery.getQuery("Student");
                        queryStudent.whereEqualTo("StudentId", ParseUser.getCurrentUser());
                        ParseQuery<ParseObject> queryJob = ParseQuery.getQuery("JobOffer");
                        queryJob.whereEqualTo("objectId", listjobs.get(position).getId());

                        ParseObject student = queryStudent.getFirst();
                        ParseObject job = queryJob.getFirst();

                        ParseQuery<ParseObject> querySavedJob = ParseQuery.getQuery("SavedJobOffer");
                        querySavedJob.whereEqualTo("StudentId", student);
                        querySavedJob.whereEqualTo("OfferId", job);
                        String message=null;
                        if(querySavedJob.count()==0) {
                            ParseObject saveJob = new ParseObject("SavedJobOffer");
                            saveJob.put("StudentId", student);
                            saveJob.put("OfferId", job);
                            saveJob.saveInBackground();
                            message=activity.getString(R.string.addedSavedJobMessage);
                        }
                        else{
                            message=activity.getString(R.string.existingSavedJobMessage);

                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("Save jobs");
                        builder.setMessage(message);
                        builder.setCancelable(true);
                        builder.setNeutralButton(android.R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                });

                        AlertDialog alert = builder.create();
                        alert.show();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        return v;
    }

    @Override
    public void onClick(View v) {

    }


    public static class ViewHolder {
        public TextView textJob;
        public TextView textCompany;
        public TextView textLocation;
        public TextView textDate;
        public Button buttonView;
        public Button buttonSave;
    }
}
