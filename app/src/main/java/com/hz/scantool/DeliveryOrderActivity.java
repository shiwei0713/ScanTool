/**
*文件：DeliveryOrderActivity,2022/5/25
*描述: 1、备货完成清单显示
 *2、扫描显示指定备货单数据
*作者：shiwei
**/

package com.hz.scantool;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.room.Room;

import android.os.Bundle;
import android.widget.TextView;

import com.hz.scantool.database.DeliveryOrderEntity;
import com.hz.scantool.database.HzDb;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class DeliveryOrderActivity extends AppCompatActivity {

    private HzDb hzDb;
    private String dataBaseName = "HzDb";
    private String strTitle;
    private TextView deliveryOrderShow;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order);

        //初始化
        initView();
        initDataBase();
        insertData();
    }

    /*
     *后台操作，创建数据库
     */
    private void initDataBase(){
        hzDb = Room.databaseBuilder(this,HzDb.class,dataBaseName).build();
    }

    /**
    *描述: 初始化view控件
    *日期：2022/5/25
    **/
    private void initView(){
        //获取工具栏
        Toolbar toolbar=findViewById(R.id.positionToolBar);
        setSupportActionBar(toolbar);

        //工具栏增加返回按钮和标题显示
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setTitle(strTitle);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        //初始化显示控件
        deliveryOrderShow = findViewById(R.id.deliveryOrderShow);
    }

    /**
    *描述: 插入本地数据库
    *日期：2022/5/25
    **/
    private void insertData(){
        Observable.create(new ObservableOnSubscribe<DeliveryOrderEntity>() {
            @Override
            public void subscribe(ObservableEmitter<DeliveryOrderEntity> e) throws Exception {
                DeliveryOrderEntity deliveryOrderEntity = new DeliveryOrderEntity(1,"123","444","","34",500,"","N");
                hzDb.deliveryOrderDao().insert(deliveryOrderEntity);

                //将数据回传到主线程显示
                String sDocno=deliveryOrderEntity.getDocNo();
                e.onNext(hzDb.deliveryOrderDao().queryOrder(sDocno));
                e.onComplete();
            }
        }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<DeliveryOrderEntity>() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onNext(DeliveryOrderEntity deliveryOrderEntity) {
                deliveryOrderShow.setText(deliveryOrderEntity.getDocNo());
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }
}