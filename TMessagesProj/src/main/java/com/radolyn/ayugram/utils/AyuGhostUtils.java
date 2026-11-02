/*
 * This is the source code of VortexGram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @Radolyn, 2023
 */

package com.radolyn.ayugram.utils;

import org.telegram.messenger.MessagesController;
import org.telegram.tgnet.ConnectionsManager;
import org.telegram.tgnet.TLObject;
import org.telegram.tgnet.TLRPC;

public class AyuGhostUtils {
    public static void markReadOnServer(int accountId, int messageId, TLRPC.InputPeer peer) {
        ConnectionsManager connectionsManager = ConnectionsManager.getInstance(accountId);

        TLObject req;
        if (peer instanceof TLRPC.TL_inputPeerChannel) {
            TLRPC.TL_channels_readHistory request = new TLRPC.TL_channels_readHistory();
            request.channel = MessagesController.getInputChannel(peer);
            request.max_id = messageId;
            req = request;
        } else {
            TLRPC.TL_messages_readHistory request = new TLRPC.TL_messages_readHistory();
            request.peer = peer;
            request.max_id = messageId;
            req = request;
        }

        AyuState.setAllowReadPacket(true, 1);
        connectionsManager.sendRequest(req, (response, error) -> {
            if (error == null) {
                if (response instanceof TLRPC.TL_messages_affectedMessages) {
                    TLRPC.TL_messages_affectedMessages res = (TLRPC.TL_messages_affectedMessages) response;
                    MessagesController.getInstance(accountId).processNewDifferenceParams(-1, res.pts, -1, res.pts_count);
                }
            }
        });
    }
}
