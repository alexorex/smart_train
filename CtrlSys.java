import java.io.*;
import java.util.ArrayList;

class CtrlSys{
  static float timeMult = 1;

  public static void main(String[] args){

    PathDevi pD = new PathDevi();
    ArrayList<Float> dLst;

    for(;;){

      dLst = pD.distToPath();
      for(float sar: dLst){
        System.out.println(sar);
      }
      System.out.println("");


      try{
        Thread.sleep(1000/(long) CtrlSys.timeMult);
      }
      catch(InterruptedException ex){}
      }

    }
}
