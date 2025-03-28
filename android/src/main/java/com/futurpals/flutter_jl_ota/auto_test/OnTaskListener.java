package com.futurpals.flutter_jl_ota.auto_test;

public interface OnTaskListener {

    void onStart(String message);

    void onLogcat(String log);

    void onFinish(int code, String message);
}