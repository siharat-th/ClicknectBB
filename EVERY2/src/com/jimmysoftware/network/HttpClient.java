package com.jimmysoftware.network;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Hashtable;

import javax.microedition.io.HttpConnection;

import net.rim.blackberry.api.browser.URLEncodedPostData;

public class HttpClient {

	/**
	 * Perform GET operation.
	 * 
	 * @param url
	 *            the GET URL.
	 * @return response.
	 * @throws Exception
	 *             when any error occurs.
	 */

	private HttpConnectionFactory factory;

	public HttpClient(HttpConnectionFactory pFactory) {
		factory = pFactory;
	}

	public StringBuffer doGet(String url) throws Exception {
		return doGet(url, null, factory);
	}

	public StringBuffer doGet(String url, Hashtable args) throws Exception {
		return doGet(url, args, factory);
	}

	public StringBuffer doGet(String url, Hashtable args, HttpConnectionFactory factory) throws Exception {
		StringBuffer urlBuffer = new StringBuffer(url);

		if ((args != null) && (args.size() > 0)) {
			urlBuffer.append('?');
			Enumeration keysEnum = args.keys();

			while (keysEnum.hasMoreElements()) {
				String key = (String) keysEnum.nextElement();
				String val = (String) args.get(key);
				urlBuffer.append(key).append('=').append(val);

				if (keysEnum.hasMoreElements()) {
					urlBuffer.append('&');
				}
			}
		}

		return doGet(urlBuffer.toString(), factory);
	}

	/**
	 * Perform GET operation.
	 * 
	 * @param url
	 *            the GET URL.
	 * @return response.
	 * @throws Exception
	 *             when any error occurs.
	 */
	public StringBuffer doGet(String url, HttpConnectionFactory factory) throws Exception {
		HttpConnection connection = null;
		StringBuffer buffer = new StringBuffer();

		try {
			if ((url == null) || url.equalsIgnoreCase("") || (factory == null)) {
				return null;
			}
			connection = factory.getHttpConnection(url);

			switch (connection.getResponseCode()) {

			case HttpConnection.HTTP_OK: {
				InputStream inputStream = connection.openInputStream();
				int c;

				while ((c = inputStream.read()) != -1) {
					buffer.append((char) c);
				}

				inputStream.close();
				break;
			}

			case HttpConnection.HTTP_TEMP_REDIRECT:
			case HttpConnection.HTTP_MOVED_TEMP:
			case HttpConnection.HTTP_MOVED_PERM: {
				url = connection.getHeaderField("Location");
				buffer = doGet(url, factory);
				break;
			}

			default:
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException e) {
				}
			}
		}

		return buffer;
	}

	/**
	 * Perform POST operation.
	 * 
	 * @param url
	 *            the POST url.
	 * @param data
	 *            the data to be POSTed
	 * @return response.
	 * @throws Exception
	 *             when any error occurs.
	 */

	public StringBuffer doPost(String url, Hashtable data) throws Exception {
		return doPost(url, data, factory);
	}

	public StringBuffer doPost(String url, Hashtable data, HttpConnectionFactory factory) throws Exception {
		URLEncodedPostData encoder = new URLEncodedPostData("UTF-8", false);
		Enumeration keysEnum = data.keys();

		while (keysEnum.hasMoreElements()) {
			String key = (String) keysEnum.nextElement();
			String val = (String) data.get(key);
			encoder.append(key, val);
		}

		return doPost(url, encoder.getBytes(), factory);
	}

	/**
	 * Perform POST operation.
	 * 
	 * @param url
	 *            the POST url.
	 * @param data
	 *            the data to be POSTed
	 * @return response.
	 * @throws Exception
	 *             when any error occurs.
	 */
	public StringBuffer doPost(String url, byte[] data, HttpConnectionFactory factory) throws Exception {
		HttpConnection connection = null;
		StringBuffer buffer = new StringBuffer();

		try {
			if ((url == null) || url.equalsIgnoreCase("") || (factory == null)) {
				return null;
			}
			connection = factory.getHttpConnection(url, null, data);

			switch (connection.getResponseCode()) {

			case HttpConnection.HTTP_OK: {
				InputStream inputStream = connection.openInputStream();
				int c;

				while ((c = inputStream.read()) != -1) {
					buffer.append((char) c);
				}

				inputStream.close();
				break;
			}

			case HttpConnection.HTTP_TEMP_REDIRECT:
			case HttpConnection.HTTP_MOVED_TEMP:
			case HttpConnection.HTTP_MOVED_PERM: {
				url = connection.getHeaderField("Location");
				buffer = doPost(url, data, factory);
				break;
			}

			default:
				break;
			}
		} catch (Exception e) {
			throw e;
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (IOException e) {
				}
			}
		}

		return buffer;
	}

}
