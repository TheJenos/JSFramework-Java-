/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 *
 * @author Thanura
 */
public class Framework {

    private JSFramwork obj;
    private Class c;
    private HttpServletRequest requ;
    private HttpServletResponse resp;
    private JSONObject json = new JSONObject();
    private PrintWriter out;

    public Framework(JSFramwork obj, HttpServletRequest resq, HttpServletResponse resp) {
        try {
            this.obj = obj;
            this.c = obj.getClass();
            this.requ = resq;
            this.resp = resp;
            this.json.put("ClassName", this.c.getName());
            out = this.resp.getWriter();
            obj.setRequ(resq);
            obj.setResp(resp);
            this.resp.setCharacterEncoding("UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendDataToJSON() {
        Method mlist[] = c.getDeclaredMethods();
        JSONArray methodArray = new JSONArray();
        for (int i = 0; i < mlist.length; i++) {
            Method method = mlist[i];
            methodArray.put(Modifier.isSynchronized(method.getModifiers())?"Sync_"+method.getName():method.getName());
        }
        this.json.put("Methods", methodArray);
    }

    public void getData() {
        if (requ.getParameter("run") != null) {
            runMethods(requ.getParameter("run"));
        } else {
            sendDataToJSON();
        }
        out.println(json.toString());
    }

    private void runMethods(String parameter) {
        try {
            Object Return = null;
            this.json.put("MethodName", parameter);
            if (requ.getParameter("para[]") != null) {
                String paraS[] = requ.getParameterValues("para[]");
                Class paraC[] = new Class[paraS.length];
                Object paraO[] = new Object[paraS.length];
                for (int i = 0; i < paraS.length; i++) {
                    String object = paraS[i];
                    DataPill dp = getRealObject(object);
                    paraC[i] = dp.getDatatype();
                    paraO[i] = dp.getObject();
                }
                Return = c.getMethod(parameter, paraC).invoke(this.obj, paraO);
            } else {
                Return = c.getMethod(parameter, null).invoke(this.obj, null);
            }
            this.json.put("Return", Return);
        } catch (Exception ex) {
            this.json.put("Return", ex.toString());
        }
    }

    private static DataPill getRealObject(String s) throws Exception {
        String data[] = s.split(":");
        String ss = data[0];
        if (data.length > 1) {
            if (ss.equalsIgnoreCase("String")) {
                return new DataPill(String.class, data[1]);
            } else if (ss.equalsIgnoreCase("int")) {
                return new DataPill(int.class, Integer.parseInt(data[1]));
            } else if (ss.equalsIgnoreCase("long")) {
                return new DataPill(long.class, Long.parseLong(data[1]));
            } else if (ss.equalsIgnoreCase("char")) {
                return new DataPill(char.class, data[1].charAt(0));
            } else if (ss.equalsIgnoreCase("byte")) {
                return new DataPill(byte.class, Byte.parseByte(data[1]));
            } else if (ss.equalsIgnoreCase("float")) {
                return new DataPill(float.class, Float.parseFloat(data[1]));
            } else if (ss.equalsIgnoreCase("double")) {
                return new DataPill(double.class, Double.parseDouble(data[1]));
            } else if (ss.equalsIgnoreCase("boolean")) {
                return new DataPill(boolean.class, Boolean.parseBoolean(data[1]));
            } else {
                return new DataPill(Class.forName(ss), data[1]);
            }
        } else {
            return new DataPill(String.class, data[1]);
        }
    }
}
