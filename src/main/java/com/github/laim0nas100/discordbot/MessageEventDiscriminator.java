package com.github.laim0nas100.discordbot;

import java.util.List;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 *
 * @author laim0nas100
 */
public interface MessageEventDiscriminator {

    public String description();

    public boolean test(DiscordControlBot bot, MessageReceivedEvent event);

    public static final MessageEventDiscriminator ENABLED_EVERYWHERE = new MessageEventDiscriminator() {
        @Override
        public String description() {
            return "enabled everywhere";
        }

        @Override
        public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
            return true;
        }
    };

    public static MessageEventDiscriminator byChannelName(String name) {
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "enabled on channel by name:" + name;
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                return event.getChannel().getName().equals(name);
            }
        };
    }

    public static MessageEventDiscriminator byCategory(String name) {
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "enabled on channel in category:" + name;
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                Category category = event.getMessage().getCategory();
                if (category != null) {
                    return category.getName().equals(name);
                }
                return false;
            }
        };
    }

    public static MessageEventDiscriminator withRole(String role) {
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "enabled for users with role:" + role;
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                return bot.getCache().userHasRole(event.getAuthor().getId(), role);

            }
        };
    }

    public static MessageEventDiscriminator and(List<MessageEventDiscriminator> discriminators) {
        if (discriminators.isEmpty()) {
            throw new IllegalArgumentException("Empty list of discriminators");
        }
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                StringBuilder sb = new StringBuilder();
                boolean first = true;
                for (MessageEventDiscriminator disc : discriminators) {

                    if (first) {
                        first = false;
                    } else {
                        sb.append(" and ");
                    }
                    sb.append(disc.description());
                }
                return sb.toString();
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent t) {
                for (MessageEventDiscriminator disc : discriminators) {
                    if (!disc.test(bot, t)) {
                        return false;
                    }
                }
                return true;
            }
        };
    }
}
