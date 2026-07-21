/*
 * This is the source code of VortexGram for Android.
 *
 * We do not and cannot prevent the use of our code,
 * but be respectful and credit the original author.
 *
 * Copyright @Radolyn, 2023
 */

package com.radolyn.ayugram.proprietary;

import android.text.TextUtils;
import com.radolyn.ayugram.AyuConstants;
import com.radolyn.ayugram.database.entities.AyuMessageBase;
import org.telegram.messenger.FileLog;
import org.telegram.tgnet.SerializedData;
import org.telegram.tgnet.TLRPC;

import java.util.ArrayList;

public class AyuHistoryHook {

    static void mapFromBase(AyuMessageBase base, TLRPC.Message msg, int currentAccount) {
        msg.id = base.messageId;
        msg.message = base.text != null ? base.text : "";
        msg.flags = base.flags;
        msg.date = base.date;
        msg.edit_date = base.editDate;
        msg.views = base.views;
        msg.grouped_id = base.groupedId;
        msg.dialog_id = base.dialogId;

        if (base.fromId != 0) {
            msg.from_id = peerFromId(base.fromId);
            msg.flags |= 0x100;
        }
        msg.peer_id = peerFromId(base.peerId != 0 ? base.peerId : base.dialogId);

        if (base.textEntities != null && base.textEntities.length > 0) {
            try {
                SerializedData sd = new SerializedData(base.textEntities);
                int count = sd.readInt32(false);
                if (count > 0 && count < 10000) {
                    msg.entities = new ArrayList<>();
                    for (int i = 0; i < count; i++) {
                        TLRPC.MessageEntity entity = TLRPC.MessageEntity.TLdeserialize(sd, sd.readInt32(false), false);
                        if (entity != null) {
                            msg.entities.add(entity);
                        }
                    }
                }
            } catch (Exception ignored) {}
        }

        if (base.fwdFromId != 0 || !TextUtils.isEmpty(base.fwdName)) {
            TLRPC.TL_messageFwdHeader fwd = new TLRPC.TL_messageFwdHeader();
            fwd.flags = base.fwdFlags;
            fwd.date = base.fwdDate;
            fwd.post_author = base.fwdPostAuthor;
            if (base.fwdFromId != 0) {
                fwd.from_id = peerFromId(base.fwdFromId);
                fwd.flags |= 1;
            }
            if (!TextUtils.isEmpty(base.fwdName)) {
                fwd.from_name = base.fwdName;
                fwd.flags |= 32;
            }
            msg.fwd_from = fwd;
            msg.flags |= 4;
        }

        if (base.replyMessageId != 0) {
            TLRPC.TL_messageReplyHeader reply = new TLRPC.TL_messageReplyHeader();
            reply.flags = base.replyFlags;
            reply.reply_to_msg_id = base.replyMessageId;
            reply.reply_to_top_id = base.replyTopId;
            reply.forum_topic = base.replyForumTopic;
            if (base.replyPeerId != 0) {
                reply.reply_to_peer_id = peerFromId(base.replyPeerId);
                reply.flags |= 1;
            }
            msg.reply_to = reply;
            msg.flags |= 8;
        }
    }

    static void mapMediaFromBase(AyuMessageBase base, TLRPC.Message msg) {
        if (base.documentType == AyuConstants.DOCUMENT_TYPE_NONE || TextUtils.isEmpty(base.mediaPath)) {
            return;
        }
        if (base.documentType == AyuConstants.DOCUMENT_TYPE_PHOTO) {
            TLRPC.TL_photo photo = new TLRPC.TL_photo();
            photo.sizes = new ArrayList<>();
            TLRPC.TL_photoSizeEmpty size = new TLRPC.TL_photoSizeEmpty();
            size.type = "s";
            photo.sizes.add(size);
            TLRPC.TL_messageMediaPhoto media = new TLRPC.TL_messageMediaPhoto();
            media.photo = photo;
            media.flags |= 1;
            msg.media = media;
            msg.flags |= 512;
        } else if (base.documentType == AyuConstants.DOCUMENT_TYPE_STICKER || base.documentType == AyuConstants.DOCUMENT_TYPE_FILE) {
            if (base.documentSerialized != null && base.documentSerialized.length > 0) {
                try {
                    SerializedData sd = new SerializedData(base.documentSerialized);
                    TLRPC.Document doc = TLRPC.Document.TLdeserialize(sd, sd.readInt32(false), false);
                    if (doc != null) {
                        TLRPC.TL_messageMediaDocument media = new TLRPC.TL_messageMediaDocument();
                        media.document = doc;
                        media.flags |= 1;
                        msg.media = media;
                        msg.flags |= 512;
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private static TLRPC.Peer peerFromId(long id) {
        if (id > 0) {
            TLRPC.TL_peerUser p = new TLRPC.TL_peerUser();
            p.user_id = id;
            return p;
        } else {
            TLRPC.TL_peerChat p = new TLRPC.TL_peerChat();
            p.chat_id = -id;
            return p;
        }
    }
}
