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

import java.lang.reflect.Method;
import java.util.ArrayList;

public class AutoCopier<DTOT, OtherT> {
	
	private final Class<DTOT> dtoClass;
	private final Class<OtherT> otherClass;
	
	private ArrayList<PropertyCopier> fromOther = new ArrayList<PropertyCopier>();
	private ArrayList<PropertyCopier> toOther = new ArrayList<PropertyCopier>();
	
	public AutoCopier(Class<DTOT> dtoClass, Class<OtherT> otherClass) throws AutoCopyException {
		this.dtoClass = dtoClass;
		this.otherClass = otherClass;
		this.scanDTO();
	}

	protected void scanDTO() throws AutoCopyException {
		
		for (Method m: dtoClass.getMethods()) {
			AutoCopyFrom fromDecl = m.getAnnotation(AutoCopyFrom.class);
			if (fromDecl != null) {
				processFromDeclaration(m, fromDecl);
			} else {
				AutoCopyTo toDecl = m.getAnnotation(AutoCopyTo.class);
				if (toDecl != null) {
					processToDeclaration(m, toDecl);
				}
			}
		}
		if ((fromOther.size() == 0) & (toOther.size() == 0)) {
			throw new AutoCopyDeclarationException(
					String.format("No operations defined for classes %1$s and %2$s.",
							dtoClass.getName(), otherClass.getName()));
		}
	}
	
	private void processFromDeclaration(Method setter, AutoCopyFrom fromDecl) throws AutoCopyException {

		for (AutoCopyGetter getterDecl: fromDecl.value()) {
			if (getterDecl.source().equals(otherClass)) {
				try {
					Method getter = otherClass.getMethod(getterDecl.name());
					fromOther.add(new PropertyCopier(getter, setter, getterDecl.useCopyConstructor()));
				} catch (Exception e) {
					throw new AutoCopyDeclarationException(
							String.format("The class %1$s do not define the method %2$s().",
									otherClass.getName(), getterDecl.name()), e);
				}
			}
		}
	}
	
	private Method findSetter(AutoCopySetter setterDecl, Class<?> paramType) throws AutoCopyDeclarationException {
		if (setterDecl.paramTypes().length == 0) {
			try {
				return otherClass.getMethod(setterDecl.name(), paramType);
			} catch (NoSuchMethodException e) {
				throw new AutoCopyDeclarationException(
						String.format("The class %1$s do not define the method %2$s().",
								otherClass.getName(), setterDecl.name()), e);
			}
		} else {
			for (Class<?> candidateParamType: setterDecl.paramTypes()) {
				try {
					return otherClass.getMethod(setterDecl.name(), candidateParamType);
				} catch (NoSuchMethodException e) {}
			}
			throw new AutoCopyDeclarationException(
					String.format("The class %1$s do not define a suitable method %2$s().",
							otherClass.getName(), setterDecl.name()));
		}
	}

	private void processToDeclaration(Method getter, AutoCopyTo toDecl) throws AutoCopyException {

		for (AutoCopySetter setterDecl: toDecl.value()) {
			if (setterDecl.target().equals(otherClass)) {
				toOther.add(new PropertyCopier(getter, findSetter(setterDecl, getter.getReturnType()), 
						setterDecl.useCopyConstructor()));
			}
		}
	}

	public void copyTo(DTOT source, OtherT target) throws AutoCopyException {
		if (toOther.size() == 0) {
			throw new UnsupportedOperationException("Unable to copy.");
		}
		for (PropertyCopier copier: toOther) {
			copier.copy(source, target);
		}
	}

	public void copyFrom(OtherT source, DTOT target) throws AutoCopyException {
		if (fromOther.size() == 0) {
			throw new UnsupportedOperationException("Unable to copy.");
		}
		for (PropertyCopier copier: fromOther) {
			copier.copy(source, target);
		}
	}
}
