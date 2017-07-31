package com.subhajitdas.c.read;

import java.util.Map;

/**
 * Created by Subhajit Das on 09-07-2017.
 */

public class CmmtData {
    public static class PostCmmt {
        public String cmmt_img_url = "";
        public String cmmt_text;
        public String name;
        public Map<String, String> time;
        public String userId;

        public PostCmmt(){

        }
    }

    public static class RetriveCmmt {
        public String cmmt_text="";
        public String name="";
        public String time="";
        public String userId="";
        public String cmmt_img_url = "";

        public RetriveCmmt(){

        }
    }

    public String key;
    public PostCmmt postData;
    public RetriveCmmt retriveData;
}
