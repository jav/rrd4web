package com.spotify.rrd4web;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.stamfest.rrd.CommandResult;
import net.stamfest.rrd.RRDp;

import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.eclipse.jetty.util.ajax.JSONObjectConvertor;
import org.json.simple.JSONObject;

public class RRDHandler extends HttpServlet {
	
	private class DataPoint{
		Integer time;
		Float value;
		DataPoint(Integer _time, Float _value) {
			this.time = _time;
			this.value = _value;
		}
		public  String toString() {
			return "["+this.time+", "+ this.value+"]";
		}
	}

	Integer getLowTime(String requestURI) {
		String[] uriParts = requestURI.split("/");
		return Integer.parseInt(Arrays.copyOfRange(uriParts, 5, 6)[0]);
	}

	Integer getHighTime(String requestURI) {
		String[] uriParts = requestURI.split("/");
		return Integer.parseInt(Arrays.copyOfRange(uriParts, 4, 5)[0]);
	}	

	
	String[] getHostNames(String requestURI) {
		String[] uriParts = requestURI.split("/");
		return Arrays.copyOfRange(uriParts, 6, uriParts.length);
	}

	String getRrdName(String requestURI) {
		
		String[] uriParts = requestURI.split("/");
		return Arrays.copyOfRange(uriParts, 3, 4)[0];
	}
	
	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
	{	
		
		String rrdName = getRrdName(request.getRequestURI());
		System.out.println("rrdName: "+ rrdName);
		String[] hosts = getHostNames(request.getRequestURI());
		Integer lowTime = getLowTime(request.getRequestURI());
		Integer highTime = getHighTime(request.getRequestURI());
		System.out.println("Hosts: "+ hosts);
		RRDp rrd = null;
		try {
			rrd = new RRDp("/tmp/", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		HashMap<String, ArrayList<DataPoint>> dataOut = new HashMap<String, ArrayList<DataPoint>>(); 
		for(String host : hosts) {
			System.out.println("Fetching data for host: "+ host);
			ArrayList<DataPoint> rrdFromHost = getRrdForHost(rrdName, host, lowTime, highTime);
			Collections.sort(rrdFromHost,  new Comparator<DataPoint>() {
			    public int compare(DataPoint a, DataPoint b) {
			        return a.time - b.time;
			    }
			});
			dataOut.put(host, rrdFromHost);
		}

		JSONObject jsonOut = new JSONObject();
		jsonOut.putAll(dataOut);
		String strOut = request.getParameter("callback")+"("+jsonOut.toJSONString()+")";
		
		wrapResponse(request, response, strOut);
	}
	
	private ArrayList<DataPoint> getRrdForHost(String rrdName, String host, Integer lowTime, Integer highTime) {
		if(highTime<lowTime){
			Integer tmp = lowTime;
			lowTime = highTime;
			highTime = tmp;
		}
		System.out.println("Fetching /home/jav/workspace/rrd4web/test/"+host+"-"+rrdName+".rrd");
		String[] command = {"fetch", "/home/jav/workspace/rrd4web/test/"+host+"-"+rrdName+".rrd", "AVERAGE", "-r", "300","-s", lowTime.toString(), "-e", highTime.toString()};

		CommandResult result = null;
		RRDp rrd = null;
		try {
			rrd = new RRDp("/tmp/", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			result = rrd.command(command);
		} catch (Exception e) {
			e.printStackTrace();
		}

		if (!result.ok) { 
		    System.out.println("ERROR: "+result.error);
		} else {
		    System.out.println("OK: "+result.output);
		}

		ArrayList<DataPoint> retPoints = new ArrayList<DataPoint>();
		for( String key :result.info.keySet() ){
			System.out.println("key: "+key+", val: "+result.info.get(key));
			if(result.info.get(key).toLowerCase().contains("nan")) {
				retPoints.add(new DataPoint(Integer.parseInt(key), null));
			}
			else {
				retPoints.add(new DataPoint(Integer.parseInt(key), Float.parseFloat(result.info.get(key))));
			}
		}
		
		return retPoints;
	}


	void wrapResponse(ServletRequest request, HttpServletResponse response, String contents){
		//response.setContentType("text/html;charset=utf-8");
		response.setContentType("application/json;charset=utf-8");
		response.setStatus(HttpServletResponse.SC_OK);
		try {
			response.getWriter().println(contents);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}