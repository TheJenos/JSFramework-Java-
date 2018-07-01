/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

/**
 *
 * @author Thanura
 */
public class MultiPartReader {

    int maxFileSize = 10000000;
    int maxMemSize = 10000000;

    private Map<String, List<FileItem>> map = null;

    public MultiPartReader(HttpServletRequest req) {
        try {
            if (req.getContentType().indexOf("multipart/form-data") >= 0) {
                DiskFileItemFactory factory = new DiskFileItemFactory();
                factory.setSizeThreshold(maxMemSize);
                factory.setRepository(new File("c:\\temp"));
                ServletFileUpload upload = new ServletFileUpload(factory);
                upload.setSizeMax(maxFileSize);
                map = upload.parseParameterMap(req);
            } else {
                throw new RuntimeException("Not a Multipart");
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    public String getSingleStringParameter(String key) {
        try {
            return map.get(key).get(0).getString();
        } catch (NullPointerException e) {
            return null;
        }
    }

    public ArrayList<String> getMutiStringParameter(String key) {
        List<FileItem> fi = map.get(key);
        ArrayList<String> re = new ArrayList<String>();
        for (FileItem fileItem : fi) {
            re.add(fileItem.getString());
        }
        return re;
    }

    public int getSingleIntParameter(String key) {
        return Integer.parseInt(map.get(key).get(0).getString());
    }

    public ArrayList<Integer> getMutiIntParameter(String key) {
        List<FileItem> fi = map.get(key);
        ArrayList<Integer> re = new ArrayList<Integer>();
        for (FileItem fileItem : fi) {
            re.add(Integer.parseInt(fileItem.getString()));
        }
        return re;
    }

    public String getSingleFileName(String key) {
        return map.get(key).get(0).getName();
    }

    public FileItem getSingleFile(String key) {
        return map.get(key).get(0);
    }

    public List<FileItem> getMultiFile(String key) {
        return map.get(key);
    }

}
