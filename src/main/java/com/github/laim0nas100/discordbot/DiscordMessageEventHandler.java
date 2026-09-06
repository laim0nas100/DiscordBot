package com.github.laim0nas100.discordbot;

import com.github.laim0nas100.uncheckedutils.func.UncheckedBiConsumer;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 *
 * @author laim0nas100
 */
public interface DiscordMessageEventHandler extends UncheckedBiConsumer<String, MessageReceivedEvent> {

}
