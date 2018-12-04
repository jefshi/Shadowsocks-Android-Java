package com.csp.sample.ui;

import android.content.Context;
import android.widget.Chronometer;

import com.csp.adapter.recyclerview.SingleAdapter;
import com.csp.adapter.recyclerview.ViewHolder;
import com.csp.sample.R;
import com.csp.sample.proxy.BoostApp;
import com.csp.sample.proxy.BoosterServer;
import com.csp.utillib.DateUtils;

class AppManagerAdapter extends SingleAdapter<BoostApp> {

    AppManagerAdapter(Context context) {
        super(context, R.layout.layout_apps_item);
    }

    @Override
    protected void onBind(ViewHolder holder, BoostApp app, int position) {
        BoosterServer.syncBoostApp(app);

        boolean boosted = app.isBoosted();
        holder.setImageDrawable(R.id.itemicon, app.getAppIcon())
                .setText(R.id.itemlable, app.getAppLabel())
                .setChecked(R.id.itemcheck, boosted);

        Chronometer itemtime = holder.getView(R.id.itemtime);
        if (boosted) {
            itemtime.setBase(DateUtils.getNowClock() - app.getBoostedTime());
            itemtime.start();
        } else {
            itemtime.setBase(DateUtils.getNowClock());
            itemtime.stop();
        }

//        holder.getConvertView().setOnClickListener(v -> {
//            Switch switchView = holder.getView(R.id.itemcheck);
//            boolean checked = !switchView.isChecked();
//            if (checked) {
//                BoosterServer.getInstance().addBoostApp(app);
//                itemtime.setBase(DateUtils.getNowClock() - app.getBoostedTime());
//                itemtime.start();
//            } else {
//                BoosterServer.getInstance().removeBoostApp(app);
//                itemtime.setBase(DateUtils.getNowClock());
//                itemtime.stop();
//            }
//            switchView.setChecked(checked);
//        });
    }
}