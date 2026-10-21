package de.kamilunavo.approvedcalcqa;
public final class NativeScrollGestureTest {
 static void inside(int[] p,int l,int t,int r,int b){if(p==null || p[0]<l || p[0]>=r || p[2]<l || p[2]>=r || p[1]<t || p[1]>=b || p[3]<t || p[3]>=b)throw new AssertionError("Gesture leaves actual scroll viewport");}
 public static void main(String[] args){
  // Exact actual missing-value.xml viewport at failure: rotation1, display2400x1080.
  inside(NativeScrollGesture.plan(2400,1080,911,263,1625,765,false),911,263,1625,765);
  inside(NativeScrollGesture.plan(2400,1080,911,263,1625,765,true),911,263,1625,765);
  inside(NativeScrollGesture.plan(1080,2400,183,522,897,1887,false),183,522,897,1887);
  inside(NativeScrollGesture.plan(1080,2400,-40,-20,600,900,false),0,0,600,900);
  if(NativeScrollGesture.plan(1080,2400,10,10,10,20,false)!=null)throw new AssertionError("Empty viewport must not swipe");
  if(NativeScrollGesture.plan(1080,2400,20,2500,800,3000,false)!=null)throw new AssertionError("Offscreen viewport must not swipe");
  System.out.println("6 native gesture geometry cases passed");
 }
}
