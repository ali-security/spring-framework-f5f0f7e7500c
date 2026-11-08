/*
 * Copyright 2002-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.web.method;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.jupiter.api.Test;

import org.springframework.core.MethodParameter;
import org.springframework.util.ClassUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link HandlerMethod} parameter annotation detection on methods
 * overridden in a type hierarchy with a parameterized supertype whose generics
 * are unresolved (CVE-2025-41249).
 *
 * <p>Adapted from the {@code AnnotatedMethodTests} added by upstream commit
 * 6d710d482a6785b069e35022e81758953afc21ff: {@code AnnotatedMethod} does not
 * exist at 5.3.x, where the fixed {@code isOverrideFor} logic lives in
 * {@link HandlerMethod}.
 */
class HandlerMethodTests {

	@Test
	void shouldFindAnnotationOnMethodParameterInGenericInterface() {
		Method processOneAndTwo = ClassUtils.getMethod(
				GenericInterfaceImpl.class, "processOneAndTwo", Long.class, Object.class);

		HandlerMethod handlerMethod = new HandlerMethod(new GenericInterfaceImpl(), processOneAndTwo);
		MethodParameter[] methodParameters = handlerMethod.getMethodParameters();

		assertThat(methodParameters).hasSize(2);
		assertThat(methodParameters[0].hasParameterAnnotation(Param.class)).isFalse();
		assertThat(methodParameters[1].hasParameterAnnotation(Param.class)).isTrue();
	}


	@Retention(RetentionPolicy.RUNTIME)
	@interface Handler {
	}

	@Retention(RetentionPolicy.RUNTIME)
	@interface Param {
	}

	interface GenericInterface<A, B> {

		@Handler
		void processOneAndTwo(A value1, @Param B value2);
	}

	abstract static class GenericAbstractSuperclass<C> implements GenericInterface<Long, C> {

		@Override
		public void processOneAndTwo(Long value1, C value2) {
		}
	}

	static class GenericInterfaceImpl extends GenericAbstractSuperclass<String> {
	}

}
