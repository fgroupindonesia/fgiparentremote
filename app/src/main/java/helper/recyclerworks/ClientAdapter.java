package helper.recyclerworks;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.fgroupindonesia.fgipc.parentremote.R;

import java.util.Collections;
import java.util.List;

import bean.TargetProfile;

public class ClientAdapter extends
        RecyclerView.Adapter<ClientAdapter.ViewHolder> {

    List<TargetProfile> list = Collections.emptyList();

    Context context;
    ClickListener listiner;
    ViewHolder tempViewHolder;


    public ClientAdapter( List<TargetProfile> listna) {
        this.list = listna;
    }

    public ClientAdapter(List<TargetProfile> list,
                                Context context,ClickListener listiner)
    {
        this.list = list;
        this.context = context;
        this.listiner = listiner;
    }

    public void clearItems(){
        this.list = Collections.emptyList();
        //this.list.clear();
        notifyDataSetChanged();
    }

    public void setNewItems(List<TargetProfile> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
       // Context context  = parent.getContext();
        LayoutInflater inflater  = LayoutInflater.from(this.context);

        // Inflate the layout
        View client = inflater.inflate(R.layout.item_recyclerview_clientprofile,
                        parent, false);

        ViewHolder viewHolder = new ViewHolder(client);

        tempViewHolder = viewHolder;

        return viewHolder;


    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final int index = holder.getAdapterPosition();

        String name = list.get(position).getName();

        if(name == null){
            name = list.get(position).getIp_address();
        }else if(name.isEmpty()){
            name = list.get(position).getIp_address();
        }

        holder.textviewName.setText(name);

        String typeNa = list.get(position).getClientType();
        if(typeNa!=null){
        if(!typeNa.isEmpty()){
            if(typeNa.equalsIgnoreCase("pc")){
                holder.imageviewType.setImageResource(R.drawable.pc32);
            } else if(typeNa.equalsIgnoreCase("laptop")){
                holder.imageviewType.setImageResource(R.drawable.laptop32);
            } else if(typeNa.equalsIgnoreCase("phone")){
                holder.imageviewType.setImageResource(R.drawable.phone32);
            }
        }}

        holder.view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                listiner.click(index);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout linearLayoutClient;
        View view;
        TextView textviewName;
        ImageView imageviewType;

        public ViewHolder(View itemView) {

            super(itemView);

            textviewName = (TextView) itemView.findViewById(R.id.textviewClientName);
            imageviewType = (ImageView) itemView.findViewById(R.id.imageviewClientType);
            linearLayoutClient = (LinearLayout) itemView.findViewById(R.id.singleClient);

            view = itemView;
        }



    }



}
