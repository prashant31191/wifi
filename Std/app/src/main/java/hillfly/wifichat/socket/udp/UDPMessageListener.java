package hillfly.wifichat.socket.udp;

import hillfly.wifichat.ActivitiesManager;
import hillfly.wifichat.BaseApplication;
import hillfly.wifichat.activity.message.ChatActivity;
import hillfly.wifichat.bean.Entity;
import hillfly.wifichat.bean.Message;
import hillfly.wifichat.bean.Users;
import hillfly.wifichat.socket.tcp.TcpService;
import hillfly.wifichat.sql.SqlDBOperate;
import hillfly.wifichat.util.ImageUtils;
import hillfly.wifichat.util.LogUtils;
import hillfly.wifichat.util.SessionUtils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;

public class UDPMessageListener implements Runnable {

    private static final String TAG = "SZU_UDPMessageListener";

    private static final int POOL_SIZE = 5; // åDEFAULT•ä¸ªCPUçº¿ç¨‹æ± å¤§å°DEFAULT
    private static final int BUFFERLENGTH = 1024; // ç¼“å†²å¤§å°DEFAULT

    private static byte[] sendBuffer = new byte[BUFFERLENGTH];
    private static byte[] receiveBuffer = new byte[BUFFERLENGTH];

    private HashMap<String, String> mLastMsgCache; // æœ€åDEFAULTŽä¸€æDEFAULT¡æ¶ˆæDEFAULT¯ç¼“å­˜ï¼Œä»¥IMEIä¸ºKEY
    private ArrayList<Users> mUnReadPeopleList; // æœªè¯»æ¶ˆæDEFAULT¯çš„ç”¨æˆ·é˜Ÿåˆ—
    private HashMap<String, Users> mOnlineUsers; // åœ¨çº¿ç”¨æˆ·é›†åDEFAULTˆï¼Œä»¥IMEIä¸ºKEY

    private String BROADCASTIP;
    private Thread receiveUDPThread;
    private boolean isThreadRunning;
    private List<OnNewMsgListener> mListenerList;

    private Users mLocalUser; // æœ¬æœºç”¨æˆ·å¯¹è±¡
    private SqlDBOperate mDBOperate;

    private static ExecutorService executor;
    private static DatagramSocket UDPSocket;
    private static DatagramPacket sendDatagramPacket;
    private DatagramPacket receiveDatagramPacket;

    private static Context mContext;
    private static UDPMessageListener instance;

    private UDPMessageListener() {
        BROADCASTIP = "255.255.255.255";
        // BROADCASTIP = WifiUtils.getBroadcastAddress();

        mDBOperate = new SqlDBOperate(mContext);
        mListenerList = new ArrayList<UDPMessageListener.OnNewMsgListener>();
        mOnlineUsers = new LinkedHashMap<String, Users>();
        mLastMsgCache = new HashMap<String, String>();
        mUnReadPeopleList = new ArrayList<Users>();

        int cpuNums = Runtime.getRuntime().availableProcessors();
        executor = Executors.newFixedThreadPool(cpuNums * POOL_SIZE); // æ ¹æDEFAULT®CPUæ•°ç›®åˆDEFAULTå§‹åŒ–çº¿ç¨‹æ± 
    }

    /**
     * <p>
     * èŽ·åDEFAULT–UDPSocketThreadå®žä¾‹
     * <p>
     * åDEFAULT•ä¾‹æ¨¡å¼DEFAULTï¼Œè¿”å›žå”¯ä¸€å®žä¾‹
     * 
     * @param paramApplication
     * @return instance
     */
    public static UDPMessageListener getInstance(Context context) {
        if (instance == null) {
            mContext = context;
            instance = new UDPMessageListener();
        }
        return instance;
    }

    @Override
    public void run() {
        while (isThreadRunning) {

            try {
                UDPSocket.receive(receiveDatagramPacket);
            }
            catch (IOException e) {
                isThreadRunning = false;
                receiveDatagramPacket = null;
                if (UDPSocket != null) {
                    UDPSocket.close();
                    UDPSocket = null;
                }
                receiveUDPThread = null;
                LogUtils.e(TAG, "UDPæ•°æDEFAULT®åŒ…æŽ¥æ”¶å¤±è´¥ï¼DEFAULTçº¿ç¨‹åDEFAULTœæ­¢");
                e.printStackTrace();
                break;
            }

            if (receiveDatagramPacket.getLength() == 0) {
                LogUtils.e(TAG, "æ— æ³•æŽ¥æ”¶UDPæ•°æDEFAULT®æˆ–è€…æŽ¥æ”¶åˆ°çš„UDPæ•°æDEFAULT®ä¸ºç©º");
                continue;
            }

            String UDPListenResStr = "";
            try {
                UDPListenResStr = new String(receiveBuffer, 0, receiveDatagramPacket.getLength(),
                        "gbk");
            }
            catch (UnsupportedEncodingException e) {
                LogUtils.e(TAG, "ç³»ç»Ÿä¸DEFAULTæ”¯æŒDEFAULTGBKç¼–ç DEFAULT");
            }

            IPMSGProtocol ipmsgRes = new IPMSGProtocol(UDPListenResStr);
            int commandNo = ipmsgRes.getCommandNo(); // èŽ·åDEFAULT–å‘½ä»¤å­—
            String senderIMEI = ipmsgRes.getSenderIMEI();
            String senderIp = receiveDatagramPacket.getAddress().getHostAddress();

            if (BaseApplication.isDebugmode) {
                processMessage(commandNo, ipmsgRes, senderIMEI, senderIp);
            }
            else {
                if (!SessionUtils.isLocalUser(senderIMEI)) {
                    processMessage(commandNo, ipmsgRes, senderIMEI, senderIp);
                }
            }

            // æ¯DEFAULTæ¬¡æŽ¥æ”¶å®ŒUDPæ•°æDEFAULT®åDEFAULTŽï¼Œé‡DEFAULTç½®é•¿åº¦ã€‚åDEFAULT¦åˆ™åDEFAULT¯èƒ½ä¼šå¯¼è‡´ä¸‹æ¬¡æ”¶åˆ°æ•°æDEFAULT®åŒ…è¢«æˆªæ–­ã€‚
            if (receiveDatagramPacket != null) {
                receiveDatagramPacket.setLength(BUFFERLENGTH);
            }

        }

        receiveDatagramPacket = null;
        if (UDPSocket != null) {
            UDPSocket.close();
            UDPSocket = null;
        }
        receiveUDPThread = null;

    }

    public void processMessage(int commandNo, IPMSGProtocol ipmsgRes, String senderIMEI,
            String senderIp) {
        TcpService tcpService;
        switch (commandNo) {

        // æ”¶åˆ°ä¸Šçº¿æ•°æDEFAULT®åŒ…ï¼Œæ·»åŠ ç”¨æˆ·ï¼Œå¹¶å›žé€DEFAULTIPMSG_ANSENTRYåº”ç­”ã€‚
            case IPMSGConst.IPMSG_BR_ENTRY: {
                LogUtils.i(TAG, "æ”¶åˆ°ä¸Šçº¿é€šçŸ¥");
                addUser(ipmsgRes);
                sendUDPdata(IPMSGConst.IPMSG_ANSENTRY, receiveDatagramPacket.getAddress(),
                        mLocalUser);
                LogUtils.i(TAG, "æˆDEFAULTåŠŸåDEFAULT‘é€DEFAULTä¸Šçº¿åº”ç­”");
            }
                break;

            // æ”¶åˆ°ä¸Šçº¿åº”ç­”ï¼Œæ›´æ–°åœ¨çº¿ç”¨æˆ·åˆ—è¡¨
            case IPMSGConst.IPMSG_ANSENTRY: {
                LogUtils.i(TAG, "æ”¶åˆ°ä¸Šçº¿åº”ç­”");
                addUser(ipmsgRes);
            }
                break;

            // æ”¶åˆ°ä¸‹çº¿å¹¿æ’­
            case IPMSGConst.IPMSG_BR_EXIT: {
                removeOnlineUser(senderIMEI, 1);
                LogUtils.i(TAG, "æˆDEFAULTåŠŸåˆ é™¤imeiä¸º" + senderIMEI + "çš„ç”¨æˆ·");
            }
                break;

            case IPMSGConst.IPMSG_REQUEST_IMAGE_DATA:
                LogUtils.i(TAG, "æ”¶åˆ°IMAGEåDEFAULT‘é€DEFAULTè¯·æ±‚");

                tcpService = TcpService.getInstance(mContext);
                tcpService.setSavePath(BaseApplication.IMAG_PATH);
                tcpService.startReceive();
                sendUDPdata(IPMSGConst.IPMSG_CONFIRM_IMAGE_DATA, senderIp);
                break;

            case IPMSGConst.IPMSG_REQUEST_VOICE_DATA:
                LogUtils.i(TAG, "æ”¶åˆ°VOICEåDEFAULT‘é€DEFAULTè¯·æ±‚");

                tcpService = TcpService.getInstance(mContext);
                tcpService.setSavePath(BaseApplication.VOICE_PATH);
                tcpService.startReceive();
                sendUDPdata(IPMSGConst.IPMSG_CONFIRM_VOICE_DATA, senderIp);
                break;

            case IPMSGConst.IPMSG_SENDMSG: {
                LogUtils.i(TAG, "æ”¶åˆ°MSGæ¶ˆæDEFAULT¯");
                Message msg = (Message) ipmsgRes.getAddObject();

                switch (msg.getContentType()) {
                    case TEXT:
                        sendUDPdata(IPMSGConst.IPMSG_RECVMSG, senderIp, ipmsgRes.getPacketNo());
                        break;

                    case IMAGE:
                        LogUtils.i(TAG, "æ”¶åˆ°å›¾ç‰‡ä¿¡æDEFAULT¯");
                        msg.setMsgContent(BaseApplication.IMAG_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        String THUMBNAIL_PATH = BaseApplication.THUMBNAIL_PATH + File.separator
                                + msg.getSenderIMEI();

                        LogUtils.d(TAG, "ç¼©ç•¥å›¾æ–‡ä»¶å¤¹è·¯å¾„:" + THUMBNAIL_PATH);
                        LogUtils.d(TAG, "å›¾ç‰‡æ–‡ä»¶è·¯å¾„:" + msg.getMsgContent());

                        ImageUtils.createThumbnail(mContext, msg.getMsgContent(), THUMBNAIL_PATH
                                + File.separator);
                        break;

                    case VOICE:
                        LogUtils.i(TAG, "æ”¶åˆ°å½•éŸ³ä¿¡æDEFAULT¯");
                        msg.setMsgContent(BaseApplication.VOICE_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                        LogUtils.d(TAG, "æ–‡ä»¶è·¯å¾„:" + msg.getMsgContent());
                        break;

                    case FILE:
                        LogUtils.i(TAG, "æ”¶åˆ°æ–‡ä»¶ åDEFAULT‘é€DEFAULTè¯·æ±‚");
                        tcpService = TcpService.getInstance(mContext);
                        tcpService.setSavePath(BaseApplication.FILE_PATH);
                        tcpService.startReceive();
                        sendUDPdata(IPMSGConst.IPMSG_CONFIRM_FILE_DATA, senderIp);
                        
                     /*   msg.setMsgContent(BaseApplication.FILE_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + msg.getMsgContent());
                      */  
                        
                        
                        String strDownloadUrl =  msg.getMsgContent();
                        
                        String fileName = strDownloadUrl.substring( strDownloadUrl.lastIndexOf('/')+1, strDownloadUrl.length() );
                		String fileNameWithoutExtn = fileName.substring(0, fileName.lastIndexOf('.'));
                		System.out.println("=fileNameWithoutExtn="+fileNameWithoutExtn);
                		System.out.println("=fileName="+fileName);
                		
                        
                        msg.setMsgContent(BaseApplication.FILE_PATH + File.separator
                                + msg.getSenderIMEI() + File.separator + fileName);
                        
                        
                        LogUtils.d(TAG, "Recived file pathæ–‡ä»¶è·¯å¾„:" + msg.getMsgContent());
                        break;
                }

                // åŠ å…¥æ•°æDEFAULT®åº“
                mDBOperate.addChattingInfo(senderIMEI, SessionUtils.getIMEI(), msg.getSendTime(),
                        msg.getMsgContent(), msg.getContentType());

                // åŠ å…¥æœªè¯»æ¶ˆæDEFAULT¯åˆ—è¡¨
                android.os.Message pMessage = new android.os.Message();
                pMessage.what = commandNo;
                pMessage.obj = msg;

                ChatActivity v = ActivitiesManager.getChatActivity();
                if (v == null) {
                    addUnReadPeople(getOnlineUser(senderIMEI)); // æ·»åŠ åˆ°æœªè¯»ç”¨æˆ·åˆ—è¡¨
                    for (int i = 0; i < mListenerList.size(); i++) {
                        android.os.Message pMsg = new android.os.Message();
                        pMsg.what = IPMSGConst.IPMSG_RECVMSG;
                        mListenerList.get(i).processMessage(pMsg);
                    }
                }
                else {
                    v.processMessage(pMessage);
                }

                addLastMsgCache(senderIMEI, msg); // æ·»åŠ åˆ°æ¶ˆæDEFAULT¯ç¼“å­˜
                BaseApplication.playNotification();

            }
                break;

            default:
                LogUtils.i(TAG, "æ”¶åˆ°å‘½ä»¤ï¼š" + commandNo);

                android.os.Message pMessage = new android.os.Message();
                pMessage.what = commandNo;

                ChatActivity v = ActivitiesManager.getChatActivity();
                if (v != null) {
                    v.processMessage(pMessage);
                }

                break;

        } // End of switch
    }

    /** å»ºç«‹Socketè¿žæŽ¥ **/
    public void connectUDPSocket() {
        try {
            // ç»‘å®šç«¯åDEFAULT£
            if (UDPSocket == null)
                UDPSocket = new DatagramSocket(IPMSGConst.PORT);
            LogUtils.i(TAG, "connectUDPSocket() ç»‘å®šç«¯åDEFAULT£æˆDEFAULTåŠŸ");

            // åˆ›å»ºæ•°æDEFAULT®æŽ¥åDEFAULT—åŒ…
            if (receiveDatagramPacket == null)
                receiveDatagramPacket = new DatagramPacket(receiveBuffer, BUFFERLENGTH);

            startUDPSocketThread();
        }
        catch (SocketException e) {
            e.printStackTrace();
        }
    }

    /** å¼€å§‹ç›‘åDEFAULT¬çº¿ç¨‹ **/
    public void startUDPSocketThread() {
        if (receiveUDPThread == null) {
            receiveUDPThread = new Thread(this);
            receiveUDPThread.start();
        }
        isThreadRunning = true;
        LogUtils.i(TAG, "startUDPSocketThread() çº¿ç¨‹åDEFAULT¯åŠ¨æˆDEFAULTåŠŸ");
    }

    /** æš‚åDEFAULTœç›‘åDEFAULT¬çº¿ç¨‹ **/
    public void stopUDPSocketThread() {
        isThreadRunning = false;
        if (receiveUDPThread != null)
            receiveUDPThread.interrupt();
        receiveUDPThread = null;
        instance = null; // ç½®ç©º, æ¶ˆé™¤éDEFAULT™æ€DEFAULTåDEFAULT˜é‡DEFAULTå¼•ç”¨
        LogUtils.i(TAG, "stopUDPSocketThread() çº¿ç¨‹åDEFAULTœæ­¢æˆDEFAULTåŠŸ");
    }

    public void addMsgListener(OnNewMsgListener listener) {
        this.mListenerList.add(listener);
    }

    public void removeMsgListener(OnNewMsgListener listener) {
        this.mListenerList.remove(listener);
    }

    /** ç”¨æˆ·ä¸Šçº¿é€šçŸ¥ **/
    public void notifyOnline() {
        // èŽ·åDEFAULT–æœ¬æœºç”¨æˆ·æ•°æDEFAULT®
        mLocalUser = SessionUtils.getLocalUserInfo();
        sendUDPdata(IPMSGConst.IPMSG_BR_ENTRY, BROADCASTIP, mLocalUser);
        LogUtils.i(TAG, "notifyOnline() ä¸Šçº¿é€šçŸ¥æˆDEFAULTåŠŸ");
    }

    /** ç”¨æˆ·ä¸‹çº¿é€šçŸ¥ **/
    public void notifyOffline() {
        sendUDPdata(IPMSGConst.IPMSG_BR_EXIT, BROADCASTIP);
        LogUtils.i(TAG, "notifyOffline() ä¸‹çº¿é€šçŸ¥æˆDEFAULTåŠŸ");
    }

    /** åˆ·æ–°ç”¨æˆ·åˆ—è¡¨ **/
    public void refreshUsers() {
        removeOnlineUser(null, 0); // æ¸…ç©ºåœ¨çº¿ç”¨æˆ·åˆ—è¡¨
        notifyOnline();
    }

    /**
     * æ·»åŠ ç”¨æˆ·åˆ°åœ¨çº¿åˆ—è¡¨ä¸­ (çº¿ç¨‹å®‰å…¨çš„)
     * 
     * @param paramIPMSGProtocol
     *            åŒ…åDEFAULT«ç”¨æˆ·ä¿¡æDEFAULT¯çš„IPMSGProtocolå­—ç¬¦ä¸²
     */
    private void addUser(IPMSGProtocol paramIPMSGProtocol) {
        String receiveIMEI = paramIPMSGProtocol.getSenderIMEI();
        if (BaseApplication.isDebugmode) {
            Users newUser = (Users) paramIPMSGProtocol.getAddObject();
            addOnlineUser(receiveIMEI, newUser);
            mDBOperate.addUserInfo(newUser);
        }
        else {
            if (!SessionUtils.isLocalUser(receiveIMEI)) {
                Users newUser = (Users) paramIPMSGProtocol.getAddObject();
                addOnlineUser(receiveIMEI, newUser);
                mDBOperate.addUserInfo(newUser);
            }
        }
        LogUtils.i(TAG, "æˆDEFAULTåŠŸæ·»åŠ imeiä¸º" + receiveIMEI + "çš„ç”¨æˆ·");

    }

    /**
     * åDEFAULT‘é€DEFAULTUDPæ•°æDEFAULT®åŒ…
     * 
     * @param commandNo
     *            æ¶ˆæDEFAULT¯å‘½ä»¤
     * @param targetIP
     *            ç›®æ ‡åœ°åDEFAULT€
     * @param addData
     *            é™„åŠ æ•°æDEFAULT®
     * @see IPMSGConst
     */
    public static void sendUDPdata(int commandNo, String targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }

    public static void sendUDPdata(int commandNo, InetAddress targetIP) {
        sendUDPdata(commandNo, targetIP, null);
    }

    public static void sendUDPdata(int commandNo, InetAddress targetIP, Object addData) {
        sendUDPdata(commandNo, targetIP.getHostAddress(), addData);
    }

    public static void sendUDPdata(int commandNo, String targetIP, Object addData) {
        IPMSGProtocol ipmsgProtocol = null;
        String imei = SessionUtils.getIMEI();

        if (addData == null) {
            ipmsgProtocol = new IPMSGProtocol(imei, commandNo);
        }
        else if (addData instanceof Entity) {
            ipmsgProtocol = new IPMSGProtocol(imei, commandNo, (Entity) addData);
        }
        else if (addData instanceof String) {
            ipmsgProtocol = new IPMSGProtocol(imei, commandNo, (String) addData);
        }
        sendUDPdata(ipmsgProtocol, targetIP);
    }

    public static void sendUDPdata(final IPMSGProtocol ipmsgProtocol, final String targetIP) {
        executor.execute(new Runnable() {

            @Override
            public void run() {
                try {
                    InetAddress targetAddr = InetAddress.getByName(targetIP); // ç›®çš„åœ°åDEFAULT€
                    sendBuffer = ipmsgProtocol.getProtocolJSON().getBytes("gbk");
                    sendDatagramPacket = new DatagramPacket(sendBuffer, sendBuffer.length,
                            targetAddr, IPMSGConst.PORT);
                    UDPSocket.send(sendDatagramPacket);
                    LogUtils.i(TAG, "sendUDPdata() æ•°æDEFAULT®åDEFAULT‘é€DEFAULTæˆDEFAULTåŠŸ");
                }
                catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e(TAG, "sendUDPdata() åDEFAULT‘é€DEFAULTUDPæ•°æDEFAULT®åŒ…å¤±è´¥");
                }

            }
        });

    }

    public synchronized void addOnlineUser(String paramIMEI, Users paramObject) {
        mOnlineUsers.put(paramIMEI, paramObject);

        for (int i = 0; i < mListenerList.size(); i++) {
            android.os.Message pMsg = new android.os.Message();
            pMsg.what = IPMSGConst.IPMSG_BR_ENTRY;
            mListenerList.get(i).processMessage(pMsg);
        }

        LogUtils.d(TAG, "addUser | OnlineUsersNumï¼š" + mOnlineUsers.size());
    }

    public Users getOnlineUser(String paramIMEI) {
        return mOnlineUsers.get(paramIMEI);
    }

    /**
     * ç§»é™¤åœ¨çº¿ç”¨æˆ·
     * 
     * @param paramIMEI
     *            éœ€è¦DEFAULTç§»é™¤çš„ç”¨æˆ·IMEI
     * @param paramtype
     *            æ“DEFAULTä½œç±»åž‹ï¼Œ0:æ¸…ç©ºåœ¨çº¿åˆ—è¡¨ï¼Œ1:ç§»é™¤æŒ‡å®šç”¨æˆ·
     */
    public void removeOnlineUser(String paramIMEI, int paramtype) {
        if (paramtype == 1) {
            mOnlineUsers.remove(paramIMEI);
            for (int i = 0; i < mListenerList.size(); i++) {
                android.os.Message pMsg = new android.os.Message();
                pMsg.what = IPMSGConst.IPMSG_BR_EXIT;
                mListenerList.get(i).processMessage(pMsg);
            }

        }
        else if (paramtype == 0) {
            mOnlineUsers.clear();
        }

        LogUtils.d(TAG, "removeUser | OnlineUsersNumï¼š" + mOnlineUsers.size());
    }

    public HashMap<String, Users> getOnlineUserMap() {
        return mOnlineUsers;
    }

    /**
     * æ–°å¢žç”¨æˆ·ç¼“å­˜
     * 
     * @param paramIMEI
     *            æ–°å¢žè®°å½•çš„å¯¹åº”ç”¨æˆ·IMEI
     * @param paramMsg
     *            éœ€è¦DEFAULTç¼“å­˜çš„æ¶ˆæDEFAULT¯å¯¹è±¡
     */
    public void addLastMsgCache(String paramIMEI, Message msg) {
        StringBuffer content = new StringBuffer();
        switch (msg.getContentType()) {
            case FILE:
                content.append("<FILE>: ").append(msg.getMsgContent());
                break;
            case IMAGE:
                content.append("<IMAGE>: ").append(msg.getMsgContent());
                break;
            case VOICE:
                content.append("<VOICE>: ").append(msg.getMsgContent());
                break;
            default:
                content.append(msg.getMsgContent());
                break;
        }
        if (msg.getMsgContent().isEmpty()) {
            content.append(" ");
        }
        mLastMsgCache.put(paramIMEI, content.toString());
    }

    /**
     * èŽ·åDEFAULT–æ¶ˆæDEFAULT¯ç¼“å­˜
     * 
     * @param paramIMEI
     *            éœ€è¦DEFAULTèŽ·åDEFAULT–æ¶ˆæDEFAULT¯ç¼“å­˜è®°å½•çš„ç”¨æˆ·IMEI
     * @return
     */
    public String getLastMsgCache(String paramIMEI) {
        return mLastMsgCache.get(paramIMEI);
    }

    /**
     * ç§»é™¤æ¶ˆæDEFAULT¯ç¼“å­˜
     * 
     * @param paramIMEI
     *            éœ€è¦DEFAULTæ¸…é™¤ç¼“å­˜çš„ç”¨æˆ·IMEI
     */
    public void removeLastMsgCache(String paramIMEI) {
        mLastMsgCache.remove(paramIMEI);
    }

    public void clearMsgCache() {
        mLastMsgCache.clear();
    }

    public void clearUnReadMessages() {
        mUnReadPeopleList.clear();
    }

    /**
     * æ–°å¢žæœªè¯»æ¶ˆæDEFAULT¯ç”¨æˆ·
     * 
     * @param people
     */
    public void addUnReadPeople(Users people) {
        if (!mUnReadPeopleList.contains(people))
            mUnReadPeopleList.add(people);
    }

    /**
     * èŽ·åDEFAULT–æœªè¯»æ¶ˆæDEFAULT¯é˜Ÿåˆ—
     * 
     * @return
     */
    public ArrayList<Users> getUnReadPeopleList() {
        return mUnReadPeopleList;
    }

    /**
     * èŽ·åDEFAULT–æœªè¯»ç”¨æˆ·æ•°
     * 
     * @return
     */
    public int getUnReadPeopleSize() {
        return mUnReadPeopleList.size();
    }

    /**
     * ç§»é™¤æŒ‡å®šæœªè¯»ç”¨æˆ·
     * 
     * @param people
     */
    public void removeUnReadPeople(Users people) {
        if (mUnReadPeopleList.contains(people))
            mUnReadPeopleList.remove(people);
    }

    /**
     * æ–°æ¶ˆæDEFAULT¯å¤„çDEFAULT†æŽ¥åDEFAULT£
     */
    public interface OnNewMsgListener {
        public void processMessage(android.os.Message pMsg);
    }

}