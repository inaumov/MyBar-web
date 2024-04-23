package mybar.repository;

import com.github.springtestdbunit.DbUnitTestExecutionListener;
import com.github.springtestdbunit.annotation.DbUnitConfiguration;
import com.github.springtestdbunit.bean.DatabaseConfigBean;
import com.github.springtestdbunit.bean.DatabaseDataSourceConnectionFactoryBean;
import mybar.context.DbTestContext;
import org.dbunit.database.DefaultMetadataHandler;
import org.dbunit.dataset.Column;
import org.dbunit.dataset.filter.IColumnFilter;
import org.dbunit.ext.postgresql.PostgresqlDataTypeFactory;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.transaction.TransactionalTestExecutionListener;

import javax.sql.DataSource;

@ContextConfiguration
@TestExecutionListeners(
        listeners = {
                DependencyInjectionTestExecutionListener.class,
                DbUnitTestExecutionListener.class,
                TransactionalTestExecutionListener.class
        },
        inheritListeners = false)
@DbUnitConfiguration(databaseConnection = "dbUnitDatabaseConnection")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public abstract class DbUnitBaseTest extends DbTestContext {

    public static class EntityIdExclusionFilter implements IColumnFilter {

        @Override
        public boolean accept(String tableName, Column column) {
            return !column.getColumnName().endsWith("ID");
        }
    }

    @TestConfiguration
    static class DbUnitConfig {

        @Bean
        public DatabaseDataSourceConnectionFactoryBean dbUnitDatabaseConnection(Environment environment, DataSource dataSource) {
            var databaseDataSourceConnectionFactoryBean = new DatabaseDataSourceConnectionFactoryBean();
            databaseDataSourceConnectionFactoryBean.setDatabaseConfig(dbUnitDatabaseConfig());
            databaseDataSourceConnectionFactoryBean.setUsername(environment.getProperty("tc.postgres.username"));
            databaseDataSourceConnectionFactoryBean.setPassword(environment.getProperty("tc.postgres.password"));
            databaseDataSourceConnectionFactoryBean.setSchema(environment.getProperty("tc.postgres.database-name"));
            databaseDataSourceConnectionFactoryBean.setDataSource(dataSource);
            return databaseDataSourceConnectionFactoryBean;
        }

        private DatabaseConfigBean dbUnitDatabaseConfig() {
            DatabaseConfigBean configBean = new DatabaseConfigBean();
            configBean.setAllowEmptyFields(true);
            configBean.setDatatypeFactory(new PostgresqlDataTypeFactory());
            configBean.setCaseSensitiveTableNames(false);
            configBean.setMetadataHandler(new DefaultMetadataHandler());
            return configBean;
        }

    }

}