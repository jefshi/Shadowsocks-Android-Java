package com.csp.sample.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.csp.sample.App;
import com.csp.sample.R;
import com.csp.sample.proxy.BoostApp;
import com.futuremind.recyclerviewfastscroll.SectionTitleProvider;

class AppManagerAdapter extends RecyclerView.Adapter<AppViewHolder> implements SectionTitleProvider {

    @Override
    public AppViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.layout_apps_item, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(AppViewHolder holder, int position) {
        BoostApp appInfo = App.getBoostApps().get(position);
        holder.bind(appInfo);
    }

    @Override
    public int getItemCount() {
        return App.getBoostApps().size();
    }

    @Override
    public String getSectionTitle(int position) {
        BoostApp appInfo = App.getBoostApps().get(position);
        return appInfo.getAppLabel();
    }
}