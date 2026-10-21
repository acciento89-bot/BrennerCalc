package de.kamilunavo.approvedcalcqa;
/** Keep both ends of a synthetic swipe inside the actual visible scroll pane. */
final class NativeScrollGesture {
 static int[] plan(int width,int height,int left,int top,int right,int bottom,boolean up){
  left=Math.max(0,left);top=Math.max(0,top);right=Math.min(width,right);bottom=Math.min(height,bottom);
  if(width<=0 || height<=0 || right-left<2 || bottom-top<4)return null;
  int x=left+(right-left)/2,span=bottom-top,low=top+span/4,high=bottom-1-span/4;
  return new int[]{x,up?low:high,x,up?high:low};
 }
}
