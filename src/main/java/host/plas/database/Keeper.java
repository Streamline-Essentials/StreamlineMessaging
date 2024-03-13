package host.plas.database;

import host.plas.StreamlineMessaging;
import host.plas.configs.ConfiguredChatChannel;
import host.plas.savables.SavableChatter;
import net.streamline.api.SLAPI;
import net.streamline.api.database.DatabaseType;
import net.streamline.api.database.modules.DBKeeper;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicReference;

public class Keeper extends DBKeeper<SavableChatter> {
    public Keeper() {
        super("utilities_users", SavableChatter::new);
    }

    @Override
    public void ensureMysqlTables() {
        String statement = "CREATE TABLE IF NOT EXISTS `%table_prefix%chatter_main` (" +
                "`Uuid` VARCHAR(36) NOT NULL PRIMARY KEY, " +
                "`CurrentChannel` TEXT NOT NULL, " +
                "`ReplyToUuid` VARCHAR(36) NOT NULL, " +
                "`LastMessage` TEXT NOT NULL, " +
                "`LastMessageSent` TEXT NOT NULL, " +
                "`LastMessageReceived` TEXT NOT NULL, " +
                "`AcceptingFriendRequests` BIT NOT NULL " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%channel_views` (" +
                "`Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`Uuid` VARCHAR(36) NOT NULL, " +
                "`Channel` TEXT NOT NULL, " +
                "`Viewed` BIT NOT NULL " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%chatter_friends` (" +
                "`Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`PlayerUuid` VARCHAR(36) NOT NULL, " +
                "`FriendUuid` VARCHAR(36) NOT NULL, " +
                "`DateAccepted` INT NOT NULL " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%chatter_ignores` (" +
                "`Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`PlayerUuid` VARCHAR(36) NOT NULL, " +
                "`IgnoreUuid` VARCHAR(36) NOT NULL, " +
                "`DateIgnored` INT NOT NULL " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%friend_invites` (" +
                "`Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`PlayerUuid` VARCHAR(36) NOT NULL, " +
                "`FriendUuid` VARCHAR(36) NOT NULL, " +
                "`TicksLeft` INT NOT NULL " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%best_friends` (" +
                "`Id` INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                "`PlayerUuid` VARCHAR(36) NOT NULL, " +
                "`FriendUuid` VARCHAR(36) NOT NULL, " +
                "`DateSet` INT NOT NULL " +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        getDatabase().execute(statement);
    }

    @Override
    public void ensureSqliteTables() {
        String statement = "CREATE TABLE IF NOT EXISTS `%table_prefix%chatter_main` (" +
                "`Uuid` TEXT NOT NULL PRIMARY KEY, " +
                "`CurrentChannel` TEXT NOT NULL, " +
                "`ReplyToUuid` TEXT NOT NULL, " +
                "`LastMessage` TEXT NOT NULL, " +
                "`LastMessageSent` TEXT NOT NULL, " +
                "`LastMessageReceived` TEXT NOT NULL, " +
                "`AcceptingFriendRequests` BOOLEAN NOT NULL " +
                ");" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%channel_views` (" +
                "`Id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`Uuid` TEXT NOT NULL, " +
                "`Channel` TEXT NOT NULL, " +
                "`Viewed` BOOLEAN NOT NULL " +
                ");" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%chatter_friends` (" +
                "`Id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`PlayerUuid` TEXT NOT NULL, " +
                "`FriendUuid` TEXT NOT NULL, " +
                "`DateAccepted` INTEGER NOT NULL " +
                ");" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%chatter_ignores` (" +
                "`Id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`PlayerUuid` TEXT NOT NULL, " +
                "`IgnoreUuid` TEXT NOT NULL, " +
                "`DateIgnored` INTEGER NOT NULL " +
                ");" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%friend_invites` (" +
                "`Id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`PlayerUuid` TEXT NOT NULL, " +
                "`FriendUuid` TEXT NOT NULL, " +
                "`TicksLeft` INTEGER NOT NULL " +
                ");" +
                "CREATE TABLE IF NOT EXISTS `%table_prefix%best_friends` (" +
                "`Id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
                "`PlayerUuid` TEXT NOT NULL, " +
                "`FriendUuid` TEXT NOT NULL, " +
                "`DateSet` INTEGER NOT NULL " +
                ");";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        getDatabase().execute(statement);
    }

    @Override
    public void saveMysql(SavableChatter obj) {
        String statement = "INSERT INTO `%table_prefix%chatter_main` " +
                "(`Uuid`, `CurrentChannel`, `ReplyToUuid`, `LastMessage`, `LastMessageSent`, `LastMessageReceived`, `AcceptingFriendRequests`) " +
                "VALUES " +
                "( '%uuid%', '%currentChannel%', '%replyToUuid%', '%lastMessage%', '%lastMessageSent%', '%lastMessageReceived%', %acceptingFriendRequests% )" +
                "ON DUPLICATE KEY UPDATE " +
                "`CurrentChannel` = '%currentChannel%', " +
                "`ReplyToUuid` = '%replyToUuid%', " +
                "`LastMessage` = '%lastMessage%', " +
                "`LastMessageSent` = '%lastMessageSent%', " +
                "`LastMessageReceived` = '%lastMessageReceived%', " +
                "`AcceptingFriendRequests` = %acceptingFriendRequests%;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        statement = statement.replace("%uuid%", obj.getUuid());
        statement = statement.replace("%currentChannel%", obj.getCurrentChatChannel().getIdentifier());
        statement = statement.replace("%replyToUuid%", obj.getReplyTo());
        statement = statement.replace("%lastMessage%", obj.getLastMessage());
        statement = statement.replace("%lastMessageSent%", obj.getLastMessageSent());
        statement = statement.replace("%lastMessageReceived%", obj.getLastMessageReceived());
        statement = statement.replace("%acceptingFriendRequests%", obj.isAcceptingFriendRequests() ? "1" : "0");

        getDatabase().execute(statement);

        statement = "INSERT INTO `%table_prefix%channel_views` " +
                "(`Uuid`, `Channel`, `Viewed`) " +
                "VALUES " +
                "( '%uuid%', '%channel%', '%viewed%' )" +
                "ON DUPLICATE KEY UPDATE " +
                "`Channel` = '%channel%', " +
                "`Viewed` = %viewed%;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement = statement;
        obj.getViewing().forEach((channel, viewed) -> {
            String s = finalStatement;
            s = s.replace("%uuid%", obj.getUuid());
            s = s.replace("%channel%", channel.getIdentifier());
            s = s.replace("%viewed%", viewed ? "1" : "0");

            getDatabase().execute(s);
        });

        statement = "INSERT INTO `%table_prefix%chatter_friends` " +
                "(`PlayerUuid`, `FriendUuid`, `DateAccepted`) " +
                "VALUES " +
                "( '%playerUuid%', '%friendUuid%', %dateAccepted% )" +
                "ON DUPLICATE KEY UPDATE " +
                "`DateAccepted` = %dateAccepted%;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement1 = statement;
        obj.getFriends().forEach((date, friend) -> {
            String s = finalStatement1;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%friendUuid%", friend);
            s = s.replace("%dateAccepted%", String.valueOf(date.getTime()));

            getDatabase().execute(s);
        });

        statement = "INSERT INTO `%table_prefix%chatter_ignores` " +
                "(`PlayerUuid`, `IgnoreUuid`, `DateIgnored`) " +
                "VALUES " +
                "( '%playerUuid%', '%ignoreUuid%', %dateIgnored% )" +
                "ON DUPLICATE KEY UPDATE " +
                "`DateIgnored` = %dateIgnored%;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement2 = statement;
        obj.getIgnoring().forEach((date, ignore) -> {
            String s = finalStatement2;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%ignoreUuid%", ignore);
            s = s.replace("%dateIgnored%", String.valueOf(date.getTime()));

            getDatabase().execute(s);
        });

        statement = "INSERT INTO `%table_prefix%friend_invites` " +
                "(`PlayerUuid`, `FriendUuid`, `TicksLeft`) " +
                "VALUES " +
                "( '%playerUuid%', '%friendUuid%', %ticksLeft% )" +
                "ON DUPLICATE KEY UPDATE " +
                "`TicksLeft` = %ticksLeft%;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement3 = statement;
        obj.getFriendInvites().forEach((date, invite) -> {
            String s = finalStatement3;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%friendUuid%", invite.getInvited().getUuid());
            s = s.replace("%ticksLeft%", String.valueOf(invite.getTicksLeft()));

            getDatabase().execute(s);
        });

        statement = "INSERT INTO `%table_prefix%best_friends` " +
                "(`PlayerUuid`, `FriendUuid`, `DateSet`) " +
                "VALUES " +
                "( '%playerUuid%', '%friendUuid%', %dateSet% )" +
                "ON DUPLICATE KEY UPDATE " +
                "`DateSet` = %dateSet%;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement4 = statement;
        obj.getBestFriends().forEach((date, bestFriend) -> {
            String s = finalStatement4;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%friendUuid%", bestFriend);
            s = s.replace("%dateSet%", String.valueOf(date.getTime()));

            getDatabase().execute(s);
        });
    }

    @Override
    public void saveSqlite(SavableChatter obj) {
        String statement = "INSERT OR REPLACE INTO `%table_prefix%chatter_main` " +
                "(`Uuid`, `CurrentChannel`, `ReplyToUuid`, `LastMessage`, `LastMessageSent`, `LastMessageReceived`, `AcceptingFriendRequests`) " +
                "VALUES " +
                "( '%uuid%', '%currentChannel%', '%replyToUuid%', '%lastMessage%', '%lastMessageSent%', '%lastMessageReceived%', %acceptingFriendRequests% );";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        statement = statement.replace("%uuid%", obj.getUuid());
        statement = statement.replace("%currentChannel%", obj.getCurrentChatChannel().getIdentifier());
        statement = statement.replace("%replyToUuid%", obj.getReplyTo());
        statement = statement.replace("%lastMessage%", obj.getLastMessage());
        statement = statement.replace("%lastMessageSent%", obj.getLastMessageSent());
        statement = statement.replace("%lastMessageReceived%", obj.getLastMessageReceived());
        statement = statement.replace("%acceptingFriendRequests%", obj.isAcceptingFriendRequests() ? "1" : "0");

        getDatabase().execute(statement);

        statement = "INSERT OR REPLACE INTO `%table_prefix%channel_views` " +
                "(`Uuid`, `Channel`, `Viewed`) " +
                "VALUES " +
                "( '%uuid%', '%channel%', '%viewed%' );";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement = statement;
        obj.getViewing().forEach((channel, viewed) -> {
            String s = finalStatement;
            s = s.replace("%uuid%", obj.getUuid());
            s = s.replace("%channel%", channel.getIdentifier());
            s = s.replace("%viewed%", viewed ? "1" : "0");

            getDatabase().execute(s);
        });

        statement = "INSERT OR REPLACE INTO `%table_prefix%chatter_friends` " +
                "(`PlayerUuid`, `FriendUuid`, `DateAccepted`) " +
                "VALUES " +
                "( '%playerUuid%', '%friendUuid%', %dateAccepted% );";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement1 = statement;
        obj.getFriends().forEach((date, friend) -> {
            String s = finalStatement1;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%friendUuid%", friend);
            s = s.replace("%dateAccepted%", String.valueOf(date.getTime()));

            getDatabase().execute(s);
        });

        statement = "INSERT OR REPLACE INTO `%table_prefix%chatter_ignores` " +
                "(`PlayerUuid`, `IgnoreUuid`, `DateIgnored`) " +
                "VALUES " +
                "( '%playerUuid%', '%ignoreUuid%', %dateIgnored% );";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement2 = statement;
        obj.getIgnoring().forEach((date, ignore) -> {
            String s = finalStatement2;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%ignoreUuid%", ignore);
            s = s.replace("%dateIgnored%", String.valueOf(date.getTime()));

            getDatabase().execute(s);
        });

        statement = "INSERT OR REPLACE INTO `%table_prefix%friend_invites` " +
                "(`PlayerUuid`, `FriendUuid`, `TicksLeft`) " +
                "VALUES " +
                "( '%playerUuid%', '%friendUuid%', %ticksLeft% );";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement3 = statement;
        obj.getFriendInvites().forEach((date, invite) -> {
            String s = finalStatement3;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%friendUuid%", invite.getInvited().getUuid());
            s = s.replace("%ticksLeft%", String.valueOf(invite.getTicksLeft()));

            getDatabase().execute(s);
        });

        statement = "INSERT OR REPLACE INTO `%table_prefix%best_friends` " +
                "(`PlayerUuid`, `FriendUuid`, `DateSet`) " +
                "VALUES " +
                "( '%playerUuid%', '%friendUuid%', %dateSet% );";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        String finalStatement4 = statement;
        obj.getBestFriends().forEach((date, bestFriend) -> {
            String s = finalStatement4;
            s = s.replace("%playerUuid%", obj.getUuid());
            s = s.replace("%friendUuid%", bestFriend);
            s = s.replace("%dateSet%", String.valueOf(date.getTime()));

            getDatabase().execute(s);
        });
    }

    @Override
    public Optional<SavableChatter> loadMysql(String identifier) {
        String statement = "SELECT * FROM `%table_prefix%chatter_main` WHERE `uuid` = '%uuid%';";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
        statement = statement.replace("%uuid%", identifier);

        AtomicReference<Optional<SavableChatter>> user = new AtomicReference<>(Optional.empty());
        getDatabase().executeQuery(statement, (result) -> {
            try {
                if (result.next()) {
                    String uuid = result.getString("Uuid");
                    String currentChannel = result.getString("CurrentChannel");
                    String replyToUuid = result.getString("ReplyToUuid");
                    String lastMessage = result.getString("LastMessage");
                    String lastMessageSent = result.getString("LastMessageSent");
                    String lastMessageReceived = result.getString("LastMessageReceived");
                    boolean acceptingFriendRequests = result.getBoolean("AcceptingFriendRequests");

                    SavableChatter u = new SavableChatter(uuid);
                    ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannel(currentChannel);
                    if (channel != null) u.setCurrentChatChannel(channel);

                    u.setReplyTo(replyToUuid);
                    u.setLastMessage(lastMessage);
                    u.setLastMessageSent(lastMessageSent);
                    u.setLastMessageReceived(lastMessageReceived);
                    u.setAcceptingFriendRequests(acceptingFriendRequests);

                    user.set(Optional.of(u));
                }

                result.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        user.get().ifPresent((u) -> {
            String statement1 = "SELECT * FROM `%table_prefix%channel_views` WHERE `uuid` = '%uuid%';";

            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
            statement1 = statement1.replace("%uuid%", identifier);

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String channel = result.getString("Channel");
                        boolean viewed = result.getBoolean("Viewed");

                        u.setViewed(channel, viewed);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%chatter_friends` WHERE `PlayerUuid` = '%playerUuid%';";

            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
            statement1 = statement1.replace("%playerUuid%", identifier);

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String friendUuid = result.getString("FriendUuid");
                        long dateAccepted = result.getLong("DateAccepted");

                        u.setFriended(dateAccepted, friendUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%chatter_ignores` WHERE `PlayerUuid` = '%playerUuid%';";

            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
            statement1 = statement1.replace("%playerUuid%", identifier);

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String ignoreUuid = result.getString("IgnoreUuid");
                        long dateIgnored = result.getLong("DateIgnored");

                        u.setIgnored(dateIgnored, ignoreUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%friend_invites` WHERE `PlayerUuid` = '%playerUuid%';";

            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
            statement1 = statement1.replace("%playerUuid%", identifier);

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String friendUuid = result.getString("FriendUuid");
                        long ticksLeft = result.getLong("TicksLeft");

                        u.setInviteSent(ticksLeft, friendUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%best_friends` WHERE `PlayerUuid` = '%player_uuid%';";

            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
            statement1 = statement1.replace("%player_uuid%", identifier);

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String friendUuid = result.getString("FriendUuid");
                        long dateSet = result.getLong("DateSet");

                        u.setBestFriended(dateSet, friendUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            user.set(Optional.of(u));
        });

        return user.get();
    }

    @Override
    public Optional<SavableChatter> loadSqlite(String identifier) {
        String statement = "SELECT * FROM `%table_prefix%chatter_main` WHERE `uuid` = '%uuid%';";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
        statement = statement.replace("%uuid%", identifier);

        AtomicReference<Optional<SavableChatter>> user = new AtomicReference<>(Optional.empty());
        getDatabase().executeQuery(statement, (result) -> {
            try {
                if (result.next()) {
                    String uuid = result.getString("Uuid");
                    String currentChannel = result.getString("CurrentChannel");
                    String replyToUuid = result.getString("ReplyToUuid");
                    String lastMessage = result.getString("LastMessage");
                    String lastMessageSent = result.getString("LastMessageSent");
                    String lastMessageReceived = result.getString("LastMessageReceived");
                    boolean acceptingFriendRequests = result.getBoolean("AcceptingFriendRequests");

                    SavableChatter u = new SavableChatter(uuid);
                    ConfiguredChatChannel channel = StreamlineMessaging.getChatChannelConfig().getChatChannel(currentChannel);
                    if (channel != null) u.setCurrentChatChannel(channel);

                    u.setReplyTo(replyToUuid);
                    u.setLastMessage(lastMessage);
                    u.setLastMessageSent(lastMessageSent);
                    u.setLastMessageReceived(lastMessageReceived);
                    u.setAcceptingFriendRequests(acceptingFriendRequests);

                    user.set(Optional.of(u));
                }

                result.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        user.get().ifPresent((u) -> {
            String statement1 = "SELECT * FROM `%table_prefix%channel_views` WHERE `uuid` = '%uuid%';";

            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
            statement1 = statement1.replace("%uuid%", identifier);

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String channel = result.getString("Channel");
                        boolean viewed = result.getBoolean("Viewed");

                        u.setViewed(channel, viewed);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%chatter_friends` WHERE `PlayerUuid` = '%playerUuid%';";

            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
            statement1 = statement1.replace("%playerUuid%", identifier);

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String friendUuid = result.getString("FriendUuid");
                        long dateAccepted = result.getLong("DateAccepted");

                        u.setFriended(dateAccepted, friendUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%chatter_ignores` WHERE `PlayerUuid` = '%playerUuid%';";
            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String ignoreUuid = result.getString("IgnoreUuid");
                        long dateIgnored = result.getLong("DateIgnored");

                        u.setIgnored(dateIgnored, ignoreUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%friend_invites` WHERE `PlayerUuid` = '%playerUuid%';";
            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String friendUuid = result.getString("FriendUuid");
                        long ticksLeft = result.getLong("TicksLeft");

                        u.setInviteSent(ticksLeft, friendUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            statement1 = "SELECT * FROM `%table_prefix%best_friends` WHERE `PlayerUuid` = '%player_uuid%';";
            statement1 = statement1.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

            getDatabase().executeQuery(statement1, (result) -> {
                try {
                    while (result.next()) {
                        String friendUuid = result.getString("FriendUuid");
                        long dateSet = result.getLong("DateSet");

                        u.setBestFriended(dateSet, friendUuid);
                    }

                    result.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            user.set(Optional.of(u));
        });

        return user.get();
    }

    @Override
    public boolean existsMysql(String identifier) {
        String statement = "SELECT * FROM `%table_prefix%chatter_main` WHERE `uuid` = '%uuid%';";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
        statement = statement.replace("%uuid%", identifier);

        AtomicReference<Boolean> exists = new AtomicReference<>(false);
        getDatabase().executeQuery(statement, (result) -> {
            try {
                exists.set(result.next());
                result.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return exists.get();
    }

    @Override
    public boolean existsSqlite(String identifier) {
        String statement = "SELECT * FROM `%table_prefix%chatter_main` WHERE `uuid` = '%uuid%';";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());
        statement = statement.replace("%uuid%", identifier);

        AtomicReference<Boolean> exists = new AtomicReference<>(false);
        getDatabase().executeQuery(statement, (result) -> {
            try {
                exists.set(result.next());
                result.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return exists.get();
    }

    public CompletableFuture<ConcurrentSkipListSet<SavableChatter>> pullAllChatters() {
        if (SLAPI.getMainDatabase().getConnectorSet().getType() == DatabaseType.MYSQL) {
            return pullAllChattersMySql();
        } else {
            return pullAllChattersSqlite();
        }
    }

    public CompletableFuture<ConcurrentSkipListSet<SavableChatter>> pullAllChattersMySql() {
        String statement = "SELECT `Uuid` FROM `%table_prefix%chatter_main`;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        ConcurrentSkipListSet<String> uuids = new ConcurrentSkipListSet<>();
        getDatabase().executeQuery(statement, (result) -> {
            try {
                while (result.next()) {
                    String uuid = result.getString("Uuid");

                    uuids.add(uuid);
                }

                result.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        AtomicReference<ConcurrentSkipListSet<SavableChatter>> chatters = new AtomicReference<>(new ConcurrentSkipListSet<>());
        uuids.forEach((uuid) -> {
            load(uuid).join().ifPresent(chatters.get()::add);
        });

        return CompletableFuture.completedFuture(chatters.get());
    }

    public CompletableFuture<ConcurrentSkipListSet<SavableChatter>> pullAllChattersSqlite() {
        String statement = "SELECT `Uuid` FROM `%table_prefix%chatter_main`;";

        statement = statement.replace("%table_prefix%", SLAPI.getMainDatabase().getConnectorSet().getTablePrefix());

        ConcurrentSkipListSet<String> uuids = new ConcurrentSkipListSet<>();
        getDatabase().executeQuery(statement, (result) -> {
            try {
                while (result.next()) {
                    String uuid = result.getString("Uuid");

                    uuids.add(uuid);
                }

                result.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        AtomicReference<ConcurrentSkipListSet<SavableChatter>> chatters = new AtomicReference<>(new ConcurrentSkipListSet<>());
        uuids.forEach((uuid) -> {
            load(uuid).join().ifPresent(chatters.get()::add);
        });

        return CompletableFuture.completedFuture(chatters.get());
    }
}