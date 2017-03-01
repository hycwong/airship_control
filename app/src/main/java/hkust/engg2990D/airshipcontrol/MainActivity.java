package hkust.engg2990D.airshipcontrol;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends Activity implements OnClickListener, OnCheckedChangeListener, SeekBar.OnSeekBarChangeListener, View.OnTouchListener {
	
	// This is the textview where we display all the status messages
	TextView mtxtStatus;
    TextView mtxtRealSpeed;
	
	// This is the reference to the Stop Button on the UI. If you include other similar image
	// buttons in your UI, you can use the same method as used for stop button. Just choose the
	// appropriate image, get a reference to the button in the onCreate() method. Then declare
	// the appropriate onClick() listener code in the onClick() method.
	ImageButton mbtnStop;
    ImageButton mbtnStopH;
    ImageButton mbtnFor;
    ImageButton mbtnBack;
    ImageButton mbtnLeft;
    ImageButton mbtnRight;
    ImageButton mbtnUpward;
    ImageButton mbtnDownward;
    ImageButton mbtnTrimUp;
    ImageButton mbtnTrimDown;

	
	// This is the Bluetooth On/Off Switch
	Switch mswConn;
	
	// These are two other switches that control Pin 7 and Pin 8. They could be used
	// to turn the electromagnet on and off, depending on which pin you attach the electromagnet.
	// Use the appropriate switch.
	Switch mswSwitch1;
	Switch mswSwitch2;
	
	// The spinner is used to select a specific motor and set its speed.
	//Spinner mspMotor;
	//ArrayAdapter<String> motorsAdapter;
	
	// This value is initialized to the currently selected motor. This variable
	// takes on values from 0 .. 5 corresponding to Motor 1 .. Motor 6. Hence if
	// you need to get the motor number, add 1 to this variable.
	//int currentMotorIndex;
	
	SeekBar mseekBarMotorSpeed;
	
	// The maximum and minimum speeds of the motors. This is used to set the range of
	// the seek bar (from -MAX_DUTY_CYCLE to +MAX_DUTY_CYCLE). The whole range of the
	// speed is 2*MAX_DUTY_CYCLE. The seekbar position value ranges from 0 .. maximum value.
	// The maximum value of the seekbar is set to 2*MAX_DUTY_CYCLE. Thus the centre point will
	// be at MAX_DUTY_CYCLE corresponding to a motor speed of 0.
	final int MIN_DUTY_CYCLE = 0;
	final int MAX_DUTY_CYCLE = 200;
	
	// This array keeps track of the seekbar position for each motor
	// set this value in the onProgressChanged() method of the seekbar
	// DO seekBarPos[currentMotorIndex] = progress;
    int seekBarPosition = MAX_DUTY_CYCLE/2;
	
	// This array keeps track of the motor speeds for each of the six motors.
	// set this value in the onProgressChanged() method of the seekbar
	// DO motorSpeed[currentMotorIndex] = (progress - MAX_DUTY_CYCLE);
	int [] motorSpeed = {MIN_DUTY_CYCLE, MIN_DUTY_CYCLE, MIN_DUTY_CYCLE, MIN_DUTY_CYCLE, MIN_DUTY_CYCLE, MIN_DUTY_CYCLE};
	
	TextView mspeedMin, mspeedMax, mspeedMid;
	
	/*
	 * Controller:
	 * 
	 *    The controller object gives us the right Application Programmer's Interface (API) to
	 *    manage the connection to the Arduino board through Bluetooth to issue commands to the
	 *    motors. This class exposes the following methods which you can call to control the motors
	 *    attached to the Arduino board
	 *    
	 *    Connect(): Turn on the Bluetooth Connection and establish connection to a device
	 *    
	 *    Disconnect(): Turn off the Bluetooth Connection
	 *    
	 *    IsConnected(): Is the Bluetooth connection on (returns true) or off (returns false)
	 *    
	 *    AutoStartBT(): Automatically enable the Bluetooth adapter so that we can establish connection. 
	 *         This is done at the start.
	 *    
	 *    AllStop(): Stop all the motors and set their speeds to zero
	 *    
	 *    MotorStart(int motorindex, int velocity): set the speed of the motor "motorindex" to "velocity"
	 *         In this case "motorindex" goes from 0 .. 5 corresponding to Motor 1 .. Motor 6
	 *         velocity ranges from -MAX_DUTY_CYCLE .. MAX_DUTY_CYCLE
	 *    
	 *    SwitchChange(int index, int state): Turn on/off the selected switch "index" to the "state"
	 *         Here "index" is either controller.SWITCH1 or controller.SWITCH2
	 *         "state" is either controller.ON or controller.OFF
	 *    
	 *    
	 */
	Controller controller;

    /*****Bluetooth protection timer and timer task begins*****/
    //To monitor the bluetooth connection
    Timer timer = new Timer(true);


    // Bluetooth connection protection
    public class timerTask extends TimerTask {
        public void run(){
            if(controller.isBTSignal == false){	// if no motor signal is currently sending
                controller.MotorStart(7, 0);
                //Log.v("Timer working", "Done!!");
            }
        }
    };
    /*****Bluetooth protection timer and timer task ends*****/

    boolean Maintain = false;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //Send bluetooth signal to Arduino in a fix period of time
        timer.schedule(new timerTask(), 1000, 100);

        // This sets the User Interface to the layout designed in activity_main.xml file
		setContentView(R.layout.activity_main);
		
		// create the controller object
		controller = new Controller(this);
		
		// check if bluetooth is available and turn it on automatically
		if(!controller.AutoStartBT()){			
			return;
		}
		
		// get a reference to the status textview
		mtxtStatus = (TextView) findViewById(R.id.txtStatus);
        mtxtRealSpeed = (TextView) findViewById(R.id.txtRealSpeed);
		
		// get a reference to the stop button and set it to be disabled
		// set the onclick listener for the button.
		// You can use the same procedure to include other image buttons on the UI
		mbtnStop = (ImageButton) findViewById(R.id.btnStop);
		mbtnStop.setOnClickListener(this);
		mbtnStop.setEnabled(Maintain);

        mbtnStopH = (ImageButton) findViewById(R.id.btnStopH);
        mbtnStopH.setOnClickListener(this);
        mbtnStopH.setOnTouchListener(this);
        mbtnStopH.setEnabled(Maintain);

        mbtnFor = (ImageButton) findViewById(R.id.btnFor);
        mbtnFor.setOnClickListener(this);
        mbtnFor.setOnTouchListener(this);
        mbtnFor.setEnabled(Maintain);

        mbtnBack = (ImageButton) findViewById(R.id.btnBack);
        mbtnBack.setOnClickListener(this);
        mbtnBack.setOnTouchListener(this);
        mbtnBack.setEnabled(Maintain);

        mbtnLeft = (ImageButton) findViewById(R.id.btnLeft);
        mbtnLeft.setOnClickListener(this);
        mbtnLeft.setOnTouchListener(this);
        mbtnLeft.setEnabled(Maintain);

        mbtnRight = (ImageButton) findViewById(R.id.btnRight);
        mbtnRight.setOnClickListener(this);
        mbtnRight.setOnTouchListener(this);
        mbtnRight.setEnabled(Maintain);

        mbtnUpward = (ImageButton) findViewById(R.id.btnUpward);
        mbtnUpward.setOnClickListener(this);
        mbtnUpward.setOnTouchListener(this);
        mbtnUpward.setEnabled(Maintain);

        mbtnDownward = (ImageButton) findViewById(R.id.btnDownward);
        mbtnDownward.setOnClickListener(this);
        mbtnDownward.setOnTouchListener(this);
        mbtnDownward.setEnabled(Maintain);

        mbtnTrimUp = (ImageButton) findViewById(R.id.btnTrimUp);
        mbtnTrimUp.setOnClickListener(this);
        mbtnTrimUp.setEnabled(Maintain);

        mbtnTrimDown = (ImageButton) findViewById(R.id.btnTrimDown);
        mbtnTrimDown.setOnClickListener(this);
        mbtnTrimDown.setEnabled(Maintain);

       		// get a reference to the switches and st them to be disabled. set their on checked change listeners
		mswConn = (Switch) findViewById(R.id.swConn);
		mswConn.setOnCheckedChangeListener(this);
		mswSwitch1 = (Switch) findViewById(R.id.swSwitch1);
		mswSwitch1.setEnabled(Maintain);
		mswSwitch2 = (Switch) findViewById(R.id.swSwitch2);
		mswSwitch2.setEnabled(Maintain);
		mswSwitch1.setOnCheckedChangeListener(this);
		mswSwitch2.setOnCheckedChangeListener(this);
		
		//get a reference to the spinner
		//mspMotor = (Spinner) findViewById(R.id.spMotor);
		//motorsAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item);
		//motorsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		//for (int i=1; i <= 6; i++) {
		//	motorsAdapter.add("Motor "+ i);
		//}
		//mspMotor.setAdapter(motorsAdapter);
		//mspMotor.setEnabled(false);
		//mspMotor.setOnItemSelectedListener(this);
		
		// Recall that the motor speed ranges from -MAX_DUTY_CYCLE to MAX_DUTY_CYCLE.
		// so the whole range is 2*MAX_DUTY_CYCLE. The seek bar position is always positive
		// Thus the seek bar position goes from 0 .. 2*MAX_DUTY_CYCLE
		mseekBarMotorSpeed = (SeekBar) findViewById(R.id.seekBarMotorSpeed);
		mseekBarMotorSpeed.setEnabled(Maintain);
		mseekBarMotorSpeed.setMax(MAX_DUTY_CYCLE*3/2);
		mseekBarMotorSpeed.setProgress(MAX_DUTY_CYCLE/2);
		mseekBarMotorSpeed.setOnSeekBarChangeListener(this);
		
		// These three textviews are used to show the scale indicator below the seekbar
		mspeedMin = (TextView) findViewById(R.id.speedMin);
		mspeedMid = (TextView) findViewById(R.id.speedMid);
		mspeedMax = (TextView) findViewById(R.id.speedMax);
		
		Integer i = MAX_DUTY_CYCLE;
		mspeedMin.setText("-" + MAX_DUTY_CYCLE/2);
		mspeedMid.setText("0");
		mspeedMax.setText(i.toString());

        mtxtRealSpeed.setText("a: "+motorSpeed[a]+" b: "+motorSpeed[b]+" c: "+motorSpeed[c]+" d: "+motorSpeed[d]+" e: "+motorSpeed[e]+" f: "+motorSpeed[f]);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}


    int [] motorDirection = {1, -1, 1, -1, 1, -1};
    int a = 0;
    int b = 1;
    int c = 2;
    int d = 3;
    int e = 4;
    int f = 5;
    int x_speed1 = 170;
    int x_speed1b = 170;
    int x_speed2 = 30;
    int tune = 53;  //B&D's speed   /\ -> More Righr    \/-> More Left
    int tune1 = 105 ; //F's speed /\ -> More Left \/ -> More Right
    int djiSpeed = 50;


	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		
		switch (v.getId()) {
		
		// if you add more image buttons to the UI, include similar case statements
		// for those buttons here and include the code before the break statement.
		// Don't forget to include the break statement at the end of each section
		// of the code. This is a major source of bugs in the code
		case R.id.btnStop:
		    mtxtStatus.setText("Stop All Motors");
				
				// This statement is used to stop all the motors and set their speeds to 0
			controller.AllStop();
            for(int i=0; i<6; i++) motorSpeed[i] = 0;
            mseekBarMotorSpeed.setProgress(MAX_DUTY_CYCLE/2);
            mtxtRealSpeed.setText("a: "+motorSpeed[a]+" b: "+motorSpeed[b]+" c: "+motorSpeed[c]+" d: "+motorSpeed[d]+" e: "+motorSpeed[e]+" f: "+motorSpeed[f]);
			break;


        case R.id.btnTrimUp:
            for(int i=0;i<4;i++) {
                if ( (motorDirection[i]>0 && motorSpeed[i]>=150 ) || (motorDirection[i]<0 && motorSpeed[i]<= -150) ) return;
            }
            mtxtStatus.setText("Trim Up by 5");
                    for(int i=0;i<4;i++) motorVary(i,5);
            seekBarPosition+=5;
            mseekBarMotorSpeed.setProgress(seekBarPosition);
            break;

        case R.id.btnTrimDown:
        for(int i=0;i<4;i++) {
            if ((motorDirection[i]>0 && motorSpeed[i]<= -75 ) || (motorDirection[i]<0 && motorSpeed[i]>= 75 ) ) return;
        }
            mtxtStatus.setText("Trim Down by 5");
            for(int i=0;i<4;i++) motorVary(i,-5);
            seekBarPosition-=5;
            mseekBarMotorSpeed.setProgress(seekBarPosition);
            break;

		default:
			break;
		}
		
	}

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {

            case R.id.btnFor:                                               //On touch -- For
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //press
                    motorStart(e, x_speed1 );
                    motorStart(f, x_speed1 *tune1/100 );

                    motorVary(a,5);
                    motorVary(b,5);
                    motorVary(c,5);
                    motorVary(d,5);

                    mtxtStatus.setText("Forwarding with speed "+x_speed1);
                    return true;
                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //release
                    motorStart(e, -(x_speed1*2) );
                    motorStart(f, -(x_speed1*2) );
                    controller.delay(100);
                    mtxtStatus.setText("Back to default speed");

                    defaultHori();
                    motorVary(a,-5);
                    motorVary(b,-5);
                    motorVary(c,-5);
                    motorVary(d,-5);

                    return true;

                }
                break;

            case R.id.btnBack:                                            //On touch -- Back
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //press
                    motorStart(e, -(x_speed1b) );
                    motorStart(f, -(x_speed1b*tune1/100) );

                    motorVary(a,10);
                    motorVary(b,10);
                    motorVary(c,10);
                    motorVary(d,10);

                    mtxtStatus.setText("Backwarding with speed "+x_speed1);
                    return true;

                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //release
                    motorStart(e, x_speed1b*2 );
                    motorStart(f, x_speed1b*2 );
                    controller.delay(100);
                    mtxtStatus.setText("Back to default speed");

                    defaultHori();
                    motorVary(a,-10);
                    motorVary(b,-10);
                    motorVary(c,-10);
                    motorVary(d,-10);

                }
                break;

            case R.id.btnLeft:                                                    //On touch -- Left
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //press
                    motorStart(e, -x_speed2);
                    motorStart(f, x_speed2);
                    mtxtStatus.setText("turning Left with speed "+x_speed2);
                    return true;

                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //release
                    motorStart(e, x_speed2*2 );
                    motorStart(f, -(x_speed2*2) );
                    controller.delay(100);
                    mtxtStatus.setText("Back to default speed");
                    defaultHori();
                    return true;

                }
                break;

            case R.id.btnRight:                                                   //On touch -- Right
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //press
                    motorStart(e, x_speed2 );
                    motorStart(f, -x_speed2 );
                    mtxtStatus.setText("turning Right with speed "+x_speed2);
                    return true;

                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //release
                    motorStart(e, -(x_speed2*2) );
                    motorStart(f, x_speed2*2 );
                    controller.delay(100);
                    mtxtStatus.setText("Back to default speed");
                    defaultHori();
                    return true;

                }
                break;

            case R.id.btnUpward:                                             //On touch -- Drift Left
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //press
                    motorVary(a,djiSpeed);
                    motorVary(b,djiSpeed);

                    motorVary(c,-djiSpeed);
                    motorVary(d,-djiSpeed);
                    mtxtStatus.setText("DJI Leftward with speed " + djiSpeed);
                    return true;

                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //release
                    motorVary(c,djiSpeed*2);
                    motorVary(d,djiSpeed*2);

                    motorVary(a,-djiSpeed*2);
                    motorVary(b,-djiSpeed*2);

                    controller.delay(400);

                    mtxtStatus.setText("Back to default speed");
                    motorVary(a,djiSpeed);
                    motorVary(b,djiSpeed);

                    motorVary(c,-djiSpeed);
                    motorVary(d,-djiSpeed);
                    return true;

                }
                break;

            case R.id.btnDownward:                                           //On touch -- Drift Right
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //press
                    motorVary(c,djiSpeed);
                    motorVary(d,djiSpeed);

                    motorVary(a,-djiSpeed);
                    motorVary(b,-djiSpeed);
                    mtxtStatus.setText("DJI Rightward with speed " + djiSpeed);
                    return true;

                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //release
                    motorVary(a,djiSpeed*2);
                    motorVary(b,djiSpeed*2);

                    motorVary(c,-djiSpeed*2);
                    motorVary(d,-djiSpeed*2);

                    controller.delay(400);

                    mtxtStatus.setText("Back to default speed");
                    motorVary(c,djiSpeed);
                    motorVary(d,djiSpeed);

                    motorVary(a,-djiSpeed);
                    motorVary(b,-djiSpeed);
                    return true;

                }
                break;

            case R.id.btnStopH:
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //press
                    motorStart(a, abs(motorSpeed[a])/5);
                    motorStart(b, abs(motorSpeed[b])/5);
                    motorStart(c, abs(motorSpeed[c])/5);
                    motorStart(d, abs(motorSpeed[d])/5);
                    mtxtStatus.setText("Falling");
                    return true;

                }
                else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //release

                    mtxtStatus.setText("Back to default speed");
                    motorStart(a, abs(motorSpeed[a])*5*3/2);
                    motorStart(b, abs(motorSpeed[b])*5*3/2);
                    motorStart(c, abs(motorSpeed[c])*5*3/2);
                    motorStart(d, abs(motorSpeed[d])*5*3/2);

                    controller.delay(400);

                    motorStart(a, abs(motorSpeed[a])*2/3);
                    motorStart(b, abs(motorSpeed[b])*2/3);
                    motorStart(c, abs(motorSpeed[c])*2/3);
                    motorStart(d, abs(motorSpeed[d])*2/3);

                    return true;

                }
                break;

        }

        return false;
    }

    public void defaultHori() {
        motorStart(e, 0);
        motorStart(f, 0);
    }

	@Override
	public void onCheckedChanged(CompoundButton btn, boolean set) {

		if (btn == mswConn && set) {
			// If the bluetooth button is turned on, then execute the code below
			
			// create asyncTask for the user interaction
			// DO NOT disturb this code.
			class EnableUI extends AsyncTask<Void, Void, Void>{

				protected void onPreExecute(){
					
					// show a paired devices list and wait for the user to select a device 
					// by pressing a selection for connection
					controller.Connect();	
				}
				
				protected void onPostExecute(Void result){
					
					// turn on UI after connected
					mbtnStop.setEnabled(true);
                    mbtnStopH.setEnabled(true);
                    mbtnFor.setEnabled(true);
                    mbtnBack.setEnabled(true);
                    mbtnLeft.setEnabled(true);
                    mbtnRight.setEnabled(true);
                    mbtnUpward.setEnabled(true);
                    mbtnDownward.setEnabled(true);
                    mbtnTrimUp.setEnabled(true);
                    mbtnTrimDown.setEnabled(true);
					//mspMotor.setEnabled(true);
					mseekBarMotorSpeed.setEnabled(true);
					mswSwitch1.setEnabled(true);
					mswSwitch2.setEnabled(true);
				}
				@Override
				protected Void doInBackground(Void... params) {
					
					// check if it is connected
					while(!controller.IsConnected());
					return null;
				}				
			}
			
			EnableUI enableUI = new  EnableUI();
			enableUI.execute();
			mtxtStatus.setText("Bluetooth Turned On!");

		}
		else if (btn == mswConn && !set) {
			// If the bluetooth button is turned off, then execute the code below
			
			// create asyncTask for the user interaction
			// DO NOT disturb this code.
			class DisableUI extends AsyncTask<Void, Void, Void>{


				protected void onPreExecute(){
					
					// disconnecting
					controller.Disconnect();	
				}
				
				protected void onPostExecute(Void result){
					
					// turn off UI after disconnected
					mbtnStop.setEnabled(false);
                    mbtnStopH.setEnabled(false);
                    mbtnFor.setEnabled(false);
                    mbtnBack.setEnabled(false);
                    mbtnLeft.setEnabled(false);
                    mbtnRight.setEnabled(false);
                    mbtnUpward.setEnabled(false);
                    mbtnDownward.setEnabled(false);
                    mbtnTrimUp.setEnabled(false);
                    mbtnTrimDown.setEnabled(false);
					//mspMotor.setEnabled(false);
					mseekBarMotorSpeed.setEnabled(false);
					mswSwitch1.setEnabled(false);
					mswSwitch2.setEnabled(false);

				}
				@Override
				protected Void doInBackground(Void... params) {

					// check if it is disconnected
					while(controller.IsConnected());
					return null;
				}				
			}
			
			DisableUI disableUI = new  DisableUI();
			disableUI.execute();
			mtxtStatus.setText("Bluetooth Turned Off!");
		}
		else if (btn == mswSwitch1 && set) {
			// if the user switches on Switch1
			// set pin 7 to high
			controller.SwitchChange(controller.SWITCH1, controller.ON);
			mtxtStatus.setText("Switch 1 Turned On!");
		}
		else if (btn == mswSwitch1 && !set) {
			// if the user switches off Switch1
			// set pin 7 to low
			controller.SwitchChange(controller.SWITCH1, controller.OFF);		
			mtxtStatus.setText("Switch 1 Turned Off!");
		}
		else if (btn == mswSwitch2 && set) {
			// if the user switches on Switch2
			// set pin 8 to high
			controller.SwitchChange(controller.SWITCH2, controller.ON);
			mtxtStatus.setText("Switch 2 Turned On!");
		}
		else if (btn == mswSwitch2 && !set) {
			// if the user switches off Switch2
			// set pin 8 to low
			controller.SwitchChange(controller.SWITCH2, controller.OFF);
			mtxtStatus.setText("Switch 2 Turned Off!");
		
		}
	}

	//@Override
	/*public void onItemSelected(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		
		// position indicated which position of the spinner. By default
		// position starts at 0. Remember, Motor 1 is in position 0 on
		// the spinner. So to get the motor number I have to add 1 to
		// position
		
		currentMotorIndex = position;
		
		mtxtStatus.setText("Motor "+(currentMotorIndex+1) + " selected");
        mtxtRealSpeed.setText("a: "+motorSpeed[a]+" b: "+motorSpeed[b]+" c: "+motorSpeed[c]+" d: "+motorSpeed[d]+" e: "+motorSpeed[e]+" f: "+motorSpeed[f]);
		
	}

	@Override
	public void onNothingSelected(AdapterView<?> arg0) {
		// TODO Auto-generated method stub
		
	}
*/

	@Override

	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		// TODO Auto-generated method stub

		if((progress-(MAX_DUTY_CYCLE/2))<10 && (progress-(MAX_DUTY_CYCLE/2))>-10){progress=MAX_DUTY_CYCLE/2;}
        int y_speed = (progress-(MAX_DUTY_CYCLE/2));
		mtxtStatus.setText("Vertical Speed set to "+ y_speed);
        seekBarPosition = progress;


		// start the motor selected by the spinner at the speed specified by the seekbar
		// Note that we identify Motor 1 as 0, Motor 2 as 1 and so on.6
		motorStart(a, y_speed);
        motorStart(b, y_speed*tune/100 );
        motorStart(c, y_speed);
        motorStart(d, y_speed*tune/100 );
        /*if(y_speed>=135){
            motorStart(e, (y_speed-110)*1/5 );
            motorStart(f, -(y_speed-110)*1/5 );
        }
        else{
            motorStart(e, 0 );
            motorStart(f, 0 );
        }*/
        //motorStart(e, -y_speed*tune1/100 );
        //motorStart(f, y_speed*tune1/100 );
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub
		
	}

    //JACK typed
    public void motorStart(int index, int speed){
        if(speed > MAX_DUTY_CYCLE ) speed =MAX_DUTY_CYCLE;
        if(speed< (-MAX_DUTY_CYCLE/2) && index<4 ) speed= -MAX_DUTY_CYCLE/2;

        controller.MotorStart(index, motorDirection[index]*speed );
        motorSpeed[index] =motorDirection[index]*speed;

        mtxtRealSpeed.setText("a: "+motorSpeed[a]+" b: "+motorSpeed[b]+" c: "+motorSpeed[c]+" d: "+motorSpeed[d]+" e: "+motorSpeed[e]+" f: "+motorSpeed[f]);
    }

    public void motorVary(int index, int addSpeed){
        if(motorDirection[index] >0) {
            if (motorSpeed[index] + motorDirection[index] * addSpeed > MAX_DUTY_CYCLE)
                addSpeed = MAX_DUTY_CYCLE - motorSpeed[index];
            if (motorSpeed[index] + motorDirection[index] * addSpeed < (-MAX_DUTY_CYCLE / 2))
                addSpeed = motorSpeed[index] - (-MAX_DUTY_CYCLE / 2);
        }
        else if(motorDirection[index] <0){
            if (motorSpeed[index] + motorDirection[index] * addSpeed < -MAX_DUTY_CYCLE)
                addSpeed = motorSpeed[index] - (-MAX_DUTY_CYCLE);
            if (motorSpeed[index] + motorDirection[index] * addSpeed > MAX_DUTY_CYCLE/2 )
                addSpeed = (MAX_DUTY_CYCLE / 2) - motorSpeed[index];
        }
        controller.MotorStart(index, (motorSpeed[index] + motorDirection[index]*addSpeed) );
        motorSpeed[index] =(motorSpeed[index] + motorDirection[index]*addSpeed);

        mtxtRealSpeed.setText("a: "+motorSpeed[a]+" b: "+motorSpeed[b]+" c: "+motorSpeed[c]+" d: "+motorSpeed[d]+" e: "+motorSpeed[e]+" f: "+motorSpeed[f]);
    }

    public int abs(int value){
        if(value<0) return -value;
        else return value;
    }

}
