package com.appdynamicspilot.persistence;

import java.lang.Math;
import java.util.List;

import org.apache.log4j.Logger;


import com.appdynamicspilot.model.Item;
import com.appdynamicspilot.oracle.jdbc.OracleQueryExecutor;
import com.appdynamicspilot.util.ArgumentUtils;
import com.appdynamicspilot.util.SpringContext;
import javax.persistence.*;

public class ItemPersistence extends BasePersistenceImpl {
	/**
	 * Logger for this class
	 */
	private static final Logger LOGGER = Logger.getLogger(ItemPersistence.class);


	@SuppressWarnings("unchecked")
	public List<Item> getAllItems() {
        List<Item> itemList = getEntityManager().createQuery("SELECT i FROM Item i ORDER BY i.id").getResultList();
		//DEMO-367 Calling Oracle db in certain percentage
		if (shouldFireSlow()) {
			LOGGER.info("Querying oracle db");

			OracleQueryExecutor oracleItems = (OracleQueryExecutor) SpringContext
					.getBean("oracleQueryExecutor");
			oracleItems.executeOracleQuery();
		}

		return itemList;
	}


    public Item getItemByID(Long id) {
        return getEntityManager().find(Item.class,id);
	}

	public Item getItemByName(String name) {
//		List<Item> itemList = getSession().createCriteria(Item.class).add(Restrictions.eq("title", name)).list();
        Query q = getEntityManager().createQuery("SELECT i FROM Item i WHERE item i.title=:title");
        q.setParameter("title",name);

        List<Item> itemList = (List<Item>) q.getResultList();

		return (ArgumentUtils.isNullOrEmpty(itemList) ? null : itemList.get(0));
	}

}
