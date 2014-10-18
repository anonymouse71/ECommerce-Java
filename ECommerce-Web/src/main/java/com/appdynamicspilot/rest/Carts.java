package com.appdynamicspilot.rest;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.jms.Queue;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;

import com.appdynamicspilot.jms.MessageProducer;
import com.appdynamicspilot.model.Cart;
import com.appdynamicspilot.model.Item;
import com.appdynamicspilot.model.User;
import com.appdynamicspilot.service.CartService;
import com.appdynamicspilot.util.SpringContext;

@Path("/cart")
public class Carts {
	private static final Logger log = Logger.getLogger(Carts.class.getName());
	@Resource(name="OrderQueue")
	private Queue orderQueue;
	
	private MessageProducer messageProducer;

	public MessageProducer getMessageProducer() {
		return (MessageProducer) SpringContext.getBean("messageProducer");
	}

	public void setMessageProducer(MessageProducer messageProducer) {
		this.messageProducer = messageProducer;
	}

	@Path("{id}")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public Response saveItemInCart(@Context HttpServletRequest req,@Context HttpServletResponse response, @PathParam("id")Long id) {
		String sessionId = req.getHeader("JSESSIONID");
		HttpSession session = req.getSession();
		Cart cart = (Cart) session.getAttribute("CART");
		if (cart == null) {
			cart = new Cart();
		}
		Item item = getCartService().getItemPersistence().getItemByID(id);
		cart= new Cart();
		User user = (User) req.getSession(true).getAttribute("USER");
		cart.setUser(user);
		cart.addItem(item);
		cart.setUser(user);
		getCartService().saveItemInCart(cart);

		session.setAttribute("CART",cart);
		response.setHeader("cart-size", String.valueOf(getCartService().getCartSize(user.getId())));
		response.setHeader("cart-total", Double.toString(cart.getCartTotal()));
		
		return Response.noContent().build();

	}
	
	@Path("/all")
	@GET
	@Produces(MediaType.APPLICATION_XML)
	public List<ShoppingCartItem> getAllItems(@Context HttpServletRequest req) {
        Cart cart = (Cart) req.getSession().getAttribute("CART");
        List<Item> cartItems = null;
        if (cart!=null) {
            cartItems = getCartService().getAllCartItems(cart.getId());
        } else {

        }
		ShoppingCart shoppingCart = new ShoppingCart();
		for (Item cartEntry: cartItems) {
			ShoppingCartItem item = new ShoppingCartItem();
			item.setId(String.valueOf(cartEntry.getId()));
			item.setImagePath(cartEntry.getImagePath());
			item.setTitle(cartEntry.getTitle());
			item.setItemId(String.valueOf(cartEntry.getId()));
			item.setPrice(cartEntry.getPrice());
            shoppingCart.addItem(item);
		}
		
		return shoppingCart.getAllItems();
	}	
	
	@Path("{id}")
	@DELETE
	@Produces(MediaType.APPLICATION_XML)
	public Response deleteItemInCart(@Context HttpServletRequest req,@Context HttpServletResponse response, @PathParam("id")Long id) {
		getCartService().deleteItemInCart(id);		
		return Response.noContent().build();
	}
	
	@Path("/checkout/")
	@POST
	@Consumes("application/x-www-form-urlencoded")
	@Produces(MediaType.TEXT_PLAIN)
	public String checkoutCartItem(@Context HttpServletRequest req,@FormParam("itemId") List<String> idList,
				@FormParam("quantity") List<String> qList,@FormParam("emailId") String emailId){
		try {
		
			List<Long> orderIdList = new ArrayList<Long>();
			boolean outOfStock = false;
			String orderIds = "";
			for (int i=0; i<idList.size(); i++){
				Long orderId = getCartService().checkOut(Long.parseLong(idList.get(i)), Integer.parseInt(qList.get(i)));
				
				if (i!=0){
					orderIds = ", " + orderIds;
				}
				if (orderId == 0) {
					outOfStock = true;
				}
				orderIdList.add(orderId);
				
				//TODO See if this is used for any messaging
	/*			OrderDetail orderDetail = new OrderDetail();
				orderDetail.setOrderId(orderId);
				orderDetail.setId(cart.getItem().getId());
				orderDetail.setTitle(cart.getItem().getTitle());
	*/			
				
			}
			if (orderIdList.size()>0 && !outOfStock) {
				messageProducer.sendMessageWithOrderId(orderIds, emailId);
				//messageProducer.sendTextMessageWithOrderId();
				return "Order ID(s) for your order(s) : "+ orderIds;
				
			} else {
				if(messageProducer!=null){//TODO where is messageProducer instantiated/injected
					messageProducer.sendMessageWithOrderId(orderIds, emailId);
				}
				return
						"Order not created as one or more items in your cart were out of stock";
			}
		}
		catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
			
		return "Error occured processing checkout";
	}
	
	@Path("/co")
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String checkout(@Context HttpServletRequest req) {
		Cart cart = (Cart) req.getSession(true).getAttribute("CART");
		User user = (User) req.getSession(true).getAttribute("USER");
		
		if (user == null) {
			return "User not logged in. Nothing to checkout.";		
		}
		if (cart == null) {
			return "Nothing In cart to checkout.";
		}
		
			List<Item> items = cart.getItems();
			List<Long> orderIdList = new ArrayList<Long>();
			String orderIds = "";
			boolean outOfStock = false;
			try {
			for (Item item:items) {
				Long orderId = getCartService().checkOut(item.getId(), 1);
				if (item.getId()!=0){
					orderIds = ", " + orderIds;
				}
				if (orderId == 0) {	
					outOfStock = true;
				}
				orderIdList.add(orderId);
			}
			
			if (orderIdList.size()>0 && !outOfStock) {
				getMessageProducer().sendMessageWithOrderId(orderIds, user.getEmail());
				getMessageProducer().sendTextMessageWithOrderId();
				return "Total amount is $" + cart.getCartTotal() + " Order ID(s) for your order(s) : "+ orderIds;
			} else {
				if(getMessageProducer() !=null) { 
					getMessageProducer().sendMessageWithOrderId(orderIds, user.getEmail());
				}
				return "Order not created as one or more items in your cart were out of stock. Total was $" + cart.getCartTotal() ;
			}	
			} catch (Exception ex) {
				log.fatal(ex);
			}
			return "Error Processing Checkout";
	}	
	
	public CartService getCartService() {
		return (CartService)SpringContext.getBean("cartService");
	}	
}
