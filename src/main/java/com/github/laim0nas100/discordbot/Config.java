package com.github.laim0nas100.discordbot;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/**
 *
 * @author laim0nas100
 */
public class Config {
    
    public static JDA getDefaultJDA(String bot_token) throws InterruptedException {
        JDA jda = JDABuilder.createDefault(bot_token)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableCache(CacheFlag.ROLE_TAGS)
                .enableIntents(
                        GatewayIntent.MESSAGE_CONTENT, // Required to read message text content
                        GatewayIntent.GUILD_MESSAGES, // Required for onMessageReceived
                        GatewayIntent.GUILD_MEMBERS // Required for Joins, Leaves, Nicknames, & Roles
                ).build();

        jda.awaitReady();
        return jda;
    }
    
    public static Services getDefaultConfig(String bot_token) throws InterruptedException{
        return new Services(getDefaultJDA(bot_token), true);
    }

    public DiscordControlBot createDefaultBot(String bot_token, String guild_id) throws InterruptedException {
        return new DiscordControlBot(Config.getDefaultConfig(bot_token), guild_id);
    }
}
