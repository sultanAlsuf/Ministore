package clients.cashier;

import catalogue.Basket;
import catalogue.Product;
import debug.DEBUG;
import middle.*;

import java.util.Observable;

/**
 * Implements the Model of the cashier client
 */
public class CashierModel extends Observable
{
  private enum State { process, checked }

  private State       theState   = State.process;   // Current state
  private Product     theProduct = null;            // Current product
  private Basket      theBasket  = null;            // Bought items

  private String      pn = "";                      // Product being processed

  private StockReadWriter theStock     = null;
  private OrderProcessing theOrder     = null;

  /**
   * Construct the model of the Cashier
   * @param mf The factory to create the connection objects
   */

  public CashierModel(MiddleFactory mf)
  {
    try                                           // 
    {
      theStock = mf.makeStockReadWriter();        // Database access
      theOrder = mf.makeOrderProcessing();        // Process order
    } catch ( Exception e )
    {
      DEBUG.error("CashierModel.constructor\n%s", e.getMessage() );
    }
    theState   = State.process;                  // Current state
  }

  /**
   * Get the Basket of products
   * @return basket
   */
  public Basket getBasket()
  {
    return theBasket;
  }

  /**
   * Check if the product is in Stock
   * @param productNum The product number
   */
  public void doCheck(String productNum, String quantityStr) {
    String theAction = "";
    theState = State.process; // State process
    pn = productNum.trim();  // Product no.

    // Parse the quantity entered by the user
    int amount = 1; // Default to 1 if the quantity is invalid
    try {
      amount = Integer.parseInt(quantityStr.trim());
    } catch (NumberFormatException e) {
      // Handle invalid quantity input
      theAction = "Invalid quantity entered!";
      setChanged();
      notifyObservers(theAction);
      return;
    }

    try {
      if (theStock.exists(pn)) { // Stock exists?
        Product pr = theStock.getDetails(pn); // Get details
        if (pr.getQuantity() >= amount) { // In stock?
          theAction = String.format("%s : %7.2f (%2d) ",
                  pr.getDescription(),  // description
                  pr.getPrice(),        // price
                  pr.getQuantity());    // quantity
          theProduct = pr;         // Remember product
          theProduct.setQuantity(amount);  // Set quantity
          theState = State.checked; // OK, await BUY
        } else { // Not enough in stock
          theAction = "Not enough in stock. " + pr.getQuantity() + " " + pr.getDescription() +" left!";
        }
      } else { // Unknown product number
        theAction = "Unknown product number " + pn;
      }
    } catch (StockException e) {
      DEBUG.error("%s\n%s", "CashierModel.doCheck", e.getMessage());
      theAction = e.getMessage();
    }

    setChanged();
    notifyObservers(theAction);
  }


  /**
   * Buy the product
   */
  public void doBuy()
  {
    String theAction = "";
    int    amount  = 1;                         //  & quantity
    try
    {
      if ( theState != State.checked )          // Not checked
      {                                         //  with customer
        theAction = "please check item availablity";
      } else {
        boolean stockBought =                   // Buy
                theStock.buyStock(                    //  however
                        theProduct.getProductNum(),         //  may fail
                        theProduct.getQuantity() );         //
        if ( stockBought )                      // Stock bought
        {                                       // T
          makeBasketIfReq();                    //  new Basket ?
          theBasket.add( theProduct );          //  Add to bought
          theAction = "Added " +            //    details
                  theProduct.getDescription() + " to basket";  //
        } else {                                // F
          theAction = "!!! Not in stock";       //  Now no stock
        }
      }
    } catch( StockException e )
    {
      DEBUG.error( "%s\n%s",
              "CashierModel.doBuy", e.getMessage() );
      theAction = e.getMessage();
    }
    theState = State.process;                   // All Done
    setChanged(); notifyObservers(theAction);
  }

  /**
   * Customer pays for the contents of the basket
   */
  public void doBought()
  {
    String theAction = "";
    int    amount  = 1;                       //  & quantity
    try
    {
      if ( theBasket != null &&
              theBasket.size() >= 1 )            // items > 1
      {                                       // T
        theOrder.newOrder( theBasket );       //  Process order
        theBasket = null;                     //  reset
      }                                       //
      theAction = "Start New Order";            // New order
      theState = State.process;               // All Done
      theBasket = null;
    } catch( OrderException e )
    {
      DEBUG.error( "%s\n%s",
              "CashierModel.doCancel", e.getMessage() );
      theAction = e.getMessage();
    }
    theBasket = null;
    setChanged(); notifyObservers(theAction); // Notify
  }

  public void clearBasket()
  {
    String theAction = "";
    try
    {
      if (theBasket != null) {
        theBasket.clear(); // Clear the items in the basket if needed.
      }
      theBasket = null; // Set the basket to null, effectively clearing it.
      theAction = "Basket cleared."; // Action message
    }
    catch (Exception e) {
      DEBUG.error("CashierModel.clearBasket\n%s", e.getMessage());
      theAction = "Failed to clear basket: " + e.getMessage();
    }

    setChanged();  // Mark as changed
    notifyObservers(theAction); // Notify observers (view)
  }
  /**
   * ask for update of view callled at start of day
   * or after system reset
   */
  public void askForUpdate()
  {
    setChanged(); notifyObservers("Welcome");
  }

  /**
   * make a Basket when required
   */
  private void makeBasketIfReq()
  {
    if ( theBasket == null )
    {
      try
      {
        int uon   = theOrder.uniqueNumber();     // Unique order num.
        theBasket = makeBasket();                //  basket list
        theBasket.setOrderNum( uon );            // Add an order number
      } catch ( OrderException e )
      {
        DEBUG.error( "Comms failure\n" +
                "CashierModel.makeBasket()\n%s", e.getMessage() );
      }
    }
  }

  /**
   * return an instance of a new Basket
   * @return an instance of a new Basket
   */
  protected Basket makeBasket()
  {
    return new Basket();
  }
}
  
