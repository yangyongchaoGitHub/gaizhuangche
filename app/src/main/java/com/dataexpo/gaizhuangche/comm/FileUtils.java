package com.dataexpo.gaizhuangche.comm;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class FileUtils {

    private static String filename = "gaizhuangchezhanpda.txt";
    /**
     * 保存记录到文件
     * @param name
     * @param date
     * @param company
     * @param role
     * @param code
     * @param expoid
     */
    public static void saveRecord(String name, String date, String company, String role, String code, String expoid) {
        File file = new File(Environment.getExternalStorageDirectory().getPath());

        //Log.i("save file-------------", file.getAbsolutePath());
        if (!file.exists()) {
            file.mkdirs();
        }

        file = new File(file, filename);

        BufferedWriter bw = null;
        FileWriter fw = null;
        try {
            fw = new FileWriter(file, true);
            bw = new BufferedWriter(fw);
            PrintWriter printWriter = new PrintWriter(bw);

            String strContent = name + " " + date + " " + company + " " +
                    role + " " + code + " " + expoid;
            printWriter.println(strContent);

            printWriter.close();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (bw != null) {
                    bw.close();
                }

                if (fw != null) {
                    fw.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
