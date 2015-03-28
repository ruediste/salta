package com.github.ruediste.salta.jsr330.wikiChecks;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.sql.DataSource;

import org.junit.Test;

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
					public List<RecipeMembersInjector> createMembersInjectors(
							RecipeCreationContext ctx, TypeToken<?> type) {
						ArrayList<RecipeMembersInjector> result = new ArrayList<>();
						// iterate all ancestors
						for (TypeToken<?> currentType : type.getTypes()) {

							// iterate the fields
							for (Field field : currentType.getRawType()
									.getDeclaredFields()) {

								// try to get the resource annotation
								Resource resource = field
										.getAnnotation(Resource.class);
								if (resource != null) {
									// if the annotation is present,
									// register an injector
									TypeToken<?> fieldType = currentType
											.resolveType(field.getGenericType());
									result.add(new FixedFieldRecipeMembersInjector(
											field,
											new SupplierRecipeImpl(
													() -> retrieveResource(
															fieldType, resource)),
											config.injectionStrategy));
								}
							}
						}
						return result;
					}

				});
			}
		}).getInstance(A.class);
		assertNotNull(a.dataSource);
	}

	private Object retrieveResource(TypeToken<?> typeToken, Resource resource) {
		return mock(typeToken.getRawType());
	}
}
