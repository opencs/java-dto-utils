/*
 * BSD 3-Clause License
 * 
 * Copyright (c) 2019, Open Communications Security
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * 3. Neither the name of the copyright holder nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package br.com.opencs.util.dto.sign;

import static org.junit.Assert.*;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

public class SignedPropertyExtractorTest {
	
	private static final byte [] EXTRACTED_SAMPLE1_DEFAULT = {
			(byte)0x30, (byte)0x01, // i
			(byte)0x30, (byte)0x01, // l
			(byte)0x02, (byte)0x01, // s
			(byte)0x66, (byte)0x61, (byte)0x6c, (byte)0x73, (byte)0x65, (byte)0x01 // b
	};

	private static final byte [] EXTRACTED_SAMPLE1_FILLED = {
			(byte)0x31, (byte)0x30, (byte)0x01, // i
			(byte)0x32, (byte)0x30, (byte)0x01, // l
			(byte)0x74, (byte)0x65, (byte)0x73, (byte)0x74, (byte)0x01, // s
			(byte)0x74, (byte)0x72, (byte)0x75, (byte)0x65, (byte)0x01 // b
	};
	
	@Test
	public void testSignedPropertyExtractor() throws Exception {
		
		SignedPropertyExtractor e = new SignedPropertyExtractor(Sample1.class);
		assertEquals(Sample1.class, e.getDTOClass());
	}
	
	@Test(expected = DTOSignerException.class)
	public void testSignedPropertyExtractorFailedNoAnnotations() throws Exception {

		@SuppressWarnings("unused")
		SignedPropertyExtractor e = new SignedPropertyExtractor(String.class);
	}
	
	@Test(expected = DTOSignerException.class)
	public void testSignedPropertyExtractorFailedMethodWithNoReturn() throws Exception {
		
		@SuppressWarnings("unused")
		SignedPropertyExtractor e = new SignedPropertyExtractor(Sample2.class);
	}
	
	@Test(expected = DTOSignerException.class)
	public void testSignedPropertyExtractorFailedMethodWithParameters() throws Exception {

		@SuppressWarnings("unused")
		SignedPropertyExtractor e = new SignedPropertyExtractor(Sample3.class);
	}
	
	@Test
	public void testFindSignedProperties() throws Exception {
		SignedPropertyExtractor e = new SignedPropertyExtractor(Sample1.class);
		
		List<Method> methods = e.findSignedProperties(Sample1.class);
		assertEquals(4, methods.size());
		HashSet<String> methodSet = new HashSet<String>();
		for (Method m: methods) {
			methodSet.add(m.getName());
		}
		assertTrue(methodSet.contains("getI"));
		assertTrue(methodSet.contains("getL"));
		assertTrue(methodSet.contains("getS"));
		assertTrue(methodSet.contains("isB"));	}

	@Test
	public void testExtract() throws Exception {
		SignedPropertyExtractor e = new SignedPropertyExtractor(Sample1.class);

		Sample1 s = new Sample1();
		byte [] bin = e.extract(s);
		assertArrayEquals(EXTRACTED_SAMPLE1_DEFAULT, bin);
		
		Sample1 s2 = new Sample1();
		byte [] bin2 = e.extract(s2);
		assertArrayEquals(bin, bin2);
		
		Sample1 s3 = new Sample1();
		s3.setB(true);
		s3.setI(10);
		s3.setL(20);
		s3.setS("test");
		s3.setX((short)123);
		byte [] bin3 = e.extract(s3);
		assertArrayEquals(EXTRACTED_SAMPLE1_FILLED, bin3);
		
		s3.setX((short)124);
		bin3 = e.extract(s3);
		assertArrayEquals(EXTRACTED_SAMPLE1_FILLED, bin3);
	}

	@Test
	public void testGetDTOClass() throws Exception {
		SignedPropertyExtractor e = new SignedPropertyExtractor(Sample1.class);
		
		assertEquals(Sample1.class, e.getDTOClass());
	}

	@Test
	public void testCanExtract() throws Exception {
		SignedPropertyExtractor e = new SignedPropertyExtractor(Sample1.class);
		
		assertTrue(e.canExtract(new Sample1()));
		assertFalse(e.canExtract(new Sample2()));
		assertFalse(e.canExtract(new Sample3()));
	}
}
