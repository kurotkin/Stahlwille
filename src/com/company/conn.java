package com.company;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class conn {
    private static Connection conn = null;
    private static Statement st = null;
    private static ResultSet resSet = null;

    public static void Connect() throws ClassNotFoundException, SQLException {
        conn = null;
        Class.forName("org.sqlite.JDBC");
        conn = DriverManager.getConnection("jdbc:sqlite:/C:/ProgramData/STAHLWILLE/Sensomaster4/sensomaster4.db");

        st = conn.createStatement();
    }

    public static void CloseDB() throws ClassNotFoundException, SQLException {
        conn.close();
        st.close();
        resSet.close();
    }

    public static void ReadDB() throws ClassNotFoundException, SQLException {
        String sqlUrl = "SELECT id,\n" +
                "       serno,\n" +
                "       time,\n" +
                "       max_torque,\n" +
                "       max_angle,\n" +
                "       sequence_id,\n" +
                "       (SELECT name FROM sequence WHERE id = joint_data.sequence_id) AS name,\n" +
                "       settings_id,\n" +
                "       (SELECT name FROM joint_setting WHERE id = joint_data.settings_id) AS nameF,\n" +
                "       deleted,\n" +
                "       assembly_object,\n" +
                "       slot_id,\n" +
                "       seq_position,\n" +
                "       result,\n" +
                "       memid,\n" +
                "       error,\n" +
                "       result_torque,\n" +
                "       release_angle\n" +
                "  FROM joint_data;";
        resSet = st.executeQuery(sqlUrl);

        while (resSet.next()) {
            int id = resSet.getInt("id");
            int sn = resSet.getInt("serno");
            int t = resSet.getInt("time");
            String n = resSet.getString("name");
            String nF = resSet.getString("nameF");
            int result = resSet.getInt("result");
            printLn(id, sn, t, n, nF, result);

        }
    }
    public static void printLn(int id, int sn, int t, String n, String nF, int result) {
        System.out.println("");
        if (result == 1)
            System.out.print("Ok  ");
        else
            System.out.print("Fail");
        System.out.print("\tid: ");
        System.out.print(id);
        System.out.print("\ts/n: ");
        System.out.print(sn);
        System.out.print("\tt: ");
        System.out.print(t);
        System.out.print("\ttime: ");
        String data = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date((long)t * 1000));
        System.out.print(data);
        System.out.print("\tname: ");
        if (new String(n).equals(""))
            System.out.print("---");
        else
            System.out.print(n);
        System.out.print("\tnameF: ");
        System.out.print(nF);

        System.out.println("");
    }

    public static void printLn2(int t, String n, String nF, int result) {
        System.out.println("");
        if (result == 1)
            System.out.print("Ok  ");
        else
            System.out.print("Fail");
        System.out.print("\ttime: ");
        String data = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new java.util.Date((long)t * 1000));
        System.out.print(data);
        System.out.print("\tname: ");
        if (new String(n).equals(""))
            System.out.print("---");
        else
            System.out.print(n);
        System.out.print("\tnameF: ");
        System.out.print(nF);

        System.out.println("");
    }
}

