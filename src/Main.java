/**
 * Created by Noah Vito on 3/17/15.
 */


import java.io.*;
import java.util.ArrayList;


public class Main {

    /**
     * Global Variables
     */
    //[circuits][jugglers]
    static int[][] dotProducts;//dot products

    /**
     * Creates a BufferedReader
     * @param file the file to read from
     * @return a BufferedReader reading from this file
     */
    public static BufferedReader createBR(String file){
        try {
            return new BufferedReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            System.out.println("createBR: FileNotFoundException");
            return null;
        }
    }

    /**
     * Reads the next line and returns it.
     * @param reader the file reader
     * @return the next line if there is one, null otherwise
     */
    public static String readNextLine(BufferedReader reader){
        try {
            return reader.readLine();
        }
        catch(IOException e) {
            System.out.println("in.readline: IOException");
            return null;
        }
    }

    /**
     *  Parses the input file and fills in the Circuit and Juggler arrays.
     * @param in the input files
     * @param circuits the Circuit array
     * @param jugglers the Juggler array
     */
    public static void parseInput(BufferedReader in, Circuit[] circuits, Juggler[] jugglers){
        String delims = "[ JCHEP:,]+";

        String currentLine = readNextLine(in);
        while(currentLine != null) {
            //System.out.println(currentLine); //for testing
            if(currentLine.length() != 0 && currentLine.charAt(0) == 'C'){//if this line is a circuit
                //parse and construct a Circuit object at the correct index of circuits[]
                String[] tokens = currentLine.split(delims);
                int id = Integer.parseInt(tokens[1]);
                circuits[id] = new Circuit(Integer.parseInt(tokens[2]),
                        Integer.parseInt(tokens[3]),
                        Integer.parseInt(tokens[4]));

                //print for testing
                //System.out.println("C" + id + " H:" + circuits[id].h + " E:" + circuits[id].e + " P:" + circuits[id].p);
            }
            else if(currentLine.length() != 0 && currentLine.charAt(0) == 'J'){//if this line is a juggler
                //parse and construct a Juggler object at the correct index of jugglers[]
                String[] tokens = currentLine.split(delims);
                int id = Integer.parseInt(tokens[1]);
                //create preferred circuits array
                int[] prefCircuits = new int[10];
                for(int i = 0; i < 10; i++) {
                    prefCircuits[i] = Integer.parseInt(tokens[5 + i]);
                }
                //construct Juggler
                jugglers[id] = new Juggler(Integer.parseInt(tokens[2]),
                        Integer.parseInt(tokens[3]),
                        Integer.parseInt(tokens[4]),
                        prefCircuits);

                //print for testing
                /*System.out.print("J" + id + " H:" + jugglers[id].h + " E:" + jugglers[id].e + " P:" + jugglers[id].p + " pC:");
                for(int i = 0; i < 10; i++){
                    System.out.print(jugglers[id].prefCircuits[i] + " ");
                }
                System.out.print("\n");*/
            }

            currentLine = readNextLine(in);
        }

    }

    /**
     * Calculates the dot products for circuits x jugglers
     * @param circuits the circuits array
     * @param jugglers the jugglers array
     * @return the dot products for all possible circuits x jugglers
     */
    public static int[][] calcDotProducts(Circuit[] circuits, Juggler[] jugglers){
        int[][] dp = new int[2000][12000];
        //[c][j]
        for(int c = 0; c < circuits.length; c++){//for each circuit
            for(int j = 0; j < jugglers.length; j++){//for each juggler
                //calculate dot product
                dp[c][j] = circuits[c].h * jugglers[j].h +
                        circuits[c].e * jugglers[j].e +
                        circuits[c].p * jugglers[j].p;
            }
        }
        return dp;
    }

    /**
     * Assigns all jugglers to teams.
     * @param jugglers the array of jugglers
     * @return the final team assignments
     */
    public static int[][] assignJugglers(Juggler[] jugglers){
        int numTeams = dotProducts.length;//number of circuits
        int teamSize = dotProducts[0].length/dotProducts.length;//num jugglers / num circuits
        int[][] teamSolutions = new int[numTeams][teamSize];//solutions array
        for(int j = 0; j < numTeams; j++){
            for(int k = 0; k < teamSize; k++){
                teamSolutions[j][k] = -1;//initialize all team members to jugglerID -1.
            }
        }

        ArrayList<Integer> unassignedJugglerIDs = new ArrayList<Integer>();//jugglers that still need to be assigned
        //for now, everyone needs to be assigned.
        for(int i = 0; i < jugglers.length; i++){
            unassignedJugglerIDs.add(i);
        }

        while(unassignedJugglerIDs.size() > 0){//while jugglers need to be assigned
            for(int i = 0; i < unassignedJugglerIDs.size(); i++){
                Juggler thisJuggler = jugglers[unassignedJugglerIDs.get(i)];//the current juggler is the juggler with the id stored at position i of unassignedJugglers.
                int thisJugglerID = unassignedJugglerIDs.get(i);
                //assign this juggler to his most preferred circuit which he is also good enough at.
                boolean assigned = false;
                for(int pC = 0; pC < thisJuggler.prefCircuits.length; pC++){//try to assign him to one of his preferred circuits.
                    int[] currentTeam = teamSolutions[thisJuggler.prefCircuits[pC]]; //the current team assigned to the circuit at position pC in this juggler's preferred circuits array
                    if(isQualified(thisJugglerID, currentTeam, thisJuggler.prefCircuits[pC])){//if he's qualified for this circuit
                        int weakest = replaceWeakest(thisJugglerID, currentTeam, thisJuggler.prefCircuits[pC]);
                        if(currentTeam[weakest] != -1)//if this juggler took someone's spot (not a free spot)
                            unassignedJugglerIDs.add(currentTeam[weakest]);//the weakest member of the team now needs to be reassigned.
                        currentTeam[weakest] = thisJugglerID;//replace weakest member of the team with this juggler.
                        unassignedJugglerIDs.remove(i);//this juggler is no longer unassigned.
                        assigned = true;
                        break;//and we don't need to check any lower on this juggler's preferred circuits.
                    }
                }
                if(!assigned){//if this juggler couldn't fit into any of his preferred circuits
                    //find him an empty spot.
                    for(int c = 0; c < numTeams; c++){//for each circuit
                        int[] thisTeam = teamSolutions[c];
                        for(int m = 0; m < thisTeam.length; m++){//for each member of this circuit
                            if(thisTeam[m] == -1) {//if this is an empty spot
                                thisTeam[m] = thisJugglerID;//put this juggler in the empty spot.
                                unassignedJugglerIDs.remove(i);//this juggler is no longer unassigned.
                                assigned = true;
                                break;
                            }
                        }
                        if(assigned)
                            break;
                    }
                    if(!assigned){
                        System.out.println("ERROR: Nowhere for JugglerID " + thisJugglerID + " to go. ");
                    }
                }
            }
        }
        return teamSolutions;
    }

    /**
     * Determines whether a juggler is qualified to join a team.
     * @param jugglerID the juggler whose qualification must be checked
     * @param currentTeam the array of jugglers currently on this team
     * @param teamNum this team's id number
     * @return true if this juggler is better than one of the current jugglers, false otherwise.
     */
    public static boolean isQualified(int jugglerID, int[] currentTeam, int teamNum){
        for(int i = 0; i < currentTeam.length; i++){
            if(currentTeam[i] == -1)//if this is a free spot
                return true;
            if(dotProducts[teamNum][jugglerID] > dotProducts[teamNum][currentTeam[i]])
                //if this juggler is a better match than one of the currently assigned jugglers
                return true;
        }
        //if all the current jugglers are better
        return false;
    }

    /**
     * Finds the weakest juggler on a team, and returns their index within the team.
     * @param newJugglerID the juggler who will be joining the team
     * @param currentTeam the array of all jugglers currently on the team
     * @param teamNum this team's id number
     * @return the index of the weakest juggler on this team
     */
    public static int replaceWeakest(int newJugglerID, int[] currentTeam, int teamNum){
        if(currentTeam[0] == -1)//if the first spot is free
            return 0;//go ahead and return it  now.
        int weakestJugglerIndex = 0;//the index of the weakest juggler on this team.
        int min = dotProducts[teamNum][currentTeam[0]];//min = the dotProduct of the current team x the first team member.
        for(int i = 0; i < currentTeam.length; i++){//for each juggler on the team
            if(currentTeam[i] == -1)//if this is a free spot
                return i;//go ahead and return this spot now.
            if(dotProducts[teamNum][currentTeam[i]] < min) {//if this team member is weaker than our previous weakest
                min = dotProducts[teamNum][currentTeam[i]];//update minimum
                weakestJugglerIndex = i;//update weakest index
            }
        }
        return weakestJugglerIndex;
    }



    public static void main(String[] args) {
        BufferedReader in = createBR("jugglefest.txt");//reader from input file

        Circuit[] circuits = new Circuit[2000];//circuit data
        Juggler[] jugglers = new Juggler[12000];//juggler data

        parseInput(in, circuits, jugglers);//fill in circuits and jugglers

        dotProducts = calcDotProducts(circuits, jugglers);//calculate dot products

        // [circuits][jugglers assigned to circuit]
        int[][] teams;

        teams = assignJugglers(jugglers);//dotProducts, jugglers);


        //writer for output.
        PrintWriter writer = null;
        try {
            writer = new PrintWriter("output.txt", "UTF-8");;//new BufferedWriter(new OutputStreamWriter(new FileOutputStream("output.txt"), "utf-8"));
        } catch (IOException ex) {
            System.out.println("Error creating PrintWriter: IOException");
        }

        //write the output
        for(int t = 0; t < teams.length; t++) {
            writer.write("C" + t);
            for(int j = 0; j < teams[t].length; j++){
                writer.write(" J" + teams[t][j]);
                for(int c = 0; c < jugglers[teams[t][j]].prefCircuits.length; c++){
                    writer.write(" C" + jugglers[teams[t][j]].prefCircuits[c] + ":" + dotProducts[jugglers[teams[t][j]].prefCircuits[c]][teams[t][j]]);
                }
                writer.write(",");
            }
            writer.write("\n");
        }


        writer.close();

        System.out.println("Done. Output in output.txt");
    }




}