package br.com.opencs.util.dto.copy;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

class PropertyCopier {
	
	private final Method getter;
	
	private final Method setter;
	
	private final Constructor<?> copyConstructor;

	public PropertyCopier(Method getter, Method setter, boolean copyValue) throws AutoCopyException {
		AutoCopyUtils.checkGetter(getter);
		AutoCopyUtils.checkSetter(setter, copyValue);

		this.getter = getter;
		this.setter = setter;
		if (copyValue) {
			this.copyConstructor = AutoCopyUtils.getCopyConstructor(setter.getParameters()[0].getType());
		} else {
			this.copyConstructor = null;
		}
	}

	public void copy(Object source, Object target) throws AutoCopyException {
		Object value;
		
		try {
			value = getter.invoke(source);
		} catch (Exception e) {
			throw new AutoCopyException(String.format("Unable to invoke the getter."), e);
		}
		if (copyConstructor != null) {
			try {
				value = copyConstructor.newInstance(value);
			} catch (Exception e) {
				throw new UnableToCopyException(
						String.format("Unable to copy the instance of %1$s.", 
								copyConstructor.getDeclaringClass().getName(), e));
			}
		}
		try {
			setter.invoke(target, value);
		} catch (Exception e) {
			throw new AutoCopyException(String.format("Unable to invoke the setter."), e);
		}	
	}
}
