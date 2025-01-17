package clients.backDoor;

import clients.TextFieldHint;
import clients.ThemeModeManager;
import middle.MiddleFactory;
import middle.StockReadWriter;

import javax.swing.*;
import java.awt.*;
import java.util.Observable;
import java.util.Observer;

/**
 * Implements the Customer view.
 */

public class BackDoorView implements Observer
{
  private static final String RESTOCK  = "Add";
  private static final String CLEAR    = "Clear";
  private static final String QUERY    = "Query";
  private static final String REPORTS  = "Reports";  // New constant for Reports button
 
  private static final int H = 300;       // Height of window pixels
  private static final int W = 400;       // Width  of window pixels

  private final JLabel      pageTitle  = new JLabel();
  private final JLabel      theAction  = new JLabel();
  private final TextFieldHint theInput   = new TextFieldHint("Enter product no");
  private final TextFieldHint  theInputNo = new TextFieldHint("Quantity");
  private final JTextArea   theOutput  = new JTextArea();
  private final JScrollPane theSP      = new JScrollPane();
  private final JButton     theBtClear = new JButton( CLEAR );
  private final JButton     theBtRStock = new JButton( RESTOCK );
  private final JButton     theBtQuery = new JButton( QUERY );
  private final JButton     theBtReports = new JButton(REPORTS);

  private final ThemeModeManager themeModeManager;
  private final JPanel mainPanel = new JPanel();  // Added to help with theming

  private StockReadWriter theStock     = null;
  private BackDoorController cont= null;

  /**
   * Construct the view
   * @param rpc   Window in which to construct
   * @param mf    Factor to deliver order and stock objects
   * @param x     x-cordinate of position of window on screen 
   * @param y     y-cordinate of position of window on screen  
   */
  public BackDoorView(  RootPaneContainer rpc, MiddleFactory mf, int x, int y )
  {
    try                                             // 
    {      
      theStock = mf.makeStockReadWriter();          // Database access
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
    pageTitle.setText( "Staff check and manage stock" );                        
    mainPanel.add( pageTitle );
    
    theBtQuery.setBounds( 16, 25+60*0, 80, 40 );    // Buy button 
    theBtQuery.addActionListener(                   // Call back code
      e -> cont.doQuery( theInput.getText() ) );
    mainPanel.add( theBtQuery );                           //  Add to canvas

    theBtRStock.setBounds( 16, 25+60*1, 80, 40 );   // Check Button
    theBtRStock.addActionListener(                  // Call back code
      e -> cont.doRStock( theInput.getText(),
                          theInputNo.getText() ) );
    mainPanel.add( theBtRStock );                          //  Add to canvas

    theBtClear.setBounds( 16, 25+60*2, 80, 40 );    // Buy button 
    theBtClear.addActionListener(                   // Call back code
      e -> cont.doClear() );
    mainPanel.add( theBtClear );                           //  Add to canvas

    // Add Reports button below Clear button
    theBtReports.setBounds(16, 25+60*3, 80, 40);
    theBtReports.addActionListener(
            e -> cont.generateReport());  // Call new controller method
    mainPanel.add(theBtReports);
 
    theAction.setBounds( 110, 25 , 270, 20 );       // Message area
    theAction.setText( "" );                        // Blank
    mainPanel.add( theAction );                            //  Add to canvas

    theInput.setBounds( 110, 50, 120, 40 );         // Input Area
    mainPanel.add( theInput );                             //  Add to canvas
    
    theInputNo.setBounds( 260, 50, 120, 40 );       // Input Area
    mainPanel.add( theInputNo );                           //  Add to canvas

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
  
  public void setController( BackDoorController c )
  {
    cont = c;
  }

  /**
   * Update the view, called by notifyObservers(theAction) in model,
   * @param modelC   The observed model
   * @param arg      Specific args 
   */
  @Override
  public void update( Observable modelC, Object arg )
  {
    BackDoorModel model  = (BackDoorModel) modelC;
    String        message = (String) arg;

    if(message != null && message.contains("\n")) {
      theAction.setText( "Stock Status Report" );
    } else {
      theAction.setText( message );
    }

    // If the message is a report (contains multiple lines), display it in theOutput
    if (message != null && message.contains("\n")) {
      theOutput.setText(message);
      theOutput.setCaretPosition(0);  // Scroll to top
    } else {
      theOutput.setText(model.getBasket().getDetails());
    }

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