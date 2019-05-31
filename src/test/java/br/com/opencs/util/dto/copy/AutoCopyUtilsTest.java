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
package br.com.opencs.util.dto.copy;

import static org.junit.Assert.*;

import java.lang.reflect.Method;

import org.junit.Test;

public class AutoCopyUtilsTest {

	@Test
	public void testCheckSetter() throws Exception {
	
		// Normal setter
		Method m = Sample1.class.getMethod("setI", Integer.TYPE);
		AutoCopyUtils.checkSetter(m, false);
		
		// Normal 2
		m = Sample1.class.getMethod("setCloneable", CloneableSample.class);
		AutoCopyUtils.checkSetter(m, false);

		// With copy
		AutoCopyUtils.checkSetter(m, true);
	}

	@Test(expected = WrongParameterException.class)
	public void testCheckSetterFailedNoParameters() throws Exception {
	
		Method m = StringBuffer.class.getMethod("reverse");
		AutoCopyUtils.checkSetter(m, false);
	}

	@Test(expected = WrongParameterException.class)
	public void testCheckSetterFailedMultipleParameters() throws Exception {
	
		Method m = StringBuffer.class.getMethod("substring", Integer.TYPE, Integer.TYPE);
		AutoCopyUtils.checkSetter(m, false);
	}
	
	@Test
	public void testCheckGetter() throws Exception {

		Method m = Sample1.class.getMethod("getI");
		AutoCopyUtils.checkGetter(m);
	}

	@Test(expected = NoReturnException.class)
	public void testCheckGetterFailedNoReturn() throws Exception {

		Method m = Sample1.class.getMethod("noReturn");
		AutoCopyUtils.checkGetter(m);
	}
	
	@Test(expected = WrongParameterException.class)
	public void testCheckGetterFailedWithParameter() throws Exception {

		Method m = Sample1.class.getMethod("withParameter", Integer.TYPE);
		AutoCopyUtils.checkGetter(m);
	}

	@Test
	public void testCreateCopy() throws Exception {
		
		CloneableSample s = new CloneableSample();
		CloneableSample r = (CloneableSample)AutoCopyUtils.createCopy(CloneableSample.class, s);
		assertNotSame(s, r);
		assertEquals(s.getValue(), s.getValue());
	}

	@Test
	public void testCopy() throws Exception {
		Sample1 a = new Sample1();
		Sample1 b = new Sample1();
		
		Method getter = Sample1.class.getMethod("getI");
		Method setter = Sample1.class.getMethod("setI", Integer.TYPE);
		a.setI(123);
		AutoCopyUtils.copy(setter, b, getter, a, false);
		assertEquals(a.getI(), b.getI());
		
		setter = Sample1.class.getMethod("setL", Long.TYPE);
		a.setI(123);
		AutoCopyUtils.copy(setter, b, getter, a, false);
		assertEquals(a.getI(), b.getL());
		
		getter = Sample1.class.getMethod("getCloneable");
		setter = Sample1.class.getMethod("setCloneable", CloneableSample.class);
		a.setCloneable(new CloneableSample());
		AutoCopyUtils.copy(setter, b, getter, a, false);
		assertSame(a.getCloneable(), b.getCloneable());
		assertNotSame(a.getCloneable().getValue(), b.getCloneable().getValue());
	
		getter = Sample1.class.getMethod("getCloneable");
		setter = Sample1.class.getMethod("setCloneable", CloneableSample.class);
		a.setCloneable(new CloneableSample());
		AutoCopyUtils.copy(setter, b, getter, a, true);
		assertNotSame(a.getCloneable(), b.getCloneable());
		assertNotSame(a.getCloneable().getValue(), b.getCloneable().getValue());
	}
}
