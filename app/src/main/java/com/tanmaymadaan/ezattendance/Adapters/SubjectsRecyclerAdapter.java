package com.tanmaymadaan.ezattendance.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tanmaymadaan.ezattendance.Models.UsersSubject;
import com.tanmaymadaan.ezattendance.R;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static java.lang.Math.ceil;
import static java.lang.StrictMath.abs;

public class SubjectsRecyclerAdapter extends RecyclerView.Adapter<SubjectsRecyclerAdapter.MyViewHolder> {

    Context context;
    ArrayList<UsersSubject> arrayList;

    public SubjectsRecyclerAdapter(Context context,ArrayList<UsersSubject> arrayList)
    {
        this.context = context;
        this.arrayList = arrayList;
    }

    @NonNull
    @Override
    public SubjectsRecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_subject_layout, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(view);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final SubjectsRecyclerAdapter.MyViewHolder holder, final int position) {
        holder.subject.setText(arrayList.get(position).getSubjectname());
        holder.attendance.setText("  "+String.valueOf(arrayList.get(position).getPresent())+"/"+String.valueOf(arrayList.get(position).getTotal()));
        holder.percentage.setText(String.valueOf((double) Math.round(arrayList.get(position).getPercentage() * 100) / 100)+" %");
        holder.status.setText("  "+arrayList.get(position).getStatus());
        holder.incre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(1,arrayList.get(position),position);
            }
        });

        holder.decre.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update(-1,arrayList.get(position),position);
            }
        });

        holder.optionsmenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(context, holder.optionsmenu);
                popupMenu.inflate(R.menu.recyclermenu);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        switch(item.getItemId()){
                            case R.id.delete:
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
                                db.collection(UID).document(arrayList.get(position).getSubjectname()).delete();

                                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
                                int mainpres = pref.getInt("pres",-1);
                                int maintot = pref.getInt("tot",-1);
                                double overall;
                                int pres = arrayList.get(position).getPresent();
                                int tot = arrayList.get(position).getTotal();
                                mainpres-=pres;
                                maintot-=tot;
                                overall=(double)mainpres/maintot;
                                arrayList.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position,arrayList.size());
                                if(arrayList.size()==0)
                                    overall=0;
                                db.collection(UID).document("overall").set((double) Math.round(((overall/arrayList.size())*100)* 100) / 100)
                                pref.edit().putInt("pres",mainpres).apply();
                                pref.edit().putInt("tot",maintot).apply();
                                break;
                            case R.id.edit_item :
                                Intent intent = new Intent(context, EditAttendance.class);
                                intent.putExtra("pos",position);
                                intent.putExtra("subject",arrayList.get(position).getSubjectname());
                                intent.putExtra("present",arrayList.get(position).getPresent());
                                intent.putExtra("total",arrayList.get(position).getTotal());
                                context.startActivity(intent);
                                break;
                            default:
                                return true;
                        }

                        return true;
                    }
                });

                popupMenu.show();
            }
        });

    }

    private void update(int x, UsersSubject particular, int pos) {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
        int mainpres = pref.getInt("pres",-1);
        int maintot = pref.getInt("tot",-1);
        double overall;
        int pres,tot;
        pres = particular.getPresent();
        tot = particular.getTotal();
        mainpres-=pres;
        maintot-=tot;

        particular.setTotal(tot+abs(x));
        if(x>0)
            particular.setPresent(pres+x);
        pres = particular.getPresent();
        tot = particular.getTotal();
        mainpres+=pres;
        maintot+=tot;
        overall=(double)mainpres/maintot;

        double value  = ((double)pres/tot);
        double comp = ((double)particular.getGoal())/100;

        if(value<comp)
            particular.setStatus("Attend "+ String.valueOf((int)(ceil((double)(comp*tot)-pres))) +" classes more.");
        else
            particular.setStatus("You are right on track");

        Log.d("hiii","--------------------------------------------------------");
        Log.d("comp/value",Double.toString(value)+" "+String.valueOf(comp));
        Log.d("pres/tot",Integer.toString(pres)+" "+String.valueOf(tot));
        Log.d("pres/tot",Integer.toString(mainpres)+" "+String.valueOf(maintot));
        particular.setPercentage(((double)pres/tot)*100);
        pref.edit().putInt("pres",mainpres).apply();
        pref.edit().putInt("tot",maintot).apply();
        notifyItemChanged(pos);
        updatedb(particular, overall);
    }

    private void updatedb(UsersSubject particular,double overall) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String UID = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String subject = particular.getSubjectname();
        HashMap<String, Integer> map = new HashMap<>();
        map.put("total",particular.getTotal());
        map.put("present",particular.getPresent());
        db.collection(UID).document(subject).set(map);
        db.collection(UID).document("overall").set((double) Math.round(((overall)*100)* 100) / 100);
    }

    @Override
    public int getItemCount() {
        return 0;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        protected TextView attendance, subject, percentage,status,optionsmenu;
        protected ImageButton incre,decre;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            attendance = itemView.findViewById(R.id.row_attendance_value);
            subject = itemView.findViewById(R.id.row_subject_name);
            percentage = itemView.findViewById(R.id.row_percentage);
            status = itemView.findViewById(R.id.row_status_value);
            incre = itemView.findViewById(R.id.row_add);
            decre = itemView.findViewById(R.id.row_remove);
            optionsmenu = itemView.findViewById(R.id.optionsmenu);
        }
    }
}
