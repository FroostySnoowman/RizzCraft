package rizzcraft.net.rizzcraft.SQL;

import rizzcraft.net.rizzcraft.Main;
import com.mysql.cj.jdbc.MysqlConnectionPoolDataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import javax.sql.PooledConnection;
import org.bukkit.entity.Player;

public class SQL {
    public PooledConnection connectionPool;
    public MysqlConnectionPoolDataSource poolSource;
    private final Main main;

    public SQL(Main main) {
        this.main = main;
    }

    public void Connect() {
        try {
            this.poolSource = new MysqlConnectionPoolDataSource();
            this.poolSource
                    .setURL(
                            "jdbc:mysql://"
                                    + this.main.config.getConfig().getString("Database_MySQL.Hostname")
                                    + ":"
                                    + this.main.config.getConfig().getString("Database_MySQL.Port")
                                    + "/"
                                    + this.main.config.getConfig().getString("Database_MySQL.Database")
                                    + "?autoReconnect=true"
                    );
            this.poolSource.setUser(this.main.config.getConfig().getString("Database_MySQL.Username"));
            this.poolSource.setPassword(this.main.config.getConfig().getString("Database_MySQL.Password"));
            this.poolSource.setAllowMultiQueries(true);
            this.connectionPool = this.poolSource.getPooledConnection();
            this.CreateSchema();
        } catch (SQLException var2) {
            throw new ArithmeticException("\u001B[31mFailed to connect to DB: " + var2.getMessage() + "\u001B[31m");
        }
    }

    public void Disconnect() {
        try {
            this.connectionPool.close();
            this.main.logger.log(Level.INFO, "DB Disconnected");
        } catch (SQLException var2) {
            throw new ArithmeticException("\u001B[31mFailed to disconnect from DB: " + var2.getMessage() + "\u001B[0m");
        }
    }

    public boolean PlayerExistsInDB(Player player) {
        try {
            boolean var6;
            try (Connection con = this.connectionPool.getConnection()) {
                PreparedStatement pstmt = con.prepareStatement("SELECT UUID FROM players WHERE UUID=?");
                pstmt.setString(1, player.getUniqueId().toString());
                ResultSet rs = pstmt.executeQuery();
                boolean result = rs.next();
                pstmt.close();
                rs.close();
                con.close();
                var6 = result;
            }

            return var6;
        } catch (SQLException var9) {
            throw new ArithmeticException("\u001B[31mFailed to check for player " + player.getUniqueId() + " in DB: " + var9.getMessage() + "\u001B[0m");
        }
    }

    public void CreatePlayerDBEntry(Player player) {
        try {
            try (Connection con = this.connectionPool.getConnection()) {
                PreparedStatement pstmt = this.connectionPool.getConnection().prepareStatement("INSERT INTO players(UUID,DisplayName) VALUES(?,?)");
                pstmt.setString(1, player.getUniqueId().toString());
                pstmt.setString(2, player.getName().toString());
                pstmt.executeUpdate();
                pstmt.close();
                con.close();
                this.main.logger.log(Level.INFO, "\u001B[32mNew player record created in DB for " + player.getUniqueId() + "\u001B[0m");
            }
        } catch (SQLException var7) {
            throw new ArithmeticException("\u001B[31mFailed to create new player entry in DB: " + var7.getMessage() + "\u001B[0m");
        }
    }

    public void CreateSchema() {
        String sql = "\tCREATE TABLE IF NOT EXISTS `players` (\n\t`UUID` varchar(255) NOT NULL,\n\t`DisplayName` varchar(255) NOT NULL,\n\t`Welcomed` int NOT NULL DEFAULT '0',\n\t`ShopFireworks` int NOT NULL DEFAULT '1',\n\t`LastJoined` timestamp NULL DEFAULT NULL,\n\t`LastQuit` timestamp NULL DEFAULT NULL,\n\t`SkinBase64` mediumtext,\n\t`StatsJSON` json DEFAULT NULL,\n\t`AdvancementsJSON` json DEFAULT NULL,\n\t`Created` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n\tPRIMARY KEY (`UUID`),\n\tUNIQUE KEY `UUID` (`UUID`)\n  \t);\n\n\tCREATE TABLE IF NOT EXISTS `players_deaths` (\n\t  `ID` int NOT NULL AUTO_INCREMENT,\n\t  `PlayerUUID` varchar(255) NOT NULL,\n\t  `Inventory` mediumtext,\n\t  `Armor` mediumtext,\n\t  `Location` mediumtext,\n\t  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n\t  PRIMARY KEY (`ID`),\n\t  KEY `FK_players_deaths_players` (`PlayerUUID`),\n\t  CONSTRAINT `FK_players_deaths_players` FOREIGN KEY (`PlayerUUID`) REFERENCES `players` (`UUID`)\n\t);\n\n\tCREATE TABLE IF NOT EXISTS `oreAlert` (\n\t`ID` int NOT NULL AUTO_INCREMENT,\n\t`PlayerUUID` varchar(255) NOT NULL DEFAULT '',\n\t`BlockType` mediumtext NOT NULL,\n\t`Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n\tPRIMARY KEY (`ID`),\n\tKEY `FK_oreAlert_players` (`PlayerUUID`),\n\tCONSTRAINT `FK_oreAlert_players` FOREIGN KEY (`PlayerUUID`) REFERENCES `players` (`UUID`)\n\t);\n\n\tCREATE TABLE IF NOT EXISTS `shops` (\n\t`ID` int NOT NULL AUTO_INCREMENT,\n\t`OwnerUUID` varchar(255) NOT NULL,\n\t`shopName` tinytext,\n\t`SellsItems` mediumtext,\n\t`Stock` mediumtext,\n\t`Payment` mediumtext NOT NULL,\n\t`PaymentCount` int NOT NULL DEFAULT '0',\n\t`Location` mediumtext NOT NULL,\n\t`Locked` int NOT NULL DEFAULT '0',\n\t`Quantity` int NOT NULL DEFAULT '1',\n\tPRIMARY KEY (`ID`),\n\tUNIQUE KEY `ID` (`ID`),\n\tKEY `FK_shops_players` (`OwnerUUID`),\n\tCONSTRAINT `FK_shops_players` FOREIGN KEY (`OwnerUUID`) REFERENCES `players` (`UUID`)\n\t);\n\n\tCREATE TABLE IF NOT EXISTS `shops_transactions` (\n\t  `ID` int NOT NULL AUTO_INCREMENT,\n\t  `ShopID` int NOT NULL,\n\t  `BuyerUUID` varchar(255) NOT NULL,\n\t  `Item` mediumtext NOT NULL,\n\t  `Payment` mediumtext NOT NULL,\n\t  `Timestamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n\t  PRIMARY KEY (`ID`),\n\t  KEY `FK_shops_transactions_players` (`BuyerUUID`),\n\t  KEY `FK_shops_transactions_shops` (`ShopID`),\n\t  CONSTRAINT `FK_shops_transactions_players` FOREIGN KEY (`BuyerUUID`) REFERENCES `players` (`UUID`),\n\t  CONSTRAINT `FK_shops_transactions_shops` FOREIGN KEY (`ShopID`) REFERENCES `shops` (`ID`)\n\t);\n\n\tCREATE TABLE IF NOT EXISTS `uptime_timeseries` (\n\t  `TimeStamp` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,\n\t  `ConnectedPlayers` int NOT NULL DEFAULT '0',\n\t  `TPS` double NOT NULL DEFAULT '0',\n\t  PRIMARY KEY (`TimeStamp`)\n\t);\n\n";

        try {
            try (Connection con = this.connectionPool.getConnection()) {
                Statement stmt = this.connectionPool.getConnection().createStatement();
                stmt.executeUpdate(sql);
                stmt.close();
                con.close();
                this.main.logger.log(Level.INFO, "\u001B[32mDB Schema Check Passed\u001B[0m");
            }
        } catch (SQLException var7) {
            var7.printStackTrace();
            throw new ArithmeticException("\u001B[31mDB Schema creation failed: " + var7.getMessage() + "\u001B[0m");
        }
    }
}