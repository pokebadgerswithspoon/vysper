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

package org.apache.vysper.xmpp.parser;

import org.apache.vysper.xmpp.stanza.Stanza;

public class StringStreamParserTestCase extends AbstractStreamParserTestCase {

    @Override
    protected Stanza getFirstStanzaFromXML(String xml) throws ParsingException {
        StreamParser streamParser = createStreamParser(xml);
        return streamParser.getNextStanza();
    }

    @Override
    protected StreamParser createStreamParser(String xml) {
        return new StringStreamParser(xml);
    }


}