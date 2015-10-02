/*
 * @#WsCommunicationVPNServer.java - 2015
 * Copyright bitDubai.com., All rights reserved.
 * You may not modify, use, reproduce or distribute this software.
 * BITDUBAI/CONFIDENTIAL
 */
package com.bitdubai.fermat_p2p_plugin.layer.ws.communications.cloud.server.developer.bitdubai.version_1.structure.vpn;

import com.bitdubai.fermat_api.layer.all_definition.components.interfaces.PlatformComponentProfile;
import com.bitdubai.fermat_api.layer.all_definition.crypto.asymmetric.AsymmectricCryptography;
import com.bitdubai.fermat_api.layer.all_definition.crypto.asymmetric.ECCKeyPair;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.contents.FermatMessageCommunication;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.contents.FermatPacketCommunicationFactory;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.contents.FermatPacketDecoder;
import com.bitdubai.fermat_p2p_api.layer.all_definition.communication.commons.contents.FermatPacketEncoder;
import com.bitdubai.fermat_p2p_api.layer.p2p_communication.commons.contents.FermatPacket;
import com.bitdubai.fermat_p2p_api.layer.p2p_communication.commons.enums.FermatPacketType;
import com.bitdubai.fermat_p2p_api.layer.p2p_communication.commons.enums.JsonAttNamesConstants;
import com.bitdubai.fermat_p2p_plugin.layer.ws.communications.cloud.server.developer.bitdubai.version_1.structure.WsCommunicationCloudServer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.java_websocket.WebSocket;
import org.java_websocket.handshake.ClientHandshake;
import org.java_websocket.server.WebSocketServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Class <code>com.bitdubai.fermat_p2p_plugin.layer.ws.communications.cloud.server.developer.bitdubai.version_1.structure.WsCommunicationVPNServer</code> is
 * a communication vpn server
 * <p/>
 * Created by Roberto Requena - (rart3001@gmail.com) on 12/09/15.
 *
 * @version 1.0
 * @since Java JDK 1.7
 */
public class WsCommunicationVPNServer extends WebSocketServer{

    /**
     * Represent the WS_PROTOCOL
     */
    private static final String WS_PROTOCOL = "ws://";

    /**
     * Represent the vpnServerIdentity
     */
    private ECCKeyPair vpnServerIdentity;

    /**
     * Represent the registered participantsConnections to this vpn
     */
    private List<PlatformComponentProfile> registeredParticipants;

    /**
     * Holds all the participants connections
     */
    private Map<String, WebSocket> participantsConnections;

    /**
     * Holds all the vpnClientIdentity By Participants
     */
    private Map<String, String> vpnClientIdentityByParticipants;

    /**
     * Represent the wsCommunicationCloudServer
     */
    private WsCommunicationCloudServer wsCommunicationCloudServer;

    /**
     * Constructor with parameters
     *
     * @param address
     * @param registeredParticipants
     */
    public WsCommunicationVPNServer(InetSocketAddress address, List<PlatformComponentProfile> registeredParticipants, WsCommunicationCloudServer wsCommunicationCloudServer) {
        super(address);
        this.vpnServerIdentity               = new ECCKeyPair();
        this.registeredParticipants          = registeredParticipants;
        this.participantsConnections = new ConcurrentHashMap<>();
        this.vpnClientIdentityByParticipants = new ConcurrentHashMap<>();
        this.wsCommunicationCloudServer      = wsCommunicationCloudServer;


        participantsConnections.clear();
        vpnClientIdentityByParticipants.clear();
        vpnClientIdentityByParticipants.clear();
    }

    /**
     * (non-javadoc)
     * @see WebSocketServer#onOpen(WebSocket, ClientHandshake)
     */
    @Override
    public void onOpen(WebSocket clientConnection, ClientHandshake handshake) {

        System.out.println(" --------------------------------------------------------------------- ");
        System.out.println(" WsCommunicationVPNServer - Starting method onOpen");
        System.out.println(" WsCommunicationVPNServer - New Participant Client: "+clientConnection.getRemoteSocketAddress().getAddress().getHostAddress() + " is connected!");
        System.out.println(" WsCommunicationVPNServer - tmp-i = " + handshake.getFieldValue(JsonAttNamesConstants.HEADER_ATT_NAME_TI));

        /*
         * Validate is a handshake valid
         */
        if (handshake.getFieldValue(JsonAttNamesConstants.HEADER_ATT_NAME_TI)     != null &&
                handshake.getFieldValue(JsonAttNamesConstants.HEADER_ATT_NAME_TI) != ""     ){

            boolean isRegistered = Boolean.FALSE;

            String messageContentJsonStringRepresentation =  AsymmectricCryptography.decryptMessagePrivateKey(handshake.getFieldValue(JsonAttNamesConstants.HEADER_ATT_NAME_TI), vpnServerIdentity.getPrivateKey());

            System.out.println(" WsCommunicationVPNServer - messageContentJsonStringRepresentation = " + messageContentJsonStringRepresentation);

            JsonParser parser = new JsonParser();
            JsonObject respond = parser.parse(messageContentJsonStringRepresentation).getAsJsonObject();

            /*
             * Get the identity send by the participant
             */
            String participantIdentity =  respond.get(JsonAttNamesConstants.JSON_ATT_NAME_REGISTER_PARTICIPANT_IDENTITY_VPN).getAsString();
            String vpnClientIdentity   =  respond.get(JsonAttNamesConstants.JSON_ATT_NAME_CLIENT_IDENTITY_VPN).getAsString();

            for (PlatformComponentProfile registeredParticipant : registeredParticipants) {

                //Validate if registered
                if (registeredParticipant.getIdentityPublicKey().equals(participantIdentity)) {
                    isRegistered = Boolean.TRUE;
                }

            }

            //If not registered close the connection
            if (!isRegistered){
                clientConnection.closeConnection(404, "NOT A PARTICIPANT REGISTER FOR THIS VPN");
            }else {
                participantsConnections.put(participantIdentity, clientConnection);
                vpnClientIdentityByParticipants.put(participantIdentity, vpnClientIdentity);
            }
            
            System.out.println(" WsCommunicationVPNServer - registeredParticipants.size() = " + registeredParticipants.size());
            System.out.println(" WsCommunicationVPNServer - participantsConnections.size() = " + participantsConnections.size());
            System.out.println(" WsCommunicationVPNServer - Integer.compare(registeredParticipants.size(), participantsConnections.size()) == 0 = " + (Integer.compare(registeredParticipants.size(), participantsConnections.size()) == 0));

            //Validate if all participantsConnections register are connect
            if(Integer.compare(registeredParticipants.size(), participantsConnections.size()) == 0){

                PlatformComponentProfile peer1 = registeredParticipants.get(0);
                PlatformComponentProfile peer2 = registeredParticipants.get((registeredParticipants.size()-1));

                sendNotificationPacketConnectionComplete(peer1, peer2);
                sendNotificationPacketConnectionComplete(peer2, peer1);

            }

        }else {
            clientConnection.closeConnection(404, "DENIED, NOT VALID HANDSHAKE");
        }

    }

    /**
     * Construct a packet whit the information that a vpn is ready
     *
     * @param destinationPlatformComponentProfile
     * @param remotePlatformComponentProfile
     */
    private void sendNotificationPacketConnectionComplete(PlatformComponentProfile destinationPlatformComponentProfile, PlatformComponentProfile remotePlatformComponentProfile){

        System.out.println(" WsCommunicationVPNServer - sendNotificationPacketConnectionComplete = " + destinationPlatformComponentProfile.getIdentityPublicKey());

         /*
         * Construct the content of the msj
         */
        Gson gson = new Gson();
        JsonObject packetContent = new JsonObject();
        packetContent.addProperty(JsonAttNamesConstants.JSON_ATT_NAME_REMOTE_PARTICIPANT_VPN,  remotePlatformComponentProfile.toJson());


        /*
         * Get the connection client of the destination
         * IMPORTANT: No send by vpn connection, no support this type of packet
         */
        WebSocket clientConnectionDestination = wsCommunicationCloudServer.getRegisteredClientConnectionsCache().get(destinationPlatformComponentProfile.getCommunicationCloudClientIdentity());

        /*
        * Construct a new fermat packet whit the same message and different destination
        */
        FermatPacket fermatPacketRespond = FermatPacketCommunicationFactory.constructFermatPacketEncryptedAndSinged(destinationPlatformComponentProfile.getCommunicationCloudClientIdentity(), //Destination
                                                                                                                    wsCommunicationCloudServer.getServerIdentityByClientCache().get(clientConnectionDestination.hashCode()).getPublicKey(),                                  //Sender
                                                                                                                    gson.toJson(packetContent),                                        //Message Content
                                                                                                                    FermatPacketType.COMPLETE_COMPONENT_CONNECTION_REQUEST,            //Packet type
                                                                                                                    wsCommunicationCloudServer.getServerIdentityByClientCache().get(clientConnectionDestination.hashCode()).getPrivateKey());                                //Sender private key
        /*
        * Send the encode packet to the destination
        */
        clientConnectionDestination.send(FermatPacketEncoder.encode(fermatPacketRespond));

    }



    /**
     * (non-javadoc)
     * @see WebSocketServer#onClose(WebSocket, int, String, boolean)
     */
    @Override
    public void onClose(WebSocket clientConnection, int code, String reason, boolean remote) {

        System.out.println(" --------------------------------------------------------------------- ");
        System.out.println(" WsCommunicationVPNServer - Starting method onClose");
        System.out.println(" WsCommunicationVPNServer - " + clientConnection.getRemoteSocketAddress() + " is disconnect! code = " + code + " reason = " + reason + " remote = " + remote);

        if (participantsConnections.size() <= 1){

            /*
             * Close all the connection
             */
            for (WebSocket conn: connections()) {
                conn.closeConnection(505, " All participantsConnections close her connections ");
            }
        }

    }

    /**
     * (non-javadoc)
     * @see WebSocketServer#onMessage(WebSocket, String)
     */
    @Override
    public void onMessage(WebSocket clientConnection, String fermatPacketEncode) {

        System.out.println("WsCommunicationVPNServer - onMessage ");

        /*
         * Decode the fermatPacketEncode into a fermatPacket
         */
        FermatPacket receiveFermatPacket = FermatPacketDecoder.decode(fermatPacketEncode, vpnServerIdentity.getPrivateKey());


        if (receiveFermatPacket.getFermatPacketType() == FermatPacketType.MESSAGE_TRANSMIT){

            /*
             * Get the FermatMessage from the message content and decrypt
             */
            String messageContentJsonStringRepresentation = AsymmectricCryptography.decryptMessagePrivateKey(receiveFermatPacket.getMessageContent(), vpnServerIdentity.getPrivateKey());

            /*
             * Construct the fermat message object
             */
            FermatMessageCommunication fermatMessage = (FermatMessageCommunication) new FermatMessageCommunication().fromJson(messageContentJsonStringRepresentation);


            System.out.println("WsCommunicationVPNServer - fermatMessage = "+fermatMessage);

            /*
             * Send the message to the other participantsConnections
             */
           // for (String participantIdentity : participantsConnections.keySet()) {

                //If participantIdentity is different from the sender, send the message
             //  if (participantIdentity != receiveFermatPacket.getSender()){

                    /*
                    * Construct a new fermat packet whit the same message and different destination
                    */
                    FermatPacket fermatPacketRespond = FermatPacketCommunicationFactory.constructFermatPacketEncryptedAndSinged(vpnClientIdentityByParticipants.get(fermatMessage.getReceiver()), //Destination
                                                                                                                                vpnServerIdentity.getPublicKey(),                         //Sender
                                                                                                                                fermatMessage.toJson(),                                   //Message Content
                                                                                                                                FermatPacketType.MESSAGE_TRANSMIT,                        //Packet type
                                                                                                                                vpnServerIdentity.getPrivateKey());                       //Sender private key

                    /*
                     * Get the connection of the destination
                     */
                    WebSocket clientConnectionDestination = participantsConnections.get(fermatMessage.getReceiver());



                    /*
                     * If the connection to client destination available
                     */
                    if (clientConnectionDestination != null && clientConnectionDestination.isOpen()){

                        System.out.println("WsCommunicationVPNServer - sending to destination "+fermatPacketRespond.getDestination());

                       /*
                        * Send the encode packet to the destination
                        */
                        clientConnectionDestination.send(FermatPacketEncoder.encode(fermatPacketRespond));

                    }

           //    }

         //   }

        }else {
            System.out.println("WsCommunicationVPNServer - Packet type " + receiveFermatPacket.getFermatPacketType() + "is not supported");
        }

    }

    /**
     * (non-javadoc)
     * @see WebSocketServer#onError(WebSocket, Exception)
     */
    @Override
    public void onError(WebSocket clientConnection, Exception ex) {

        System.out.println(" --------------------------------------------------------------------- ");
        System.out.println(" WsCommunicationVPNServer - Starting method onError");
        ex.printStackTrace();

        /*
         * Close all the connection
         */
        for (WebSocket conn: connections()) {
            conn.closeConnection(505, "- ERROR :" + ex.getLocalizedMessage());
        }

    }

    /**
     * Indicate is active this vpn
     *
     * @return boolean
     */
    public boolean isActive(){
        return (registeredParticipants.size() == connections().size());
    }

    /**
     * Get the VpnServerIdentityPublicKey
     * @return String
     */
    public String getVpnServerIdentityPublicKey(){
       return vpnServerIdentity.getPublicKey();
    }

    /**
     * Get the UriConnection
     * @return URI
     * @throws URISyntaxException
     */
    public URI getUriConnection() {

        try {
            return new URI(WsCommunicationVPNServer.WS_PROTOCOL + getAddress().getHostString()  + ":" + getPort());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}