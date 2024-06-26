/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.couchdb;

import java.util.UUID;

import com.google.gson.JsonObject;
import com.ibm.cloud.cloudant.v1.model.Document;
import com.ibm.cloud.cloudant.v1.model.DocumentResult;
import com.ibm.cloud.sdk.core.http.Response;
import org.apache.camel.Exchange;
import org.apache.camel.InvalidPayloadException;
import org.apache.camel.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.Answer;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CouchDbProducerTest {

    @Mock
    private CouchDbClientWrapper client;

    @Mock
    private CouchDbEndpoint endpoint;

    @Mock
    private Exchange exchange;

    @Mock
    private Message msg;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Response<DocumentResult> response;

    private CouchDbProducer producer;

    @BeforeEach
    public void before() {
        producer = new CouchDbProducer(endpoint, client);
        when(exchange.getIn()).thenReturn(msg);
    }

    @Test
    void testBodyMandatory() throws Exception {
        when(msg.getMandatoryBody()).thenThrow(InvalidPayloadException.class);
        assertThrows(InvalidPayloadException.class, () -> {
            producer.process(exchange);
        });
    }

    @Test
    void testDocumentHeadersAreSet() throws Exception {
        String id = UUID.randomUUID().toString();
        String rev = UUID.randomUUID().toString();

        Document doc = new Document.Builder()
                .add("_rev", rev)
                .id(id)
                .build();

        DocumentResult documentResult = mock(DocumentResult.class, Answers.RETURNS_DEEP_STUBS);
        when(msg.getMandatoryBody()).thenReturn(doc);
        when(client.update(doc)).thenReturn(response);
        when(response.getResult()).thenReturn(documentResult);
        when(response.getResult().getId()).thenReturn(id);
        when(response.getResult().getRev()).thenReturn(rev);

        producer.process(exchange);
        verify(msg).setHeader(CouchDbConstants.HEADER_DOC_ID, id);
        verify(msg).setHeader(CouchDbConstants.HEADER_DOC_REV, rev);
    }

    @Test
    void testNullSaveResponseThrowsError() throws Exception {
        when(exchange.getIn().getMandatoryBody()).thenThrow(InvalidPayloadException.class);
        assertThrows(InvalidPayloadException.class, () -> {
            producer.process(exchange);
        });
    }

    @Test
    void testDeleteResponse() throws Exception {
        String id = UUID.randomUUID().toString();
        String rev = UUID.randomUUID().toString();

        Document doc = new Document.Builder()
                .id(id)
                .add("_rev", rev)
                .build();

        DocumentResult documentResult = mock(DocumentResult.class, Answers.RETURNS_DEEP_STUBS);
        when(msg.getHeader(CouchDbConstants.HEADER_METHOD, String.class)).thenReturn("DELETE");
        when(msg.getMandatoryBody()).thenReturn(doc);
        when(client.removeByIdAndRev(id, rev)).thenReturn(response);
        when(response.getResult()).thenReturn(documentResult);
        when(response.getResult().getId()).thenReturn(id);
        when(response.getResult().getRev()).thenReturn(rev);

        producer.process(exchange);
        verify(msg).setHeader(CouchDbConstants.HEADER_DOC_ID, id);
        verify(msg).setHeader(CouchDbConstants.HEADER_DOC_REV, rev);
    }

    @Test
    void testGetResponse() throws Exception {
        String id = UUID.randomUUID().toString();

        JsonObject doc = new JsonObject();
        doc.addProperty("_id", id);

        when(msg.getHeader(CouchDbConstants.HEADER_METHOD, String.class)).thenReturn("GET");
        when(msg.getHeader(CouchDbConstants.HEADER_DOC_ID, String.class)).thenReturn(id);
        when(msg.getMandatoryBody()).thenReturn(doc);
        when(client.get(id)).thenReturn(response);

        producer.process(exchange);
        verify(msg).getHeader(CouchDbConstants.HEADER_DOC_ID, String.class);
    }

    @Test
    void testStringBodyIsConvertedToJsonTree() throws Exception {
        when(msg.getMandatoryBody()).thenReturn("{ \"name\" : \"coldplay\" }");
        when(client.save(any())).thenAnswer(new Answer<Response>() {

            @Override
            public Response answer(InvocationOnMock invocation) {
                assertTrue(invocation.getArguments()[0] instanceof Document,
                        invocation.getArguments()[0].getClass() + " but wanted " + Document.class);

                DocumentResult documentResult = mock(DocumentResult.class);
                Response response = mock(Response.class);
                when(response.getResult()).thenReturn(documentResult);

                return response;
            }
        });
        producer.process(exchange);
        verify(client).save(any(Document.class));
    }
}
