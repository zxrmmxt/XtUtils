package com.xt.common.basestation;

/**
 * created by XuTi on 2019/5/10 13:16
 */
public interface OnCellLocationCallback {
    void OnCellLocation(int mcc, int mnc, int lac, int cid);
    void OnCellLocationError();
}
