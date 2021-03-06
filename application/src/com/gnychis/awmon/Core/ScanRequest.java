package com.gnychis.awmon.Core;

import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

import com.gnychis.awmon.BackgroundService.ScanManager;

public class ScanRequest implements Parcelable  {
	
	boolean _doNameResoution;
	boolean _doMerging;
	boolean _doFiltering;
	String _anchorMAC;
	String _snapshotName;

	public ScanRequest() {
		_doNameResoution=false;
		_doMerging=false;
		_doFiltering=false;
		_anchorMAC=null;
	}
	
	public void setSnapshotName(String name) {
		_snapshotName = name;
	}
	
	public String getSnapshotName() {
		return _snapshotName;
	}
	
	public String getAnchor() {
		return _anchorMAC;
	}
	
	public void setAnchor(String anchorMAC) {
		_anchorMAC = anchorMAC;
	}
	
	public void setNameResolution(boolean value) { _doNameResoution=value; }
	public void setMerging(boolean value) { _doMerging=value; }
	public boolean doNameResolution() { return _doNameResoution; }
	public boolean doMerging() { return _doMerging; }
	public void setFiltering(boolean value) { _doFiltering=value; }
	public boolean doFiltering() { return _doFiltering; }
	
	public void makeSnapshot() {
		_doNameResoution=false;
		_doMerging=false;
		_doFiltering=false;
	}
	
	public void send(Context parent) {
		Intent i = new Intent();
		i.setAction(ScanManager.SCAN_REQUEST);
		i.putExtra("request", this);
		parent.sendBroadcast(i);
	}
	
	// ********************************************************************* //
	// This code is to make this class parcelable and needs to be updated if
	// any new members are added to the Device class
	// ********************************************************************* //
	public int describeContents() {
		return this.hashCode();
	}

	public static final Parcelable.Creator<ScanRequest> CREATOR = new Parcelable.Creator<ScanRequest>() {
		public ScanRequest createFromParcel(Parcel in) {
			return new ScanRequest(in);
		}

		public ScanRequest[] newArray(int size) {
			return new ScanRequest[size];
		}
	};

	public void writeToParcel(Parcel dest, int parcelableFlags) {
		dest.writeInt(_doNameResoution ? 1 : 0 );
		dest.writeInt(_doMerging ? 1 : 0 );
		dest.writeInt(_doFiltering ? 1 : 0 );
		dest.writeString(_anchorMAC);
		dest.writeString(_snapshotName);
	}
	
	//@SuppressWarnings("unchecked")
	private ScanRequest(Parcel source) {
		_doNameResoution = ((source.readInt()==1) ? true : false );
		_doMerging = ((source.readInt()==1) ? true : false );
		_doFiltering = ((source.readInt()==1) ? true : false );
		_anchorMAC = source.readString();
		_snapshotName = source.readString();
	}

}
