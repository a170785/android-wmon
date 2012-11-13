package com.gnychis.awmon.InterfaceScanners;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.gnychis.awmon.DeviceAbstraction.Interface;
import com.gnychis.awmon.HardwareHandlers.HardwareHandler;
import com.gnychis.awmon.HardwareHandlers.InternalRadio;
import com.gnychis.awmon.NameResolution.NameResolutionManager;

// The purpose of this class is to keep track of a scan taking place across
// all of the protocols.  That way, we can cache results and determine when
// each of the protocols has been scanned for.
public class InterfaceScanManager extends Activity { 
	
	private static final String TAG = "DeviceScanManager";
	
	private static final boolean OVERLAP_SCANS = true;
	
	public static final String INTERFACE_SCAN_REQUEST = "awmon.scanmanager.interface_scan_request";
	public static final String INTERFACE_SCAN_RESULT = "awmon.scanmanager.interface_scan_result";

	HardwareHandler _device_handler;
	NameResolutionManager _nameResolutionManager;
	ArrayList<Interface> _interfaceScanResults;
	Queue<InternalRadio> _scanQueue;
	Queue<Class<?>> _pendingResults;
	
	State _state;
	public enum State {
		IDLE,
		SCANNING,
	}

	public InterfaceScanManager(HardwareHandler dh) {
		_device_handler=dh;
		_nameResolutionManager = new NameResolutionManager(_device_handler._parent);
		_state = State.IDLE;
		
		// Register a receiver to handle the incoming scan requests
        _device_handler._parent.registerReceiver(new BroadcastReceiver()
        { @Override public void onReceive(Context context, Intent intent) { scanRequest(); }
        }, new IntentFilter(INTERFACE_SCAN_REQUEST));
        
        _device_handler._parent.registerReceiver(incomingInterfaceScanResult, new IntentFilter(InterfaceScanner.HW_SCAN_RESULT));
	}
	
	// On a scan request, we check for the hardware devices connected and then
	// put them in a queue which we will trigger scans on.
	public void scanRequest() {
		Log.d(TAG, "Receiving an incoming scanRequest()");
		if(_state!=State.IDLE)
			return;
		
		// Set the state to scanning, then clear the scan results.
		_state = State.SCANNING;
		_interfaceScanResults = new ArrayList<Interface>();
		
		// Put all of the devices in a queue that we will scan devices on
		_scanQueue = new LinkedList < InternalRadio >();
		_pendingResults = new LinkedList < Class<?> >();
		for (InternalRadio hwDev : _device_handler._internalRadios) {
			if(hwDev.isConnected()) { 
				_scanQueue.add(hwDev);
				_pendingResults.add(hwDev.getClass());
			}
		}
		
		// Start the chain of device scans by triggering one of them
		triggerNextInterfaceScan();
	}
	
	// To trigger the next scan, we pull the next device from the queue.  If there are no
	// devices left, the scan is complete.
	public void triggerNextInterfaceScan() {
		if(_scanQueue.isEmpty())
			return;
		
		InternalRadio dev = _scanQueue.remove();
		dev.startDeviceScan();
		
		if(OVERLAP_SCANS)				// If we are overlapping scans, just go ahead and
			triggerNextInterfaceScan();	// trigger the next one.
	}
	
	// When the scan is complete, we send out a broadcast with the results.
	public void interfaceScanComplete() {
		Intent i = new Intent();
		i.setAction(INTERFACE_SCAN_RESULT);
		i.putExtra("result", _interfaceScanResults);
		_device_handler._parent.sendBroadcast(i);
		_state=State.IDLE;
	}
	
    // A broadcast receiver to get messages from background service and threads
    private BroadcastReceiver incomingInterfaceScanResult = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
        	InterfaceScanResult scanResult = (InterfaceScanResult) intent.getExtras().get("result");
        	Class<?> ifaceType = InternalRadio.deviceType((String)intent.getExtras().get("hwType")); 
        	for(Interface iface : scanResult._interfaces) 
        		_interfaceScanResults.add(iface);
        	
        	if(!OVERLAP_SCANS)				// If we are not overlapping scans, we do it when we get
        		triggerNextInterfaceScan();	// results of the previous scan
        	
        	// If we have all of the results we need, we can set it to complete
        	_pendingResults.remove(ifaceType);
        	if(_pendingResults.size()==0)
        		interfaceScanComplete();
        }
    }; 
}