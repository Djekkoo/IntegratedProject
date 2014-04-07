package main;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Callback {
	private Object o;
	private String method;

	public Callback(Object o, String method) {
		this.o = o;
		this.method = method;
	}
	
	public Object invoke(Object... parameters) throws CallbackException {
		
		Class<?>[] classes = new Class<?>[parameters.length];
		for (int i = 0; i < parameters.length; i++) {
			classes[i] = parameters[i].getClass();
		}
		
		try {
			Method method = this.o.getClass().getMethod(this.method, classes);
			return method.invoke(this.o, parameters);
		} catch (InvocationTargetException e) {
			throw new CallbackException(e.getTargetException().getClass(), e.getTargetException().toString());
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			throw new CallbackException(e.getClass(), e.toString());
		}
		
	}
}
