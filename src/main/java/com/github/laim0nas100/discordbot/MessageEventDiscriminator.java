package com.github.laim0nas100.discordbot;

import java.util.List;
import java.util.Optional;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

/**
 *
 * @author laim0nas100
 */
public interface MessageEventDiscriminator {

    public String description();

    public boolean test(DiscordControlBot bot, MessageReceivedEvent event);

    public default MessageEventDiscriminator not() {
        final MessageEventDiscriminator me = this;
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "not " + me.description();
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                return !me.test(bot, event);
            }
        };
    }

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
    
     public static MessageEventDiscriminator onReply() {
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "enabled on reply";
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                return Optional.ofNullable(event).map(m->m.getMessage()).map(m->m.getMessageReference()).isPresent();
            }
        };
    }

    public static MessageEventDiscriminator forMembers() {
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "enabled for members";
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                return Optional.ofNullable(event).map(m -> m.getMember()).isPresent();
            }
        };
    }

    public static MessageEventDiscriminator forBots() {
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "enabled for bots";
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                return Optional.ofNullable(event).map(m -> m.getAuthor()).map(m -> m.isBot()).orElse(false);
            }
        };
    }
    
    public static MessageEventDiscriminator forWebhooks() {
        return new MessageEventDiscriminator() {
            @Override
            public String description() {
                return "enabled for webhooks";
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent event) {
                return Optional.ofNullable(event).map(m -> m.isWebhookMessage()).orElse(false);
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

    public static MessageEventDiscriminator or(List<MessageEventDiscriminator> discriminators) {
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
                        sb.append(" or ");
                    }
                    sb.append(disc.description());
                }
                return sb.toString();
            }

            @Override
            public boolean test(DiscordControlBot bot, MessageReceivedEvent t) {
                for (MessageEventDiscriminator disc : discriminators) {
                    if (disc.test(bot, t)) {
                        return true;
                    }
                }
                return false;
            }
        };
    }
}
