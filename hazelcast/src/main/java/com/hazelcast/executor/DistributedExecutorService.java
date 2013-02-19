/*
 * Copyright (c) 2008-2013, Hazelcast, Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hazelcast.executor;

import com.hazelcast.core.DistributedObject;
import com.hazelcast.spi.ManagedService;
import com.hazelcast.spi.NodeEngine;
import com.hazelcast.spi.RemoteService;
import com.hazelcast.spi.ResponseHandler;
import com.hazelcast.spi.impl.ExecutionServiceImpl;

import java.util.Collections;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

/**
 * @mdogan 1/18/13
 */
public class DistributedExecutorService implements ManagedService, RemoteService {

    public static final String SERVICE_NAME = "hz:impl:executorService";

    private final Set<String> shutdownExecutors = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
    private NodeEngine nodeEngine;
    private ExecutionServiceImpl executionService;

    public void init(NodeEngine nodeEngine, Properties properties) {
        this.nodeEngine = nodeEngine;
        this.executionService = (ExecutionServiceImpl) nodeEngine.getExecutionService();
    }

    public void reset() {
        shutdownExecutors.clear();
    }

    public void shutdown() {
        reset();
    }

    public void execute(String name, final Callable callable, final ResponseHandler responseHandler) {
        executionService.execute(name, new CallableProcessor(callable, responseHandler));
    }

    private class CallableProcessor implements Runnable {
        final Callable callable;
        final ResponseHandler responseHandler;

        private CallableProcessor(Callable callable, ResponseHandler responseHandler) {
            this.callable = callable;
            this.responseHandler = responseHandler;
        }

        public void run() {
            Object result = null;
            try {
                result = callable.call();
            } catch (Exception e) {
                nodeEngine.getLogger(DistributedExecutorService.class.getName())
                        .log(Level.FINEST, "While executing callable: " + callable, e);
                result = e;
            } finally {
                responseHandler.sendResponse(result);
            }
        }
    }

    public void shutdownExecutor(String name) {
        executionService.destroyExecutor(name);
        shutdownExecutors.add(name);
    }

    public boolean isShutdown(String name) {
        return shutdownExecutors.contains(name);
    }

    public String getServiceName() {
        return SERVICE_NAME;
    }

    public DistributedObject createDistributedObject(Object objectId) {
        final String name = String.valueOf(objectId);
        return new ExecutorServiceProxy(name, nodeEngine, this);
    }

    public DistributedObject createDistributedObjectForClient(Object objectId) {
        return null;
    }

    public void destroyDistributedObject(Object objectId) {
        final String name = String.valueOf(objectId);
        shutdownExecutors.remove(name);
        executionService.destroyExecutor(name);
    }
}
