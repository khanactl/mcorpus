/*
 * mcorpus data stub script.
 * 
 * Author:      jkirton
 * Date:        10/17/2017
 */
import java.util.Random;

println 'mcorpus-data-stub.groovy START..'

Random rand = new Random()

// member - master file holding the member PKs (uuid)
def fnameMember = 'mock-member.csv'
println 'Loading ' + fnameMember + '..'
def fMember = new File('./' + fnameMember)
def mrows = fMember.readLines()
def mrowsSize = mrows.size()
if(mrowsSize < 1) {
  println 'No member rows found.'
  return
}
println mrowsSize + ' root member rows loaded.'

// prep: mauth
def authRows = null
def authSize = 0
def authRowsWithMid = null
def fnameAuth = 'mock-mauth.csv'
def fnameAuthWithMid = 'mock-mauth-mid.csv'
def File fAuthWithMid = new File('./' + fnameAuthWithMid)
if(fAuthWithMid.exists()) {
  fAuthWithMid = null
}
else {
  def fAuth = new File('./' + fnameAuth)
  authRows = fAuth.readLines()
  authSize = authRows.size()
  if(authSize != mrowsSize) {
    println 'Invalid number of member auth rows.'
    return
  }
  authRowsWithMid = new ArrayList(authSize)
  println 'Loaded ' + authSize + ' member auth rows.'
}

// prep: maddress
def adrsRows = null
def adrsSize = 0
def adrsRowsWithMid = null
def fnameAdrsWithMid = 'mock-maddress-mid.csv'
def File fAdrsWithMid = new File('./' + fnameAdrsWithMid)
if(fAdrsWithMid.exists()) {
  fAdrsWithMid = null
}
else {
  def fnameAdrs = 'mock-maddress.csv'
  def fAdrs = new File('./' + fnameAdrs)
  adrsRows = fAdrs.readLines()
  adrsSize = adrsRows.size()
  if(adrsSize != mrowsSize) {
    println 'Invalid number of member address rows.'
    return
  }
  adrsRowsWithMid = new ArrayList(adrsSize)
  println 'Loaded ' + adrsSize + ' member address rows.'
}

// prep: mbenefits
def benRows = null
def benSize = 0
def benRowsWithMid = null
def fnameBenWithMid = 'mock-mbenefits-mid.csv'
def File fBenWithMid = new File('./' + fnameBenWithMid)
if(fBenWithMid.exists()) {
  fBenWithMid = null
}
else {
  def fnameBen = 'mock-mbenefits.csv'
  def fBen = new File('./' + fnameBen)
  benRows = fBen.readLines()
  benSize = benRows.size()
  if(benSize != mrowsSize) {
    println 'Invalid number of member benefit rows.'
    return
  }
  benRowsWithMid = new ArrayList(benSize)
  println 'Loaded ' + benSize + ' member benefit rows.'
}


/*
 * genrowBenefits
 */
def genrowBenefits = { String mid, String row, int mindex ->  
  String[] cols = row.split(',')
  int numCols = cols.size()
  println "benefits row: '" + row + "' numCols: " + numCols

  // validate
  if(numCols != 11) {
    println "Invalid mbenefits row."
    return null
  }

  // set the MCB
  def mcb = Math.abs(rand.nextInt(300001)) + 10000
  cols[2] = mcb

  // pre-pend the mid
  def colist = []
  colist.addAll(cols)
  colist.add(0, mid)
  
  def genrow = colist.join(",")
  return genrow
}

// for each [master] member row..
mrows.eachWithIndex { mline, mcount ->
  if(mcount == 0) {
    println mline
  }
  else {
    def cols = mline.split(",")
    def mid = cols[0]
    println 'mid: ' + mid
    
    // mauth
    if(authSize > 0) {
      def authRow = authRows.get(mcount)
      def authRowWithMid = mid + ',' + authRow
      authRowsWithMid.add(authRowWithMid)
    }

    // maddress
    if(adrsSize > 0) {
      def adrsRow = adrsRows.get(mcount)
      def adrsRowWithMid = mid + ',' + adrsRow
      adrsRowsWithMid.add(adrsRowWithMid)
    }
    
    // mbenefits
    if(benSize > 0) {
      def benRow = benRows.get(mcount)
      // ** genrowBenefits **
      def genrow = genrowBenefits(mid, benRow, mcount)
      println genrow
      benRowsWithMid.add(genrow)
    }
    
  }
}

if(fAuthWithMid != null) {
  println 'Writing ' + fnameAuthWithMid + '..'
  fAuthWithMid.withWriter('UTF-8') { fAuthWriter ->
    authRowsWithMid.each { line ->
      fAuthWriter.writeLine(line)
    }
  }
  println fnameAuthWithMid + ' written.'
}

if(fAdrsWithMid != null) {
  println 'Writing ' + fnameAdrsWithMid + '..'
  fAdrsWithMid.withWriter('UTF-8') { fAdrsWriter ->
    adrsRowsWithMid.each { line ->
      fAdrsWriter.writeLine(line)
    }
  }
  println fnameAdrsWithMid + ' written.'
}

if(fBenWithMid != null) {
  println 'Writing ' + fnameBenWithMid + '..'
  fBenWithMid.withWriter('UTF-8') { fBenWriter ->
    benRowsWithMid.each { line ->
      fBenWriter.writeLine(line)
    }
  }
  println fnameBenWithMid + ' written.'
}

println 'mcorpus-data-stub.groovy FINISHED.'