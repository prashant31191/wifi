package hillfly.wifichat.activity.maintabs;

import hillfly.wifichat.BaseFragment;
import hillfly.wifichat.R;
import hillfly.wifichat.util.ClientScanResult;
import hillfly.wifichat.util.WifiApManager;
import hillfly.wifichat.view.MultiListView;
import hillfly.wifichat.view.MultiListView.OnRefreshListener;

import java.util.ArrayList;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class ConnectionFragment extends BaseFragment implements OnRefreshListener  {


	MultiListView friends_list;
	WifiApManager wifiApManager;

	public ConnectionFragment() {
	}

	public ConnectionFragment(Context context) {
		super(context);
	}

	@Override
	public View
	onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mView = inflater.inflate(R.layout.fragment_friends, container, false);
		return super.onCreateView(inflater, container, savedInstanceState);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		super.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	protected void initViews() {
		friends_list = (MultiListView) findViewById(R.id.friends_list);
		friends_list.setVisibility(View.VISIBLE);



		wifiApManager = new WifiApManager(getActivity());

		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy); 

		RetrieveFeedTask retrieveFeedTask = new RetrieveFeedTask();
		retrieveFeedTask.execute();

		//scan();

	}
/*
	private class RetrieveFeedTask extends AsyncTask <Void, Void, Void> {
	    private ProgressDialog dialog;
	     
	 
	 
	    @Override
	    protected void onPreExecute() {
	    	dialog = new ProgressDialog(getActivity());
	        dialog.setMessage("Doing something, please wait.");
	        dialog.show();
	    }
	     
	    @Override
	    protected void onPostExecute(Void result) {
	        if (dialog.isShowing()) {
	            dialog.dismiss();
	        }
	    }
	     
	    @Override
	    protected Void doInBackground(Void... params) {
	        try {
	            Thread.sleep(5000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	 
	        return null;
	    }
	     
	}*/

	class RetrieveFeedTask extends AsyncTask<Void, Void, Void> {

		private ProgressDialog dialog;
		 @Override
		    protected void onPreExecute() {
		    	dialog = new ProgressDialog(getActivity());
		        dialog.setMessage("Get wifi connected users ...");
		        dialog.show();
		    }
		 
		
		
		  @Override
		    protected void onPostExecute(Void result) {
			  System.out.println("==On post==");
			  if (dialog.isShowing()) {
		            dialog.dismiss();
		        }
		        friends_list.onRefreshComplete();
		    }
		  

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			// TODO Auto-generated method stub
			

			getActivity().runOnUiThread(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					System.out.println("=running...=");
					scan();		
				}
			});
			return null;
		
		}

	}

	private void scan() {


		getActivity().runOnUiThread(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				System.out.println("=running...=");

				ArrayList<ClientScanResult> clients = wifiApManager.getClientList(false);
				String strDeviceDetails = "\n";



				int i = 0;
				String[] strArrLvData = new String[clients.size()];


				for (ClientScanResult clientScanResult : clients) {
					System.out.println("Device: " + clientScanResult.getDevice() );
					strDeviceDetails = "\n";
					strDeviceDetails = strDeviceDetails+"####################"+"\n";
					strDeviceDetails = strDeviceDetails + "IpAddr: " + clientScanResult.getIpAddr() + "\n";					
					strDeviceDetails = strDeviceDetails +"Device: " + clientScanResult.getDevice() + "\n";
					strDeviceDetails = strDeviceDetails +"HWAddr: " + clientScanResult.getHWAddr() + "\n";
					strDeviceDetails = strDeviceDetails +"isReachable: " + clientScanResult.isReachable() + "\n";

					/*				    htvDevName.append("####################\n");
					htvDevName.append("IpAddr: " + clientScanResult.getIpAddr() + "\n");
					htvDevName.append("Device: " + clientScanResult.getDevice() + "\n");
					htvDevName.append("HWAddr: " + clientScanResult.getHWAddr() + "\n");
					htvDevName.append("isReachable: " + clientScanResult.isReachable() + "\n");
					 */
					//htvDevName.setText("Device: " + clientScanResult.getDevice() + "\n");

					strArrLvData[i] = strDeviceDetails;
					i = i+1;
				}





				friends_list.setAdapter(new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1, strArrLvData));



				/*  htvDevName.append("Clients: \n");
			        for (ClientScanResult clientScanResult : clients) {
			        	System.out.println("Device: " + clientScanResult.getDevice() );


			        	htvDevName.append("####################\n");
			        	htvDevName.append("IpAddr: " + clientScanResult.getIpAddr() + "\n");
			        	htvDevName.append("Device: " + clientScanResult.getDevice() + "\n");
			        	htvDevName.append("HWAddr: " + clientScanResult.getHWAddr() + "\n");
			        	htvDevName.append("isReachable: " + clientScanResult.isReachable() + "\n");

			        	//htvDevName.setText("Device: " + clientScanResult.getDevice() + "\n");
			        }*/

			}
		});





	}
	@Override
	protected void initEvents() {
		// TODO Auto-generated method stub
		/*  mAdapter = new FriendsAdapter(getActivity(), mUsersList);
	        mListView.setAdapter(mAdapter);
	        mListView.setOnRefreshListener(this);
	        mListView.setOnItemClickListener(this);*/

		friends_list.setOnRefreshListener(this);
	}

	@Override
	protected void init() {
		// TODO Auto-generated method stub

	}

	@Override
	public void onRefresh() {
		// TODO Auto-generated method stub

		RetrieveFeedTask retrieveFeedTask = new RetrieveFeedTask();
		retrieveFeedTask.execute();


	}






}
