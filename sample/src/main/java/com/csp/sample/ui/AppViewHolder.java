package com.csp.sample.ui;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import com.csp.proxy.core.AppProxyManager;
import com.csp.sample.App;
import com.csp.sample.R;
import com.csp.sample.proxy.BoostApp;
import com.csp.utillib.AppUtil;


class AppViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    private ImageView itemicon = (ImageView) itemView.findViewById(R.id.itemicon);
    private TextView itemlable = (TextView) itemView.findViewById(R.id.itemlable);
    private Chronometer itemtime = (Chronometer) itemView.findViewById(R.id.itemtime);
    private Switch check = (Switch) itemView.findViewById(R.id.itemcheck);

    private BoostApp item;
    private Boolean proxied = false;

    AppViewHolder(View itemView) {
        super(itemView);
        itemView.setOnClickListener(this);
    }

    void bind(BoostApp app) {
        this.item = app;
        proxied = AppProxyManager.getInstance().isProxyApp(app);
        itemlable.setText(app.getAppLabel());
        check.setChecked(proxied);

        Drawable icon = item.getAppIcon();
        if (icon != null)
            itemicon.setImageDrawable(icon);

        new Thread(new Runnable() {
            @Override
            public void run() {
                Context context = App.getContext();
                PackageManager pm = context.getPackageManager();
                ResolveInfo resolveInfo = AppUtil.searchApplication(context, item.getPackageName());
                if (resolveInfo != null) {
                    final Drawable icon = resolveInfo.loadIcon(pm);

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            itemicon.setImageDrawable(icon);
                        }
                    });
                    item.setAppIcon(icon);
                }
            }
        }).start();
    }


    @Override
    public void onClick(View view) {
        if (proxied) {
            AppProxyManager.getInstance().removeProxyApp(item);
            check.setChecked(false);
        } else {
            AppProxyManager.getInstance().addProxyApp(item);
            check.setChecked(true);
        }
        proxied = !proxied;
    }
}