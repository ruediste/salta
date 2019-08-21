package com.github.ruediste.salta.jsr330.test.wikiChecks;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.mockito.Mockito;

import com.github.ruediste.salta.core.CoreInjectorConfiguration;
import com.github.ruediste.salta.core.RecipeCreationContext;
import com.github.ruediste.salta.core.compile.SupplierRecipeImpl;
import com.github.ruediste.salta.jsr330.AbstractModule;
import com.github.ruediste.salta.jsr330.Salta;
import com.github.ruediste.salta.standard.recipe.FixedFieldRecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjector;
import com.github.ruediste.salta.standard.recipe.RecipeMembersInjectorFactory;
import com.google.common.reflect.TypeToken;

public class CustomInjectionPointsTest {

	private interface DataSource {
		String getName();
	}

	@Target({ TYPE, FIELD, METHOD })
	@Retention(RUNTIME)
	private @interface Resource {
		String name() default "";
	}

	private static class A {
		@Resource(name = "app")
		DataSource dataSource;
	}

	@Test
	public void testInjectResource() {
		A a = Salta.createInjector(new AbstractModule() {

			@Override
			protected void configure() throws Exception {
				// save the config for later use. May not use any function of
				// the Binder or AbstractModule after configure() returned
				CoreInjectorConfiguration config = config().standardConfig.config;

				bindMembersInjectorFactory(new RecipeMembersInjectorFactory() {
					@Override
					public List<RecipeMembersInjector> createMembersInjectors(RecipeCreationContext ctx,
							TypeToken<?> type) {
						ArrayList<RecipeMembersInjector> result = new ArrayList<>();
						// iterate all ancestors
						for (TypeToken<?> currentType : type.getTypes()) {

							// iterate the fields
							for (Field field : currentType.getRawType().getDeclaredFields()) {

								// try to get the resource annotation
								Resource resource = field.getAnnotation(Resource.class);
								if (resource != null) {
									// if the annotation is present,
									// register an injector
									TypeToken<?> fieldType = currentType.resolveType(field.getGenericType());
									result.add(new FixedFieldRecipeMembersInjector(field,
											new SupplierRecipeImpl(() -> retrieveResource(fieldType, resource))));
								}
							}
						}
						return result;
					}

				});
			}
		}).getInstance(A.class);
		assertNotNull(a.dataSource);
		assertEquals("app", a.dataSource.getName());
	}

	private Object retrieveResource(TypeToken<?> typeToken, Resource resource) {
		DataSource result = (DataSource) mock(typeToken.getRawType());
		Mockito.when(result.getName()).thenReturn(resource.name());
		return result;
	}
}
