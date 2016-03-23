import java.io.*;
import java.util.List;    
import java.util.ArrayList;
import java.util.Arrays;


public class readInput {

   public enum Predicate {
      singlePredicate, doublePredicate,unknown;
   }


   public static void main(String args[]) throws IOException
   {
      FileInputStream in = null;
      FileOutputStream out = null;
      String line = null;
      String fileName = "query_1a.txt";
      List<String> queryList = new ArrayList<String>();
      try {
         FileReader fileReader =  new FileReader(fileName);
         BufferedReader bufferedReader =  new BufferedReader(fileReader);
         while((line = bufferedReader.readLine()) != null) {
                queryList.add(line);
            }   
            // Always close files.
            bufferedReader.close();        
      }
      catch(FileNotFoundException ex) {
            System.out.println("Unable to open file '" + fileName + "'");
      }
      catch(IOException ex) {
            System.out.println("Error reading file '" + fileName + "'");
            // Or we could just do this: 
            // ex.printStackTrace();
      }

      int listSize = queryList.size();
      for(int i = 0; i < listSize; ++i)
         System.out.println(queryList.get(i));

      // output fields + offsets
      String output = queryList.get(0);
      String[] data = output.split(" ");
      for (int i=0;i<data.length;i++)
      {
         System.out.println(data[i]);
         String[] results = data[i].split("_");
         System.out.println(Arrays.toString(results));
      }

      String[] filesToRead = queryList.get(1).split(" ");

      Predicate predicateType = Predicate.unknown;
      if(queryList.size() == 3 ){
         // Single predicate query
         predicateType = Predicate.singlePredicate;
      }
      else if (queryList.size() == 5)
      {
         // Double predicate query  
         predicateType = Predicate.doublePredicate;
      }
      else{
         //unknown predicate query
         predicateType = Predicate.unknown;
      }
   }
}
