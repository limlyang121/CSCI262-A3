import java.util.ArrayList;
import java.util.Scanner;
import java.io.File;
import java.io.FileWriter;



public class IDS{
    public static int eventsLine;
    public static int statsLine;

    public static void main(String[] args){
        //Check if parameter is 3, if not exit/return
        if (args.length != 3){
            System.out.println("Need 3 parameter");
            return;
        }

        String fileNameOne = args[0];
        String fileNameTwo = args[1];
        int totalDays = Integer.parseInt(args[2]);
        
        ArrayList<Events> myEvents = readEventFile(fileNameOne);
        boolean correctParameter = readStatFile(fileNameTwo, myEvents);

        //Check if the parameter in events txt and stst txt are the same
        if (!correctParameter){
            System.out.println("Events and Stats Parameter is different");
            return;
        }

        Scanner sc = new Scanner(System.in);


        //Activity Engine 

        //store the log information to the Arraylist
        ArrayList<String[][]> activityEngineLog =  generateLogs(myEvents,totalDays);

        //Analysis Engine
        displayLogInfo(activityEngineLog);
        System.out.println("Please enter to Continue");
        sc.nextLine();
        System.out.println("============================================================");
        
        //Create new Baseline with the Log Data
        newBaseLineData(activityEngineLog);
        
        
        boolean EndAlert = false;
        //This part is for alertEngine
        while (!EndAlert){
            System.out.println("============================================================");
            System.out.println("Enter 'Exit' to Exit Program");
            System.out.println("Please enter New Stats.file with Days");
            String userInput = sc.nextLine();

            if (userInput.equalsIgnoreCase("Exit")){
                EndAlert = true;
                continue;
            }
            //Split the user Input into two and if user input is not 2, then continue
            String[] splitInput = userInput.split(" ");
            if (splitInput.length != 2){
                System.out.println("Please Input Correctly");
                System.out.println("Exampe : 'Stats1.txt 100' ");
                continue;
            }

            
            int newDays = Integer.parseInt(splitInput[1]);
            correctParameter = readStatFile(splitInput[0], myEvents);

            if (!correctParameter){
                System.out.println("Different Parameter, Please Change the parameter");
                continue;
            }

            //Generate new Log with the new Stats File
            ArrayList<String[][]> tempData = generateLogs(myEvents, newDays);

            alertEngine(tempData, myEvents);
            
            System.out.println("Please enter to Continue");
            sc.nextLine();
            
        }
        sc.close();
    }

    //This Function is AlertEngine.if there is any anomality, it will display the date and current threshold of that day
    public static void alertEngine(ArrayList<String[][]> activityEngine, ArrayList<Events> myEvents){
        //Calculate the AlertValue first, since the value is the same though all days
        int maximumThreshold = 0 ;
        for (int i = 0 ; i < myEvents.size(); i++){
            maximumThreshold += myEvents.get(i).getWeight();
        }
        maximumThreshold *= 2;

        //Get the data from "Baseline.txt " and store to ArrayList
        ArrayList<Stats> baseLineData = GetBaseLineData("BaseLine.txt");
        boolean alerted = false;
        //loop activityEngine (which is the new Log Data)
        for (int i = 0 ; i < activityEngine.size(); i++){
            double anomalityContent[] = new double[activityEngine.get(i).length]; 
            //Loop though all events for each day
            for (int j = 0 ; j < activityEngine.get(i).length; j++){
                //Get the events value
                double myValue = Double.parseDouble(activityEngine.get(i)[j][1]) ;

                //Using the formula to get the value (minus by mean, and divide by std)
                //from the newbaseline arraylist
                myValue -= baseLineData.get(j).getMean();

                myValue /= baseLineData.get(j).getStd();

                //Times the weight
                myValue *= myEvents.get(j).getWeight();
                myValue = reduceToTwoDecimal(myValue);

                anomalityContent[j] = myValue;

            }
            double currentThreshold = 0;
            //Loop though all the final result and add then together
            for (int j = 0 ; j < anomalityContent.length; j++){
                currentThreshold += anomalityContent[j];
            }

            //If current threshold is larger than maximum threshold then dispaly alert
            if (currentThreshold >= maximumThreshold){
                System.out.println("============================================================");
                System.out.println("Anomalities Found on Days " + (i + 1));
                System.out.println("Maximum Threshold is " + maximumThreshold);
                System.out.println("Current Threshold is " + reduceToTwoDecimal(currentThreshold));
                alerted = true;
            }
        }
        if (!alerted)
            System.out.println("No Anomality found");

        System.out.println("============================================================");

    }

    //This function use to read data from txt and grab the store to arraylist
    public static ArrayList<Stats> GetBaseLineData(String fileName){
        ArrayList<Stats> myTempStats = new ArrayList<Stats>();
        File myFile = new File(fileName);
        try(Scanner sc = new Scanner(myFile)){
            String temp;
            while (sc.hasNextLine()){
                temp = sc.nextLine();
                String[] splitTemp = temp.split(":");
                double mean = Double.parseDouble(splitTemp[1]);
                double std = Double.parseDouble(splitTemp[2]);
                Stats tempData = new Stats(mean, std);
                myTempStats.add(tempData);
                
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        
        
        return myTempStats;
    }

    //This function is use to read txt file (for Events) 
    public static ArrayList<Events> readEventFile(String fileName){
        ArrayList<Events> myEvents = new ArrayList<Events>();
        try{
            File myFile = new File(fileName);
            Scanner sc = new Scanner(myFile);
                
            String temp;
            while (sc.hasNextLine()){
                temp = sc.nextLine();
                String[] textSplit = temp.split(":");
                //This part is to check the total parameter
                if (textSplit.length == 1){
                    IDS.eventsLine = Integer.parseInt(textSplit[0]);
                    continue;
                }

                String name = textSplit[0];
                String type = textSplit[1];
                double min = Double.parseDouble(textSplit[2]);
                
                //Default value max value doesn't specified
                double max = 100;
                String maxString = textSplit[3];
                if (!maxString.isEmpty())
                    max = Double.parseDouble(textSplit[3]);
                double weight = Double.parseDouble(textSplit[4]);

                Events tempEvents = new Events(name, type.charAt(0), min, max, weight);
                myEvents.add(tempEvents);

            }
            sc.close();
            
        }catch (Exception e){
            e.printStackTrace();
        }
        
        return myEvents;
    }

    //This function is use to read txt file (for Stats) 
    public static boolean readStatFile (String fileName, ArrayList<Events> myEvents ){
        File myFile = new File(fileName);
        try(Scanner sc = new Scanner(myFile)){

            String temp;
            int currentLine = 0;
            while (sc.hasNextLine()) {
                temp = sc.nextLine();
                String[] textSplit = temp.split(":");
                //This part is to check if current line is parameter or not
                if (textSplit.length == 1){
                    IDS.statsLine = Integer.parseInt(textSplit[0]);
                    //If Events and Stats parameter is different, return false
                    if (IDS.statsLine != IDS. eventsLine)
                        return false;
                    continue;
                }

                double mean = Double.parseDouble(textSplit[1]);
                double std = Double.parseDouble(textSplit[2]);

                myEvents.get(currentLine).getStats().setMean(mean);
                myEvents.get(currentLine).getStats().setStd(std);
                currentLine++;

            }
            sc.close();
            
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
        return true;
    }

    //This function is use to generate log from ArrayList of events
    public static ArrayList<String[][]> generateLogs(ArrayList<Events> myEvents, int days){
        //Create new ArrayLsit of String double dimenstion array (To Store each day and each Event and it value)
        ArrayList<String[][]> MyEventLog = new ArrayList<String[][]>();
        for (int i = 0 ; i< days ; i++){
            int currentArray = 0;

            //New Multidimension array to store Day and it event and value
            String[][] tempStoreData = new String[IDS.eventsLine][];

            //Loop through ArraySize of Events parameter
            for (int j = 0 ; j < myEvents.size();j++){
                //New String array to store events and it value
                String tempStore[] = new String[2];

                
                tempStore[0] = myEvents.get(j).getName();
                //If current type is C, since C value need to be in double
                if (myEvents.get(j).getType() == 'C' ){
                    //Get random value from mean and std
                    double value = myEvents.get(j).getRandomize();
                    value = reduceToTwoDecimal(value);
                    tempStore[1] = String.valueOf(value) ;

                //If the type is D, set int value      
                }else if (myEvents.get(j).getType() == 'D'){
                    //Get Random value from mean and std
                    int value = (int) Math.round(myEvents.get(j).getRandomize());
                    
                    tempStore[1] = String.valueOf(value);
                }
                //Store the events and it value to tempStoreData (kinda like day)
                tempStoreData[currentArray] = tempStore;
                
                //Increase currentArray/Days by 1
                currentArray++;
            }
            
            MyEventLog.add(tempStoreData);

        }

        return MyEventLog;
    }

    //This function is use to create newBaseLine with the arraylist
    public static void newBaseLineData(ArrayList<String[][]> myEventsLog){
        System.out.println("Create New BaseLine based on ActivityEngine...");
        //Set the mean and std array with total event size
        double[] meanValue = new double[(myEventsLog.get(0).length)];
        double[] stdValue = new double[(myEventsLog.get(0).length)];
        //Loop though all event size
        for (int i = 0 ; i < myEventsLog.get(0).length;i++){
            //Loop though all days
            for (int j = 0 ; j < myEventsLog.size(); j ++)
                meanValue[i] += Double.parseDouble(myEventsLog.get(j)[i][1]) ;
            
            //Divide the mean by total days then reduce to two decimal points
            meanValue[i] /= myEventsLog.size();
            meanValue[i] = reduceToTwoDecimal(meanValue[i]);

            //Time to calculate the varience
            double myVarience = 0;
            for (int j = 0 ; j < myEventsLog.size() ; j++)
                myVarience += Math.pow((meanValue[i] -  Double.parseDouble(myEventsLog.get(j)[i][1])), 2) ;  

            //After add all the Varience, divide by total days 
            myVarience /= myEventsLog.size();

            //Std is sqrt root of varience
            stdValue[i] = Math.sqrt(myVarience);
            stdValue[i] = reduceToTwoDecimal(stdValue[i]);
        }
        
        try{
            FileWriter fw  = new FileWriter("BaseLine.txt");
            //Loop though the mean Array Size
            for (int i = 0 ; i < meanValue.length; i++)
                fw.write(myEventsLog.get(0)[i][0]+":"+meanValue[i]+":"+stdValue[i]+":\n" );
            
            fw.close();            
        }catch (Exception e ){
            System.err.println("Failed to create New Baseline");
            return;
        }

        System.out.println("Sucessfully Create New BaseLine");

    }

    public static double reduceToTwoDecimal(double value){
        value = Math.round(value * 100);
        value /= 100;
        return value;
    }

    public static void displayLogInfo(ArrayList<String[][]> myEventsLog){
        System.out.println("Displaying Events");
        System.out.println("------------------------------------------------");
        for (int i = 0 ; i < myEventsLog.size() ; i++){
            System.out.println("Days " + (i+1));
            System.out.println("===================================");
            System.out.println("User Login for about " + myEventsLog.get(i)[0][1] + " Times");
            System.out.println("User Login for about " + myEventsLog.get(i)[1][1] + " Minutes");
            System.out.println("User Send about  " + myEventsLog.get(i)[2][1] + " Emails");
            System.out.println("User Open about  " + myEventsLog.get(i)[3][1] + " Emails");
            System.out.println("User Delete about  " + myEventsLog.get(i)[4][1] + " Emails");
            System.out.println("===================================");

        }

    }

    public static void displayEventsInside (ArrayList<Events> myEvents ){
        for (Events a : myEvents){
            System.out.println("============================================================");
            System.out.println("Parameter Name : " + a.getName());
            System.out.println("Parameter Type : " + a.getType());
            System.out.println("Minimal Value  : " + a.getMin());
            System.out.println("Maximum value  : " + a.getMax());
            System.out.println("The Weight     : " + a.getWeight());
            System.out.println("Stats Mean     : " + a.getStats().getMean());
            System.out.println("Stats STD      : " +a.getStats().getStd());
        }
        System.out.println("============================================================");

    }
    
}