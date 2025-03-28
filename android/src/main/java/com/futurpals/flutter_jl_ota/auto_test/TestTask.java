package com.futurpals.flutter_jl_ota.auto_test;

import androidx.annotation.NonNull;

public abstract class TestTask {

    public static final int TASK_TYPE_CONNECT = 1;
    public static final int TASK_TYPE_UPDATE = 2;

    // Error Code
    public static final int ERR_SUCCESS = 0;
    public static final int ERR_FAILED = 1;
    public static final int ERR_INVALID_PARAM = 2;
    public static final int ERR_TASK_IN_PROGRESS = 3;
    public static final int ERR_USE_CANCEL = 4;

    private final int type;

    protected TestTask(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public abstract String getName();

    public abstract boolean isRun();

    public abstract void startTest(@NonNull OnTaskListener listener);

    public abstract boolean stopTest();

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        TestTask testTask = (TestTask) other;
        return type == testTask.type;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(type);
    }

    @Override
    public String toString() {
        return "TestTask(type=" + type + "), name = " + getName() + ", isRun = " + isRun();
    }
}