/*
 * Copyright (c) 2008 - 2013 10gen, Inc. <http://10gen.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.mongodb.operation;

import org.bson.io.OutputBuffer;
import org.mongodb.MongoException;
import org.mongodb.MongoInternalException;
import org.mongodb.connection.AsyncServerConnection;
import org.mongodb.connection.ResponseBuffers;
import org.mongodb.connection.SingleResultCallback;

abstract class ResponseCallback implements SingleResultCallback<ResponseBuffers> {
    private volatile boolean closed;
    private AsyncServerConnection connection;
    private final OutputBuffer writtenBuffer;
    private long requestId;

    public ResponseCallback(final AsyncServerConnection connection, final OutputBuffer writtenBuffer, final long requestId) {
        this.connection = connection;
        this.writtenBuffer = writtenBuffer;
        this.requestId = requestId;
    }

    protected AsyncServerConnection getConnection() {
        return connection;
    }

    protected long getRequestId() {
        return requestId;
    }

    @Override
    public void onResult(final ResponseBuffers responseBuffers, final MongoException e) {
        if (closed) {
            throw new MongoInternalException("Callback should not be invoked more than once", null);
        }
        closed = true;
        if (writtenBuffer != null) {
            writtenBuffer.close();
        }
        if (responseBuffers != null) {
            callCallback(responseBuffers, e);
        }
        else {
            callCallback(null, e);
        }
    }

    protected abstract void callCallback(ResponseBuffers responseBuffers, MongoException e);
}
