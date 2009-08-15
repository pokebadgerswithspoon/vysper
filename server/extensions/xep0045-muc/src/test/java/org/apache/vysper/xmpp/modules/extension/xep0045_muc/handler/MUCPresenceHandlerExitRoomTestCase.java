package org.apache.vysper.xmpp.modules.extension.xep0045_muc.handler;

import java.util.List;

import org.apache.vysper.xmpp.addressing.Entity;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Occupant;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.Room;
import org.apache.vysper.xmpp.modules.extension.xep0045_muc.model.RoomType;
import org.apache.vysper.xmpp.protocol.NamespaceURIs;
import org.apache.vysper.xmpp.protocol.ProtocolException;
import org.apache.vysper.xmpp.protocol.ResponseStanzaContainer;
import org.apache.vysper.xmpp.protocol.StanzaHandler;
import org.apache.vysper.xmpp.stanza.PresenceStanzaType;
import org.apache.vysper.xmpp.stanza.Stanza;
import org.apache.vysper.xmpp.stanza.StanzaBuilder;
import org.apache.vysper.xmpp.xmlfragment.XMLElement;
import org.apache.vysper.xmpp.xmlfragment.XMLSemanticError;

/**
 */
public class MUCPresenceHandlerExitRoomTestCase extends AbstractMUCHandlerTestCase {
    
    private Stanza exitRoom(Entity occupantJid, Entity roomJid) throws ProtocolException {
        return exitRoom(occupantJid, roomJid, null);
    }
    
    private Stanza exitRoom(Entity occupantJid, Entity roomJid, String status) throws ProtocolException {
        StanzaBuilder stanzaBuilder = StanzaBuilder.createPresenceStanza(occupantJid, roomJid, null, PresenceStanzaType.UNAVAILABLE, null, status);

        Stanza presenceStanza = stanzaBuilder.getFinalStanza();
        ResponseStanzaContainer container = handler.execute(presenceStanza, sessionContext.getServerRuntimeContext(), true, sessionContext, null);
        if(container != null) {
            return container.getResponseStanza();
        } else {
            return null;
        }
    }
    
    @Override
    protected StanzaHandler createHandler() {
        return new MUCPresenceHandler(conference);
    }
    
    public void testExitRoom() throws Exception {
        Room room = conference.findRoom(room1Jid);
        room.addOccupant(occupant1Jid, "Nick1");
        room.addOccupant(occupant2Jid, "Nick2");

        assertNull(exitRoom(occupant1Jid, room1JidWithNick));

        assertEquals(1, room.getOccupants().size());
        Occupant occupant = room.getOccupants().iterator().next();
        
        assertEquals(occupant2Jid, occupant.getJid());
    }

    public void testExitNonexistingRoom() throws Exception {
        // Quietly ignore
        assertNull(exitRoom(occupant1Jid, room2JidWithNick));
    }

    public void testExitRoomWithoutEntering() throws Exception {
        // Exit a room where the user is not a participant, quietly ignore
        assertNull(exitRoom(occupant1Jid, room1JidWithNick));
    }

    public void testTemporaryRoomDeleted() throws Exception {
        // Room1 is temporary
        Room room = conference.findRoom(room1Jid);
        assertTrue(room.isRoomType(RoomType.Temporary));
        room.addOccupant(occupant1Jid, "Nick1");

        // exit room, room should be deleted
        assertNull(exitRoom(occupant1Jid, room1JidWithNick));
        assertNull(conference.findRoom(room1Jid));
    }
    

    public void testPersistentRoomNotDeleted() throws Exception {
        // Room2 is persistent
        Room room = conference.createRoom(room2Jid, "Room 2", RoomType.Persistent);
        room.addOccupant(occupant1Jid, "Nick1");

        // exit room, room should be deleted
        assertNull(exitRoom(occupant1Jid, room1JidWithNick));
        assertNotNull(conference.findRoom(room1Jid));
    }
    
    public void testExitRoomWithRelays() throws Exception {
        // add occupants to the room
        Room room = conference.findOrCreateRoom(room1Jid, "Room 1");
        room.addOccupant(occupant1Jid, "Nick 1");
        room.addOccupant(occupant2Jid, "Nick 2");
        
        // now, let user 2 exit room
        exitRoom(occupant2Jid, room1JidWithNick);

        // verify stanzas to existing occupants on the exiting user
        assertExitPresenceStanza(room1Jid, "Nick 2", occupant1Jid, occupant1Queue.getNext(), null, false);
        assertExitPresenceStanza(room1Jid, "Nick 2", occupant2Jid, occupant2Queue.getNext(), null, true);
    }
    
    public void testExitRoomWithRelaysWithStatus() throws Exception {
        String statusMessage = "Custom status";
        
        // add occupants to the room
        Room room = conference.findOrCreateRoom(room1Jid, "Room 1");
        room.addOccupant(occupant1Jid, "Nick 1");
        room.addOccupant(occupant2Jid, "Nick 2");
        
        // now, let user 2 exit room
        exitRoom(occupant2Jid, room1JidWithNick, statusMessage);

        // verify stanzas to existing occupants on the exiting user
        assertExitPresenceStanza(room1Jid, "Nick 2", occupant1Jid, occupant1Queue.getNext(), statusMessage, false);
        assertExitPresenceStanza(room1Jid, "Nick 2", occupant2Jid, occupant2Queue.getNext(), statusMessage, true);
    }

    private void assertExitPresenceStanza(Entity roomJid, String nick, Entity to, Stanza stanza, String expectedStatus, boolean own) throws XMLSemanticError {
        // should be from room + nick name
        assertEquals(roomJid.getFullQualifiedName() + "/" + nick, stanza.getFrom().getFullQualifiedName());
        // should be to the existing user
        assertEquals(to, stanza.getTo());
        
        assertEquals("unavailable", stanza.getAttributeValue("type"));
        
        XMLElement xElement = stanza.getSingleInnerElementsNamed("x");
        assertEquals(NamespaceURIs.XEP0045_MUC_USER, xElement.getNamespaceURI());
        
        // since this room is non-anonymous, x must contain an item element with the users full JID
        XMLElement itemElement = xElement.getSingleInnerElementsNamed("item");
        assertEquals("none", itemElement.getAttributeValue("affiliation"));
        assertEquals("none", itemElement.getAttributeValue("role"));
        
        if(own) {
            List<XMLElement> statuses = xElement.getInnerElementsNamed("status");
            assertEquals(1, statuses.size());
            assertEquals("110", statuses.get(0).getAttributeValue("code"));
        }
        
        if(expectedStatus != null) {
            List<XMLElement> statuses = xElement.getInnerElementsNamed("status");
            assertEquals(1, statuses.size());
            assertEquals(expectedStatus, statuses.get(0).getInnerText().getText());
            
        }
    }
}