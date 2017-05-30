package com.company;

public class SQLQ {
    public static String url_1 () {
        return "SELECT time, max_torque, result FROM joint_data;";
    }

    public static String url_2 () {
        String sqlUrl = "SELECT id,\n" +
                "       serno,\n" +
                "       time,\n" +
                "       max_torque,\n" +
                "       max_angle,\n" +
                "       sequence_id,\n" +
                "       (SELECT name FROM joint_setting WHERE id = joint_data.settings_id) AS name,\n" +
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
                "       release_angle,\n" +
                "       (SELECT torque FROM joint_setting WHERE id = joint_data.settings_id) AS torque," +
                "       (SELECT tol_lower FROM joint_setting WHERE id = joint_data.settings_id) AS setting_tol_lower," +
                "       (SELECT tol_upper FROM joint_setting WHERE id = joint_data.settings_id) AS setting_tol_upper" +
                "  FROM joint_data;";
        return sqlUrl;
    }
}
