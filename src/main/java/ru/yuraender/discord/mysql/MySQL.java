package ru.yuraender.discord.mysql;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import lombok.AccessLevel;
import lombok.Getter;
import org.jooq.DSLContext;
import org.jooq.SQLDialect;
import org.jooq.impl.DSL;
import ru.yuraender.discord.Main;
import ru.yuraender.discord.util.configuration.file.FileConfiguration;

import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MySQL {

    private final String host;
    private final String username;
    private final String password;
    private final String database;

    private static HikariDataSource dataSource;
    @Getter(AccessLevel.PROTECTED)
    private static DSLContext context;
    @Getter(AccessLevel.PROTECTED)
    private static final ExecutorService service = Executors.newSingleThreadExecutor();

    public MySQL() {
        FileConfiguration config = Main.getInstance().getMysqlConfig().getConfig();
        host = config.getString("mysql.host") + ":" + config.getInt("mysql.port");
        username = config.getString("mysql.username");
        password = config.getString("mysql.password");
        database = config.getString("mysql.database");
        connectMySQL();
        shutdownHook();
    }

    private void connectMySQL() {
        HikariConfig config = new HikariConfig();

        config.setJdbcUrl("jdbc:mysql://" + host + "/" + database);
        config.setUsername(username);
        config.setPassword(password);
        config.setDriverClassName("com.mysql.jdbc.Driver");
        config.addDataSourceProperty("useSSL", false);

        //Performance
        config.addDataSourceProperty("useLocalSessionState", true);
        config.addDataSourceProperty("prepStmtCacheSize", 250);
        config.addDataSourceProperty("prepStmtCacheSqlLimit", 2048);
        config.addDataSourceProperty("maintainTimeStats", false);
        config.addDataSourceProperty("cachePrepStmts", true);
        config.addDataSourceProperty("cacheResultSetMetadata", true);
        config.addDataSourceProperty("cacheServerConfiguration", true);
        config.addDataSourceProperty("elideSetAutoCommits", true);
        config.addDataSourceProperty("useServerPrepStmts", true);
        //Encoding (utf8 or utf8mb4)
        config.addDataSourceProperty("characterEncoding", "utf8");
        config.addDataSourceProperty("characterSetResults", "utf8");
        //Miscellaneous
        config.addDataSourceProperty("serverTimezone", TimeZone.getDefault().getID());
        config.addDataSourceProperty("useJDBCCompliantTimezoneShift", true);
        config.addDataSourceProperty("useLegacyDatetimeCode", true);

        try {
            dataSource = new HikariDataSource(config);
            context = DSL.using(dataSource, SQLDialect.MYSQL);
            startCheckScheduler();
        } catch (Exception ex) {
            throw new RuntimeException("СОЕДИНЕНИЕ С БД НЕ УСТАНОВЛЕНО!", ex);
        }
    }

    private void startCheckScheduler() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            if (!service.isShutdown() && dataSource.isClosed()) {
                System.out.println("Соединение с БД пропало! Восстанавливаем...");
                connectMySQL();
                if (dataSource.isClosed()) {
                    System.out.println("Соединение восстановить не удалось.");
                } else {
                    System.out.println("Соединение восстановлено.");
                }
            }
        }, 10L, 10L, TimeUnit.SECONDS);
    }

    private void shutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("Выполняем оставшиеся запросы...");
            service.shutdown();
            try {
                service.awaitTermination(15, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            System.out.println("Отключение сессии с базой данных.");
            dataSource.close();
        }));
    }
}
