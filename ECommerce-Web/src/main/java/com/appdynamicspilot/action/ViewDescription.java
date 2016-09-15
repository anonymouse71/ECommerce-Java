package com.appdynamicspilot.action;

import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.ServletRequestAware;
import com.appdynamicspilot.service.ItemService;
import com.appdynamicspilot.model.Item;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by aleftik on 8/11/16.
 */
public class ViewDescription extends ActionSupport implements ServletRequestAware {
    private HttpServletRequest request;


    private ItemService itemService;

    public String view () {
        String id = getServletRequest().getParameter("id");
        if (("".equals(id)) || (id == null)) {
             return "FAILURE";
        } else {
            Item item = loadItem(Long.parseLong(id));
            getServletRequest().setAttribute("item",item);
            return "SUCCESS";
        }
    }


    private Item loadItem(Long id) {
        return getItemService().getItemByID(id);
    }

    @java.lang.Override
    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletRequest getServletRequest () {
        return this.request;
    }

    public ItemService getItemService() {
        return itemService;
    }

    public void setItemService(ItemService itemService) {
        this.itemService = itemService;
    }
}
