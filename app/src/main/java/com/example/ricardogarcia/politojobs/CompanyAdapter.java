package com.example.ricardogarcia.politojobs;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ricardogarcia on 26/04/15.
 */
public class CompanyAdapter extends BaseAdapter implements View.OnClickListener {

    public static final String COMPANY = "com.example.ricardogarcia.politojobs.COMPANY";


    private LayoutInflater inflater;
    private Activity activity;
    private List<Company> listcompanies;


    public CompanyAdapter(Activity activity, ArrayList list) {
        inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.activity = activity;
        this.listcompanies = list;

    }


    @Override
    public int getCount() {
        return listcompanies.size();
    }

    @Override
    public Object getItem(int position) {
        return listcompanies.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {

        ViewHolder vholder;
        View v = convertView;

        if (listcompanies.size() > 0) {
            if (v == null) {
                v = inflater.inflate(R.layout.company_row, parent, false);
                vholder = new ViewHolder();
                vholder.textName = (TextView) v.findViewById(R.id.textName);
                vholder.textIndustry = (TextView) v.findViewById(R.id.textIndustry);
                vholder.buttonView = (Button) v.findViewById(R.id.buttonView);
                vholder.buttonSave = (Button) v.findViewById(R.id.buttonSave);
                v.setTag(vholder);
            } else {
                vholder = (ViewHolder) v.getTag();
            }
            vholder.textName.setText(listcompanies.get(position).getName().substring(0, 1).toUpperCase() + listcompanies.get(position).getName().substring(1));
            vholder.textIndustry.setText(listcompanies.get(position).getIndustry().substring(0, 1).toUpperCase() + listcompanies.get(position).getIndustry().substring(1));
            vholder.buttonView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, ViewCompany.class);
                    intent.putExtra(COMPANY, listcompanies.get(position));
                    activity.startActivity(intent);
                }
            });

            vholder.buttonSave.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    try {
                        ParseQuery<ParseObject> queryStudent = ParseQuery.getQuery("Student");
                        queryStudent.whereEqualTo("StudentId", ParseUser.getCurrentUser());
                        ParseQuery<ParseObject> queryCompany = ParseQuery.getQuery("Company");
                        queryCompany.whereEqualTo("objectId", listcompanies.get(position).getId());


                        ParseObject student = queryStudent.getFirst();
                        ParseObject company = queryCompany.getFirst();

                        ParseQuery<ParseObject> querySavedCompany = ParseQuery.getQuery("SavedCompany");
                        querySavedCompany.whereEqualTo("StudentId", student);
                        querySavedCompany.whereEqualTo("CompanyId", company);
                        String message = null;

                        if (querySavedCompany.count() == 0) {
                            ParseObject savedCompany = new ParseObject("SavedCompany");
                            savedCompany.put("StudentId", student);
                            savedCompany.put("CompanyId", company);
                            savedCompany.saveInBackground();
                            message = activity.getString(R.string.addedSavedCompanyMessage);

                        } else {
                            message = activity.getString(R.string.existingSavedCompanyMessage);

                        }

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setTitle("Save companies");
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
        public TextView textName;
        public TextView textIndustry;
        public Button buttonView;
        public Button buttonSave;
    }
}
