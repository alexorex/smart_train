import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Process;

class CoordTrack implements  Runnable{
  // X Y Z and X Y Z W
  static volatile boolean readReq = false;
  static volatile String s;
  static ArrayList<float[][]> carCrd = new ArrayList<float[][]>();
  String ss, str = "";
  String[] cars = {"train", "\"train::car_1::chassis\"", "\"train::car_2::chassis\"",
    "\"train::car_3::chassis\"", "\"train::car_4::chassis\"",
    "\"train::car_5::chassis\"" };
  Process p;
  BufferedReader br;

  CoordTrack(){
      (new Thread(this)).start();
  }

  ArrayList<float[][]> parse(String s, String[] sarr){
    float[] loc = {0, 0, 0};
    float[] rot = {0, 0, 0, 0};
    Scanner sc;
    // int i = 0;

    CoordTrack.carCrd.clear();
    carCrd.ensureCapacity(sarr.length);

    for(String par: sarr){
      sc = new Scanner(ss = s.substring(s.indexOf(par),
        s.indexOf("} }", s.indexOf(par)) + 3));
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

      System.out.println(carCrd.size());
        System.out.println("x: " + loc[0] + " y: " + loc[1] + " z: " + loc[2]);
        System.out.println("x: " + rot[0] + " y: " + rot[1] + " z: " + rot[2] + " w: " + rot[3]);

      CoordTrack.carCrd.add(new float[][] { loc, rot });
      // i++;
    }
    for(float[][] sar: carCrd){
      System.out.println("x: " + sar[0][0] + " y: " + sar[0][1] + " z: " + sar[0][2]);
      System.out.println("x: " + sar[1][0] + " y: " + sar[1][1] + " z: " + sar[1][2] + " w: " + sar[1][3]);
    }

    return(carCrd);
  }

  public ArrayList<float[][]> trainPose(){
    CoordTrack.readReq = true;
    while(CoordTrack.readReq);
    return parse(CoordTrack.s, cars);
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
