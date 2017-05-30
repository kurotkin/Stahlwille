package com.company;

import com.healthmarketscience.jackcess.*;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.*;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogManager;
import java.util.logging.Logger;

public class Main {
    // Настройки по умолчанию
    private static String sqliteUrl = "C:/ProgramData/STAHLWILLE/Sensomaster4/sensomaster4.db";
    private static String dbUrl = "C:/ProgramData/STAHLWILLE/Sensomaster4/Database.accdb";
    private static String prUrl = "C:/Stahlwille";
    // Параметры
    private static DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");
    private static DateFormat dateFormatForName = new SimpleDateFormat("yyyyMMddHHmmss");
    private static Logger log = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException, InterruptedException, ParseException {
        // Запуск лога
        loadLog();
        System.out.println("Start...");
        // Запуск коннектора
        try {
            Connect();
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        System.out.println("Ok");
    }

    private static void Connect() throws ClassNotFoundException, SQLException, IOException, ParseException, DocumentException {
        Class.forName("org.sqlite.JDBC");
        Connection conn = DriverManager.getConnection("jdbc:sqlite:/" + sqliteUrl);
        Statement st = conn.createStatement();
        String sqlUrl = SQLQ.url_2();
        ResultSet resSet = st.executeQuery(sqlUrl);

        Database db = DatabaseBuilder.open(new File(dbUrl));
        Table table = db.getTable("Stahlwille714");
        Cursor cursor = CursorBuilder.createCursor(table);

        // pdf
        Document document = new Document(PageSize.A4.rotate(), 20, 20, 50, 50);
        File folder = new File(prUrl);
        if (!folder.exists()) {
            folder.mkdir();
        }
        String prUrlFull = prUrl + File.separator + dateFormatForName.format(new Date()) + ".pdf";
        PdfWriter.getInstance(document, new FileOutputStream(prUrlFull));
        document.open();
        // Шрифты
        BaseFont bf = BaseFont.createFont("Arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
        Font fontBig18 = new Font(bf, 18, Font.BOLDITALIC, new CMYKColor(0, 0, 0,255));
        Font fontBig14 = new Font(bf, 12, Font.BOLDITALIC, new CMYKColor(0, 0, 0,255));
        Font fontBig14red = new Font(bf, 12, Font.BOLDITALIC, new CMYKColor(0, 255, 20,0));
        Font font = new Font(bf, 12);

        // Составление документа
        Paragraph title1 = new Paragraph("Протокол замеров затяжки", fontBig18);
        title1.setAlignment(Element.ALIGN_CENTER);
        Chapter chapter1 = new Chapter(title1, 1);
        chapter1.setNumberDepth(0);

        Paragraph someSectionText2 = new Paragraph("Документ составляется автоматически. \n" +
                "Дата составления документа: " + dateFormat.format(new Date()), font);
        someSectionText2.setAlignment(Element.ALIGN_CENTER);
        chapter1.add(someSectionText2);

        // Таблица
        PdfPTable t = new PdfPTable(9);
        t.setWidthPercentage(100);;
        t.setSpacingBefore(25);
        t.setSpacingAfter(25);
        t.addCell(new PdfPCell(new Phrase("№ п/п", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Обозначение тех. процесса / операции", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Серийный номер оборудования", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Дата, время затяжки", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Момент фактический на ключе (Нм)", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Момент номинальный (Нм)", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Нижний допустимый момент (Нм)", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Верхний допустимый момент (Нм)", fontBig14)));
        t.addCell(new PdfPCell(new Phrase("Оценка", fontBig14)));

        System.out.print("[");
        String toLog = "[";
        int id = 1;
        while (resSet.next()) {
            int serno = resSet.getInt("serno");
            int timeInt = resSet.getInt("time");
            long timeLong = (long)timeInt * 1000L;
            Date date = new Date(timeLong);
            int torqueInt = resSet.getInt("max_torque");
            double torque = (double)torqueInt/1000.0;
            String torqueString = Double.toString(torque);
            BigDecimal val = new BigDecimal(torqueString);
            double torqueNom = resSet.getDouble("torque");
            double tol_lower = torqueNom - resSet.getDouble("setting_tol_lower");
            double tol_upper = torqueNom + resSet.getDouble("setting_tol_upper");;
            String name = resSet.getString("name");
            String result = "Не норма";;
            if(resSet.getInt("result") == 1){
                result = "Норма";
            } else if (resSet.getInt("result") == 3){
                if ((torque > tol_lower) && (torque < tol_upper)){
                    result = "Норма";
                } else {
                    result = "Не норма";
                }
            }

            for (Row row : cursor.newIterable().addMatchPattern("Код", id)) {
                row.put("active", true);
                row.put("Time", date);
                row.put("Torque", val);
                row.put("Ok", result);
                table.updateRow(row);
            }
            System.out.print("*");

            toLog += "{name:" + name + ",serno:" + Integer.toString(serno) + ",time:" + dateFormat.format(date) +
                    ",torque:" + torqueString + ",torqueNom:" + Double.toString(torqueNom) +
                    ",lower:" + Double.toString(tol_lower) + ",upper:" +
                    Double.toString(tol_upper) + ",result:" + result + "},";
            t.addCell(new PdfPCell(new Phrase(Integer.toString(id), font)));
            t.addCell(new PdfPCell(new Phrase(name, font)));
            t.addCell(new PdfPCell(new Phrase(Integer.toString(serno), font)));
            t.addCell(new PdfPCell(new Phrase(dateFormat.format(date), font)));
            t.addCell(new PdfPCell(new Phrase(Double.toString(torque), font)));
            t.addCell(new PdfPCell(new Phrase(Double.toString(torqueNom), font)));
            t.addCell(new PdfPCell(new Phrase(Double.toString(tol_lower), font)));
            t.addCell(new PdfPCell(new Phrase(Double.toString(tol_upper), font)));
            Font fontResult = result.equals("Норма") ? font : fontBig14red;
            t.addCell(new PdfPCell(new Phrase(result, fontResult)));
            id++;
        }
        while (id <= 24) {
            for (Row row : cursor.newIterable().addMatchPattern("Код", id)) {
                row.put("active", true);
                row.put("Time", null);
                row.put("Torque", null);
                row.put("Ok", null);
                table.updateRow(row);
            }
            System.out.print("-");
            id++;
        }
        toLog = toLog + "]";
        log.info(toLog);
        System.out.println("]");
        chapter1.add(t);
        document.add(chapter1);
        document.close();
        db.flush();
        db.close();
        st.close();
        conn.close();
    }

    private static void loadLog() {
        try {
            LogManager.getLogManager().readConfiguration(Main.class.getResourceAsStream("logging.properties"));
        } catch (IOException e) {
            System.err.println("Could not setup logger configuration: " + e.toString());
        }
    }
}
