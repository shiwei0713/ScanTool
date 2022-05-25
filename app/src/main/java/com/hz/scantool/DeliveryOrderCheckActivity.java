/**
*文件：DeliveryOrderCheckActivity,2022/5/25
*描述: 1、出货扫码三点照合，扫描内部标签显示零件信息，扫描客户标签二次核对，并记录核对记录
*作者：
**/package com.hz.scantool;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

public class DeliveryOrderCheckActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_delivery_order_check);
    }
}