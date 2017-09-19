package com.michaeltroger.prediction1;

// Generated with Weka 3.8.1
// This code is public domain and comes with no warranty.
// Timestamp: Sat Jun 17 12:53:33 CEST 2017

class WekaClassifier {

  public static double classify(Object[] i)
    throws Exception {

    double p = Double.NaN;
    p = WekaClassifier.N167cbf650(i);
    return p;
  }
  static double N167cbf650(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 1;
    } else if (((Double) i[0]).doubleValue() <= 785.685759) {
    p = WekaClassifier.N134f97d21(i);
    } else if (((Double) i[0]).doubleValue() > 785.685759) {
      p = 2;
    } 
    return p;
  }
  static double N134f97d21(Object []i) {
    double p = Double.NaN;
    if (i[0] == null) {
      p = 0;
    } else if (((Double) i[0]).doubleValue() <= 329.764526) {
    p = WekaClassifier.N3918309f2(i);
    } else if (((Double) i[0]).doubleValue() > 329.764526) {
      p = 1;
    } 
    return p;
  }
  static double N3918309f2(Object []i) {
    double p = Double.NaN;
    if (i[2] == null) {
      p = 0;
    } else if (((Double) i[2]).doubleValue() <= 51.924982) {
    p = WekaClassifier.N37c51e063(i);
    } else if (((Double) i[2]).doubleValue() > 51.924982) {
      p = 0;
    } 
    return p;
  }
  static double N37c51e063(Object []i) {
    double p = Double.NaN;
    if (i[24] == null) {
      p = 0;
    } else if (((Double) i[24]).doubleValue() <= 0.650227) {
    p = WekaClassifier.N44b1180c4(i);
    } else if (((Double) i[24]).doubleValue() > 0.650227) {
    p = WekaClassifier.N53cbb77d6(i);
    } 
    return p;
  }
  static double N44b1180c4(Object []i) {
    double p = Double.NaN;
    if (i[1] == null) {
      p = 0;
    } else if (((Double) i[1]).doubleValue() <= 15.160101) {
    p = WekaClassifier.N2b2fe64f5(i);
    } else if (((Double) i[1]).doubleValue() > 15.160101) {
      p = 0;
    } 
    return p;
  }
  static double N2b2fe64f5(Object []i) {
    double p = Double.NaN;
    if (i[6] == null) {
      p = 0;
    } else if (((Double) i[6]).doubleValue() <= 2.036786) {
      p = 0;
    } else if (((Double) i[6]).doubleValue() > 2.036786) {
      p = 1;
    } 
    return p;
  }
  static double N53cbb77d6(Object []i) {
    double p = Double.NaN;
    if (i[9] == null) {
      p = 2;
    } else if (((Double) i[9]).doubleValue() <= 2.659628) {
      p = 2;
    } else if (((Double) i[9]).doubleValue() > 2.659628) {
    p = WekaClassifier.N4c61a62c7(i);
    } 
    return p;
  }
  static double N4c61a62c7(Object []i) {
    double p = Double.NaN;
    if (i[14] == null) {
      p = 1;
    } else if (((Double) i[14]).doubleValue() <= 9.785798) {
      p = 1;
    } else if (((Double) i[14]).doubleValue() > 9.785798) {
      p = 2;
    } 
    return p;
  }
}

