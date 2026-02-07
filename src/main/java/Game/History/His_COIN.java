package Game.History;

import Game.core.SQL;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

public class His_COIN {
    public String nameuser;
    public String namePlayer;
    public int coin_change;
    public int coin_last;
    public String Logger;
    public His_COIN(String name_user,String name){
        this.nameuser = name_user;
        this.namePlayer = name;
    }
    public void Flus(){
        String query
                = "INSERT INTO `history_coin` (`user_name`, `name_player`, `coin_change`,`coin_last`, `logger`) VALUES ('"
                + this.nameuser + "', '" + this.namePlayer + "', '" + coin_change + "', '" + coin_last + "', '"  + this.Logger +  "')";
        try (Connection connection = SQL.gI().getConnection(); Statement statement = connection.createStatement();) {
            if (statement.executeUpdate(query) > 0) {
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
