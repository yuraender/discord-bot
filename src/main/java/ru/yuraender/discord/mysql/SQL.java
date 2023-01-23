package ru.yuraender.discord.mysql;

import lombok.Getter;
import org.jooq.DSLContext;

import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;

public class SQL {

    @Getter
    private static final DSLContext context = MySQL.getContext();

    public static void sync(String sql, Object... bindings) {
        context.execute(sql, bindings);
    }

    public static void async(String query, Object... bindings) {
        async(create -> create.execute(query, bindings));
    }

    public static Future<?> async(Consumer<DSLContext> create) {
        return CompletableFuture.runAsync(() -> {
            try {
                create.accept(context);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, MySQL.getService());
    }

    public static <R> Future<?> async(SQLFunction<R> create, Consumer<R> result) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                return create.apply(context);
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return null;
        }, MySQL.getService()).thenAccept(result);
    }

    public interface SQLFunction<R> {

        R apply(DSLContext create) throws SQLException;
    }
}
