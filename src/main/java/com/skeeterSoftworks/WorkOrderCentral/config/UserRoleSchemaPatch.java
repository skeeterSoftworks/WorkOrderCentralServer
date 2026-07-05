package com.skeeterSoftworks.WorkOrderCentral.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Copies legacy single {@code role} column values into {@code application_user_roles}
 * when the collection table exists but has no rows for a user.
 */
@Slf4j
@Component
public class UserRoleSchemaPatch implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public UserRoleSchemaPatch(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            if (!rolesTableExists()) {
                return;
            }
            int migrated = jdbcTemplate.update("""
                    INSERT INTO application_user_roles (user_id, role)
                    SELECT u.id, u.role
                    FROM application_user u
                    WHERE u.role IS NOT NULL
                      AND NOT EXISTS (
                          SELECT 1 FROM application_user_roles r WHERE r.user_id = u.id
                      )
                    """);
            if (migrated > 0) {
                log.info("Migrated {} application user role(s) into application_user_roles", migrated);
            }
        } catch (Exception e) {
            log.warn("Could not migrate application user roles: {}", e.getMessage());
        }
    }

    private boolean rolesTableExists() {
        try {
            jdbcTemplate.queryForObject(
                    "SELECT COUNT(*) FROM information_schema.tables WHERE table_name = 'application_user_roles'",
                    Integer.class);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
