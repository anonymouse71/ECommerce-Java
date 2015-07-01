/*
 * Copyright 2015 AppDynamics, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.appdynamicspilot.restv2;

import com.appdynamicspilot.faultinjection.FaultInjection;
import com.appdynamicspilot.faultinjection.FaultInjectionFactory;
import com.appdynamicspilot.jms.MessageProducer;
import com.appdynamicspilot.model.Cart;
import com.appdynamicspilot.model.Fault;
import com.appdynamicspilot.model.Item;
import com.appdynamicspilot.model.User;
import com.appdynamicspilot.service.CartService;
import com.appdynamicspilot.service.FaultService;
import com.appdynamicspilot.service.UserService;
import com.appdynamicspilot.util.SpringContext;
import com.google.gson.Gson;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Path("/json/cart")
public class Carts {
    private static final Logger log = Logger.getLogger(Carts.class.getName());
    // Not used in rest
    @Resource(name = "OrderQueue")
    private Queue orderQueue;
    private MessageProducer messageProducer;


    public MessageProducer getMessageProducer() {
        return (MessageProducer) SpringContext.getBean("messageProducer");
    }

    public void setMessageProducer(MessageProducer messageProducer) {
        this.messageProducer = messageProducer;
    }

    /**
     * Gets cartService bean
     *
     * @return CartService
     */
    public CartService getCartService() {
        return (CartService) SpringContext.getBean("cartService");
    }

    /**
     * Gets userService bean
     *
     * @return UserService
     */
    public UserService getUserService() {
        return (UserService) SpringContext.getBean("userService");
    }

    /**
     * Gets FaultService bean
     *
     * @return FaultService
     */
    public FaultService getFIBugService() {
        return (FaultService) SpringContext.getBean("faultService");
    }

    /**
     * Saves Item to cart
     * Creates a session and inserts records to mysql tables "cart" & "cart-item" as well
     *
     * @param req
     * @param -Item Id to be added to the cart
     * @return CartResponse in json format
     * @throws Exception
     */
    @Path("/{id}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String saveItemInCart(@Context HttpServletRequest req, @PathParam("id") Long id) throws Exception {

        Gson gsonSaveItemsToCart = new Gson();
        CartResponse response = new CartResponse();
        try {
            String username = req.getHeader("USERNAME");
            Item item = getCartService().getItemPersistence().getItemByID(id);
            User user = (User) req.getSession(true).getAttribute("USER");

            if (user == null) {
                user = getUserService().getMemberByLoginName(username);
            }

            /**
             *  Reading time range, user name and fault type.
             *  Applicable only for Fault Injection
             */
            if(!StringUtils.isBlank(username)){
                List<Fault> lsFaultFromCache =  (List<Fault>) CacheManager.getInstance().get(username + "faultCache");
                if(lsFaultFromCache != null && lsFaultFromCache.size() > 0){
                    injectFault(username,lsFaultFromCache);
                }
                else {
                    List<Fault> lsFault = getFIBugService().getAllBugsByUser(username);
                    injectFault(username,lsFault);
                }
            }
            Cart cart = getCartService().getCartByUser(user.getId());
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                cart.addItem(item);
                getCartService().saveItemInCart(cart);
            } else {
                cart.setUser(user);
                cart.addItem(item);
                getCartService().updateItemInCart(cart);
            }
            response.setCartSize(String.valueOf(cart.getCartSize()));
            response.setCartTotal(cart.getCartTotal());

        }catch (Exception e) {
            log.error(e);
        }
        return gsonSaveItemsToCart.toJson(response);
    }

    /**
     * Injects Faults
     * @param username
     * @param lsFault - List of faults available
     */
    private void injectFault(String username, List<Fault> lsFault){
        FaultInjectionFactory fiFactory = new FaultInjectionFactory();
        FaultInjection fi = null;
        for(Fault fault : lsFault){

            //Creating Fault injection object parsing the bugName removing spaces.
            fi = fiFactory.getFaultInjection(fault.getBugname().replace(" ", ""));

            //Parsing time frame and calling the inject fault method based on time and user.
            if(username == fault.getUsername()) {
                if (checkTime(fault.getTimeframe())) ;
                fi.injectFault();
            }
        }
    }

    /**
     * Helper for time frame parser and comparison
     * @param timeFrame
     * @return
     * @throws Exception
     */
    private boolean checkTime(String timeFrame){
        //Variables used for comparison with current date in the parsed format.
        Date parsedStartTime = null, parsedEndTime = null, parsedCurrentTime = null;
        Calendar cal = new GregorianCalendar();

        //Parsing the date according to Hours, Minutes set on the UI and setting the Locale to US.
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm", Locale.US);

        String startTimeString = timeFrame.substring(0, 5);
        String endTimeString = timeFrame.substring(8);

        String currentTime = cal.get(Calendar.HOUR_OF_DAY)+ ":" +cal.get(Calendar.MINUTE);


        try {
            parsedStartTime= parser.parse(startTimeString);
            parsedEndTime = parser.parse(endTimeString);
            parsedCurrentTime = parser.parse(currentTime);
            //returns only if the time is within the time range selected on the UI.
            if(parsedCurrentTime.after(parsedStartTime) && parsedCurrentTime.before(parsedEndTime)){
                return true;
            }
        } catch (ParseException e) {
            log.error(e);
        }
        return false;
    }

    /**
     * Gets all the items from cart
     * Either by Cart Session or from mysql table "cart" using user id
     *
     * @param req
     * @return List of ShoppingCartItem object
     * @throws Exception
     */
    @Path("/all")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public String getAllItems(@Context HttpServletRequest req) throws Exception {
        Gson gsonGetAllItems = new Gson();
        ShoppingCart shoppingCart = new ShoppingCart();
        try {
            String username = req.getHeader("USERNAME");
            User user = getUserService().getMemberByLoginName(username);
            List<Item> cartItems = getCartService().getAllItemsByUser(user.getId());
            for (Item cartEntry : cartItems) {
                ShoppingCartItem item = new ShoppingCartItem();
                item.setId(String.valueOf(cartEntry.getId()));
                item.setImagePath(cartEntry.getImagePath());
                item.setTitle(cartEntry.getTitle());
                item.setItemId(String.valueOf(cartEntry.getId()));
                item.setPrice(cartEntry.getPrice());
                shoppingCart.addItem(item);
            }
        } catch (Exception e) {
            log.error(e);
        }
        return gsonGetAllItems.toJson(shoppingCart.getAllItems());
    }

    /**
     * Deletes Items from cart based on user id and item id
     * Removes the removed item from session and updates the session with the new cart object
     * Deletes the item from mysqsl tables "cart" & "cart-item" as well
     *
     * @param req
     * @param id
     * @return plain text
     * @throws Exception
     */
    @Path("/{id}")
    @DELETE
    @Produces(MediaType.TEXT_PLAIN)
    public String deleteItemInCart(@Context HttpServletRequest req, @PathParam("id") Long id) throws Exception {
        try {
            String username = req.getHeader("username");
            if (username != null) {
                Integer iReturnValue = getCartService().deleteItemInCartV2(username, id);
                log.info("iReturnValue" + iReturnValue);
                if (iReturnValue == 0)
                    return "Deleted item id " + id + " Successfully.";
                else if (iReturnValue == 2)
                    return "There is no item id " + id + " in the cart.";
                else if (iReturnValue == 1)
                    return "Cart is empty.";
            }
        } catch (Exception e) {
            log.error(e);
        }
        return "Error deleting items from your cart";
    }

    /**
     * Checks out the cart
     * Clears the session
     *
     * @param req
     * @return Message with orderid
     * @throws Exception
     */
    @Path("/co")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String checkout(@Context HttpServletRequest req) throws Exception {
        User user = (User) req.getSession(true).getAttribute("USER");
        if (user == null) {
            String username = req.getHeader("USERNAME");
            if (username == null) {
                return "User not logged in. Nothing to checkout.";
            } else {
                user = getUserService().getMemberByLoginName(username);
            }
        }
        Cart cart = getCartService().getCartByUser(user.getId());
        if (cart == null) {
            return "Nothing In cart to checkout.";
        }

        List<Item> items = cart.getItems();
        List<Long> orderIdList = new ArrayList<Long>();
        String orderIds = "";
        boolean outOfStock = false;
        try {
            for (Item item : items) {
                Long orderId = getCartService().checkOut(item.getId(), 1);
                if (item.getId() != 0) {
                    orderIds = ", " + orderId;
                }
                if (orderId == 0) {
                    outOfStock = true;
                }
                orderIdList.add(orderId);
            }
            log.info("orderIds : " + orderIds);
            if (orderIdList.size() > 0 && !outOfStock) {
                getMessageProducer().sendMessageWithOrderId(orderIds, user.getEmail());
                getMessageProducer().sendTextMessageWithOrderId();
                return "Total amount is $" + cart.getCartTotal() + " Order ID(s) for your order(s) : " + orderIds;
            } else {
                if (getMessageProducer() != null) {
                    getMessageProducer().sendMessageWithOrderId(orderIds, user.getEmail());
                }
                return "Order not created as one or more items in your cart were out of stock. Total was $" + cart.getCartTotal();
            }
        } catch (Exception ex) {
            log.error(ex);
        }
        return "Error Processing Checkout";
    }

    //Used in Fault Injection to store the faultinfo - user, bug name , Time Frame
    @Path("/savefaults")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.TEXT_PLAIN)
    public String saveBugsForFaultInjection(List<Fault> lsFault) throws Exception {
        String returnMessage = "";
        String userName = "";
        try {
            if (lsFault != null && lsFault.size() > 0) {
                for(Fault fault : lsFault) {
                    userName = fault.getUsername();
                    getFIBugService().saveFIBugs(fault);
                }

                //Check if cache already exists
                List<Fault> lsFaultFromCache =  (List<Fault>) CacheManager.getInstance().get(userName + "faultCache");
                if(lsFaultFromCache != null && lsFaultFromCache.size() > 0){
                    //If yes, get the existing list and add it to the newly created list
                    for(Fault fault : lsFault) {
                        lsFaultFromCache.add(fault);
                    }

                    CacheManager.getInstance().put(userName + "faultCache", lsFaultFromCache);
                }
                else {
                    CacheManager.getInstance().put(userName + "faultCache", lsFault);
                }
                returnMessage = "Fault(s) injected successfully";
            } else{
                returnMessage = "No Fault received";
            }

        } catch (Exception ex) {
            log.error(ex);
        }
        return returnMessage;
    }

    //Used in Fault Injection to store the faultinfo - user, bug name , Time Frame
    @Path("/readfaults")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Fault> readBugsForFaultInjection(@Context HttpServletRequest req) throws Exception {
        String username = req.getHeader("USERNAME");
        List<Fault> lsFault = new ArrayList<Fault>();
        try {
            if(!StringUtils.isBlank(username)){
                lsFault = getFIBugService().getAllBugsByUser(username);
            }
        } catch (Exception ex) {
            log.error(ex);
        }
        return lsFault;
    }

    //Not used in rest
    @Path("/checkout/")
    @POST
    @Consumes("application/x-www-form-urlencoded")
    @Produces(MediaType.TEXT_PLAIN)
    public String checkoutCartItem(@Context HttpServletRequest req, @FormParam("itemId") List<String> idList,
                                   @FormParam("quantity") List<String> qList, @FormParam("emailId") String emailId) {
        try {

            List<Long> orderIdList = new ArrayList<Long>();
            boolean outOfStock = false;
            String orderIds = "";
            for (int i = 0; i < idList.size(); i++) {
                Long orderId = getCartService().checkOut(Long.parseLong(idList.get(i)), Integer.parseInt(qList.get(i)));

                if (i != 0) {
                    orderIds = ", " + orderIds;
                }
                if (orderId == 0) {
                    outOfStock = true;
                }
                orderIdList.add(orderId);

                //TODO See if this is used for any messaging
                /*OrderDetail orderDetail = new OrderDetail();
                orderDetail.setOrderId(orderId);
				orderDetail.setId(cart.getItem().getId());
				orderDetail.setTitle(cart.getItem().getTitle());
	            */

            }
            if (orderIdList.size() > 0 && !outOfStock) {
                messageProducer.sendMessageWithOrderId(orderIds, emailId);
                //messageProducer.sendTextMessageWithOrderId();
                return "Order ID(s) for your order(s) : " + orderIds;

            } else {
                if (messageProducer != null) {//TODO where is messageProducer instantiated/injected
                    messageProducer.sendMessageWithOrderId(orderIds, emailId);
                }
                return
                        "Order not created as one or more items in your cart were out of stock";
            }
        } catch (NumberFormatException e) {
            log.error(e);
        } catch (Exception e) {
            log.error(e);
        }
        return "Error occured processing checkout";
    }
}
