/*
 * Copyright (c) 2011-2013 GoPivotal, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.core.composable.spec;

import reactor.core.Environment;
import reactor.core.Observable;
import reactor.core.action.BufferAction;
import reactor.core.composable.Composable;
import reactor.core.composable.Deferred;
import reactor.core.composable.Stream;
import reactor.event.Event;
import reactor.event.selector.Selector;
import reactor.tuple.Tuple2;

/**
 * A helper class for specifying a {@link Deferred} {@link Stream}.
 *
 * @param <T> The type of values that the stream will contain
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public final class DeferredStreamSpec<T> extends ComposableSpec<DeferredStreamSpec<T>, Deferred<T, Stream<T>>> {

	private int batchSize = -1;

	/**
	 * Configures the stream to have the given {@code batchSize}. A value of {@code -1}, which
	 * is the default configuration, configures the stream to not be batched.
	 *
	 * @param batchSize The batch size of the stream
	 * @return {@code this}
	 */
	public DeferredStreamSpec<T> batchSize(int batchSize) {
		this.batchSize = batchSize;
		return this;
	}

	@Override
	protected Deferred<T, Stream<T>> createComposable(Environment env, Observable observable,
	                                                  Tuple2<Selector, Object> accept) {
		Stream<T> stream =
				new Stream<T>(observable, batchSize, null, accept, env);
		if (batchSize > 1) {
			return new BatchStreamDeferred<T>(stream, batchSize);
		} else {
			return new Deferred<T, Stream<T>>(stream);
		}

	}

	private static class BatchStreamDeferred<T> extends Deferred<T, Stream<T>> {
		private final BufferAction<T> consumer;

		public BatchStreamDeferred(Stream<T> stream, int batchSize) {
			super(stream);
			consumer = batcher(batchSize);
		}

		@Override
		public void acceptEvent(Event<T> value) {
			consumer.accept(value);
		}
	}
}
