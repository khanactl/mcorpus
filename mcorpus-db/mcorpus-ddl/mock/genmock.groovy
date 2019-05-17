/*
 * Generate mock member data for mcorpus db.
 * 
 * Author:      jkirton
 * Date:        8/19/2018
 *
 * psql>
 *   COPY member(mid,emp_id,location,name_first,name_middle,name_last,display_name) FROM '/Users/d2d/dev/mcorpus/mcorpus-db/mcorpus-ddl/mock/mout-member.csv' DELIMITER ',' CSV HEADER;
 *   COPY mauth(mid,dob,ssn,email_personal,email_work,mobile_phone,home_phone,work_phone,fax,username,pswd) FROM '/Users/d2d/dev/mcorpus/mcorpus-db/mcorpus-ddl/mock/mout-mauth.csv' DELIMITER ',' CSV HEADER;
 *   COPY maddress(mid,address_name,attn,street1,street2,city,state,postal_code,country) FROM '/Users/d2d/dev/mcorpus/mcorpus-db/mcorpus-ddl/mock/mout-maddress.csv' DELIMITER ',' QUOTE '"' CSV HEADER;
 */

import java.util.Random;
import java.util.UUID;
import java.util.regex.*

println 'genmock STARTED in dir: ' + System.getProperty("user.dir")

class StCity {
  String st
  String city

  StCity(String st, String city) {
    this.st = st
    this.city = city
  }

  String toString() { "ST: $st, city: $city" }
}

class Data {
  Random rand = new Random()
  
  String alphas = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
  String alphanums = alphas + "0123456789"
  String pswdsoup = alphanums + "[{]}=+-_)(*&^%\$#@!"

  List addressNames = ['home', 'work', 'other']

  List locations = [ '01', '02', '03', '04', '05', '06', '07', '08', '09', '98', '20' ]
  
  Date dobStart = Date.parse('yyyy-MM-dd', '1931-01-01')
  Date dobEnd = Date.parse('yyyy-MM-dd', '2002-01-01')

  List surnames
  List firstnames
  List emailDomains
  Map areaCodes
  Map cityStates
  List states
  List streetnames
  Map zipz

  boolean isEmpty(String s) { return s == null || s.isEmpty() }
  boolean has2Chars(String s) { return s.length() > 1 }
  String ctrim(String s) { return s == null ? "" : s.trim() }

  Integer rint(Integer upper) { return rand.nextInt(upper) }
  
  Data() {
    println '\nLoading data..'
    
    this.surnames = loadSurnames()
    println 'Num surnames loaded: ' + surnames.size()

    this.firstnames = loadFirstnames()
    println 'Num firstnames loaded: ' + firstnames.size()

    this.emailDomains = loadEmailDomains()
    println 'Num email domains loaded: ' + emailDomains.size()

    this.areaCodes = loadAreaCodesByState()
    println 'Num area codes loaded: ' + areaCodes.size()

    this.cityStates = loadCityStates()
    this.states = extractStates()
    println 'Num states loaded: ' + states.size()

    this.streetnames = loadStreetnames()
    println 'Num streetnames loaded: ' + streetnames.size()
    
    this.zipz = loadZipz()
    println 'Num zipz loaded: ' + zipz.size()
    
    println 'Data loaded.\n\n'
  }

  Map loadZipz() {
    def f = new File('./genmock-zipz.txt')
    def map = new HashMap()
    def lines = f.readLines()
    lines.each { line -> 
      String[] sline = line.split("\\t")
      if(sline != null && sline.length == 2) {
        String zippfx = ctrim(sline[0])
        String cityst = ctrim(sline[1])
        Matcher m_cityst = cityst =~ /^([A-Z]{2}|Not in use|Destinations outside U\.S\.)(.*)$/
        // println "zippfx: $zippfx, cityst: $cityst - m_cityst.hasGroup(): ${m_cityst.hasGroup()}, m_cityst.groupCount(): ${m_cityst.groupCount()}"
        if(m_cityst.matches() && m_cityst.hasGroup() && m_cityst.groupCount() == 2) {
          // we have a valid ST city
          def st = ctrim(m_cityst.group(1))
          def city = ctrim(m_cityst.group(2))
          if(st.length() > 0 && city.length() > 0) {
            StCity stcity = new StCity(st, city)
            map.put(zippfx, stcity)
            // println "Added - zippfx: $zippfx, stcity: $stcity"
          }
        }
      }
    }
    return map
  }

  List loadStreetnames() {
    def f = new File('./genmock-streetnames.txt')
    def list = new ArrayList();
    def lines = f.readLines()
    lines.each { line -> list.add(line) }
    return list
  }

  /**
   * @return map of popular citiy names keyed by ST abbr.
   */
  Map loadCityStates() {
    def f = new File('./genmock-city-states.txt')
    def map = new HashMap()
    def lines = f.readLines()
    lines.each { line -> 
      String[] sline = line.split("\\t")
      if(sline != null && sline.length == 2) {
        def city = sline[0]
        def st = sline[1]
        if(!map.containsKey(st)) {
          map.put(st, new ArrayList())
        }
        map.get(st).add(city)
      }
    }
    return map
  }

  /**
   * Depends on the cityStates map to be loaded.
   */
  List extractStates() {
    def list = new ArrayList()
    for(String st : cityStates.keySet()) {
      list.add(st)
    }
    return list
  }

  /**
   * @return a map of areacode lists keyed by 2-char ST abbrev strings.
   */
  Map loadAreaCodesByState() {
    def f = new File('./genmock-areacodes-by-st.txt')
    def map = new HashMap()
    def lines = f.readLines()
    lines.each { line -> 
      String[] sline = line.split("\\t")
      if(sline != null && sline.length == 2) {
        def st = sline[0]
        def listAreaCodes = sline[1].replace(" ", "").split(",")
        map.put(st, listAreaCodes)
      }
    }
    return map
  }

  List loadSurnames() {
    def f = new File('./genmock-surnames.txt')
    def list = new ArrayList();
    def lines = f.readLines()
    lines.each { line -> list.add(line) }
    return list
  }

  List loadFirstnames() {
    def f = new File('./genmock-firstnames.txt')
    def list = new ArrayList();
    def lines = f.readLines()
    lines.each { line -> list.add(line) }
    return list
  }

  List loadEmailDomains() {
    def f = new File('./genmock-emaildomains.txt')
    def list = new ArrayList();
    def lines = f.readLines()
    lines.each { line -> list.add(line) }
    return list
  }

  String randomEmpId() {
    return String.format("%02d-%07d",
      rint(100) + 1,
      rint(10000000 - 1) + 1
    );
  }

  String cap(String s) {
    return isEmpty(s) ? 
      "" : has2Chars(s) ? 
        (Character.toUpperCase(s.charAt(0)).toString() + s.substring(1).toLowerCase()) : s.charAt(0)
  }

  String randomLocation() {
    return locations[rint(locations.size())];
  }

  String randomSurname() {
    return cap(surnames.get(rint(surnames.size())))
  }

  String randomFirstname() {
    return cap(firstnames.get(rint(firstnames.size())))
  }

  Date randomDate(Range<Date> range) {
    return range.from + rint(range.to - range.from + 1)
  }

  Date randomDob() {
    return randomDate(dobStart..dobEnd)
  }

  String randomSsn() {
    return String.format("%03d%02d%04d", rint(999), rint(99), rint(9999))
  }

  String randomEmailDomain() {
    return emailDomains.get(rint(emailDomains.size()))
  }

  String randomUsername() {
    return String.format("%s%s%s%s%s%s%s%s", 
      alphas.charAt(rint(52)),
      alphas.charAt(rint(52)),
      alphas.charAt(rint(52)),
      alphas.charAt(rint(52)),
      alphas.charAt(rint(52)),
      alphas.charAt(rint(52)),
      alphas.charAt(rint(52)),
      alphas.charAt(rint(52))
    )
  }

  String randomPassword() {
    def len = pswdsoup.length()
    return String.format("%s%s%s%s%s%s%s%s", 
      pswdsoup.charAt(rint(len)),
      pswdsoup.charAt(rint(len)),
      pswdsoup.charAt(rint(len)),
      pswdsoup.charAt(rint(len)),
      pswdsoup.charAt(rint(len)),
      pswdsoup.charAt(rint(len)),
      pswdsoup.charAt(rint(len)),
      pswdsoup.charAt(rint(len))
    )
  }

  String randomState() {
    return states[rint(states.size())]
  }

  String randomCity(String st) {
    List cities = cityStates.get(st)
    return cities.get(rint(cities.size()))
  }

  String randomZip(String st, String city) {
    String firstZipPfx = null
    for(e in zipz) {
      if(e.value.st == st) {
        if(firstZipPfx == null) firstZipPfx = e.key
        if(e.value.city.toLowerCase().contains(city.toLowerCase())) {
          // println 'Found zip by st and city.'
          return String.format("%s%d%d", e.key, rint(10), rint(10))
        }
      }
    }
    // no exact match on both city and state so fallback on first found zip prefix with matching state
    // println 'Found zip by st only.'
    return String.format("%s%d%d", firstZipPfx, rint(10), rint(10))
  }

  String randomAreaCode(String st) {
    List areacodes = areaCodes.get(st)
    return areacodes.get(rint(areacodes.size()))
  }

  String[] randomPhone4(String areaCode) {
    Integer pfx = rint(999 - 100) + 100
    return [
      String.format("%s-%d-%04d", areaCode, pfx, rint(9999)),
      String.format("%s-%d-%04d", areaCode, pfx, rint(9999)),
      String.format("%s-%d-%04d", areaCode, pfx, rint(9999)),
      String.format("%s-%d-%04d", areaCode, pfx, rint(9999))
    ]
  }

  String randomStreetAddress() {
    return String.format("%d %s", rint(99999), streetnames[rint(streetnames.size())])
  }

} // Data class

class Member {
  UUID mid
  String nameFirst
  String nameMiddle
  String nameLast
  String empId
  String location
  String displayName
  String dob
  String ssn
  String emailPersonal
  String emailWork
  String phoneMobile
  String phoneHome
  String phoneWork
  String fax
  String username
  String pswd
  List addresses

  /**
   * Constructor.
   *
   * @param d the loaded data to use
   */
  Member(Data d) {
    this.mid = UUID.randomUUID()
    
    this.nameFirst = d.randomFirstname()
    this.nameMiddle = d.randomFirstname()
    this.nameLast = d.randomSurname()
    
    this.empId = d.randomEmpId()
    this.location = d.randomLocation()
    
    this.displayName = String.format("%s%s%d", 
      Character.toLowerCase(this.nameFirst.charAt(0)),
      this.nameLast.toLowerCase(),
      d.rint(100))
    
    this.dob = d.randomDob()
    this.ssn = d.randomSsn()
    
    this.emailPersonal = this.nameFirst.toLowerCase() + "@" + d.randomEmailDomain()
    this.emailWork = this.displayName + "@" + d.randomEmailDomain()
    
    this.username = d.randomUsername()
    this.pswd = d.randomPassword()

    String st = d.randomState()
    String city = d.randomCity(st)

    String areaCode = d.randomAreaCode(st)
    String[] phones = d.randomPhone4(areaCode)
    this.phoneMobile = phones[0]
    this.phoneWork = phones[1]
    this.phoneHome = phones[2]
    this.fax = phones[3]
    
    def numAddresses = d.rint(3) + 1 // i.e. 1 - 3
    this.addresses = new ArrayList(numAddresses)
    for(int i = 0; i < numAddresses; i++) {
      String addressName = d.addressNames[i]
      String attn = this.nameFirst
      String street1 = d.randomStreetAddress()
      String street2 = ''
      String postalCode = d.randomZip(st, city)
      String country = 'US'
      MAddress address = new MAddress(this.mid, addressName, attn, street1, street2, city, st, postalCode, country)
      this.addresses.add(address)
    }
  }

  static void moutHeaders(FileWriter fwMem, FileWriter fwMaut, fwAdd) {
    if(fwMem != null) fwMem.write("mid,emp_id,location,name_first,name_middle,name_last,display_name\n")
    if(fwMaut != null) fwMaut.write("mid,dob,ssn,email_personal,email_work,mobile_phone,home_phone,work_phone,fax,username,pswd\n")
    if(fwAdd != null) fwAdd.write("mid,address_name,attn,street1,street2,city,state,postal_code,country\n")
  }

  static void moutMember(Member m, FileWriter fwMem, FileWriter fwMaut, fwAdd) {
    // member
    String smem = String.format("%s,%s,%s,%s,%s,%s,%s\n", m.mid, m.empId, m.location, m.nameFirst, m.nameMiddle, m.nameLast, m.displayName)
    fwMem.write(smem)
    
    // mauth
    String smaut = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n", m.mid, m.dob, m.ssn, m.emailPersonal, m.emailWork, m.phoneMobile, m.phoneHome, m.phoneWork, m.fax, m.username, m.pswd)
    fwMaut.write(smaut)
    
    // maddress
    if(m.addresses != null) {
      for(MAddress ma : m.addresses) {
        String sadd = String.format("%s,%s,%s,\"%s\",\"%s\",%s,%s,%s,%s\n", m.mid, ma.addressName, ma.attn, ma.street1, ma.street2, ma.city, ma.state, ma.postalCode, ma.country)
        fwAdd.write(sadd)
      }
    }
  }

  /**
   * The output workhorse method for generating import ready member db records.
   *
   * @param d the data object
   * @param n the number of records to generate
   */
  static void mout(Data d, int n) {    
    File fmem = new File('mout-member.csv')
    boolean fmemExists = fmem.exists()
    if(!fmemExists) fmem.createNewFile()
    FileWriter fwMem = new FileWriter(fmem, fmemExists)
    
    File fmaut = new File('mout-mauth.csv')
    boolean fmautExists = fmaut.exists()
    if(!fmautExists) fmaut.createNewFile()
    FileWriter fwMaut = new FileWriter(fmaut, fmautExists)
    
    File fadd = new File('mout-maddress.csv')
    boolean faddExists = fadd.exists()
    if(!faddExists) fadd.createNewFile()
    FileWriter fwAdd = new FileWriter(fadd, faddExists)

    // output headers dependening on if the file exists already or not
    // if the file exists, assume the headers are already present 
    moutHeaders(
      fmemExists ? null : fwMem, 
      fmautExists ? null : fwMaut, 
      faddExists ? null : fwAdd
    )
    
    for(int i = 0; i < n; i++) {
      Member m = new Member(d)
      moutMember(m, fwMem, fwMaut, fwAdd)
    }    
    
    fwMem.flush()
    fwMem.close()

    fwMaut.flush()
    fwMaut.close()

    fwAdd.flush()
    fwAdd.close()
  }

  String toString() {
    "mid: ${mid}\nname: ${nameFirst} ${nameMiddle} ${nameLast}\nempId: ${empId}\nLoc: ${location}\ndisplayName: ${displayName}\ndob: ${dob}\nssn: ${ssn}\nemails: ${emailPersonal},${emailWork}\nPhones: ${phoneMobile},${phoneWork},${phoneHome},${fax}\nusername: ${username}\npswd: ${pswd}\nAddresses:\n${addresses}"
  }
}

class MAddress {
  UUID mid
  String addressName
  String attn
  String street1
  String street2
  String city
  String state
  String postalCode
  String country

  /**
   * Constructor.
   */
  MAddress(UUID mid, String addressname, String attn, String street1, String street2, String city, String st, String postalCode, String country) {
    this.mid = mid
    this.addressName = addressname
    this.attn = attn
    this.street1 = street1
    this.street2 = street2
    this.city = city
    this.state = st
    this.postalCode = postalCode
    this.country = country
  }

  public String toString() {
    "${addressName} - attn: ${attn}, Address: ${street1} - ${street2}, city: ${city}, ST: ${state}, Zip: ${postalCode}, Country: ${country}"
  }
}

// go
def nstr = System.console().readLine 'Generate how many member records? '
Integer n = Integer.parseInt(nstr)
Data d = new Data()
Member.mout(d, n)
println "$n CSV member records generated."
