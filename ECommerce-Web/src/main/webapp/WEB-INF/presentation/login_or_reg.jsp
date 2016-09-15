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

    <body>
          <c:set var="refererPage" scope="request" value="${request.getHeader}"/>
          <c:out value="${refererPage}"/>
          <form action="UserLogin.action" method="post">


            <fieldset>
              <c:if test="${request.inCheckoutFlow == true}">
                 <input type="hidden" id="inCheckoutFlow" name="inCheckoutFlow" value="true">
              </c:if>
              <legend><span class="number">1</span>Sign in</legend>

              <label for="mail">Username:</label>
                <s:textfield id="textBox" name="loginName"/>

              <label for="password">Password:</label>
              <s:password name="password" id="password"/>

            </fieldset>
            <button type="submit">Login</button>
          </form>


      <form action="Register.action" method="post">
        
        <fieldset>
          <legend><span class="number">1</span>Shipping</legend>
          <label for="name">Name:</label>
          <input type="text" id="name" name="user_name">
          
          <label for="mail">Email:</label>
          <input type="email" id="mail" name="user_email">
          
          <label for="password">Password:</label>
          <input type="password" id="password" name="user_password">

          <label for="street1">Street:</label>
          <input type="text" id="street1" name="street1">

          <label for="street2">Street 2:</label>
          <input type="text" id="street2" name="street2">

          <label for="city">City:</label>
          <input type="text" id="city" name="city">

        </fieldset>
        <button type="submit">Register</button>
      </form>

    </body>
</html>
