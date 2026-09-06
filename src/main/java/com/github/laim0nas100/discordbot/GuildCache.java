package com.github.laim0nas100.discordbot;

import com.github.laim0nas100.uncheckedutils.SafeOpt;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;

/**
 * Memory based cache, with updates on demand. Guild-local.
 *
 * @author laim0nas100
 */
public class GuildCache {

    protected Map<String, CachedRole> roleCache = new ConcurrentHashMap<>();
    protected Map<String, CachedMember> userCache = new ConcurrentHashMap<>();
    protected Map<String, CachedCategory> categoryCache = new ConcurrentHashMap<>();
    protected Map<String, CachedTextChannel> textChannelCache = new ConcurrentHashMap<>();

    protected void buildUserRoleCache(Guild guild, Map<String, CachedMember> userMap, Map<String, CachedRole> roleMap) {
        if (guild == null) {
            throw new IllegalArgumentException("No guild provided");
        }
        for (Role role : guild.getRoles()) {
            CachedRole r = new CachedRole(role, role.getId(), role.getName());
            roleMap.put(r.id, r);
        }

        List<Member> members = new ArrayList<>();
        members.add(guild.retrieveOwner().complete());
        members.addAll(guild.getMembers());

        for (Member member : members) {
            if (userMap.containsKey(member.getId())) {
                continue;//repeated
            }
            CachedMember m = new CachedMember(member, member.getId());
            userMap.put(m.id, m);
            List<Role> roles = member.getRoles();
            for (Role r : roles) {
                CachedRole cachedRole = roleMap.get(r.getId());
                if (cachedRole != null) {
                    cachedRole.members.computeIfAbsent(m.id, k -> m);
                }
                m.roles.computeIfAbsent(cachedRole.id, k -> cachedRole);
            }
        }
    }

    protected void buildCategoryChannelCache(Guild guild, Map<String, CachedCategory> categoryMap, Map<String, CachedTextChannel> textChannelMap) {
        if (guild == null) {
            throw new IllegalArgumentException("No guild provided");
        }
        for (Category category : guild.getCategories()) {
            CachedCategory c = new CachedCategory(category, category.getId(), category.getName());
            categoryMap.put(c.id, c);
        }
        for (TextChannel textChannel : guild.getTextChannels()) {
            CachedTextChannel t = new CachedTextChannel(textChannel, textChannel.getId(), textChannel.getName());
            textChannelMap.put(t.id, t);
            String parentCategoryId = textChannel.getParentCategoryId();
            CachedCategory category = categoryMap.get(parentCategoryId);
            if (category != null) {
                category.channels.computeIfAbsent(t.id, k -> t);
                t.category = category;
            }
        }
    }

    protected void buildFullCache(JDA jda, String guild_id) {

        Guild guild = jda.getGuildById(guild_id);

        if (guild == null) {
            throw new IllegalArgumentException("No guild by id:" + guild_id);
        }

        Map<String, CachedMember> userMap = new ConcurrentHashMap<>();
        Map<String, CachedRole> roleMap = new ConcurrentHashMap<>();
        Map<String, CachedCategory> categoryMap = new ConcurrentHashMap<>();
        Map<String, CachedTextChannel> textChannelMap = new ConcurrentHashMap<>();
        buildUserRoleCache(guild, userMap, roleMap);
        buildCategoryChannelCache(guild, categoryMap, textChannelMap);

        this.roleCache = roleMap;
        this.userCache = userMap;
        this.categoryCache = categoryMap;
        this.textChannelCache = textChannelMap;

    }

    protected void updateChannelDeleted(String channelId) {
        CachedTextChannel channel = textChannelCache.remove(channelId);
        if (channel != null) {
            channel.category.channels.remove(channelId);
        }
    }

    protected void updateCategoryDeleted(String categoryId) {
        CachedCategory category = categoryCache.remove(categoryId);
        if (category != null) {
            Collection<CachedChannel> values = category.channels.values();
            for (CachedChannel channel : values) {
                if (channel instanceof CachedTextChannel) {
                    CachedTextChannel cached = (CachedTextChannel) channel;
                    cached.category = null;
                }
            }
        }
    }

    protected void updateChannelCreated(TextChannel channel) {
        CachedTextChannel c = new CachedTextChannel(channel, channel.getId(), channel.getName());
        textChannelCache.put(c.id, c);
        String categoryId = channel.getParentCategoryId();
        if (categoryId != null) {
            CachedCategory cachedCategory = categoryCache.get(categoryId);
            if (cachedCategory != null) {
                c.category = cachedCategory;
                cachedCategory.channels.put(c.id, c);
            }
        }
    }

    protected void updateCategoryCreated(Category category) {

        CachedCategory c = new CachedCategory(category, category.getId(), category.getName());

        categoryCache.put(c.id, c);
        //should be empty
    }

    protected void updateChannelRenamed(String channelId, String newName) {
        CachedTextChannel get = textChannelCache.get(channelId);
        if (get != null) {
            get.name = newName;
        }
    }

    protected void updateCategoryRenamed(String categoryId, String newName) {
        CachedCategory get = categoryCache.get(categoryId);
        if (get != null) {
            get.name = newName;
        }
    }

    protected void updateChannelParent(String channelId, String newParentId) {
        CachedTextChannel c = textChannelCache.get(channelId);
        if (c != null) {
            CachedCategory prevCategory = c.category;
            if (prevCategory != null) {
                prevCategory.channels.remove(channelId);
            }
            CachedCategory newCategory = categoryCache.get(newParentId);
            if (newCategory != null) {
                newCategory.channels.put(channelId, c);
                c.category = newCategory;
            }
        }
    }

    protected void updateRoleName(String roleId, String newName) {
        CachedRole role = roleCache.get(roleId);
        if (role != null) {
            role.name = newName;
        }
    }

    protected void updateRoleDelete(String roleId) {
        CachedRole role = roleCache.remove(roleId);
        if (role != null) {
            role.members.values().forEach(member -> {
                member.roles.remove(role.id);
            });
        }
    }

    protected void updateRoleCreate(Role role) {
        CachedRole c = new CachedRole(role, role.getId(), role.getName());
        roleCache.put(c.id, c);
        //should be empty
    }

    protected void updateUserNameChange(String userId, String newName) {
        CachedMember c = userCache.get(userId);
        if (c != null) {
            c.name = newName;
        }
    }

    protected void updateMemberNicknameChange(String userId, String newNickname) {
        CachedMember c = userCache.get(userId);
        if (c != null) {
            c.nickName = newNickname;
        }
    }

    protected void updateRoleAddToUser(String userId, List<String> rolesIds) {
        CachedMember member = userCache.get(userId);
        if (member == null) {
            return;
        }
        for (String roleId : rolesIds) {
            CachedRole role = roleCache.get(roleId);
            if (role == null) {
                continue;
            }
            role.members.put(member.id, member);
            member.roles.put(role.id, role);
        }
    }

    protected void updateRoleRemoveFromUser(String userId, List<String> rolesIds) {
        CachedMember member = userCache.get(userId);
        if (member == null) {
            return;
        }
        for (String roleId : rolesIds) {
            CachedRole role = member.roles.get(roleId);

            if (role == null) {
                continue;
            }
            role.members.remove(member.id);
            member.roles.remove(roleId);
        }
    }

    protected void updateAddUser(Member member) {
        CachedMember m = new CachedMember(member, member.getId());
        userCache.put(m.id, m);
        // should have no roles
    }

    protected void updateRemoveUser(String userId) {
        CachedMember m = userCache.remove(userId);
        if (m != null) {
            m.roles.values().forEach(role -> {
                role.members.remove(m.id);
            });
        }
    }

    public static abstract class CachedElement<T> {

        public final T value;
        public final String id;
        public String name;

        public CachedElement(T value, String id, String name) {
            this.value = value;
            this.id = id;
            this.name = name;
        }

    }

    public static abstract class CachedChannel<T extends Channel> extends CachedElement<T> {

        public CachedChannel(T value, String id, String name) {
            super(value, id, name);
        }

    }

    public static class CachedTextChannel extends CachedChannel<TextChannel> {

        public CachedCategory category;

        public CachedTextChannel(TextChannel value, String id, String name) {
            super(value, id, name);
        }

    }

    public static class CachedCategory extends CachedChannel<Category> {

        public Map<String, CachedChannel> channels = new ConcurrentHashMap<>();

        public CachedCategory(Category value, String id, String name) {
            super(value, id, name);
        }

    }

    public static class CachedMember extends CachedElement<Member> {

        public Map<String, CachedRole> roles = new ConcurrentHashMap<>();
        public User user;
        public String nickName;

        public CachedMember(Member value, String id) {
            super(value, id, "");
            this.user = value.getUser();
            this.name = user.getName();
            this.nickName = value.getNickname();
        }

    }

    public static class CachedRole extends CachedElement<Role> {

        public Map<String, CachedMember> members = new ConcurrentHashMap<>();

        public CachedRole(Role value, String id, String name) {
            super(value, id, name);
        }

    }

    public boolean userHasRole(String userId, String roleName) {
        return SafeOpt.ofNullable(userCache.get(userId))
                .map(m -> m.roles.values().stream().anyMatch(r -> r.name.equals(roleName))).orElse(Boolean.FALSE);
    }

    public List<CachedTextChannel> getChannelsByName(String name) {
        return textChannelCache.values().stream().filter(m -> m.name.equals(name)).collect(Collectors.toList());
    }

    public boolean hasUser(String userId) {
        return userCache.containsKey(userId);
    }

}
