import java.io.*;
import java.util.Scanner;
import java.util.ArrayList;
import java.lang.Process;

class CoordTrack implements  Runnable{
  // X Y Z and X Y Z W
  static ArrayList<float[][]> carCrd = new ArrayList<float[][]>();
  static ArrayList<float[]>  globCrd = new ArrayList<float[]>();

  volatile boolean threadStarted = false;
  static volatile String s;
  String ss, str = "";
  String[] cars = {"train", "\"train::car_1::chassis\"",
    "\"train::car_2::chassis\"", "\"train::car_3::chassis\"",
    "\"train::car_4::chassis\"", "\"train::car_5::chassis\"" };
  Process p;
  BufferedReader br;

  CoordTrack(){
      (new Thread(this)).start();
      while(s == null);
      }



  ArrayList<float[][]> parse(String s, String[] sarr){
    float[] loc = {0, 0, 0};
    float[] rot = {0, 0, 0, 0};
    Scanner sc;

    CoordTrack.carCrd.clear();
    CoordTrack.carCrd.ensureCapacity(sarr.length);

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

      CoordTrack.carCrd.add(new float[][] { loc.clone(), rot.clone() });
      // System.out.println(carCrd.size());
      // for(float[][] sar: carCrd){
      //   System.out.println("x: " + sar[0][0] + " y: " + sar[0][1] + " z: " + sar[0][2]);
      //   System.out.println("x: " + sar[1][0] + " y: " + sar[1][1] + " z: " + sar[1][2] + " w: " + sar[1][3]);
      // }
      //   System.out.println("");
    }

    return(carCrd);
  }

  ArrayList<float[]> toGlobalCrd(ArrayList<float[][]> locCrd){
    // X = X0 + (x*cos(z) - y*sin(z))*cos(y)
    // Y = Y0 + (x*sin(z) + y*cos(z))*cos(x)
    // Z = Z0 + z*cos(y)*cos(x)
    // gMLoc = x, y, z of model train
    // gMRot = rotations of x, y, z, w (QUATERNION)

    float[] gLoc = new float[3], gMLoc = (locCrd.get(0)[0]).clone(),
      gMRot = new float[4];
      gMRot[0] = (float)(Math.PI - 2*Math.acos(locCrd.get(0)[1][0]));
      gMRot[1] = (float)(Math.PI - 2*Math.acos(locCrd.get(0)[1][1]));
      gMRot[2] =  Math.signum(locCrd.get(0)[1][3])*
        (float)(Math.PI - 2*Math.acos(locCrd.get(0)[1][2]));
      gMRot[3] = locCrd.get(0)[1][3];

    CoordTrack.globCrd.clear();
    CoordTrack.globCrd.ensureCapacity(locCrd.size());
    CoordTrack.globCrd.add(locCrd.get(0)[0]);

    locCrd.remove(0);
    for(float[][] e: locCrd){
      gLoc[0] = gMLoc[0] + (float) ((e[0][0]*Math.cos(gMRot[2]) -
        e[0][1]*Math.sin(gMRot[2]))*Math.cos(gMRot[1]));
      gLoc[1] = gMLoc[1] + (float) ((e[0][0]*Math.sin(gMRot[2]) +
        e[0][1]*Math.cos(gMRot[2]))*Math.cos(gMRot[0]));
      gLoc[2] = gMLoc[2] + (float) (e[0][2]*Math.cos(gMRot[0])*Math.cos(gMRot[1]));

      CoordTrack.globCrd.add(gLoc.clone());
    }
    // for(float[] sar: globCrd){
    //   System.out.println("x: " + sar[0] + " y: " + sar[1] + " z: " + sar[2]);
    // }
    //   System.out.println("");

    return(CoordTrack.globCrd);
  }

  synchronized public ArrayList<float[]> trainPose(){
    return toGlobalCrd(parse(CoordTrack.s, cars));
  }


  public void run(){
    try{
      p = Runtime.getRuntime().exec("gz topic -e /gazebo/default/pose/info -u");
      br = new BufferedReader(new InputStreamReader(p.getInputStream()), 5000);

      for(;;){
        CoordTrack.s = br.readLine();
      }
    }
    catch(IOException ex){}
  }

  public static void main(String[] args){
    CoordTrack ct = new CoordTrack();


    ArrayList<float[][]> res;
    ArrayList<float[]> res2;
    while(true){
    try{
      Thread.sleep(500);
    }
    catch(InterruptedException ex){}

      // res = ct.parse(CoordTrack.s, ct.cars);
      // for(float[][] p: res){
      //   System.out.println(p[0][0] + " " + p[0][1] + " " + p[0][2]);
      //   System.out.println(p[1][0] + " " + p[1][1] + " " +
      //     p[1][2] + " " + p[1][3]);
      // }
      //   System.out.println("");

      res2 = ct.trainPose();
      // for(float[] p: res2){
      //   System.out.println(p[0] + " " + p[1] + " " + p[2]);
      // }
      // System.out.println("");
    }
  }
}
