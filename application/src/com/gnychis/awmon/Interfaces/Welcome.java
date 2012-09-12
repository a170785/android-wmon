package com.gnychis.awmon.Interfaces;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHeader;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;

import com.gnychis.awmon.R;
import com.gnychis.awmon.Core.UserSettings;
import com.nullwire.trace.ExceptionHandler;

public class Welcome extends Activity {
	
    Spinner netlist, agelist;
	private UserSettings _settings;
	WifiManager _wifi;
	boolean _reverse_sort;

	@Override
	public void onCreate(Bundle savedInstanceState) {
	  super.onCreate(savedInstanceState);
	  setContentView(R.layout.welcome);
	 
	  _settings = new UserSettings(this);	// Get a handle to the user settings
	  
	  ExceptionHandler.register(this, "http://moo.cmcl.cs.cmu.edu/pastudy/"); 
	  
	  _reverse_sort=false;
	  updateNetworkList();
      
      // Setup the age-range list and put it in a drop-down menu for them to select.
      agelist = (Spinner) findViewById(R.id.age_group);
      ArrayAdapter<CharSequence> ageAdapter = ArrayAdapter.createFromResource(this, R.array.age_ranges, android.R.layout.simple_spinner_item);
      ageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
      agelist.setAdapter(ageAdapter);
      
      // Set the checkboxes back to what the user had
      int ar = _settings.getAgeRange();
      if(ar!=-1)
    	  agelist.setSelection(ar);
      ((CheckBox) findViewById(R.id.kitchen)).setChecked(_settings.getSurveyKitchen());
      ((CheckBox) findViewById(R.id.bedroom)).setChecked(_settings.getSurveyBedroom());
      ((CheckBox) findViewById(R.id.livingRoom)).setChecked(_settings.getSurveyLivingRoom());
      ((CheckBox) findViewById(R.id.bathroom)).setChecked(_settings.getSurveyBathroom());
      
      // Test if they have GPS enabled or disabled.  If it's disabled, we ask them to enable it to participate
      String provider = Settings.Secure.getString(getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
      if (!provider.contains("network"))
      	buildAlertMessageNoGps();  
	}
	
	public void updateNetworkList() {
		  // Create an instance to the Wifi manager and get a list of networks that the user
		  // has associated to.  Pull this up as their list.
		  _wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	      List<WifiConfiguration> cfgNets = _wifi.getConfiguredNetworks();
	      netlist = (Spinner) findViewById(R.id.network_list);
	      ArrayList<String> spinnerArray = new ArrayList<String>();
	      if(!_reverse_sort)
	    	  Collections.sort(cfgNets,netsort);
	      else
	    	  Collections.sort(cfgNets,netsort_reverse);
	      for (WifiConfiguration config: cfgNets)
	      	spinnerArray.add(config.SSID.replaceAll("^\"|\"$", ""));
	      spinnerArray.add("* I don't see my home network! *");
	      ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, spinnerArray);
	      spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	      netlist.setAdapter(spinnerArrayAdapter);
	      String homeSSID = _settings.getHomeSSID();
	      if(homeSSID!=null)
	    	  netlist.setSelection(spinnerArray.indexOf(homeSSID));
	}
	
    // When the user clicks finished, we save some information locally.  The home network name is
    // only saved locally (so that our application can work), and it is never shared back with us.
    public void clickedFinished(View v) {
    	String home_ssid = (String) netlist.getSelectedItem();
    	
    	// If they do not see their home network, we ask them to first associate the phone to
    	// their home network.
    	if(home_ssid.equalsIgnoreCase("* I don't see my home network! *")) {
    		buildAlertMessageNoNetwork();
    		return;
    	}
    	
    	// Save their settings and set it to initialized
    	_settings.setHomeSSID(home_ssid);
    	_settings.setSurvey((int) agelist.getSelectedItemId(),
    						((CheckBox) findViewById(R.id.kitchen)).isChecked(),
    						((CheckBox) findViewById(R.id.bedroom)).isChecked(),
    						((CheckBox) findViewById(R.id.livingRoom)).isChecked(),
    						((CheckBox) findViewById(R.id.bathroom)).isChecked());
    	//_settings.setHaveUserSettings();
    	finish();
    }
    
    @Override
    public void onBackPressed() {
    }
	
    private void buildAlertMessageNoGps() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your location services must be enabled for our study to work, do you want to enable them?\n\nIf you choose YES, you only need to enable 'Use wireless networks' and then click your back button.")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                	   startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       	dialog.cancel();
                       	finish();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    
    private void buildAlertMessageNoNetwork() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Your phone must first be associated to your home wireless network.  Would you like to do this now?")
               .setCancelable(false)
               .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                   public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                	   _reverse_sort=true;
                	   startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
                   }
               })
               .setNegativeButton("No", new DialogInterface.OnClickListener() {
                   public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                       	dialog.cancel();
                   }
               });
        final AlertDialog alert = builder.create();
        alert.show();
    }
    
    // For converting an incoming input stream to a string
    public static String convertStreamToString(InputStream is) {
  	    BufferedReader reader = new BufferedReader(new InputStreamReader(is));
  	    StringBuilder sb = new StringBuilder();
  	    String line = null;
  	    try {
  	        while ((line = reader.readLine()) != null) {
  	            sb.append(line + "\n");
  	        }
  	    } catch (IOException e) {
  	        e.printStackTrace();
  	    } finally {
  	        try {
  	            is.close();
  	        } catch (IOException e) {
  	            e.printStackTrace();
  	        }
  	    }
  	    return sb.toString();
  	}
	
    // This is a comparator to sort the networks on your phone, so that your home network is
    // more likely to be at the top of the list.
    Comparator<Object> netsort = new Comparator<Object>() {
    	public int compare(Object arg0, Object arg1) {
    		if(((WifiConfiguration)arg0).priority > ((WifiConfiguration)arg1).priority)
    			return 1;
    		else if( ((WifiConfiguration)arg0).priority < ((WifiConfiguration)arg1).priority)
    			return -1;
    		else
    			return 0;
    	}
      };
      
      Comparator<Object> netsort_reverse = new Comparator<Object>() {
      	public int compare(Object arg0, Object arg1) {
    		if(((WifiConfiguration)arg0).priority < ((WifiConfiguration)arg1).priority)
    			return 1;
    		else if( ((WifiConfiguration)arg0).priority > ((WifiConfiguration)arg1).priority)
    			return -1;
    		else
    			return 0;

      	}
      };
      
      @Override
      public void onPause() { super.onPause(); Log.d("AWMonWelcome", "onPause()"); }
      @Override
      public void onResume() { super.onResume(); 
      	Log.d("AWMonWelcome", "onResume()");
      	updateNetworkList();
      }
      
}
