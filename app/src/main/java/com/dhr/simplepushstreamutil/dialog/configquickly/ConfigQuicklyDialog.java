package com.dhr.simplepushstreamutil.dialog.configquickly;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.activity.MainActivity;
import com.dhr.simplepushstreamutil.bean.ConfigQuicklyPushBean;
import com.dhr.simplepushstreamutil.bean.LocalDataBean;
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ConfigQuicklyDialog extends Dialog {
    private SharedPreferencesUtil sharedPreferencesUtil;
    private MainActivity mainActivity;
    private Gson gson = new Gson();

    private Button btnOk;
    private Button btnCancel;

    private TextView tvAreaAndRoomName;
    private Button btnLiveArea;
    private Spinner spFormatList;
    private List<String> listFormat;
    private ArrayAdapter<String> formatListAdapter;
    private ConfigQuicklyPushBean configQuicklyPushBean;
    private String roomName;
    private String liveArea;

    public ConfigQuicklyDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.mainActivity = (MainActivity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_config_quickly);

        initView();
    }

    private void initView() {
        sharedPreferencesUtil = mainActivity.getSharedPreferencesUtil();

        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);

        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);

        tvAreaAndRoomName = findViewById(R.id.tvAreaAndRoomName);
        btnLiveArea = findViewById(R.id.btnLiveArea);
        btnLiveArea.setOnClickListener(onClickListener);
        spFormatList = findViewById(R.id.spFormatList);
        listFormat = new ArrayList<>();
        listFormat.add("640x360");
        listFormat.add("854x480");
        listFormat.add("1280x720");
        listFormat.add("1920x1080");
        formatListAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, listFormat);
        formatListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormatList.setAdapter(formatListAdapter);

        configQuicklyPushBean = mainActivity.getLocalDataBean().getConfigQuicklyPushBean();
        if (null == configQuicklyPushBean) {
            configQuicklyPushBean = new ConfigQuicklyPushBean();
        } else {
            liveArea = configQuicklyPushBean.getAreaId();
            roomName = configQuicklyPushBean.getRoomName();
            for (int i = 0; i < listFormat.size() - 1; i++) {
                if (configQuicklyPushBean.getResolution().equals(listFormat.get(i))) {
                    spFormatList.setSelection(i);
                }
            }
            tvAreaAndRoomName.setText(String.format("分区id：%s  房间标题：%s", liveArea, roomName));
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnOk:
                    if (null == roomName || roomName.isEmpty()) {
                        Toast.makeText(mainActivity.getApplicationContext(), "请输入房间标题", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (null == liveArea || liveArea.isEmpty()) {
                        Toast.makeText(mainActivity.getApplicationContext(), "请选择分区", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    configQuicklyPushBean.setAreaId(liveArea);
                    configQuicklyPushBean.setRoomName(roomName);
                    configQuicklyPushBean.setResolution(listFormat.get(spFormatList.getSelectedItemPosition()));
                    mainActivity.getLocalDataBean().setConfigQuicklyPushBean(configQuicklyPushBean);
                    sharedPreferencesUtil.put(LocalDataBean.class.getSimpleName(), gson.toJson(mainActivity.getLocalDataBean()));
                    dismiss();
                    break;
                case R.id.btnCancel:
                    dismiss();
                    break;
                case R.id.btnLiveArea:
                    mainActivity.getAreaList();
                    break;
            }
        }
    };

    public void updateData(String liveArea, String roomName) {
        this.liveArea = liveArea;
        this.roomName = roomName;
        tvAreaAndRoomName.setText(String.format("分区id：%s  房间标题：%s", liveArea, roomName));
    }

    @Override
    public void show() {
        super.show();
        /**
         * 设置宽度全屏，要设置在show的后面
         */
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.gravity = Gravity.CENTER;
        layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
        layoutParams.height = ViewGroup.LayoutParams.WRAP_CONTENT;

        getWindow().getDecorView().setPadding(0, 0, 0, 0);

        getWindow().setAttributes(layoutParams);
    }

}
