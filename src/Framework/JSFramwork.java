/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package Framework;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Thanura
 */
public class JSFramwork {

    private HttpServletRequest requ;
    private HttpServletResponse resp;

    public void setRequ(HttpServletRequest requ) {
        this.requ = requ;
    }

    public HttpServletRequest getRequ() {
        return requ;
    }

    public HttpServletResponse getResp() {
        return resp;
    }

    public void setResp(HttpServletResponse resp) {
        this.resp = resp;
    }
}
