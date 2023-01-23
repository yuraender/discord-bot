package ru.yuraender.discord.registry;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import org.apache.commons.lang3.EnumUtils;
import org.jooq.Record;
import org.jooq.Result;
import ru.yuraender.discord.entity.BotGuild;
import ru.yuraender.discord.entity.BotUser;
import ru.yuraender.discord.entity.enums.Permission;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.music.speechkit.YandexSpeechkitAPI;
import ru.yuraender.discord.mysql.SQL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

public class GuildRegistry {

    @Getter
    private final Set<BotGuild> guilds = Sets.newConcurrentHashSet();

    public void loadAll(JDA jda) {
        Result<Record> records = SQL.getContext()
                .selectFrom(table("guilds"))
                .fetch();
        for (Record record : records) {
            long id = record.get("id", long.class);
            if (jda.getGuildById(id) == null) {
                delete(new BotGuild(id, 0, 0, 0, null));
                continue;
            }
            long channelId = record.get("channel_id", long.class);
            long privateChannelId = record.get("private_channel_id", long.class);
            long muteRoleId = record.get("mute_role_id", long.class);
            Result<Record> userRecords = SQL.getContext()
                    .selectFrom(table("users"))
                    .where(field("server_id").equal(id))
                    .fetch();
            List<BotUser> users = new ArrayList<>();
            for (Record userRecord : userRecords) {
                long userId = userRecord.get("id", long.class);
                if (jda.getGuildById(id).getMemberById(userId) == null) {
                    removeUser(new BotUser(
                            userId, id,
                            Sets.newHashSet(), Sets.newHashSet(),
                            YandexSpeechkitAPI.Voice.JANE, YandexSpeechkitAPI.Voice.Emotion.GOOD
                    ), false);
                    continue;
                }
                Set<PermissionGroup> groups = Arrays
                        .stream(userRecord.get("groups", String.class).split(","))
                        .filter(g -> EnumUtils.isValidEnum(PermissionGroup.class, g))
                        .map(PermissionGroup::valueOf)
                        .collect(Collectors.toSet());
                Set<Permission> permissions = Arrays
                        .stream(userRecord.get("permissions", String.class).split(","))
                        .filter(p -> EnumUtils.isValidEnum(PermissionGroup.class, p))
                        .map(Permission::valueOf)
                        .collect(Collectors.toSet());
                users.add(new BotUser(
                        userId, userRecord.get("server_id", long.class),
                        groups, permissions,
                        userRecord.get("voice", YandexSpeechkitAPI.Voice.class),
                        userRecord.get("emotion", YandexSpeechkitAPI.Voice.Emotion.class)
                ));
            }
            guilds.add(new BotGuild(id, channelId, privateChannelId, muteRoleId, users));
        }
        for (Guild guild : jda.getGuilds()) {
            if (get(guild.getIdLong()) == null) {
                create(guild);
            }
        }
    }

    public void create(Guild guild) {
        BotGuild botGuild = new BotGuild(guild.getIdLong(), 0, 0, 0, Lists.newArrayList());
        SQL.async(create -> create.insertInto(table("guilds"))
                .values(botGuild.getId(),
                        botGuild.getChannelId(), botGuild.getPrivateChannelId(), botGuild.getMuteRoleId())
                .execute());
        guilds.add(botGuild);
    }

    public BotGuild get(long id) {
        return guilds.stream().filter(g -> g.getId() == id).findAny().orElse(null);
    }

    public void removeUser(BotUser user, boolean cache) {
        if (cache) {
            get(user.getServerId()).getUsers().remove(user);
        }
        SQL.async(create -> create.deleteFrom(table("users"))
                .where(
                        field("id").equal(user.getId()),
                        field("server_id").equal(user.getServerId())
                )
                .execute());
    }

    public void saveUser(BotUser user) {
        SQL.async(create -> create.batched(c -> {
            if (user.getGroups().size() == 1 && user.getPermissions().size() == 0
                    && user.getVoice() == YandexSpeechkitAPI.Voice.JANE
                    && user.getEmotion() == YandexSpeechkitAPI.Voice.Emotion.GOOD) {
                removeUser(user, true);
                return;
            }
            List<String> groups = user.getGroups()
                    .stream()
                    .map(PermissionGroup::name)
                    .collect(Collectors.toList());
            List<String> permissions = user.getPermissions()
                    .stream()
                    .map(Permission::name)
                    .collect(Collectors.toList());
            int count = c.dsl().selectCount().from(table("users"))
                    .where(
                            field("id").equal(user.getId()),
                            field("server_id").equal(user.getServerId())
                    )
                    .fetchOne()
                    .value1();
            if (count != 0) {
                create.update(table("users"))
                        .set(field("groups"), String.join(",", groups))
                        .set(field("permissions"), String.join(",", permissions))
                        .set(field("voice"), user.getVoice().name())
                        .set(field("emotion"), user.getEmotion().name())
                        .where(
                                field("id").equal(user.getId()),
                                field("server_id").equal(user.getServerId())
                        )
                        .execute();
            } else {
                get(user.getServerId()).getUsers().add(user);
                c.dsl().insertInto(table("users"))
                        .values(user.getId(), user.getServerId(),
                                String.join(",", groups), String.join(",", permissions))
                        .execute();
            }
        }));
    }

    public void save(BotGuild botGuild) {
        SQL.async(create -> create.update(table("guilds"))
                .set(field("channel_id"), botGuild.getChannelId())
                .set(field("private_channel_id"), botGuild.getPrivateChannelId())
                .set(field("mute_role_id"), botGuild.getMuteRoleId())
                .where(field("id").equal(botGuild.getId()))
                .execute());
    }

    public void delete(BotGuild guild) {
        guilds.remove(guild);
        SQL.async(create -> create.deleteFrom(table("guilds"))
                .where(field("id").equal(guild.getId()))
                .execute());
    }
}
