package com.github.laim0nas100.discordbot;

import java.util.concurrent.ExecutorService;
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

/**
 *
 * @author Lemmin
 */
public class CommanderDiscordListenerAsync extends CommanderDiscordListener {

    public CommanderDiscordListenerAsync(DiscordControlBot bot) {
        super(bot);
        cacheExecutor = bot.services.getCacheExecutor(bot.guild_id);
    }

    protected ExecutorService cacheExecutor;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        bot.services.executors.execute(() -> {
            super.onMessageReceived(event);
        });

    }

    @Override
    public void onGuildMemberUpdateNickname(GuildMemberUpdateNicknameEvent event) {
        cacheExecutor.execute(() -> {
            super.onGuildMemberUpdateNickname(event);
        });
    }

    @Override
    public void onGuildMemberRemove(GuildMemberRemoveEvent event) {
        cacheExecutor.execute(() -> {
            super.onGuildMemberRemove(event);
        });
    }

    @Override
    public void onGuildMemberJoin(GuildMemberJoinEvent event) {
        cacheExecutor.execute(() -> {
            super.onGuildMemberJoin(event);
        });
    }

    @Override
    public void onGuildMemberRoleRemove(GuildMemberRoleRemoveEvent event) {
        cacheExecutor.execute(() -> {
            super.onGuildMemberRoleRemove(event);
        });
    }

    @Override
    public void onGuildMemberRoleAdd(GuildMemberRoleAddEvent event) {
        cacheExecutor.execute(() -> {
            super.onGuildMemberRoleAdd(event);
        });
    }

    @Override
    public void onUserUpdateName(UserUpdateNameEvent event) {
        cacheExecutor.execute(() -> {
            super.onUserUpdateName(event);
        });
    }

    @Override
    public void onRoleCreate(RoleCreateEvent event) {
        cacheExecutor.execute(() -> {
            super.onRoleCreate(event);
        });
    }

    @Override
    public void onRoleDelete(RoleDeleteEvent event) {
        cacheExecutor.execute(() -> {
            super.onRoleDelete(event);
        });
    }

    @Override
    public void onRoleUpdateName(RoleUpdateNameEvent event) {
        cacheExecutor.execute(() -> {
            super.onRoleUpdateName(event);
        });
    }

    @Override
    public void onChannelUpdateParent(ChannelUpdateParentEvent event) {
        cacheExecutor.execute(() -> {
            super.onChannelUpdateParent(event);
        });
    }

    @Override
    public void onChannelCreate(ChannelCreateEvent event) {
        cacheExecutor.execute(() -> {
            super.onChannelCreate(event);
        });
    }

    @Override
    public void onChannelDelete(ChannelDeleteEvent event) {
        cacheExecutor.execute(() -> {
            super.onChannelDelete(event);
        });
    }

    @Override
    public void onChannelUpdateName(ChannelUpdateNameEvent event) {
        cacheExecutor.execute(() -> {
            super.onChannelUpdateName(event);
        });
    }

}
