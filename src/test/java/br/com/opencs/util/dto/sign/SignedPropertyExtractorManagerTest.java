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

import org.junit.Test;

public class SignedPropertyExtractorManagerTest {

	@Test
	public void testGetExtractor() throws Exception {
		
		SignedPropertyExtractor e1 = SignedPropertyExtractorManager.getExtractor(Sample1.class);
		assertNotNull(e1);
		assertEquals(Sample1.class, e1.getDTOClass());
		SignedPropertyExtractor e2 = SignedPropertyExtractorManager.getExtractor(Sample1.class);
		assertSame(e1, e2);
		
		SignedPropertyExtractor e3 = SignedPropertyExtractorManager.getExtractor(Sample4.class);
		assertNotNull(e3);
		assertEquals(Sample4.class, e3.getDTOClass());

		SignedPropertyExtractor e4 = SignedPropertyExtractorManager.getExtractor(Sample1.class);
		assertNotNull(e4);
		assertEquals(Sample1.class, e4.getDTOClass());
	}
	
	@Test(expected = DTOSignerException.class)
	public void testGetExtractorFailed() throws Exception {
		
		@SuppressWarnings("unused")
		SignedPropertyExtractor e1 = SignedPropertyExtractorManager.getExtractor(Sample2.class);
	}
	
	@Test
	public void testClear() throws Exception {
		
		SignedPropertyExtractor e1 = SignedPropertyExtractorManager.getExtractor(Sample1.class);
		SignedPropertyExtractor e2 = SignedPropertyExtractorManager.getExtractor(Sample4.class);

		SignedPropertyExtractorManager.clear();
		
		SignedPropertyExtractor e3 = SignedPropertyExtractorManager.getExtractor(Sample1.class);
		SignedPropertyExtractor e4 = SignedPropertyExtractorManager.getExtractor(Sample4.class);
	
		assertNotSame(e1, e3);
		assertNotSame(e2, e4);
	}

}
