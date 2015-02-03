
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Array;
import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Scanner;
import java.lang.Object;

/**
 * @author Ping Lang    pil8@pitt.edu
 * @author Matthew
 * 
 * administration login: username: root , password: root
 * user login: username: user, password: user
 */
 
public class team03 {

    private Connection connection;
    private String username, password;
    private Scanner input;
    static String adminUsername = "root";
    static String adminPassword = "root";
	static String userUsername = "user";
    static String userPassword = "user";
    
    public team03(){
            try {
                   /* get username and password */
                    Scanner scanner = new Scanner(new File("db.config"));
                    String username = scanner.nextLine();
                    String password = scanner.nextLine();
                    scanner.close();
                    
                    try {
                            /* register oracle driver and connect to db */
                            DriverManager.registerDriver (new oracle.jdbc.driver.OracleDriver());
                            String url = "jdbc:oracle:thin:@db10.cs.pitt.edu:1521:dbclass"; 
                            connection = DriverManager.getConnection(url, username, password); 
                            input = new Scanner(System.in);
                            
                            System.out.println("Welcome to PittAirways!");
                           // showReservationInfo("10001");
                          //  buyTicket("10002");
                            promptMenu(0);
                            // for test
                           // eraseDatabase();
                           // loadScheduleInformation("flightInfo.txt");
                            //loadPriceInformation("priceInfo.txt");
                            //changePriceInformation("NYC","PIT", 300, 10);
                            //loadPlaneInformation("planeInfo.txt");
                           // generatePassengerManifest("09/09/2013 03:25:00", "153"); 
                    } catch(SQLException e)  {
                            System.err.println("Error connecting to database: " + e.toString());
                    }
                    
            } catch (FileNotFoundException e) {
                    System.err.println("Could not find db.config file: " + e.toString());
            }
    }
    
    public void promptMenu(int menu){
    	List<String> menuList = new ArrayList<String>();
    	int userChoice = 0;
    	String menuPrompt = "Please input a number as your choice from the menu";
    	
    	switch(menu){
    	/* Admin Interface */
	    case 2:
	            menuList = Arrays.asList(
	                    "1.Erase the Database",
	                    "2.Load schedule information",
	                    "3.Load pricing information",
	                    "4.Load plane information",
	                    "5.Generate passenger manifest for specific flight on given day",
	                    "6.Logout"
	            );
	            
	            userChoice = getUserNumericMenuChoice("Administrator menu", menuList, menuPrompt, true);
	            break;
	    /* Customer Interface */
	    case 1:
	            menuList = Arrays.asList(
	            		"1. Add customer",
	            		"2. Show customer information",
	            		"3. Find price for flights",
	            		"4. Find routes between two cities",
	            		"5. Find available seats for a flight on a given date",
	            		"6. Add reservation",
	            		"7. Show reservation infomation",
	            		"8. Buy ticket",
	                    "9. Logout"
	            );
	            userChoice = getUserNumericMenuChoice("Customer menu", menuList, menuPrompt, true);
	            break;
	    /* main menu */
	    default:
	            menuList = Arrays.asList(
	                    "1.Administrator login",
	                    "2.Customer login",
	                    "3.Exit"
	            );
	            userChoice = getUserNumericMenuChoice("Main menu", menuList, menuPrompt, true);
	            break;
	    }
    	
        /* handle user's choice */
        System.out.println("\n" + menuList.get(userChoice - 1));
     
        if (menu == 2) {
        	 /* deal with admin user choice */
        	String inputFilenamePrompt = "Please specified the input filename";
        	switch(userChoice) {
	            	case 1:
		                    /* Erase the database */
		            		System.out.println("Are you sure to erase the database?");
		            		String inputString = getUserInput("Please choose Y/N", true);
		            		
		            		if (inputString.equals("Y")){
		            			eraseDatabase();
		            		}else if(inputString.equals("N")){
		            			System.out.println("Returning buck to Admin menu");
		            		}else{
		            			System.out.println("Invalid input! Returning back to Admin menu.");
		            		}
		            		
		                    promptMenu(2);
		                    break;
		            case 2:
			                /* Load schedule information */
			            	String flightFilename = getUserInput(inputFilenamePrompt, true);
			            	//loadScheduleInformation("flightInfo.txt");
			            	loadScheduleInformation(flightFilename);
			            	
			            	promptMenu(2);
			                break;
		            case 3:
			                /* load price information */
			            	System.out.println("Do you want to load pricing information L or change the price of an existing flight C?");
			            	String loadOrChange = getUserInput("Please choose L/C", true);	
			            	
			            	if (loadOrChange.equals("L")){
			            		String priceFilename = getUserInput(inputFilenamePrompt, true);
			            		loadPriceInformation(priceFilename);
			            		//loadPriceInformation("priceInfo.txt");
			            	}else if (loadOrChange.equals("C")){
			            	/* change price information */
			            		System.out.println("Please provide the following information");
			            		String departureCity = getUserInput("departure_city", true);
			            		String arrivalCity = getUserInput("arrival_city", true);
			            		int highPrice = getUserNumericInput("high_price", true);
			            		int lowPrice = getUserNumericInput("low_price", true);
			            		changePriceInformation(departureCity, arrivalCity, highPrice, lowPrice);
			            	}else{
			            		System.out.println("Invalid input! Returning back to Admin menu.");
			            	}
			            	
			                promptMenu(2);
			                break;
		            case 4:
		                    /* load plane information */
			            	String planeFilename = getUserInput(inputFilenamePrompt, true);
			            	//loadScheduleInformation("planeInfo.txt");
			            	loadPlaneInformation(planeFilename);
			            	
			            	promptMenu(2);
		                    break;
		            case 5:
		                	/* Generate passenger manifest for specific flight on given day */
		            		System.out.println("Please provide the following information");
		            		String date = getUserInput("Date DD/MM/YYYY HH24:MI:SS", true);
		                	String flightNumber = getUserInput("Flight number", true);
		                	generatePassengerManifest(date, flightNumber);
		                	promptMenu(2);
		                	break;
		            
		            default:
		                    promptMenu(0);
		                    break;

                }
        } else if (menu == 1){
         /* deal with customer user choice */
        	String inputReservationNumPrompt = "Please enter the reservation number";
        	switch(userChoice) {
        			case 1:
        				//"1. Add customer"
        				makeNewCustomer();
        				
        				promptMenu(1);
        				break;
        			case 2:
        				//"2. Show customer information"
        				showCustomerInfo();
        				
        				promptMenu(1);
        				break;
        			case 3:
        				//"3. Find price for flights"
        				showPricingInfo();
        				
        				promptMenu(1);
        				break;
        			case 4:
        				//"4. Find routes between two cities",
	            		showRoutes();
	            		
	            		promptMenu(1);
	            		break;
        			case 5:
        				//"5. Find available seats for a flight on a given date",
	            		showAvailableSeats();
	            		
	            		promptMenu(1);
	            		break;
        			case 6:
        				//"6. Add reservation",
        				addReservation();
        				
        				promptMenu(1);
        				break;
        			case 7:
        				String reservationNumForInfo = getUserInput(inputReservationNumPrompt, true);
        				showReservationInfo(reservationNumForInfo);
        				
        				promptMenu(1);
        				break;
        			case 8:
        				String reservationNumToBuyTicket = getUserInput(inputReservationNumPrompt, true);
        				buyTicket(reservationNumToBuyTicket);
        				
        				promptMenu(1);
        				break;
		            default:
	                    promptMenu(0);
	                    break;
        	}
        	
        } else {
            /* deal with main menu choices */
            switch(userChoice) {
                    case 1:
                            /* administrator login */
                            if (login(1)) {
                                    System.out.println("\nWelcome, " + username + "!");
                                    promptMenu(2);
                            } else {
                                    System.out.println("\nError! Invalid username/password!");
                                    promptMenu(0);
                            }
                            break;
                    case 2:        
                            /* user login */
                            if (login(2)) {
                                    System.out.println("\nWelcome, " + username + "!");
                                    promptMenu(1);
                            } else {
                                    System.out.println("\nError! Invalid username/password!");
                                    promptMenu(0);
                            }
                            break;
                    default:
                            System.out.println("You're leaving PittAirways! Goodbye!!");
                            break;
            }
        }
    	
    }


	public void addReservation() {
		
		String cid = null;
		String res_num = null;
		String deptCity = null;
		String arrCity = null;
		String deptCity1 = null;
		String arrCity1 = null;
		String departureDate = null;
		String returnDate = null;
		String userChoice = null;
		String flightNo1 = null;
		String flightNo2 = null;
		int trip1=0, trip2=0;
		
		DateFormat dateFormat = new SimpleDateFormat("MM/dd/yyyy kk:mm:ss");
		Calendar cal = Calendar.getInstance();
		System.out.println(dateFormat.format(cal.getTime()));
		boolean success = false, legReserved = false, completed = false, isRoundTrip = false;
		PreparedStatement statement = null;
		
		System.out.println("Please enter the following information at each prompt.");
		System.out.println("Your customer ID: ");
		cid = input.nextLine();
		
		//Now to create the reservation
		String sqlInsertResInfo = "insert into reservation(reservation_number, cid, cost, " +
				"reservation_date, ticketed) " +
				"values('0',?,0,to_date(?,'MM/DD/YYYY HH24:MI:SS'),'N')";
		
		String sqlGetReservation = "select reservation_number from reservation " + 
		"where cid = ? and reservation_date = to_date(?,'MM/DD/YYYY HH24:MI:SS')";
		ResultSet resultSet = null;
		
		try {
			statement = connection.prepareStatement(sqlInsertResInfo);

            statement.setString(1,cid);
            statement.setString(2, dateFormat.format(cal.getTime()));
            statement.executeUpdate();

            statement = connection.prepareStatement(sqlGetReservation);
            statement.setString(1,cid);
            statement.setString(2, dateFormat.format(cal.getTime()));
            resultSet = statement.executeQuery();
            if(resultSet.next()){
            	res_num = resultSet.getString(1);
            }
            
	        System.out.println("Sucessfully created a new reservation.");
	        
		} catch (SQLException e) {
			handleSQLException(sqlInsertResInfo, e);
			handleSQLException(sqlGetReservation, e);
		} 
		
		do {
			System.out.println("Is this a round trip reservation? [yes/no]: ");
			userChoice = input.nextLine();
			if (userChoice.equalsIgnoreCase("yes")) {
				isRoundTrip = true;
				int legCount = 1;
				
				/*
				 * First trip to destination city. It could be multiple legs.
				 * Need to get destination city, arrival city, and date
				 */
				System.out
						.println("The departing city of the first trip: ");
				deptCity = input.nextLine();
				System.out
						.println("The destination city of the first trip: ");
				arrCity = input.nextLine();
				do {
					System.out
							.println("The flight date of the first leg (MM/DD/YYYY): ");
					departureDate = input.nextLine();
					String[] split = departureDate.split("/");
					if (Integer.parseInt(split[0]) > 0
							&& Integer.parseInt(split[0]) < 13)
						if (Integer.parseInt(split[1]) > 0
								&& Integer.parseInt(split[1]) < 32)
							if (Integer.parseInt(split[2]) > 2012
									&& Integer.parseInt(split[2]) < 2020)
								success = true;
					if (!success)
						System.out
								.println("Please remember to format your date as such MM/DD/YYYY");

				} while (!success);
				
				
				//Now we are going to confirm reservation of outgoing trip
				do {
					
					showAvailableSeats(deptCity, arrCity, departureDate);
					trip1 = showPricingInfo(deptCity, arrCity, isRoundTrip);
					System.out
							.println("Please enter the flight number(s)" +
									" you would like to choose (separated by spaces): ");
					flightNo1 = input.nextLine();
					String[] split = flightNo1.split(" ");
					System.out.println("You selected flight(s) ");
					for (int i = 0; i < split.length; i++) {
						System.out.println(split[i]);
					}
					System.out
							.println("Please confirm this reservation [yes/no]: ");
					userChoice = input.nextLine();
					if(userChoice.equalsIgnoreCase("yes")){
						String sqlFirstTrip = "insert into reservation_detail(reservation_number, " +
								"flight_number, flight_date, leg) " +
								"values(?,?,to_date(?,'MM/DD/YYYY'),?)"; 
								
						for (int i = 0; i < split.length; i++) {
							try{
								statement = connection.prepareStatement(sqlFirstTrip);
								statement.setString(1, res_num);
								statement.setString(2, split[i]);
								statement.setString(3, departureDate);
								statement.setInt(4, legCount++);
								statement.executeUpdate();
								System.out.println("Leg reserved!");
								legReserved = true;
							} catch(SQLException e){
								handleSQLException(sqlFirstTrip, e);
							}
						}		
					}
				} while (!legReserved);
				
				legReserved = false;
				
				/*
				 * Now for the return trip of the flight
				 * Again need destination city, arrival city should be same of course
				 * Could be multiple legs
				 */
				System.out.println("The departing city of the return trip: ");
				deptCity1 = input.nextLine();
				System.out.println("The destination city of the return trip: ");
				arrCity1 = input.nextLine();
				do {
					System.out.println("The flight date of the return trip (MM/DD/YYYY): ");
					returnDate = input.nextLine();
					String[] split = returnDate.split("/");
					if (Integer.parseInt(split[0]) > 0
							&& Integer.parseInt(split[0]) < 13)
						if (Integer.parseInt(split[1]) > 0
								&& Integer.parseInt(split[1]) < 32)
							if (Integer.parseInt(split[2]) > 2012
									&& Integer.parseInt(split[2]) < 2020)
								success = true;
					if (!success)
						System.out.println("Please remember to format your date as such MM/DD/YYYY");
					
				} while (!success);
				
				//Now we are going to confirm reservation of return trip
				do {
					
					showAvailableSeats(deptCity1, arrCity1, departureDate);
					trip2 = showPricingInfo(deptCity1, arrCity1, isRoundTrip);
					System.out
							.println("Please enter the flight number(s)" +
									" you would like to choose (separated by spaces): ");
					flightNo2 = input.nextLine();
					String[] split = flightNo2.split(" ");
					System.out.println("You selected flight(s) ");
					for (int i = 0; i < split.length; i++) {
						System.out.println(split[i]);
					}
					System.out
							.println("Please confirm this reservation [yes/no]: ");
					userChoice = input.nextLine();
					if(userChoice.equalsIgnoreCase("yes")){
						String sqlFirstTrip = "insert into reservation_detail(reservation_number, " +
								"flight_number, flight_date, leg) " +
								"values(?,?,to_date(?,'MM/DD/YYYY'),?)"; 
								
						for (int i = 0; i < split.length; i++) {
							try{
								statement = connection.prepareStatement(sqlFirstTrip);
								statement.setString(1, res_num);
								statement.setString(2, split[i]);
								statement.setString(3, returnDate);
								statement.setInt(4, legCount++);
								statement.executeUpdate();
								System.out.println("Leg reserved!");
								legReserved = true;
							} catch(SQLException e){
								handleSQLException(sqlFirstTrip, e);
							}
						}
								
					}
				} while (!legReserved);
				
				//Now one last chore. Update the original reservation with the cost
				
				sqlInsertResInfo = "update reservation set cost = ? where reservation_number = ?";
				
				try {
					statement = connection.prepareStatement(sqlInsertResInfo);

		            statement.setInt(1,trip1 + trip2);
		            System.out.println("trip1 = " + trip1 + " trip2 = " + trip2);
		            statement.setString(2, res_num);
		            System.out.println(res_num);
		            statement.executeUpdate();
		            
			        System.out.println("Sucessfully updated reservation.");
			        
				} catch (SQLException e) {
					handleSQLException(sqlInsertResInfo, e);
				}
				
				completed = true;
			}else if(userChoice.equalsIgnoreCase("no")){
				/*
				 * One way flight to destination city. It could be multiple legs.
				 * Need to get destination city, arrival city, and date
				 */
				System.out.println("The departing city of the first trip: ");
				deptCity = input.nextLine();
				System.out.println("The destination city of the first trip: ");
				arrCity = input.nextLine();
				do {
					System.out.println("The flight date (MM/DD/YYYY): ");
					departureDate = input.nextLine();
					String[] split = departureDate.split("/");
					if (Integer.parseInt(split[0]) > 0
							&& Integer.parseInt(split[0]) < 13)
						if (Integer.parseInt(split[1]) > 0
								&& Integer.parseInt(split[1]) < 32)
							if (Integer.parseInt(split[2]) > 2012
									&& Integer.parseInt(split[2]) < 2020)
								success = true;
					if (!success)
						System.out.println("Please remember to format your date as such MM/DD/YYYY");
				} while (!success);
				
				//Now we are going to confirm reservation of outgoing trip
				do {
					
					showAvailableSeats(deptCity, arrCity, departureDate);
					trip1 = showPricingInfo(deptCity, arrCity, isRoundTrip);
					System.out
							.println("Please enter the flight number(s)" +
									" you would like to choose (separated by spaces): ");
					flightNo1 = input.nextLine();
					String[] split = flightNo1.split(" ");
					System.out.println("You selected flight(s) ");
					for (int i = 0; i < split.length; i++) {
						System.out.println(split[i]);
					}
					System.out
							.println("Please confirm this reservation [yes/no]: ");
					userChoice = input.nextLine();
					if(userChoice.equalsIgnoreCase("yes")){
						String sqlFirstTrip = "insert into reservation_detail(reservation_number, " +
								"flight_number, flight_date, leg) " +
								"values(?,?,to_date(?,'MM/DD/YYYY'),?)"; 
								
						for (int i = 0; i < split.length; i++) {
							try{
								statement = connection.prepareStatement(sqlFirstTrip);
								statement.setString(1, res_num);
								statement.setString(2, split[i]);
								statement.setString(3, departureDate);
								statement.setInt(4, i+1);
								statement.executeUpdate();
								System.out.println("Leg reserved!");
								legReserved = true;
							} catch(SQLException e){
								handleSQLException(sqlFirstTrip, e);
							}
						}
								
					}
				} while (!legReserved);
				
				//Now one last chore. Update the original reservation with the cost
				
				sqlInsertResInfo = "update reservation set cost = ? where reservation_number = ?";
				
				try {
					statement = connection.prepareStatement(sqlInsertResInfo);

		            statement.setInt(1,trip1);
		            //System.out.println(trip1);
		            statement.setString(2, res_num);
		            //System.out.println(res_num);
		            statement.executeUpdate();
		            
			        System.out.println("Sucessfully updated reservation.");
			        
				} catch (SQLException e) {
					handleSQLException(sqlInsertResInfo, e);
				}
				
				completed = true;
				
			}else{
				System.out.println("Please enter \"yes\" or \"no\" as input.");
			}
		} while (!completed);
	}

	private int showPricingInfo(String deptCity, String arrCity, boolean isRoundTrip) {
		int Cost = 0;
		//Leaving from the original city
    	PreparedStatement statement = null;
    	ResultSet resultSet = null;
	    String sqlPricingInfo = "SELECT high_price, low_price " +
	    		"from price " +
	    		"where departure_city = ? and arrival_city = ?";  

        try {
        	
			statement = connection.prepareStatement(sqlPricingInfo);
			statement.setString(1, deptCity);
			statement.setString(2, arrCity);
			resultSet = statement.executeQuery();
			
			if (resultSet.next()){
				
				if (isRoundTrip) {
					System.out.println("Cost from " + deptCity + " to "
							+ arrCity + " is $" + resultSet.getString(2));
					Cost = Integer.parseInt(resultSet.getString(2));
				}else{
					System.out.println("Cost from " + deptCity + " to "
							+ arrCity + " is $" + resultSet.getString(1));
					Cost = Integer.parseInt(resultSet.getString(1));
				}
			}		        	
	        
		} catch (SQLException e) {
			handleSQLException(sqlPricingInfo, e);
		} catch (Exception e){
			System.out.println(e.getMessage());
		}
        return Cost;
	}

	private void showAvailableSeats(String deptCity, String arrCity,
			String possibleDate) {

		//One-way flights
    	PreparedStatement statement = null;
    	ResultSet resultSet = null;
	    String sqlAvailableOne = "SELECT FLIGHT_NUMBER, DEPARTURE_TIME, ARRIVAL_TIME " +
	    		"FROM flight " +
	    		"WHERE DEPARTURE_CITY=? AND ARRIVAL_CITY=? AND " + 
	    		"fun_current_cap(FLIGHT_NUMBER, to_date(?,'MM/DD/YYYY')) < " + 
	    		"(select plane_capacity from plane where flight.plane_type = plane_type)";
	    
	    String sqlAvailableTwo = "SELECT FLIGHT_NUMBER, DEPARTURE_TIME, ARRIVAL_TIME " +
	    		"FROM flight " +
	    		"WHERE DEPARTURE_CITY=? AND ARRIVAL_CITY=? AND " + 
	    		"check_flight_days(weekly_schedule, format_schedule(to_char(to_date(?," +
	    		"'MM/DD/YYYY'), 'DAY')))=1";
	    
	    
	    //Multi-leg flights
    	PreparedStatement statement1 = null;
    	ResultSet resultSet1 = null;
	    String sqlAvailable1 = 
	    		"SELECT S1.FLIGHT_NUMBER, S1.ARRIVAL_CITY, S1.DEPARTURE_TIME, S1.ARRIVAL_TIME, " + 
	    		"S2.FLIGHT_NUMBER, S2.DEPARTURE_TIME, S2.ARRIVAL_TIME " +
	    		"from (select * from flight where departure_city = ? AND " + 
	    		
	    		"fun_current_cap(FLIGHT_NUMBER, to_date(?,'MM/DD/YYYY')) < " + 
	    		"(select plane_capacity from plane where flight.plane_type = plane_type)) S1, " + 
	    		"(select * from flight where arrival_city = ? AND " + 
	    		
	    		"fun_current_cap(FLIGHT_NUMBER, to_date(?,'MM/DD/YYYY')) < " + 
	    		"(select plane_capacity from plane where flight.plane_type = plane_type)) S2 " +
	    		"where S1.arrival_city = S2.departure_city";

	    String sqlAvailable2 = 
	    		"SELECT S1.FLIGHT_NUMBER, S1.ARRIVAL_CITY, S1.DEPARTURE_TIME, S1.ARRIVAL_TIME, " + 
	    		"S2.FLIGHT_NUMBER, S2.DEPARTURE_TIME, S2.ARRIVAL_TIME " +
	    		"from (select * from flight where departure_city = ? AND " + 
	    		
				"check_flight_days(weekly_schedule, format_schedule(to_char(to_date(?," +
				"'MM/DD/YYYY'), 'DAY')))=1) S1, " + 
	    		"(select * from flight where arrival_city = ? AND " + 
	    		
				"check_flight_days(weekly_schedule, format_schedule(to_char(to_date(?," +
				"'MM/DD/YYYY'), 'DAY')))=1) S2 " +
	    		"where S1.arrival_city = S2.departure_city";

	    try {
        	
			statement = connection.prepareStatement(sqlAvailableOne);
			statement.setString(1, deptCity);
			statement.setString(2, arrCity);
			statement.setString(3, possibleDate);
			resultSet = statement.executeQuery();
			

			
			if (resultSet.next()==false) {
				statement = connection.prepareStatement(sqlAvailableTwo);
				statement.setString(1, deptCity);
				statement.setString(2, arrCity);
				statement.setString(3, possibleDate);
				resultSet = statement.executeQuery();
				while (resultSet.next()) {
					System.out.println("One-way flight "
							+ resultSet.getString(1) + " from " + deptCity
							+ " to " + arrCity + " departs at "
							+ resultSet.getString(2) + " and arrives at "
							+ resultSet.getString(3));
				}
			}else{
				do{
					System.out.println("One-way flight "
							+ resultSet.getString(1) + " from " + deptCity
							+ " to " + arrCity + " departs at "
							+ resultSet.getString(2) + " and arrives at "
							+ resultSet.getString(3));
				}while (resultSet.next());
			}
			
		} catch (SQLException e) {
			handleSQLException(sqlAvailableTwo, e);
			handleSQLException(sqlAvailableOne, e);
			System.out.println("Something went wrong!");
		}
        
        try{
        	
			statement1 = connection.prepareStatement(sqlAvailable1);
			statement1.setString(1, deptCity);
			statement1.setString(2, possibleDate);
			statement1.setString(3, arrCity);
			statement1.setString(4, possibleDate);
			resultSet1 = statement1.executeQuery();

        	if (resultSet1.next()==false) {
        		statement1 = connection.prepareStatement(sqlAvailable2);
    			statement1.setString(1, deptCity);
    			statement1.setString(2, possibleDate);
    			statement1.setString(3, arrCity);
    			statement1.setString(4, possibleDate);
    			resultSet1 = statement1.executeQuery();
    			
				while (resultSet1.next()) {
					System.out.println("Multi-leg flight\nFlight one "
							+ resultSet1.getString(1) + " from " + deptCity
							+ " to " + resultSet1.getString(2) + " departs at "
							+ resultSet1.getString(3) + " and arrives at "
							+ resultSet1.getString(4) + "\nFlight two "
							+ resultSet1.getString(5)
							+ " connecting flight from "
							+ resultSet1.getString(2) + " to " + arrCity
							+ " departs at " + resultSet1.getString(6)
							+ " and arrives at " + resultSet1.getString(7));
				}
			}else{
				do {
					System.out.println("Multi-leg flight\nFlight one "
							+ resultSet1.getString(1) + " from " + deptCity
							+ " to " + resultSet1.getString(2) + " departs at "
							+ resultSet1.getString(3) + " and arrives at "
							+ resultSet1.getString(4) + "\nFlight two "
							+ resultSet1.getString(5)
							+ " connecting flight from "
							+ resultSet1.getString(2) + " to " + arrCity
							+ " departs at " + resultSet1.getString(6)
							+ " and arrives at " + resultSet1.getString(7));
				}while (resultSet1.next());
			}
        	
        }catch(SQLException e){
        	handleSQLException(sqlAvailable2, e);
        	handleSQLException(sqlAvailable1, e);
        }
	}


	public void showAvailableSeats() {
		
		String deptCity = null;
		String arrivalCity = null;
		String possibleDate = null;
		
		System.out.println("Please enter the following information at each prompt.");
		System.out.println("Departing city: ");
		deptCity = input.nextLine();
		System.out.println("Arrival city: ");
		arrivalCity = input.nextLine();
		boolean success = false;
		do{
			
			System.out.println("Date of departure (MM/DD/YYYY): ");
			possibleDate = input.nextLine();
			String[] split = possibleDate.split("/");
			if(Integer.parseInt(split[0]) > 0 && Integer.parseInt(split[0]) < 13)
				if(Integer.parseInt(split[1]) > 0 && Integer.parseInt(split[1]) < 32)
					if(Integer.parseInt(split[2]) > 2012 && Integer.parseInt(split[2]) < 2020)
						success = true;
			if(!success)
				System.out.println("Please remember to format your date as such MM/DD/YYYY");
			
		}while(!success);
		
		//One-way flights
    	PreparedStatement statement = null;
    	ResultSet resultSet = null;
	    String sqlAvailableOne = "SELECT FLIGHT_NUMBER, DEPARTURE_TIME, ARRIVAL_TIME " +
	    		"FROM flight " +
	    		"WHERE DEPARTURE_CITY=? AND ARRIVAL_CITY=? AND " + 
	    		"fun_current_cap(FLIGHT_NUMBER, to_date(?,'MM/DD/YYYY')) < " + 
	    		"(select plane_capacity from plane where flight.plane_type = plane_type)";
	    
	    String sqlAvailableTwo = "SELECT FLIGHT_NUMBER, DEPARTURE_TIME, ARRIVAL_TIME " +
	    		"FROM flight " +
	    		"WHERE DEPARTURE_CITY=? AND ARRIVAL_CITY=? AND " + 
	    		"check_flight_days(weekly_schedule, format_schedule(to_char(to_date(?," +
	    		"'MM/DD/YYYY'), 'DAY')))=1";
	    
	    
	    //Multi-leg flights
    	PreparedStatement statement1 = null;
    	ResultSet resultSet1 = null;
	    String sqlAvailable1 = 
	    		"SELECT S1.FLIGHT_NUMBER, S1.ARRIVAL_CITY, S1.DEPARTURE_TIME, S1.ARRIVAL_TIME, " + 
	    		"S2.FLIGHT_NUMBER, S2.DEPARTURE_TIME, S2.ARRIVAL_TIME " +
	    		"from (select * from flight where departure_city = ? AND " + 
	    		
	    		"fun_current_cap(FLIGHT_NUMBER, to_date(?,'MM/DD/YYYY')) < " + 
	    		"(select plane_capacity from plane where flight.plane_type = plane_type)) S1, " + 
	    		"(select * from flight where arrival_city = ? AND " + 
	    		
	    		"fun_current_cap(FLIGHT_NUMBER, to_date(?,'MM/DD/YYYY')) < " + 
	    		"(select plane_capacity from plane where flight.plane_type = plane_type)) S2 " +
	    		"where S1.arrival_city = S2.departure_city";

	    String sqlAvailable2 = 
	    		"SELECT S1.FLIGHT_NUMBER, S1.ARRIVAL_CITY, S1.DEPARTURE_TIME, S1.ARRIVAL_TIME, " + 
	    		"S2.FLIGHT_NUMBER, S2.DEPARTURE_TIME, S2.ARRIVAL_TIME " +
	    		"from (select * from flight where departure_city = ? AND " + 
	    		
				"check_flight_days(weekly_schedule, format_schedule(to_char(to_date(?," +
				"'MM/DD/YYYY'), 'DAY')))=1) S1, " + 
	    		"(select * from flight where arrival_city = ? AND " + 
	    		
				"check_flight_days(weekly_schedule, format_schedule(to_char(to_date(?," +
				"'MM/DD/YYYY'), 'DAY')))=1) S2 " +
	    		"where S1.arrival_city = S2.departure_city";

	    try {
        	
			statement = connection.prepareStatement(sqlAvailableOne);
			statement.setString(1, deptCity);
			statement.setString(2, arrivalCity);
			statement.setString(3, possibleDate);
			resultSet = statement.executeQuery();
			

			
			if (resultSet.next()==false) {
				statement = connection.prepareStatement(sqlAvailableTwo);
				statement.setString(1, deptCity);
				statement.setString(2, arrivalCity);
				statement.setString(3, possibleDate);
				resultSet = statement.executeQuery();
				while (resultSet.next()) {
					System.out.println("One-way flight "
							+ resultSet.getString(1) + " from " + deptCity
							+ " to " + arrivalCity + " departs at "
							+ resultSet.getString(2) + " and arrives at "
							+ resultSet.getString(3));
				}
			}else{
				do{
					System.out.println("One-way flight "
							+ resultSet.getString(1) + " from " + deptCity
							+ " to " + arrivalCity + " departs at "
							+ resultSet.getString(2) + " and arrives at "
							+ resultSet.getString(3));
				}while (resultSet.next());
			}
			
		} catch (SQLException e) {
			handleSQLException(sqlAvailableTwo, e);
			handleSQLException(sqlAvailableOne, e);
			System.out.println("Something went wrong!");
		}
        
        try{
        	
			statement1 = connection.prepareStatement(sqlAvailable1);
			statement1.setString(1, deptCity);
			statement1.setString(2, possibleDate);
			statement1.setString(3, arrivalCity);
			statement1.setString(4, possibleDate);
			resultSet1 = statement1.executeQuery();

        	if (resultSet1.next()==false) {
        		statement1 = connection.prepareStatement(sqlAvailable2);
    			statement1.setString(1, deptCity);
    			statement1.setString(2, possibleDate);
    			statement1.setString(3, arrivalCity);
    			statement1.setString(4, possibleDate);
    			resultSet1 = statement1.executeQuery();
    			
				while (resultSet1.next()) {
					System.out.println("Multi-leg flight\nFlight one "
							+ resultSet1.getString(1) + " from " + deptCity
							+ " to " + resultSet1.getString(2) + " departs at "
							+ resultSet1.getString(3) + " and arrives at "
							+ resultSet1.getString(4) + "\nFlight two "
							+ resultSet1.getString(5)
							+ " connecting flight from "
							+ resultSet1.getString(2) + " to " + arrivalCity
							+ " departs at " + resultSet1.getString(6)
							+ " and arrives at " + resultSet1.getString(7));
				}
			}else{
				do {
					System.out.println("Multi-leg flight\nFlight one "
							+ resultSet1.getString(1) + " from " + deptCity
							+ " to " + resultSet1.getString(2) + " departs at "
							+ resultSet1.getString(3) + " and arrives at "
							+ resultSet1.getString(4) + "\nFlight two "
							+ resultSet1.getString(5)
							+ " connecting flight from "
							+ resultSet1.getString(2) + " to " + arrivalCity
							+ " departs at " + resultSet1.getString(6)
							+ " and arrives at " + resultSet1.getString(7));
				}while (resultSet1.next());
			}
        	
        }catch(SQLException e){
        	handleSQLException(sqlAvailable1, e);
        }
	}

	public void showRoutes() {
		
		String deptCity = null;
		String arrivalCity = null;
		
		System.out.println("Please enter the following information at each prompt.");
		System.out.println("Departing city: ");
		deptCity = input.nextLine();
		System.out.println("Arrival city: ");
		arrivalCity = input.nextLine();
		
		//One-way flights
    	PreparedStatement statement = null;
    	ResultSet resultSet = null;
	    String sqlRouteInfo = "SELECT FLIGHT_NUMBER, DEPARTURE_TIME, ARRIVAL_TIME " +
	    		"FROM flight " +
	    		"WHERE DEPARTURE_CITY=? AND ARRIVAL_CITY=?";

	    //Multi-leg flights
    	PreparedStatement statement1 = null;
    	ResultSet resultSet1 = null;
	    String sqlRouteInfo1 = 
	    		"SELECT S1.FLIGHT_NUMBER, S1.ARRIVAL_CITY, S1.DEPARTURE_TIME, S1.ARRIVAL_TIME, " + 
	    		"S2.FLIGHT_NUMBER, S2.DEPARTURE_TIME, S2.ARRIVAL_TIME " +
	    		"from (select * from flight where departure_city=?) S1, " + 
	    		"(select * from flight where arrival_city=?) S2 " +
	    		"where S1.arrival_city = S2.departure_city AND " + 
	    		"check_flight_days(S1.weekly_schedule, S2.weekly_schedule)=1";
	    
        try {
        	
			statement = connection.prepareStatement(sqlRouteInfo);
			statement.setString(1, deptCity);
			statement.setString(2, arrivalCity);
			resultSet = statement.executeQuery();
			
			statement1 = connection.prepareStatement(sqlRouteInfo1);
			statement1.setString(1, deptCity);
			statement1.setString(2, arrivalCity);
			resultSet1 = statement1.executeQuery();
			
			while(resultSet.next()){
			System.out.println("One-way flight " + resultSet.getString(1) + " from " + deptCity + 
					" to " + arrivalCity + " departs at " + resultSet.getString(2) + 
					" and arrives at " + resultSet.getString(3));
			}
			
			while(resultSet1.next()){
				System.out.println("Multi-leg flight\nFlight "
						+ resultSet1.getString(1) + " from " + deptCity
						+ " to " + resultSet1.getString(2) + " departs at "
						+ resultSet1.getString(3) + " and arrives at "
						+ resultSet1.getString(4) + "\nConnecting flight "
						+ resultSet1.getString(5) + " from "
						+ resultSet1.getString(2) + " to " + arrivalCity
						+ " departs at " + resultSet1.getString(6)
						+ " and arrives at " + resultSet1.getString(7));
			}
			
		} catch (SQLException e) {
			handleSQLException(sqlRouteInfo, e);
		} 
	}

	public void showPricingInfo() {
		String deptCity = null;
		String arrivalCity = null;
		int roundtrip = 0;
		
		//Prompt user for input
		System.out.println("Please enter the following information at each prompt.");
		System.out.println("Departing city: ");
		deptCity = input.nextLine();
		System.out.println("Arrival city: ");
		arrivalCity = input.nextLine();
		
		//Leaving from the original city
    	PreparedStatement statement = null;
    	ResultSet resultSet = null;
	    String sqlPricingInfo = "SELECT high_price, low_price " +
	    		"from price " +
	    		"where departure_city = ? and arrival_city = ?"; 
	    
	    //Now for the opposite direction
    	PreparedStatement statement1 = null;
    	ResultSet resultSet1 = null;
	    String sqlPricingInfo1 = "SELECT high_price, low_price " +
	    		"from price " +
	    		"where departure_city = ? and arrival_city = ?"; 

        try {
        	
			statement = connection.prepareStatement(sqlPricingInfo);
			statement.setString(1, deptCity);
			statement.setString(2, arrivalCity);
			resultSet = statement.executeQuery();
			statement1 = connection.prepareStatement(sqlPricingInfo1);
			statement1.setString(1, arrivalCity);
			statement1.setString(2, deptCity);
			resultSet1 = statement1.executeQuery();
			
			while(resultSet.next()){
				System.out.println("One-way from " + deptCity + " to " + 
						arrivalCity + " is $" + resultSet.getString(1));
				
				roundtrip = Integer.parseInt(resultSet.getString(2));			        	
			    if (resultSet1.next()) {
					roundtrip += Integer.parseInt(resultSet1.getString(2));
					System.out.println("Round trip from " + deptCity + " to "
							+ arrivalCity + " is $" + roundtrip);
				}
				roundtrip = 0;
		      }
	        
		} catch (SQLException e) {
			handleSQLException(sqlPricingInfo, e);
		} 
		
	}

	public void showCustomerInfo() {
		String firstName = null;
		String lastName = null;
		
		System.out.println("Please enter the following information at each prompt.");
		System.out.println("Your first name: ");
		firstName = input.nextLine();
		System.out.println("Your last name: ");
		lastName = input.nextLine();
		
    	PreparedStatement statement = null;
    	ResultSet resultSet = null;
	    String sqlCustInfo = "SELECT cid, first_name, last_name, street, " +
	    		"city, state, phone, email " +
	    		"from customer " +
	    		"where first_name = ? and last_name = ?"; 

        try {
        	
			statement = connection.prepareStatement(sqlCustInfo);
			statement.setString(1, firstName);
			statement.setString(2, lastName);
			resultSet = statement.executeQuery();
			while(resultSet.next()){
	        	System.out.println(resultSet.getString(1) + " " + 
	                    resultSet.getString(2) + " " +  
	                    resultSet.getString(3) + " " + 
	                    resultSet.getString(4) + " " + 
	                    resultSet.getString(5) + " " + 
	                    resultSet.getString(6) + " " + 
	                    resultSet.getString(7) + " " + 
	                    resultSet.getString(8)); 
			}
		} catch (SQLException e) {
			handleSQLException(sqlCustInfo, e);
		} 
		
	}

	public void makeNewCustomer() {
		String salutation = null;
		String firstName = null;
		String lastName = null;
		String creditCardNum = null;
		String creditCardExpire = null;
		String street = null;
		String state = null;
		String city	= null;
		String phone = null;
		String email = null;
		boolean success = false;
		PreparedStatement statement = null;
		String sqlInsertCustInfo = "insert into customer(salutation, first_name, " +
		"last_name, credit_card_num, credit_card_expire, street, state, city, " +
				"phone, email) " +
				"values(?,?,?,?,to_date(?,'MM/DD/YYYY'),?,?,?,?,?)";
		
		System.out.println("Please enter the following information at each prompt.");
		System.out.println("Your salutation: ");
		salutation = input.nextLine();
		System.out.println("Your first name: ");
		firstName = input.nextLine();
		System.out.println("Your last name: ");
		lastName = input.nextLine();
		System.out.println("Your credit card number: ");
		creditCardNum = input.nextLine();
		
		do{
				
			System.out.println("Your credit card expiration date (MM/DD/YYYY): ");
			creditCardExpire = input.nextLine();
			String[] split = creditCardExpire.split("/");
			if(Integer.parseInt(split[0]) > 0 && Integer.parseInt(split[0]) < 13)
				if(Integer.parseInt(split[1]) > 0 && Integer.parseInt(split[1]) < 32)
					if(Integer.parseInt(split[2]) > 2013 && Integer.parseInt(split[2]) < 2020)
						success = true;
			if(!success)
				System.out.println("Please remember to format your date as such MM/DD/YYYY");
			
		}while(!success);
		
		System.out.println("Your street: ");
		street = input.nextLine();
		System.out.println("Your state: ");
		state = input.nextLine();
		System.out.println("Your city: ");
		city = input.nextLine();
		System.out.println("Your phone: ");
		phone = input.nextLine();
		System.out.println("Your email: ");
		email = input.nextLine();
		
		
		try {
			statement = connection.prepareStatement(sqlInsertCustInfo);

            statement.setString(1,salutation);
            statement.setString(2,firstName); 
            statement.setString(3,lastName); 
            statement.setString(4,creditCardNum); 
            statement.setString(5,creditCardExpire); 
            statement.setString(6,street); 
            statement.setString(7,state); 
            statement.setString(8, city);
            statement.setString(9, phone);
            statement.setString(10, email);
            
            statement.executeUpdate();

	        System.out.println("Sucessfully created a new customer account.");
		} catch (SQLException e) {
			handleSQLException(sqlInsertCustInfo, e);
		}
	}

	/*
     *  login: 1. admin 2. user 
     *  @param type 1. admin 2. user
     */
    public boolean login(int type){
        System.out.println("\nPlease enter your login information.");
        username = getUserInput("Username", true);
        password = getUserInput("Password", true);
        if (type == 1){
        	if ((username.equals(adminUsername)) && (password.equals(adminPassword))){
        		return true;
        	}
        }else {
        	if ((username.equals(userUsername)) && (password.equals(userPassword))){
        		return true;
        	}
        }
        return false;
    }
    
    /*
     * get user numeric choice
     * @param menuList, prompt message, whether user input is required or not
     * @return user choice integer
     */
    public int getUserNumericMenuChoice(String menuTile, List<String> menuList, String prompt, boolean required){
    	// output menu
    	System.out.println("------------" + menuTile + "------------");
    	for(String menu : menuList){
    		System.out.println(menu);
    	}
    	// get numeric user choice
    	int numericChoice = getUserNumericInput(prompt, required);
    	return numericChoice;
    }
    
    /*
     * get user input 
     * @param prompt message, whether user input is required or not
     * return user input string
     */
    public String getUserInput(String prompt, boolean required){
    	String inputString;
    	
    	do {
    		
    		System.out.println(prompt + ":");
        	inputString = input.nextLine();
        	
    	} while (required && inputString.isEmpty());
    	
    	return inputString;
    }
    
    /*
     * get user numeric input
     * @param prompt message, whether user input is required or not
     * @return user input num
     */
    public int getUserNumericInput(String prompt, boolean required){
    	int inputNum;
    	
    	String inputString = getUserInput(prompt, required);
    	inputNum = Integer.parseInt(inputString);
    	
    	return inputNum;
    }
    
    
    /*
     * check whether the reservation number exits or not
     * @param reservationNum
     * @return true or false
     */
    public boolean checkReservationNum(String reservationNum){
    	Statement statement = null;
    	ResultSet resultSet = null;
    	ArrayList<String> allReservationNum = new ArrayList<String>();
    	String sqlReservationNum = "select reservation_number from reservation";
    	try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlReservationNum);
			while(resultSet.next()){
				allReservationNum.add(resultSet.getString(1));
			}
			if (allReservationNum.contains(reservationNum)){
				return true;
			}
			
			return false;
		} catch (SQLException e) {
			handleSQLException(sqlReservationNum , e);
			return false;
		}
		
    }
    
    /*
     * customer tasks: 7.showReservationInfo 8.buyTicket
     * 
     */
    /*
     * Task7. show reservation info 
     * @param reservationNum supplied by user
     * @return reservation is ticketed in reservation table
     */
    public void showReservationInfo(String reservationNum){
    	if(!checkReservationNum(reservationNum)){
    		System.out.println("Reservation number does not exist. Returning to customer interface.");
    		promptMenu(1);
    	} else {
        	PreparedStatement statement = null;
        	ResultSet resultSet = null;
        	String sqlShowReservationInfo = "select flight_number " +
        			"from reservation_detail " +
        			"where reservation_number = ?";
        	try {
    			statement = connection.prepareStatement(sqlShowReservationInfo);
    			statement.setString(1, reservationNum);
    			resultSet = statement.executeQuery();
    	    	System.out.println("Information for the reservation " + reservationNum + " is as follows:");   	
    	    	while(resultSet.next()){
    	    		System.out.println(resultSet.getString(1));
    	    	}
    		} catch (SQLException e) {
    			handleSQLException(sqlShowReservationInfo , e);
    		}
    	}
    }
    
    /*
     * Task8. buy ticket from existing reservation
     * @param reservationNum supplied by user
     * @return reservation is ticketed in reservation table
     */
    public void buyTicket(String reservationNum){
    	if(!checkReservationNum(reservationNum)){
    		System.out.println("Reservation number not exits. Returning to customer interface.");
    		promptMenu(1);
    	} else {
        	PreparedStatement statement = null;
        	int rowUpdated = 0;
        	String sqlBuyTicket = "update reservation " +
    				"set ticketed = 'Y' " +
    				"where reservation_number = ?";
        	try {
    			statement = connection.prepareStatement(sqlBuyTicket);
    			statement.setString(1, reservationNum);
    			rowUpdated = statement.executeUpdate();
    		} catch (SQLException e) {
    			handleSQLException(sqlBuyTicket, e);
    		}
        	
        	System.out.println(rowUpdated + " Reservation has been ticketed!");
    	}
    }
    
    /* 
     * administrator tasks: 1.eraseDB 2.loadScheduleInfo 3.loadPricingInfo/changePriceInfo
     * 4.loadPlaneInfo 5.generatePassengerManifest 
     */
    
    /*
     * Task1. Erase the database, to make the task simple,
     * make all table foreign keys "on delete cascade". 
     * Cannot handle materialized views. 
     */
    public void eraseDatabase(){
    	Statement statement = null;
    	ResultSet resultSet = null;
    	String sqlEraseDatabase = "select 'DELETE FROM '||table_name||''" +
    			"from user_tables" ;
		String strLine = null;
    	try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlEraseDatabase);
			printToFile(resultSet,"eraseDatabase.sql");			
			// Open the SQL file 
			FileInputStream fstream = new FileInputStream("eraseDatabase.sql");
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
			// Read File Line By Line 
			while ((strLine = br.readLine()) != null){
				statement.executeUpdate(strLine);				
			}
			// Close the input stream
			br.close();
			System.out.println("Successfully erased database.");
		} catch (SQLException e) {
			handleSQLException(strLine,e);
		} catch (IOException e) {
			handleIOException(e);
		}
    }
    
    /* 
     * Task2 Load schedule information
     * @param Filename supplied by user
     * @Ouput Number of rows loaded 
     */
    public void loadScheduleInformation(String fileName){
        PreparedStatement statement = null;	
		String sqlInsertFlightInfo = "insert into flight(flight_number, plane_type, departure_city, " +
				"arrival_city, departure_time, arrival_time, weekly_schedule) " +
				"values(?,?,?,?,?,?,?)";
		try {
			statement = connection.prepareStatement(sqlInsertFlightInfo);
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	        String flightInfoLine=null;
	        int rowsInserted = 0;
	        while((flightInfoLine = br.readLine())!=null){ 
	                String[] flightInfoArray=flightInfoLine.split(" "); 
	                statement.setString(1,flightInfoArray[0]);
	                statement.setString(2,flightInfoArray[1]); 
	                statement.setString(3,flightInfoArray[2]); 
	                statement.setString(4,flightInfoArray[3]); 
	                statement.setString(5,flightInfoArray[4]); 
	                statement.setString(6,flightInfoArray[5]); 
	                statement.setString(7,flightInfoArray[6]); 
	                rowsInserted += statement.executeUpdate();
	         }	
	        System.out.println("Sucessfully inserted "+ rowsInserted + " rows into table flight.");
		} catch (IOException e) {
			handleIOException(e);
		} catch (SQLException e) {
			handleSQLException(sqlInsertFlightInfo, e);
		}
    }
    
    /* Task3 Part1 Load price information
     * @param Filename supplied by user
     * @Ouput Number of rows loaded*/
    public void loadPriceInformation(String fileName){
        PreparedStatement statement = null;	
		String sqlInsertPriceInfo = "insert into price(departure_city, " +
				"arrival_city, high_price, low_price) " +
				"values(?,?,?,?)";
		try {
			statement = connection.prepareStatement(sqlInsertPriceInfo);
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	        String priceInfoLine=null;
	        int rowsInserted = 0;
	        while((priceInfoLine = br.readLine())!=null){ 
	                String[] flightInfoArray=priceInfoLine.split(" "); 
	                statement.setString(1,flightInfoArray[0]);
	                statement.setString(2,flightInfoArray[1]); 
	                statement.setString(3,flightInfoArray[2]); 
	                statement.setString(4,flightInfoArray[3]); 
	                rowsInserted += statement.executeUpdate();
	         }	
	        System.out.println("Sucessfully inserted "+ rowsInserted + " rows into table price.");
		} catch (IOException e) {
			handleIOException(e);
		} catch (SQLException e) {
			handleSQLException(sqlInsertPriceInfo, e);
		}
    }
    
    /* Task3 Part2 Change price information
     * @param departureCity, arrivalCity, highPrice, lowPrice supplied by user
     * @Ouput Rows have been changed. Assuming one row each time.*/
    public void changePriceInformation(String departureCity, String arrivalCity, int highPrice, int lowPrice){
        PreparedStatement statement = null;	
		String sqlChangePriceInfo = "update price " +
				"set high_price = ? , low_price = ?" +
				"where departure_city = ? and arrival_city = ? ";
		try {
			statement = connection.prepareStatement(sqlChangePriceInfo);
			statement.setInt(1, highPrice);
			statement.setInt(2, lowPrice);
			statement.setString(3, departureCity);
			statement.setString(4, arrivalCity);
			statement.executeUpdate();
	        System.out.println("Sucessfully changed prices from " + departureCity + " to " + arrivalCity);
		} catch (SQLException e) {
			handleSQLException(sqlChangePriceInfo, e);
		}
    }
    
    /* 
     * Task4 Load plane information
     * @param Filename supplied by user
     * @Ouput Number of rows loaded
     */
    public void loadPlaneInformation(String fileName){
        PreparedStatement statement = null;	
		String sqlInsertPlaneInfo = "insert into plane(plane_type, manufacture, " +
				"plane_capacity, last_service, year) " +
				"values(?,?,?,to_date(?, 'MM/DD/YYYY'),?)";
		try {
			statement = connection.prepareStatement(sqlInsertPlaneInfo);
			FileInputStream fstream = new FileInputStream(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(fstream));
	        String planeInfoLine=null;
	        int rowsInserted = 0;
	        while((planeInfoLine = br.readLine())!=null){ 
	                String[] flightInfoArray=planeInfoLine.split(" "); 
	                statement.setString(1,flightInfoArray[0]);
	                statement.setString(2,flightInfoArray[1]); 
	                statement.setString(3,flightInfoArray[2]); 
	                statement.setString(4,flightInfoArray[3]); 
	                statement.setString(5,flightInfoArray[4]); 
	                rowsInserted += statement.executeUpdate();
	         }	
	        System.out.println("Sucessfully inserted "+ rowsInserted + " rows into table plane.");
		} catch (IOException e) {
			handleIOException(e);
		} catch (SQLException e) {
			handleSQLException(sqlInsertPlaneInfo, e);
		}
    }
    
    /* 
     * Task5. Generate passenger manifest for specific flight on given day.
     * @param flightNum and the date
     * @Output Print the passenger list. 
     */
    public void generatePassengerManifest(String flightDate, String flightNum){
       	int counter=0 ;
    	if(!checkFlightNum(flightNum)){
    		System.out.println("Flight number does not exist. Returning to administration interface.");
    		promptMenu(2);
    	} else {
	    	PreparedStatement statement = null;
	    	ResultSet resultSet = null;
		    String sqlPassengerMainfest = "select c.salutation, c.first_name, c.last_name  " +
		    		"from reservation_detail rd, reservation r, customer c " +
		    		"where rd.reservation_number = r.reservation_number " +
		    		"and r.cid = c.cid " +
		    		"and rd.flight_number =? " +
		    		"and rd.flight_date = to_date( ? , 'DD/MM/YYYY')"; 
	
	        try {
	        	
				statement = connection.prepareStatement(sqlPassengerMainfest);
				statement.setString(1, flightNum);
				statement.setString(2, flightDate);
				resultSet = statement.executeQuery();
				
		        while(resultSet.next()) 
		        {
		        	System.out.println("Record " + counter + ": " +
		        			resultSet.getString(1) + " " + 
	                        resultSet.getString(2) + " " +   
	                        resultSet.getString(3)); 
		        	counter++;
		        }
		        
			} catch (SQLException e) {
				handleSQLException(sqlPassengerMainfest, e);
			} 
    	}
    }
    
    /*
    * check whether the flight number exits or not
    * @param reservationNum
    * @return true or false
    */
   public boolean checkFlightNum(String flightNum){
   	Statement statement = null;
   	ResultSet resultSet = null;
   	ArrayList<String> allFlightNum = new ArrayList<String>();
   	String sqlFlightNum = "select flight_number from flight";
   	try {
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlFlightNum);
			while(resultSet.next()){
				allFlightNum.add(resultSet.getString(1));
			}
			if (allFlightNum.contains(flightNum)){
				return true;
			}
			
			return false;

		} catch (SQLException e) {
			handleSQLException(sqlFlightNum , e);
			return false;
		}
		
   }
    
    // get sql script file
    public void printToFile( ResultSet rs, String path ){ 
		try {
			PrintStream out = new PrintStream(new FileOutputStream(path));
	        while( rs.next() ) { 
	        	out.printf("%s\n", rs.getObject(1) );
	        }
	        out.close();
	        rs.close();
		} catch (IOException e) {
			handleIOException(e);
		} catch (SQLException e) {
			handleSQLException("Erase database error.",e);
		}  
    }
    
    /* File not found exception handler */
    public void handleIOException(Exception e) {
    	System.err.println("Could not find specified file: " + e.toString());
        e.printStackTrace();
        System.exit(1);
        
    }
     
    /* SQL exception handler */
    public void handleSQLException(String query, Exception e) {
        System.err.println("Error running database query: " + query + e.toString());
        e.printStackTrace();
        System.exit(1);
    }
    
    public static void main(String args[])
    {
      team03 test = new team03();
    }
	
}
