package hillfly.wifichat.dialog;

import hillfly.wifichat.BaseDialog;
import hillfly.wifichat.R;
import hillfly.wifichat.activity.wifiap.WifiApConst;
import hillfly.wifichat.util.WifiUtils;
import hillfly.wifichat.util.WifiUtils.WifiCipherType;
import android.content.Context;
import android.content.DialogInterface;
import android.net.wifi.ScanResult;
import android.os.Handler;
import android.text.Editable;
import android.text.InputType;
import android.text.Selection;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class ConnWifiDialog extends BaseDialog {

    private EditText mEtConnectPwd;
    private CheckBox mCkShowPwd;
    private ScanResult mScanResult;
    private Handler mHandler;

    public ConnWifiDialog(Context context, Handler handler) {
        super(context);
        setDialogContentView(R.layout.include_dialog_connectwifi);
        mHandler = handler;
        initViews();
        initEvents();

    }

    private void initViews() {
        mEtConnectPwd = (EditText) findViewById(R.id.dialog_et_connectWifi);
        mCkShowPwd = (CheckBox) findViewById(R.id.dialog_cb_showpwd);
    }

    private void initEvents() {

        setButton1(mContext.getString(R.string.btn_yes), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                // TODO Auto-generated method stub
                String pwd = getWifiPwd();
                if (TextUtils.isEmpty(pwd)) {
                    return;
                }
                else {

                    ConnWifiDialog.this.setButton1Text(mContext
                            .getString(R.string.wifiap_btn_connecting));
                    ConnWifiDialog.this.setButton1Clickable(false);
                    ConnWifiDialog.this.setButton2Clickable(false);

                    WifiCipherType type = null;
                    String capString = mScanResult.capabilities;
                    if (capString.toUpperCase().contains("WPA")) {
                        type = WifiCipherType.WIFICIPHER_WPA;
                    }
                    else if (capString.toUpperCase().contains("WEP")) {
                        type = WifiCipherType.WIFICIPHER_WEP;
                    }
                    else {
                        type = WifiCipherType.WIFICIPHER_NOPASS;
                    }

                    // è¿žæŽ¥ç½‘ç»œ
                    boolean connFlag = WifiUtils.connectWifi(mScanResult.SSID, pwd, type);
                    ConnWifiDialog.this.setButton1Text(mContext.getString(R.string.btn_yes));
                    ConnWifiDialog.this.setButton1Clickable(true);
                    ConnWifiDialog.this.setButton2Clickable(true);
                    if (connFlag) {
                        clearInput();
                        ConnWifiDialog.this.cancel();
                        System.out.println("=======Connect True=====");
                    }
                    else {
                        mHandler.sendEmptyMessage(WifiApConst.WiFiConnectError);
                        System.out.println("=======Connect False=====");
                    }
                }
                System.out.println("=======Determine=====");
            }
        });

        setButton2(mContext.getString(R.string.btn_cancel), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                clearInput();
                ConnWifiDialog.this.cancel();
                
                System.out.println("=======Cancel=====");

            }
        });

        setButton3(mContext.getString(R.string.btn_auto_pass), new OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
                //clearInput();
                //ConnWifiDialog.this.cancel();
                
                System.out.println("====btn_auto_pass===Cancel=====");
                                
                long  i=0,j=10,k=5;

        		for(i=0; i<j; i=i+1)
        		{
        			

System.out.println("--->>"+i);

mEtConnectPwd.setText(""+i);
                    ConnWifiDialog.this.setButton1Text(mContext
                            .getString(R.string.wifiap_btn_connecting));
                    ConnWifiDialog.this.setButton1Clickable(false);
                    ConnWifiDialog.this.setButton2Clickable(false);

                    WifiCipherType type = null;
                    String capString = mScanResult.capabilities;
                    if (capString.toUpperCase().contains("WPA")) {
                        type = WifiCipherType.WIFICIPHER_WPA;
                    }
                    else if (capString.toUpperCase().contains("WEP")) {
                        type = WifiCipherType.WIFICIPHER_WEP;
                    }
                    else {
                        type = WifiCipherType.WIFICIPHER_NOPASS;
                    }

                    // è¿žæŽ¥ç½‘ç»œ
                    boolean connFlag = WifiUtils.connectWifi(mScanResult.SSID, ""+i, type);
                    ConnWifiDialog.this.setButton1Text(mContext.getString(R.string.btn_yes));
                    ConnWifiDialog.this.setButton1Clickable(true);
                    ConnWifiDialog.this.setButton2Clickable(true);
                    if (connFlag) {
                        clearInput();
                        ConnWifiDialog.this.cancel();
                        System.out.println("=======Connect True=====");
                        
                      //  break;
                    }
                    else {
                        mHandler.sendEmptyMessage(WifiApConst.WiFiConnectError);
                        System.out.println("=======Connect False=====");
                    }
        		}
            }
        });
        
        mEtConnectPwd.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (TextUtils.isEmpty(s)) {
                    mCkShowPwd.setEnabled(false);
                    ConnWifiDialog.this.setButton1Clickable(false);
                }
                else {
                    mCkShowPwd.setEnabled(true);
                    ConnWifiDialog.this.setButton1Clickable(true);

                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mCkShowPwd.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // æ–‡æœ¬æ­£å¸¸æ˜¾ç¤º
                    mEtConnectPwd.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                    Editable etable = mEtConnectPwd.getText();
                    Selection.setSelection(etable, etable.length());

                }
                else {
                    // æ–‡æœ¬ä»¥å¯†ç DEFAULTå½¢å¼DEFAULTæ˜¾ç¤º
                    mEtConnectPwd.setInputType(InputType.TYPE_CLASS_TEXT
                            | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                    Editable etable = mEtConnectPwd.getText();
                    Selection.setSelection(etable, etable.length());

                }
            }
        });

    }

    public String getWifiPwd() {
        return mEtConnectPwd.getText().toString().trim();
    }

    public void setBtn1ClickListener(DialogInterface.OnClickListener listener) {
        ConnWifiDialog.this.setButton1(mContext.getString(R.string.wifiap_btn_connectwifi),
                listener);
    }

    public void setScanResult(ScanResult scanResult) {
        this.mScanResult = scanResult;
    }

    private void clearInput() {
        this.mEtConnectPwd.setText("");
        this.mCkShowPwd.setChecked(false);
    }
}
