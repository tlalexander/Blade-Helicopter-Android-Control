package com.tlalexander.bladeandroid;



/*
 * Copyright (C) 2011 @ksksue
 * Licensed under the Apache License, Version 2.0
 * http://www.apache.org/licenses/LICENSE-2.0
 */


import android.app.Activity;
import android.os.Bundle;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import jp.ksksue.driver.serial.*;

public class BladeAndroidControlActivity extends Activity {
	
	FTDriver mSerial;

	private TextView[] textviews = new TextView[6];
	private ProgressBar[] progressbars = new ProgressBar[6];
	private int values[] = new int[6];
	
	private boolean serialConnected=false;

	private boolean mStop=false;
	private boolean mStopped=false;
	private boolean dataPaused=true;	
	private boolean bind=false;

	private String mText;
	
	
	private Button buttonDelete;
	private Button buttonToggle;
	
	private Heli helicopter;
	private TextView textOver;
	private TextView textOver2;
	
	private LinearLayout l1;
	private LinearLayout l2;
	
	private View view1;
	private View view2;
	
		
	String TAG = "FTSampleTerminal";
    
    Handler mHandler = new Handler(); 

    //private Button btWrite;
   // private EditText etWrite;
    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        textviews[0] = (TextView) findViewById(R.id.textCH1);
        textviews[1] = (TextView) findViewById(R.id.textCH2);
        textviews[2] = (TextView) findViewById(R.id.textCH3);
        textviews[3] = (TextView) findViewById(R.id.textCH4);
        textviews[4] = (TextView) findViewById(R.id.textCH5);
        textviews[5] = (TextView) findViewById(R.id.textCH6);
        
        textOver = (TextView) findViewById(R.id.textOverflow);
        textOver2 = (TextView) findViewById(R.id.textOverflow2);
        
        view1 = (View) findViewById(R.id.view1);
        view1.setVisibility(View.INVISIBLE);
        view2 = (View) findViewById(R.id.view2);
        
        progressbars[0] = (ProgressBar) findViewById(R.id.progressCH1);
        progressbars[1] = (ProgressBar) findViewById(R.id.progressCH2);
        progressbars[2] = (ProgressBar) findViewById(R.id.progressCH3);
        progressbars[3] = (ProgressBar) findViewById(R.id.progressCH4);
        progressbars[4] = (ProgressBar) findViewById(R.id.progressCH5);
        progressbars[5] = (ProgressBar) findViewById(R.id.progressCH6);
        
        l1 = (LinearLayout) findViewById(R.id.layoutLeft);
        l2 = (LinearLayout) findViewById(R.id.layoutRight);
        
        for(int i=0;i<6;i++)
        {
        	progressbars[i].setMax(1024);
        }
        
        helicopter= new Heli();
        
       
        
        buttonDelete =(Button) findViewById(R.id.buttonDelete);
        buttonToggle =(Button) findViewById(R.id.toggleviews);
        
        

        
        // get service
        mSerial = new FTDriver((UsbManager)getSystemService(Context.USB_SERVICE));
          
        // listen for new devices
        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mUsbReceiver, filter);
        
       
        
       
        l1.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			   
				String text;
				float X = (event.getX()-(float)v.getWidth()/2)/((float)v.getWidth()/2);
				float Y =  (event.getY()-(float)v.getHeight()/2)/((float)v.getHeight()/2);
				
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
			        // manage down press
			    	text="Down "+X+" "+Y;
			    }
			    else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			        // manage move
			    	text="Move "+X+" "+Y;
			    	
			    	int throttle=(int) (X*512+512);
			    	int yaw = (int)(Y*Y*Y*512+512);
			    	if(throttle>170 && throttle<852)
			    	{
			    		helicopter.setValue(0,throttle); //throttle
			    	}
			    	if(yaw>170 && yaw<852)
			    	{
			    		//helicopter.setValue(3,Y);
			    	}
			    }
			    else {
			        // manage any other MotionEvent
			    	text="Up "+X+" "+Y;
			    	helicopter.returnToNeutral();
			    }
			    
			    textOver.setText(text);
			    return true;
			}
		});
        
        
        l2.setOnTouchListener(new View.OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
			   
				String text;
				float X = (event.getX()-(float)v.getWidth()/2)/((float)v.getWidth()/2);
				float Y =  (event.getY()-(float)v.getHeight()/2)/((float)v.getHeight()/2);
				
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
			        // manage down press
			    	text="Down "+X+" "+Y;
			    }
			    else if (event.getAction() == MotionEvent.ACTION_MOVE) {
			        // manage move
			    	text="Move "+X+" "+Y;
			    	
			    	int pitch=(int) (Integer.signum((int) (X*1000))*X*X*512+512);
			    	int roll = (int)(Integer.signum((int) (Y*1000))*Y*Y*512+512);
			    	
			    	if(pitch>170 && pitch<852)
			    	{
			    	
			    		helicopter.setValue(2,pitch);
			    	}
			    	if(roll>170 && roll<852)
			    	{
			    		
			    		helicopter.setValue(1,1024-roll);
			    	}
			    }
			    else {
			        // manage any other MotionEvent
			    	text="Up "+X+" "+Y;
			    	helicopter.returnToNeutral();
			    }
			    
			    textOver2.setText(text);
			    return true;
			}
		});
        

        buttonDelete.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {   
    			
    			finish();
    			
    		}
        }); /**/
        
        buttonToggle.setOnClickListener(new View.OnClickListener() {
    		public void onClick(View v) {   
    			
    			if(view1.getVisibility()==View.INVISIBLE)
    			{
    				view2.setVisibility(View.INVISIBLE);
    				view1.setVisibility(View.VISIBLE);
    				
    			}else{
    				view1.setVisibility(View.INVISIBLE);
    				view2.setVisibility(View.VISIBLE);
    			}
    			
    		}
        }); /**/
        
        
        
        Log.i(TAG,"Starting main thread from OnCreate");
		 new Thread(mLoop,"Main Loop").start();/**/ //don't put this in OnStart, or it will
		 											//create another thread when it comes back from Settings
	

        
       
    }
    
    

    
    @Override
	protected void onStart(){	
		super.onStart();
		
		
	}
    
    @Override
	protected void onResume(){	
		super.onResume();
		
		
	}
    
    
	@Override
	protected void onPause(){	
		super.onPause();
	}
	
	@Override
	protected void onStop(){	
		super.onStop(); 
		
	
	}
	
	
	@Override
	protected void onRestart(){
		super.onRestart();
	
	}
	
    
    @Override
    public void onDestroy() {
    	
    	mStop=true;		
    	mSerial.end();		
       unregisterReceiver(mUsbReceiver);
		super.onDestroy();
    }
       
    
    
    public class Heli{
    	
    	private Channel _ch1; //throttle
    	private Channel _ch2; //roll - smaller is right
    	private Channel _ch3; //pitch - more is forward
    	private Channel _ch4; //yaw - smaller is right (on the stick)
    	private Channel _ch5;
    	private Channel _ch6;
    	
    	private byte[] _connectionBytes = new byte[]{(byte) 0x80,0x00,0x00,(byte) 0xAA,(byte) 0x05,(byte) 0xFF,(byte) 0x09,(byte) 0xFF,0x0D,(byte) 0xFF,(byte) 0x13, 0x54, 0x14, (byte) 0xAA};
    	
    	private Channel[] channels;
    	
    	public Heli()
    	{
    		_ch1 = new Channel(1,170);
    		_ch2 = new Channel(2,511);
    		_ch3 = new Channel(3,511);
    		_ch4 = new Channel(4,511);
    		_ch5 = new Channel(5,852);
    		_ch6 = new Channel(6,170);
    		
    		channels = new Channel[]{_ch1,_ch2,_ch3,_ch4,_ch5,_ch6};
    		
    	}
    	
    	public void setValue(int ch, int val)
    	{
    		channels[ch].setValue(val);    		
    	}
    	
    	public void returnToNeutral()
    	{
    		_ch1.setValue(170);
    		_ch2.setValue(511);
    		_ch3.setValue(511);
    		_ch4.setValue(511);
    		_ch5.setValue(511);
    		_ch6.setValue(511);
    		
    	}
    	
    	public byte[] bindingBytes()
    	{
    		return _connectionBytes;
    	}
    	
    	public byte[] getBytes()
    	{
    		byte[] b = new byte[14];
    		b[0]=0;
    		b[1]=0;
    		
    		for(int i=0;i<6;i++)
    		{
    			byte[] ch = ((Channel)channels[i]).getBytes();
    			b[i*2+2] = ch[0];
    			b[i*2+3] = ch[1];    			
    			
    		}
    		b[12]=(byte)0x14;
    		b[13]=(byte)0xAA;
    		
    		
    		return b;
    	}
    	
    	
    	
    	
    	public class Channel{
    		
    		private int _chan;
    		private int _val;
    		
    		public Channel(int chan, int value)
    		{
    			_chan=chan-1;
    			_val=value;    			
    		
    		}
    		
    		public void setValue (int v)
    		{
    			_val=v;
    			
    		}
    		public int getValue()
    		{
    			return _val;
    		}
    		public byte[] getBytes()
    		{
    			return(new byte[]{(byte)((_chan<<2)|(0xFF & (_val>>8))),(byte)(_val & 0xFF) });
    			
    		}
    		public short getShort()
    		{
    			return (short) ((short)(_chan<<10)|(_val & 0x3F));
    			
    		}
    		
    	}
    }
    
   
    
    
    

	
	private void updateText(String text)
	{
        textOver.setText(text);
	}
	
	private void updateValues()
	{
	    for(int i=0;i<6;i++)
        {
        	
        	textviews[i].setText("Ch"+(i+1)+" Val: "+values[i]);
        	progressbars[i].setProgress(values[i]);
        	
        }
		
		
	}
	
	private Runnable mLoop = new Runnable() {
		public void run() {		
			
			int i;
			int len;
			byte[] rbuf = new byte[4096];
			int chan;
			int value;
		
			
			
			mText="";
		
			
			while(true){
				
			Log.i(TAG,"mloop");			
			if(serialConnected==false) 
			{
				Log.i(TAG,"Connecting... ");
				
				if(mSerial.begin(FTDriver.BAUD125000))
				{
					Log.i(TAG,"Connected");
					serialConnected=true;
					mSerial.write(new byte[]{0},1);
					
					sleep(100);
					
					i=0;
					
					if(bind)
					while(i<400){
						
						mSerial.write(helicopter._connectionBytes,14);				
						sleep(15);
						i++;
						Log.i(TAG,"Binding... "+i);
					}
					i=0;
					len=0;
					
				
					dataPaused=false;
				}else{
					Log.i(TAG,"Connect failed!");
				}
				
				
				
			}else						
			{//this is the main loop for reading data
			
				
				
				mSerial.write(helicopter.getBytes(),14);
				
				
				
				len = mSerial.read(rbuf);
				
				//Log.i(TAG,"Length: "+len);
				
				
			
				
				if(len>=14)
					{
				
					 if(rbuf[0]==0 && rbuf[1]==0 && (int)rbuf[12]==20 && ((int)rbuf[13]&0xFF)==170)
					    {
							for (i=2;i<14;i+=2) {
								
								chan=(rbuf[i]>>2);
								value=0x3FF&((((int)rbuf[i]&0x3)<<8)|(0xFF&(int)rbuf[i+1]));
								//value=(int)rbuf[i+1];
								
								if(value<0)
								{
									Log.i(TAG,"value: "+value);
								}
								
								values[chan]=value;
								
							   // hexString.append("Chan: "+chan+" "+value+"\n");
							    }
								
								//mText = hexString.toString();
									
									mHandler.post(new Runnable() {
										public void run() {
											updateValues();
										}
									});
							}else{
								StringBuffer hexString = new StringBuffer();
							 	for(i=0;i<14;i++){								 
								 hexString.append(Integer.toHexString((int)rbuf[i] & 0xFF)+" "); 
							 	}
							 	mText=hexString.toString();
							 	
							 	mHandler.post(new Runnable() {
									public void run() {
										updateText(mText);
									}
								});
							 	
							}
					 
					}

					
				   
						
			sleep(20);
				
				  
				
				
			}
				
				if(mStop) {
				
						return;
				}
			
			
			}
			
		}
	};
	

	
	public void sleep(int i)
	{
		try {
			Thread.sleep(i);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	  // ignore orientation/keyboard change
	  super.onConfigurationChanged(newConfig);
	}
	
    // BroadcastReceiver when insert/remove the device USB plug into/from a USB port  
    BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
    		String action = intent.getAction();
    		if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
    			mSerial.usbAttached(intent);
    			
				
    		} else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
    			mSerial.usbDetached(intent);
    			mSerial.end();    			
				serialConnected=false;			
				mStopped=false;
				mStop=false;	
				dataPaused=true;
				
    			
    		}
        }
    };
}
