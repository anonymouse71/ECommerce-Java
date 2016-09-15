package com.appdynamicspilot.action;

import com.appdynamicspilot.model.StoreOrder;
import com.appdynamicspilot.model.User;
import com.opensymphony.xwork2.ActionSupport;
import org.apache.struts2.interceptor.ServletRequestAware;
import org.apache.struts2.interceptor.ServletResponseAware;
import org.apache.commons.validator.routines.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


import com.appdynamicspilot.model.Cart;
import com.appdynamicspilot.model.Item;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aleftik on 8/16/16.
 */
public class CheckoutAction extends ActionSupport implements ServletResponseAware, ServletRequestAware {
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;
    private StoreOrder storeOrder = null;
    private static final String ORDER_DETAILS = "ORDER_DETAILS";
    private static final String LOGIN_OR_REGISTER = "LOGIN_OR_REGISTER";
    private static final String CREDIT_CARD_DETAILS = "CREDIT_CARD_DETAILS";
    private static final String SHIPPING_CONFIRM = "SHIPPING_CONFIRM";
    private static final String CONFIRM_ORDER = "CONFIRM_ORDER";


    public String doCheckout() {
        request.setAttribute("inCheckoutFlow",true);
        if (getServletRequest().getParameter("ShippingConfirmed") != null) {
            getServletRequest().getSession().setAttribute("ShippingConfirmed", true);
        }
        String result = CONFIRM_ORDER;
        storeOrder = (StoreOrder) request.getSession(true).getAttribute("STORE_ORDER");
        Cart cart = (Cart) request.getSession(true).getAttribute("CART");
        User user = (User) request.getSession(true).getAttribute("USER");

        if (user == null) {
               return LOGIN_OR_REGISTER;
        }

        if (storeOrder != null) {
            if (getServletRequest().getSession(true).getAttribute("ShippingConfirmed") == null) {
                return SHIPPING_CONFIRM;
            } else {
                boolean isReadyToProcess = validateOrder(getStoreOrder());
                if (isReadyToProcess == true) {
                    return CONFIRM_ORDER;
                }   else {
                    return CREDIT_CARD_DETAILS;
                }
            }
        }   else {
            setStoreOrder(createOrderFromCart(cart));
            request.getSession(true).setAttribute("STORE_ORDER", getStoreOrder());
            return SHIPPING_CONFIRM;
        }
    }

    private StoreOrder createOrderFromCart(Cart cart) {
        StoreOrder order = new StoreOrder();
        if (cart.getUser()!= null) {
            order.getAddress().setStreet1(cart.getUser().getAddress().getStreet1());
            order.getAddress().setStreet2(cart.getUser().getAddress().getStreet2());
            order.getAddress().setCity(cart.getUser().getAddress().getCity());
            order.getAddress().setState(cart.getUser().getAddress().getState());
            order.getAddress().setCountry(cart.getUser().getAddress().getCountry());
            order.getAddress().setZip(cart.getUser().getAddress().getZip());
        }
        List itemsForOrder = new ArrayList<Item>();
        for (Item i : cart.getItems()) {
            itemsForOrder.add(new Item(i));
        }
        order.setItems(itemsForOrder);
        return order;
    }

    public boolean validateOrder(StoreOrder order) {
        if (order == null) return false;
        if (order.getCcNumber() != null) {
            CreditCardValidator v = new CreditCardValidator(CreditCardValidator.NONE + CreditCardValidator.AMEX + CreditCardValidator.VISA + CreditCardValidator.MASTERCARD);
            return v.isValid (order.getCcNumber());
        }
        return true;
    }

    public HttpServletRequest getServletRequest() {
        return request;
    }


    public void setServletRequest(HttpServletRequest request) {
        this.request = request;
    }

    public HttpServletResponse getServletResponse() {
        return response;
    }

    public void setServletResponse(HttpServletResponse response) {
        this.response = response;
    }

    public StoreOrder getStoreOrder() {
        return storeOrder;
    }

    public void setStoreOrder(StoreOrder storeOrder) {
        this.storeOrder = storeOrder;
    }

    @Override
    public void validate() {
        Cart cart = (Cart) request.getSession(true).getAttribute("CART");
        if (cart == null) {
            addActionError("Inactive cart.");
        } else if (cart.getCartSize() == 0) {
            addActionError("Nothing in your cart.");
        }
    }
}
