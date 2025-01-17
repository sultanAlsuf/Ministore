package clients.cashier;

import catalogue.Basket;
import clients.TextFieldHint;
import clients.ThemeModeManager;
import middle.MiddleFactory;
import middle.OrderProcessing;
import middle.StockReadWriter;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;


/**
 * View of the model 
 */
public class CashierView implements Observer
{
  private static final int H = 300;       // Height of window pixels
  private static final int W = 400;       // Width  of window pixels
  
  private static final String CHECK  = "Check";
  private static final String BUY    = "+ Item";
  private static final String BOUGHT = "Buy";
  private static final String CANCEL = "Clear";

  private final JLabel      pageTitle  = new JLabel();
  private final JLabel      theAction  = new JLabel();
  private final TextFieldHint theInput   = new TextFieldHint("Enter product no");
  private final TextFieldHint theInputNo = new TextFieldHint("Item quantity");
  private final JTextArea   theOutput  = new JTextArea();
  private final JScrollPane theSP      = new JScrollPane();
  private final JButton     theBtCheck = new JButton( CHECK );
  private final JButton     theBtBuy   = new JButton( BUY );
  private final JButton     theBtBought= new JButton( BOUGHT );
  private final JButton theBtCancel = new JButton( CANCEL );

  private final ThemeModeManager themeModeManager;
  private final JPanel mainPanel = new JPanel();  // Added to help with theming

  private StockReadWriter theStock     = null;
  private OrderProcessing theOrder     = null;
  private CashierController cont       = null;
  
  /**
   * Construct the view
   * @param rpc   Window in which to construct
   * @param mf    Factor to deliver order and stock objects
   * @param x     x-coordinate of position of window on screen 
   * @param y     y-coordinate of position of window on screen  
   */
          
  public CashierView(  RootPaneContainer rpc,  MiddleFactory mf, int x, int y  )
  {
    try                                           // 
    {      
      theStock = mf.makeStockReadWriter();        // Database access
      theOrder = mf.makeOrderProcessing();        // Process order
    } catch ( Exception e )
    {
      System.out.println("Exception: " + e.getMessage() );
    }
    Container cp         = rpc.getContentPane();    // Content Pane
    Container rootWindow = (Container) rpc;         // Root Window

    // Initialize theme manager
    themeModeManager = ThemeModeManager.getInstance();;

    // Set up main panel to hold all components
    cp.setLayout(new BorderLayout());
    mainPanel.setLayout(null);  // Keep the null layout for existing absolute positioning
    cp.add(mainPanel, BorderLayout.CENTER);

    rootWindow.setSize( W, H );
    rootWindow.setLocation( x, y );

    Font f = new Font("Monospaced",Font.PLAIN,12);  // Font f is

    pageTitle.setBounds( 110, 0 , 270, 20 );       
    pageTitle.setText( "Thank You for Shopping at MiniStore" );
    mainPanel.add( pageTitle );

    theBtCheck.setBounds( 16, 25+60*0, 80, 40 );    // Check Button
    theBtCheck.addActionListener(e -> {
      String productNum = theInput.getText();
      String quantity = theInputNo.getText();
      cont.doCheck(productNum, quantity);
    });
    mainPanel.add( theBtCheck );                           //  Add to canvas

    theBtBuy.setBounds( 16, 25+60*1, 80, 40 );      // Buy button
    theBtBuy.addActionListener(                     // Call back code
            e -> cont.doBuy() );
    mainPanel.add( theBtBuy );                             //  Add to canvas

    theBtCancel.setBounds( 16, 25+60*2, 80, 40 );  // Clear button, placed between Add and Buy
    theBtCancel.addActionListener(e -> {
      cont.doClearBasket();
      theOutput.setText("");
    });
    mainPanel.add( theBtCancel );

    theBtBought.setBounds( 16, 25+60*3, 80, 40 );   // Bought Button
    theBtBought.addActionListener(                  // Call back code
            e -> cont.doBought() );
    mainPanel.add( theBtBought );                          //  Add to canvas

    theAction.setBounds( 110, 25 , 270, 20 );       // Message area
    theAction.setText( "" );                        // Blank
    mainPanel.add( theAction );                            //  Add to canvas

    theInput.setBounds( 110, 50, 120, 40 );         // Input Area
    mainPanel.add( theInput );                             //  Add to canvas

    theInputNo.setBounds(260, 50, 120, 40 );
    mainPanel.add( theInputNo );

    theSP.setBounds( 110, 100, 270, 160 );          // Scrolling pane
    theOutput.setText( "" );                        //  Blank
    theOutput.setFont( f );                         //  Uses font  
    mainPanel.add( theSP );                                //  Add to canvas
    theSP.getViewport().add( theOutput );           //  In TextArea

    // Set background color for mainPanel to match theme
    mainPanel.setOpaque(true);

    themeModeManager.applyTheme(rootWindow);

    rootWindow.setVisible( true );                  // Make visible
    theInput.requestFocus();                        // Focus is here
  }

  /**
   * The controller object, used so that an interaction can be passed to the controller
   * @param c   The controller
   */

  public void setController( CashierController c )
  {
    cont = c;
  }

  /**
   * Update the view
   * @param modelC   The observed model
   * @param arg      Specific args 
   */
  @Override
  public void update( Observable modelC, Object arg )
  {
    CashierModel model  = (CashierModel) modelC;
    String      message = (String) arg;
    theAction.setText( message );
    Basket basket = model.getBasket();
    if ( basket == null )
      theOutput.setText( "Customers order" );
    else
      theOutput.setText( basket.getDetails() );

    // Schedule focus requests for both text fields on the event dispatch thread
    SwingUtilities.invokeLater(() -> {
      theInput.requestFocus();
      theInputNo.requestFocus();
    });

    // Reapply theme to ensure consistency
    themeModeManager.applyTheme((Container) theInput.getRootPane());
    themeModeManager.applyTheme((Container) theInputNo.getRootPane());
  }
}
