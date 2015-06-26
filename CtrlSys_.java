import java.io.*;
import java.util.ArrayList;

class CtrlSys{
  public static void main(String[] args){

    CoordTrack ct = new CoordTrack();
    float[][][] cLst;
  //   Process p;
  //   BufferedReader br;
  //
  //   try{
  // p = Runtime.getRuntime().exec("gz topic -e /gazebo/default/pose/info -u -d 1");
  // br = new BufferedReader(new InputStreamReader(p.getInputStream()), 5000);
  // System.out.println(br.readLine());
  // }
  // catch(IOException ex){}

    for(;;){
      // System.out.println("for");
      cLst = ct.trainPose();

      // for(float[][] sar: cLst){
      //   System.out.println("x: " + sar[0][0] + " y: " + sar[0][1] + " z: " + sar[0][2]);
      //   System.out.println("x: " + sar[1][0] + " y: " + sar[1][1] + " z: " + sar[1][2] + " w: " + sar[1][3]);
      // }
      System.out.print("\n");

      try{
        Thread.sleep(1000);
      }
      catch(InterruptedException ex){}
      }

    }
}
