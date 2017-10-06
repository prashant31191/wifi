package hillfly.wifichat.activity;

import hillfly.wifichat.BaseActivity;
import hillfly.wifichat.R;
import hillfly.wifichat.adapter.SimpleListDialogAdapter;
import hillfly.wifichat.bean.Users;
import hillfly.wifichat.dialog.SimpleListDialog;
import hillfly.wifichat.dialog.SimpleListDialog.onSimpleListItemClickListener;
import hillfly.wifichat.util.DateUtils;
import hillfly.wifichat.util.ImageUtils;
import hillfly.wifichat.util.SessionUtils;
import hillfly.wifichat.util.SharePreferenceUtils;
import hillfly.wifichat.util.TextUtils;
import hillfly.wifichat.view.HandyTextView;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

/**
 * @fileName LoginActivity.java
 * @description ç”¨æˆ·ç™»é™†ç±»
 */
public class LoginActivity extends BaseActivity implements OnClickListener,
        onSimpleListItemClickListener, OnDateChangedListener {

    // ç™»é™†å¹´é¾„é™DEFAULTåˆ¶
    private static final int MAX_AGE = 80;
    private static final int MIN_AGE = 12;
    private static final String DEFAULT_DATA = "19951104";

    private LinearLayout mLlayoutMain; // é¦–æ¬¡ç™»é™†ä¸»ç•ŒéDEFAULT¢
    private HandyTextView mHtvSelectOnlineState;
    private EditText mEtNickname;

    private HandyTextView mHtvConstellation;
    private HandyTextView mHtvAge;
    private DatePicker mDpBirthday;
    private Calendar mCalendar;
    private Date mMinDate;
    private Date mMaxDate;
    private Date mSelectDate;

    private LinearLayout mLlayoutExMain; // äºŒæ¬¡ç™»é™†é¡µéDEFAULT¢
    private ImageView mImgExAvatar;
    private TextView mTvExNickmame;
    private LinearLayout mLayoutExGender; // æ€§åˆ«æ ¹å¸ƒå±€
    private ImageView mIvExGender;
    private HandyTextView mHtvExAge;
    private TextView mTvExConstellation;// æ˜Ÿåº§
    private TextView mTvExLogintime; // ä¸Šæ¬¡ç™»å½•æ—¶é—´

    private Button mBtnBack;
    private Button mBtnNext;
    private Button mBtnChangeUser;
    private RadioGroup mRgGender;
    private TelephonyManager mTelephonyManager;
    private SimpleListDialog mSimpleListDialog;

    private int mAge;
    private int mAvatar;
    private String mBirthday;
    private String mGender;
    private String mIMEI;
    private String mConstellation; // æ˜Ÿåº§
    private String mLastLogintime; // ä¸Šæ¬¡ç™»å½•æ—¶é—´
    private String mNickname = "";
    private String mOnlineStateStr = "åœ¨çº¿"; // é»˜è®¤ç™»å½•çŠ¶æ€DEFAULT
    private int mOnlineStateInt = 0; // é»˜è®¤ç™»å½•çŠ¶æ€DEFAULTç¼–åDEFAULT·
    private String[] mOnlineStateType;
    
    String TAG = "Mobile Data";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        initViews();
        initData();
        initEvents();
        
        
        
        /*System.out.println("===Mobile data is enable===="+getMobileDataState());
        if(getMobileDataState() == false)
        {
        	System.out.println("==set mobile data enable true==");
        	setMobileDataState(true);
        	
        }
        
        
        mobiledataenable(true);*/
        
    }
    
    
    
    
    public void setMobileDataState(boolean mobileDataEnabled)
    {
        try
        {
        	System.out.println("==setMobileDataState=="+mobileDataEnabled);
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Method setMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("setDataEnabled", boolean.class);

            if (null != setMobileDataEnabledMethod)
            {
                setMobileDataEnabledMethod.invoke(telephonyService, mobileDataEnabled);
            }
            System.out.println("==setMobileDataState=="+mobileDataEnabled);
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Error setting mobile data state", ex);
        }
    }

    public boolean getMobileDataState()
    {
        try
        {
            TelephonyManager telephonyService = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

            Method getMobileDataEnabledMethod = telephonyService.getClass().getDeclaredMethod("getDataEnabled");

            if (null != getMobileDataEnabledMethod)
            {
                boolean mobileDataEnabled = (Boolean) getMobileDataEnabledMethod.invoke(telephonyService);

                return mobileDataEnabled;
            }
        }
        catch (Exception ex)
        {
            Log.e(TAG, "Error getting mobile data state", ex);
        }

        return false;
    }
    

    
    public void mobiledataenable(boolean enabled) {

    	try { 
    			System.out.println("====mobiledataenable=="+enabled);
    	      

    		    //final ConnectivityManager conman = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    			final ConnectivityManager conman = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
    		    final Class conmanClass = Class.forName(conman.getClass().getName());
    		    final Field iConnectivityManagerField = conmanClass.getDeclaredField("mService");
    		    iConnectivityManagerField.setAccessible(true);
    		    final Object iConnectivityManager = iConnectivityManagerField.get(conman);
    		    final Class iConnectivityManagerClass = Class.forName(iConnectivityManager.getClass().getName());
    		    final Method setMobileDataEnabledMethod = iConnectivityManagerClass.getDeclaredMethod("setMobileDataEnabled", Boolean.TYPE);
    		    setMobileDataEnabledMethod.setAccessible(true);

    		    setMobileDataEnabledMethod.invoke(iConnectivityManager, enabled);
    		    
    	        System.out.println("====mobiledataenable=222="+enabled);
    	    }
    	    catch (Exception e)
    	    {
    	    	System.out.println("====mobiledataenable=Some thing went to wrong==\n==>>"+e.toString());
    	        e.printStackTrace();
    	    }     
    	}
    
    @Override
    protected void initViews() {

        mEtNickname = (EditText) findViewById(R.id.login_et_nickname);
        mHtvSelectOnlineState = (HandyTextView) findViewById(R.id.login_htv_onlinestate);
        mRgGender = (RadioGroup) findViewById(R.id.login_baseinfo_rg_gender);
        mHtvConstellation = (HandyTextView) findViewById(R.id.login_birthday_htv_constellation);
        mHtvAge = (HandyTextView) findViewById(R.id.login_birthday_htv_age);
        mDpBirthday = (DatePicker) findViewById(R.id.login_birthday_dp_birthday);

        mBtnBack = (Button) findViewById(R.id.login_btn_back);
        mBtnNext = (Button) findViewById(R.id.login_btn_next);
        mBtnChangeUser = (Button) findViewById(R.id.login_btn_changeUser);

        SharePreferenceUtils sp = new SharePreferenceUtils();
        mNickname = sp.getNickname();

        // è‹¥mNicknameæœ‰å†…å®¹ï¼Œåˆ™è¯»åDEFAULT–æœ¬åœ°å­˜å‚¨çš„ç”¨æˆ·ä¿¡æDEFAULT¯
        if (mNickname.length() != 0) {
            mTvExNickmame = (TextView) findViewById(R.id.login_tv_existName);
            mImgExAvatar = (ImageView) findViewById(R.id.login_img_existImg);
            mLayoutExGender = (LinearLayout) findViewById(R.id.login_layout_gender);
            mIvExGender = (ImageView) findViewById(R.id.login_iv_gender);
            mHtvExAge = (HandyTextView) findViewById(R.id.login_htv_age);
            mTvExConstellation = (TextView) findViewById(R.id.login_tv_constellation);
            mTvExLogintime = (TextView) findViewById(R.id.login_tv_lastlogintime);
            mLlayoutExMain = (LinearLayout) findViewById(R.id.login_linearlayout_existmain);
            mLlayoutMain = (LinearLayout) findViewById(R.id.login_linearlayout_main);
            mLlayoutMain.setVisibility(View.GONE);
            mLlayoutExMain.setVisibility(View.VISIBLE);

            mAvatar = sp.getAvatarId();
            mBirthday = sp.getBirthday();
            mOnlineStateInt = sp.getOnlineStateId();
            mGender = sp.getGender();
            mAge = sp.getAge();
            mConstellation = sp.getConstellation();
            mLastLogintime = sp.getLogintime();
            
            Picasso.with(mContext).load(ImageUtils.getImageID(Users.AVATAR + mAvatar))
                    .into(mImgExAvatar);
            mTvExNickmame.setText(mNickname);
            mTvExConstellation.setText(mConstellation);
            mHtvExAge.setText(mAge + "");
            mTvExLogintime.setText(DateUtils.getBetweentime(mLastLogintime));
            if ("å¥³".equals(mGender)) {
                mIvExGender.setBackgroundResource(R.drawable.ic_user_famale);
                mLayoutExGender.setBackgroundResource(R.drawable.bg_gender_famal);
            }
            else {
                mIvExGender.setBackgroundResource(R.drawable.ic_user_male);
                mLayoutExGender.setBackgroundResource(R.drawable.bg_gender_male);
            }
        }
    }

    @Override
    protected void initEvents() {
        mHtvSelectOnlineState.setOnClickListener(this);
        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnChangeUser.setOnClickListener(this);
    }

    private void initData() {
        if (android.text.TextUtils.isEmpty(mBirthday)) {
            mSelectDate = DateUtils.getDate(DEFAULT_DATA);
            mBirthday = DEFAULT_DATA;
        }
        else {
            mSelectDate = DateUtils.getDate(mBirthday);
        }

        Calendar mMinCalendar = Calendar.getInstance();
        Calendar mMaxCalendar = Calendar.getInstance();

        mMinCalendar.set(Calendar.YEAR, mMinCalendar.get(Calendar.YEAR) - MIN_AGE);
        mMinDate = mMinCalendar.getTime();
        mMaxCalendar.set(Calendar.YEAR, mMaxCalendar.get(Calendar.YEAR) - MAX_AGE);
        mMaxDate = mMaxCalendar.getTime();

        mCalendar = Calendar.getInstance();
        mCalendar.setTime(mSelectDate);
        flushBirthday(mCalendar);
        mDpBirthday.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                mCalendar.get(Calendar.DAY_OF_MONTH), this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_htv_onlinestate:
                mOnlineStateType = getResources().getStringArray(R.array.onlinestate_type);
                mSimpleListDialog = new SimpleListDialog(LoginActivity.this);
                mSimpleListDialog.setTitle("é€‰æ‹©åœ¨çº¿çŠ¶æ€DEFAULT");
                mSimpleListDialog.setTitleLineVisibility(View.GONE);
                mSimpleListDialog.setAdapter(new SimpleListDialogAdapter(LoginActivity.this,
                        mOnlineStateType));
                mSimpleListDialog.setOnSimpleListItemClickListener(LoginActivity.this);
                mSimpleListDialog.show();
                break;

            // æ›´æDEFAULT¢ç”¨æˆ·,æ¸…ç©ºæ•°æDEFAULT®
            case R.id.login_btn_changeUser:
                mNickname = "";
                mAge = -1;
                mGender = null;
                mIMEI = null;
                mOnlineStateStr = "åœ¨çº¿"; // é»˜è®¤ç™»å½•çŠ¶æ€DEFAULT
                mAvatar = 0;
                mConstellation = null;
                mOnlineStateInt = 0; // é»˜è®¤ç™»å½•çŠ¶æ€DEFAULTç¼–åDEFAULT·
                SessionUtils.clearSession(); // æ¸…ç©ºSessionæ•°æDEFAULT®
                mLlayoutMain.setVisibility(View.VISIBLE);
                mLlayoutExMain.setVisibility(View.GONE);
                break;

            case R.id.login_btn_back:
                finish();
                break;

            case R.id.login_btn_next:
                doLoginNext();
                break;
        }
    }

    @Override
    public void onItemClick(int position) {
        mOnlineStateStr = mOnlineStateType[position];
        mOnlineStateInt = position; // èŽ·åDEFAULT–åœ¨çº¿çŠ¶æ€DEFAULTç¼–åDEFAULT·
        mHtvSelectOnlineState.requestFocus();
        mHtvSelectOnlineState.setText(mOnlineStateStr);
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mBirthday = String.valueOf(year) + String.format("%02d", monthOfYear+1)
                + String.format("%02d", dayOfMonth);
        System.out.println("====onDateChanged=mBirthday==="+mBirthday);
        mCalendar = Calendar.getInstance();
        mCalendar.set(year, monthOfYear, dayOfMonth);
        if (mCalendar.getTime().after(mMinDate) || mCalendar.getTime().before(mMaxDate)) {
            mCalendar.setTime(mSelectDate);
            mDpBirthday.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                    mCalendar.get(Calendar.DAY_OF_MONTH), this);
        }
        else {
            flushBirthday(mCalendar);
        }
    }

    private void flushBirthday(Calendar calendar) {
        String constellation = TextUtils.getConstellation(calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        mSelectDate = calendar.getTime();
        mHtvConstellation.setText(constellation);
        int age = TextUtils.getAge(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH));
        mHtvAge.setText(age + "");
    }

    /**
     * ç™»å½•èµ„æ–™å®Œæ•´æ€§éªŒè¯DEFAULTï¼Œä¸DEFAULTå®Œæ•´åˆ™æ— æ³•ç™»é™†ï¼Œå®Œæ•´åˆ™è®°å½•è¾“å…¥çš„ä¿¡æDEFAULT¯ã€‚
     * 
     * @return boolean è¿”å›žæ˜¯åDEFAULT¦ä¸ºå®Œæ•´ï¼Œ å®Œæ•´(true),ä¸DEFAULTå®Œæ•´(false)
     */
    private boolean isValidated() {
        mNickname = "";
        mGender = null;
        if (TextUtils.isNull(mEtNickname)) {
            showShortToast(R.string.login_toast_nickname);
            mEtNickname.requestFocus();
            return false;
        }

        switch (mRgGender.getCheckedRadioButtonId()) {
            case R.id.login_baseinfo_rb_female:
                mGender = "å¥³";
                break;
            case R.id.login_baseinfo_rb_male:
                mGender = "ç”·";
                break;
            default:
                showShortToast(R.string.login_toast_sex);
                return false;
        }

        mNickname = mEtNickname.getText().toString().trim(); // èŽ·åDEFAULT–æ˜µç§°
        mAvatar = (int) (Math.random() * 12 + 1); // èŽ·åDEFAULT–å¤´åƒDEFAULTç¼–åDEFAULT·
        mConstellation = mHtvConstellation.getText().toString().trim(); // èŽ·åDEFAULT–æ˜Ÿåº§
        mAge = Integer.parseInt(mHtvAge.getText().toString().trim()); // èŽ·åDEFAULT–å¹´é¾„
        return true;
    }

    /**
     * æ‰§è¡Œä¸‹ä¸€æ­¥è·³è½¬
     * <p>
     * åDEFAULTŒæ—¶èŽ·åDEFAULT–å®¢æˆ·ç«¯çš„IMIEä¿¡æDEFAULT¯
     */
    private void doLoginNext() {
        if (mNickname.length() == 0) {
            if ((!isValidated())) {
                return;
            }
        }
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog(getString(R.string.login_dialog_saveInfo));
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    mIMEI = mTelephonyManager.getDeviceId(); // èŽ·åDEFAULT–IMEI

                    // è®¾ç½®ç”¨æˆ·Sessionä¿¡æDEFAULT¯
                    SessionUtils.setIMEI(mIMEI);
                    SessionUtils.setNickname(mNickname);
                    SessionUtils.setAge(mAge);
                    SessionUtils.setBirthday(mBirthday);
                    SessionUtils.setGender(mGender);
                    SessionUtils.setAvatar(mAvatar);
                    SessionUtils.setOnlineStateInt(mOnlineStateInt);
                    SessionUtils.setConstellation(mConstellation);
                    return true;
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean result) {
                super.onPostExecute(result);
                dismissLoadingDialog();
                if (result) {
                    startActivity(WifiapActivity.class);
                    finish();
                }
                else {
                    showShortToast(R.string.login_toast_loginfailue);
                }
            }
        });
    }
}
