package com.example.ricardogarcia.politojobs;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


public class JobSearchResults extends ActionBarActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_job_search_results);


        Parse.initialize(JobSearchResults.this, "H9NFC1K9LmahxGcCrMOdT0qMaE0lDGT6BgbrSOAc", "4K2VfxRGIyk69KlQJ2B8NMnD71llrlkEPLdTNh9M");

        Intent intent = getIntent();
        Bundle b = intent.getExtras();

        new RetrieveFromDatabase().execute((HashMap<String, String>) b.getSerializable(JobSearch.INFO_HASH));


    }

    public void goHome(View view) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        String typeUser = currentUser.getString("TypeUser");
        if (typeUser.equals("Student")) {
            Intent intent = new Intent(this, StudentHome.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, CompanyHome.class);
            startActivity(intent);
        }
    }

    public void goProfile(View view) {
        ParseUser currentUser = ParseUser.getCurrentUser();
        String typeUser = currentUser.getString("TypeUser");
        if (typeUser.equals("Student")) {
            Intent intent = new Intent(this, ProfileStudent.class);
            startActivity(intent);
        } else {
            Intent intent = new Intent(this, ProfileCompany.class);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_job_search_results, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private class RetrieveFromDatabase extends AsyncTask<HashMap<String, String>, Void, ArrayList<Job>> {

        private ProgressDialog progressDialog = new ProgressDialog(JobSearchResults.this);


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog.setTitle("Loading jobs");
            if (!progressDialog.isShowing()) {
                progressDialog.show();
            }
        }

        @Override
        protected ArrayList<Job> doInBackground(HashMap<String, String>... params) {

            ArrayList<Job> jobs = new ArrayList<Job>();
            HashMap<String, String> search_data = params[0];

            if (search_data.get(JobSearch.INFO_SEARCHTYPE).equals("Search")) {
                //Search
                ParseQuery<ParseObject> searchJobQuery = ParseQuery.getQuery("JobOffer");

                //Location Filter
                if (search_data.containsKey(JobSearch.INFO_LOCATION)) {
                    searchJobQuery.whereEqualTo("Location", search_data.get(JobSearch.INFO_LOCATION));
                }

                //Industry Filter
                if (search_data.containsKey(JobSearch.INFO_INDUSTRY)) {
                    searchJobQuery.whereEqualTo("Industry", search_data.get(JobSearch.INFO_INDUSTRY));
                }

                //Type of Job Filter
                if (search_data.containsKey(JobSearch.INFO_JOBTYPE)) {
                    searchJobQuery.whereEqualTo("JobType", search_data.get(JobSearch.INFO_JOBTYPE));
                }

                //Type of contract filter
                if (search_data.containsKey(JobSearch.INFO_CONTRACT_TYPE)) {
                    searchJobQuery.whereEqualTo("ContractType", search_data.get(JobSearch.INFO_CONTRACT_TYPE));
                }

                //Duration filter
                if (search_data.containsKey(JobSearch.INFO_DURATION)) {
                    String[] arrayDuration = getResources().getStringArray(R.array.arrayDuration);
                    if (search_data.get(JobSearch.INFO_DURATION).equals(arrayDuration[0])) {
                        searchJobQuery.whereLessThan("Duration", 6);
                    } else if (search_data.get(JobSearch.INFO_DURATION).equals(arrayDuration[1])) {
                        searchJobQuery.whereGreaterThanOrEqualTo("Duration", 6);
                        searchJobQuery.whereLessThanOrEqualTo("Duration", 12);
                    } else if (search_data.get(JobSearch.INFO_DURATION).equals(arrayDuration[2])) {
                        searchJobQuery.whereGreaterThan("Duration", 12);
                    } else if (search_data.get(JobSearch.INFO_DURATION).equals(arrayDuration[3])) {
                        searchJobQuery.whereEqualTo("Duration", 0);
                    }
                }

                //Salary filter
                if (search_data.containsKey(JobSearch.INFO_SALARY)) {
                    String[] arraySalary = getResources().getStringArray(R.array.arraySalary);
                    if (search_data.get(JobSearch.INFO_SALARY).equals(arraySalary[0])) {
                        searchJobQuery.whereGreaterThanOrEqualTo("Salary", 1000);
                    } else if (search_data.get(JobSearch.INFO_SALARY).equals(arraySalary[1])) {
                        searchJobQuery.whereGreaterThanOrEqualTo("Salary", 5000);
                    } else if (search_data.get(JobSearch.INFO_SALARY).equals(arraySalary[2])) {
                        searchJobQuery.whereGreaterThanOrEqualTo("Salary", 10000);
                    } else if (search_data.get(JobSearch.INFO_SALARY).equals(arraySalary[3])) {
                        searchJobQuery.whereGreaterThanOrEqualTo("Salary", 20000);
                    }
                }

                //Keywords Filter
                if (search_data.containsKey(JobSearch.INFO_KEYWORDS)) {
                    searchJobQuery.whereContains("Position", search_data.get(JobSearch.INFO_KEYWORDS));
                }
                //Company Filter
                if (search_data.containsKey(JobSearch.INFO_COMPANY)) {
                    ParseQuery<ParseObject> companyQuery = ParseQuery.getQuery("Company");
                    companyQuery.whereContains("Name", search_data.get(JobSearch.INFO_COMPANY));
                    try {
                        List<ParseObject> results = companyQuery.find();
                        for (ParseObject parseCompany : results) {
                            searchJobQuery.whereEqualTo("CompanyId", parseCompany.get("CompanyId"));
                            List<ParseObject> matching_jobs = searchJobQuery.find();

                            Company company = new Company();

                            if (matching_jobs.size() > 0) {
                                if (parseCompany.get("CompanyID") != null)
                                    company.setId(parseCompany.get("CompanyId").toString());
                                if (parseCompany.get("Name") != null)
                                    company.setName(parseCompany.get("Name").toString());
                                if (parseCompany.get("Location") != null)
                                    company.setLocation(parseCompany.get("Location").toString());
                                if (parseCompany.get("Address") != null)
                                    company.setAddress(parseCompany.get("Address").toString());
                                if (parseCompany.get("Industry") != null)
                                    company.setIndustry(parseCompany.get("Industry").toString());
                                if (parseCompany.get("Description") != null)
                                    company.setDescription(parseCompany.get("Description").toString());
                                if (parseCompany.get("Size") != null)
                                    company.setCompany_size(parseCompany.get("Size").toString());
                                if (parseCompany.get("Website") != null)
                                    company.setWebsite(parseCompany.get("Website").toString());
                                if (parseCompany.get("Clients") != null)
                                    company.setClients(parseCompany.get("Clients").toString());
                            }

                            for (ParseObject parseJob : matching_jobs) {
                                Job job = new Job();
                                job.setId(parseJob.getObjectId());
                                if (parseJob.get("Position") != null)
                                    job.setPosition(parseJob.get("Position").toString());
                                if (parseJob.get("Industry") != null)
                                    job.setIndustry(parseJob.get("Industry").toString());
                                if (parseJob.get("Description") != null)
                                    job.setDescription(parseJob.get("Description").toString());
                                if (parseJob.get("Location") != null)
                                    job.setLocation(parseJob.get("Location").toString());
                                if (parseJob.get("Salary") != null)
                                    job.setSalary(parseJob.get("Salary").toString());
                                if (parseJob.get("JobType") != null)
                                    job.setTypeJob(parseJob.get("JobType").toString());
                                if (parseJob.get("Duration") != null)
                                    job.setDuration(parseJob.get("Duration").toString());
                                if (parseJob.get("ContractType") != null)
                                    job.setContractType(parseJob.get("ContractType").toString());
                                if (parseJob.getCreatedAt() != null)
                                    job.setDate(parseJob.getCreatedAt().toString());
                                job.setCompany(company);
                                jobs.add(job);
                            }
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                } else {

                    try {
                        List<ParseObject> matching_jobs = searchJobQuery.find();

                        for (ParseObject parseJob : matching_jobs) {
                            Job job = new Job();
                            job.setId(parseJob.getObjectId());
                            if (parseJob.get("Position") != null)
                                job.setPosition(parseJob.get("Position").toString());
                            if (parseJob.get("Industry") != null)
                                job.setIndustry(parseJob.get("Industry").toString());
                            if (parseJob.get("Description") != null)
                                job.setDescription(parseJob.get("Description").toString());
                            if (parseJob.get("Location") != null)
                                job.setLocation(parseJob.get("Location").toString());
                            if (parseJob.get("Salary") != null)
                                job.setSalary(parseJob.get("Salary").toString());
                            if (parseJob.get("JobType") != null)
                                job.setTypeJob(parseJob.get("JobType").toString());
                            if (parseJob.get("Duration") != null)
                                job.setDuration(parseJob.get("Duration").toString());
                            if (parseJob.get("ContractType") != null)
                                job.setContractType(parseJob.get("ContractType").toString());
                            if (parseJob.getCreatedAt() != null) {
                                job.setDate(parseJob.getCreatedAt().toString());
                            }
                            ParseQuery<ParseObject> companyQuery = ParseQuery.getQuery("Company");
                            companyQuery.whereEqualTo("CompanyId", parseJob.get("CompanyId"));
                            ParseObject company_result = companyQuery.getFirst();
                            Company company = new Company();
                            if (company_result.get("CompanyID") != null)
                                company.setId(company_result.get("CompanyId").toString());
                            if (company_result.get("Name") != null)
                                company.setName(company_result.get("Name").toString());
                            if (company_result.get("Location") != null)
                                company.setLocation(company_result.get("Location").toString());
                            if (company_result.get("Address") != null)
                                company.setAddress(company_result.get("Address").toString());
                            if (company_result.get("Industry") != null)
                                company.setIndustry(company_result.get("Industry").toString());
                            if (company_result.get("Description") != null)
                                company.setDescription(company_result.get("Description").toString());
                            if (company_result.get("Size") != null)
                                company.setCompany_size(company_result.get("Size").toString());
                            if (company_result.get("Website") != null)
                                company.setWebsite(company_result.get("Website").toString());
                            if (company_result.get("Clients") != null)
                                company.setClients(company_result.get("Clients").toString());

                            job.setCompany(company);
                            jobs.add(job);
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }


            } else {
                //Saved Jobs

                //Retrieve rows from SavedJobOffer table with the StudentId equals to the id of the current user
                ParseQuery<ParseObject> savedOfferQuery = ParseQuery.getQuery("SavedJobOffer");
                savedOfferQuery.whereEqualTo("StudentId", ParseUser.getCurrentUser().getObjectId());

                try {
                    List<ParseObject> results = savedOfferQuery.find();
                    for (ParseObject p : results) {
                        //For each offer in the SavedJobOffer, retrieve the data contained in the JobOffer table with all the information
                        // of the specific job offer

                        ParseQuery<ParseObject> jobOfferQuery = ParseQuery.getQuery("JobOffer");
                        jobOfferQuery.whereEqualTo("objectId", p.get("OfferId"));
                        ParseObject job_result = jobOfferQuery.getFirst();
                        Job job = new Job();
                        job.setId(job_result.getObjectId());
                        if (job_result.get("Position") != null)
                            job.setPosition(job_result.get("Position").toString());
                        if (job_result.get("Industry") != null)
                            job.setIndustry(job_result.get("Industry").toString());
                        if (job_result.get("Description") != null)
                            job.setDescription(job_result.get("Description").toString());
                        if (job_result.get("Location") != null)
                            job.setLocation(job_result.get("Location").toString());
                        if (job_result.get("Salary") != null)
                            job.setSalary(job_result.get("Salary").toString());
                        if (job_result.get("JobType") != null)
                            job.setTypeJob(job_result.get("JobType").toString());
                        if (job_result.get("Duration") != null)
                            job.setDuration(job_result.get("Duration").toString());
                        if (job_result.get("ContractType") != null)
                            job.setContractType(job_result.get("ContractType").toString());
                        if (job_result.getCreatedAt()!= null)
                            job.setDate(job_result.getCreatedAt().toString());

                        //Each job offer is related with a company through the companyId. Retrieve the company related
                        //by searching it on the Company table
                        ParseQuery<ParseObject> companyQuery = ParseQuery.getQuery("Company");
                        companyQuery.whereEqualTo("CompanyId", job_result.get("CompanyId"));
                        ParseObject company_result = companyQuery.getFirst();
                        Company company = new Company();
                        if (company_result.get("CompanyID") != null)
                            company.setId(company_result.get("CompanyId").toString());
                        if (company_result.get("Name") != null)
                            company.setName(company_result.get("Name").toString());
                        if (company_result.get("Location") != null)
                            company.setLocation(company_result.get("Location").toString());
                        if (company_result.get("Address") != null)
                            company.setAddress(company_result.get("Address").toString());
                        if (company_result.get("Industry") != null)
                            company.setIndustry(company_result.get("Industry").toString());
                        if (company_result.get("Description") != null)
                            company.setDescription(company_result.get("Description").toString());
                        if (company_result.get("Size") != null)
                            company.setCompany_size(company_result.get("Size").toString());
                        if (company_result.get("Website") != null)
                            company.setWebsite(company_result.get("Website").toString());
                        if (company_result.get("Clients") != null)
                            company.setClients(company_result.get("Clients").toString());

                        job.setCompany(company);
                        jobs.add(job);

                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            return jobs;
        }

        @Override
        protected void onPostExecute(ArrayList<Job> jobs) {
            super.onPostExecute(jobs);
            if (progressDialog.isShowing()) {
                progressDialog.hide();
            }

            JobAdapter jAdapter = new JobAdapter(JobSearchResults.this, jobs);

            ListView list_jobs = (ListView) findViewById(R.id.listResults);
            list_jobs.setAdapter(jAdapter);
            list_jobs.setEmptyView(findViewById(R.id.textNoResults));

        }
    }
}
