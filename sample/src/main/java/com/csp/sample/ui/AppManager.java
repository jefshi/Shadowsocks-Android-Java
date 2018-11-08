package com.csp.sample.ui;

import android.animation.Animator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;

import com.csp.sample.App;
import com.csp.sample.R;
import com.csp.sample.proxy.BoostApp;
import com.csp.utillib.AppUtil;
import com.futuremind.recyclerviewfastscroll.FastScroller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by so898 on 2017/5/3.
 */

public class AppManager extends Activity {
    private View loadingView;
    private RecyclerView appListView;
    private FastScroller fastScroller;
    private AppManagerAdapter adapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.layout_apps);

        ActionBar actionBar = getActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        loadingView = findViewById(R.id.loading);
        appListView = (RecyclerView) findViewById(R.id.list);
        appListView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        appListView.setItemAnimator(new DefaultItemAnimator());
        fastScroller = (FastScroller) findViewById(R.id.fastscroller);

        Observable<List<BoostApp>> observable = Observable.create(new ObservableOnSubscribe<List<BoostApp>>() {
            @Override
            public void subscribe(ObservableEmitter<List<BoostApp>> appInfo) throws Exception {
                queryAppInfo();
                adapter = new AppManagerAdapter();
                appInfo.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        Observer<List<BoostApp>> observer = new Observer<List<BoostApp>>() {
            @Override
            public void onSubscribe(Disposable d) {
            }

            @Override
            public void onNext(List<BoostApp> aLong) {
            }

            @Override
            public void onError(Throwable e) {
            }

            @Override
            public void onComplete() {
                appListView.setAdapter(adapter);
                fastScroller.setRecyclerView(appListView);
                long shortAnimTime = 1;
                appListView.setAlpha(0);
                appListView.setVisibility(View.VISIBLE);
                appListView.animate().alpha(1).setDuration(shortAnimTime);
                loadingView.animate().alpha(0).setDuration(shortAnimTime).setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animator) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animator) {
                        loadingView.setVisibility(View.GONE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animator) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animator) {
                    }
                });
            }
        };
        observable.subscribe(observer);
    }

    public void queryAppInfo() {
        final PackageManager pManager = getPackageManager();
        List<PackageInfo> packageInfos = AppUtil.getAppNotSystem(this);

        // 耗时
        Collections.sort(packageInfos, new Comparator<PackageInfo>() {
            @Override
            public int compare(PackageInfo o1, PackageInfo o2) {
                CharSequence label1 = o1.applicationInfo.loadLabel(pManager);
                CharSequence label2 = o2.applicationInfo.loadLabel(pManager);
                return label1.toString().compareTo(label2.toString());
            }
        });

//        if (AppProxyManager.Instance.mlistAppInfo != null) {
//            AppProxyManager.Instance.mlistAppInfo.clear();
        App.getmBoostApps().clear();
        for (PackageInfo packageInfo : packageInfos) {
            String pkgName = packageInfo.packageName; // reInfo.activityInfo.packageName; // 获得应用程序的包名
            String appLabel = packageInfo.applicationInfo.loadLabel(pManager).toString(); // String) reInfo.loadLabel(pm); // 获得应用程序的Label
            // Drawable icon = reInfo.loadIcon(pm); // 获得应用程序图标
            BoostApp appInfo = new BoostApp();
            appInfo.setAppLabel(appLabel);
            appInfo.setPackageName(pkgName);
            // appInfo.setAppIcon(icon);
            if (!appInfo.getPackageName().equals(getPackageName()))//App本身会强制加入代理列表
                App.getmBoostApps().add(appInfo);
//        }
        }
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

}
