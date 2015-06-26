import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Process;

class CoordTrack implements  Runnable{
  // X Y Z and X Y Z W
  static volatile boolean readReq = false;
  static volatile String s;
  // static ArrayList<float[][]> carCrd = new ArrayList<float[][]>();
  String ss, str = "";
  static String[] cars = {"train", "\"train::car_1::chassis\"", "\"train::car_2::chassis\"",
    "\"train::car_3::chassis\"", "\"train::car_4::chassis\"",
    "\"train::car_5::chassis\"" };
  static float[][][] carCrd = new float[cars.length][2][4];
  Process p;
  BufferedReader br;

  CoordTrack(){
      (new Thread(this)).start();
  }

  float[][][] parse(String s, String[] sarr){
    float[] loc = {0, 0, 0};
    float[] rot = {0, 0, 0, 0};
    Scanner sc;
    int i = 0;

    // CoordTrack.carCrd.clear();
    // carCrd.ensureCapacity(sarr.length);

    for(int f = 0; f < sarr.length; f++){
      sc = new Scanner(ss = s.substring(s.indexOf(sarr[i]),
        s.indexOf("} }", s.indexOf(sarr[i])) + 3));
      // System.out.println(ss);

      while(!(ss = sc.next()).equals("x:"));
      loc[0] = Float.valueOf(sc.next());
      sc.next();
      loc[1] = Float.valueOf(sc.next());
      sc.next();
      loc[2] = Float.valueOf(sc.next());

      while(!(ss = sc.next()).equals("x:"));
      rot[0] = Float.valueOf(sc.next());
      sc.next();
      rot[1] = Float.valueOf(sc.next());
      sc.next();
      rot[2] = Float.valueOf(sc.next());
      sc.next();
      rot[3] = Float.valueOf(sc.next());

      sc.close();

        float[][] t = new float[][] { loc, rot };
        System.out.println("x: " + t[0][0] + " y: " + t[0][1] + " z: " + t[0][2]);
        System.out.println("x: " + t[1][0] + " y: " + t[1][1] + " z: " + t[1][2] + " w: " + t[1][3]);

      System.out.println(i);
      CoordTrack.carCrd[i] = t;
      i++;
    }
    for(int sar = 0; sar<carCrd.length; sar++){
      System.out.println("x: " + carCrd[sar][0][0] + " y: " + carCrd[sar][0][1] + " z: " + carCrd[sar][0][2]);
      System.out.println("x: " + carCrd[sar][1][0] + " y: " + carCrd[sar][1][1] + " z: " + carCrd[sar][1][2] + " w: " + carCrd[sar][1][3]);
    }

    return(carCrd);
  }

  public float[][][] trainPose(){
    CoordTrack.readReq = true;
    while(CoordTrack.readReq);
    return parse(CoordTrack.s, CoordTrack.cars);
  }


  public void run(){
System.out.println("run");

    try{
      p = Runtime.getRuntime().exec("gz topic -e /gazebo/default/pose/info -u");
      br = new BufferedReader(new InputStreamReader(p.getInputStream()), 5000);
      // System.out.println(br.readLine());

      for(;;){
          do
            str = br.readLine();
          while(!CoordTrack.readReq);
        CoordTrack.s = str;
        CoordTrack.readReq = false;
      }
    }
    catch(IOException ex){}
  }
}
