package com.github.laim0nas100.discordbot;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import com.github.laim0nas100.uncheckedutils.PassableException;
import com.github.laim0nas100.uncheckedutils.SafeOpt;
import java.util.ArrayList;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.UnicodeEmoji;
import net.dv8tion.jda.api.events.Event;
import net.dv8tion.jda.api.events.channel.GenericChannelEvent;
import net.dv8tion.jda.api.events.guild.member.GenericGuildMemberEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.role.update.GenericRoleUpdateEvent;
import net.dv8tion.jda.api.events.user.update.GenericUserUpdateEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Server (guild) localized bot instance.
 *
 * @author laim0nas100
 */
public class DiscordControlBot {

    static Logger logger = LoggerFactory.getLogger(DiscordControlBot.class);

    public static final String DEFAULT_COMMAND_PREFIX = "..";
    protected final JDA jda;

    protected final String guild_id;
    protected final String commandPrefix;

    protected final CommanderDiscordListener listener;
    protected final Map<String, DiscordMessageEventHandler> eventHandlers = new LinkedHashMap<>();

    protected final GuildCache cache;

    protected boolean ownerOnly = true;

    public DiscordControlBot(JDA jda, String guild_id) throws InterruptedException {
        this(jda, guild_id, DEFAULT_COMMAND_PREFIX);
    }

    public DiscordControlBot(JDA jda, String guild_id, String commandPrefix) {
        if (StringUtils.isBlank(commandPrefix)) {
            throw new IllegalArgumentException("Blank command prefix");
        }
        this.commandPrefix = commandPrefix;
        this.guild_id = Objects.requireNonNull(guild_id);
        this.jda = Objects.requireNonNull(jda);
        JDA.Status status = jda.getStatus();
        if (status != JDA.Status.CONNECTED) {
            throw new IllegalStateException("JDA is not connected");
        }

        this.listener = new CommanderDiscordListener(this);
        jda.addEventListener(listener);
        this.cache = new GuildCache();
        //assume the jda is ready
        cache.buildFullCache(jda, guild_id);

        logger.info("Bot is ready");
    }

    public Guild getGuild() {
        return jda.getGuildById(guild_id);
    }

    public GuildCache getCache() {
        return cache;
    }

    public static final UnicodeEmoji THUMBS_UP = Emoji.fromUnicode("\uD83D\uDC4D");
    public static final UnicodeEmoji OK = Emoji.fromUnicode("\u2705");
    public static final UnicodeEmoji FAIL = Emoji.fromUnicode("\u274C");

    protected boolean isCommandEnabled(String cmd, MessageReceivedEvent event) {
        DiscordMessageEventHandler handler = eventHandlers.get(cmd);
        if (handler == null) {
            return false;
        }
        if (handler instanceof NamedDiscordEventHandler) {
            NamedDiscordEventHandler named = (NamedDiscordEventHandler) handler;
            return named.enabled(this, event);
        }
        return true; // found handler with no discriminator, allow
    }

    protected boolean shouldIgnore(Event event) {
        if (event instanceof MessageReceivedEvent) {
            return shouldIgnoreMessage((MessageReceivedEvent) event);
        }
        if (event instanceof GenericChannelEvent) {
            return shouldIgnoreChannel((GenericChannelEvent) event);
        }
        if (event instanceof GenericRoleUpdateEvent) {
            return shouldIgnoreRole((GenericRoleUpdateEvent) event);
        }
        if (event instanceof GenericUserUpdateEvent) {
            return shouldIgnoreUser((GenericUserUpdateEvent) event);
        }

        if (event instanceof GenericGuildMemberEvent) {
            return shouldIgnoreGuildMember((GenericGuildMemberEvent) event);
        }

        //disregard everything else
        return true;
    }

    protected boolean shouldIgnoreGuildMember(GenericGuildMemberEvent event) {
        return !event.getGuild().getId().equals(guild_id);
    }

    protected boolean shouldIgnoreUser(GenericUserUpdateEvent event) {
        return !getCache().hasUser(event.getUser().getId());
    }

    protected boolean shouldIgnoreRole(GenericRoleUpdateEvent event) {
        return !event.getGuild().getId().equals(guild_id);
    }

    protected boolean shouldIgnoreChannel(GenericChannelEvent event) {
        if (!event.isFromGuild()) {
            return true;
        }
        return event.getGuild().getId().equals(guild_id);
    }

    protected boolean shouldIgnoreMessage(MessageReceivedEvent event) {
        if (!event.isFromGuild() || !event.getGuild().getId().equals(guild_id)) {
            return true;
        }

        if (ownerOnly) {
            if (event.isWebhookMessage() || event.getAuthor().isBot()) {
                return true;
            }
            Member member = event.getMember();
            if(member == null){
                return true;
            }
            return !member.isOwner();
        }
        return false;
    }

    public <T extends NamedDiscordEventHandler> SafeOpt<T> registerMessageEventHandler(T event) {
        return SafeOpt.ofNullable(event).flatMap(m -> registerMessageEventHandler(m.getCmd(), m));
    }

    public <T extends DiscordMessageEventHandler> SafeOpt<T> registerMessageEventHandler(String cmd, T event) {
        Objects.requireNonNull(cmd);
        Objects.requireNonNull(event);

        String key = cmd.toLowerCase();
        if (eventHandlers.containsKey(key)) {
            return SafeOpt.error(new PassableException(cmd + " handler is already registered"));
        }
        eventHandlers.put(key, event);
        return SafeOpt.of(event);
    }

    public static boolean purge(MessageReceivedEvent event, Message message, int limit) {

        int deleted = 0;
        for (int i = 0; i < limit; i += 100) {
            MessageHistory complete = event.getChannel().getHistoryBefore(message, Math.min(100, limit)).complete();
            List<Message> retrievedHistory = complete.getRetrievedHistory();
            event.getChannel().purgeMessages(retrievedHistory);
            deleted += retrievedHistory.size();
        }
        message.reply("Deleted " + deleted + " messages").queue(msg -> {
            message.delete().queueAfter(3, TimeUnit.SECONDS);
            msg.delete().queueAfter(3, TimeUnit.SECONDS);
        });
        return true;
    }

    public List<DiscordMessageEventHandler> getHandlers() {
        return new ArrayList<>(this.eventHandlers.values());
    }

    public SafeOpt<Void> sendMessage(String channelName, String msg) {
        return SafeOpt.ofAsync(channelName).map(name -> {
            this.cache.getChannelsByName(channelName).forEach(channel -> {
                channel.value.sendMessage(msg).queue();
            });
            return null;
        });
    }

}
