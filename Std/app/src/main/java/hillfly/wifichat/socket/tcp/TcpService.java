package hillfly.wifichat.socket.tcp;

import hillfly.wifichat.BaseApplication;
import hillfly.wifichat.bean.Message;
import hillfly.wifichat.bean.Message.CONTENT_TYPE;
import hillfly.wifichat.file.Constant;
import hillfly.wifichat.file.FileState;
import hillfly.wifichat.util.LogUtils;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;

public class TcpService implements Runnable {
    private static final String TAG = "SZU_TcpService";

    private ServerSocket serviceSocket;
    private boolean SCAN_FLAG = false;
    private Thread mThread;
    ArrayList<FileState> receivedFileNames;
    ArrayList<SaveFileToDisk> saveFileToDisks;
    private static Handler mHandler;
    private String filePath = null;

    private static Context mContext;
    private static TcpService instance;

    private boolean IS_THREAD_STOP = false;

    private TcpService() {
        try {
            serviceSocket = new ServerSocket(Constant.TCP_SERVER_RECEIVE_PORT);
            saveFileToDisks = new ArrayList<TcpService.SaveFileToDisk>();
            LogUtils.d(TAG, "Build the listening server ServerSocket successfully");
        }
        catch (IOException e) {
            // TODO Auto-generated catch block
            LogUtils.d(TAG, "ServerSocket failed to establish listening server");
            e.printStackTrace();
        }
        mThread = new Thread(this);
    }

 /*   / **
            * <p>
?????* Get the TcpService instance
?????* <p>
?????* Singleton mode,// return the same instance
?????* /*/


    public static TcpService getInstance(Context context) {
        mContext = context;
        if (instance == null) {
            instance = new TcpService();
        }
        return instance;
    }

    public static void setHandler(Handler paramHandler) {
        mHandler = paramHandler;
    }

    public void setSavePath(String fileSavePath) {
        LogUtils.d(TAG, "Set up storage path is successful, the path is" + fileSavePath);
        this.filePath = fileSavePath;
        // REV_FLAG=true;
    }

    public TcpService(Context context) {
        this();
        mContext = context;
    }

    private void scan_recv() {
        try {
            Socket socket = serviceSocket.accept();
            // socket.setSoTimeout(5000);
            LogUtils.d(TAG, "The client is connected successfully");

            SaveFileToDisk fileToDisk = new SaveFileToDisk(socket, filePath);
            fileToDisk.start();

        }
        catch (IOException e) {
            e.printStackTrace();
            LogUtils.d(TAG, "???????");
            SCAN_FLAG = false;
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        LogUtils.d(TAG, "TCP_Service thread is on");
        while (!IS_THREAD_STOP) {
            if (SCAN_FLAG) {
                scan_recv();

            }
        }
    }

    public void release() {
        if (null != serviceSocket && !serviceSocket.isClosed())
            try {
                serviceSocket.close();
                serviceSocket = null;
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        while (SCAN_FLAG == true)
            ;
        SCAN_FLAG = false;
        IS_THREAD_STOP = true;
    }

    public void startReceive() {
        SCAN_FLAG = true;
        if (!mThread.isAlive())
            mThread.start();
    }

    public void startReceive(ArrayList<FileState> receivedFileNames) {
        SCAN_FLAG = true;
        if (!mThread.isAlive())
            mThread.start();
        this.receivedFileNames = receivedFileNames;
    }

    public void stopReceive() {
        while (SCAN_FLAG == true);
        SCAN_FLAG = false;
    }

    public class SaveFileToDisk extends Thread {
        private boolean SCAN_RECIEVE = true;
        private InputStream input = null;
        private DataInputStream dataInput;
        private byte[] mBuffer = new byte[Constant.READ_BUFFER_SIZE];
        private String savePath;
        private String type[] = { "TEXT", "IMAGE", "FILE", "VOICE" };

        public SaveFileToDisk(Socket socket) {
            try {
                input = socket.getInputStream();
                dataInput = new DataInputStream(input);
                LogUtils.d(TAG, "Get the network input stream successfully");
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                LogUtils.d(TAG, "Failed to get network input stream");
                SCAN_RECIEVE = false;
                e.printStackTrace();
            }
        }

        public SaveFileToDisk(Socket socket, String savePath) {
            this(socket);
            this.savePath = savePath;
        }

        public void recieveFile() {
            int readSize = 0;
            FileOutputStream fileOutputStream = null;
            BufferedOutputStream bufferOutput = null;
            String strFiledata;
            String[] strData = null;
            String fileSavePath;
            
            try {
                strFiledata = dataInput.readUTF().toString();
                strData = strFiledata.split("!");
                long length = Long.parseLong(strData[1]);

                LogUtils.d(TAG, "Transfer file type:" + strData[3]);
                fileSavePath = savePath + File.separator + strData[2] + File.separator + strData[0];
                fileOutputStream = new FileOutputStream(new File(fileSavePath));
                LogUtils.d(TAG, "Full Path is -->File storage path:-->" + fileSavePath);
                FileState fileState = new FileState(length, 0, fileSavePath, getType(strData[3]));
                BaseApplication.recieveFileStates.put(fileSavePath, fileState);
                FileState fs = BaseApplication.recieveFileStates.get(fileSavePath);
                bufferOutput = new BufferedOutputStream(fileOutputStream);
                long lastLength = 0;
                long currentLength = 0;
                long lastTime = System.currentTimeMillis();
                long currentTime = 0;
                int count = 0;
                long startTime = System.currentTimeMillis();
                while (-1 != (readSize = dataInput.read(mBuffer))) {
                    bufferOutput.write(mBuffer, 0, readSize);
                    currentLength += readSize;
                    count++;
                    if (count % 10 == 0) {
                        currentTime = System.currentTimeMillis();
                        long time = currentTime - lastTime;
                        lastTime = currentTime;
                        long Length = currentLength - lastLength;
                        lastLength = currentLength;
                        fs.currentSize = currentLength;
                        fs.percent = (int) ((float) currentLength / (float) length * 100);

                        switch (fs.type) {
                            case IMAGE:
                                break;

                            case VOICE:
                                break;

                            case FILE:
                                android.os.Message msg = mHandler.obtainMessage();
                                msg.obj = fs;
                                msg.sendToTarget();
                                break;

                            default:
                                break;
                        }
                    }
                }

                bufferOutput.flush();

                input.close();
                dataInput.close();
                bufferOutput.close();
                fileOutputStream.close();

                switch (fs.type) {
                    case IMAGE:
                        break;

                    case VOICE:
                        break;

                    case FILE:
                        android.os.Message msg = mHandler.obtainMessage();
                        fs.percent = 100;
                        msg.obj = fs;
                        msg.sendToTarget();
                        break;

                    default:
                        break;
                }

                BaseApplication.recieveFileStates.remove(fs.fileName);
            }
            catch (IOException e) {
                // TODO Auto-generated catch block
                LogUtils.d(TAG, "Writing file failed");
                e.printStackTrace();
            }
        }

        private Message.CONTENT_TYPE getType(String string) {
            if (string.equals(type[0]))
                return CONTENT_TYPE.TEXT;
            else if (string.equals(type[1]))
                return CONTENT_TYPE.IMAGE;
            else if (string.equals(type[2]))
                return CONTENT_TYPE.FILE;
            else if (string.equals(type[3]))
                return CONTENT_TYPE.VOICE;
            return null;

        }

        @Override
        public void run() {
            super.run();
            LogUtils.d(TAG, "SaveFileToDisk thread open");
            if (SCAN_RECIEVE)
                recieveFile();
        }
    }
}
