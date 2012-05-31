package com.clicknect.webapi;

import net.rim.device.api.system.DeviceInfo;

public interface WebAPI {
	String UID = Integer.toHexString(DeviceInfo.getDeviceId());
	void request();
}
