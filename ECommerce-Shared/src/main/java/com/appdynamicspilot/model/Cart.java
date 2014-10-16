package com.appdynamicspilot.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.persistence.*;

import java.util.List;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import java.lang.Override;

/**
 * @author Ravichandra
 */
@XmlRootElement(name="cart")
@XmlAccessorType(XmlAccessType.FIELD)
@Entity(name="Cart")
@Table(name="cart")
public class Cart implements java.io.Serializable {

	private static Logger log = Logger.getLogger(Cart.class.getName());
	private static final long serialVersionUID = 1L;


    @Id
    @GeneratedValue( strategy = GenerationType.IDENTITY)
    @XmlElement(name="cart-id")
	private Long id;

    @OneToMany(cascade = CascadeType.ALL,fetch=FetchType.LAZY)
	private List<Item> items = new ArrayList<Item>();

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="user_id")
	private User user;
	


	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}


	public List<Item> getItems() {
		return items;
	}
	public void setItems(List<Item >items) {
		this.items = items;
	}


	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}


    public double getCartTotal() {
        double total=0;
        if (items != null ) {
           for (Item item:items)  {
                total+=item.getPrice();
           }
        }
        return total;
	}

    public void addItem(Item item) {
      getItems().add(item);
    }


    public void removeItem(Item item) {
        getItems().remove(item);
    }
	
}
