package com.dhr.simplepushstreamutil.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import com.badoo.mobile.util.WeakHandler;
import com.dhr.simplepushstreamutil.R;
import com.dhr.simplepushstreamutil.bean.ConfigSchemeBean;
import com.dhr.simplepushstreamutil.bean.FromClientBean;
import com.dhr.simplepushstreamutil.bean.LiveRoomUrlInfoBean;
import com.dhr.simplepushstreamutil.bean.LocalDataBean;
import com.dhr.simplepushstreamutil.bean.ResolutionBean;
import com.dhr.simplepushstreamutil.bean.ServerInfoBean;
import com.dhr.simplepushstreamutil.bean.SourceUrlInfoBean;
import com.dhr.simplepushstreamutil.dialog.areasettingdialog.AreaSettingDialog;
import com.dhr.simplepushstreamutil.dialog.bilibiliacount.BilibiliAccountDialog;
import com.dhr.simplepushstreamutil.dialog.configscheme.ConfigSchemeDialog;
import com.dhr.simplepushstreamutil.dialog.log.LogDialog;
import com.dhr.simplepushstreamutil.dialog.resourceurl.ResourceUrlInfoDialog;
import com.dhr.simplepushstreamutil.dialog.resourceurl.SaveResourceUrlInfoDialog;
import com.dhr.simplepushstreamutil.dialog.serverinfo.SaveServerInfoDialog;
import com.dhr.simplepushstreamutil.dialog.serverinfo.ServerInfoDialog;
import com.dhr.simplepushstreamutil.dialog.targeturl.SaveTargetUrlInfoDialog;
import com.dhr.simplepushstreamutil.dialog.targeturl.TargetUrlInfoDialog;
import com.dhr.simplepushstreamutil.entity.LiveAreaListEntity;
import com.dhr.simplepushstreamutil.mina.MinaClient;
import com.dhr.simplepushstreamutil.params.MainParam;
import com.dhr.simplepushstreamutil.util.ParseMessageUtil;
import com.dhr.simplepushstreamutil.util.SharedPreferencesUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {
    private Toolbar mNormalToolbar;

    private EditText etServerIp;
    private EditText etServerPort;
    private EditText etUserName;
    private EditText etPassWord;
    private Button btnSaveServerInfo;
    private Button btnLoadServerInfo;
    private Button btnConnectServer;
    private Button btnDisConnect;

    private String serverIp;
    private int serverPort;
    private String userName;
    private String userPassword;

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private SharedPreferencesUtil sharedPreferencesUtil;
    private SaveServerInfoDialog saveServerInfoDialog;
    private ServerInfoDialog serverInfoDialog;
    private ConfigSchemeDialog configSchemeDialog;

    private EditText etResourceUrl;
    private Button btnSaveResourceUrl;
    private Button btnLoadResourceUrlInfo;
    private SaveResourceUrlInfoDialog saveResourceUrlInfoDialog;
    private ResourceUrlInfoDialog resourceUrlInfoDialog;

    private EditText etTargetUrl;
    private Button btnSaveTargetUrl;
    private Button btnLoadTargetUrl;
    private SaveTargetUrlInfoDialog saveTargetUrlInfoDialog;
    private TargetUrlInfoDialog targetUrlInfoDialog;

    private String resourceUrl;
    private String m3u8Url;
    private String liveRoomUrl;

    private Button btnOpenLiveRoom;
    private Button btnCloseLiveRoom;
    private Button btnToMyLiveRoom;
    private Button btnGetFormatList;
    private Button btnStartPushStream;
    private Button btnStopPushStream;

    private boolean isLocalFile = false;

    private Spinner spFormatList;
    private ArrayAdapter<String> formatListAdapter;
    private List<String> listFormat;
    private List<ResolutionBean> listResolutions;

    private LogDialog logDialog;

    private BilibiliAccountDialog bilibiliAcountDialog;
    private RadioGroup rgInputOrGet;
    private RadioButton rbInput;
    private RadioButton rbGet;
    private LinearLayout llTargetUrl;
    private LinearLayout llTargetUrlCtrl;

    private RadioButton rbBoth;
    private RadioButton rbOnlyAudio;
    private RadioButton rbOnlyImage;
    private CheckBox cbTwoInOne;

    private MinaClient minaClient;

    private AreaSettingDialog areaSettingDialog;
    private Gson gson;
    private List<LiveAreaListEntity> liveAreaListEntities;
    private LocalDataBean localDataBean;
    private String roomName;
    private String areaId;

    private WeakHandler weakHandler = new WeakHandler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            if (!isFinishing()) {
                switch (msg.what) {
                    case MainParam.connectServiceSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "连接服务器成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n连接服务器成功");
                        break;
                    case MainParam.connectServiceFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "连接服务器失败", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n连接服务器失败，请检查输入的信息");
                        break;
                    case MainParam.disconnectSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "断开连接", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n断开连接");
                        break;
                    case MainParam.startGetFormatList:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始获取分辨率列表", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("开始获取分辨率列表，请稍候...");
                        break;
                    case MainParam.getFormatListSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取分辨率列表成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n" + "获取分辨率列表成功，请选择推送分辨率，检查直播间地址是否有误，检查无误后点击开始推流按钮");
                        formatListAdapter.notifyDataSetChanged();
                        break;
                    case MainParam.getFormatListFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取分辨率列表失败", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog(msg.obj.toString());
                        formatListAdapter.notifyDataSetChanged();
                        break;
                    case MainParam.startGetM3u8Url:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始获取直播源，请稍候...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开始获取直播源，请稍候...");
                        break;
                    case MainParam.getM3u8UrlSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取直播源成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n获取直播源成功");
                        //直推m3u8或本地视频文件
                        pushStreamToLiveRoomInLinux();
                        break;
                    case MainParam.getM3u8UrlFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取直播源失败", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog(msg.obj.toString());
                        break;
                    case MainParam.startAssemblePushStreamCommand:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始组装推流参数即将开始推流，请稍候...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开始组装推流参数即将开始推流，请稍候...");
                        break;
                    case MainParam.pushStreamLog:
                        String result = msg.obj.toString();
                        logDialog.addLog(result);
                        break;
                    case MainParam.stopPushStreamSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "结束推流成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n" + msg.obj.toString());
                        break;
                    case MainParam.getAreaListSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取分区列表信息成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n获取分区列表信息成功");
                        configRoom();
                        break;
                    case MainParam.startChangeLiveRoomTitle:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "获取开启直播参数成功，开始修改房间标题...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n获取开启直播参数成功，开始修改房间标题...");
                        break;
                    case MainParam.startOpenLiveRoom:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "修改房间标题成功，正在开启直播...", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n修改房间标题成功，正在开启直播...");
                        break;
                    case MainParam.openLiveRoomSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开启直播成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开启直播成功");
                        btnCloseLiveRoom.setEnabled(true);
                        btnToMyLiveRoom.setEnabled(true);
                        break;
                    case MainParam.openLiveRoomFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开启直播失败，请稍后再试", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n开启直播失败，请稍后再试");
                        break;
                    case MainParam.getAreaListFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, msg.obj.toString().replaceAll("\n", ""), Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog(msg.obj.toString());
                        break;
                    case MainParam.loginInfoEmpty:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "登录信息为空，请在菜单栏中登录B站账号信息后重试", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n\n登录信息为空，请在菜单栏中登录B站账号信息后重试");
                        break;
                    case MainParam.closeLiveRoomSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "关闭直播间成功", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n" + msg.obj.toString());
                        btnCloseLiveRoom.setEnabled(false);
                        btnToMyLiveRoom.setEnabled(false);
                        break;
                    case MainParam.closeLiveRoomFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n" + msg.obj.toString());
                        break;
                    case MainParam.updateTitleAndOpenLiveRoomSuccess:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, msg.obj.toString().replaceAll("\n", ""), Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n" + msg.obj.toString());
                        btnCloseLiveRoom.setEnabled(true);
                        btnToMyLiveRoom.setEnabled(true);
                        break;
                    case MainParam.updateTitleAndOpenLiveRoomFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, msg.obj.toString().replaceAll("\n", ""), Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n" + msg.obj.toString());
                        break;
                    case MainParam.toMyLIveRoomSuccess:
                        Intent intent = new Intent();
                        intent.setData(Uri.parse(msg.obj.toString()));//Url 就是你要打开的网址
                        intent.setAction(Intent.ACTION_VIEW);
                        startActivity(intent); //启动浏览器
                        break;
                    case MainParam.toMyLIveRoomFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "打开我的直播间失败", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n" + msg.obj.toString());
                        break;
                    case MainParam.liveRoomIsOpenFail:
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
                        }
                        logDialog.addLog("\n" + msg.obj.toString());
                        break;
                }
            }
            return false;
        }
    });

    /**
     * 配置直播间信息
     */
    private void configRoom() {
        if (null == areaSettingDialog) {
            areaSettingDialog = new AreaSettingDialog(MainActivity.this, R.style.dialog_custom);
            areaSettingDialog.setCallBack(new AreaSettingDialog.CallBack() {
                @Override
                public void callBack(String roomName, String areaId) {
                    MainActivity.this.roomName = roomName;
                    MainActivity.this.areaId = areaId;
                    updateTitleAndOpenLiveRoom();
                }
            });
        }
        areaSettingDialog.show();
        areaSettingDialog.updateData(liveAreaListEntities);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initData();
    }

    private void initView() {
        mNormalToolbar = findViewById(R.id.mNormalToolbar);
        //设置menu
        mNormalToolbar.inflateMenu(R.menu.menu);
        //设置menu的点击事件
        mNormalToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                int menuItemId = item.getItemId();
                if (menuItemId == R.id.action_log) {
                    logDialog.show();
                } else if (menuItemId == R.id.action_config) {
                    if (null == bilibiliAcountDialog) {
                        bilibiliAcountDialog = new BilibiliAccountDialog(MainActivity.this, R.style.dialog_custom);
                    }
                    bilibiliAcountDialog.show();
                } else if (menuItemId == R.id.action_config_scheme) {
                    if (null == configSchemeDialog) {
                        configSchemeDialog = new ConfigSchemeDialog(MainActivity.this, R.style.dialog_custom);
                    }
                    configSchemeDialog.show();
                }
                return true;
            }
        });

        etServerIp = findViewById(R.id.etServerIp);
        etServerPort = findViewById(R.id.etServerPort);
        etUserName = findViewById(R.id.etUserName);
        etPassWord = findViewById(R.id.etPassWord);
        btnSaveServerInfo = findViewById(R.id.btnSaveServerInfo);
        btnLoadServerInfo = findViewById(R.id.btnLoadServerInfo);
        btnConnectServer = findViewById(R.id.btnConnectServer);
        btnDisConnect = findViewById(R.id.btnDisConnect);

        btnSaveServerInfo.setOnClickListener(onClickListener);
        btnLoadServerInfo.setOnClickListener(onClickListener);
        btnConnectServer.setOnClickListener(onClickListener);
        btnDisConnect.setOnClickListener(onClickListener);

        etResourceUrl = findViewById(R.id.etResourceUrl);
        btnSaveResourceUrl = findViewById(R.id.btnSaveResourceUrl);
        btnLoadResourceUrlInfo = findViewById(R.id.btnLoadResourceUrlInfo);
        btnSaveResourceUrl.setOnClickListener(onClickListener);
        btnLoadResourceUrlInfo.setOnClickListener(onClickListener);

        etTargetUrl = findViewById(R.id.etTargetUrl);
        btnSaveTargetUrl = findViewById(R.id.btnSaveTargetUrl);
        btnLoadTargetUrl = findViewById(R.id.btnLoadTargetUrl);
        btnSaveTargetUrl.setOnClickListener(onClickListener);
        btnLoadTargetUrl.setOnClickListener(onClickListener);

        btnOpenLiveRoom = findViewById(R.id.btnOpenLiveRoom);
        btnCloseLiveRoom = findViewById(R.id.btnCloseLiveRoom);
        btnToMyLiveRoom = findViewById(R.id.btnToMyLiveRoom);
        btnOpenLiveRoom.setOnClickListener(onClickListener);
        btnCloseLiveRoom.setOnClickListener(onClickListener);
        btnToMyLiveRoom.setOnClickListener(onClickListener);

        btnGetFormatList = findViewById(R.id.btnGetFormatList);
        btnStartPushStream = findViewById(R.id.btnStartPushStream);
        btnStopPushStream = findViewById(R.id.btnStopPushStream);
        btnGetFormatList.setOnClickListener(onClickListener);
        btnStartPushStream.setOnClickListener(onClickListener);
        btnStopPushStream.setOnClickListener(onClickListener);

        spFormatList = findViewById(R.id.spFormatList);
        listFormat = new ArrayList<>();
        listResolutions = new ArrayList<>();
        formatListAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listFormat);
        formatListAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spFormatList.setAdapter(formatListAdapter);

        logDialog = new LogDialog(this, R.style.dialog_custom);

        rgInputOrGet = findViewById(R.id.rgInputOrGet);
        rbInput = findViewById(R.id.rbInput);
        rbGet = findViewById(R.id.rbGet);
        llTargetUrl = findViewById(R.id.llTargetUrl);
        llTargetUrlCtrl = findViewById(R.id.llTargetUrlCtrl);
        rgInputOrGet.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.rbInput:
                        llTargetUrl.setVisibility(View.VISIBLE);
                        llTargetUrlCtrl.setVisibility(View.VISIBLE);
                        break;
                    case R.id.rbGet:
                        llTargetUrl.setVisibility(View.GONE);
                        llTargetUrlCtrl.setVisibility(View.GONE);
                        break;
                }
            }
        });
        rbInput.setChecked(true);

        rbBoth = findViewById(R.id.rbBoth);
        rbOnlyAudio = findViewById(R.id.rbOnlyAudio);
        rbOnlyImage = findViewById(R.id.rbOnlyImage);
        cbTwoInOne = findViewById(R.id.cbTwoInOne);
        rbBoth.setChecked(true);

        btnCloseLiveRoom.setEnabled(false);
        btnToMyLiveRoom.setEnabled(false);
    }

    private void initData() {
        sharedPreferencesUtil = new SharedPreferencesUtil(this);
        gson = new Gson();
        String localData = sharedPreferencesUtil.getSharedPreference(LocalDataBean.class.getSimpleName(), "").toString();
        if (TextUtils.isEmpty(localData)) {
            localDataBean = new LocalDataBean();
            sharedPreferencesUtil.put(LocalDataBean.class.getSimpleName(), gson.toJson(localDataBean));
        } else {
            localDataBean = gson.fromJson(localData, LocalDataBean.class);
        }
        if (null == localDataBean.getConfigSchemeBean()) {
            ConfigSchemeBean configSchemeBean = new ConfigSchemeBean();
            configSchemeBean.setSchemeType(0);
            localDataBean.setConfigSchemeBean(configSchemeBean);
            sharedPreferencesUtil.put(LocalDataBean.class.getSimpleName(), gson.toJson(localDataBean));
        }
    }

    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btnSaveServerInfo:
                    boolean b = loadServerInfo();
                    if (!b) {
                        if (null == saveServerInfoDialog) {
                            saveServerInfoDialog = new SaveServerInfoDialog(MainActivity.this, R.style.dialog_custom, new SaveServerInfoDialog.CallBack() {
                                @Override
                                public void confirm(String name) {
                                    List<ServerInfoBean> serverInfoBeans = localDataBean.getServerInfoBeans();
                                    if (null == serverInfoBeans) {
                                        serverInfoBeans = new ArrayList<>();
                                    }
                                    ServerInfoBean serverInfoBean = new ServerInfoBean();
                                    serverInfoBean.setSaveName(name);
                                    serverInfoBean.setIp(serverIp);
                                    serverInfoBean.setPort(serverPort);
                                    serverInfoBean.setUserName(userName);
                                    serverInfoBean.setUserPassword(userPassword);
                                    serverInfoBeans.add(serverInfoBean);
                                    localDataBean.setServerInfoBeans(serverInfoBeans);
                                    sharedPreferencesUtil.put(LocalDataBean.class.getSimpleName(), gson.toJson(localDataBean));
                                    Toast.makeText(getApplicationContext(), "服务器信息保存记录成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        saveServerInfoDialog.show();
                    }
                    break;
                case R.id.btnLoadServerInfo:
                    if (null == serverInfoDialog) {
                        serverInfoDialog = new ServerInfoDialog(MainActivity.this, R.style.dialog_custom, new ServerInfoDialog.CallBack() {
                            @Override
                            public void confirm(String ip, int port, String userName, String userPassword) {
                                etServerIp.setText(ip);
                                etServerPort.setText(String.valueOf(port));
                                etUserName.setText(userName);
                                etPassWord.setText(userPassword);
                            }
                        });
                    }
                    serverInfoDialog.show();
                    break;
                case R.id.btnConnectServer:
                    b = loadServerInfo();
                    if (!b) {
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "开始连接服务器", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("开始连接服务器");
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                if (null == minaClient) {
                                    minaClient = new MinaClient(MainActivity.this, serverIp);
                                }
                                try {
                                    minaClient.open();
                                    weakHandler.sendEmptyMessage(MainParam.connectServiceSuccess);
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                    weakHandler.sendEmptyMessage(MainParam.connectServiceFail);
                                }
                            }
                        });
                    }
                    break;
                case R.id.btnDisConnect:
                    if (null == minaClient) {
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
                    } else {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                minaClient.close();
                                weakHandler.sendEmptyMessage(MainParam.disconnectSuccess);
                            }
                        });
                    }
                    break;
                case R.id.btnSaveResourceUrl:
                    resourceUrl = etResourceUrl.getText().toString();
                    if (TextUtils.isEmpty(resourceUrl)) {
                        Toast.makeText(getApplicationContext(), "直播源地址不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (null == saveResourceUrlInfoDialog) {
                            saveResourceUrlInfoDialog = new SaveResourceUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new SaveResourceUrlInfoDialog.CallBack() {
                                @Override
                                public void confirm(String name) {
                                    List<SourceUrlInfoBean> sourceUrlInfoBeans = localDataBean.getSourceUrlInfoBeans();
                                    if (null == sourceUrlInfoBeans) {
                                        sourceUrlInfoBeans = new ArrayList<>();
                                    }
                                    SourceUrlInfoBean resourceUrlInfoBean = new SourceUrlInfoBean();
                                    resourceUrlInfoBean.setSaveName(name);
                                    resourceUrlInfoBean.setUrl(resourceUrl);
                                    sourceUrlInfoBeans.add(resourceUrlInfoBean);
                                    localDataBean.setSourceUrlInfoBeans(sourceUrlInfoBeans);
                                    sharedPreferencesUtil.put(LocalDataBean.class.getSimpleName(), gson.toJson(localDataBean));
                                    Toast.makeText(getApplicationContext(), "直播源信息保存记录成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        saveResourceUrlInfoDialog.show();
                    }
                    break;
                case R.id.btnLoadResourceUrlInfo:
                    if (null == resourceUrlInfoDialog) {
                        resourceUrlInfoDialog = new ResourceUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new ResourceUrlInfoDialog.CallBack() {
                            @Override
                            public void confirm(String url) {
                                etResourceUrl.setText(url);
                            }
                        });
                    }
                    resourceUrlInfoDialog.show();
                    break;
                case R.id.btnSaveTargetUrl:
                    liveRoomUrl = etTargetUrl.getText().toString();
                    if (TextUtils.isEmpty(liveRoomUrl)) {
                        Toast.makeText(getApplicationContext(), "直播间地址不能为空", Toast.LENGTH_SHORT).show();
                    } else {
                        if (null == saveTargetUrlInfoDialog) {
                            saveTargetUrlInfoDialog = new SaveTargetUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new SaveTargetUrlInfoDialog.CallBack() {
                                @Override
                                public void confirm(String name) {
                                    List<LiveRoomUrlInfoBean> liveRoomUrlInfoBeans = localDataBean.getLiveRoomUrlInfoBeans();
                                    if (null == liveRoomUrlInfoBeans) {
                                        liveRoomUrlInfoBeans = new ArrayList<>();
                                    }
                                    LiveRoomUrlInfoBean targetUrlInfoBean = new LiveRoomUrlInfoBean();
                                    targetUrlInfoBean.setSaveName(name);
                                    targetUrlInfoBean.setUrl(liveRoomUrl);
                                    liveRoomUrlInfoBeans.add(targetUrlInfoBean);
                                    localDataBean.setLiveRoomUrlInfoBeans(liveRoomUrlInfoBeans);
                                    sharedPreferencesUtil.put(LocalDataBean.class.getSimpleName(), gson.toJson(localDataBean));
                                    Toast.makeText(getApplicationContext(), "直播间信息保存记录成功", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                        saveTargetUrlInfoDialog.show();
                    }
                    break;
                case R.id.btnLoadTargetUrl:
                    if (null == targetUrlInfoDialog) {
                        targetUrlInfoDialog = new TargetUrlInfoDialog(MainActivity.this, R.style.dialog_custom, new TargetUrlInfoDialog.CallBack() {
                            @Override
                            public void confirm(String url) {
                                etTargetUrl.setText(url);
                            }
                        });
                    }
                    targetUrlInfoDialog.show();
                    break;
                case R.id.btnGetFormatList:
                    b = loadServerInfo();
                    if (!b) {
                        resourceUrl = etResourceUrl.getText().toString();
                        if (TextUtils.isEmpty(resourceUrl)) {
                            Toast.makeText(MainActivity.this, "请输入直播源地址后重试", Toast.LENGTH_SHORT).show();
                        } else {
                            String message = "";
                            ConfigSchemeBean configSchemeBean = localDataBean.getConfigSchemeBean();
                            switch (configSchemeBean.getSchemeType()) {
                                case 0:
                                    message = "该地址是否需要使用youtube-dl进行解析？\n（如填入的为m3u8地址或本地视频文件地址，请选否）";
                                    break;
                                case 1:
                                    message = "该地址是否需要使用streamlink进行解析？\n（如填入的为m3u8地址或本地视频文件地址，请选否）";
                                    break;
                            }
                            showConfirmDialog(message);
                        }
                    }
                    break;
                case R.id.btnStartPushStream:
                    if (rbGet.isChecked()) {
                        if (null == minaClient) {
                            if (!logDialog.isShowing()) {
                                Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
                            }
                            logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
                        } else {
                            executorService.execute(new Runnable() {
                                @Override
                                public void run() {
                                    FromClientBean fromClientBean = new FromClientBean();
                                    fromClientBean.setType(ParseMessageUtil.TYPE_LIVEROOMISOPEN);
                                    minaClient.send(fromClientBean);
                                }
                            });
                        }
                    } else {
                        //手动填写直播间地址
                        liveRoomUrl = etTargetUrl.getText().toString();
                        if (TextUtils.isEmpty(liveRoomUrl)) {
                            if (!logDialog.isShowing()) {
                                Toast.makeText(MainActivity.this, "直播间地址为空，请输入后重试。", Toast.LENGTH_SHORT).show();
                            }
                            logDialog.clearAndAddLog("\n直播间地址为空，请输入后重试。");
                        } else {
                            pushStream();
                        }
                    }
                    break;
                case R.id.btnStopPushStream:
                    stopPushStreamInLinux();
                    break;
                case R.id.btnOpenLiveRoom:
                    if (null == minaClient) {
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
                    } else {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                FromClientBean fromClientBean = new FromClientBean();
                                fromClientBean.setType(ParseMessageUtil.TYPE_OPENLIVEROOM);
                                minaClient.send(fromClientBean);
                            }
                        });
                    }
                    break;
                case R.id.btnCloseLiveRoom:
                    if (null == minaClient) {
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
                    } else {
                        executorService.execute(new Runnable() {
                            @Override
                            public void run() {
                                FromClientBean fromClientBean = new FromClientBean();
                                fromClientBean.setType(ParseMessageUtil.TYPE_CLOSELIVEROOM);
                                minaClient.send(fromClientBean);
                            }
                        });
                    }
                    break;
                case R.id.btnToMyLiveRoom:
                    if (null == minaClient) {
                        if (!logDialog.isShowing()) {
                            Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
                        }
                        logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
                    } else {
                        FromClientBean fromClientBean = new FromClientBean();
                        fromClientBean.setType(ParseMessageUtil.TYPE_TOMYLIVEROOM);
                        minaClient.send(fromClientBean);
                    }
                    break;
            }
        }
    };

    /**
     * 显示确认对话框
     *
     * @param content
     */
    private void showConfirmDialog(String content) {
        AlertDialog.Builder
                normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("温馨提示：");
        normalDialog.setCancelable(false);
        normalDialog.setMessage(content);
        normalDialog.setPositiveButton("是",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listFormat.clear();
                        formatListAdapter.notifyDataSetChanged();
                        isLocalFile = false;
                        getFormatListInLinux();
                    }
                });
        normalDialog.setNegativeButton("否",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        listFormat.clear();
                        formatListAdapter.notifyDataSetChanged();
                        isLocalFile = true;
                        m3u8Url = resourceUrl;
                    }
                });
        normalDialog.show();
    }

    /**
     * linux服务器环境获取分辨率列表
     */
    private void getFormatListInLinux() {
        if (null == minaClient) {
            if (!logDialog.isShowing()) {
                Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
            }
            logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    weakHandler.sendEmptyMessage(MainParam.startGetFormatList);
                    try {
                        String cmd = "";
                        switch (localDataBean.getConfigSchemeBean().getSchemeType()) {
                            case 0:
                                cmd = "youtube-dl --list-formats " + resourceUrl;
                                break;
                            case 1:
                                cmd = "streamlink " + resourceUrl;
                                break;
                        }
                        FromClientBean fromClientBean = new FromClientBean();
                        fromClientBean.setSchemeType(localDataBean.getConfigSchemeBean().getSchemeType());
                        fromClientBean.setType(ParseMessageUtil.TYPE_GETFORMATLIST);
                        fromClientBean.setCmd(cmd);
                        minaClient.send(fromClientBean);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private void getM3u8UrlInLinux() {
        if (null == minaClient) {
            if (!logDialog.isShowing()) {
                Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
            }
            logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    weakHandler.sendEmptyMessage(MainParam.startGetM3u8Url);
                    try {
                        String cmd;
                        if (0 == localDataBean.getConfigSchemeBean().getSchemeType()) {
                            String resolutionNo = listResolutions.get(spFormatList.getSelectedItemPosition()).getResolutionNo();
                            //通过youtube-dl获取m3u8地址
                            cmd = "youtube-dl -f " + resolutionNo + " -g " + resourceUrl;
                        } else {
                            String resolutionPx = listResolutions.get(spFormatList.getSelectedItemPosition()).getResolutionPx();
                            if (resolutionPx.contains("(")) {
                                resolutionPx = resolutionPx.substring(0, resolutionPx.lastIndexOf("("));
                            }
                            cmd = "streamlink --stream-url " + resourceUrl + " " + resolutionPx;
                        }
                        FromClientBean fromClientBean = new FromClientBean();
                        fromClientBean.setType(ParseMessageUtil.TYPE_GETM3U8);
                        fromClientBean.setCmd(cmd);
                        minaClient.send(fromClientBean);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }

    private void stopPushStreamInLinux() {
        if (null == minaClient) {
            if (!logDialog.isShowing()) {
                Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
            }
            logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    FromClientBean fromClientBean = new FromClientBean();
                    fromClientBean.setType(ParseMessageUtil.TYPE_STOPPUSHSTREAM);
                    minaClient.send(fromClientBean);
                }
            });
        }
    }

    private boolean loadServerInfo() {
        serverIp = etServerIp.getText().toString();
        userName = etUserName.getText().toString();
        userPassword = etPassWord.getText().toString();
        String port = etServerPort.getText().toString();
        if (null == serverIp || serverIp.isEmpty()) {
            Toast.makeText(getApplicationContext(), "服务器ip不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        } else if (TextUtils.isEmpty(port)) {
            Toast.makeText(getApplicationContext(), "端口号不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        } else if (null == userName || userName.isEmpty()) {
            Toast.makeText(getApplicationContext(), "用户名不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        } else if (null == userPassword || userPassword.isEmpty()) {
            Toast.makeText(getApplicationContext(), "密码不能为空，请输入后重试", Toast.LENGTH_SHORT).show();
            return true;
        }
        serverPort = Integer.parseInt(port);
        return false;
    }

    public void getFormatListSuccess(List<ResolutionBean> listResolutions) {
        MainActivity.this.listResolutions.clear();
        MainActivity.this.listResolutions.addAll(listResolutions);
        listFormat.clear();
        for (ResolutionBean resolution : listResolutions) {
            String result = "";
            if (null != resolution.getResolutionPx() && !resolution.getResolutionPx().isEmpty()) {
                result += resolution.getResolutionPx();
            } else {
                result += "无分辨率参数";
            }
            if (null != resolution.getFps() && !resolution.getFps().isEmpty()) {
                result += " " + resolution.getFps();
            }
            listFormat.add(result);
        }
        weakHandler.sendEmptyMessage(MainParam.getFormatListSuccess);
    }

    public void getFormatListFail(String errLog) {
        listFormat.clear();
        listResolutions.clear();
        Message msg = new Message();
        msg.obj = errLog;
        msg.what = MainParam.getFormatListFail;
        weakHandler.sendMessage(msg);
    }

    public void getM3u8UrlSuccess(String m3u8Url) {
        MainActivity.this.m3u8Url = m3u8Url;
        weakHandler.sendEmptyMessage(MainParam.getM3u8UrlSuccess);
    }

    public void getM3u8UrlFail(String errLog) {
        Message msg = new Message();
        msg.what = MainParam.getM3u8UrlFail;
        msg.obj = errLog;
        weakHandler.sendMessage(msg);
    }

    public void pushStreamToLiveRoomSuccess(String log) {
        Message msg = new Message();
        msg.what = MainParam.pushStreamLog;
        msg.obj = log;
        weakHandler.sendMessage(msg);
    }

    public void pushStreamToLiveRoomFail(String log) {
        Message msg = new Message();
        msg.what = MainParam.pushStreamLog;
        msg.obj = log;
        weakHandler.sendMessage(msg);
    }

    public BilibiliAccountDialog getBilibiliAccountDialog() {
        return bilibiliAcountDialog;
    }

    public void openLiveRoomFail(String result) {
        Message msg = new Message();
        msg.what = MainParam.openLiveRoomFail;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void getAreaListSuccess(List<LiveAreaListEntity> liveAreaListEntities) {
        this.liveAreaListEntities = liveAreaListEntities;
        weakHandler.sendEmptyMessage(MainParam.getAreaListSuccess);
    }

    public void getAreaListFail(String log) {
        Message msg = new Message();
        msg.what = MainParam.getAreaListFail;
        msg.obj = log;
        weakHandler.sendMessage(msg);
    }

    /**
     * 更新房间标题并打开直播间
     */
    private void updateTitleAndOpenLiveRoom() {
        if (null == minaClient) {
            if (!logDialog.isShowing()) {
                Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
            }
            logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    FromClientBean fromClientBean = new FromClientBean();
                    fromClientBean.setType(ParseMessageUtil.TYPE_UPDATETITLEANDOPENLIVEROOM);
                    fromClientBean.setAreaId(areaId);
                    fromClientBean.setRoomName(roomName);
                    minaClient.send(fromClientBean);
                }
            });
        }
    }

    public void updateTitleAndOpenLiveRoomSuccess(String result) {
        Message msg = new Message();
        msg.what = MainParam.updateTitleAndOpenLiveRoomSuccess;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void updateTitleAndOpenLiveRoomFail(String result) {
        Message msg = new Message();
        msg.what = MainParam.updateTitleAndOpenLiveRoomFail;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void closeLiveRoomSuccess(String result) {
        Message msg = new Message();
        msg.what = MainParam.closeLiveRoomSuccess;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void closeLiveRoomFail(String result) {
        Message msg = new Message();
        msg.what = MainParam.closeLiveRoomFail;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void toMyLIveRoomSuccess(String site) {
        Message msg = new Message();
        msg.what = MainParam.toMyLIveRoomSuccess;
        msg.obj = site;
        weakHandler.sendMessage(msg);
    }

    public void toMyLIveRoomFail(String result) {
        Message msg = new Message();
        msg.what = MainParam.toMyLIveRoomFail;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void liveRoomIsOpenSuccess(String url) {
        liveRoomUrl = url;
        pushStream();
    }

    public void liveRoomIsOpenFail(String result) {
        Message msg = new Message();
        msg.what = MainParam.liveRoomIsOpenFail;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    public void stopPushStreamSuccess(String result) {
        Message msg = new Message();
        msg.what = MainParam.stopPushStreamSuccess;
        msg.obj = result;
        weakHandler.sendMessage(msg);
    }

    /**
     * 推流
     */
    private void pushStream() {
        if (isLocalFile) {
            //直推m3u8或本地视频文件
            pushStreamToLiveRoom();
        } else {
            if (0 == localDataBean.getConfigSchemeBean().getSchemeType()) {
                startGetM3u8Url();
            } else if (1 == localDataBean.getConfigSchemeBean().getSchemeType()) {
                pushStreamToLiveRoom();
            }
        }
    }

    /**
     * 推流到直播间
     */
    private void pushStreamToLiveRoom() {
        pushStreamToLiveRoomInLinux();
    }

    /**
     * linux平台推流
     */
    private void pushStreamToLiveRoomInLinux() {
        if (null == minaClient) {
            if (!logDialog.isShowing()) {
                Toast.makeText(MainActivity.this, "请先连接服务器后再进行操作", Toast.LENGTH_SHORT).show();
            }
            logDialog.clearAndAddLog("\n请先连接服务器后再进行操作");
        } else {
            executorService.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        weakHandler.sendEmptyMessage(MainParam.startAssemblePushStreamCommand);
                        String videoParams = null;
                        String cache;
                        if (rbBoth.isChecked()) {
                            videoParams = " -c:v copy -c:a aac -strict -2 -f flv ";
                        } else if (rbOnlyAudio.isChecked()) {
                            videoParams = " -vn -c:a aac -strict -2  -f flv ";
                        } else if (rbOnlyImage.isChecked()) {
                            videoParams = " -c:v copy -an -strict -2  -f flv ";
                        }
                        if (cbTwoInOne.isChecked()) {
                            videoParams = " -ac 1 " + videoParams;
                        }
                        if (0 == localDataBean.getConfigSchemeBean().getSchemeType()) {
                            cache = "ffmpeg -thread_queue_size 1024 -i " + m3u8Url + videoParams + "\"" + liveRoomUrl + "\"";
                        } else {
                            String resolutionPx = listResolutions.get(spFormatList.getSelectedItemPosition()).getResolutionPx();
                            if (resolutionPx.contains("(")) {
                                resolutionPx = resolutionPx.substring(0, resolutionPx.lastIndexOf("("));
                            }
                            cache = "streamlink -O " + resourceUrl + " " + resolutionPx + " | ffmpeg -thread_queue_size 1024 -i pipe:0 " + videoParams + "\"" + liveRoomUrl + "\"";
                        }
                        System.out.println(cache);
                        FromClientBean fromClientBean = new FromClientBean();
                        fromClientBean.setType(ParseMessageUtil.TYPE_PUSHSTREAMTOLIVEROOM);
                        fromClientBean.setCmd(cache);
                        minaClient.send(fromClientBean);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });
        }
    }


    /**
     * 开始获取m3u8地址
     */
    private void startGetM3u8Url() {
        getM3u8UrlInLinux();
    }

    public MinaClient getMinaClient() {
        return minaClient;
    }

    public LocalDataBean getLocalDataBean() {
        return localDataBean;
    }

}
