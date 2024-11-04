package com.upn.contactsapp;

import static android.content.ContentValues.TAG;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.upn.contactsapp.activities.CreateContactActivity;
import com.upn.contactsapp.activities.LoginActivity;
import com.upn.contactsapp.adapters.ContactAdaptar;
import com.upn.contactsapp.daos.ContactDAO;
import com.upn.contactsapp.entities.Contact;
import com.upn.contactsapp.services.ContactService;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    List<Contact> elementos = new ArrayList<>();
    ContactAdaptar adaptar;
    private boolean isLoading = false;
    private int currentPage = 1;
    ContactService service;
    RecyclerView rvContacts;
    ProgressBar progressBar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = findViewById(R.id.progressBar);
        SharedPreferences sharedPref = getSharedPreferences("com.upn.contactsapp", Context.MODE_PRIVATE);
        String token = sharedPref.getString("TOKEN", null);
        Log.i("LoginActivity", "TOKEN: " + token);

        if (token == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }


//        AppDatabase db = AppDatabase.getInstance(this);
//        ContactDAO contactDAO = db.contactDAO();


//        List<Contact> contacts = contactDAO.getAll();
//        elementos.addAll(contacts);


        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://66d5b903f5859a7042673752.mockapi.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        service = retrofit.create(ContactService.class);
        setUpRecyclerView();
        cargarContactos(currentPage);
        rvContacts.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                LinearLayoutManager layoutManager = (LinearLayoutManager) recyclerView.getLayoutManager();
                if (layoutManager != null && layoutManager.findLastCompletelyVisibleItemPosition() == elementos.size() - 1) {
                    if (!isLoading) {
                        currentPage++;
                        cargarContactos(currentPage);

                    }
                }
            }
        });


//        service.getAll().enqueue(new Callback< List<Contact> >() {
//            @Override
//            public void onResponse(Call<List<Contact>> call, Response< List<Contact> > response) {
//                //if (response.code() == 200)
//                Log.i("MAIN_APP", String.valueOf(response.code()));
//                if (response.isSuccessful()){
//                    //elementos = response.body();
//                    elementos.clear();
//                    elementos.addAll(response.body());
//                    adaptar.notifyDataSetChanged();
//
//                    for(Contact contact: response.body()) {
//                        Contact localContact = contactDAO.findRemote(contact.id);
//                        if (localContact == null) {
//                            contactDAO.insert(contact);
//                        }
//                    }
//
//                }
//                // aca puedo trabajar con el resultado
//            }
//
//            @Override
//            public void onFailure(Call<List<Contact>> call, Throwable throwable) {
//                Log.e("MAIN_APP", throwable.getMessage());
//            }
//        });



        FloatingActionButton btnCreateContact = findViewById(R.id.btnCreateContact);
        btnCreateContact.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, CreateContactActivity.class);
            startActivityForResult(intent, 100);
        });

        //Log.i("MAIN_APP", new Gson().toJson(contacts));

//        for (Contact contact: contacts) {
//            if (contact.id != 0) continue;
//            service.create(contact).enqueue(new Callback<Contact>() {
//                @Override
//                public void onResponse(Call<Contact> call, Response<Contact> response) {
//                    Log.i("MAIN_APP", String.valueOf(response.code()));
//
//                    if (response.isSuccessful()) {
//
//                        Contact newContact = response.body();
//
//                        Intent intent = getIntent();
//                        intent.putExtra("CONTACT", new Gson().toJson(newContact));
//                        contactDAO.update(contact.localId, newContact.id);
//
//                    }
//
//                }
//
//                @Override
//                public void onFailure(Call<Contact> call, Throwable throwable) {
//                    Log.e("MAIN_APP", throwable.getMessage());
//                }
//            });
//        }

    }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data) {
         super.onActivityResult(requestCode, resultCode, data);

         if (requestCode == 100 && resultCode == 100) {
             String contactJson = data.getStringExtra("CONTACT");
             Contact contact = new Gson().fromJson(contactJson, Contact.class);

             elementos.add(contact);
             adaptar.notifyDataSetChanged();
         }

     }

    private void setUpRecyclerView() {
        rvContacts = findViewById(R.id.rvContacts);
        rvContacts.setLayoutManager(new LinearLayoutManager(this));

        adaptar = new ContactAdaptar(elementos);
        rvContacts.setAdapter(adaptar);
    }
    private void cargarContactos(int page){
        isLoading=true;
        progressBar.setVisibility(View.VISIBLE);
        service.getByPage(10,page).enqueue(new Callback<List<Contact>>() {
            @Override
            public void onResponse(Call<List<Contact>> call, Response<List<Contact>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    elementos.addAll(response.body());
                    adaptar.notifyDataSetChanged();
                }
                isLoading = false;
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onFailure(Call<List<Contact>> call, Throwable throwable) {
                Log.d(TAG,"Error de Conexion");
                isLoading= false;
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}