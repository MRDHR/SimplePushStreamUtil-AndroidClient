package com.dhr.simplepushstreamutil.util;

import com.dhr.simplepushstreamutil.activity.MainActivity;
import com.dhr.simplepushstreamutil.bean.FromServerBean;
import com.dhr.simplepushstreamutil.bean.ResolutionBean;
import com.dhr.simplepushstreamutil.entity.LiveAreaListEntity;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.List;

public class CommandUtil {
    private MainActivity mainActivity;
    private Gson gson;

    public CommandUtil(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.gson = new Gson();
    }

    public void getFormatList(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            List<ResolutionBean> resolutionBeans = gson.fromJson(fromServerBean.getResult(), new TypeToken<List<ResolutionBean>>() {
            }.getType());
            mainActivity.getFormatListSuccess(resolutionBeans);
        } else {
            mainActivity.getFormatListFail(fromServerBean.getResult());
        }
    }

    public void getM3u8Url(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.getM3u8UrlSuccess(fromServerBean.getResult());
        } else {
            mainActivity.getM3u8UrlFail(fromServerBean.getResult());
        }
    }

    public void pushStreamToLiveRoom(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.pushStreamToLiveRoomSuccess(fromServerBean.getResult());
        } else {
            mainActivity.pushStreamToLiveRoomFail(fromServerBean.getResult());
        }
    }

    public void login(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.getBilibiliAccountDialog().loginSuccess(fromServerBean.getResult());
        } else {
            mainActivity.getBilibiliAccountDialog().loginFail(fromServerBean.getResult());
        }
    }

    public void saveLoginInfo(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.getBilibiliAccountDialog().saveLoginInfoSuccess(fromServerBean.getResult());
        } else {
            mainActivity.getBilibiliAccountDialog().saveLoginInfoFail(fromServerBean.getResult());
        }
    }

    public void removeLoginInfo(FromServerBean fromServerBean) {
        mainActivity.getBilibiliAccountDialog().removeLoginInfoSuccess(fromServerBean.getResult());
    }

    public void openLiveRoom(FromServerBean fromServerBean) {
        mainActivity.openLiveRoomFail(fromServerBean.getResult());
    }

    public void getAreaList(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            List<LiveAreaListEntity> resolutionBeans = gson.fromJson(fromServerBean.getResult(), new TypeToken<List<LiveAreaListEntity>>() {
            }.getType());
            mainActivity.getAreaListSuccess(resolutionBeans);
        } else {
            mainActivity.getAreaListFail(fromServerBean.getResult());
        }
    }

    public void updateTitleAndOpenLiveRoom(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.updateTitleAndOpenLiveRoomSuccess(fromServerBean.getResult());
        } else {
            mainActivity.updateTitleAndOpenLiveRoomFail(fromServerBean.getResult());
        }
    }

    public void closeLiveRoom(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.closeLiveRoomSuccess(fromServerBean.getResult());
        } else {
            mainActivity.closeLiveRoomFail(fromServerBean.getResult());
        }
    }

    public void toMyLIveRoom(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.toMyLIveRoomSuccess(fromServerBean.getResult());
        } else {
            mainActivity.toMyLIveRoomFail(fromServerBean.getResult());
        }
    }

    public void liveRoomIsOpen(FromServerBean fromServerBean) {
        int code = fromServerBean.getCode();
        if (0 == code) {
            mainActivity.liveRoomIsOpenSuccess(fromServerBean.getResult());
        } else {
            mainActivity.liveRoomIsOpenFail(fromServerBean.getResult());
        }
    }

    public void stopPushStream(FromServerBean fromServerBean) {
        mainActivity.stopPushStreamSuccess(fromServerBean.getResult());
    }
}
