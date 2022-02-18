package com.example.foodiehub.DeliveryFoodPanel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.foodiehub.R;
import com.example.foodiehub.SendNotification.APIService;
import com.example.foodiehub.SendNotification.Client;
import com.example.foodiehub.SendNotification.Data;
import com.example.foodiehub.SendNotification.MyResponse;
import com.example.foodiehub.SendNotification.NotificationSender;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DeliveryShipOrderFragmentAdapter extends RecyclerView.Adapter<DeliveryShipOrderFragmentAdapter.ViewHolder> {

    private final Context context;
    private final List<DeliveryShipFinalOrders1> deliveryShipFinalOrders1list;
    private APIService apiService;


    public DeliveryShipOrderFragmentAdapter(Context context, List<DeliveryShipFinalOrders1> deliveryShipFinalOrders1list) {
        this.deliveryShipFinalOrders1list = deliveryShipFinalOrders1list;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.delivery_shiporders, parent, false);
        apiService = Client.getClient("https://fcm.googleapis.com/").create(APIService.class);
        return new ViewHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        final DeliveryShipFinalOrders1 deliveryShipFinalOrders1 = deliveryShipFinalOrders1list.get(position);
        holder.Address.setText(deliveryShipFinalOrders1.getAddress());
        holder.grandtotalprice.setText("Grand Total: ₹ " + deliveryShipFinalOrders1.getGrandTotalPrice());
        holder.mobilenumber.setText("+91" + deliveryShipFinalOrders1.getMobileNumber());
        final String random = deliveryShipFinalOrders1.getRandomUID();
        final String userid = deliveryShipFinalOrders1.getUserId();
        holder.Vieworder.setOnClickListener(v -> {
            Intent intent = new Intent(context, DeliveryShipOrderView.class);
            intent.putExtra("RandomUID", random);
            context.startActivity(intent);
        });

        holder.ShipOrder.setOnClickListener(v -> FirebaseDatabase.getInstance().getReference("CustomerFinalOrders").child(userid).child(random).child("OtherInformation").child("Status").setValue("Your Order is on the way...").addOnSuccessListener(aVoid -> FirebaseDatabase.getInstance().getReference().child("Tokens").child(userid).child("token").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String usertoken = dataSnapshot.getValue(String.class);
                sendNotifications(usertoken);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        })).addOnSuccessListener(aVoid -> {
            Intent intent = new Intent(context, Delivery_ShippingOrder.class);
            intent.putExtra("RandomUID",random);
            context.startActivity(intent);
        }));

    }

    private void sendNotifications(String usertoken) {

        Data data = new Data("Estimated Time", "Your Order has been collected by Delivery Person, He is on the way", "DeliverOrder");
        NotificationSender sender = new NotificationSender(data, usertoken);
        apiService.sendNotification(sender).enqueue(new Callback<MyResponse>() {
            @Override
            public void onResponse(@NonNull Call<MyResponse> call, @NonNull Response<MyResponse> response) {
                if (response.code() == 200) {
                    assert response.body() != null;
                    if (response.body().success != 1) {
                        Toast.makeText(context, "Failed", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<MyResponse> call, @NonNull Throwable t) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return deliveryShipFinalOrders1list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        TextView Address, grandtotalprice, mobilenumber;
        Button Vieworder, ShipOrder;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            Address = itemView.findViewById(R.id.ad2);
            mobilenumber = itemView.findViewById(R.id.MB2);
            grandtotalprice = itemView.findViewById(R.id.TP2);
            Vieworder = itemView.findViewById(R.id.view2);
            ShipOrder = itemView.findViewById(R.id.ship2);
        }
    }
}
