package com.anji.appsp.sdktest.notice;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.anji.appsp.sdk.AppSpConfig;
import com.anji.appsp.sdk.IAppSpNoticeCallback;
import com.anji.appsp.sdk.AppSpStatusCode;
import com.anji.appsp.sdk.model.SpNoticeModelItem;
import com.anji.appsp.sdk.model.SpRespNoticeModel;
import com.anji.appsp.sdktest.R;

import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import static com.anji.appsp.sdktest.notice.NoticeEnum.Normal;


public class NoticeTestActivity extends AppCompatActivity implements View.OnClickListener {
    //appkey在移动服务平台创建项目时生成
    private static final String appKey = "b9abfa24ee644e1d8baa39cef165261d";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice_test);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        findViewById(R.id.notice_get_btn).setOnClickListener(this);
        findViewById(R.id.notice_dialog_btn).setOnClickListener(this);
        findViewById(R.id.notice_scroll_btn).setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onClick(View v) {
        NoticeEnum noticeType = null;
        switch (v.getId()) {
            case R.id.notice_get_btn:
                //从服务器读取
                noticeType = Normal;
                break;
            case R.id.notice_dialog_btn:
                //模拟弹窗
                noticeType = NoticeEnum.Dialog;
                break;
            case R.id.notice_scroll_btn:
                //模拟跑马灯
                noticeType = NoticeEnum.Scroll;
                break;
            default:
                break;
        }
        checkNotice(noticeType);
    }

    private void checkNotice(final NoticeEnum noticeType) {
        AppSpConfig.getInstance().init(this, appKey);
        AppSpConfig.getInstance().setNoticeCallback(new IAppSpNoticeCallback() {
            @Override
            public void notice(SpRespNoticeModel notice) {
                //因为是异步，注意当前窗口是否活跃
                if (!isActive()) {
                    return;
                }
                String errorMsg = null;
                switch (notice.getStatusCode()) {
                    case AppSpStatusCode.StatusCode_Success:
                        break;
                    case AppSpStatusCode.StatusCode_Cancel:
                        errorMsg = "用户已取消json文件下载";
                        break;
                    case AppSpStatusCode.StatusCode_Timeout:
                        errorMsg = "服务器json文件地址连接超时";
                        break;
                    case AppSpStatusCode.StatusCode_UrlFormatError:
                        errorMsg = "请求地址格式错误";
                        break;
                }
                if (errorMsg != null) {
                    Toast.makeText(NoticeTestActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                } else {
                    //弹出公告
                    handleNotices(notice.getModelItemList(), noticeType);
                }
            }
        });

    }

    private void handleNotices(List<SpNoticeModelItem> notices, NoticeEnum noticeType) {
        //其中"dialog"和"scroll"是产品和开发约定好的type，根据type决定显示的风格,这两个type是我们约定的
        //ignore
        showNotices(notices, noticeType);
    }


    private void showNotices(List<SpNoticeModelItem> modelItems, NoticeEnum noticeType) {
        switch (noticeType) {
            case Normal:
                //ignore
                for (SpNoticeModelItem modelItem : modelItems) {
                    displayNotice(modelItem);
                }
                break;
            case Dialog://如果显示样式是dialog
                for (SpNoticeModelItem modelItem : modelItems) {
                    modelItem.setTemplateType(NoticeType.Dialog);
                    displayNotice(modelItem);
                }
                break;
            case Scroll://如果显示样式是跑马灯
                ViewGroup parent = findViewById(R.id.scroll_container);
                parent.removeAllViews();
                for (SpNoticeModelItem modelItem : modelItems) {
                    modelItem.setTemplateType(NoticeType.Scroll);
                    displayNotice(modelItem);
                }
                break;
            default:
                break;
        }

    }

    private void displayNotice(SpNoticeModelItem modelItem) {
        if (NoticeType.Dialog.equals(modelItem.templateType)) {
            Bundle bundler = new Bundle();
            bundler.putSerializable("notice", modelItem);
            NoticeDialogFragment noticeDialogFragment = new NoticeDialogFragment();
            noticeDialogFragment.setArguments(bundler);
            noticeDialogFragment.show(getSupportFragmentManager(), "notice");
        } else if (NoticeType.Scroll.equals(modelItem.templateType)) {
            ViewGroup parent = findViewById(R.id.scroll_container);
            View child = LayoutInflater.from(this).inflate(R.layout.scroll_view_item, null);
            AutoTextView autoTextView = child.findViewById(R.id.scroll_textview);
            autoTextView.setScrollMode(getScrollMode());
            autoTextView.setText(modelItem.getDetails());
            autoTextView.requestTextChange();
            parent.addView(child);
        }
    }

    //生成随机的滚动速度
    private int getScrollMode() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int number = ThreadLocalRandom.current().nextInt(0, 10);
            if (number % 2 == 0) {
                return AutoTextView.SCROLL_FAST;
            } else {
                return AutoTextView.SCROLL_NORM;
            }
        }
        return AutoTextView.SCROLL_FAST;
    }


    protected boolean isActive() {
        boolean active = true;
        Activity curContext = this;
        if (curContext == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            active = !(curContext.isDestroyed() || curContext.isFinishing());
        } else {
            active = !curContext.isFinishing();
        }
        return active;
    }

}
