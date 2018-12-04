package com.csp.sample.ui;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.csp.proxy.core.ProxyReceiver;
import com.csp.proxy.core.ProxyState;
import com.csp.sample.R;
import com.csp.sample.proxy.BoostApp;
import com.csp.sample.proxy.BoosterServer;
import com.csp.utillib.AppUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by so898 on 2017/5/3.
 */

public class AppManager extends Activity implements ProxyReceiver {

    private static final int START_VPN_SERVICE_REQUEST_CODE = 1985;

    private AppManagerAdapter mAdapter;
    private BoostApp mTempApp;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.layout_apps);

        // 返回键
        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        final ProgressBar prbLoading = findViewById(R.id.prb_loading);

        mAdapter = new AppManagerAdapter(this);
        mAdapter.setOnItemClickListener((parent, view, viewHolder, position, id) -> {
            BoosterServer server = BoosterServer.getInstance();
            BoostApp item = mAdapter.getItem(position);
            boolean boosted = BoosterServer.getInstance().isBoostApp(item);
            if (boosted) {
                server.removeBoostApp(item);
                return;
            }

            if (!server.isPrepareProxy()) {
                mTempApp = item;
                Intent intent = server.prepareProxy();
                startActivityForResult(intent, START_VPN_SERVICE_REQUEST_CODE);
            } else {
                server.addBoostApp(item);
            }
        });

        RecyclerView rcvContent = findViewById(R.id.rcv_content);
        rcvContent.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rcvContent.setItemAnimator(new DefaultItemAnimator());
        rcvContent.setAdapter(mAdapter);

        new Thread(() -> {
            List<BoostApp> boostApps = queryAppInfo();
            runOnUiThread(() -> {
                mAdapter.addData(boostApps, false);
                mAdapter.notifyDataSetChanged();
                prbLoading.setVisibility(View.GONE);
            });
        }).start();

        BoosterServer.getInstance().registerReceiver(this);
    }

    public List<BoostApp> queryAppInfo() {
        final PackageManager manager = getPackageManager();
        List<PackageInfo> packageInfos = AppUtil.getAppNotSystem(this);

        // 首次耗时
        Collections.sort(packageInfos, (o1, o2) -> {
            CharSequence label1 = o1.applicationInfo.loadLabel(manager);
            CharSequence label2 = o2.applicationInfo.loadLabel(manager);
            return label1.toString().compareTo(label2.toString());
        });

        // loadIcon：首次耗时
        List<BoostApp> list = new ArrayList<>();
        for (PackageInfo packageInfo : packageInfos) {
            BoostApp appInfo = new BoostApp();
            appInfo.setAppLabel(packageInfo.applicationInfo.loadLabel(manager).toString());
            appInfo.setPackageName(packageInfo.packageName);
            appInfo.setAppIcon(packageInfo.applicationInfo.loadIcon(manager));

            if (!appInfo.getPackageName().equals(getPackageName()))
                list.add(appInfo);
        }
        return list;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (requestCode == START_VPN_SERVICE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                BoosterServer.getInstance().addBoostApp(mTempApp);
            } else {
                onStatusChanged(new ProxyState("canceled."));
            }
            return;
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onStatusChanged(ProxyState state) {
        switch (state.getCode()) {
            case ProxyState.CODE_APP_PROXY_ADD:
            case ProxyState.CODE_APP_PROXY_REMOVE:
                mAdapter.notifyDataSetChanged();
                break;
        }
    }
}
