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
package org.apache.vysper.xmpp.modules.roster;

import java.util.List;

import org.apache.vysper.storage.StorageProvider;
import org.apache.vysper.xmpp.modules.DefaultModule;
import org.apache.vysper.xmpp.modules.roster.persistence.RosterManager;
import org.apache.vysper.xmpp.protocol.HandlerDictionary;
import org.apache.vysper.xmpp.server.ServerRuntimeContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * all the roster stuff assembled in a module
 *
 * @author The Apache MINA Project (dev@mina.apache.org)
 */
public class RosterModule extends DefaultModule {

    final Logger logger = LoggerFactory.getLogger(RosterModule.class);

    @Override
    public String getName() {
        return "roster";
    }

    @Override
    public String getVersion() {
        return "1.0beta";
    }

    @Override
    protected void addHandlerDictionaries(List<HandlerDictionary> dictionary) {
        dictionary.add(new RosterDictionary());
    }

    @Override
    public void initialize(ServerRuntimeContext serverRuntimeContext) {
        StorageProvider storageProvider = serverRuntimeContext.getStorageProvider(RosterManager.class);
        if (storageProvider == null) {
            logger.error("no roster storage provider found");
        }
    }
}