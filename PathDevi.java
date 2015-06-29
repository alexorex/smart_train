import java.io.*;
// import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.LinkedList;
import java.util.ArrayList;

class PathDevi implements Runnable{
  static volatile LinkedList<float[]> path = new LinkedList<float[]>();
  CoordTrack cTr;
  float minPtP = (float) 0.05;
  float modelLength = (float) 2.6;
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

  float direcToPath(float[] v0, float[] v1, float[] p){
    // return direction of closest vector spinning to point direction:
    // 1 - right, -1 - left
    // spin vector Pi/2 using corresp. operator matrix
    // result = Sign(Cos(angle(spinned vec. and vec. to the point in question)))
    float[] spinVec = { v1[1]-v0[1], -(v1[0]-v0[0]) };
    float[] vecToP = { p[0]-v0[0], p[1]-v0[1] };
    return(Math.signum(spinVec[0]*vecToP[0] + spinVec[1]*vecToP[1]));
  }

  ArrayList<Float> distToPath(){
    ArrayList<Float> distLst = new ArrayList<Float>();
    LinkedList<float[]> pathSeg, cLstSub;

    lck.lock();
      pathSeg = new LinkedList<float[]>(path.subList( (path.size() > modelLength/minPtP ?
        path.size() - (int) (modelLength/minPtP) : 0), path.size()));
    lck.unlock();
    synchronized(this){
      cLstSub = new LinkedList<float[]>(cLst.subList(1, cLst.size()));
    }

    //limit the number of path points to search
    float d, par;
    float[] v0 = new float[2], v1 = new float[2];
    for(float[] c: cLstSub){
      d = Float.MAX_VALUE;
      // System.out.println("");
      for(float[] p: pathSeg){
        // System.out.print("PSx: "+p[0] + " PSy: "+p[1]+" dst: "+ PtoP(p, c) + " || ");
        if((par = PtoP(c, p)) < d){
          d = par;
          v0[0] = p[0]; v0[1] = p[1];
        }
        else
          v1[0] = p[0]; v1[1] = p[1];

}
      distLst.add(d*direcToPath(v0, v1, c));
      // System.out.println(" i: "+i);
// System.out.println(b[0]+" "+b[1]+" dst: "+d+" i: "+i);
// System.out.println(cLstSub.get(cLstSub.indexOf(c))[0]+" "+cLstSub.get(cLstSub.indexOf(c))[1]);
    }

    return(distLst);
  }

  public void run(){
  // fill path
    boolean blockedOnPrevIter = false;
    float[] pathLastPoint;
    LinkedList<float[]> pathTmp = new LinkedList<float[]>();
    // ArrayList<float[]> cLst;
        pl.pw.println("plot '-' with lines, '-' with lines");

    try{
      for(;;){
        synchronized(this){
          cLst = cTr.trainPose();
            int t=0;
            float[] f;
          for(java.util.Iterator<float[]> I = path.descendingIterator();
            I.hasNext() && t<100; t++){
            f =  I.next();
            pl.pw.println(f[0] + " " + f[1]);
            if (t>100)
              break;
          }
        pl.pw.println("e");

          for(float[] n: cLst)
            pl.pw.println(n[0] + " " + n[1]);
        }

        pl.pw.println("e");
        pl.pw.println("replot");
        pl.pw.flush();

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
