package ru.yuraender.discord.entity;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.yuraender.discord.entity.enums.PermissionGroup;
import ru.yuraender.discord.music.speechkit.YandexSpeechkitAPI;

import java.util.List;

@Getter
@AllArgsConstructor
public class BotGuild {

    private final long id;
    @Setter
    private long channelId;
    @Setter
    private long privateChannelId;
    @Setter
    private long muteRoleId;

    private final List<BotUser> users;

    public BotUser getUser(long id) {
        return users.stream()
                .filter(user -> user.getId() == id)
                .findAny()
                .orElse(new BotUser(
                        id, this.id,
                        Sets.newHashSet(PermissionGroup.UNTRUSTED), Sets.newHashSet(),
                        YandexSpeechkitAPI.Voice.JANE, YandexSpeechkitAPI.Voice.Emotion.GOOD
                ));
    }
}
