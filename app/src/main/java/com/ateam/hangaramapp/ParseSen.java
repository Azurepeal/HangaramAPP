package com.ateam.hangaramapp;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.regex.Pattern;

/**
 * Created by Suhyun on 2015-12-11.
 */
public class ParseSen {


    private ParseCallBack callbackEvent;


    String menu_l[];
    String menu_d[];
    boolean check[];

    static String PARSE_ERROR = "정보가 존재하지 않습니다.";
    static int TIME_LUNCH = 1;
    static int TIME_DINNER = 2;
    int mm, ay;

    ParseSen(ParseCallBack event) {

        callbackEvent = event;

        check = new boolean[31];
        menu_d = new String[1000];
        menu_l = new String[1000];
        for(int i=0;i<31;i++) {
           check[i] = false;
            menu_d[i]=menu_l[i]="";
        }

    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            callbackEvent.callbackMethod(ParseSen.this);
        }
    };
    public boolean isMenuExist(int d){
        return check[d];
    }
    public String getMenu(int d){
        if(isMenuExist(d)) {
            // 원하는 꼴로 수정하셈 (menu_l[날짜] = 점심 메뉴 menu_d[날짜] = 저녁메뉴
            return "점심 : " + menu_l[d] + "저녁 : " + menu_d[d];
        }
        else return PARSE_ERROR;


    }
    public void parse(){
        Thread myThread = new Thread(new Runnable() {
            public void run() {
                String urlToRead="http://hes.sen.go.kr/spr_sci_md00_001.do?";
                urlToRead+="mm="+mm;
                urlToRead+="&ay="+ay;
                int time = 0;
                urlToRead+="&schulCode=B100000549&schulCrseScCode=4";

                Log.i("info", "PARSE TARGET : " + urlToRead);

                URL url; // The URL to read
                HttpURLConnection conn; // The actual connection to the web page
                BufferedReader rd; // Used to read results from the web page
                String line; // An individual line of the web page HTML
                String result = ""; // A long string containing all the HTML


                try {
                    url = new URL(urlToRead);
                    conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("GET");
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));

                    while ((line = rd.readLine()) != null) {

                        if(line.contains("<td>")){
                            // 이상한건 삭제한다.
                            line = line.replace("\t","");
                            line = line.replace("</td>","");
                            line = line.replace("<br />"," ");

                            int d=0; // 일자

                            if(line.contains("[")) {
                                if (line.substring(line.indexOf("<td>") + 1, line.indexOf("[")) != null) {
                                    d = Integer.parseInt(line.substring(line.indexOf("<td>") + "<td>".length(), line.indexOf("[")).replace(" ",""));
                                }
                            }

                            line = line.replace("<td>","");

                            if(d==0) continue;

                            if (line.contains("[중식]")) {
                                if(line.contains("[석식]")) { // 점심 + 저녁
                                    menu_l[d] = line.substring(line.indexOf("[중식]") + "[중식]".length(), line.indexOf("[석식]"));
                                    menu_d[d] = line.substring(line.indexOf("[석식]") + "[석식]".length(), line.length());

                                    check[d] = true;
                                }
                                else{ // only 점심
                                    menu_l[d] = line.substring(line.indexOf("[중식]") + "[중식]".length(), line.length());
                                    check[d] = true;
                                }
                            }
                            else if(line.contains("[석식]")){ // only 저녁
                                menu_d[d] = line.substring(line.indexOf("[석식]") + "[석식]".length(), line.length());
                                check[d] = true;
                            }
                        }
                    }
                    rd.close();

                    handler.sendMessage(handler.obtainMessage());

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });

        myThread.start();
    }
    void setMM(int m){
        mm = m;
    }
    void setAY(int y){
        ay =y;
    }

}
