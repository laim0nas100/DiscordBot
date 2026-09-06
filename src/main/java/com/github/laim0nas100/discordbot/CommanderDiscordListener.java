package com.github.laim0nas100.discordbot;

import static com.github.laim0nas100.discordbot.DiscordControlBot.FAIL;
import static com.github.laim0nas100.discordbot.DiscordControlBot.OK;
import static com.github.laim0nas100.discordbot.DiscordControlBot.THUMBS_UP;
import com.github.laim0nas100.uncheckedutils.NestedException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent;
import net.dv8tion.jda.api.events.channel.ChannelDeleteEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateNameEvent;
import net.dv8tion.jda.api.events.channel.update.ChannelUpdateParentEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleAddEvent;
import net.dv8tion.jda.api.events.guild.member.GuildMemberRoleRemoveEvent;
import net.dv8tion.jda.api.events.guild.member.update.GuildMemberUpdateNicknameEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.events.role.RoleCreateEvent;
import net.dv8tion.jda.api.events.role.RoleDeleteEvent;
import net.dv8tion.jda.api.events.role.update.RoleUpdateNameEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateNameEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author laim0nas100
 */
public class CommanderDiscordListener extends ListenerAdapter {

    public final DiscordControlBot bot;

    public CommanderDiscordListener(DiscordControlBot bot) {
        this.bot = Objects.requireNonNull(bot);
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        if (event.getChannelType() == ChannelType.CATEGORY) {
            bot.getCache().updateCategoryRenamed(event.getChannel().getId(), event.getNewValue());
        } else if (event.getChannelType() == ChannelType.TEXT) {
            bot.getCache().updateChannelRenamed(event.getChannel().getId(), event.getNewValue());
        }

    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        if (event.getChannelType() == ChannelType.CATEGORY) {
            bot.getCache().updateCategoryDeleted(event.getChannel().getId());
        } else if (event.getChannelType() == ChannelType.TEXT) {
            bot.getCache().updateChannelDeleted(event.getChannel().getId());
        }
        //ignore the rest
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }

        if (event.getChannelType() == ChannelType.CATEGORY) {
            bot.getCache().updateCategoryCreated(event.getChannel().asCategory());
        } else if (event.getChannelType() == ChannelType.TEXT) {
            bot.getCache().updateChannelCreated(event.getChannel().asTextChannel());
        }
    }

    @Override
    public void onChannelUpdateParent(ChannelUpdateParentEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        bot.getCache().updateChannelParent(event.getChannel().getId(), event.getNewValue().getId());
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        bot.getCache().updateRoleName(event.getRole().getId(), event.getNewName());
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        bot.getCache().updateRoleDelete(event.getRole().getId());
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }

        bot.getCache().updateRoleCreate(event.getRole());
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        bot.getCache().updateUserNameChange(event.getUser().getId(), event.getNewName());
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        List<String> roleIds = event.getRoles().stream().map(m -> m.getId()).collect(Collectors.toList());
        bot.getCache().updateRoleAddToUser(event.getUser().getId(), roleIds);
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        List<String> roleIds = event.getRoles().stream().map(m -> m.getId()).collect(Collectors.toList());
        bot.getCache().updateRoleRemoveFromUser(event.getUser().getId(), roleIds);
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }

        bot.getCache().updateAddUser(event.getMember());
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }

        bot.getCache().updateRemoveUser(event.getUser().getId());
    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }

        bot.getCache().updateMemberNicknameChange(event.getMember().getId(), event.getNewNickname());
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (bot.shouldIgnore(event)) {
            return;
        }
        Message message = event.getMessage();

        String content = message.getContentRaw();
        if (!content.startsWith(bot.commandPrefix)) {
            return; // Not a command
        }

        String noPrefix = content.substring(bot.commandPrefix.length());
        if (StringUtils.isBlank(noPrefix)) {
            return; //empty message after command prefix
        }

        String cmd = "";
        int index = noPrefix.indexOf(" ");
        if (index > 0) {// has space
            cmd = noPrefix.substring(0, index).toLowerCase();
        } else {
            cmd = noPrefix.toLowerCase(); //no spaces
        }

        if (!bot.isCommandEnabled(cmd, event)) {
            return;
        }
        // Acknowledge with 👍
        message.addReaction(THUMBS_UP).queue();// thumbs up
        String rest = "";
        if (index > 0) {
            rest = StringUtils.substring(noPrefix, index + 1);//remove the cmd and first space, empty otherwise
        }

        boolean success = false;
        String failureReason = null;
        try {
            DiscordMessageEventHandler handler = bot.eventHandlers.getOrDefault(cmd, null);
            if (handler != null) {//execute
                handler.acceptUnchecked(rest, event);
                success = true;
            }
        } catch (Throwable th) {
            Throwable unwrap = NestedException.unwrap(th);
            failureReason = unwrap.getClass().getSimpleName() + " " + unwrap.getMessage();
        }

        // React with ✅ or ❌
        message.addReaction(success ? OK : FAIL).queue();
        if (failureReason != null) {
            message.reply(failureReason).complete();
        }

    }
}
