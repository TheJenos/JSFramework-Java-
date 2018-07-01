/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.io.File;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.servlet.http.*;
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
    private boolean debug;

    public Framework(JSFramwork obj, HttpServletRequest resq, HttpServletResponse resp, boolean debug) {
        try {
            this.obj = obj;
            this.c = obj.getClass();
            this.requ = resq;
            this.resp = resp;
            this.debug = debug;
            if (this.debug) {
                this.json.put("ClassName", this.c.getName());
            }
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
            boolean file = method.getReturnType().equals(File.class);
            if (method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(MultiPartReader.class)) {
                methodArray.put("Multi_" + method.getName() + (file ? "_File" : ""));
            } else if (Modifier.isSynchronized(method.getModifiers())) {
                methodArray.put("Sync_" + method.getName() + (file ? "_File" : ""));
            } else {
                methodArray.put(method.getName() + (file ? "_File" : ""));
            }
        }
        this.json.put("Methods", methodArray);
    }

    public void getData() {
        if (requ.getContentType() != null && requ.getContentType().contains("multipart/form-data")) {
            MultiPartReader mpr = new MultiPartReader(requ);
            runMethods_Multi(mpr.getSingleStringParameter("run"), mpr);
        } else if (requ.getParameter("run") != null) {
            runMethods(requ.getParameter("run"));
        } else {
            sendDataToJSON();
        }
        if (json.toString().length() > 6) {
            out.println(json.toString());
        }
    }

    private void runMethods_Multi(String parameter, MultiPartReader mpr) {
        try {
            Object Return = c.getMethod(parameter, mpr.getClass()).invoke(this.obj, mpr);
            if (this.debug) {
                this.json.put("MethodName", parameter);
            }
            if (Return instanceof File) {
                File ff = (File) Return;
                this.obj.getResp().setHeader("Content-Disposition", "attachment; filename=\"" + ff.getName()+ "\"");
                java.io.FileInputStream fileInputStream = new java.io.FileInputStream(ff);
                int i;
                while ((i = fileInputStream.read()) != -1) {
                    this.obj.getResp().getWriter().write(i);
                }
                fileInputStream.close();
            } else {
                this.json.put("Return", Return);
            }
        } catch (Exception ex) {
            this.json.put("ErrorMsg", ex.getCause().getMessage());
            if (this.debug) {
                this.json.put("Error", stackTraceToString(ex.getCause()));
            }
        }
    }

    private void runMethods(String parameter) {
        try {
            Object Return = null;
            if (this.debug) {
                this.json.put("MethodName", parameter);
            }
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
            if (Return instanceof File) {
                File ff = (File) Return;
                this.obj.getResp().setHeader("Content-Disposition", "attachment; filename=\"" + ff.getName()+ "\"");
                java.io.FileInputStream fileInputStream = new java.io.FileInputStream(ff);
                int i;
                while ((i = fileInputStream.read()) != -1) {
                    this.obj.getResp().getWriter().write(i);
                }
                fileInputStream.close();
            } else {
                this.json.put("Return", Return);
            }
        } catch (Exception ex) {
            this.json.put("ErrorMsg", ex.getCause().getMessage());
            if (this.debug) {
                this.json.put("Error", stackTraceToString(ex.getCause()));
            }
        }
    }

    public String stackTraceToString(Throwable e) {
        StringBuilder sb = new StringBuilder();
        for (StackTraceElement element : e.getStackTrace()) {
            sb.append(element.toString());
            sb.append("\n");
        }
        return sb.toString();
    }

    private static DataPill getRealObject(String s) throws Exception {
        String data[] = s.split("::");
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
            } else if (ss.equalsIgnoreCase("json")) {
                return new DataPill(JSONObject.class, new JSONObject(data[1]));
            } else {
                return new DataPill(Class.forName(ss), data[1]);
            }
        } else {
            return new DataPill(String.class, data[1]);
        }
    }
}
