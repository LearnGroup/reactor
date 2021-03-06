/*
 * Copyright (c) 2011-2014 Pivotal Software, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package reactor.rx.action;

import org.reactivestreams.Subscription;
import reactor.bus.Event;
import reactor.bus.Observable;
import reactor.core.Dispatcher;

/**
 * @author Stephane Maldini
 * @since 1.1
 */
public class ObservableAction<T> extends Action<T, Void> {

	private final Observable observable;
	private final Object     key;
	private int count = 0;

	public ObservableAction(Dispatcher dispatcher, Observable observable, Object key) {
		super(dispatcher);
		this.observable = observable;
		this.key = key;
	}

	@Override
	protected void doSubscribe(Subscription subscription) {
		consume();
	}

	@Override
	protected void doNext(T ev) {
		observable.notify(key, Event.wrap(ev));
		if(count++ >= capacity){
			consume();
		}
	}

	@Override
	protected void doComplete() {
		cancel();
		super.doComplete();
	}

	@Override
	protected void doError(Throwable ev) {
		cancel();
		super.doError(ev);
	}
}
