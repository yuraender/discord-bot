package ru.yuraender.discord.entity.enums;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public enum PermissionGroup {

    UNTRUSTED("Гость", Lists.newArrayList()),
    USER("Пользователь", Lists.newArrayList()),
    ADMIN("Администратор", Lists.newArrayList(Permission.ROOT));

    private final String name;
    private final List<Permission> permissions;

    public enum Action {

        ADD,
        REMOVE
    }
}
