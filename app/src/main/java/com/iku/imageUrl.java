package com.iku;

import android.text.Editable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class imageUrl {

//    Map<String, List<String>> map1;
//    Map<String,Object> map2,map3;
List<String> arrangements;
    String subject,text;
    public imageUrl()
    {

    }
    public imageUrl(List<String> asList, String subject,String text)
    {
       this.arrangements= asList;
       this.subject=subject;
       this.text=text;
    }


    public String getSubject() {
        return subject;
    }

    public String getText() {
        return text;
    }

    public List<String> getArrangements() {
        return arrangements;
    }
}
