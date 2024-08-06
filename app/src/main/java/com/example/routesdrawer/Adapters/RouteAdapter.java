package com.example.routesdrawer.Adapters;

import android.content.Context;
import android.media.MediaRouter2;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.routesdrawer.Models.Route;
import com.example.routesdrawer.R;
import com.google.android.material.textview.MaterialTextView;

import java.util.List;

public class RouteAdapter extends RecyclerView.Adapter<RouteAdapter.RouteViewHolder> {
    private Context context;
    private RouteCallbacks routeCallbacks;
    private List<Route> routes;

    public RouteAdapter(Context context, List<Route> routes) {
        this.context = context;
        this.routes = routes;
    }

    public RouteAdapter setRouteCallbacks(RouteCallbacks routeCallbacks) {
        this.routeCallbacks = routeCallbacks;
        return this;
    }

    public RouteAdapter setRoutes(List<Route> routes) {
        this.routes = routes;
        return this;
    }

    @NonNull
    @Override
    public RouteAdapter.RouteViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.route_item, viewGroup, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RouteAdapter.RouteViewHolder routeViewHolder, int i) {
        Route route = getRoute(i);
        routeViewHolder.routeName.setText(route.getName());
        routeViewHolder.routeSize.setText(route.getLocations().size()+"");
        if (routeCallbacks != null) {
            routeViewHolder.itemView.setOnClickListener(v -> routeCallbacks.onRouteClicked(getRoute(i)));
        }
    }
    public Route getRoute(int position) {
        return routes.get(position);
    }

    @Override
    public int getItemCount() {
        return routes.size();
    }
    public class RouteViewHolder extends RecyclerView.ViewHolder {
        MaterialTextView routeName;
        MaterialTextView routeSize;
        public RouteViewHolder(@NonNull View view) {
            super(view);
            routeName = view.findViewById(R.id.route_name);
            routeSize = view.findViewById(R.id.route_size);
        }
    }

    public interface RouteCallbacks {
        void onRouteClicked(Route route);
    }

}
