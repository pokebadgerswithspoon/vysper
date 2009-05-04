/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.apache.vysper.xmpp.server.response;

import org.apache.vysper.xmpp.protocol.NamespaceURIs;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;
import org.apache.vysper.xmpp.server.XMPPVersion;
import org.apache.vysper.xmpp.server.SessionContext;
import org.apache.vysper.xmpp.server.SessionState;
import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.authorization.SASLMechanism;

import java.util.List;

/**
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 * @version $Revision$ , $Date: 2009-04-21 13:13:19 +0530 (Tue, 21 Apr 2009) $
 */
public class ServerResponses {

    public Stanza getStreamOpenerForError(boolean forClient, Entity from, XMPPVersion version, Stanza errorStanza) {
        return getStreamOpener(forClient, from, null, version, errorStanza).getFinalStanza();
    }

    public Stanza getStreamOpener(boolean forClient, Entity from, XMPPVersion version, SessionContext sessionContext) {
        Stanza innerFeatureStanza;
        if (sessionContext.getState() == SessionState.INITIATED) innerFeatureStanza = getFeaturesForEncryption(sessionContext);
        else if (sessionContext.getState() == SessionState.ENCRYPTED) innerFeatureStanza = getFeaturesForAuthentication(sessionContext.getServerRuntimeContext().getServerFeatures().getAuthenticationMethods());
        else if (sessionContext.getState() == SessionState.AUTHENTICATED) {
            sessionContext.setIsReopeningXMLStream();
            innerFeatureStanza = getFeaturesForSession();
        } else {
            throw new IllegalStateException("unsupported state for responding with stream opener");
        }

        StanzaBuilder stanzaBuilder = getStreamOpener(forClient, from, sessionContext.getXMLLang(), version, sessionContext.getSessionId(), innerFeatureStanza);

        return stanzaBuilder.getFinalStanza();
    }

    public StanzaBuilder getStreamOpener(boolean forClient, Entity from, String xmlLang, XMPPVersion version, Stanza innerStanza) {
        return getStreamOpener(forClient, from, xmlLang, version, null, innerStanza);
    }

    public StanzaBuilder getStreamOpener(boolean forClient, Entity from, String xmlLang, XMPPVersion version, String sessionId, Stanza innerStanza) {
        StanzaBuilder stanzaBuilder = new StanzaBuilder("stream", NamespaceURIs.HTTP_ETHERX_JABBER_ORG_STREAMS)
            .addNamespaceAttribute(forClient ?  NamespaceURIs.JABBER_CLIENT : NamespaceURIs.JABBER_SERVER)
            .addNamespaceAttribute("stream", NamespaceURIs.HTTP_ETHERX_JABBER_ORG_STREAMS)
            .addAttribute("from", from.getFullQualifiedName());
        if (xmlLang != null) stanzaBuilder.addAttribute("xml:lang", xmlLang);
        if (version != null) stanzaBuilder.addAttribute("version", version.toString());
        if (forClient && sessionId != null) stanzaBuilder.addAttribute("id", sessionId);
        if (innerStanza != null) stanzaBuilder.addPreparedElement(innerStanza);
        return stanzaBuilder;
    }

    public Stanza getFeaturesForEncryption(SessionContext sessionContext) {

        StanzaBuilder stanzaBuilder = new StanzaBuilder("features");
        stanzaBuilder.startInnerElement("starttls")
            .addNamespaceAttribute(NamespaceURIs.URN_IETF_PARAMS_XML_NS_XMPP_TLS);
            if (sessionContext.getServerRuntimeContext().getServerFeatures().isStartTLSRequired()) {
                stanzaBuilder.startInnerElement("required").endInnerElement();
            }
        stanzaBuilder.endInnerElement();

        return stanzaBuilder.getFinalStanza();
    }

    public Stanza getFeaturesForAuthentication(List<SASLMechanism> authenticationMethods) {

        StanzaBuilder stanzaBuilder = new StanzaBuilder("features");

        stanzaBuilder.startInnerElement("mechanisms")
            .addNamespaceAttribute(NamespaceURIs.URN_IETF_PARAMS_XML_NS_XMPP_SASL);
            for (SASLMechanism authenticationMethod : authenticationMethods) {
                stanzaBuilder.startInnerElement("mechanism").addText(authenticationMethod.getName()).endInnerElement();
            }
            stanzaBuilder.endInnerElement();

        return stanzaBuilder.getFinalStanza();
    }

    private Stanza getFeaturesForSession() {
        StanzaBuilder stanzaBuilder = new StanzaBuilder("features");

        stanzaBuilder.startInnerElement("bind")
            .addNamespaceAttribute(NamespaceURIs.URN_IETF_PARAMS_XML_NS_XMPP_BIND)
            .startInnerElement("required").endInnerElement();
        stanzaBuilder.endInnerElement();

        // session establishment is here for RFC3921 compatibility and is planed to be removed in revisions of this RFC.
        stanzaBuilder.startInnerElement("session")
            .addNamespaceAttribute(NamespaceURIs.URN_IETF_PARAMS_XML_NS_XMPP_SESSION)
            .startInnerElement("required").endInnerElement();
        stanzaBuilder.endInnerElement();

        return stanzaBuilder.getFinalStanza();
    }

    public Stanza getTLSProceed() {

        StanzaBuilder stanzaBuilder = new StanzaBuilder("proceed");
        stanzaBuilder.addNamespaceAttribute(NamespaceURIs.URN_IETF_PARAMS_XML_NS_XMPP_TLS);
        return stanzaBuilder.getFinalStanza();
    }

    public Stanza getAuthAborted() {

        StanzaBuilder stanzaBuilder = new StanzaBuilder("aborted");
        stanzaBuilder.addNamespaceAttribute(NamespaceURIs.URN_IETF_PARAMS_XML_NS_XMPP_TLS);
        return stanzaBuilder.getFinalStanza();
    }

}