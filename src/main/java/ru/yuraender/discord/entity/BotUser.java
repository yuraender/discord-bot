package ru.yuraender.discord.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.yuraender.discord.entity.enums.Permission;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.music.speechkit.YandexSpeechkitAPI;

import java.util.Collections;
import java.util.Comparator;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Setter
@AllArgsConstructor
public class BotUser {

    private final long id;
    private final long serverId;
    private final Set<PermissionGroup> groups;
    private final Set<Permission> permissions;

    private YandexSpeechkitAPI.Voice voice;
    private YandexSpeechkitAPI.Voice.Emotion emotion;

    public PermissionGroup getMaxGroup() {
        return Collections.max(groups.stream()
                .sorted(Comparator.<PermissionGroup>comparingInt(Enum::ordinal).reversed())
                .collect(Collectors.toList()));
    }

    public boolean hasPermission(Permission permission) {
        boolean has = false;
        for (PermissionGroup group : getGroups()) {
            has |= group.getPermissions().contains(permission);
        }
        return getPermissions().contains(permission) || has;
    }
}
