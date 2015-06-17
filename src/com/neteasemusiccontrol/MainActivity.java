package com.neteasemusiccontrol;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.StrictMode;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity {
   
    protected class IPAddress {
    	
    	protected String ipAddr;
    	protected String broadcastAddr;
    	
    	protected IPAddress(){
    		
    		this.ipAddr = "0.0.0.0";
    		this.broadcastAddr = "255.255.255.255";
    		
    	}
    }
    
    protected boolean playButtonClickFlag;
    protected boolean connectButtonClickFlag;
    protected ImageView cdRound;
    protected Animation cdAnim;   
    protected IPAddress ipAddress;
    
    protected class SendCommand implements Runnable {
    	
    	protected String command;
    	protected Handler commandHandler;
    	
    	private SendCommand(String cmd) {
    		
    		command = cmd;
    		
    		commandHandler = new Handler() {  
                @Override  
                public void handleMessage(Message msg) {  
                    super.handleMessage(msg);
                    Bundle result = msg.getData();  
                    String val = result.getString("state");
                    String cmd = result.getString("command");
                    Log.d("Command", "Command:"+cmd+" Result:"+val);
                    if (val.equals("succeed")) {
                    	Toast.makeText(MainActivity.this, cmd+" suceeded", Toast.LENGTH_SHORT).show();
                    } else {
                    	Toast.makeText(MainActivity.this, cmd+" failed", Toast.LENGTH_SHORT).show();
                    }
                }
    		};
    		
    	}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			Message msg = new Message();  
            Bundle result = new Bundle();  
            result.putString("command", this.command);
        	try {
				MulticastSocket multicastSocket= new  MulticastSocket(14569);
				Log.d("Broadcast","Setup multicast.");
				InetAddress serverAddress = InetAddress.getByName(ipAddress.broadcastAddr);
				byte[] data = this.command.getBytes();     
                DatagramPacket dataPacket = new DatagramPacket(data, data.length, serverAddress, 14569);
                Log.d("Broadcast","Broadcasting command " + this.command);
                multicastSocket.send(dataPacket);
                Log.d("Broadcast","Close multicast.");
                multicastSocket.close();
                
                result.putString("state", "succeed");
                
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				Log.e("Broadcast", "Cannot setup multicast.");
				result.putString("state", "fail");
			}
        	
            msg.setData(result);
            this.commandHandler.sendMessage(msg);
		}
    	
    }

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        playButtonClickFlag = false;
        connectButtonClickFlag = false;
        ipAddress = new IPAddress();
        
        cdRound = (ImageView)findViewById(R.id.cd);
        ImageView buttonPlay = (ImageView) findViewById(R.id.button_play);
        ImageView buttonBack = (ImageView) findViewById(R.id.button_back);
        ImageView buttonForward = (ImageView) findViewById(R.id.button_forward);
        ImageView buttonUpVol = (ImageView) findViewById(R.id.button_upvol);
        ImageView buttonDownVol = (ImageView) findViewById(R.id.button_downvol);
        TextView buttonConnect = (TextView) findViewById(R.id.banner);
      
        cdAnim = AnimationUtils.loadAnimation(this, R.drawable.cd_round);  
        LinearInterpolator lin = new LinearInterpolator();  
        cdAnim.setInterpolator(lin);
        
        try {
			this.getAddress();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			Log.e("AppNetwork","Cannot get IP address");
		}
        
        buttonPlay.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		if (!playButtonClickFlag) {
        			v.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_pause));
        			playButtonClickFlag = true;
        	        if (cdAnim != null) {  
        	            cdRound.startAnimation(cdAnim);
        	            SendCommand sendCmd = new SendCommand("play");
        	            new Thread(sendCmd).start();
        	        }
        		}
        		else {
        			v.setBackgroundDrawable(getResources().getDrawable(R.drawable.ic_play));
        			playButtonClickFlag = false;
        			if (cdAnim != null) {  
        	            cdRound.clearAnimation();
        	            SendCommand sendCmd = new SendCommand("play");
        	            new Thread(sendCmd).start();
        	        }
        		}
        		
        	}
        });
        
        buttonBack.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){

        		SendCommand sendCmd = new SendCommand("back");
	            new Thread(sendCmd).start();
                 
            }     
             
        });
        
        buttonForward.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		
        		SendCommand sendCmd = new SendCommand("forward");
	            new Thread(sendCmd).start();
        		
        	}
        });
        
        buttonUpVol.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
        		
        		SendCommand sendCmd = new SendCommand("upvol");
	            new Thread(sendCmd).start();
	            
            }     
             
        });
        
        buttonDownVol.setOnClickListener(new OnClickListener(){
        	@Override
        	public void onClick(View v){
                 
        		SendCommand sendCmd = new SendCommand("downvol");
	            new Thread(sendCmd).start();
        		
            }     
             
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void getAddress() throws IOException {
    	
	    WifiManager myWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	    DhcpInfo myDhcpInfo = myWifiManager.getDhcpInfo();  
	    ipAddress.broadcastAddr = intToIp((myDhcpInfo.ipAddress & myDhcpInfo.netmask) | ~myDhcpInfo.netmask);  
	    ipAddress.ipAddr = intToIp(myDhcpInfo.ipAddress);
	        
	    Log.d("AppNetwork","IP:"+ipAddress.ipAddr+"/"+ipAddress.broadcastAddr);
	        
	    if (ipAddress.ipAddr.equals("0.0.0.0")) {
	        	
	        Log.d("AppNetwork","Cannot get valid IP address throught WIFI, WIFI may not turned on");
	        AlertDialog.Builder wifiDialog = new AlertDialog.Builder(MainActivity.this);
	        wifiDialog.setTitle("No WIFI connected");
	        wifiDialog.setMessage("Application will close until there's WIFI connection.");
	        wifiDialog.setCancelable(true);
	        wifiDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					
	        	@Override
				public void onClick(DialogInterface dialog, int which) {
					// TODO Auto-generated method stub
	        		finish();
				}
	        });
	        wifiDialog.show();
    	}
    }
    
    private String intToIp(int i){
        return (i & 0xFF ) + "." + ((i >> 8 ) & 0xFF) + "." + ((i >> 16 ) & 0xFF) + "." + ( i >> 24 & 0xFF); 
	}

}
