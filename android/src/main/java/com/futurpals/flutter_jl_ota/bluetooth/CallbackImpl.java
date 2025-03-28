package com.futurpals.flutter_jl_ota.bluetooth;


/**
 * @author zqjasonZhong
 * @since 2022/9/14
 * @email zhongzhuocheng@zh-jieli.com
 * @desc 回调实现类
 */
public interface CallbackImpl<T> {

    void onCallback(T callback);
}