package hillfly.wifichat.activity.wifiap;
public class WifiApConst {
    public static final int CLOSE = 0x001;
    public static final int SEARCH = 0x002;
    public static final int CREATE = 0x003;
    public static final int NOTHING = 0x004;

    public static final int WiFiConnectError = 0;
    public static final int ApScanResult = 1;
    public static final int WiFiConnectSuccess = 2;
    public static final int ApCreateApSuccess = 3;
    public static final int ApUserChanged = 4;
    public static final int NetworkChanged = 5;
    public static final int ApConnectting = 6;

    public static final String WIFI_AP_HEADER = "Chat_";
    public static final String WIFI_AP_PASSWORD = "wifichat123";
}