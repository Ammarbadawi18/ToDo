package com.example.todo;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.FirebaseApp;
import com.google.firebase.appcheck.FirebaseAppCheck;
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.util.Date;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FloatingActionButton actionButton;
    private DatabaseReference reference;
    private FirebaseAuth auth;
    private FirebaseUser user;
    private String onlineuser;
    private ProgressDialog loader;

    private String key ="";
    private String task;
    private String description;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        FirebaseApp.initializeApp(this);
        FirebaseAppCheck firebaseAppCheck = FirebaseAppCheck.getInstance();
        firebaseAppCheck.installAppCheckProviderFactory(SafetyNetAppCheckProviderFactory.getInstance());
        recyclerView = findViewById(R.id.recyle);
        LinearLayoutManager Linear = new LinearLayoutManager(this);
        Linear.setReverseLayout(true);
        Linear.setStackFromEnd(true);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(Linear);
        loader = new ProgressDialog(this);
        auth=FirebaseAuth.getInstance();
        user =auth.getCurrentUser();
        onlineuser=user.getUid();



        reference = FirebaseDatabase.getInstance().getReference().child("tasks").child(onlineuser);
        actionButton = findViewById(R.id.f);
        actionButton.setOnClickListener((v -> {addTask();} ));

    }
    private void addTask() {
        AlertDialog.Builder myd= new AlertDialog.Builder(this);
        LayoutInflater lf= LayoutInflater.from(this);
        View myv = lf.inflate(R.layout.input_file,null);
        myd.setView(myv);
        final AlertDialog dialog = myd.create();
      dialog.setCancelable(false);

      final EditText task = myv.findViewById(R.id.task);
      final EditText description = myv.findViewById(R.id.desc);
      Button save = myv.findViewById(R.id.savebtn);
      Button cancel = myv.findViewById(R.id.cacelbtn);

      cancel.setOnClickListener((v -> {dialog.dismiss();}));

      save.setOnClickListener((v -> {
          String mtask=task.getText().toString().trim();
          String mdesc=description.getText().toString().trim();
          String id= reference.push().getKey();
          String date= DateFormat.getDateInstance().format(new Date());

          if(TextUtils.isEmpty(mtask)){
              task.setError("Task required");
              return;
          }
          if(TextUtils.isEmpty(mdesc)){
              task.setError("Description required");
              return;
          }else{
            loader.setMessage("Adding your data");
            loader.setCanceledOnTouchOutside(false);
            loader.show();

            Model model = new Model(mtask,mdesc,id,date);
            reference.child(id).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(HomeActivity.this, "Task added", Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }else{
                        String error = task.getException().toString();
                        Toast.makeText(HomeActivity.this, "Failed" +error, Toast.LENGTH_SHORT).show();
                        loader.dismiss();
                    }
                }
            });
          }
            dialog.dismiss();
      }));
        dialog.show();

    }
    @Override
    public void onStart(){
        super.onStart();
        FirebaseRecyclerOptions<Model>options = new FirebaseRecyclerOptions.Builder<Model>().setQuery(reference,Model.class).build();
        FirebaseRecyclerAdapter<Model,MyViewHolder>adapter = new FirebaseRecyclerAdapter<Model, MyViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull MyViewHolder holder, @SuppressLint("RecyclerView") final int position, @NonNull Model model) {
                holder.setDate(model.getDate());
                holder.setDescription(model.getDescription());
                holder.setTask(model.getTask());

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        key = getRef(position).getKey();
                        task =model.getTask();
                        description= model.getDescription();
                        updateTask();

                    }
                });
            }

            @NonNull
            @Override
            public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                View view = getLayoutInflater().from(parent.getContext()).inflate(R.layout.retrieved_layout,parent,false);
                return new MyViewHolder(view);
            }
        };
        recyclerView.setAdapter(adapter);
        adapter.startListening();
    }
    public static class MyViewHolder extends RecyclerView.ViewHolder{
            View mv;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            mv=itemView;
        }
        public void setTask(String task){
            TextView TaskTextView = mv.findViewById(R.id.texttask);
            TaskTextView.setText(task);
        }
        public void setDescription(String description){
            TextView DescTextView = mv.findViewById(R.id.textdesc);
            DescTextView.setText(description);
        }
        public void setDate(String date){
            TextView DateTextView = mv.findViewById(R.id.textdate);
            DateTextView.setText(date);
        }
    }

    private void updateTask(){
        AlertDialog.Builder myd= new AlertDialog.Builder(this);
        LayoutInflater lf= LayoutInflater.from(this);
        View v= lf.inflate(R.layout.update_date,null);
        myd.setView(v);

        AlertDialog dialog = myd.create();
        EditText utask= v.findViewById(R.id.editupdate);
        EditText udesc=v.findViewById(R.id.editdescupdate);

        utask.setText(task);
        utask.setSelection(task.length());
        udesc.setText(description);
        udesc.setSelection(description.length());
        Button delete =v.findViewById(R.id.deletebtn);
        Button update = v.findViewById(R.id.updatebtn);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                task = utask.getText().toString().trim();
                description = udesc.getText().toString().trim();
                String date = DateFormat.getDateInstance().format(new Date());
                Model model = new Model(task,description,key,date);
                reference.child(key).setValue(model).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Update done.", Toast.LENGTH_SHORT).show();
                        }else{
                            String error = task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Update failed." +error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();
            }
        });
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reference.child(key).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if(task.isSuccessful()){
                            Toast.makeText(HomeActivity.this, "Task deleted.", Toast.LENGTH_SHORT).show();
                        }else{
                            String error =task.getException().toString();
                            Toast.makeText(HomeActivity.this, "Failed" +error, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                dialog.dismiss();

            }
        });
        dialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.logout:
                auth.signOut();
                Intent i =new Intent(HomeActivity.this,LoginActivity.class);
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(i);
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}