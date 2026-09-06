package com.github.laim0nas100.discordbot;

import java.util.Objects;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class NamedDiscordEventHandler implements DiscordMessageEventHandler {

    private final DiscordMessageEventHandler inner;
    private final String cmd;
    private final String param;
    private final String description;
    private final MessageEventDiscriminator discriminator;

    public NamedDiscordEventHandler(String cmd, String param, String description, MessageEventDiscriminator disc, DiscordMessageEventHandler inner) {
        this.cmd = Objects.requireNonNull(cmd);
        this.param = Objects.requireNonNull(param);
        this.description = Objects.requireNonNull(description);
        this.discriminator = Objects.requireNonNull(disc);
        this.inner = Objects.requireNonNull(inner);
    }

    public NamedDiscordEventHandler(String cmd, String args, String description, DiscordMessageEventHandler inner) {
        this(cmd, args, description, MessageEventDiscriminator.ENABLED_EVERYWHERE, inner);
    }

    @Override
    public void acceptUnchecked(String t, MessageReceivedEvent r) throws Throwable {
        inner.acceptUnchecked(t, r);
    }

    public boolean enabled(DiscordControlBot bot, MessageReceivedEvent r) {
        return discriminator.test(bot, r);
    }

    public String getCmd() {
        return cmd;
    }

    public String getParam() {
        return param;
    }

    public String getDescription() {
        return description;
    }

    public String prettyPrint() {
        StringBuilder sb = new StringBuilder();
        sb.append(getCmd());
        if (!StringUtils.isBlank(getParam())) {
            sb.append(" ").append(getParam());
        }
        sb.append(": ").append(getDescription());
        sb.append(", ").append(discriminator.description());
        return sb.toString();
    }

}
