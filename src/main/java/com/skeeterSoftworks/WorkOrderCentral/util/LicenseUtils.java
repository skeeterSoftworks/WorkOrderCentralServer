package com.skeeterSoftworks.WorkOrderCentral.util;

import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class LicenseUtils {


	public static List<String> getMacAddresses() throws SocketException{


		Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();

		List<String> macAddresses = new ArrayList<>();

		while (networkInterfaces.hasMoreElements()) {
		    NetworkInterface ni = networkInterfaces.nextElement();
		    byte[] hardwareAddress = ni.getHardwareAddress();
		    if (hardwareAddress != null) {
		        String[] hexadecimalFormat = new String[hardwareAddress.length];
		        for (int i = 0; i < hardwareAddress.length; i++) {
		            hexadecimalFormat[i] = String.format("%02X", hardwareAddress[i]);
		        }

		        macAddresses.add(String.join("-", hexadecimalFormat));
		    }
		}

		return macAddresses;
	}

}
