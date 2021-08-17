import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class TravellingSalesmanProblem {

    public static void main(String[] args) {

        TSPGUI GUI = new TSPGUI();


        //initial list of csv lines
        ArrayList <String> list = new ArrayList<>();
        //delivery objects with delivery details
        List<Object> deliveries = new ArrayList<>();
        //fastest route output
        List<Integer> routeByOrderNumber = new ArrayList<>();

        //all roads lead out from apache
        deliveries.add(new Delivery(0, "Apache", 0, 53.38133, -6.59299));

        //sample data is the excel sample data file shared be professor, file converted to csv and read in from desktop
        File f = new File("C:\\Users\\jpsho\\Desktop\\leaner-data.csv");

        //we take in the csv file and add each line to a String array list

        try (Scanner sc = new Scanner(f)) {
            sc.useDelimiter(",");
            while(sc.hasNextLine()){
                list.add((sc.nextLine()));
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < list.size(); i++){
            System.out.println(list.get(i));
        }

        //int count = 0;
        //            for(int i = 0; i < elements.length; i++){
        //                for(String in : elements[i].split(",")){
        //                    elementList[count] = in;
        //                    count++;
        //                }
        //            }

        //size of string array for individual elements must be list by 5, as we need 5 elements per object later, 5 cols per row
        String[] elementList = new String[list.size()*5];

        int count = 0;
        for(int i = 0; i < list.size(); i++){
            for(String in : list.get(i).split(",")){
                elementList[count] = in;
                System.out.println(elementList[count]);
                count++;
            }
        }
        count=0;

        //create a new array list of deliveries objects, parse each string and add to the relevant attribute in the deliveries class object
        //the setters contain the needed parsing methods
        while(count < elementList.length){
            Delivery details = new Delivery();

            details.setNumber(elementList[0+count].trim());
            details.setAddress(elementList[1+count].trim());
            details.setWaitingTime(elementList[2+count].trim());
            details.setLat(elementList[3+count].trim());
            details.setLon(elementList[4+count].trim());

            deliveries.add(details);

            count += 5;
        }

        //the routes 2d array will take each row as an origin and calculate the distance from origin to next col til end of row
        //row number represents origin, next stop represented by col number, where origin and next are equal return 0 distance, if different address return 1
        Route[][] routes = new Route[deliveries.size()][deliveries.size()];

        for(int j = 0; j < deliveries.size(); j++) {
            for (int k = 0; k < deliveries.size(); k++) {
                Delivery Origin = (Delivery) deliveries.get(j);
                Delivery NextDestination = (Delivery) deliveries.get(k);
                int timeTaken = geoTime(Origin, NextDestination);
                routes[j][k] = new Route(j, k, timeTaken);
            }
        }

        //printing an unsorted 2d array of distances
        System.out.println("         ---Unsorted Route Objects Matrix---");
        for(int j = 0; j < deliveries.size(); j++){
            for(int k = 0; k < deliveries.size(); k++) {
                Route unsortedRes = routes[j][k];
                System.out.print("["+unsortedRes.getRow()+","+unsortedRes.getCol()+"]  "+unsortedRes.getTimeTaken()+"   ");
            }
            System.out.println();
        }

        System.out.println();
        //helper sorting method takes unsorted 2d array object and sorts each row in ascending distance returns sorted 2d array
        Route[][] sortRouteByTime = sortTimeAsc(routes);

        //printing a now sorted 2d array to compare before and after
        System.out.println("         ---Sorted Route Objects Matrix---");
        for(int j = 0; j < deliveries.size(); j++){
            for(int k = 0; k < deliveries.size(); k++) {
                Route sortedRes = sortRouteByTime[j][k];
                System.out.print("["+sortedRes.getRow()+","+sortedRes.getCol()+"]  "+sortedRes.getTimeTaken()+"   ");
            }
            System.out.println();
        }

        //fill order number list by fastest route
        //never consider starting point as delivery so add column 0 before loop
        routeByOrderNumber.add(0);
        //filling a list for shortest route by 'order number'
        for(int j = 0; j < deliveries.size(); j++){
            for(int k = 0; k < deliveries.size(); k++) {

                if(sortRouteByTime[j][k].getTimeTaken() != 0 && !routeByOrderNumber.contains(sortRouteByTime[j][k].getCol())){
                    routeByOrderNumber.add(sortRouteByTime[j][k].getCol());
//                    System.out.println("****************");
//                    System.out.println("at index");
//                    System.out.println(""+j+", "+k+": "+sortRouteByTime[j][k].getTimeTaken());
                    //very important and a point where i was stuck, assign the last col gotten and added to list to be the the row from where we continue searching
                    j = sortRouteByTime[j][k].getCol();

                    k=0;

                }
            }
        }
//
        int i = 1;
        System.out.println();
        System.out.println("---FASTEST ROUTE FROM APACHE---   ");
        while(i<routeByOrderNumber.size()){
            int orderNum = routeByOrderNumber.get(i);
            System.out.print(orderNum+",");
            i++;
        }
        System.out.println();
        System.out.println("*****************End Of Console Solution************************");
        System.out.println();

    }

    //haversine method converting distance to time
    static int geoTime(Delivery X, Delivery Y) {

        //because some lon/lat are identical but addresses are different give shortest distance possible
        //this insures the locations are treated differently during shortest route
        if(X.getLat() == Y.getLat() && X.getLon() == Y.getLon() && !X.getAddress().equals(Y.getAddress())){
            return 1;
        }

        double angleA = Math.toRadians(X.lat);
        double angleB = Math.toRadians(Y.lat);
        double theta = Math.toRadians(Y.lat - X.lat);
        double lambda = Math.toRadians(Y.lon - X.lon);

        double temp = Math.pow(Math.sin(theta / 2.0), 2) + Math.cos(angleA) * Math.cos(angleB) *
                Math.pow(Math.sin(lambda / 2.0), 2);

        double unit = 2 * Math.asin(Math.sqrt(temp));
        double one = 6371 * unit;
        double res = (double) Math.round(one * 1000) / 1000.0;

        if (res == 0.0) {
            return (int) res;
        }

        //begin converting distance to time, time conversion is in seconds elapsed
        BigDecimal bD = new BigDecimal(String.valueOf(res));
        int km = bD.intValue();
        double kmMinus = km;
        double resInM = res - kmMinus;

        String s = String.valueOf(resInM).replaceAll("[0.]", "");

        //catches results of long decimal places and cuts off at 3 decimals
        if (s.length() > 3) {
            String nearestKm = s.substring(0, 3);
            int metersToConvert = Integer.parseInt(nearestKm);
            int seconds = (int) Math.round(metersToConvert / 16.6);
            int totalSeconds = (km * 60) + seconds;

            return totalSeconds;
        }

        //if less than three decimals  carryout same conversion
        int metersToConvert = Integer.parseInt(s);
        int seconds = (int) Math.round(metersToConvert / 16.6);
        int total = (km * 60) + seconds;

        return total;
    }

    //sorting 2d array method
    static Route[][] sortTimeAsc(Route[][] routes) {

        for (int i = 0; i < routes.length; i++) {
            for (int j = 0; j < routes[i].length; j++) {

                // loop for comparison and swapping
                for (int k = 0; k < routes[i].length - j - 1; k++) {

                    if (routes[i][k].getTimeTaken() > routes[i][k+1].getTimeTaken()) {
                        // swapping of elements
                        Route temp;
                        temp = routes[i][k];
                        routes[i][k] = routes[i][k + 1];
                        routes[i][k + 1] = temp;
                    }
                }

            }
        }
        return routes;
    }


}


//class holds all delivery details
class Delivery {
    int number;
    String address;
    int waitingTime;
    double lat;
    double lon;
    double routeTime;

    Delivery(){

    }

    public Delivery(int num, String address, int waitingTime, double lat, double lon){
        this.number = num;
        this.address = address;
        this.waitingTime = waitingTime;
        this.lon = lon;
        this.lat = lat;
    }

    public void setNumber(String num){
        this.number = Integer.parseInt(num);
    }

    public void setAddress(String add){
        this.address = add;
    }

    public void setWaitingTime(String waiting){
        this.waitingTime = Integer.parseInt(waiting);
    }

    public void setLon(String lon){
        this.lon = Double.parseDouble(lon);
    }

    public void setLat(String lat){
        this.lat = Double.parseDouble(lat);
    }

    public int getNumber(){
        return number;
    }

    public String getAddress(){
        return address;
    }

    public int getWaitingTime(){
        return waitingTime;
    }

    public double getLon(){
        return lon;
    }

    public double getLat(){
        return lat;
    }

    public double getRouteTime(){
        return routeTime;
    }
}

//route class, objects from here are used make matrix to compare/swap
class Route{

    private int row;
    private int col;
    private int timeTaken;

    Route(int row, int col, int timeTaken){
        this.row = row;
        this.col = col;
        this.timeTaken = timeTaken;
    }

    public int getRow(){return  this.row;}

    public int getCol(){return  this.col;}

    public double getTimeTaken(){return  this.timeTaken;}

    public void setTimeTaken(int update){
        this.timeTaken = update;
    }

}

//graphics section
class TSPGUI implements ActionListener {

    JButton button;
    JFrame frame;
    JTextArea ti;
    JTextArea to;
    JLabel label;
    JLabel label2;
    JLabel label3;
    JPanel panel;

    public TSPGUI() {

        //label for gui header
        label = new JLabel();
        label.setText("PIZZA PAZZA DELIVERY ROUTE FINDER");
        label.setForeground(Color.black);
        label.setFont(new Font("Monaco", Font.BOLD,40));
        label.setVerticalAlignment(JLabel.TOP);
        label.setHorizontalAlignment(JLabel.CENTER);


        //label for text title of output component
        label2 = new JLabel();
        label2.setText("ORDER NUMBER SEQUENCE");
        label2.setForeground(Color.black);
        label2.setFont(new Font("Monaco", Font.ITALIC,20));
        label2.setVerticalAlignment(JLabel.BOTTOM);
        label2.setHorizontalAlignment(JLabel.CENTER);


        //label direction
        label3 = new JLabel();
        label3.setText("Input Orders Below");
        label3.setForeground(Color.black);
        label3.setFont(new Font("Monaco", Font.ITALIC,15));
        label3.setVerticalAlignment(JLabel.BOTTOM);
        label3.setHorizontalAlignment(JLabel.CENTER);

        //textarea input
        ti = new JTextArea(30, 45);

        //textarea output
        to = new JTextArea(2,140);



        //scrollbar for text area for input has vertical scroll only
        JScrollPane sp = new JScrollPane(ti);
        sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        //scrollbar for text output only needs horizontal scroll
        JScrollPane spOut = new JScrollPane(to);
        spOut.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        spOut.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        //panel to hold header
        JPanel panel1 = new JPanel();
        //panel to hold text input
        JPanel panel2 = new JPanel();
        //panel to hold output
        JPanel panel3 = new JPanel();

        panel1.setBackground(Color.GREEN);
        panel2.setBackground(Color.WHITE);
        panel3.setBackground(Color.RED);


        panel1.setPreferredSize(new Dimension(100, 94));
        panel2.setPreferredSize(new Dimension(150, 150));
        panel3.setPreferredSize(new Dimension(100, 114));


        //creating button and setting styles and text
        button = new JButton("Find Fastest Route");
        button.setPreferredSize(new Dimension(150, 150));
        button.addActionListener(this);
        button.setBackground(new Color(125, 125, 125));
        button.setFont(new Font("Monaco", Font.ITALIC, 15));
        button.setForeground(new Color(250, 250, 250));
        button.setBorder(BorderFactory.createEtchedBorder());
        button.setFocusable(false);

        //setting up the frame
        frame = new JFrame("\"TravellingSalesmanProblem\"");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(250, 250);
        frame.setVisible(true);
        frame.add(panel1, BorderLayout.NORTH);
        frame.add(panel2, BorderLayout.CENTER);
        frame.add(panel3, BorderLayout.SOUTH);

        //adding all necessary components to panel containers
        panel1.add(label);
        panel1.add(label3);
        panel2.add(sp);
        panel2.add(button);
        panel3.add(label2);
        panel3.add(spOut);

    }

    @Override
    public void actionPerformed(ActionEvent e) {

        //the code contained within this method is identical to that of the main runner method bar a few variable name changes
        //method to take in csv input slightly different also

        if(e.getSource()== ti || e.getSource() == button){

            System.out.println(ti.getText());

            List<Object> deliveries = new ArrayList<>();
            String[] elements = ti.getText().trim().split("\\n");
            List<Integer> shortestRoute = new ArrayList<>();

            System.out.println(elements.length);

            String[] elementList = new String[elements.length*5];

            int count = 0;
            for(int i = 0; i < elements.length; i++){
                for(String in : elements[i].split(",")){
                    elementList[count] = in;
                    count++;
                }
            }

            deliveries.add(new Delivery(0, "Apache", 0, 53.38133, -6.59299));
            Delivery X = (Delivery) deliveries.get(0);
            System.out.println(X.getAddress());


            count = 0;

            while(count < elementList.length){
                Delivery details = new Delivery();

                details.setNumber(elementList[0+count].trim());
                details.setAddress(elementList[1+count].trim());
                details.setWaitingTime(elementList[2+count].trim());
                details.setLat(elementList[3+count].trim());
                details.setLon(elementList[4+count].trim());

                deliveries.add(details);

                count += 5;
            }

            Route[][] routes = new Route[deliveries.size()][deliveries.size()];

            for(int j = 0; j < deliveries.size(); j++) {
                for (int k = 0; k < deliveries.size(); k++) {
                    Delivery Origin = (Delivery) deliveries.get(j);
                    Delivery NextDestination = (Delivery) deliveries.get(k);
                    int timeTaken = TravellingSalesmanProblem.geoTime(Origin, NextDestination);
                    routes[j][k] = new Route(j, k, timeTaken);
                }
            }

            Route[][] sortRouteByTime = TravellingSalesmanProblem.sortTimeAsc(routes);

            shortestRoute.add(0);
            //filling a list for shortest route by 'order number'
            for(int j = 0; j < deliveries.size(); j++){
                for(int k = 0; k < deliveries.size(); k++) {
                    if(sortRouteByTime[j][k].getTimeTaken() != 0 && !shortestRoute.contains(sortRouteByTime[j][k].getCol())){

                        shortestRoute.add(sortRouteByTime[j][k].getCol());
                        //assigning last col value to next row index you wish to go to jump
                        j = sortRouteByTime[j][k].getCol();
                        k=0;

                    }
                }
            }

            int i = 1;
            System.out.println();
            System.out.println("---FASTEST ROUTE FROM APACHE---   ");
            String output = "";

            while(i<shortestRoute.size()){
                int orderNum = shortestRoute.get(i);
                System.out.print(orderNum+",");
                output += String.valueOf(shortestRoute.get(i));
                output += ",";
                i++;
            }

            //remove final comma
            String outputTwo = output.substring(0, output.length()-1);
            to.setText(outputTwo);

            System.out.println();
            System.out.println("*****************End Of GUI Solution************************");
            System.out.println();

        }
    }
}




