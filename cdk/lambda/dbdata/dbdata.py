def main(event, context):
  import logging as log
  import boto3
  import psycopg2
  from botocore.exceptions import ClientError
  import secrets
  import json
  from urllib.parse import unquote_plus
  from zipfile import ZipFile
  import os

  log.getLogger().setLevel(log.INFO)

  physical_id = 'mcorpus-db-data-ingest'
  region_name = 'us-west-2'
  
  response_obj = {}

  try:
    log.info('Input event: %s', event)

    db_json_secret_arn = os.environ['DbJsonSecretArn']

    s3_client = boto3.client('s3')

    record = event['Records'][0]
    bucket = record['s3']['bucket']['name']
    key = unquote_plus(record['s3']['object']['key'])
    
    mdata_zip_path = '/tmp/mdata.zip'
    
    # download
    s3_client.download_file(bucket, key, mdata_zip_path)

    member_csv = '/tmp/member.csv'
    mauth_csv = '/tmp/mauth.csv'
    maddress_csv = '/tmp/maddress.csv'
    
    session = boto3.session.Session()

    secrets_client = session.client(
      service_name = 'secretsmanager',
      region_name = region_name
    )

    # pull the db rds secret json object
    try:
      get_secret_value_response = secrets_client.get_secret_value(
        SecretId = db_json_secret_arn
      )
    except ClientError as e:
      log.error(e)
      response_obj['status'] = "ERROR (DB CRED)"
      return response_obj
    else:
      secret = json.loads(get_secret_value_response['SecretString'])
      db_host = secret['host']
      db_port = secret['port']
      db_name = secret['dbname']
      db_masteruser = secret['username']
      db_masterpswd = secret['password']
      log.info("db: {dbHost}:{dbPort}/{dbName} ({dbMasterUser})".format(
        dbHost = db_host, 
        dbPort = db_port, 
        dbName = db_name, 
        dbMasterUser = db_masteruser
      ))

      try:
        # db connect
        conn = psycopg2.connect(
          host = db_host, 
          database = db_name, 
          user = db_masteruser, 
          password = db_masterpswd
        )          
        cursor = conn.cursor()
        log.info('db connected')

        # delete existing member records
        cursor.execute("DELETE FROM maddress;")
        cursor.execute("DELETE FROM mauth;")
        cursor.execute("DELETE FROM member;")
        log.info('existing db data cleared out')

        # ingest csv db data
        # member
        with ZipFile(mdata_zip_path, 'r') as zipObj:
          zipObj.extract('member.csv', '/tmp')
        with open(member_csv, 'r') as f:
          cursor.copy_expert("COPY member from STDIN CSV HEADER NULL '\\N';", f)
        os.remove(member_csv)
        log.info('member data copied')

        # mauth
        with ZipFile(mdata_zip_path, 'r') as zipObj:
          zipObj.extract('mauth.csv', '/tmp')
        with open(mauth_csv, 'r') as f:
          cursor.copy_expert("COPY mauth from STDIN CSV HEADER NULL '\\N';", f)
        os.remove(mauth_csv)
        log.info('mauth data copied')

        # maddress
        with ZipFile(mdata_zip_path, 'r') as zipObj:
          zipObj.extract('maddress.csv', '/tmp')
        with open(maddress_csv, 'r') as f:
          cursor.copy_expert("COPY maddress from STDIN CSV HEADER NULL '\\N';", f)
        os.remove(maddress_csv)
        log.info('maddress data copied')

        # commit
        conn.commit()
        log.info('committed db alterations ok')

      except Exception as e:
        log.error(e)
        response_obj['status'] = "ERROR (DB)"
        return response_obj
      finally:
        if(cursor):
          cursor.close()
        if(conn):
          conn.close()

      # success
      log.info('mcorpus member data ingested from downloaded files ok')
      response_obj['status'] = "OK"
      return response_obj

  except Exception as e:
    log.exception(e)
    response_obj['status'] = "ERROR"
    return response_obj
