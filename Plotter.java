import java.io.*;

class Plotter{
  Process p;
  PrintWriter pw;
  BufferedReader br;

  Plotter(){
    try{
      p = Runtime.getRuntime().exec("gnuplot -persist");
      pw = new PrintWriter(new OutputStreamWriter(p.getOutputStream()));
      br = new BufferedReader(new InputStreamReader(p.getInputStream()), 5000);
    }
    catch(IOException ex){}
    }

    public static void main(String[] Args){
      Plotter pl = new Plotter();

      pl.pw.println("plot '-' with lines");
      // try{
        for(int i=0; i<20; i++){
        // Thread.sleep(500);
          pl.pw.println(i + " " + 2*i*Math.random());
        }
      // }
      // catch(InterruptedException ex){}
      pl.pw.println("e");
      pl.pw.flush();

    }
}
