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

import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class implements the signed property extractor for a given class.
 * 
 * <p>All instances of this class are expected to be thread safe.</p>
 * 
 * @author Fabio Jun Takada Chino
 */
class SignedPropertyExtractor {
	
	private static final Charset DEFAULT_CHARSET = Charset.forName("utf8");
	
	private static final char PROPERTY_SEPARATOR = (char)1;

	private static final char NULL_MARKER = (char)2;
	
	private Class<?> dtoClass;
	
	private List<Method> methodList;
	
	/**
	 * Creates a new instance of this class.
	 * 
	 * @param dtoClass The class to be extracted. At least one method should be annotated with
	 * SignedProperty otherwise it will lead to an error.
	 * @throws DTOSignerException In case of error.
	 */
	public SignedPropertyExtractor(Class<?> dtoClass) throws DTOSignerException {
		
		this.dtoClass = dtoClass;
		this.methodList = findSignedProperties(dtoClass);
		if (this.methodList.size() == 0) {
			throw new DTOSignerException(String.format(
					"The class %1$s has no properties to sign.", dtoClass.getName()));
		}
		Collections.sort(methodList, MethodNameComparator.INSTANCE);
	}

	private void checkMethodSuitablility(Method method) throws DTOSignerException {
		if (method.getParameterCount() > 0) {
			throw new DTOSignerException(
					String.format("The method %1$s.%2$s() cannot have parameters.", dtoClass.getName(), method.getName()));
		}
		if (method.getReturnType().getTypeName().equals("void")) {
			throw new DTOSignerException(
					String.format("The method %1$s.%2$s() must have a return value.", dtoClass.getName(), method.getName()));
		}
	}
	
	/**
	 * Finds a list of all methods annotated with SignedProperty.
	 * 
	 * @param dtoClass The class of the object.
	 * @return A sorted list of the suitable methods.
	 * @throws DTOSignerException In case of error.
	 */
	protected List<Method> findSignedProperties(Class<?> dtoClass) throws DTOSignerException {
		ArrayList<Method> methods = new ArrayList<Method>();

		for (Method m: dtoClass.getMethods()) {
			if (m.getAnnotation(SignedProperty.class) != null) {
				checkMethodSuitablility(m);
				methods.add(m);
			}
		}
		return methods;
	}
	
	/**
	 * Extracts the value of all signed properties to be signed. It is important
	 * to notice that distinct instances with the same contents must generate the
	 * same output.
	 * 
	 * <p>This method extracts the values from the properties using the lexicographic
	 * order of the method names and concatenate them. The concatenation is performed
	 * using the Object.toString() method, using the character 0x01 as the separator of
	 * the fields and 0x02 to represent null values.</p>
	 * 
	 * @param dto The DTO to be extracted. It must match the type used to create
	 * this extractor.
	 * @return The extracted value of the properties.
	 * @throws DTOSignerException In case of error.
	 */
	public byte[] extract(Object dto) throws DTOSignerException {
		
		if (!canExtract(dto)) {
			throw new IllegalArgumentException(
					String.format("This extractor handles %1$s but the argument has the class %2$d.", 
					this.getDTOClass().getName(), dto.getClass().getName()));
		}
		return this.extract(dto, this.methodList);
	}

	private byte[] extract(Object dto, List<Method> methods) throws DTOSignerException {
		StringBuffer sb = new StringBuffer();
		for (Method method: methods) {
			Object ret;
			try {
				ret = method.invoke(dto);
			} catch (Exception e) {
				throw new DTOSignerException(
						String.format("Unable to invoke the method %1$s.%2$s().", dto.getClass().getName(), method.getName()));
			}
			if (ret != null) {
				sb.append(ret.toString());
			} else {
				sb.append(NULL_MARKER);
			}
			sb.append(PROPERTY_SEPARATOR);
		}
		
		ByteBuffer bin = DEFAULT_CHARSET.encode(CharBuffer.wrap(sb.toString()));
		byte [] ret = new byte[bin.remaining()];
		bin.get(ret);
		return ret;
	}
	
	/**
	 * Returns the class associated with this extractor.
	 * 
	 * @return The class.
	 */
	public Class<?> getDTOClass(){
		return this.dtoClass;
	}

	/**
	 * Verifies if a given object can be extracted using this extractor.
	 * 
	 * @param dtoClass
	 * @return true if it can or false otherwise.
	 */
	public boolean canExtract(Object dtoClass) {
		return this.dtoClass.equals(dtoClass.getClass());
	}
}
