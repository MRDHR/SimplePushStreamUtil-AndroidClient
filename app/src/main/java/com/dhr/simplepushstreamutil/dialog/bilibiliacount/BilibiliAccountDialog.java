package com.dhr.simplepushstreamutil.dialog.bilibiliacount;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.badoo.mobile.util.WeakHandler;
import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.activity.MainActivity;
import com.dhr.simplepushstreamutil.bean.FromClientBean;
import com.dhr.simplepushstreamutil.params.MainParam;
import com.dhr.simplepushstreamutil.util.ParseMessageUtil;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BilibiliAccountDialog extends Dialog {
    private Context context;
    private MainActivity mainActivity;
    private EditText etUserName;
    private EditText etPassWord;
    private Button btnTestLogin;
    private Button btnSave;
    private Button btnRemove;
    private Button btnCancel;

    private String userName;
    private String password;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private WeakHandler weakHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MainParam.testLoginSuccess:
                    Toast.makeText(mainActivity.getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case MainParam.testLoginFail:
                    Toast.makeText(mainActivity.getApplicationContext(), msg.obj.toString(), Toast.LENGTH_SHORT).show();
                    break;
                case MainParam.userNameOrPassWordError:
                    Toast.makeText(context, "用户名密码错误，请重新输入后再试", Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    public BilibiliAccountDialog(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
        this.context = context;
        this.mainActivity = (MainActivity) context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_bilibili_acount);

        initView();
    }

    private void initView() {
        etUserName = findViewById(R.id.etUserName);
        etPassWord = findViewById(R.id.etPassWord);
        btnTestLogin = findViewById(R.id.btnTestLogin);
        btnSave = findViewById(R.id.btnSave);
        btnRemove = findViewById(R.id.btnRemove);
        btnCancel = findViewById(R.id.btnCancel);

        btnTestLogin.setOnClickListener(onClickListener);
        btnSave.setOnClickListener(onClickListener);
        btnRemove.setOnClickListener(onClickListener);
        btnCancel.setOnClickListener(onClickListener);
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

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnTestLogin:
                    userName = etUserName.getText().toString();
                    password = etPassWord.getText().toString();
                    if (userName.isEmpty()) {
                        Toast.makeText(context, "账号不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
                    } else if (password.isEmpty()) {
                        Toast.makeText(context, "密码不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
                    } else {
                        testLogin();
                    }
                    break;
                case R.id.btnSave:
                    saveLoginInfo();
                    break;
                case R.id.btnRemove:
                    removeLoginInfo();
                    break;
                case R.id.btnCancel:
                    dismiss();
                    break;
            }
        }
    };

    private void testLogin() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (null == mainActivity.getMinaClient()) {
                    loginFail("请先连接服务器后再进行操作");
                } else {
                    FromClientBean fromClientBean = new FromClientBean();
                    fromClientBean.setType(ParseMessageUtil.TYPE_LOGIN);
                    fromClientBean.setUserName(userName);
                    fromClientBean.setPassword(password);
                    mainActivity.getMinaClient().send(fromClientBean);
                }
            }
        });
    }

    public void loginSuccess(String result) {
        Message msg = new Message();
        msg.what = MainParam.testLoginSuccess;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void loginFail(String result) {
        Message msg = new Message();
        msg.what = MainParam.testLoginFail;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    private void saveLoginInfo() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (null == mainActivity.getMinaClient()) {
                    loginFail("请先连接服务器后再进行操作");
                } else {
                    FromClientBean fromClientBean = new FromClientBean();
                    fromClientBean.setType(ParseMessageUtil.TYPE_SAVELOGININFO);
                    mainActivity.getMinaClient().send(fromClientBean);
                }
            }
        });
    }

    public void saveLoginInfoSuccess(String result) {
        loginSuccess(result);
    }

    public void saveLoginInfoFail(String result) {
        loginFail(result);
    }

    private void removeLoginInfo() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                if (null == mainActivity.getMinaClient()) {
                    loginFail("请先连接服务器后再进行操作");
                } else {
                    FromClientBean fromClientBean = new FromClientBean();
                    fromClientBean.setType(ParseMessageUtil.TYPE_REMOVELOGININFO);
                    mainActivity.getMinaClient().send(fromClientBean);
                }
            }
        });
    }

    public void removeLoginInfoSuccess(String result) {
        loginSuccess(result);
    }

}
