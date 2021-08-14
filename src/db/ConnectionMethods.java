/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;
import java.sql.*;
import javax.swing.JOptionPane;
/**
 *
 * @author Danie
 */
public class ConnectionMethods {
    Connection conn;
    String baseUrl;
    
    public ConnectionMethods(){
    conn = null;
    baseUrl = "jdbc:mysql://127.0.0.1:3306/";
    
    }

public Connection initConnection(String dbname, String username, String password){
    
    String connectionString = this.baseUrl + dbname;

        try {
        conn = DriverManager.getConnection(connectionString,username,password);

    } catch (SQLException e) {
        popUpErrorMsg(e.getLocalizedMessage()); 
    }
        return conn;
}    
    

public void popUpErrorMsg(String msg){
    JOptionPane.showMessageDialog(null,"Error: " +msg, "Exception", JOptionPane.ERROR_MESSAGE);
 }
  public void popUpSuccessMsg(String msg){      
          JOptionPane.showMessageDialog(null,msg ,"Succes", JOptionPane.INFORMATION_MESSAGE);
  }  
  
}
