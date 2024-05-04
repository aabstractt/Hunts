package it.bitrule.hunts;

import cn.nukkit.utils.Config;
import cn.nukkit.utils.TextFormat;
import lombok.NonNull;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public enum TranslationKey {

    // Translation keys for player
    PLAYER_NOT_ONLINE("player.not-online", "player"),

    PLAYER_SELF_ALREADY_IN_FACTION("player.self-already-in-faction"),
    PLAYER_ALREADY_IN_FACTION("player.already-in-faction", "player"),
    PLAYER_SELF_MUST_BE_IN_FACTION("player.self-must-be-in-faction"),
    PLAYER_NOT_FACTION_MEMBER("player.not-faction-member"),
    PLAYER_CANNOT_KICK_SELF("player.cannot-kick-self"),
    PLAYER_SELF_KICKED("player.self-kicked", "who"),
    PLAYER_CANNOT_INVITE_SELF("player.cannot-invite-self"),
    PLAYER_SELF_DEMOTED("player.self-demoted", "who", "role"),
    PLAYER_SELF_PROMOTED("player.self-promoted", "who", "role"),
    PLAYER_NOT_ENOUGH_BALANCE("player.not-enough-balance", "balance", "amount"),
    PLAYER_SELF_DEPOSITED_MONEY("player.self-deposited-money", "amount"),

    // Translation keys for Factions
    FACTION_ALREADY_EXISTS("faction.already-exists", "faction"),
    FACTION_SUCCESSFULLY_CREATED("faction.successfully-created", "faction"),
    FACTION_SUCCESSFULLY_DISBANDED("faction.successfully-disbanded", "who"),
    FACTION_SUCCESSFULLY_KICKED_SOMEONE("faction.successfully-kicked-someone", "who", "player"),
    FACTION_MEMBER_ALREADY_INVITED("faction.member-already-invited", "player"),
    FACTION_INVITE_RECEIVED("faction.invite-received", "faction", "who"),
    FACTION_INVITE_SENT("faction.invite-sent", "player"),
    FACTION_INVITE_BROADCAST("faction.invite-broadcast", "player", "who"),
    FACTION_FULL("faction.full"),
    FACTION_MEMBER_DEMOTED("faction.member-demoted", "player", "who", "role"),
    FACTION_MEMBER_PROMOTED("faction.member-promoted", "player", "who", "role"),
    FACTION_HQ_UPDATED("faction.hq-updated", "player"),
    FACTION_MEMBER_DEPOSITED_MONEY("faction.member-deposited-money", "player", "amount"),
    FACTION_NO_VALID_NAME("faction.no-valid-name");

    private final static @NonNull Map<String, String> translations = new HashMap<>();

    /**
     * The message key
     */
    private final @NonNull String messageKey;
    /**
     * The arguments to replace
     */
    private final @NonNull String[] arguments;

    TranslationKey(@NonNull String messageKey, @NonNull String... arguments) {
        this.messageKey = messageKey;
        this.arguments = arguments;
    }

    /**
     * Build the message with the arguments
     *
     * @param args the arguments
     * @return the built message
     */
    public @NonNull String build(@NonNull Object... args) {
        if (args.length != this.arguments.length) {
            throw new IllegalArgumentException("Invalid number of arguments");
        }

        String message = wrapMessage(this.messageKey);
        for (int i = 0; i < this.arguments.length; i++) {
            try {
                message = message.replaceAll("<" + this.arguments[i] + ">", args[i].toString());
            } catch (Exception e) {
                e.printStackTrace(System.out);

                break;
            }
        }

        return message;
    }

    /**
     * Wrap the message with the key
     * @param messageKey The message key
     * @return The wrapped message
     */
    public static @NonNull String wrapMessage(@NonNull String messageKey) {
        if (translations.isEmpty()) {
            return TextFormat.colorize("&f<Missing key '&a" + messageKey + "&f'>");
        }

        String message = translations.get(messageKey);
        if (message.isBlank() || message.isEmpty()) return TextFormat.colorize("&f<Missing key '&a" + messageKey + "&f'>");

//        String[] subStrings = JavaUtils.substringsBetween(message, "<!", ">");
        String[] subStrings = null;
        if (subStrings == null) return TextFormat.colorize(message);

        for (String substring : subStrings) {
            message = message.replaceAll("<!" + substring + ">", wrapMessage(substring));
        }

        return TextFormat.colorize(message);
    }

    static void adjustIntern() {
        Config messagesConfig = new Config(new File(Hunts.getInstance().getDataFolder(), "messages.yml"));

        for (TranslationKey translationKey : values()) {
            String message = messagesConfig.getString(translationKey.messageKey);
            if (message == null || message.trim().isEmpty()) continue;

            translations.put(translationKey.messageKey, TextFormat.colorize(message.trim()));
        }
    }
}