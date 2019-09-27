# mcorpus mock member data
Herein are the genmock groovy scripts that generate mock member data csv records
and the *baseline mock member csv data* files zipped in the mock-mcorpus-data.zip file.

The [mock-mcorpus-data.zip](https://mcorpus-db-data-bucket.s3-us-west-2.amazonaws.com/mock-mcorpus-data.zip) was removed from SCM and now lives in an AWS S3 bucket.

## mock-mcorpus-data.zip contents
There are three csv files that together comprise mock member data.  

*All csv files are comma-delimited and each value may or may not be quoted with a double-quote character.*  

*NULL values are denoted by '\N' in the csv files.*  

*timestamp values may be denoted by 'now'*  

1. **member.csv**  
   Contains header row:  
   `mid,created,modified,emp_id,location,name_first,name_middle,name_last,display_name,status`
   There are 1,002,149 member record rows after the header row.  

2. **mauth.csv**  
   Contains header row:  
   `mid,modified,dob,ssn,email_personal,email_work,mobile_phone,home_phone,work_phone,fax,username,pswd`
   The complimenting member auth data records where each data row corresponds to a row in member.csv keyed by the mid (member pk).  
   All pswd values are encrypted.  (Good luck)
   There are 1,002,149 mauth record rows after the header row.  

3. **maddress.csv**  
   Contains header row:  
   `mid,address_name,modified,attn,street1,street2,city,state,postal_code,country`
   The related many member address records keyed my mid.  
   There are 1,903,076 member address record rows after the header row.  
