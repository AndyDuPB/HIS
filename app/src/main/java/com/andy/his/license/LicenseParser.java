package com.andy.his.license;

import android.content.Context;

import com.andy.his.HomeSteadInformationHelper;

import java.io.File;

public class LicenseParser {

	private static final String publicKey = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCkJU15EXMJG114fyGTAAPlO1rk9xQo7lq2tBvvqhIaf225uAHBuFSBL57szYxh+6wdSUDHj3pX6x4tIL3y9WJZjKYid26PN/9GIzCx3SuWc6GRPqrE5eBXDZ5VjbdvgVI4E+2LklZOP3jmaAmQloSC2fu3rG7hDESbIyq8mudseQIDAQAB";

	public static Boolean validationLicenseFile(String licenseNumber, Context context) {

		try
		{
			byte[] decodedData = RSAUtils.decryptByPublicKey(Base64Utils.decode(licenseNumber), publicKey);
			String licenseSerialNumber = new String(decodedData,"GBK");
			String[] split = licenseSerialNumber.split(";");
			if (split != null && split.length == 2) {
				String localSerialNumber = AndroidDeviceInfoCollector.getLicenseSerialNumber(context);
				if (localSerialNumber.equals(split[0]))
				{
					try
					{
						if (Long.valueOf(split[1]) > System.currentTimeMillis()) {
							HomeSteadInformationHelper.createDirectory(HomeSteadInformationHelper.getLicenseFileDirectory());
							HomeSteadInformationHelper.saveFile(licenseNumber, new File(HomeSteadInformationHelper.getLicenseFilePath()));
							return true;
						}
					} catch (Exception e) {
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return false;
	}

}
