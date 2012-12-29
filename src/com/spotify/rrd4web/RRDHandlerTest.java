package com.spotify.rrd4web;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class RRDHandlerTest {
	RRDHandler rrdHandler = null;

	@Before
	public void setUp() throws Exception {
		rrdHandler = new RRDHandler();
	}

	@Test
	public void testGetRrdName() {
		assertEquals("foo",rrdHandler.getRrdName("foo/bar/baz"));
	}

	@Test
	public void testGetHostNames() {
		String[] strRet = null;
		strRet = rrdHandler.getHostNames("rrd/json/foo/bar");
		assertEquals("bar", strRet[0]);
		assertEquals(1, strRet.length);
		strRet = rrdHandler.getHostNames("rrd/json/foo/bar/");
		assertEquals("bar", strRet[0]);
		assertEquals(1, strRet.length);
		strRet = rrdHandler.getHostNames("rrd/json/foo/bar/baz");
		assertEquals("bar", strRet[0]);
		assertEquals("baz", strRet[1]);
		assertEquals(2, strRet.length);
		strRet = rrdHandler.getHostNames("rrd/json/foo/bar/baz/");
		assertEquals("bar", strRet[0]);
		assertEquals("baz", strRet[1]);
		assertEquals(2, strRet.length);

	}
	
}
