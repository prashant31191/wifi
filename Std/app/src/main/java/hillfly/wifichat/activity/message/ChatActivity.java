package hillfly.wifichat.activity.message;

import hillfly.wifichat.ActivitiesManager;
import hillfly.wifichat.BaseApplication;
import hillfly.wifichat.R;
import hillfly.wifichat.activity.ImageBrowserActivity;
import hillfly.wifichat.activity.OtherProfileActivity;
import hillfly.wifichat.adapter.ChatAdapter;
import hillfly.wifichat.bean.Message;
import hillfly.wifichat.bean.Message.CONTENT_TYPE;
import hillfly.wifichat.bean.Users;
import hillfly.wifichat.socket.tcp.TcpClient;
import hillfly.wifichat.socket.udp.IPMSGConst;
import hillfly.wifichat.sql.SqlDBOperate;
import hillfly.wifichat.util.AudioRecorderUtils;
import hillfly.wifichat.util.FileUtils;
import hillfly.wifichat.util.ImageUtils;
import hillfly.wifichat.util.LogUtils;
import hillfly.wifichat.util.SessionUtils;
import hillfly.wifichat.view.EmoteInputView;
import hillfly.wifichat.view.EmoticonsEditText;
import hillfly.wifichat.view.MultiListView;
import hillfly.wifichat.view.ScrollLayout;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.app.ActionBar;
import android.content.ActivityNotFoundException;
import android.content.ContentUris;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

@SuppressLint("NewApi")
public class ChatActivity extends BaseMessageActivity implements OnItemClickListener {

	private static final String TAG = "SZU_ChatActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ActivitiesManager.initChatActivity(this);
		try {
			init();	
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			Toast.makeText(ChatActivity.this, "some error = "+e.toString(), 200).show();
		}

	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	// ç›‘åDEFAULT¬è¿”å›žé”®
	@Override
	public void onBackPressed() {
		if (mLayoutMessagePlusBar.isShown()) {
			hidePlusBar();
		}
		else if (mInputView.isShown()) {
			mIbTextDitorKeyBoard.setVisibility(View.GONE);
			mIbTextDitorEmote.setVisibility(View.VISIBLE);
			mInputView.setVisibility(View.GONE);
		}
		else if (getWindow().getAttributes().softInputMode == WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE) {
			mIbTextDitorKeyBoard.setVisibility(View.VISIBLE);
			mIbTextDitorEmote.setVisibility(View.GONE);
			hideKeyBoard();
		}
		else if (mLayoutScroll.getCurScreen() == 1) {
			mLayoutScroll.snapToScreen(0);
		}
		else {
			finish();
		}
	}

	@Override
	public void finish() {
		ActivitiesManager.removeChatActivity();

		if (null != mDBOperate) {// å…³é—­æ•°æDEFAULT®åº“è¿žæŽ¥
			mDBOperate.close();
			mDBOperate = null;
		}
		mRecordThread = null;
		super.finish();
	}

	@Override
	protected void initViews() {
		mClvList = (MultiListView) findViewById(R.id.chat_clv_list);
		mLayoutScroll = (ScrollLayout) findViewById(R.id.chat_slayout_scroll);
		mLayoutRounds = (LinearLayout) findViewById(R.id.chat_layout_rounds);
		mInputView = (EmoteInputView) findViewById(R.id.chat_eiv_inputview);

		mIbTextDitorPlus = (ImageButton) findViewById(R.id.chat_textditor_ib_plus);
		mIbTextDitorKeyBoard = (ImageButton) findViewById(R.id.chat_textditor_ib_keyboard);
		mIbTextDitorEmote = (ImageButton) findViewById(R.id.chat_textditor_ib_emote);
		mIvTextDitorAudio = (ImageView) findViewById(R.id.chat_textditor_iv_audio);
		mBtnTextDitorSend = (Button) findViewById(R.id.chat_textditor_btn_send);
		mEetTextDitorEditer = (EmoticonsEditText) findViewById(R.id.chat_textditor_eet_editer);

		mIbAudioDitorPlus = (ImageButton) findViewById(R.id.chat_audioditor_ib_plus);
		mIbAudioDitorKeyBoard = (ImageButton) findViewById(R.id.chat_audioditor_ib_keyboard);
		mIvAudioDitorAudioBtn = (ImageView) findViewById(R.id.chat_audioditor_iv_audiobtn);

		mLayoutFullScreenMask = (LinearLayout) findViewById(R.id.fullscreen_mask);
		mLayoutMessagePlusBar = (LinearLayout) findViewById(R.id.message_plus_layout_bar);
		mLayoutMessagePlusPicture = (LinearLayout) findViewById(R.id.message_plus_layout_picture);
		mLayoutMessagePlusCamera = (LinearLayout) findViewById(R.id.message_plus_layout_camera);
		mLayoutMessagePlusFile = (LinearLayout) findViewById(R.id.message_plus_layout_file);
	}

	@Override
	protected void initEvents() {
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);

		mClvList.setStackFromBottom(true);
		mClvList.setFastScrollEnabled(true);

		mLayoutScroll.setOnScrollToScreen(this);
		mIbTextDitorPlus.setOnClickListener(this);
		mIbTextDitorEmote.setOnClickListener(this);
		mIbTextDitorKeyBoard.setOnClickListener(this);
		mBtnTextDitorSend.setOnClickListener(this);
		mIvTextDitorAudio.setOnClickListener(this);
		mEetTextDitorEditer.addTextChangedListener(this);
		mEetTextDitorEditer.setOnTouchListener(this);
		mIbAudioDitorPlus.setOnClickListener(this);
		mIbAudioDitorKeyBoard.setOnClickListener(this);
		mIvAudioDitorAudioBtn.setOnTouchListener(this);
		mIvAudioDitorAudioBtn.setOnLongClickListener(this);
		mLayoutFullScreenMask.setOnTouchListener(this);
		mLayoutMessagePlusPicture.setOnClickListener(this);
		mLayoutMessagePlusCamera.setOnClickListener(this);
		mLayoutMessagePlusFile.setOnClickListener(this);

	}

	private void init() {
		mID = SessionUtils.getLocalUserID();
		mNickName = SessionUtils.getNickname();
		mIMEI = SessionUtils.getIMEI();
		mChatUser = getIntent().getParcelableExtra(Users.ENTITY_PEOPLE);
		mDBOperate = new SqlDBOperate(this);
		mSenderID = mDBOperate.getIDByIMEI(mChatUser.getIMEI());

		mMessagesList = new ArrayList<Message>();
		mMessagesList = mDBOperate.getScrollMessageOfChattingInfo(0, 5, mSenderID, mID);

		mInputView.setEditText(mEetTextDitorEditer);
		setTitle(mChatUser.getNickname());
		initRounds();
		initfolder();

		mAdapter = new ChatAdapter(ChatActivity.this, mMessagesList);
		mAdapter.setListView(mClvList);
		mAdapter.setChatUser(mChatUser);
		mClvList.setAdapter(mAdapter);        
		mClvList.setOnItemClickListener(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_chat, menu);
		return true;
	}

	// actionBarçš„ç›‘åDEFAULT¬
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_userinfo:
			Intent intent = new Intent(ChatActivity.this, OtherProfileActivity.class);
			intent.putExtra(Users.ENTITY_PEOPLE, mChatUser);
			startActivity(intent);
			break;
		case android.R.id.home:
			finish();
			break;

		default:
			break;
		}
		return true;
	}

	@Override
	public void doAction(int whichScreen) {
		switch (whichScreen) {
		case 0:
			((ImageView) mLayoutRounds.getChildAt(0)).setImageBitmap(mRoundsSelected);
			((ImageView) mLayoutRounds.getChildAt(1)).setImageBitmap(mRoundsNormal);
			break;

		case 1:
			((ImageView) mLayoutRounds.getChildAt(1)).setImageBitmap(mRoundsSelected);
			((ImageView) mLayoutRounds.getChildAt(0)).setImageBitmap(mRoundsNormal);
			mIbTextDitorKeyBoard.setVisibility(View.GONE);
			mIbTextDitorEmote.setVisibility(View.VISIBLE);
			if (mInputView.isShown()) {
				mInputView.setVisibility(View.GONE);
			}
			hideKeyBoard();
			break;
		}
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {

		case MotionEvent.ACTION_DOWN:
			if (recordState == RECORD_OFF) {
				switch (v.getId()) {
				case R.id.chat_textditor_eet_editer:
					mIbTextDitorKeyBoard.setVisibility(View.GONE);
					mIbTextDitorEmote.setVisibility(View.VISIBLE);
					showKeyBoard();
					break;

				case R.id.fullscreen_mask:
					hidePlusBar();
					break;

				case R.id.chat_audioditor_iv_audiobtn:
					downY = event.getY();
					break;
				}
			}
			break;

		case MotionEvent.ACTION_MOVE:
			if (recordState == RECORD_ON) {
				float moveY = event.getY();
				if (moveY - downY < -50) {
					isMove = true;
					showVoiceDialog(1);
				}
				else if (moveY - downY < -20) {
					isMove = false;
					showVoiceDialog(0);
				}
			}
			break;

		case MotionEvent.ACTION_UP:
			if (recordState == RECORD_ON) {

				stopRecord();

				if (mRecordDialog.isShowing()) {
					mRecordDialog.dismiss();
				}

				if (!isMove) {
					if (recodeTime < MIN_RECORD_TIME) {
						showWarnToast(R.string.chat_toast_record_shorttime);
					}
					else {
						mVoicePath = mAudioRecorder.getVoicePath();
						sendMessage(mVoicePath, CONTENT_TYPE.VOICE);
						refreshAdapter();
					}
				}

				isMove = false;
			}

			if (mAudioRecorder != null)
				mAudioRecorder = null;
			mLayoutScroll.requestDisallowInterceptTouchEvent(false); // æDEFAULT¢å¤DEFAULTç›‘æµ‹æ»šåŠ¨äº‹ä»¶
			break;

		}

		return false;
	}

	@Override
	public boolean onLongClick(View v) {
		mLayoutScroll.requestDisallowInterceptTouchEvent(true); // å±DEFAULTè”½æ»šåŠ¨äº‹ä»¶
		startRecord();
		return false;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.chat_textditor_ib_plus:
			if (!mLayoutMessagePlusBar.isShown()) {
				showPlusBar();
			}
			break;

		case R.id.chat_textditor_ib_emote:
			mIbTextDitorKeyBoard.setVisibility(View.VISIBLE);
			mIbTextDitorEmote.setVisibility(View.GONE);
			mEetTextDitorEditer.requestFocus();
			if (mInputView.isShown()) {
				hideKeyBoard();
			}
			else {
				hideKeyBoard();
				mInputView.setVisibility(View.VISIBLE);
			}
			break;

		case R.id.chat_textditor_ib_keyboard:
			mIbTextDitorKeyBoard.setVisibility(View.GONE);
			mIbTextDitorEmote.setVisibility(View.VISIBLE);
			showKeyBoard();
			break;

		case R.id.chat_textditor_btn_send:
			// 	System.out.println("===no trim========="+ mEetTextDitorEditer.getText().toString());
			String content = mEetTextDitorEditer.getText().toString().trim();
			//   System.out.println("==with trim=="+content);


			if (!TextUtils.isEmpty(content)) {
				mEetTextDitorEditer.setText(null);
				sendMessage(content, CONTENT_TYPE.TEXT);
				refreshAdapter();
			}

			/* try {
                	System.out.println("========lines======"+mEetTextDitorEditer.getLineCount());

                	String[] inputSplitNewLine =content.split("\\n");
                    System.out.println("\n After split:\n");
                    for(int i=0;i<inputSplitNewLine.length;i++){
                        System.out.println(i+"---->>"+inputSplitNewLine[i]);

                    }

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}*/
			break;

		case R.id.chat_textditor_iv_audio:
			mLayoutScroll.snapToScreen(1);
			break;

		case R.id.chat_audioditor_ib_plus:
			if (!mLayoutMessagePlusBar.isShown()) {
				showPlusBar();
			}
			break;

		case R.id.chat_audioditor_ib_keyboard:
			mLayoutScroll.snapToScreen(0);
			break;

		case R.id.message_plus_layout_picture:
			ImageUtils.selectPhoto(ChatActivity.this);
			hidePlusBar();
			break;

		case R.id.message_plus_layout_camera:
			mCameraImagePath = ImageUtils.takePicture(ChatActivity.this);
			hidePlusBar();
			break;

		case R.id.message_plus_layout_file:
			showFileChooser();
			hidePlusBar();
			break;
		}
	}

	@Override
	public void afterTextChanged(Editable s) {

	}

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {

	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (TextUtils.isEmpty(s)) {
			mIvTextDitorAudio.setVisibility(View.VISIBLE);
			mBtnTextDitorSend.setVisibility(View.GONE);
		}
		else {
			mIvTextDitorAudio.setVisibility(View.GONE);
			mBtnTextDitorSend.setVisibility(View.VISIBLE);
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case ImageUtils.INTENT_REQUEST_CODE_ALBUM:
			if (data == null) {
				return;
			}
			if (resultCode == RESULT_OK) {
				if (data.getData() == null) {
					return;
				}
				if (!FileUtils.isSdcardExist()) {
					showShortToast(R.string.toast_sdcard_unavailable);
					return;
				}
				Uri uri = data.getData();
				String[] proj = { MediaStore.Images.Media.DATA };
				Cursor cursor = managedQuery(uri, proj, null, null, null);
				if (cursor != null) {
					int column_index = cursor
							.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
					if (cursor.getCount() > 0 && cursor.moveToFirst()) {

						String path = cursor.getString(column_index);

						mCameraImagePath = path;
						Bitmap bitmap = ImageUtils.getBitmapFromPath(path);
						if (ImageUtils.bitmapIsLarge(bitmap)) {
							ImageUtils.cropPhoto(this, this, path);
						}
						else {
							if (path != null) {
								ImageUtils.createThumbnail(this, path, THUMBNAIL_PATH
										+ File.separator);
								sendMessage(path, CONTENT_TYPE.IMAGE);
								refreshAdapter();
							}
						}
					}
				}
			}
			break;

		case ImageUtils.INTENT_REQUEST_CODE_CAMERA:
			if (resultCode == RESULT_OK) {
				if (mCameraImagePath != null) {
					mCameraImagePath = ImageUtils.savePhotoToSDCard(
							ImageUtils.CompressionPhoto(mScreenWidth, mCameraImagePath, 2),
							BaseApplication.CAMERA_IMAGE_PATH, null);
					ImageUtils.fliterPhoto(this, this, mCameraImagePath);
				}
			}
			break;

		case ImageUtils.INTENT_REQUEST_CODE_CROP:
			if (resultCode == RESULT_OK) {
				String path = data.getStringExtra("path");
				mCameraImagePath = path;
				if (path != null) {
					ImageUtils.createThumbnail(this, path, THUMBNAIL_PATH + File.separator);
					sendMessage(path, CONTENT_TYPE.IMAGE);
					refreshAdapter();
				}
			}
			break;

		case ImageUtils.INTENT_REQUEST_CODE_FLITER:
			if (resultCode == RESULT_OK) {
				String path = data.getStringExtra("path");
				if (path != null) {
					ImageUtils.createThumbnail(this, path, THUMBNAIL_PATH + File.separator);
					sendMessage(path, CONTENT_TYPE.IMAGE);
					refreshAdapter();
				}
			}
			break;

		case FILE_SELECT_CODE: {
			if (resultCode == RESULT_OK) {
				Uri uri = data.getData();
				String path = uri.getPath();

				System.out.println("===File path is=="+path);


				try {

					if (path != null) {
						//sendFilePath = path;
						/*sendFilePath = getRealFilePath(ChatActivity.this, uri);
						System.out.println("==path2=="+sendFilePath);
					*/	
						sendFilePath = getPath(ChatActivity.this, uri);
						System.out.println("==path2=="+sendFilePath);
						
						
						
						sendMessage(sendFilePath, CONTENT_TYPE.FILE);
						refreshAdapter();
					}

					//String path2 = getRealFilePath(ChatActivity.this, uri);
					//System.out.println("==path2=="+path2);

				} catch (Exception e) {
					// TODO: handle exception
					e.printStackTrace();
				}
			}
		}
		break;
		}
	}

	public boolean isThisActivityMsg(Message msg) {
		String senderIMEI = msg.getSenderIMEI();
		if (mChatUser.getIMEI().equals(senderIMEI)) {
			mMessagesList.add(msg);
			return true;
		}
		return false;

	}

	public void processMessage(android.os.Message pMsg) {
		switch (pMsg.what) {
		case IPMSGConst.IPMSG_SENDMSG:
			if (isThisActivityMsg((Message) pMsg.obj)) {
				handler.sendEmptyMessage(1);
			}
			break;

		case IPMSGConst.IPMSG_CONFIRM_IMAGE_DATA: { // å›¾ç‰‡å¼€å§‹åDEFAULT‘é€DEFAULT
			LogUtils.d(TAG, "æŽ¥æ”¶æ–¹ç¡®è®¤å›¾ç‰‡è¯·æ±‚,åDEFAULT‘é€DEFAULTçš„æ–‡ä»¶ä¸º" + mCameraImagePath);
			tcpClient = TcpClient.getInstance(ChatActivity.this);
			tcpClient.startSend();
			tcpClient.sendFile(mCameraImagePath, mChatUser.getIpaddress(),
					Message.CONTENT_TYPE.IMAGE);
		}
		break;

		case IPMSGConst.IPMSG_CONFIRM_VOICE_DATA: { // è¯­éŸ³å¼€å§‹åDEFAULT‘é€DEFAULT
			LogUtils.d(TAG, "æŽ¥æ”¶æ–¹ç¡®è®¤è¯­éŸ³è¯·æ±‚,åDEFAULT‘é€DEFAULTçš„æ–‡ä»¶ä¸º" + mVoicePath);
			tcpClient = TcpClient.getInstance(ChatActivity.this);
			tcpClient.startSend();
			if (FileUtils.isFileExists(mVoicePath))
				tcpClient.sendFile(mVoicePath, mChatUser.getIpaddress(),
						Message.CONTENT_TYPE.VOICE);
		}
		break;

		case IPMSGConst.IPMSG_CONFIRM_FILE_DATA: { // æ–‡ä»¶å¼€å§‹åDEFAULT‘é€DEFAULT
			LogUtils.d(TAG, "æŽ¥æ”¶æ–¹ç¡®è®¤æ–‡ä»¶è¯·æ±‚,åDEFAULT‘é€DEFAULTçš„æ–‡ä»¶ä¸º" + sendFilePath);
			tcpClient = TcpClient.getInstance(ChatActivity.this);
			tcpClient.startSend();
			if (FileUtils.isFileExists(sendFilePath))
				tcpClient.sendFile(sendFilePath, mChatUser.getIpaddress(),
						Message.CONTENT_TYPE.FILE);
		}
		break;

		}
		// End of switch
	}

	private void startRecord() {
		recordState = RECORD_ON;
		RECORD_FILENAME = System.currentTimeMillis()
				+ hillfly.wifichat.util.TextUtils.getRandomNumStr(3);

		mAudioRecorder = new AudioRecorderUtils();
		mAudioRecorder.setVoicePath(VOICE_PATH, RECORD_FILENAME);
		mRecordThread = new Thread(recordThread);

		try {
			mAudioRecorder.start();
			mRecordThread.start();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		showVoiceDialog(0);

	}

	private void stopRecord() {
		recordState = RECORD_OFF;
		try {
			mRecordThread.interrupt();
			mRecordThread = null;
			mAudioRecorder.stop();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		voiceValue = 0.0;
	}

	// å½•éŸ³çº¿ç¨‹
	private Runnable recordThread = new Runnable() {

		@Override
		public void run() {
			recodeTime = 0.0f;
			while (recordState == RECORD_ON) {
				// é™DEFAULTåˆ¶å½•éŸ³æ—¶é•¿
				// if (recodeTime >= MAX_RECORD_TIME && MAX_RECORD_TIME != 0) {
				// imgHandle.sendEmptyMessage(0);
				// } else
				{
					try {
						Thread.sleep(200);
						recodeTime += 0.2;
						// èŽ·åDEFAULT–éŸ³é‡DEFAULTï¼Œæ›´æ–°dialog
						if (!isMove) {
							voiceValue = mAudioRecorder.getAmplitude();
							handler.sendEmptyMessage(0);
						}
					}
					catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}

		}
	};

	public Handler handler = new Handler() {
		@Override
		public void handleMessage(android.os.Message msg) {
			switch (msg.what) {
			case 0: // æ›´æ–°å½•éŸ³éŸ³é‡DEFAULT
				setDialogImage();
				break;

			case 1:
				refreshAdapter(); // æ›´æ–°æ¶ˆæDEFAULT¯æ˜¾ç¤º
				break;

			default:
				break;
			}

		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
		// TODO Auto-generated method stub
		Message msg = mMessagesList.get((int) id);
		switch (msg.getContentType()) {
		case IMAGE:
			Intent imgIntent = new Intent(mContext, ImageBrowserActivity.class);
			imgIntent.putExtra(ImageBrowserActivity.IMAGE_TYPE, ImageBrowserActivity.TYPE_PHOTO);
			imgIntent.putExtra(ImageBrowserActivity.PATH, msg.getMsgContent());
			startActivity(imgIntent);
			overridePendingTransition(R.anim.zoom_enter, 0);

			break;

		case VOICE:
			// æ’­æ”¾å½•éŸ³
			final ImageView imgView = (ImageView) view
			.findViewById(R.id.voice_message_iv_msgimage);
			if (!isPlay) {
				mMediaPlayer = new MediaPlayer();
				String filePath = msg.getMsgContent();
				try {
					mMediaPlayer.setDataSource(filePath);
					mMediaPlayer.prepare();
					imgView.setImageResource(R.drawable.voicerecord_stop);
					isPlay = true;
					mMediaPlayer.start();
					// è®¾ç½®æ’­æ”¾ç»“æDEFAULTŸæ—¶ç›‘åDEFAULT¬
					mMediaPlayer.setOnCompletionListener(new OnCompletionListener() {

						@Override
						public void onCompletion(MediaPlayer mp) {
							if (isPlay) {
								imgView.setImageResource(R.drawable.voicerecord_right);
								isPlay = false;
								mMediaPlayer.stop();
								mMediaPlayer.release();
							}
						}
					});
				}
				catch (IOException e) {
					e.printStackTrace();
				}
			}
			else {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.stop();
					mMediaPlayer.release();
					isPlay = false;
				}
				else {
					isPlay = false;
					mMediaPlayer.release();
				}
				imgView.setImageResource(R.drawable.voicerecord_right);
			}

			break;

		case FILE:
			//saf
			System.out.println("=file path is ="+FileUtils.getPathByFullPath(msg.getMsgContent()).toString());
			System.out.println("=file path222 is ="+msg.getMsgContent().toString());
			
			/*  	
			 * main 
                Intent fileIntent = new Intent();
			 */
			//fileIntent.setType("*/*");
			/*
                fileIntent.setData(Uri.parse("file://"
                        + FileUtils.getPathByFullPath(msg.getMsgContent())));
                mContext.startActivity(fileIntent);

			 */     


			try {


				/*                	String strFilePath = appDir.getAbsolutePath()+values[position];
	            	File filestrFilePath = new File(appDir.getAbsolutePath()+File.separator+values[position]);*/ 

				MimeTypeMap myMime = MimeTypeMap.getSingleton();
				Intent newIntent = new Intent(Intent.ACTION_VIEW);
				//String mimeType = myMime.getMimeTypeFromExtension(fileExt(strFilePath).substring(1));
				String mimeType = myMime.getMimeTypeFromExtension(fileExt(msg.getMsgContent().toString()).substring(1));
				newIntent.setDataAndType(Uri.parse("file://"+ msg.getMsgContent().toString()),mimeType); // change this path if error like audio can't play
				startActivity(newIntent);
				

			} catch (ActivityNotFoundException e) {
				Toast.makeText(ChatActivity.this, "No handler for this type of file.", Toast.LENGTH_LONG).show();
			}
			catch (Exception e) {
				// TODO: handle exception
				System.out.println("=============Myexp=====");
				e.printStackTrace();
			}


			break;

		default:
			break;

		}

	}


	private String fileExt(String url) {
		if (url.indexOf("?") > -1) {
			url = url.substring(0, url.indexOf("?"));
		}
		if (url.lastIndexOf(".") == -1) {
			return null;
		} else {
			String ext = url.substring(url.lastIndexOf("."));
			if (ext.indexOf("%") > -1) {
				ext = ext.substring(0, ext.indexOf("%"));
			}
			if (ext.indexOf("/") > -1) {
				ext = ext.substring(0, ext.indexOf("/"));
			}
			return ext.toLowerCase();

		}
	}
	public boolean isValidURL(String url) {  

		URL u = null;

		try {  
			u = new URL(url);  
		} catch (MalformedURLException e) {  
			return false;  
		}

		try {  
			u.toURI();  
		} catch (URISyntaxException e) {  
			return false;  
		}  

		return true;  
	} 



	public class RealPathUtil {

		@SuppressLint("NewApi")
		public  String getRealPathFromURI_API19(Context context, Uri uri){
			String filePath = "";
			String wholeID = DocumentsContract.getDocumentId(uri);

			// Split at colon, use second item in the array
			String id = wholeID.split(":")[1];

			String[] column = { MediaStore.Images.Media.DATA };     

			// where id is equal to             
			String sel = MediaStore.Images.Media._ID + "=?";

			Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
					column, sel, new String[]{ id }, null);

			int columnIndex = cursor.getColumnIndex(column[0]);

			if (cursor.moveToFirst()) {
				filePath = cursor.getString(columnIndex);
			}   
			cursor.close();
			return filePath;
		}


		@SuppressLint("NewApi")
		public String getRealPathFromURI_API11to18(Context context, Uri contentUri) {
			String[] proj = { MediaStore.Images.Media.DATA };
			String result = null;

			CursorLoader cursorLoader = new CursorLoader(
					context, 
					contentUri, proj, null, null, null);        
			Cursor cursor = cursorLoader.loadInBackground();

			if(cursor != null){
				int column_index = 
						cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				result = cursor.getString(column_index);
			}
			return result;  
		}

		public String getRealPathFromURI_BelowAPI11(Context context, Uri contentUri){
			String[] proj = { MediaStore.Images.Media.DATA };
			Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
			int column_index
			= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
			cursor.moveToFirst();
			return cursor.getString(column_index);
		}




	}




	@SuppressLint("NewApi")
	public String getRealFilePath(Context context,Uri contentUri)
	{

		System.out.println("==your device version=="+Build.VERSION.SDK_INT);
		if (Build.VERSION.SDK_INT < 11)
		{    String[] proj = { MediaStore.Images.Media.DATA };
		Cursor cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
		int column_index
		= cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
		cursor.moveToFirst();
		return cursor.getString(column_index);

		}
		else if(Build.VERSION.SDK_INT >= 11 && Build.VERSION.SDK_INT  <=18 ) 
		{
			String[] proj = { MediaStore.Images.Media.DATA };
			String result = null;

			CursorLoader cursorLoader = new CursorLoader(
					context, 
					contentUri, proj, null, null, null);        
			Cursor cursor = cursorLoader.loadInBackground();

			if(cursor != null){
				int column_index = 
						cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
				cursor.moveToFirst();
				result = cursor.getString(column_index);
			}
			return result;  
		}
		else
		{
			String filePath = "";
			String wholeID = DocumentsContract.getDocumentId(contentUri);

			// Split at colon, use second item in the array
			String id = wholeID.split(":")[1];

			String[] column = { MediaStore.Images.Media.DATA };     

			// where id is equal to             
			String sel = MediaStore.Images.Media._ID + "=?";

			Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, 
					column, sel, new String[]{ id }, null);

			int columnIndex = cursor.getColumnIndex(column[0]);

			if (cursor.moveToFirst()) {
				filePath = cursor.getString(columnIndex);
			}   
			cursor.close();
			return filePath;




		}
	}

	
	/**
	 * Get a file path from a Uri. This will get the the path for Storage Access
	 * Framework Documents, as well as the _data field for the MediaStore and
	 * other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @author paulburke
	 */
	public static String getPath(final Context context, final Uri uri) {

	    final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

	    // DocumentProvider
	    if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
	        // ExternalStorageProvider
	        if (isExternalStorageDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            if ("primary".equalsIgnoreCase(type)) {
	                return Environment.getExternalStorageDirectory() + "/" + split[1];
	            }

	            // TODO handle non-primary volumes
	        }
	        // DownloadsProvider
	        else if (isDownloadsDocument(uri)) {

	            final String id = DocumentsContract.getDocumentId(uri);
	            final Uri contentUri = ContentUris.withAppendedId(
	                    Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

	            return getDataColumn(context, contentUri, null, null);
	        }
	        // MediaProvider
	        else if (isMediaDocument(uri)) {
	            final String docId = DocumentsContract.getDocumentId(uri);
	            final String[] split = docId.split(":");
	            final String type = split[0];

	            Uri contentUri = null;
	            if ("image".equals(type)) {
	                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
	            } else if ("video".equals(type)) {
	                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
	            } else if ("audio".equals(type)) {
	                contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
	            }

	            final String selection = "_id=?";
	            final String[] selectionArgs = new String[] {
	                    split[1]
	            };

	            return getDataColumn(context, contentUri, selection, selectionArgs);
	        }
	    }
	    // MediaStore (and general)
	    else if ("content".equalsIgnoreCase(uri.getScheme())) {

	        // Return the remote address
	        if (isGooglePhotosUri(uri))
	            return uri.getLastPathSegment();

	        return getDataColumn(context, uri, null, null);
	    }
	    // File
	    else if ("file".equalsIgnoreCase(uri.getScheme())) {
	        return uri.getPath();
	    }

	    return null;
	}

	/**
	 * Get the value of the data column for this Uri. This is useful for
	 * MediaStore Uris, and other file-based ContentProviders.
	 *
	 * @param context The context.
	 * @param uri The Uri to query.
	 * @param selection (Optional) Filter used in the query.
	 * @param selectionArgs (Optional) Selection arguments used in the query.
	 * @return The value of the _data column, which is typically a file path.
	 */
	public static String getDataColumn(Context context, Uri uri, String selection,
	        String[] selectionArgs) {

	    Cursor cursor = null;
	    final String column = "_data";
	    final String[] projection = {
	            column
	    };

	    try {
	        cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
	                null);
	        if (cursor != null && cursor.moveToFirst()) {
	            final int index = cursor.getColumnIndexOrThrow(column);
	            return cursor.getString(index);
	        }
	    } finally {
	        if (cursor != null)
	            cursor.close();
	    }
	    return null;
	}


	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is ExternalStorageProvider.
	 */
	public static boolean isExternalStorageDocument(Uri uri) {
	    return "com.android.externalstorage.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is DownloadsProvider.
	 */
	public static boolean isDownloadsDocument(Uri uri) {
	    return "com.android.providers.downloads.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is MediaProvider.
	 */
	public static boolean isMediaDocument(Uri uri) {
	    return "com.android.providers.media.documents".equals(uri.getAuthority());
	}

	/**
	 * @param uri The Uri to check.
	 * @return Whether the Uri authority is Google Photos.
	 */
	public static boolean isGooglePhotosUri(Uri uri) {
	    return "com.google.android.apps.photos.content".equals(uri.getAuthority());
	}



}
