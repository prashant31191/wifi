package hillfly.wifichat.activity;

import hillfly.wifichat.BaseActivity;
import hillfly.wifichat.R;
import hillfly.wifichat.bean.Users;
import hillfly.wifichat.socket.udp.UDPMessageListener;
import hillfly.wifichat.util.DateUtils;
import hillfly.wifichat.util.ImageUtils;
import hillfly.wifichat.util.SessionUtils;
import hillfly.wifichat.util.SharePreferenceUtils;
import hillfly.wifichat.util.TextUtils;
import hillfly.wifichat.view.HandyTextView;

import java.util.Calendar;
import java.util.Date;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.squareup.picasso.Picasso;

public class SettingInfoActivity extends BaseActivity implements OnClickListener,
        OnDateChangedListener {

    private static final int REQUEST_CODE = 1;
    // ç™»é™†å¹´é¾„é™DEFAULTåˆ¶
    private static final int MAX_AGE = 80;
    private static final int MIN_AGE = 12;

    private EditText mEtNickname;

    private HandyTextView mHtvConstellation;
    private HandyTextView mHtvAge;
    private ImageView mIvAvater;
    private DatePicker mDpBirthday;
    private Calendar mCalendar;
    private Date mMinDate;
    private Date mMaxDate;
    private Date mSelectDate;

    private RadioGroup mRgGender;
    private RadioButton mRbGirl;
    private RadioButton mRbBoy;
    private Button mBtnBack;
    private Button mBtnNext;

    private int mAge;
    private int mAvatar;
    private String mGender;
    private String mBirthday;
    private String mConstellation; // æ˜Ÿåº§
    private String mNickname = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_myprofile);
        mUDPListener = UDPMessageListener.getInstance(getApplicationContext());
        initViews();
        initData();
        initEvents();
    }

    @Override
    protected void initViews() {

        mIvAvater = (ImageView) findViewById(R.id.setting_my_avater_img);
        mEtNickname = (EditText) findViewById(R.id.setting_my_nickname);
        mRgGender = (RadioGroup) findViewById(R.id.setting_baseinfo_rg_gender);
        mHtvConstellation = (HandyTextView) findViewById(R.id.setting_birthday_htv_constellation);
        mHtvAge = (HandyTextView) findViewById(R.id.setting_birthday_htv_age);
        mDpBirthday = (DatePicker) findViewById(R.id.setting_birthday_dp_birthday);

        mRbBoy = (RadioButton) findViewById(R.id.setting_baseinfo_rb_male);
        mRbGirl = (RadioButton) findViewById(R.id.setting_baseinfo_rb_female);

        mBtnBack = (Button) findViewById(R.id.setting_btn_back);
        mBtnNext = (Button) findViewById(R.id.setting_btn_next);

    }

    @Override
    protected void initEvents() {
        setTitle(getString(R.string.setting_text_profile));

        mBtnBack.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mIvAvater.setOnClickListener(this);
    }

    private void initData() {
        mAge = SessionUtils.getAge();
        mAvatar = SessionUtils.getAvatar();
        mGender = SessionUtils.getGender();
        mConstellation = SessionUtils.getConstellation(); // æ˜Ÿåº§
        mBirthday = SessionUtils.getBirthday();
        mSelectDate = DateUtils.getDate(mBirthday);

        if (mGender.equals("å¥³")) {
            mRbGirl.setChecked(true);
        }
        else {
            mRbBoy.setChecked(true);
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
        mDpBirthday.init(mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),mCalendar.get(Calendar.DAY_OF_MONTH), this);
        
        
        mHtvAge.setText(mAge + "");
        Picasso.with(mContext).load(ImageUtils.getImageID(Users.AVATAR + mAvatar)).into(mIvAvater);
        mEtNickname.setText(SessionUtils.getNickname());
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

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
        switch (v.getId()) {
            case R.id.setting_btn_back:
                finish();
                break;

            case R.id.setting_btn_next:
                doNext();
                break;
            case R.id.setting_my_avater_img:
                Intent intent = new Intent(this, ChooseAvatarActivity.class);
                startActivityForResult(intent, REQUEST_CODE);
                break;
        }

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
            case R.id.setting_baseinfo_rb_female:
                mGender = "å¥³";
                break;
            case R.id.setting_baseinfo_rb_male:
                mGender = "ç”·";
                break;
            default:
                showShortToast(R.string.login_toast_sex);
                return false;
        }

        mNickname = mEtNickname.getText().toString().trim(); // èŽ·åDEFAULT–æ˜µç§°
        mConstellation = mHtvConstellation.getText().toString().trim(); // èŽ·åDEFAULT–æ˜Ÿåº§
        mAge = Integer.parseInt(mHtvAge.getText().toString().trim()); // èŽ·åDEFAULT–å¹´é¾„
        return true;
    }

    private void doNext() {
        if ((!isValidated())) {
        	System.out.println("========!isValidated() - return===");
            return;
        }
        setAsyncTask();

    }

    private void setAsyncTask() {
        putAsyncTask(new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                showLoadingDialog(getString(R.string.login_dialog_saveInfo));
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                try {

                    // è®¾ç½®ç”¨æˆ·Sessionä¿¡æDEFAULT¯
                	System.out.println("==set details---setAsyncTask()=");
                	
                	
                	if(mBirthday !=null)
                	{
                		System.out.println("=======mBirthday==="+mBirthday);
                	}
                    SessionUtils.setNickname(mNickname);
                    SessionUtils.setBirthday(mBirthday);
                    SessionUtils.setAge(mAge);
                    SessionUtils.setGender(mGender);
                    SessionUtils.setAvatar(mAvatar);
                    SessionUtils.setConstellation(mConstellation);
                    SessionUtils.updateUserInfo();

                    // åœ¨SDåDEFAULT¡ä¸­å­˜å‚¨ç™»é™†ä¿¡æDEFAULT¯
                    SharePreferenceUtils mSPUtils = new SharePreferenceUtils();
                    SharedPreferences.Editor mEditor = mSPUtils.getEditor();
                    mEditor.putString(Users.NICKNAME, mNickname).putString(Users.GENDER, mGender)
                            .putInt(Users.AVATAR, mAvatar).putInt(Users.AGE, mAge)
                            .putString(Users.BIRTHDAY, mBirthday)
                            .putString(Users.CONSTELLATION, mConstellation);
                    mEditor.commit();
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
                    mUDPListener.notifyOnline();
                    finish();
                }
                else {
                    showShortToast("æ“DEFAULTä½œå¤±è´¥,è¯·å°DEFAULTè¯•é‡DEFAULTåDEFAULT¯ç¨‹åºDEFAULTã€‚");
                }
            }
        });
    }

    @Override
    public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mBirthday = String.valueOf(year) + String.format("%02d", monthOfYear + 1)
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                int result = data.getExtras().getInt("result");

                mAvatar = result + 1;
                Picasso.with(mContext).load(ImageUtils.getImageID(Users.AVATAR + mAvatar))
                        .into(mIvAvater);
            }
        }

    }

}
