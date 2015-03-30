package com.github.ruediste.salta.jsr330.wikiChecks;

import static org.junit.Assert.fail;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

import net.sf.cglib.proxy.MethodProxy;

import org.junit.Test;

import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.matchers.Matchers;
import com.github.ruediste.salta.standard.binder.SaltaMethodInterceptor;

public class AopTest {

	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	private @interface NotOnWeekends {
	}

	private class WeekendBlocker implements SaltaMethodInterceptor {

		private Calendar today = new GregorianCalendar();

		@Override
		public Object intercept(Object delegate, Method method, Object[] args,
				MethodProxy proxy) throws Throwable {
			if (today.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
					Locale.ENGLISH).startsWith("S")) {
				throw new IllegalStateException(method.getName()
						+ " not allowed on weekends!");
			}
			return proxy.invoke(delegate, args);
		}
	}

	static class RealBillingService {

		@NotOnWeekends
		public void chargeOrder() {
			// ...
		}
	}

	@Test
	public void testAop() {
		WeekendBlocker blocker = new WeekendBlocker();
		RealBillingService service = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() throws Exception {
				bindInterceptor(Matchers.any(),
						Matchers.annotatedWith(NotOnWeekends.class), blocker);
			}
		}).getInstance(RealBillingService.class);

		blocker.today = new GregorianCalendar(2015, 1, 1);
		try {
			service.chargeOrder();
			fail();
		} catch (IllegalStateException e) {
			// e.printStackTrace();
		}

		blocker.today = new GregorianCalendar(2015, 1, 3);
		service.chargeOrder();
	}
}
