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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * This class implements some utility methods to handle the reflection part
 * of the copy operations.
 * 
 * <p>This part of the project is highly experimental and should not
 * be used in production yet.</p>
 * 
 * @author Fabio Jun Takada Chino
 * @since 2019.05.30
 */
class AutoCopyUtils {
	
	public static void checkSetter(Method method, boolean copyValue) throws AutoCopyException {
		
		if (method.getParameterCount() != 1) {
			throw new WrongParameterException(String.format(
					"The setter %1$s.%2$s() must have only one parameter.", 
					method.getDeclaringClass().getName(),
					method.getName()));
		}
		if (copyValue) {
			if (!hasCopyConstructor(method.getParameters()[0].getType())) {
				throw new UnableToCopyException(String.format(
						"The parameter of the setter %1$s.%2$s() must have a copy constructor.", 
						method.getDeclaringClass().getName(),
						method.getName()));
			}
		}
	}
	
	public static void checkGetter(Method method) throws AutoCopyException {
		
		if (method.getParameterCount() != 0) {
			throw new WrongParameterException(String.format(
					"The getter %1$s.%2$s() cannot have parameter.", 
					method.getDeclaringClass().getName(),
					method.getName()));
		}
		if (method.getReturnType().equals(Void.TYPE)) {
			throw new NoReturnException(String.format(
					"The getter %1$s.%2$s() must return a value.", 
					method.getDeclaringClass().getName(),
					method.getName()));
		}
	}

	public static Object createCopy(Class<?> targetType, Object source) throws UnableToCopyException {

		try {
			Constructor<?> copyConstructor = targetType.getConstructor(targetType);
			return copyConstructor.newInstance(source);
		} catch (Exception e) {
			throw new UnableToCopyException(
					String.format("Unable to create a new instance of %1$s using an instance of $2$s.", 
							targetType.getName(), source.getClass().getName()), e);
		}
	}
	
	public static boolean hasCopyConstructor(Class<?> type) {
		
		try {
			type.getConstructor(type);
			return true;
		} catch (NoSuchMethodException e) {
			return false;
		}
	}

	/**
	 * This method gets the value from the getter of the source object and call
	 * the setter on the destination object.
	 * 
	 * @param setter The setter method.
	 * @param target The target object.
	 * @param getter
	 * @param source
	 * @param copyValue
	 * @throws AutoCopyException
	 */
	public static void copy(Method setter, Object target, Method getter, Object source, boolean copyValue) throws AutoCopyException {
		Object value;
		
		try {
			value = getter.invoke(source);
		} catch (Exception e) {
			throw new AutoCopyException(String.format("Unable to invoke the getter."), e);
		}
		if (copyValue) {
			value = createCopy(setter.getParameters()[0].getType(),  value);
		}
		try {
			setter.invoke(target, value);
		} catch (Exception e) {
			throw new AutoCopyException(String.format("Unable to invoke the setter."), e);
		}
	}
}
