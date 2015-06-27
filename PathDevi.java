import java.io.*;
// import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.ArrayList;

class PathDevi implements Runnable{
  static volatile LinkedList<float[]> path = new LinkedList<float[]>();
  CoordTrack cTr;
  float minPtP = (float) 0.05;
  float modelLength = (float) 2.4;
  private ReentrantLock lck = new ReentrantLock();
  volatile ArrayList<float[]> cLst;
  Plotter pl;

  PathDevi(){
    pl = new Plotter();

    cTr = new CoordTrack();
    cLst = cTr.trainPose();
    path.addAll(cLst.subList(1, cLst.size()));
    (new Thread(this)).start();
  }

  float PtoP(float[] a, float[] b){
    return((float) Math.sqrt((b[0]-a[0])*(b[0]-a[0]) + (b[1]-a[1])*(b[1]-a[1])));
  }

  ArrayList<Float> distToPath(){
    ArrayList<Float> distLst = new ArrayList<Float>();
    LinkedList<float[]> pathSeg, cLstSub;

//     try{
//     while(pl.br.ready()){
//       pl.br.readLine();
//     }
// } catch(IOException ex){}

    lck.lock();
      pathSeg = new LinkedList<float[]>(path.subList( (path.size() > modelLength/minPtP ?
        path.size() - (int) (modelLength/minPtP) : 0), path.size()));
    lck.unlock();
    synchronized(this){
      cLstSub = new LinkedList<float[]>(cLst.subList(1, cLst.size()));
    }

    // pl.pw.println("plot '-' with lines,'-' with lines");
    // for(float[] p: pathSeg){
    //   pl.pw.println(p[0] + " " + p[1]);
    // }
    // pl.pw.println("e");

    //limit the number of path points to search
    int i = 999;
    float d, par;
    float[] b = new float[2];
    for(float[] c: cLstSub){
      d = Float.MAX_VALUE;
      // pl.pw.println(c[0] + " " + c[1]);
      // System.out.println("");
      for(float[] p: pathSeg){
        // System.out.print("PSx: "+p[0] + " PSy: "+p[1]+" dst: "+ PtoP(p, c) + " || ");
        if((par = PtoP(c, p)) < d){
          d = par;
          b[0] = p[0]; b[1] = p[1];
          i = pathSeg.lastIndexOf(p);
        }
}
      distLst.add(d);
      // System.out.println(" i: "+i);
// System.out.println(b[0]+" "+b[1]+" dst: "+d+" i: "+i);
// System.out.println(cLstSub.get(cLstSub.indexOf(c))[0]+" "+cLstSub.get(cLstSub.indexOf(c))[1]);
    }
    // pl.pw.println("e");
    // pl.pw.flush();

    return(distLst);
  }

  // fill path
  public void run(){
    boolean blockedOnPrevIter = false;
    float[] pathLastPoint;
    LinkedList<float[]> pathTmp = new LinkedList<float[]>();
    // ArrayList<float[]> cLst;
        pl.pw.println("plot '-' with lines, '-' with lines");

    try{
      for(;;){
        synchronized(this){
          cLst = cTr.trainPose();
        //     int t=0;
        //   for(float[] n: path){
        //     t++;
        //     pl.pw.println(n[0] + " " + n[1]);
        //     if (t>100)
        //       break;
        //   }
        // pl.pw.println("e");
        //   for(float[] n: cLst)
        //     pl.pw.println(n[0] + " " + n[1]);
        }

        // pl.pw.println("e");
        // pl.pw.flush();
        // pl.pw.println("replot");

        if( PtoP(cLst.get(1), path.getLast()) > minPtP ){

            if( lck.tryLock()){
              if(blockedOnPrevIter){
                path.addAll(pathTmp);
                pathTmp.clear();
                blockedOnPrevIter = false;
              }
              path.add(cLst.get(1).clone());
              lck.unlock();
            }
            else{
              pathTmp.add(cLst.get(1).clone());
              blockedOnPrevIter = true;
            }
          }

          Thread.sleep(200/(long) CtrlSys.timeMult);
        }
      }
      catch(InterruptedException ex){}
  }
}
