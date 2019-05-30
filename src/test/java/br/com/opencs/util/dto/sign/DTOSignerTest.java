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

import java.util.Arrays;

import org.junit.Test;

public class DTOSignerTest {

	@Test
	public void testDTOSigner() throws Exception{
		DTOSigner s = new DTOSigner(new byte[16]);
		
		assertNotNull(s);
	}

	@Test
	public void testCreateSignature() throws Exception {
		DTOSigner s = new DTOSigner(new byte[16]);
		
		Sample4 d = new Sample4();
		d.setFixed("fixed");
		d.setVariable("variable");
		
		byte [] sig = s.createSignature(d);
		assertEquals(32, sig.length);
		
		d.setVariable("variable2");
		byte [] sig2 = s.createSignature(d);
		assertArrayEquals(sig, sig2);

		d.setFixed("123");
		byte [] sig3 = s.createSignature(d);
		assertFalse(Arrays.equals(sig, sig3));
	}

	@Test
	public void testSign() throws Exception {
		DTOSigner s = new DTOSigner(new byte[16]);
		
		Sample4 d = new Sample4();
		d.setFixed("fixed");
		d.setVariable("variable");

		byte [] sig = s.createSignature(d);
		assertEquals(32, sig.length);
	
		SignedDTO<Sample4> signed = new SignedDTO<Sample4>(d);
		assertNull(signed.getSignature());
		s.sign(signed);
		assertArrayEquals(sig, signed.getSignature());
	}

	@Test
	public void testCheckSignature() throws Exception {
		DTOSigner s = new DTOSigner(new byte[16]);
		
		Sample4 d = new Sample4();
		d.setFixed("fixed");
		d.setVariable("variable");

		SignedDTO<Sample4> signed = new SignedDTO<Sample4>(d);
		s.sign(signed);
		
		assertTrue(s.checkSignature(signed));
		d.setVariable("");
		assertTrue(s.checkSignature(signed));
		
		d.setFixed("");
		assertFalse(s.checkSignature(signed));		
	}
}
