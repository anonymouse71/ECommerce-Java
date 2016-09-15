<%@ taglib uri='http://java.sun.com/jsp/jstl/core' prefix='c' %>
<%@ taglib uri="/struts-tags" prefix="s" %>
<html >
      <head>
        <meta charset="utf-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Sign Up Form</title>
        <link rel="stylesheet" href="css/normalize.css">
        <link href='http://fonts.googleapis.com/css?family=Nunito:400,300' rel='stylesheet' type='text/css'>
        <link rel="stylesheet" href="css/style.css">
    </head>



      <form action="Checkout.action" method="post">

        <fieldset>
          <legend><span class="number">1</span>Shipping Info</legend>

          <input type="hidden" name="ShippingConfirmed" value="true">
          <label for="street1">Street:</label>
          <s:textfield id="street1" name="storeOrder.street1"/>

          <label for="street2">Street 2:</label>
          <s:textfield id="street2" name="storeOrder.street2"/>

          <label for="city">City:</label>
          <s:textfield id="city" name="storeOrder.city"/>

          <label for="state">State:</label>
          <s:textfield id="state" name="storeOrder.state"/>

        </fieldset>
        <button type="submit">Confirm Order</button>
      </form>

    </body>
</html>