package net.stamfest.rrd.tests;

import java.util.HashMap;
import java.util.Map.Entry;

import net.stamfest.rrd.CommandResult;
import net.stamfest.rrd.RRDp;

public class HelloRRD {
	public static void main(String[] args) throws Exception {
		String rrdFileName = args[0];
		
		RRDp rrd = new RRDp(".", null);
		CommandResult result = rrd.command(new String[] { "info", rrdFileName  });
		System.out.println(result);
		
		HashMap<String, String> hm = result.getInfo();
		
		if (hm != null) {
			for (Entry<String,String> e : hm.entrySet()) {
				System.out.printf("%s:\t%s\n", e.getKey(), e.getValue());
			}
		}
	}
}
