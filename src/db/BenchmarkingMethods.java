/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package db;

import java.sql.*;
import GUI.MainWindow;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;
import java.util.Vector;
import java.util.GregorianCalendar;
import javax.swing.SwingWorker;
import javax.swing.text.DefaultCaret;

/**
 *
 * @author Danie
 */
public class BenchmarkingMethods {

    Connection conn;
    MainWindow window;
    ConnectionMethods conn_methods;

    public BenchmarkingMethods(Connection _conn, MainWindow _window) {
        this.conn = _conn;
        this.window = _window;
        conn_methods = new ConnectionMethods();
    }

    public boolean fill_DB_tables(Vector<Vector> data) {
        window.jTextArea1.setText("");
        window.jTextArea1.append("Start Time: " + getCurDate() + "...\n");

        SwingWorker<Void, Integer> swingWorker = new SwingWorker<Void, Integer>() {
            @Override
            protected Void doInBackground() throws Exception {
                try {

                    String SQL_INSERT = "";

                    DefaultCaret caret = (DefaultCaret) window.jTextArea1.getCaret();
                    caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);

                    //Select query to retrieve metadata from table
                    for (Vector row : data) {

                        String SQL_SELECT = "SELECT * FROM " + row.get(1).toString();
                        ResultSetMetaData resMD = conn.createStatement().executeQuery(SQL_SELECT).getMetaData();
                        window.jTextArea1.append("Getting metada from " + row.get(1).toString() + " ...\n");

                        if ("true".equals(row.get(0).toString())) {

                            if (row.get(3).toString().equals("true")) {
                                conn.createStatement().execute("DELETE FROM " + row.get(1));
                                window.jTextArea1.append("Cleaning table " + row.get(1) + "....\n");
                            }
                            
                            
                            for (int k = 1; k <= (int) row.get(2); k++) {

                                int per = k * 100 / (int) row.get(2);
                                window.jProgressBar1.setValue(per);

                                SQL_INSERT = "INSERT INTO " + row.get(1) + "(";

                                for (int i = 1; i <= resMD.getColumnCount(); i++) {
                                    if (resMD.isAutoIncrement(i) == false) {
                                        SQL_INSERT += resMD.getColumnName(i);
                                        if (i < resMD.getColumnCount()) {
                                            SQL_INSERT += ",";
                                        }
                                    }
                                }
                                SQL_INSERT += ") VALUES(";

                                
                                //Foreign Keys 
                                ResultSet resFK = conn.getMetaData().getImportedKeys(conn.getCatalog(), conn.getSchema(), row.get(1).toString());
                                boolean isFK = false;
                                String FK_Table = "";
                                String FK_ColumnName = "";
                                //VALUES TO INSERT
                                for (int i = 1; i <= resMD.getColumnCount() ; i++) {
                                    
                                    while (resFK.next()) {
                                        if (resMD.getColumnName(i).equals(resFK.getString("PKCOLUMN_NAME"))) {
                                            isFK = true;
                                            FK_Table = resFK.getString("PKTABLE_NAME");
                                            FK_ColumnName = resFK.getString("PKCOLUMN_NAME");
                                        }
                                    }
                                    resFK.beforeFirst();

                                                        System.out.println(resMD.getColumnTypeName(i));

                                    
                                    if (resMD.isAutoIncrement(i) == false && isFK == false && resMD.getColumnType(i) == java.sql.Types.INTEGER
                                            | resMD.getColumnType(i) == java.sql.Types.NUMERIC
                                            | resMD.getColumnType(i) == java.sql.Types.FLOAT
                                            | resMD.getColumnType(i) == java.sql.Types.DECIMAL
                                            | resMD.getColumnType(i) == java.sql.Types.BIGINT
                                            | resMD.getColumnType(i) == java.sql.Types.TINYINT
                                            | resMD.getColumnType(i) == java.sql.Types.DOUBLE
                                            | resMD.getColumnType(i) == java.sql.Types.SMALLINT) {
                                        SQL_INSERT += getRandom_Int();
                                    } else if (resMD.getColumnType(i) == java.sql.Types.VARCHAR) {
                                        SQL_INSERT += "'" + getRandom_Str(resMD.getPrecision(i)) + "'";
                                    } else if (resMD.getColumnType(i) == java.sql.Types.BOOLEAN) {
                                        SQL_INSERT += getRandom_Boolean();
                                    } else if (resMD.getColumnType(i) == java.sql.Types.CHAR) {
                                        SQL_INSERT += "'" + (char) getRandom_Char() + "'";
                                    } else if (resMD.getColumnType(i) == java.sql.Types.DATE) {
                                        SQL_INSERT += "'" + getRandom_Date() + "'";
                                    } else if ("BLOB".equals(resMD.getColumnTypeName(i))) {
                                        if (resMD.isNullable(i) == ResultSetMetaData.columnNullable) {
                                            SQL_INSERT += "null";
                                        } else {
                                            conn_methods.popUpErrorMsg("'" + resMD.getColumnName(i).toUpperCase() + "'" + " column in table '" + resMD.getTableName(i) + "' can not be null nor random...");
                                            return null;
                                        }

                                    } else if (isFK) {

                                        ResultSet resFK_Table;

                                        resFK_Table = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY).executeQuery("SELECT " + FK_ColumnName + " FROM " + FK_Table);
                                        //check if referenced table is empty
                                        if (resFK_Table.next() == false) {
                                            conn_methods.popUpErrorMsg("Referenced Column '" + FK_ColumnName + "' in referenced table '" + FK_Table.toUpperCase() + "' is empty...");
                                            return null;
                                        } else {
                                            System.gc();
                                            //otherwise choose random foreign key
                                            resFK_Table.last();
                                            int randFK = ((int) (Math.random() * (resFK_Table.getRow() - 1))) + 1;
                                            int index = 0;
                                            resFK_Table.beforeFirst();
                                            System.out.println("FK : " + FK_ColumnName);
                                            while (resFK_Table.next()) {
                                                if (index == randFK) {
                                                    SQL_INSERT += resFK_Table.getString(FK_ColumnName);
                                                }
                                                index++;
                                            }
                                        }
                                    }
                                    if (resMD.isAutoIncrement(i) == false && i < resMD.getColumnCount() ) {
                                        SQL_INSERT += ",";
                                    }
                                }
                                SQL_INSERT += ")";

                                window.jTextArea1.append(k + "  Inserting data into " + row.get(1).toString() + " ...\n");
                                   System.out.println(SQL_INSERT); 
                            conn.createStatement().executeUpdate(SQL_INSERT);
                                   System.gc();
                            }

                        }
                        
                        System.gc();
                    }
                                       // conn_methods.popUpSuccessMsg("Tables Filled!");

                } catch (SQLException e) {
                    window.jTextArea1.append("Failed...");
                    conn_methods.popUpErrorMsg(e.getLocalizedMessage());
                }
                return null;
            }

            @Override
            protected void done() {
                window.jProgressBar1.setValue(0);
                window.jTextArea1.append("End Time: " + getCurDate());
            }
        };
        swingWorker.execute();

        return true;
    }

    public void cleanTable() {

    }

    public String getRandom_Str(int maxSize) {
        String seed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "abcdefghijklmnopqrstuvxyz";

        int sizeof_ranStr = (int) (Math.random() * (maxSize - 1) + 1);
        StringBuilder ranStr = new StringBuilder(sizeof_ranStr);

        for (int i = 0; i < sizeof_ranStr; i++) {
            int index = (int) (seed.length() * Math.random());

            ranStr.append(seed.charAt(index));
        }
        return ranStr.toString();
    }

    public int getRandom_Int() {
        Random rand = new Random();
        int top = 1000000;
        return rand.nextInt(top);
    }

    public int getRandom_Char() {
        Random rand = new Random();
        return (char) ('a' + rand.nextInt(26));
    }

    public boolean getRandom_Boolean() {
        return new Random().nextBoolean();
    }

    public String getRandom_Date() {

        GregorianCalendar cal = new GregorianCalendar();

        int randomYear = rand_withingBounds(1980, 2020);
        cal.set(cal.YEAR, randomYear);

        int day = rand_withingBounds(1, cal.getActualMaximum(cal.DAY_OF_YEAR));

        cal.set(cal.DAY_OF_YEAR, day);

        return (cal.get(cal.YEAR) + "-" + (cal.get(cal.MONTH) + 1) + "-" + cal.get(cal.DAY_OF_MONTH));
    }

    public int rand_withingBounds(int start, int end) {
        return start + (int) Math.round(Math.random() * (end - start));
    }

    public String getCurDate() {
        LocalDateTime date = LocalDateTime.now();

        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        return date.format(format);
    }

}
