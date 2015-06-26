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
    ArrayList<float[]> cLst;

  PathDevi(){
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
    LinkedList<float[]> pathSeg;
    //limit the number of path points to search
    lck.lock();
      pathSeg = new LinkedList<float[]>(path.subList( (path.size() > modelLength/minPtP + 1 ?
        path.size() - (int) (modelLength/minPtP) : 0), path.size()));
    lck.unlock();

    float d = 0;
    for(float[] c: cLst.subList(1, cLst.size())){
      d = PtoP(c, pathSeg.getLast());
      for(float[] p: pathSeg)
        if(PtoP(c, p) < d)
          d = PtoP(c, p);

      distLst.add(d);
    }

    System.out.println(path.getLast()[0] + " " + path.getLast()[1] + " dst: " +
      PtoP(cLst.get(1), path.getLast()));

    return(distLst);
  }

  // fill path
  public void run(){
    boolean blockedOnPrevIter = false;
    float[] pathLastPoint;
    LinkedList<float[]> pathTmp = new LinkedList<float[]>();
    ArrayList<float[]> cLst;

    try{
      for(;;){
        pathLastPoint = path.getLast();
        cLst = cTr.trainPose();
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

          Thread.sleep(250/(long) CtrlSys.timeMult);
        }
      }
      catch(InterruptedException ex){}

  }
}
