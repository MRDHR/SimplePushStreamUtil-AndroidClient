package com.dhr.simplepushstreamutil.dialog.serverinfo;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.activity.MainActivity;
import com.dhr.simplepushstreamutil.bean.LocalDataBean;
import com.dhr.simplepushstreamutil.bean.ServerInfoBean;
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class ServerInfoDialog extends Dialog {
    private CallBack callBack;
    private MainActivity mainActivity;

    private Button btnOk;
    private Button btnCancel;
    private Button btnRemove;

    private SharedPreferencesUtil sharedPreferencesUtil;
    private Gson gson = new Gson();
    private List<ServerInfoBean> serverInfoBeans;

    private ListView listContent;
    private ArrayAdapter<String> arrayAdapter;
    private List<String> list;
    private int checkedItemPosition;

    public ServerInfoDialog(@NonNull Context context, @StyleRes int themeResId, CallBack callBack) {
        super(context, themeResId);
        this.mainActivity = (MainActivity) context;
        this.callBack = callBack;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_serverinfo_load);

        initView();
        setOnShowListener(new OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                loadDataFromJson();
            }
        });
    }

    private void initView() {
        btnOk = findViewById(R.id.btnOk);
        btnCancel = findViewById(R.id.btnCancel);
        btnRemove = findViewById(R.id.btnRemove);

        btnOk.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
        btnRemove.setOnClickListener(onClickListener);

        listContent = findViewById(R.id.listContent);
    }

    private void loadDataFromJson() {
        sharedPreferencesUtil = mainActivity.getSharedPreferencesUtil();
        serverInfoBeans = mainActivity.getLocalDataBean().getServerInfoBeans();
        if (null != serverInfoBeans) {
            if (null == list) {
                list = new ArrayList<>();
            }
            list.clear();
            for (ServerInfoBean bean : this.serverInfoBeans) {
                list.add(bean.getSaveName());
            }
            if (null == arrayAdapter) {
                arrayAdapter = new ArrayAdapter<>(mainActivity, android.R.layout.simple_list_item_single_choice, list);
                listContent.setAdapter(arrayAdapter);
            } else {
                arrayAdapter.notifyDataSetChanged();
            }
            if (!this.serverInfoBeans.isEmpty()) {
                listContent.setItemChecked(0, true);
            }
        } else {
            serverInfoBeans = new ArrayList<>();
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btnCancel:
                    dismiss();
                    break;
                case R.id.btnOk:
                    checkedItemPosition = listContent.getCheckedItemPosition();
                    if (checkedItemPosition >= 0) {
                        ServerInfoBean serverInfoBean = serverInfoBeans.get(checkedItemPosition);
                        callBack.confirm(serverInfoBean.getIp(), serverInfoBean.getPort(), serverInfoBean.getUserName(), serverInfoBean.getUserPassword());
                        dismiss();
                    } else {
                        Toast.makeText(mainActivity.getApplicationContext(), "请选择需要提取的记录", Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.btnRemove:
                    checkedItemPosition = listContent.getCheckedItemPosition();
                    if (checkedItemPosition >= 0) {
                        AlertDialog.Builder
                                normalDialog =
                                new AlertDialog.Builder(mainActivity);
                        normalDialog.setTitle("温馨提示：");
                        normalDialog.setCancelable(false);
                        normalDialog.setMessage("是否删除该条数据？");
                        normalDialog.setPositiveButton("是",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (!serverInfoBeans.isEmpty()) {
                                            serverInfoBeans.remove(checkedItemPosition);
                                            mainActivity.getLocalDataBean().setServerInfoBeans(serverInfoBeans);
                                            sharedPreferencesUtil.put(LocalDataBean.class.getSimpleName(), gson.toJson(mainActivity.getLocalDataBean()));
                                        }
                                        loadDataFromJson();
                                    }
                                });
                        normalDialog.setNegativeButton("否",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });
                        normalDialog.show();
                    } else {
                        Toast.makeText(mainActivity.getApplicationContext(), "请选择需要删除的记录", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };

    public interface CallBack {
        void confirm(String ip, int port, String userName, String userPassword);
    }
}
