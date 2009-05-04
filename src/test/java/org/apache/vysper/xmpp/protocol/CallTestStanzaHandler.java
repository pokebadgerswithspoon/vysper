/***********************************************************************
 * Copyright (c) 2006-2007 The Apache Software Foundation.             *
 * All rights reserved.                                                *
 * ------------------------------------------------------------------- *
 * Licensed under the Apache License, Version 2.0 (the "License"); you *
 * may not use this file except in compliance with the License. You    *
 * may obtain a copy of the License at:                                *
 *                                                                     *
 *     http://www.apache.org/licenses/LICENSE-2.0                      *
 *                                                                     *
 * Unless required by applicable law or agreed to in writing, software *
 * distributed under the License is distributed on an "AS IS" BASIS,   *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or     *
 * implied.  See the License for the specific language governing       *
 * permissions and limitations under the License.                      *
 ***********************************************************************/

package org.apache.vysper.xmpp.protocol;

import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.xmlfragment.XMLElement;

public class CallTestStanzaHandler implements StanzaHandler {
    private boolean handlerCalled = false;
    private String name;
    private String namespaceURI = null;
    private ProtocolException exception = null;
    private boolean verifyCalled = false;

    public CallTestStanzaHandler(String name, String namespaceURI) {
        this.name = name;
        this.namespaceURI = namespaceURI;
    }

    public CallTestStanzaHandler(String name) {
        this(name, null);
    }

    public String getName() {
        return name;
    }

    public boolean verify(Stanza stanza) {
        verifyCalled = true;
        if (name != null && !name.equals(stanza.getName())) return false;
        if (namespaceURI == null) return true;
        
        boolean elementNamespaceMatches = false;
        if (namespaceURI != null && namespaceURI.equals(stanza.getNamespace())) elementNamespaceMatches = true;
        
        boolean outerNamespaceMatches = false;
        boolean innerNamespaceMatches = false;
        if (namespaceURI != null && namespaceURI.equals(stanza.getVerifier().getUniqueXMLNSValue())) outerNamespaceMatches = true;
        XMLElement firstInnerElement = stanza.getFirstInnerElement();
        if (firstInnerElement != null) {
            if (namespaceURI != null && namespaceURI.equals(firstInnerElement.getVerifier().getUniqueXMLNSValue())) innerNamespaceMatches = true;
        }
        return elementNamespaceMatches || outerNamespaceMatches || innerNamespaceMatches;
    }

    public boolean isSessionRequired() {
        return true;
    }

    public boolean isVerifyCalled() {
        boolean isVerifyCalled = verifyCalled;
        verifyCalled = false; // reset for next time.
        return isVerifyCalled;
    }

    public void setProtocolException(ProtocolException exception) {
        this.exception = exception;
    }

    public ResponseStanzaContainer execute(Stanza stanza, ServerRuntimeContext serverRuntimeContext, boolean isOutboundStanza, SessionContext sessionContext, SessionStateHolder sessionStateHolder) throws ProtocolException {
        if (stanza == null || !stanza.getName().equals(getName()) || sessionContext == null) throw new RuntimeException("test failed");
        handlerCalled = true;
        if (exception != null) throw exception;
        return null;
    }

    public void assertHandlerCalled() {
        if (!handlerCalled) throw new RuntimeException("handler not called");
        handlerCalled = false;
    }
}